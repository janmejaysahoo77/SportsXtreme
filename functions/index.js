const crypto = require("crypto");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { HttpsError, onCall } = require("firebase-functions/v2/https");

initializeApp();

const db = getFirestore();
const INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/team-invite?token=";
const TOURNAMENT_INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/tournament-invite?token=";
const TEAM_INVITE_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const TOURNAMENT_INVITE_TTL_MS = 14 * 24 * 60 * 60 * 1000;
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
  const authUsers = await Promise.all(members.map(async (member) => {
    try {
      return await getAuth().getUser(member.userId);
    } catch (_) {
      return null;
    }
  }));
  return {
    members: members.map((member, index) => {
      const profile = userSnapshots[index];
      const profileName = profile.exists
        ? [profile.get("name"), profile.get("displayName"), profile.get("fullName")]
          .find((value) => typeof value === "string" && value.trim())?.trim() || ""
        : "";
      const authName = authUsers[index]?.displayName?.trim() || "";
      const memberName = [member.displayName, member.name]
        .find((value) => typeof value === "string" && value.trim())?.trim() || "";
      const displayName = profileName || authName || memberName;
      const profilePlayingRole = profile.exists && typeof profile.get("role") === "string"
        ? profile.get("role").trim()
        : "";
      return {
        userId: member.userId,
        displayName: displayName || "",
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

/** Creates a reusable invitation that lets a captain register one of their teams. */
exports.createTournamentInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before creating an invitation.");
  const tournamentId = requireTeamId(request.data?.tournamentId);
  const tournament = await db.collection("tournaments").doc(tournamentId).get();
  if (!tournament.exists) throw new HttpsError("not-found", "Tournament not found.");
  if (tournament.get("hostUid") !== request.auth.uid) {
    throw new HttpsError("permission-denied", "Only the tournament organiser can invite teams.");
  }
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const token = crypto.randomBytes(32).toString("base64url");
    const inviteRef = db.collection("tournamentInvites").doc(crypto.createHash("sha256").update(token, "utf8").digest("hex"));
    try {
      await db.runTransaction(async (transaction) => {
        if ((await transaction.get(inviteRef)).exists) throw new Error("TOKEN_COLLISION");
        transaction.create(inviteRef, {
          tournamentId,
          organiserId: request.auth.uid,
          status: "OPEN",
          createdAt: FieldValue.serverTimestamp(),
          expiresAt: new Date(Date.now() + TOURNAMENT_INVITE_TTL_MS)
        });
      });
      return {
        invitationUrl: `${TOURNAMENT_INVITE_URL_PREFIX}${encodeURIComponent(token)}`,
        tournamentName: String(tournament.get("name") || "Tournament")
      };
    } catch (error) {
      if (error?.message !== "TOKEN_COLLISION") throw error;
    }
  }
  throw new HttpsError("internal", "Unable to create a unique invitation. Please try again.");
});

/** Deletes a tournament and its nested entries after verifying the organiser. */
exports.deleteTournament = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before deleting a tournament.");
  const tournamentId = requireTeamId(request.data?.tournamentId);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const tournament = await tournamentRef.get();
  if (!tournament.exists) throw new HttpsError("not-found", "Tournament not found.");
  if (tournament.get("hostUid") !== request.auth.uid) {
    throw new HttpsError("permission-denied", "Only the tournament organiser can delete it.");
  }

  const invites = await db.collection("tournamentInvites").where("tournamentId", "==", tournamentId).get();
  const batches = [];
  for (let index = 0; index < invites.docs.length; index += 500) {
    const batch = db.batch();
    invites.docs.slice(index, index + 500).forEach((invite) => batch.delete(invite.ref));
    batches.push(batch.commit());
  }
  await Promise.all(batches);
  await db.recursiveDelete(tournamentRef);
  return { tournamentId };
});

