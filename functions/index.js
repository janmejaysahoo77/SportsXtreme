const crypto = require("crypto");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { HttpsError, onCall } = require("firebase-functions/v2/https");

initializeApp();

const db = getFirestore();
const INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/team-invite?token=";
const TEAM_INVITE_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const MAX_TEAM_MEMBERS = 18;
const ROLES = Object.freeze({ ADMIN: "ADMIN", CAPTAIN: "CAPTAIN", VICE_CAPTAIN: "VICE_CAPTAIN" });

function memberRoles(member) {
  if (Array.isArray(member?.roles)) return member.roles.filter((role) => Object.values(ROLES).includes(role));
  if (member?.role === "OWNER") return [ROLES.ADMIN, ROLES.CAPTAIN]; // legacy teams
  if (member?.role === "CAPTAIN") return [ROLES.CAPTAIN];
  if (member?.role === "VICE_CAPTAIN") return [ROLES.VICE_CAPTAIN];
  return [];
}
function hasRole(member, role) { return memberRoles(member).includes(role); }
function findMember(members, userId) { return members.find((member) => member && member.userId === userId); }
function isManager(member) { return hasRole(member, ROLES.ADMIN) || hasRole(member, ROLES.CAPTAIN); }
function requireTeamId(value) {
  const teamId = typeof value === "string" ? value.trim() : "";
  if (!teamId || teamId.length > 150) throw new HttpsError("invalid-argument", "A valid team ID is required.");
  return teamId;
}
function storedMember(member, roles = memberRoles(member), playingRole = member?.playingRole || "PLAYER") {
  return { userId: member.userId, roles: [...new Set(roles)], playingRole, joinedAtEpochMs: member.joinedAtEpochMs || Date.now() };
}
async function teamInTransaction(transaction, teamId) {
  const ref = db.collection("teams").doc(teamId);
  const snapshot = await transaction.get(ref);
  if (!snapshot.exists) throw new HttpsError("not-found", "Team not found.");
  return { ref, members: Array.isArray(snapshot.get("members")) ? snapshot.get("members") : [] };
}

/**
 * Returns only the display data needed by a team roster. User profiles remain
 * private in Firestore; callers must already belong to the requested team.
 */
exports.getTeamMemberProfiles = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before viewing team members.");
  const teamId = requireTeamId(request.data?.teamId);
  const team = await db.collection("teams").doc(teamId).get();
  if (!team.exists) throw new HttpsError("not-found", "Team not found.");
  const members = Array.isArray(team.get("members")) ? team.get("members") : [];
  if (!findMember(members, request.auth.uid)) {
    throw new HttpsError("permission-denied", "Only team members can view this roster.");
  }
  const userSnapshots = await db.getAll(
    ...members.map((member) => db.collection("users").doc(member.userId))
  );
  return {
    members: members.map((member, index) => {
      const profile = userSnapshots[index];
      const displayName = profile.exists && typeof profile.get("name") === "string"
        ? profile.get("name").trim()
        : "";
      const profilePlayingRole = profile.exists && typeof profile.get("role") === "string"
        ? profile.get("role").trim()
        : "";
      return {
        userId: member.userId,
        displayName: displayName || "Team member",
        playingRole: member.playingRole || profilePlayingRole || "Player"
      };
    })
  };
});

exports.createTeamInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before creating an invitation.");
  const teamId = requireTeamId(request.data?.teamId);
  const team = await db.collection("teams").doc(teamId).get();
  if (!team.exists) throw new HttpsError("not-found", "Team not found.");
  const members = Array.isArray(team.get("members")) ? team.get("members") : [];
  const caller = findMember(members, request.auth.uid);
  if (!isManager(caller) && !hasRole(caller, ROLES.VICE_CAPTAIN)) {
    throw new HttpsError("permission-denied", "Only a team admin, captain, or vice-captain can invite members.");
  }
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const token = crypto.randomBytes(32).toString("base64url");
    const inviteRef = db.collection("teamInvites").doc(crypto.createHash("sha256").update(token, "utf8").digest("hex"));
    try {
      await db.runTransaction(async (transaction) => {
        if ((await transaction.get(inviteRef)).exists) throw new Error("TOKEN_COLLISION");
        transaction.create(inviteRef, { teamId, ownerUserId: request.auth.uid, status: "OPEN", createdAt: FieldValue.serverTimestamp(), expiresAt: new Date(Date.now() + TEAM_INVITE_TTL_MS) });
      });
      return { invitationUrl: `${INVITE_URL_PREFIX}${encodeURIComponent(token)}` };
    } catch (error) {
      if (error?.message !== "TOKEN_COLLISION") throw error;
    }
  }
  throw new HttpsError("internal", "Unable to create a unique invitation. Please try again.");
});

