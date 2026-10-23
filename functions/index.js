const crypto = require("crypto");
const { initializeApp } = require("firebase-admin/app");
const { getAuth } = require("firebase-admin/auth");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { HttpsError, onCall } = require("firebase-functions/v2/https");
const { defineSecret } = require("firebase-functions/params");
const leaderboardScoring = require("./leaderboardScoring");

initializeApp();

const db = getFirestore();
const INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/team-invite?token=";
const TOURNAMENT_INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/tournament-invite?token=";
const TEAM_INVITE_TTL_MS = 7 * 24 * 60 * 60 * 1000;
const TOURNAMENT_INVITE_TTL_MS = 14 * 24 * 60 * 60 * 1000;
const MAX_TEAM_MEMBERS = 18;
const FINALIZED = "FINALIZED";
const ROLES = Object.freeze({ ADMIN: "ADMIN", CAPTAIN: "CAPTAIN", VICE_CAPTAIN: "VICE_CAPTAIN" });
const cloudinaryCloudName = defineSecret("CLOUDINARY_CLOUD_NAME");
const cloudinaryApiKey = defineSecret("CLOUDINARY_API_KEY");
const cloudinaryApiSecret = defineSecret("CLOUDINARY_API_SECRET");

/**
 * Signs one deterministic Cloudinary profile-photo upload for the calling user.
 * The API secret never leaves Firebase Functions or the user's device.
 */
exports.createProfilePhotoUploadSignature = onCall({
  secrets: [cloudinaryCloudName, cloudinaryApiKey, cloudinaryApiSecret],
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before updating your profile photo.");

  // Secret Manager preserves pasted whitespace; Cloudinary treats it as part
  // of the credential, so normalize values before creating the signature.
  const cloudName = String(cloudinaryCloudName.value() || "").trim();
  const apiKey = String(cloudinaryApiKey.value() || "").trim();
  const apiSecret = String(cloudinaryApiSecret.value() || "").trim();
  if (!cloudName || !apiKey || !apiSecret) {
    throw new HttpsError("failed-precondition", "Cloudinary is not configured on the server.");
  }

  const folder = "profile_photos";
  const publicId = request.auth.uid;
  const timestamp = Math.floor(Date.now() / 1000);
  const signaturePayload = `folder=${folder}&overwrite=true&public_id=${publicId}&timestamp=${timestamp}`;
  const signature = crypto.createHash("sha1").update(`${signaturePayload}${apiSecret}`).digest("hex");

  return { cloudName, apiKey, folder, publicId, timestamp, signature };
});

exports.createTournamentLogoUploadSignature = onCall({
  secrets: [cloudinaryCloudName, cloudinaryApiKey, cloudinaryApiSecret],
}, async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before uploading a tournament logo.");
  const assetId = typeof request.data?.assetId === "string" ? request.data.assetId.trim() : "";
  const assetType = typeof request.data?.assetType === "string" ? request.data.assetType.trim() : "logo";
  if (!/^[A-Za-z0-9_-]{1,80}$/.test(assetId)) {
    throw new HttpsError("invalid-argument", "A valid image upload ID is required.");
  }
  if (!["logo", "banner", "teamLogo"].includes(assetType)) {
    throw new HttpsError("invalid-argument", "Image type must be logo, banner, or teamLogo.");
  }

  const cloudName = String(cloudinaryCloudName.value() || "").trim();
  const apiKey = String(cloudinaryApiKey.value() || "").trim();
  const apiSecret = String(cloudinaryApiSecret.value() || "").trim();
  if (!cloudName || !apiKey || !apiSecret) {
    throw new HttpsError("failed-precondition", "Cloudinary is not configured on the server.");
  }

  const folder = assetType === "teamLogo"
    ? "team_logos"
    : assetType === "banner" ? "tournament_banners" : "tournament_logos";
  const publicId = `${request.auth.uid}_${assetId}`;
  const timestamp = Math.floor(Date.now() / 1000);
  const signaturePayload = `folder=${folder}&overwrite=false&public_id=${publicId}&timestamp=${timestamp}`;
  const signature = crypto.createHash("sha1").update(`${signaturePayload}${apiSecret}`).digest("hex");
  return { cloudName, apiKey, folder, publicId, timestamp, signature };
});

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
function isGoogleAccountPhotoUrl(value) {
  try {
    const host = new URL(value).hostname.toLowerCase();
    return host === "googleusercontent.com" || host.endsWith(".googleusercontent.com");
  } catch (_) {
    return false;
  }
}
function storedMember(member, roles = memberRoles(member), playingRole = member?.playingRole || "PLAYER") {
  const stored = { userId: member.userId, roles: [...new Set(roles)], playingRole, joinedAtEpochMs: member.joinedAtEpochMs || Date.now() };
  // Keep manually added, not-yet-registered roster entries intact when other
  // team operations normalize the members array. Their phone is used later,
  // after ownership is verified, to connect the player to an account.
  for (const field of ["displayName", "phoneNumber", "isRegistered"]) {
    if (member[field] !== undefined) stored[field] = member[field];
  }
  return stored;
}
async function teamInTransaction(transaction, teamId) {
  const ref = db.collection("teams").doc(teamId);
  const snapshot = await transaction.get(ref);
  if (!snapshot.exists) throw new HttpsError("not-found", "Team not found.");
  return { ref, members: Array.isArray(snapshot.get("members")) ? snapshot.get("members") : [] };
}

/**
 * Returns the public display data needed by a scorecard or team roster.
 * The callable only exposes a member's user ID, display name, profile photo URL,
 * playing role, and cricket styles used on the public team roster.
 */