/** Deletes an organiser-owned match and all of its nested scoring data. */
exports.deleteMatch = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before deleting a match.");
  const matchId = requireTeamId(request.data?.matchId);
  const matchRef = db.collection("matches").doc(matchId);
  const match = await matchRef.get();
  if (!match.exists) throw new HttpsError("not-found", "Match not found.");
  const ownerId = match.get("ownerId") || match.get("organiserId");
  if (ownerId !== request.auth.uid) {
    throw new HttpsError("permission-denied", "Only the match organiser can delete it.");
  }

  const invites = await db.collection("matchInvites").where("matchId", "==", matchId).get();
  for (let index = 0; index < invites.docs.length; index += 500) {
    const batch = db.batch();
    invites.docs.slice(index, index + 500).forEach((invite) => batch.delete(invite.ref));
    await batch.commit();
  }
  await db.recursiveDelete(matchRef);
  return { matchId };
});

/** Registers a captain's team in a tournament. Membership and duplicate checks are transactional. */
exports.joinTournamentInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before joining a tournament.");
  const token = typeof request.data?.token === "string" ? request.data.token.trim() : "";
  if (!/^[A-Za-z0-9_-]{43}$/.test(token)) throw new HttpsError("invalid-argument", "Invalid invitation link.");
  const teamId = requireTeamId(request.data?.teamId);
  const inviteRef = db.collection("tournamentInvites").doc(crypto.createHash("sha256").update(token, "utf8").digest("hex"));
  return db.runTransaction(async (transaction) => {
    const invite = await transaction.get(inviteRef);
    if (!invite.exists || invite.get("status") !== "OPEN") throw new HttpsError("failed-precondition", "This invitation is no longer available.");
    const expiresAt = invite.get("expiresAt");
    if (expiresAt && expiresAt.toDate() <= new Date()) throw new HttpsError("deadline-exceeded", "This invitation has expired.");
    const tournamentId = requireTeamId(invite.get("tournamentId"));
    const tournamentRef = db.collection("tournaments").doc(tournamentId);
    const tournament = await transaction.get(tournamentRef);
    if (!tournament.exists) throw new HttpsError("not-found", "Tournament not found.");
    const { ref: teamRef, members } = await teamInTransaction(transaction, teamId);
    if (!isManager(findMember(members, request.auth.uid))) {
      throw new HttpsError("permission-denied", "Only a team captain or admin can register a team.");
    }
    const entryRef = tournamentRef.collection("teams").doc(teamId);
    const existingEntry = await transaction.get(entryRef);
    if (!existingEntry.exists) {
      const team = await transaction.get(teamRef);
      const maxTeams = Number.parseInt(String(tournament.get("requirements")?.numberOfTeams || ""), 10);
      const teamIds = Array.isArray(tournament.get("teamIds")) ? tournament.get("teamIds") : [];
      if (Number.isFinite(maxTeams) && maxTeams > 0 && teamIds.length >= maxTeams) {
        throw new HttpsError("failed-precondition", "This tournament has reached its team limit.");
      }
      const teamName = String(team.get("teamName") || team.get("name") || "Team").trim() || "Team";
      transaction.create(entryRef, { teamId, teamName, captainUserId: request.auth.uid, joinedAt: FieldValue.serverTimestamp() });
      transaction.update(tournamentRef, { teamIds: FieldValue.arrayUnion(teamId), updatedAtEpochMs: Date.now() });
    }
    return { tournamentId, tournamentName: String(tournament.get("name") || "Tournament"), alreadyJoined: existingEntry.exists };
  });
});

/** Resolves a match invitation without claiming the slot, so the captain can choose a team first. */
exports.resolveMatchInvite = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before joining a match.");
  const token = typeof request.data?.token === "string" ? request.data.token.trim() : "";
  if (!/^[A-Za-z0-9_-]{43}$/.test(token)) throw new HttpsError("invalid-argument", "Invalid match invitation.");
  const tokenHash = crypto.createHash("sha256").update(token, "utf8").digest("hex");
  const matches = await db.collection("matchInvites").where("tokenHash", "==", tokenHash).limit(2).get();
  if (matches.size !== 1) throw new HttpsError("not-found", "This match invitation is invalid.");
  const invite = matches.docs[0];
  if (invite.get("status") !== "OPEN") throw new HttpsError("failed-precondition", "This match slot is no longer available.");
  if (!invite.get("expiresAt") || invite.get("expiresAt").toDate() <= new Date()) {
    throw new HttpsError("deadline-exceeded", "This match invitation has expired.");
  }
  const teamSlot = invite.get("teamSlot");
  if (!["TEAM_A", "TEAM_B"].includes(teamSlot)) throw new HttpsError("data-loss", "The invitation has an invalid team slot.");
  const match = await db.collection("matches").doc(String(invite.get("matchId") || "")).get();
  if (!match.exists) throw new HttpsError("not-found", "The match no longer exists.");
  return { matchId: match.id, teamSlot, title: String(match.get("title") || "Match") };
});