exports.joinTeamInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before joining a team.");
  const token = typeof request.data?.token === "string" ? request.data.token.trim() : "";
  if (!/^[A-Za-z0-9_-]{43}$/.test(token)) throw new HttpsError("invalid-argument", "Invalid invitation link.");
  const inviteRef = db.collection("teamInvites").doc(crypto.createHash("sha256").update(token, "utf8").digest("hex"));
  const uid = request.auth.uid;
  try {
    return await db.runTransaction(async (transaction) => {
      const invite = await transaction.get(inviteRef);
      if (!invite.exists) throw new HttpsError("not-found", "Invitation is invalid.");
      const status = invite.get("status") || "OPEN";
      if (status === "USED" && invite.get("usedByUserId") !== uid) throw new HttpsError("failed-precondition", "This invitation has already been used.");
      if (status === "REVOKED" || (status !== "OPEN" && status !== "USED")) throw new HttpsError("failed-precondition", "This invitation is no longer valid.");
      const expiresAt = invite.get("expiresAt");
      if (expiresAt && expiresAt.toDate() <= new Date()) throw new HttpsError("deadline-exceeded", "This invitation has expired.");
      const teamId = requireTeamId(invite.get("teamId"));
      const { ref, members } = await teamInTransaction(transaction, teamId);
      const alreadyMember = Boolean(findMember(members, uid));
      if (!alreadyMember && members.length >= MAX_TEAM_MEMBERS) throw new HttpsError("failed-precondition", `A team can have at most ${MAX_TEAM_MEMBERS} players.`);
      const updated = alreadyMember ? members.map((member) => storedMember(member)) : [...members.map((member) => storedMember(member)), storedMember({ userId: uid })];
      if (!alreadyMember) transaction.update(ref, { members: updated, memberIds: updated.map((member) => member.userId), updatedAtEpochMs: Date.now() });
      if (status === "OPEN") transaction.update(inviteRef, { status: "USED", usedByUserId: uid, usedAt: FieldValue.serverTimestamp() });
      return { teamId, alreadyMember };
    });
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("joinTeamInvite failed", error);
    throw new HttpsError("internal", "Unable to join team. Please try again.");
  }
});

exports.updateTeamMemberRole = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before changing team roles.");
  const teamId = requireTeamId(request.data?.teamId);
  const targetUserId = typeof request.data?.targetUserId === "string" ? request.data.targetUserId.trim() : "";
  const teamRole = request.data?.teamRole;
  const playingRole = request.data?.playingRole;
  if (!targetUserId || !["CAPTAIN", "VICE_CAPTAIN", "PLAYER", undefined].includes(teamRole) || !["WICKET_KEEPER", "PLAYER", undefined].includes(playingRole) || (teamRole === undefined && playingRole === undefined)) throw new HttpsError("invalid-argument", "Invalid team role update.");
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    if (!isManager(findMember(members, request.auth.uid))) throw new HttpsError("permission-denied", "Only a team admin or captain can assign roles.");
    if (!findMember(members, targetUserId)) throw new HttpsError("not-found", "Team member not found.");
    let updated = members.map((member) => storedMember(member));
    if (teamRole !== undefined) {
      const appointment = teamRole === "PLAYER" ? null : teamRole;
      if (appointment) updated = updated.map((member) => member.userId === targetUserId ? member : { ...member, roles: member.roles.filter((role) => role !== appointment) });
      updated = updated.map((member) => member.userId !== targetUserId ? member : { ...member, roles: [...member.roles.filter((role) => role !== ROLES.CAPTAIN && role !== ROLES.VICE_CAPTAIN), ...(appointment ? [appointment] : [])] });
    }
    if (playingRole !== undefined) updated = updated.map((member) => member.userId === targetUserId ? { ...member, playingRole } : member);
    transaction.update(ref, { members: updated, updatedAtEpochMs: Date.now() });
    return { teamId, targetUserId };
  });
});