exports.getTeamMemberProfiles = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before viewing team members.");
  const teamId = requireTeamId(request.data?.teamId);
  const team = await db.collection("teams").doc(teamId).get();
  if (!team.exists) throw new HttpsError("not-found", "Team not found.");
  const members = Array.isArray(team.get("members")) ? team.get("members") : [];
  const authUsers = await Promise.all(members.map(async (member) => {
    try {
      return await getAuth().getUser(member.userId);
    } catch (_) {
      return null;
    }
  }));
  const userSnapshots = await Promise.all(members.map(async (member, index) => {
    const directProfile = await db.collection("users").doc(member.userId).get();
    if (directProfile.exists) return directProfile;
    // Legacy accounts can have an auto-generated document ID, while the
    // team member and Firebase Auth use the UID stored in users.id.
    const legacyProfiles = await db.collection("users")
      .where("id", "==", member.userId)
      .limit(1)
      .get();
    if (!legacyProfiles.empty) return legacyProfiles.docs[0];
    // Some early phone-auth accounts were recreated with a new Auth UID.
    // Their profile remains associated with the verified phone number, which
    // lets scorecards resolve the correct manually uploaded photo securely.
    const phoneNumber = authUsers[index]?.phoneNumber;
    if (typeof phoneNumber === "string" && phoneNumber.trim()) {
      const phoneProfiles = await db.collection("users")
        .where("phoneNumber", "==", phoneNumber.trim())
        .limit(1)
        .get();
      if (!phoneProfiles.empty) return phoneProfiles.docs[0];
    }
    return directProfile;
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
      const storedPhotoUrl = profile.exists && typeof profile.get("profilePhotoUrl") === "string"
        ? profile.get("profilePhotoUrl").trim()
        : "";
      return {
        userId: member.userId,
        displayName: displayName || "",
        // A Google identity image is not a manually selected SportsXtreme photo.
        profilePhotoUrl: isGoogleAccountPhotoUrl(storedPhotoUrl) ? "" : storedPhotoUrl,
        playingRole: member.playingRole || profilePlayingRole || "Player",
        battingStyle: profile.exists && typeof profile.get("battingStyle") === "string" ? profile.get("battingStyle").trim() : "",
        bowlingStyle: profile.exists && typeof profile.get("bowlingStyle") === "string" ? profile.get("bowlingStyle").trim() : ""
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

function normalizeIndianMobile(value) {
  const digits = String(value || "").replace(/\D/g, "");
  const national = digits.length === 12 && digits.startsWith("91") ? digits.slice(2) : digits;
  return /^[6-9]\d{9}$/.test(national) ? `+91${national}` : null;
}

/** Adds unregistered roster placeholders; phone ownership is verified in a later OTP flow. */
exports.addTeamPlayersByMobile = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before adding players.");
  const teamId = requireTeamId(request.data?.teamId);
  const inputs = request.data?.players;
  if (!Array.isArray(inputs) || inputs.length < 1 || inputs.length > MAX_TEAM_MEMBERS) {
    throw new HttpsError("invalid-argument", "Add between 1 and 18 players.");
  }

  const players = inputs.map((input) => {
    const displayName = typeof input?.name === "string" ? input.name.trim().replace(/\s+/g, " ") : "";
    const phoneNumber = normalizeIndianMobile(input?.mobileNumber);
    if (!displayName || displayName.length > 80) throw new HttpsError("invalid-argument", "Enter a player name up to 80 characters.");
    if (!phoneNumber) throw new HttpsError("invalid-argument", `Enter a valid Indian mobile number for ${displayName}.`);
    return { displayName, phoneNumber };
  });
  if (new Set(players.map((player) => player.phoneNumber)).size !== players.length) {
    throw new HttpsError("invalid-argument", "A mobile number appears more than once in this list.");
  }

  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    const actor = findMember(members, request.auth.uid);
    if (!isManager(actor) && !hasRole(actor, ROLES.VICE_CAPTAIN)) {
      throw new HttpsError("permission-denied", "Only a team admin, captain, or vice-captain can add players.");
    }
    const existingPhones = new Set(members.map((member) => normalizeIndianMobile(member.phoneNumber)).filter(Boolean));
    if (players.some((player) => existingPhones.has(player.phoneNumber))) {
      throw new HttpsError("already-exists", "A player with this mobile number is already on the team.");
    }
    if (members.length + players.length > MAX_TEAM_MEMBERS) {
      throw new HttpsError("failed-precondition", `A team can have at most ${MAX_TEAM_MEMBERS} players.`);
    }

    const addedMembers = players.map(({ displayName, phoneNumber }) => ({
      userId: `pending_${crypto.createHash("sha256").update(`${teamId}:${phoneNumber}`).digest("hex").slice(0, 32)}`,
      displayName,
      phoneNumber,
      isRegistered: false,
      roles: [],
      playingRole: "PLAYER",
      joinedAtEpochMs: Date.now()
    }));
    const updated = [...members.map((member) => storedMember(member)), ...addedMembers];
    transaction.update(ref, { members: updated, memberIds: updated.map((member) => member.userId), updatedAtEpochMs: Date.now() });
    return { teamId, addedCount: addedMembers.length };
  });
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

/** Lets the tournament organiser register one of their teams without an invite link. */
exports.addTournamentTeamManually = onCall(async (request) => {
  if (!request.auth) throw new HttpsError("unauthenticated", "Sign in before adding a team.");
  const tournamentId = requireTeamId(request.data?.tournamentId);
  const teamId = requireTeamId(request.data?.teamId);
  const tournamentRef = db.collection("tournaments").doc(tournamentId);
  const teamRef = db.collection("teams").doc(teamId);
  const entryRef = tournamentRef.collection("teams").doc(teamId);

  return db.runTransaction(async (transaction) => {
    const tournament = await transaction.get(tournamentRef);
    const team = await transaction.get(teamRef);
    const existingEntry = await transaction.get(entryRef);
    if (!tournament.exists) throw new HttpsError("not-found", "Tournament not found.");
    if (tournament.get("hostUid") !== request.auth.uid) {
      throw new HttpsError("permission-denied", "Only the tournament organiser can add teams manually.");
    }
    if (!team.exists) throw new HttpsError("not-found", "Team not found.");
    if (existingEntry.exists) return { tournamentId, teamId, alreadyJoined: true };

    const maxTeams = Number.parseInt(String(tournament.get("requirements")?.numberOfTeams || ""), 10);
    const teamIds = Array.isArray(tournament.get("teamIds")) ? tournament.get("teamIds") : [];
    if (Number.isFinite(maxTeams) && maxTeams > 0 && teamIds.length >= maxTeams) {
      throw new HttpsError("failed-precondition", "This tournament has reached its team limit.");
    }
    const teamName = String(team.get("teamName") || team.get("name") || "Team").trim() || "Team";
    transaction.create(entryRef, {
      teamId,
      teamName,
      captainUserId: request.auth.uid,
      manuallyAdded: true,
      addedBy: request.auth.uid,
      joinedAt: FieldValue.serverTimestamp()
    });
    transaction.update(tournamentRef, { teamIds: FieldValue.arrayUnion(teamId), updatedAtEpochMs: Date.now() });
    return { tournamentId, teamId, teamName, alreadyJoined: false };
  });
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
  if (match.get("status") === "COMPLETED" || match.get("finalization.state") === "FINALIZED") {
    throw new HttpsError("failed-precondition", "A finalized match cannot be deleted.");
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

/** Deletes the caller's account data with Admin privileges, then removes Auth. */
exports.deleteAccount = onCall(async (request) => {
  const uid = request.auth?.uid;
  if (!uid) throw new HttpsError("unauthenticated", "Sign in to delete your account.");

  const authTimeSeconds = Number(request.auth.token.auth_time || 0);
  if (!authTimeSeconds || Date.now() / 1000 - authTimeSeconds > 5 * 60) {
    throw new HttpsError("failed-precondition", "Please sign in again before deleting your account.");
  }

  const ownedQueries = [
    ["users", "id"],
    ["teams", "ownerUserId"],
    ["tournaments", "hostUid"],
    ["matches", "ownerId"],
    ["matches", "organizerId"],
    ["matches", "createdBy"],
    ["grounds", "ownerId"],
    ["grounds", "ownerUserId"],
    ["communityProfiles", "ownerId"],
    ["communityProfiles", "userId"],
    ["matchInvites", "createdByUserId"],
    ["teamInvites", "createdByUserId"],
    ["tournamentInvites", "createdByUserId"],
  ];
  const references = new Map();
  for (const [collection, field] of ownedQueries) {
    const snapshot = await db.collection(collection).where(field, "==", uid).get();
    for (const document of snapshot.docs) references.set(document.ref.path, document.ref);
  }
  const profileRef = db.collection("users").doc(uid);
  references.set(profileRef.path, profileRef);

  for (const reference of references.values()) {
    await db.recursiveDelete(reference);
  }

  // Leaderboards are materialized separately from user profiles. Remove this
  // account from each source contribution and recalculate player aggregates so
  // future match rebuilds cannot make the deleted account reappear.
  const contributionSnapshots = await db.collection("leaderboards")
    .doc("matchContributions").collection("entries").get();
  for (const contributionSnapshot of contributionSnapshots.docs) {
    const contribution = contributionSnapshot.data();
    if (!contribution.active || !Array.isArray(contribution.rows)) continue;
    let changed = false;
    const replacement = {
      ...contribution,
      rows: contribution.rows.map((row) => {
        const players = { ...(row.players || {}) };
        for (const [playerKey, player] of Object.entries(players)) {
          const playerId = safeName(player.playerId) || playerKey;
          if (player.canonicalPlayerId === uid || playerId === uid || playerId.endsWith(`::${uid}`)) {
            delete players[playerKey];
            changed = true;
          }
        }
        return { ...row, players };
      }),
    };
    if (changed) {
      await db.runTransaction((transaction) =>
        replaceLeaderboardContribution(transaction, contributionSnapshot.ref, replacement));
    }
  }

  await getAuth().deleteUser(uid);
  return { deleted: true };
});

function failFinalization(code, message) { throw new HttpsError(code, message); }
function safeName(value) { return typeof value === "string" ? value.trim() : ""; }
function asNumber(value) { return typeof value === "number" && Number.isFinite(value) ? value : 0; }

function validateMatchParticipantSet(match) {
  const teamA = match.teamA?.teamId; const teamB = match.teamB?.teamId;
  if (!teamA || !teamB || teamA === teamB) failFinalization("failed-precondition", "Match teams are incomplete or invalid.");
  const xi = match.playingXI || {};
  const players = new Map();
  for (const [side, teamId, squadKey] of [["TEAM_A", teamA, "A"], ["TEAM_B", teamB, "B"]]) {
    let selectedXi = xi[side];
    let playerIds = selectedXi?.playerIds;
    if (!Array.isArray(playerIds) || playerIds.length === 0) {
      const squad = match.matchSquads?.[squadKey];
      if (squad?.teamId === teamId && Array.isArray(squad.players)) {
        playerIds = squad.players.map((player) => {
          const explicitPlayerId = safeName(player?.playerId);
          const userId = safeName(player?.userId);
          return explicitPlayerId || (userId ? `${teamId}::${userId}` : "");
        }).filter(Boolean);
        selectedXi = {
          teamId,
          side,
          selectedByUserId: safeName(squad.selectedByUserId) || safeName(match.ownerId) || safeName(match.organiserId),
          playerIds
        };
      }
    }
    if (selectedXi?.teamId !== teamId || selectedXi?.side !== side || !safeName(selectedXi?.selectedByUserId)) failFinalization("failed-precondition", `A valid ${side} playing XI or match squad is required.`);
    if (!Array.isArray(playerIds) || playerIds.length === 0 || new Set(playerIds).size !== playerIds.length) {
      failFinalization("failed-precondition", `A valid ${side} playing XI or match squad is required.`);
    }
    for (const id of playerIds) {
      const playerId = String(id);
      if (players.has(playerId) && players.get(playerId) !== teamId) failFinalization("failed-precondition", "A player cannot represent both teams in the same match.");
      players.set(playerId, teamId);
    }
  }
  return { teamA, teamB, players };
}

async function validateMatchTournament(match, teamA, teamB) {
  const tournamentId = safeName(match.tournamentId);
  if (!tournamentId) {
    if (match.matchType === "TOURNAMENT") failFinalization("failed-precondition", "A tournament match must identify its tournament.");
    return null;
  }
  const tournament = await db.collection("tournaments").doc(tournamentId).get();
  if (!tournament.exists) failFinalization("failed-precondition", "The selected tournament does not exist.");
  const enrolledTeams = Array.isArray(tournament.get("teamIds")) ? tournament.get("teamIds") : [];
  if (!enrolledTeams.includes(teamA) || !enrolledTeams.includes(teamB)) failFinalization("failed-precondition", "Both match teams must be registered in the selected tournament.");
  return tournament.data();
}

function projectRemoteInnings(deliveries, matchId, inningsId, inningsNumber, battingTeamId, bowlingTeamId, participants) {
  const rows = deliveries.map((doc) => ({ id: doc.id, ...doc.data() }))
    .filter((row) => row.inningsId === inningsId && row.eventType !== "REVERSAL");
  const reversals = deliveries.map((doc) => doc.data()).filter((row) => row.inningsId === inningsId && row.eventType === "REVERSAL");
  const reversed = new Set();
  const deliveryIds = new Set(rows.map((row) => row.deliveryId || row.id));
  for (const row of reversals) {
    if (!row.reversedEventId || !deliveryIds.has(row.reversedEventId) || reversed.has(row.reversedEventId)) failFinalization("data-loss", "Invalid duplicate score correction event.");
    reversed.add(row.reversedEventId);
  }
  const active = rows.filter((row) => !reversed.has(row.deliveryId || row.id)).sort((a, b) => asNumber(a.sequenceNumber) - asNumber(b.sequenceNumber));
  const usedSequences = new Set(); const bat = new Map(); const bowl = new Map(); const fielding = new Map(); const bowlerEvents = new Map();
  let totalScore = 0; let wickets = 0; let legalBalls = 0; let expectedLegalBall = 0; let expectedOver = 0;
  for (const row of active) {
    if (row.matchId !== matchId || !row.deliveryId || asNumber(row.inning) !== inningsNumber || !Number.isInteger(row.sequenceNumber) || row.sequenceNumber < 0 || usedSequences.has(row.sequenceNumber)) failFinalization("data-loss", "A delivery has an invalid or duplicate sequence.");
    usedSequences.add(row.sequenceNumber);
    if (row.battingTeamId !== battingTeamId || row.bowlingTeamId !== bowlingTeamId || participants.players.get(row.batsmanId) !== battingTeamId || participants.players.get(row.nonStrikerId) !== battingTeamId || participants.players.get(row.bowlerId) !== bowlingTeamId) failFinalization("data-loss", "A delivery contains a player outside the selected teams.");
    const extras = Array.isArray(row.extraBreakdown) ? row.extraBreakdown : [];
    const knownExtras = new Set(["NONE", "WIDE", "NO_BALL", "BYE", "LEG_BYE", "PENALTY"]);
    for (const extra of extras) if (!knownExtras.has(extra?.type) || !Number.isInteger(extra?.runs) || extra.runs < 0) failFinalization("data-loss", "A delivery contains invalid extras.");
    const runs = asNumber(row.runs); if (!Number.isInteger(runs) || runs < 0) failFinalization("data-loss", "A delivery contains invalid batter runs.");
    const extraTotal = extras.reduce((sum, item) => sum + item.runs, 0);
    if (extraTotal !== asNumber(row.extras)) failFinalization("data-loss", "Delivery extras do not match their breakdown.");
    const legal = row.isLegalDelivery === true;
    if (legal === extras.some((item) => item.type === "WIDE" || item.type === "NO_BALL")) failFinalization("data-loss", "Wide and no-ball deliveries must be recorded as illegal deliveries.");
    const overNumber = asNumber(row.over); const ballNumber = asNumber(row.ball);
    if (legal) { expectedLegalBall++; legalBalls++; }
    if (overNumber !== expectedOver || ballNumber !== (legal ? expectedLegalBall - expectedOver * 6 : expectedLegalBall - expectedOver * 6 + 1)) {
      // Client over/ball numbering is presentation-oriented; only enforce a sane range below.
      if (!Number.isInteger(overNumber) || overNumber < 0 || !Number.isInteger(ballNumber) || ballNumber < 0 || ballNumber > 10) failFinalization("data-loss", "A delivery has invalid over numbering.");
    }
    if (legal && expectedLegalBall % 6 === 0) expectedOver++;
    totalScore += runs + extraTotal;
    const batter = bat.get(row.batsmanId) || { runs: 0, balls: 0, fours: 0, sixes: 0, status: "NOT_OUT", dismissalType: null, dismissedByBowlerId: null, dismissedByFielderId: null };
    batter.runs += runs; if (!(extras.some((item) => item.type === "WIDE"))) batter.balls++;
    if (runs === 4) batter.fours++; if (runs === 6) batter.sixes++;
    const bowler = bowl.get(row.bowlerId) || { legalBalls: 0, maidens: 0, runsConceded: 0, wickets: 0, wides: 0, noBalls: 0 };
    if (legal) bowler.legalBalls++;
    bowler.runsConceded += runs + extras.filter((item) => ["WIDE", "NO_BALL"].includes(item.type)).reduce((sum, item) => sum + item.runs, 0);
    bowler.wides += extras.filter((item) => item.type === "WIDE").reduce((sum, item) => sum + item.runs, 0);
    bowler.noBalls += extras.filter((item) => item.type === "NO_BALL").reduce((sum, item) => sum + item.runs, 0);
    if (!bowlerEvents.has(row.bowlerId)) bowlerEvents.set(row.bowlerId, []);
    bowlerEvents.get(row.bowlerId).push({ overNumber, legal, conceded: runs + extras.filter((item) => ["WIDE", "NO_BALL"].includes(item.type)).reduce((sum, item) => sum + item.runs, 0) });
    if (row.wicket === true) {
      const type = row.dismissalType; const dismissed = row.dismissedPlayerId;
      if (!dismissed || participants.players.get(dismissed) !== battingTeamId || !new Set(["BOWLED", "CAUGHT", "LBW", "RUN_OUT", "STUMPED", "HIT_WICKET", "TIMED_OUT", "OBSTRUCTING_THE_FIELD", "RETIRED_OUT", "RETIRED_HURT", "HIT_BALL_TWICE", "OTHER"]).has(type)) failFinalization("data-loss", "A dismissal is invalid.");
      if (type !== "RETIRED_HURT") wickets++;
      batter.status = type === "RETIRED_HURT" ? "RETIRED_HURT" : "OUT"; batter.dismissalType = type;
      const fielder = row.fielderId;
      if (fielder && participants.players.get(fielder) !== bowlingTeamId) failFinalization("data-loss", "A dismissal credits a non-fielding player.");
      if (["CAUGHT", "STUMPED"].includes(type) && !fielder) failFinalization("data-loss", "A catch or stumping must identify its credited fielder.");
      // The current delivery schema stores one primary fielderId; that player
      // alone receives a run-out credit when a dismissal records one.
      const creditedEvent = leaderboardScoring.creditedFieldingEvent(type, fielder);
      if (creditedEvent) {
        const counts = fielding.get(fielder) || { catches: 0, runOuts: 0, stumpings: 0 };
        counts[creditedEvent]++;
        fielding.set(fielder, counts);
      }
      if (leaderboardScoring.isBowlerCreditedDismissal(type)) { bowler.wickets++; batter.dismissedByBowlerId = row.bowlerId; }
      batter.dismissedByFielderId = fielder || null;
    } else if (row.wicket === true || (row.dismissalType && row.dismissalType !== "NONE")) failFinalization("data-loss", "A wicket flag and dismissal type disagree.");
    bat.set(row.batsmanId, batter); bowl.set(row.bowlerId, bowler);
  }
  for (const [playerId, events] of bowlerEvents) {
    bowl.get(playerId).maidens = [...new Set(events.map((event) => event.overNumber))].filter((over) => {
      const overEvents = events.filter((event) => event.overNumber === over);
      return overEvents.filter((event) => event.legal).length === 6 && overEvents.reduce((sum, event) => sum + event.conceded, 0) === 0;
    }).length;
  }
  return { active, totalScore, wickets, legalBalls, batting: bat, bowling: bowl, fielding };
}

async function validateFinalizedScorecard(matchRef, match) {
  const participants = validateMatchParticipantSet(match);
  const tournament = await validateMatchTournament(match, participants.teamA, participants.teamB);
  const inningsList = Array.isArray(match.innings) ? match.innings : [];
  if (!inningsList.length || inningsList.some((innings) => innings.status !== "COMPLETED")) failFinalization("failed-precondition", "Every match innings must be completed before finalization.");
  if (new Set(inningsList.map((innings) => innings.inningsId || innings.id)).size !== inningsList.length || new Set(inningsList.map((innings) => innings.number)).size !== inningsList.length || inningsList.some((innings, index) => !safeName(innings.inningsId || innings.id) || innings.number !== index + 1 || (index > 0 && innings.battingTeamId === inningsList[index - 1].battingTeamId))) failFinalization("failed-precondition", "The match innings sequence is incomplete or invalid.");
  const deliveriesSnap = await matchRef.collection("deliveries").get();
  const allDeliveries = deliveriesSnap.docs;
  const projections = [];
  for (const innings of inningsList) {
    if (![participants.teamA, participants.teamB].includes(innings.battingTeamId) || ![participants.teamA, participants.teamB].includes(innings.bowlingTeamId) || innings.battingTeamId === innings.bowlingTeamId) failFinalization("data-loss", "An innings has invalid teams.");
    const projected = projectRemoteInnings(allDeliveries, matchRef.id, innings.inningsId || innings.id, innings.number, innings.battingTeamId, innings.bowlingTeamId, participants);
    const scoreWithPenalties = Math.max(0, projected.totalScore + asNumber(innings.penaltyScoreAdjustment));
    if (scoreWithPenalties !== asNumber(innings.score) || projected.wickets !== asNumber(innings.wickets) || projected.legalBalls !== asNumber(innings.legalBalls)) failFinalization("failed-precondition", "Saved innings totals do not match the delivery history.");
    projections.push({ innings, ...projected, totalScore: scoreWithPenalties });
  }
  const [batting, bowling, summaries] = await Promise.all([
    matchRef.collection("scorecard").doc("batting").collection("entries").get(),
    matchRef.collection("scorecard").doc("bowling").collection("entries").get(),
    matchRef.collection("scorecard").doc("summary").collection("entries").get()
  ]);
  for (const projection of projections) {
    const inningsId = projection.innings.inningsId || projection.innings.id;
    const battingRows = batting.docs.filter((doc) => doc.get("inningsId") === inningsId);
    const bowlingRows = bowling.docs.filter((doc) => doc.get("inningsId") === inningsId);
    const activeBattingRows = battingRows.filter((doc) => asNumber(doc.get("runs")) !== 0 || asNumber(doc.get("balls")) !== 0 || asNumber(doc.get("fours")) !== 0 || asNumber(doc.get("sixes")) !== 0);
    const activeBowlingRows = bowlingRows.filter((doc) => asNumber(doc.get("legalBalls")) !== 0 || asNumber(doc.get("runs")) !== 0 || asNumber(doc.get("wickets")) !== 0);
    if (activeBattingRows.length !== projection.batting.size || activeBowlingRows.length !== projection.bowling.size) failFinalization("failed-precondition", "Scorecard contains missing or extra player rows.");
    for (const row of projection.batting) {
      const saved = battingRows.find((doc) => doc.get("playerId") === row[0]);
      if (!saved || asNumber(saved.get("runs")) !== row[1].runs || asNumber(saved.get("balls")) !== row[1].balls || asNumber(saved.get("fours")) !== row[1].fours || asNumber(saved.get("sixes")) !== row[1].sixes || saved.get("status") !== row[1].status || (saved.get("dismissalType") || null) !== row[1].dismissalType || (saved.get("dismissedByBowlerId") || null) !== row[1].dismissedByBowlerId || (saved.get("dismissedByFielderId") || null) !== row[1].dismissedByFielderId) failFinalization("failed-precondition", "Batting scorecard does not match delivery history.");
    }
    for (const row of projection.bowling) {
      const saved = bowlingRows.find((doc) => doc.get("playerId") === row[0]);
      if (!saved || asNumber(saved.get("legalBalls")) !== row[1].legalBalls || asNumber(saved.get("runs")) !== row[1].runsConceded || asNumber(saved.get("wickets")) !== row[1].wickets || asNumber(saved.get("maidens")) !== row[1].maidens || asNumber(saved.get("wides")) !== row[1].wides || asNumber(saved.get("noBalls")) !== row[1].noBalls) failFinalization("failed-precondition", "Bowling scorecard does not match delivery history.");
    }
    const summary = summaries.docs.find((doc) => doc.get("inningsId") === inningsId);
    if (!summary || asNumber(summary.get("totalScore")) !== projection.totalScore || asNumber(summary.get("wickets")) !== projection.wickets || asNumber(summary.get("legalBalls")) !== projection.legalBalls) failFinalization("failed-precondition", "Scorecard summary does not match delivery history.");
  }
  if (projections.length >= 2) {
    const [first, second] = projections;
    if (first.innings.battingTeamId === second.innings.battingTeamId) failFinalization("failed-precondition", "The final innings must be played by the opposing team.");
  }
  return { participants, projections, tournament };
}

function leaderboardId(value) { return crypto.createHash("sha256").update(String(value)).digest("hex").slice(0, 32); }
function leaderboardScopeKey(scope) { return leaderboardId([scope.sport, scope.ballType, scope.format, scope.tournamentId].join("|")); }
function playerTotals() {
  return { matches: 0, battingInnings: 0, runs: 0, balls: 0, fours: 0, sixes: 0, outs: 0, battingPoints: 0,
    bowlingInnings: 0, legalBalls: 0, runsConceded: 0, wickets: 0, maidens: 0, bowlingPoints: 0,
    catches: 0, runOuts: 0, stumpings: 0, fieldingPoints: 0, allRounderPoints: 0 };
}

function matchPlayerProfile(match, playerId) {
  const profile = match.playerProfiles?.[playerId] || {};
  const separator = String(playerId).lastIndexOf("::");
  const userId = separator >= 0 ? String(playerId).slice(separator + 2) : "";
  const squadPlayer = Object.values(match.matchSquads || {})
    .flatMap((squad) => Array.isArray(squad?.players) ? squad.players : [])
    .find((player) => safeName(player?.playerId) === playerId || (userId && safeName(player?.userId) === userId));
  return {
    name: safeName(profile.name) || safeName(squadPlayer?.displayName),
    linkedUserId: safeName(profile.linkedUserId) || userId
  };
}

function buildLeaderboardContribution(match, validated, revision) {
  const players = {};
  const ensurePlayer = (playerId) => {
    if (!players[playerId]) {
      const profile = matchPlayerProfile(match, playerId);
      const teamId = validated.participants.players.get(playerId) || "";
      const team = teamId === match.teamA?.teamId ? match.teamA : match.teamB;
      players[playerId] = { ...playerTotals(), playerId, name: safeName(profile.name) || playerId,
        teamShortName: safeName(team?.shortName) || safeName(team?.name).slice(0, 3), teamName: safeName(team?.name) || teamId,
        linkedUserId: safeName(profile.linkedUserId) || "", profilePhotoUrl: "", teamId };
    }
    return players[playerId];
  };
  const teamIds = [validated.participants.teamA, validated.participants.teamB];
  const teamScores = Object.fromEntries(teamIds.map((id) => [id, 0]));
  const teamBalls = Object.fromEntries(teamIds.map((id) => [id, 0]));
  for (const projection of validated.projections) {
    const inningsId = projection.innings.inningsId || projection.innings.id;
    for (const [playerId, row] of projection.batting) {
      const player = ensurePlayer(playerId); player.battingInnings++; player.runs += row.runs; player.balls += row.balls;
      player.fours += row.fours; player.sixes += row.sixes; if (row.status === "OUT") player.outs++;
      player.battingPoints += leaderboardScoring.scoreBatting(row);
    }
    for (const [playerId, row] of projection.bowling) {
      const player = ensurePlayer(playerId); player.bowlingInnings++; player.legalBalls += row.legalBalls;
      player.runsConceded += row.runsConceded; player.wickets += row.wickets; player.maidens += row.maidens;
      player.bowlingPoints += leaderboardScoring.scoreBowling(row);
    }
    for (const [playerId, row] of projection.fielding) {
      const player = ensurePlayer(playerId); player.catches += row.catches; player.runOuts += row.runOuts; player.stumpings += row.stumpings;
      player.fieldingPoints += leaderboardScoring.scoreFielding(row);
    }
    teamScores[projection.innings.battingTeamId] += asNumber(projection.innings.score);
    const legalBalls = asNumber(projection.innings.legalBalls);
    const quotaBalls = match.format !== "TEST" && asNumber(match.overs) > 0 ? asNumber(match.overs) * 6 : legalBalls;
    teamBalls[projection.innings.battingTeamId] += asNumber(projection.innings.wickets) >= 10 ? Math.max(legalBalls, quotaBalls) : legalBalls;
  }
  for (const player of Object.values(players)) {
    player.matches = 1;
    player.allRounderPoints = player.battingPoints + player.bowlingPoints;
    // Team-scoped player IDs use `${teamId}::${userId}`. Persist the linked
    // account ID separately so the client can collapse one person across teams.
    player.canonicalPlayerId = player.linkedUserId || player.playerId;
    delete player.linkedUserId;
  }
  const result = match.finalization?.result;
  const pointRules = match.tournamentRules?.pointsRules || {};
  const winnerTeamId = match.finalization?.winnerTeamId;
  const rows = leaderboardScoring.rankingScopes(match).map((scope) => {
    const teamData = {};
    for (const teamId of teamIds) {
      const otherTeamId = teamIds.find((id) => id !== teamId);
      const team = teamId === (match.teamA?.teamId) ? match.teamA : match.teamB;
      const totalRuns = teamScores[teamId] || 0;
      const totalAgainst = teamScores[otherTeamId] || 0;
      const points = leaderboardScoring.teamPointsForResult(result, pointRules);
      teamData[teamId] = { teamId, name: safeName(team?.name) || teamId, shortName: safeName(team?.shortName) || safeName(team?.name).slice(0, 3),
        logoUrl: "", matchesPlayed: 1, wins: result === "WIN" && winnerTeamId === teamId ? 1 : 0,
        losses: result === "WIN" && winnerTeamId !== teamId ? 1 : 0,
        ties: result === "TIE" ? 1 : 0, noResults: result === "NO_RESULT" ? 1 : 0, points, runsFor: totalRuns, ballsFaced: teamBalls[teamId] || 0,
        runsAgainst: totalAgainst, ballsBowled: teamBalls[otherTeamId] || 0 };
    }
    const scopedPlayers = Object.fromEntries(Object.entries(players).map(([id, stats]) => [id, { ...stats, scopeKey: leaderboardScopeKey(scope) }]));
    return { ...scope, scopeKey: leaderboardScopeKey(scope), players: scopedPlayers, teams: teamData };
  });
  return { matchId: match.matchId, revision, scoringVersion: leaderboardScoring.SCORING_VERSION,
    scoringConfig: leaderboardScoring.SCORING, teamPointsRules: pointRules, active: true, rows };
}

async function enrichLeaderboardDisplayData(contribution, match) {
  const teamIds = [match.teamA?.teamId, match.teamB?.teamId].filter(Boolean);
  const playerProfiles = new Map(Object.values(contribution.rows.flatMap((row) => Object.values(row.players)))
    .map((player) => [player.playerId, matchPlayerProfile(match, player.playerId)]));
  const [teamSnapshots, profileSnapshots] = await Promise.all([
    Promise.all(teamIds.map((id) => db.collection("teams").doc(id).get())),
    Promise.all([...new Set([...playerProfiles.values()].map((profile) => profile.linkedUserId).filter(Boolean))]
      .map((id) => db.collection("users").doc(id).get()))
  ]);
  const logos = new Map(teamSnapshots.map((snapshot) => [snapshot.id, safeName(snapshot.get("logoUrl") || snapshot.get("logo"))]));
  const profiles = new Map(profileSnapshots.map((snapshot) => [snapshot.id, {
    name: safeName(snapshot.get("name") || snapshot.get("displayName")),
    photo: safeName(snapshot.get("profilePhotoUrl"))
  }]));
  for (const row of contribution.rows) {
    for (const [id, team] of Object.entries(row.teams)) team.logoUrl = logos.get(id) || "";
    for (const player of Object.values(row.players)) {
      const profile = playerProfiles.get(player.playerId) || {};
      const user = profiles.get(profile.linkedUserId) || {};
      if (!safeName(player.name) || player.name === player.playerId) player.name = profile.name || user.name || player.playerId;
      player.profilePhotoUrl = user.photo || "";
    }
  }
  return contribution;
}

function leaderboardAggregateRef(kind, scopeKey, id) {
  const collection = kind === "player" ? "playerAggregates" : "teamAggregates";
  return db.collection("leaderboards").doc(collection).collection("entries").doc(`${scopeKey}_${leaderboardId(id)}`);
}

async function replaceLeaderboardContribution(transaction, contributionRef, replacement) {
  const previousSnapshot = await transaction.get(contributionRef);
  const previous = previousSnapshot.exists && previousSnapshot.get("active") ? previousSnapshot.data() : null;
  const changes = new Map();
  const addRow = (kind, scope, id, stats, multiplier) => {
    const ref = leaderboardAggregateRef(kind, scope.scopeKey, id);
    const key = `${kind}:${ref.path}`;
    if (!changes.has(key)) changes.set(key, { kind, ref, scope, id, delta: {}, metadata: {} });
    const change = changes.get(key);
    for (const [field, value] of Object.entries(stats)) {
      if (typeof value === "number") change.delta[field] = (change.delta[field] || 0) + value * multiplier;
      else if (multiplier > 0 && !["scopeKey", "playerId", "teamId"].includes(field)) change.metadata[field === "name" ? "name" : field] = value;
    }
  };
  for (const [source, multiplier] of [[previous, -1], [replacement, 1]]) {
    if (!source?.active) continue;
    for (const scope of source.rows || []) {
      for (const [id, stats] of Object.entries(scope.players || {})) addRow("player", scope, id, stats, multiplier);
      for (const [id, stats] of Object.entries(scope.teams || {})) addRow("team", scope, id, stats, multiplier);
    }
  }
  const snapshots = await Promise.all([...changes.values()].map((change) => transaction.get(change.ref)));
  const writes = [];
  [...changes.values()].forEach((change, index) => {
    const snapshot = snapshots[index]; const current = snapshot.exists ? snapshot.data() : {};
    const aggregate = change.kind === "player"
      ? leaderboardScoring.aggregatePlayer(current, change.delta)
      : leaderboardScoring.aggregateTeam(current, change.delta);
    const hasTotals = Object.entries(aggregate).some(([key, value]) => typeof value === "number" && value !== 0 && key !== "netRunRate");
    if (!hasTotals) writes.push(() => transaction.delete(change.ref));
    else writes.push(() => transaction.set(change.ref, { ...current, ...change.scope, ...change.metadata, ...aggregate,
      ...(change.kind === "player" ? { playerId: change.id } : { teamId: change.id }),
      updatedAt: FieldValue.serverTimestamp() }));
  });
  for (const write of writes) write();
  if (replacement) {
    transaction.set(contributionRef, replacement);
    transaction.set(contributionRef.collection("revisions").doc(String(replacement.revision)), replacement);
  } else if (previousSnapshot.exists) {
    const prior = previousSnapshot.data();
    transaction.set(contributionRef, { ...prior, active: false, invalidatedAt: FieldValue.serverTimestamp() });
    if (prior.active) transaction.set(contributionRef.collection("revisions").doc(String(prior.revision)), { active: false, invalidatedAt: FieldValue.serverTimestamp() }, { merge: true });
  }
}

exports.finalizeMatch = onCall(async (request) => {
  if (!request.auth) failFinalization("unauthenticated", "Sign in before finalizing a match.");
  const matchId = requireTeamId(request.data?.matchId); const matchRef = db.collection("matches").doc(matchId);
  const snapshot = await matchRef.get(); if (!snapshot.exists) failFinalization("not-found", "Match not found.");
  const match = snapshot.data();
  if ((match.ownerId || match.organiserId) !== request.auth.uid) failFinalization("permission-denied", "Only the match organiser may finalize it.");
  if (match.finalization?.state === FINALIZED) return { matchId, finalized: true, revision: match.finalization.revision };
  if (match.status !== "FINALIZATION_PENDING" && !["CORRECTION_OPEN", "FINALIZING"].includes(match.finalization?.state)) failFinalization("failed-precondition", "Sync the locally completed match before requesting finalization.");
  const previousFinalization = match.finalization?.state === "FINALIZING"
    ? (match.finalization.previousState === "CORRECTION_OPEN" ? { ...match.finalization, state: "CORRECTION_OPEN" } : null)
    : (match.finalization || null);
  await db.runTransaction(async (transaction) => {
    const current = await transaction.get(matchRef);
    if (current.get("finalization.state") === FINALIZED || current.get("finalization.state") === "FINALIZING") return;
    if ((current.get("ownerId") || current.get("organiserId")) !== request.auth.uid || (current.get("status") !== "FINALIZATION_PENDING" && current.get("finalization.state") !== "CORRECTION_OPEN")) failFinalization("aborted", "Match changed before finalization.");
    transaction.update(matchRef, { finalization: { ...(current.get("finalization") || {}), previousState: current.get("finalization.state") || "", state: "FINALIZING", requestedBy: request.auth.uid, validationStartedAt: FieldValue.serverTimestamp() } });
  });
  const lockedMatchSnap = await matchRef.get();
  if (lockedMatchSnap.get("finalization.state") === FINALIZED) return { matchId, finalized: true, revision: lockedMatchSnap.get("finalization.revision") };
  if (lockedMatchSnap.get("finalization.state") !== "FINALIZING") failFinalization("aborted", "Could not lock match data for finalization.");
  let validated;
  try {
    validated = await validateFinalizedScorecard(matchRef, lockedMatchSnap.data());
  } catch (error) {
    await db.runTransaction(async (transaction) => {
      const current = await transaction.get(matchRef);
      if (current.get("finalization.state") === "FINALIZING") transaction.update(matchRef, { finalization: previousFinalization, status: previousFinalization?.state === "CORRECTION_OPEN" ? "IN_PROGRESS" : "FINALIZATION_PENDING" });
    });
    throw error;
  }
  const teamScores = new Map();
  for (const item of validated.projections) teamScores.set(item.innings.battingTeamId, (teamScores.get(item.innings.battingTeamId) || 0) + asNumber(item.innings.score));
  const scoreValues = [...teamScores.values()];
  if (scoreValues.length !== 2) {
    await db.runTransaction(async (transaction) => {
      const current = await transaction.get(matchRef);
      if (current.get("finalization.state") === "FINALIZING") transaction.update(matchRef, { finalization: previousFinalization, status: previousFinalization?.state === "CORRECTION_OPEN" ? "IN_PROGRESS" : "FINALIZATION_PENDING" });
    });
    failFinalization("failed-precondition", "A completed match must contain innings for both teams to determine its result.");
  }
  const result = scoreValues[0] === scoreValues[1] ? "TIE" : "WIN";
  const winnerTeamId = result === "WIN" ? [...teamScores.entries()].sort((a, b) => b[1] - a[1])[0][0] : null;
  const now = FieldValue.serverTimestamp();
  const revision = Math.max(1, asNumber(lockedMatchSnap.get("finalization.revision")));
  const matchForRanking = { ...lockedMatchSnap.data(), matchId, tournamentRules: validated.tournament,
    finalization: { state: FINALIZED, revision, result, teamScores: Object.fromEntries(teamScores), winnerTeamId } };
  let contribution;
  try {
    contribution = await enrichLeaderboardDisplayData(buildLeaderboardContribution(matchForRanking, validated, revision), matchForRanking);
  } catch (error) {
    await db.runTransaction(async (transaction) => {
      const current = await transaction.get(matchRef);
      if (current.get("finalization.state") === "FINALIZING") transaction.update(matchRef, { finalization: previousFinalization, status: previousFinalization?.state === "CORRECTION_OPEN" ? "IN_PROGRESS" : "FINALIZATION_PENDING" });
    });
    throw error;
  }
  const contributionRef = db.collection("leaderboards").doc("matchContributions").collection("entries").doc(matchId);
  try {
    await db.runTransaction(async (transaction) => {
      const current = await transaction.get(matchRef);
      if (current.get("finalization.state") === FINALIZED) return;
      if ((current.get("ownerId") || current.get("organiserId")) !== request.auth.uid || current.get("finalization.state") !== "FINALIZING") failFinalization("aborted", "Match changed while finalization was being checked.");
      await replaceLeaderboardContribution(transaction, contributionRef, contribution);
      transaction.update(matchRef, { status: "COMPLETED", finalization: { state: FINALIZED, revision, by: request.auth.uid, finalizedAt: now, result, teamScores: Object.fromEntries(teamScores), winnerTeamId }, finalizedAt: now, finalizedBy: request.auth.uid });
      if (previousFinalization?.state === "CORRECTION_OPEN") {
        transaction.update(matchRef.collection("correctionAudit").doc(String(revision)), { state: FINALIZED, finalizedAt: now, finalizedBy: request.auth.uid });
      }
    });
  } catch (error) {
    await db.runTransaction(async (transaction) => {
      const current = await transaction.get(matchRef);
      if (current.get("finalization.state") === "FINALIZING") transaction.update(matchRef, { finalization: previousFinalization, status: previousFinalization?.state === "CORRECTION_OPEN" ? "IN_PROGRESS" : "FINALIZATION_PENDING" });
    });
    throw error;
  }
  return { matchId, finalized: true, revision: Math.max(1, asNumber(match.finalization?.revision)) };
});

exports.requestMatchCorrection = onCall(async (request) => {
  if (!request.auth) failFinalization("unauthenticated", "Sign in before requesting a correction.");
  const matchId = requireTeamId(request.data?.matchId); const reason = safeName(request.data?.reason);
  if (reason.length < 8 || reason.length > 500) failFinalization("invalid-argument", "Provide a correction reason between 8 and 500 characters.");
  const matchRef = db.collection("matches").doc(matchId);
  await db.runTransaction(async (transaction) => {
    const current = await transaction.get(matchRef);
    if (!current.exists) failFinalization("not-found", "Match not found.");
    if ((current.get("ownerId") || current.get("organiserId")) !== request.auth.uid) failFinalization("permission-denied", "Only the match organiser may request a correction.");
    if (current.get("finalization.state") !== FINALIZED) failFinalization("failed-precondition", "Only a finalized match can be corrected.");
    const revision = asNumber(current.get("finalization.revision")) + 1;
    const contributionRef = db.collection("leaderboards").doc("matchContributions").collection("entries").doc(matchId);
    await replaceLeaderboardContribution(transaction, contributionRef, null);
    transaction.update(matchRef, { finalization: { state: "CORRECTION_OPEN", revision, correctionReason: reason, correctionBy: request.auth.uid, correctionRequestedAt: FieldValue.serverTimestamp(), previousFinalizedAt: current.get("finalization.finalizedAt") }, status: "IN_PROGRESS" });
    transaction.create(matchRef.collection("correctionAudit").doc(String(revision)), { revision, reason, requestedBy: request.auth.uid, requestedAt: FieldValue.serverTimestamp(), state: "OPEN" });
  });
  return { matchId, correctionOpen: true };
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
  const allowedFields = new Set(["description", "founded", "homeGround", "captainName", "captainMobile", "coachManager", "teamMotto", "logoUrl"]);
  const changes = request.data?.changes;
  if (!changes || typeof changes !== "object" || Array.isArray(changes)) throw new HttpsError("invalid-argument", "Invalid team details.");
  const keys = Object.keys(changes);
  if (keys.length !== 1 || !allowedFields.has(keys[0]) || typeof changes[keys[0]] !== "string") {
    throw new HttpsError("invalid-argument", "Invalid team detail.");
  }
  const value = changes[keys[0]].trim();
  if (value.length > 500) throw new HttpsError("invalid-argument", "Team detail is too long.");
  if (keys[0] === "logoUrl") {
    let parsedUrl;
    try {
      parsedUrl = new URL(value);
    } catch {
      throw new HttpsError("invalid-argument", "Select a valid team photo.");
    }
    if (parsedUrl.protocol !== "https:" || !(parsedUrl.hostname === "cloudinary.com" || parsedUrl.hostname.endsWith(".cloudinary.com"))) {
      throw new HttpsError("invalid-argument", "Team photos must use a secure Cloudinary image URL.");
    }
  }
  return db.runTransaction(async (transaction) => {
    const { ref, members } = await teamInTransaction(transaction, teamId);
    if (!isManager(findMember(members, request.auth.uid))) throw new HttpsError("permission-denied", "Only a team admin or captain can edit team details.");
    transaction.update(ref, { [keys[0]]: value, updatedAtEpochMs: Date.now() });
    return { teamId };
  });
});
