const crypto = require("crypto");
const { initializeApp } = require("firebase-admin/app");
const { getFirestore, FieldValue } = require("firebase-admin/firestore");
const { HttpsError, onCall } = require("firebase-functions/v2/https");

initializeApp();

const db = getFirestore();
const INVITE_URL_PREFIX = "https://sportsxtreme-95fbb.web.app/team-invite?token=";
const TEAM_INVITE_TTL_MS = 7 * 24 * 60 * 60 * 1000;

/**
 * Creates a single team invitation URL. The raw token is returned only in this
 * response; Firestore contains its SHA-256 hash, never the usable token.
 */
exports.createTeamInvite = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before creating an invitation.");
  }

  const teamId = typeof request.data?.teamId === "string" ? request.data.teamId.trim() : "";
  if (!teamId || teamId.length > 150) {
    throw new HttpsError("invalid-argument", "A valid team ID is required.");
  }

  const teamRef = db.collection("teams").doc(teamId);
  const teamSnapshot = await teamRef.get();
  if (!teamSnapshot.exists) {
    throw new HttpsError("not-found", "Team not found.");
  }
  if (teamSnapshot.get("ownerUserId") !== request.auth.uid) {
    throw new HttpsError("permission-denied", "Only the team owner can invite members.");
  }

  // A 256-bit token makes a collision impractical; the existence check keeps
  // the document ID collision-safe even in the event of an unexpected repeat.
  for (let attempt = 0; attempt < 5; attempt += 1) {
    const token = crypto.randomBytes(32).toString("base64url");
    const tokenHash = crypto.createHash("sha256").update(token, "utf8").digest("hex");
    const inviteRef = db.collection("teamInvites").doc(tokenHash);

    try {
      await db.runTransaction(async (transaction) => {
        if ((await transaction.get(inviteRef)).exists) {
          throw new Error("TOKEN_COLLISION");
        }
        transaction.create(inviteRef, {
          teamId,
          ownerUserId: request.auth.uid,
          status: "OPEN",
          createdAt: FieldValue.serverTimestamp(),
          expiresAt: new Date(Date.now() + TEAM_INVITE_TTL_MS)
        });
      });
      return { invitationUrl: `${INVITE_URL_PREFIX}${encodeURIComponent(token)}` };
    } catch (error) {
      if (error?.message !== "TOKEN_COLLISION") throw error;
    }
  }

  throw new HttpsError("internal", "Unable to create a unique invitation. Please try again.");
});

/**
 * Resolves and consumes a team invitation. This callable is the only path
 * that can inspect teamInvites or alter team membership.
 */
exports.joinTeamInvite = onCall(async (request) => {
  if (!request.auth) {
    throw new HttpsError("unauthenticated", "Sign in before joining a team.");
  }
  const token = typeof request.data?.token === "string" ? request.data.token.trim() : "";
  if (!/^[A-Za-z0-9_-]{43}$/.test(token)) {
    throw new HttpsError("invalid-argument", "Invalid invitation link.");
  }

  const tokenHash = crypto.createHash("sha256").update(token, "utf8").digest("hex");
  const inviteRef = db.collection("teamInvites").doc(tokenHash);
  const uid = request.auth.uid;

  try {
    return await db.runTransaction(async (transaction) => {
      const invite = await transaction.get(inviteRef);
      if (!invite.exists) throw new HttpsError("not-found", "Invitation is invalid.");

      const status = invite.get("status") || "OPEN"; // compatible with Phase 1 documents
      const usedByUserId = invite.get("usedByUserId");
      if (status === "USED" && usedByUserId !== uid) {
        throw new HttpsError("failed-precondition", "This invitation has already been used.");
      }
      if (status === "REVOKED") throw new HttpsError("failed-precondition", "This invitation has been revoked.");
      if (status !== "OPEN" && !(status === "USED" && usedByUserId === uid)) {
        throw new HttpsError("failed-precondition", "This invitation is no longer valid.");
      }
      const expiresAt = invite.get("expiresAt");
      if (expiresAt && expiresAt.toDate() <= new Date()) {
        throw new HttpsError("deadline-exceeded", "This invitation has expired.");
      }

      const teamId = invite.get("teamId");
      if (typeof teamId !== "string" || !teamId) {
        throw new HttpsError("failed-precondition", "Invitation is invalid.");
      }
      const teamRef = db.collection("teams").doc(teamId);
      const team = await transaction.get(teamRef);
      if (!team.exists) throw new HttpsError("not-found", "This team no longer exists.");

      const memberIds = Array.isArray(team.get("memberIds")) ? team.get("memberIds") : [];
      const members = Array.isArray(team.get("members")) ? team.get("members") : [];
      const alreadyMember = memberIds.includes(uid) || members.some((member) => member && member.userId === uid);
      if (!alreadyMember) {
        transaction.update(teamRef, {
          memberIds: [...new Set([...memberIds, uid])],
          members: [...members, { userId: uid, role: "MEMBER", joinedAtEpochMs: Date.now() }],
          updatedAtEpochMs: Date.now()
        });
      }
      if (status === "OPEN") {
        transaction.update(inviteRef, {
          status: "USED",
          usedByUserId: uid,
          usedAt: FieldValue.serverTimestamp()
        });
      }
      // The name comes from the team document read in this transaction, rather
      // than from the invite URL, so the app can safely welcome the new member.
      const storedTeamName = team.get("teamName") ?? team.get("name");
      const teamName = typeof storedTeamName === "string" && storedTeamName.trim()
        ? storedTeamName.trim()
        : "the team";
      return { teamId, teamName, alreadyMember };
    });
  } catch (error) {
    if (error instanceof HttpsError) throw error;
    console.error("joinTeamInvite failed", error);
    throw new HttpsError("internal", "Unable to join team. Please try again.");
  }
});
