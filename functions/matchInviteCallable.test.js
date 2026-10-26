"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
process.env.GCLOUD_PROJECT ||= "demo-sportsxtreme-match-invite-test";
const { getFirestore, Timestamp } = require("firebase-admin/firestore");
const { hashMatchInviteSecret } = require("./matchInviteToken");
const functions = require("./index");
const db = getFirestore();

const organiserId = "invite-test-organiser";
const captainId = "invite-test-captain";
const inviteeId = "invite-test-outsider";
const teamId = "invite-test-team";
const matchId = "invite-test-match";
const secret = "C".repeat(43);

function rawToken(inviteId) { return `${inviteId}.${secret}`; }
function request(uid, data) { return { auth: { uid, token: {} }, data }; }

async function seedInvite(inviteId, { status = "OPEN", expiresAt = Date.now() + 60_000, tokenSecret = secret } = {}) {
  await db.collection("matchInvites").doc(inviteId).set({
    inviteId,
    matchId,
    teamSlot: "TEAM_A",
    organiserId,
    tokenHash: hashMatchInviteSecret(tokenSecret),
    status,
    createdAt: Timestamp.fromMillis(Date.now() - 1000),
    expiresAt: Timestamp.fromMillis(expiresAt),
    claimedByUserId: null,
    claimedAt: null
  });
}

test("callables resolve composite links and reject malformed or wrong-secret tokens", async () => {
  const id = `${matchId}-resolve-team_a`;
  await db.collection("matches").doc(matchId).set({ title: "Test match", teamAClaim: null, teamBClaim: null });
  await seedInvite(id);
  const valid = await functions.resolveMatchInvite.run(request(inviteeId, { token: rawToken(id) }));
  assert.equal(valid.matchId, matchId);
  assert.equal(valid.teamSlot, "TEAM_A");
  await assert.rejects(functions.resolveMatchInvite.run(request(inviteeId, { token: secret })), { code: "invalid-argument" });
  await assert.rejects(functions.resolveMatchInvite.run(request(inviteeId, { token: `${id}.${"D".repeat(43)}` })), { code: "not-found" });
});

test("claim callable enforces expiry, prior claims, captain authorization, and commits a valid claim", async () => {
  await db.collection("teams").doc(teamId).set({
    teamName: "Invite Test Team",
    ownerUserId: captainId,
    members: [{ userId: captainId, roles: ["CAPTAIN"] }]
  });
  await db.collection("users").doc(captainId).set({ name: "Invite Test Captain" });
  const expiredId = `${matchId}-expired-team_a`;
  const claimedId = `${matchId}-claimed-team_a`;
  const unauthorizedId = `${matchId}-unauthorized-team_a`;
  const validId = `${matchId}-valid-team_a`;
  await seedInvite(expiredId, { expiresAt: Date.now() - 1 });
  await seedInvite(claimedId, { status: "CLAIMED" });
  await seedInvite(unauthorizedId);
  await seedInvite(validId);

  await assert.rejects(functions.claimMatchInviteForTeam.run(request(captainId, { token: rawToken(expiredId), teamId })), { code: "deadline-exceeded" });
  await assert.rejects(functions.claimMatchInviteForTeam.run(request(captainId, { token: rawToken(claimedId), teamId })), { code: "failed-precondition" });
  await assert.rejects(functions.claimMatchInviteForTeam.run(request(inviteeId, { token: rawToken(unauthorizedId), teamId })), { code: "permission-denied" });

  const result = await functions.claimMatchInviteForTeam.run(request(captainId, { token: rawToken(validId), teamId }));
  assert.equal(result.matchId, matchId);
  assert.equal(result.teamId, teamId);
  assert.equal((await db.collection("matchInvites").doc(validId).get()).get("status"), "CLAIMED");
  assert.equal((await db.collection("matches").doc(matchId).get()).get("teamAClaim").teamId, teamId);
});

test("organiser lookup is authenticated and does not return the token hash", async () => {
  const id = `${matchId}-lookup-team_a`;
  await seedInvite(id);
  await assert.rejects(functions.getMatchInviteForOrganiser.run({ data: { inviteId: id } }), { code: "unauthenticated" });
  await assert.rejects(functions.getMatchInviteForOrganiser.run(request(inviteeId, { inviteId: id })), { code: "permission-denied" });
  const result = await functions.getMatchInviteForOrganiser.run(request(organiserId, { inviteId: id }));
  assert.equal(result.inviteId, id);
  assert.equal(Object.hasOwn(result, "tokenHash"), false);
});