exports.removeTeamMember = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before removing a player.");
  const teamId = requireTeamId(request.data?.teamId);
  const targetUserId = typeof request.data?.targetUserId === "string" ? request.data.targetUserId.trim() : "";
  if (!targetUserId || targetUserId === request.auth.uid) throw new HttpsError("invalid-argument", "Use leaveTeam to leave a team.");
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    const actor = findMember(members, request.auth.uid);
    const target = findMember(members, targetUserId);
    if (!actor || !target) throw new HttpsError("not-found", "Team member not found.");
    const managerMayRemove = isManager(actor) && !hasRole(target, ROLES.ADMIN);
    const viceMayRemove = hasRole(actor, ROLES.VICE_CAPTAIN) && memberRoles(target).length === 0;
    if (!managerMayRemove && !viceMayRemove) throw new HttpsError("permission-denied", "You do not have permission to remove this member.");
    const updated = members.filter((member) => member.userId !== targetUserId).map((member) => storedMember(member));
    transaction.update(ref, { members: updated, memberIds: updated.map((member) => member.userId), updatedAtEpochMs: Date.now() });
    return { teamId, targetUserId };
  });
});

exports.resignTeamAdmin = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before resigning.");
  const teamId = requireTeamId(request.data?.teamId);
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    const actor = findMember(members, request.auth.uid);
    if (!hasRole(actor, ROLES.ADMIN)) throw new HttpsError("permission-denied", "Only an admin can resign as admin.");
    const captain = members.find((member) => member.userId !== request.auth.uid && hasRole(member, ROLES.CAPTAIN));
    if (!captain) throw new HttpsError("failed-precondition", "Assign another captain before resigning as admin.");
    const updated = members.map((member) => member.userId === request.auth.uid ? storedMember(member, memberRoles(member).filter((role) => role !== ROLES.ADMIN)) : member.userId === captain.userId ? storedMember(member, [...memberRoles(member), ROLES.ADMIN]) : storedMember(member));
    transaction.update(ref, { members: updated, updatedAtEpochMs: Date.now() });
    return { teamId, newAdminUserId: captain.userId };
  });
});

exports.leaveTeam = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before leaving a team.");
  const teamId = requireTeamId(request.data?.teamId);
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    const actor = findMember(members, request.auth.uid);
    if (!actor) throw new HttpsError("not-found", "Team member not found.");
    const remaining = members.filter((member) => member.userId !== request.auth.uid);
    const captain = hasRole(actor, ROLES.ADMIN) ? remaining.find((member) => hasRole(member, ROLES.CAPTAIN)) : null;
    if (hasRole(actor, ROLES.ADMIN) && !captain) throw new HttpsError("failed-precondition", "Assign a captain before an admin leaves the team.");
    const updated = remaining.map((member) => member.userId === captain?.userId ? storedMember(member, [...memberRoles(member), ROLES.ADMIN]) : storedMember(member));
    transaction.update(ref, { members: updated, memberIds: updated.map((member) => member.userId), updatedAtEpochMs: Date.now() });
    return { teamId, newAdminUserId: captain?.userId || null };
  });
});

/** Saves editable team details after checking the caller's live team role. */
exports.updateTeamProfile = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before editing a team.");
  const teamId = requireTeamId(request.data?.teamId);
  const allowedFields = new Set(["description", "founded", "homeGround", "captainName", "captainMobile", "coachManager", "teamMotto"]);
  const changes = request.data?.changes;
  if (!changes || typeof changes !== "object" || Array.isArray(changes)) throw new HttpsError("invalid-argument", "Invalid team details.");
  const keys = Object.keys(changes);
  if (keys.length !== 1 || !allowedFields.has(keys[0]) || typeof changes[keys[0]] !== "string") {
    throw new HttpsError("invalid-argument", "Invalid team detail.");
  }
  const value = changes[keys[0]].trim();
  if (value.length > 500) throw new HttpsError("invalid-argument", "Team detail is too long.");
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    if (!isManager(findMember(members, request.auth.uid))) throw new HttpsError("permission-denied", "Only a team admin or captain can edit team details.");
    transaction.update(ref, { [keys[0]]: value, updatedAtEpochMs: Date.now() });
    return { teamId };
  });
});