/** A captain selects one of their teams and atomically fills the organiser's invited slot. */
exports.claimMatchInviteForTeam = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before joining a match.");
  const token = typeof request.data?.token === "string" ? request.data.token.trim() : "";
  if (!/^[A-Za-z0-9_-]{43}$/.test(token)) throw new HttpsError("invalid-argument", "Invalid match invitation.");
  const teamId = requireTeamId(request.data?.teamId);
  const uid = request.auth.uid;
  const tokenHash = crypto.createHash("sha256").update(token, "utf8").digest("hex");
  const matches = await db.collection("matchInvites").where("tokenHash", "==", tokenHash).limit(2).get();
  if (matches.size !== 1) throw new HttpsError("not-found", "This match invitation is invalid.");
  const inviteRef = matches.docs[0].ref;

  return db.runTransaction(async (transaction) => {
    const invite = await transaction.get(inviteRef);
    if (!invite.exists || invite.get("status") !== "OPEN") throw new HttpsError("failed-precondition", "This match slot is no longer available.");
    const expiresAt = invite.get("expiresAt");
    if (!expiresAt || expiresAt.toDate() <= new Date()) throw new HttpsError("deadline-exceeded", "This match invitation has expired.");
    const matchId = String(invite.get("matchId") || "");
    const teamSlot = invite.get("teamSlot");
    if (!["TEAM_A", "TEAM_B"].includes(teamSlot)) throw new HttpsError("data-loss", "The invitation has an invalid team slot.");
    const matchRef = db.collection("matches").doc(matchId);
    const teamRef = db.collection("teams").doc(teamId);
    const match = await transaction.get(matchRef);
    const team = await transaction.get(teamRef);
    if (!match.exists) throw new HttpsError("not-found", "The match no longer exists.");
    if (!team.exists) throw new HttpsError("not-found", "The selected team no longer exists.");
    const members = Array.isArray(team.get("members")) ? team.get("members") : [];
    const captain = findMember(members, uid);
    if (!hasRole(captain, ROLES.CAPTAIN) && team.get("ownerUserId") !== uid && team.get("ownerId") !== uid && team.get("captainUserId") !== uid) {
      throw new HttpsError("permission-denied", "Only a captain of the selected team can join this match.");
    }
    const claimField = teamSlot === "TEAM_A" ? "teamAClaim" : "teamBClaim";
    const otherClaimField = teamSlot === "TEAM_A" ? "teamBClaim" : "teamAClaim";
    if (match.get(claimField)) throw new HttpsError("failed-precondition", "This team slot has already been filled.");
    if (match.get(otherClaimField)?.userId === uid) throw new HttpsError("failed-precondition", "You have already joined the other team slot.");
    const teamName = String(team.get("teamName") || team.get("name") || "Team").trim() || "Team";
    const teamShortName = String(team.get("shortName") || teamName.slice(0, 3).toUpperCase()).trim();
    const profile = await transaction.get(db.collection("users").doc(uid));
    const captainName = String(profile.get("name") || "Team captain").trim() || "Team captain";
    const replacementId = teamSlot === "TEAM_A" ? "dA1" : "dB1";
    transaction.update(matchRef, {
      [claimField]: {
        userId: uid,
        displayName: captainName,
        replacedDummyPlayerId: replacementId,
        teamId,
        teamName,
        teamShortName,
        claimedAt: FieldValue.serverTimestamp()
      },
      updatedAtEpochMs: Date.now()
    });
    transaction.update(inviteRef, {
      status: "CLAIMED",
      claimedByUserId: uid,
      claimedTeamId: teamId,
      claimedAt: FieldValue.serverTimestamp()
    });
    return { matchId, teamSlot, teamId, teamName };
  });
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
