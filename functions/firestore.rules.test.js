const fs = require("node:fs");
const path = require("node:path");
const assert = require("node:assert/strict");
const { after, before, beforeEach, test } = require("node:test");
const {
  assertFails,
  assertSucceeds,
  initializeTestEnvironment
} = require("@firebase/rules-unit-testing");
const {
  collection,
  doc,
  deleteDoc,
  getDocs,
  getDoc,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where
} = require("firebase/firestore");

const projectId = "demo-sportsxtreme-firestore-rules-test";
process.env.GCLOUD_PROJECT ||= projectId;
let testEnvironment;

before(async () => {
  testEnvironment = await initializeTestEnvironment({
    projectId,
    firestore: {
      host: "127.0.0.1",
      port: 8580,
      rules: fs.readFileSync(path.join(__dirname, "..", "firestore.rules"), "utf8")
    }
  });
});

beforeEach(async () => {
  await testEnvironment.clearFirestore();
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    const db = context.firestore();
    await setDoc(doc(db, "users/alice"), {
      id: "alice",
      name: "Alice",
      createdAt: new Date(),
      updatedAt: new Date(),
      // Simulate fields from the legacy schema to prove clients cannot rewrite them.
      email: "alice@example.com",
      phoneNumber: "+15555550123",
      authProvider: "EMAIL_PASSWORD",
      isPro: false,
      isEmailVerified: false,
      isPhoneVerified: false,
      matchesPlayed: 0,
      wins: 0,
      bestScore: "",
      trophies: 0,
      hostedTournaments: 0,
      hostedSeries: 0,
      hostedMatches: 0,
      topPerformerStreak: false
    });
    await setDoc(doc(db, "users/alice/private/account"), {
      id: "alice",
      email: "alice@example.com",
      phoneNumber: "+15555550123"
    });
    await setDoc(doc(db, "users/ordinary"), {
      id: "ordinary",
      name: "Ordinary User",
      createdAt: new Date(),
      updatedAt: new Date()
    });
    await setDoc(doc(db, "publicProfiles/alice"), {
      id: "alice",
      name: "Alice",
      profilePhotoUrl: "https://example.invalid/alice.png",
      location: "Local ground",
      createdAt: new Date(),
      updatedAt: new Date()
    });
    await setDoc(doc(db, "admins/moderator"), { role: "Moderator" });
    await setDoc(doc(db, "admins/support"), { role: "Support Agent" });
    await setDoc(doc(db, "admins/super"), { role: "Super Admin" });
    await setDoc(doc(db, "teams/harbor"), {
      teamName: "Harbor CC",
      captainName: "Alice",
      captainMobile: "+919876543210",
      ownerUserId: "alice",
      memberIds: ["alice", "pending_random_member"],
      members: [
        { userId: "alice", roles: ["ADMIN", "CAPTAIN"], playingRole: "PLAYER" },
        { userId: "pending_random_member", displayName: "Manual Player", roles: [], playingRole: "PLAYER", isRegistered: false }
      ]
    });
    await setDoc(doc(db, "teams/harbor/privateMemberContacts/pending_random_member"), {
      phoneNumber: "+919123456789"
    });
    await setDoc(doc(db, "teams/harbor/internalMetadata/private-note"), {
      note: "Server-only team metadata"
    });
    await setDoc(doc(db, "matches/live"), {
      ownerId: "alice",
      status: "IN_PROGRESS",
      finalization: { state: "" },
      innings: []
    });
    await setDoc(doc(db, "matchInvites/live-team_a"), {
      inviteId: "live-team_a",
      matchId: "live",
      teamSlot: "TEAM_A",
      organiserId: "alice",
      tokenHash: "a".repeat(64),
      status: "OPEN",
      createdAt: new Date(Date.now() - 1000),
      expiresAt: new Date(Date.now() + 60_000),
      claimedByUserId: null,
      claimedAt: null
    });
    await setDoc(doc(db, "matches/live/deliveries/delivery-1"), { runs: 1 });
    for (const scorecardType of ["batting", "bowling", "summary"]) {
      await setDoc(doc(db, `matches/live/scorecard/${scorecardType}/entries/innings-1`), { inningsId: "innings-1" });
    }
    await setDoc(doc(db, "matches/live/correctionAudit/1"), {
      revision: 1,
      reason: "Audit reason",
      requestedBy: "alice",
      state: "OPEN"
    });
  });
});

after(async () => {
  await testEnvironment?.cleanup();
});

test("private account contacts are readable by the owner and existing user managers only", async () => {
  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  const moderatorDb = testEnvironment.authenticatedContext("moderator").firestore();
  const superAdminDb = testEnvironment.authenticatedContext("super").firestore();

  await assertSucceeds(getDoc(doc(ownerDb, "users/alice/private/account")));
  await assertFails(getDoc(doc(otherDb, "users/alice/private/account")));
  await assertFails(getDoc(doc(anonymousDb, "users/alice/private/account")));
  await assertSucceeds(getDoc(doc(moderatorDb, "users/alice/private/account")));
  await assertSucceeds(getDoc(doc(superAdminDb, "users/alice/private/account")));
});

test("user root documents are owner-only and cannot be queried by ordinary users", async () => {
  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  const moderatorDb = testEnvironment.authenticatedContext("moderator").firestore();

  await assertSucceeds(getDoc(doc(ownerDb, "users/alice")));
  await assertSucceeds(getDoc(doc(testEnvironment.authenticatedContext("ordinary").firestore(), "users/ordinary")));
  await assertFails(getDoc(doc(otherDb, "users/alice")));
  await assertFails(getDoc(doc(anonymousDb, "users/alice")));
  await assertFails(getDoc(doc(moderatorDb, "users/alice")));
  await assertFails(getDocs(collection(otherDb, "users")));
  await assertFails(getDocs(query(collection(otherDb, "users"), where("phoneNumber", "==", "+15555550123"))));
});

test("signed-in users can read safe public profiles while anonymous reads are denied", async () => {
  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  const projection = doc(otherDb, "publicProfiles/alice");

  await assertSucceeds(getDoc(projection));
  await assertSucceeds(getDocs(collection(otherDb, "publicProfiles")));
  await assertFails(getDoc(doc(anonymousDb, "publicProfiles/alice")));
  await assertFails(getDocs(collection(anonymousDb, "publicProfiles")));
  await assertSucceeds(updateDoc(doc(ownerDb, "publicProfiles/alice"), {
    name: "Updated public name",
    updatedAt: serverTimestamp()
  }));
  for (const [field, value] of Object.entries({
    email: "private@example.invalid",
    phoneNumber: "+15555550999",
    authProvider: "PHONE",
    isPhoneVerified: true,
    isEmailVerified: true,
    isPro: true
  })) {
    await assertFails(updateDoc(doc(ownerDb, "publicProfiles/alice"), {
      [field]: value,
      updatedAt: serverTimestamp()
    }));
  }
  await assertFails(setDoc(doc(otherDb, "publicProfiles/forged"), {
    id: "forged",
    name: "Forged",
    email: "private@example.invalid"
  }));
});

test("ordinary clients can create only a whitelisted public profile", async () => {
  const aliceDb = testEnvironment.authenticatedContext("new-user").firestore();
  const validProfile = {
    id: "new-user",
    name: "New User",
    createdAt: serverTimestamp(),
    updatedAt: serverTimestamp()
  };
  await assertSucceeds(setDoc(doc(aliceDb, "users/new-user"), validProfile));

  const forbiddenFields = {
    email: "new@example.com",
    phoneNumber: "+15555550123",
    authProvider: "GOOGLE",
    isPro: false,
    isEmailVerified: false,
    isPhoneVerified: false,
    matchesPlayed: 0,
    wins: 0,
    bestScore: "0",
    trophies: 0,
    hostedTournaments: 0,
    hostedSeries: 0,
    hostedMatches: 0,
    topPerformerStreak: false
  };

  for (const [field, value] of Object.entries(forbiddenFields)) {
    await assertFails(setDoc(doc(aliceDb, `users/injected-${field}`), {
      id: `injected-${field}`,
      name: "Injected",
      createdAt: serverTimestamp(),
      updatedAt: serverTimestamp(),
      [field]: value
    }));
  }
});

test("ordinary clients cannot modify contact, verification, provider, or authoritative stats", async () => {
  const ordinaryDb = testEnvironment.authenticatedContext("ordinary").firestore();
  const profileRef = doc(ordinaryDb, "users/ordinary");
  const ownAdminDoc = await getDoc(doc(ordinaryDb, "admins/ordinary"));
  assert.equal(ownAdminDoc.exists(), false, "ordinary test user must not have an Admin Panel role");

  await assertSucceeds(updateDoc(profileRef, {
    name: "Updated Ordinary User",
    location: "Local ground",
    updatedAt: serverTimestamp()
  }));

  const forbiddenUpdates = {
    email: "attacker@example.com",
    phoneNumber: "+15555550999",
    authProvider: "GOOGLE",
    isPro: true,
    isEmailVerified: true,
    isPhoneVerified: true,
    matchesPlayed: 999,
    wins: 999,
    bestScore: "999",
    trophies: 999,
    hostedTournaments: 999,
    hostedSeries: 999,
    hostedMatches: 999,
    topPerformerStreak: true
  };

  for (const [field, value] of Object.entries(forbiddenUpdates)) {
    try {
      await assertFails(updateDoc(profileRef, {
        [field]: value,
        updatedAt: serverTimestamp()
      }));
    } catch (error) {
      error.message = `${field}: ${error.message}`;
      throw error;
    }
  }
  await assertFails(setDoc(doc(ordinaryDb, "users/ordinary/private/account"), {
    id: "ordinary",
    email: "attacker@example.com",
    phoneNumber: "+15555550999",
    authProvider: "GOOGLE",
    isEmailVerified: true,
    isPhoneVerified: true
  }, { merge: true }));
});

test("existing admin user-management roles retain verification and private-account access", async () => {
  const moderatorDb = testEnvironment.authenticatedContext("moderator").firestore();
  const supportDb = testEnvironment.authenticatedContext("support").firestore();
  const superAdminDb = testEnvironment.authenticatedContext("super").firestore();

  await assertSucceeds(updateDoc(doc(moderatorDb, "users/alice/private/account"), {
    isEmailVerified: true
  }));
  await assertSucceeds(updateDoc(doc(supportDb, "users/alice/private/account"), {
    isPhoneVerified: true
  }));
  await assertSucceeds(updateDoc(doc(moderatorDb, "users/alice"), {
    name: "Reviewed public name",
    updatedAt: serverTimestamp()
  }));
  await assertFails(updateDoc(doc(moderatorDb, "users/alice"), {
    isEmailVerified: false
  }));
  await assertSucceeds(updateDoc(doc(moderatorDb, "users/alice/private/account"), {
    reviewNote: "Reviewed"
  }));
  await assertFails(getDoc(doc(moderatorDb, "users/alice")));
  await assertFails(getDocs(collection(moderatorDb, "users")));
  await assertFails(getDoc(doc(superAdminDb, "users/alice")));
  await assertSucceeds(updateDoc(doc(superAdminDb, "users/alice/private/account"), {
    isEmailVerified: false
  }));
  await assertSucceeds(getDoc(doc(supportDb, "users/alice/private/account")));
  await assertFails(getDoc(doc(testEnvironment.authenticatedContext("ordinary").firestore(), "users/alice/private/account")));
});

test("team rosters and captain contact remain readable while private member contacts stay server-only", async () => {
  const managerDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();

  const team = await getDoc(doc(otherDb, "teams/harbor"));
  assert.equal(team.get("captainMobile"), "+919876543210");
  assert.equal(team.get("members")[1].displayName, "Manual Player");
  assert.equal(team.get("members")[1].phoneNumber, undefined);
  await assertSucceeds(getDoc(doc(managerDb, "teams/harbor")));
  await assertFails(getDoc(doc(anonymousDb, "teams/harbor")));

  const memberContact = "teams/harbor/privateMemberContacts/pending_random_member";
  await assertFails(getDoc(doc(otherDb, "teams/harbor/internalMetadata/private-note")));
  await assertFails(getDoc(doc(otherDb, memberContact)));
  await assertFails(getDoc(doc(managerDb, memberContact)));
  await assertFails(getDocs(collection(otherDb, "teams/harbor/privateMemberContacts")));
  await assertFails(setDoc(doc(managerDb, memberContact), { phoneNumber: "+919123456789" }));
  await assertFails(setDoc(doc(otherDb, "teams/harbor/privateMemberContacts/injected"), { phoneNumber: "+919999999999" }));

  const managerTeams = query(collection(managerDb, "teams"), where("memberIds", "array-contains", "alice"));
  await assertSucceeds(getDocs(managerTeams));
});

test("match roots, deliveries, and required scorecard entries remain publicly readable", async () => {
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  await assertSucceeds(getDoc(doc(anonymousDb, "matches/live")));
  await assertSucceeds(getDoc(doc(anonymousDb, "matches/live/deliveries/delivery-1")));
  await assertSucceeds(getDocs(collection(anonymousDb, "matches/live/deliveries")));
  for (const scorecardType of ["batting", "bowling", "summary"]) {
    await assertSucceeds(getDoc(doc(anonymousDb, `matches/live/scorecard/${scorecardType}/entries/innings-1`)));
    await assertSucceeds(getDocs(collection(anonymousDb, `matches/live/scorecard/${scorecardType}/entries`)));
  }
});

test("only the match owner may write deliveries and required scorecard entries before finalization", async () => {
  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();

  await assertSucceeds(setDoc(doc(ownerDb, "matches/live/deliveries/delivery-2"), { runs: 4 }));
  await assertSucceeds(updateDoc(doc(ownerDb, "matches/live/deliveries/delivery-1"), { runs: 2 }));
  for (const scorecardType of ["batting", "bowling", "summary"]) {
    const entry = doc(ownerDb, `matches/live/scorecard/${scorecardType}/entries/innings-1`);
    await assertSucceeds(updateDoc(entry, { updated: true }));
  }

  await assertFails(setDoc(doc(otherDb, "matches/live/deliveries/attacker"), { runs: 999 }));
  await assertFails(updateDoc(doc(otherDb, "matches/live/deliveries/delivery-1"), { runs: 999 }));
  await assertFails(setDoc(doc(otherDb, "matches/live/scorecard/batting/entries/attacker"), { runs: 999 }));
});

test("correction audit records deny client reads and writes for every client role", async () => {
  const contexts = [
    testEnvironment.unauthenticatedContext(),
    testEnvironment.authenticatedContext("bob"),
    testEnvironment.authenticatedContext("alice")
  ];

  for (const context of contexts) {
    const db = context.firestore();
    const auditRef = doc(db, "matches/live/correctionAudit/1");
    await assertFails(getDoc(auditRef));
    await assertFails(getDocs(collection(db, "matches/live/correctionAudit")));
    await assertFails(setDoc(doc(db, "matches/live/correctionAudit/2"), { state: "FORGED" }));
    await assertFails(updateDoc(auditRef, { state: "FORGED" }));
    await assertFails(deleteDoc(auditRef));
  }
});

test("match invites deny all direct client reads and lists", async () => {
  const organiserDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();

  for (const db of [organiserDb, otherDb, anonymousDb]) {
    await assertFails(getDoc(doc(db, "matchInvites/live-team_a")));
    await assertFails(getDocs(collection(db, "matchInvites")));
  }
});

test("match invite creation is organiser-only and existing invites cannot be updated", async () => {
  const organiserDb = testEnvironment.authenticatedContext("alice").firestore();
  const otherDb = testEnvironment.authenticatedContext("bob").firestore();
  const freshInvite = {
    inviteId: "live-team_b",
    matchId: "live",
    teamSlot: "TEAM_B",
    organiserId: "alice",
    tokenHash: "b".repeat(64),
    status: "OPEN",
    createdAt: serverTimestamp(),
    expiresAt: new Date(Date.now() + 60_000),
    claimedByUserId: null,
    claimedAt: null
  };

  await assertSucceeds(setDoc(doc(organiserDb, "matchInvites/live-team_b"), freshInvite));
  await assertFails(setDoc(doc(otherDb, "matchInvites/forged"), { ...freshInvite, inviteId: "forged", organiserId: "bob" }));
  await assertFails(updateDoc(doc(organiserDb, "matchInvites/live-team_a"), { status: "CLAIMED", claimedByUserId: "alice" }));
  await assertFails(updateDoc(doc(otherDb, "matchInvites/live-team_a"), { status: "CLAIMED", claimedByUserId: "bob" }));
});

test("unrecognized match paths and scorecard types are denied", async () => {
  const anonymousDb = testEnvironment.unauthenticatedContext().firestore();
  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();

  await assertFails(getDoc(doc(anonymousDb, "matches/live/privateNotes/note-1")));
  await assertFails(setDoc(doc(ownerDb, "matches/live/privateNotes/note-1"), { secret: true }));
  await assertFails(getDoc(doc(anonymousDb, "matches/live/scorecard/fielding/entries/innings-1")));
  await assertFails(setDoc(doc(ownerDb, "matches/live/scorecard/fielding/entries/innings-1"), { runs: 1 }));
});

test("scoring writes stop after the parent match is finalized", async () => {
  await testEnvironment.withSecurityRulesDisabled(async (context) => {
    await updateDoc(doc(context.firestore(), "matches/live"), {
      status: "COMPLETED",
      finalization: { state: "FINALIZED" }
    });
  });

  const ownerDb = testEnvironment.authenticatedContext("alice").firestore();
  await assertFails(setDoc(doc(ownerDb, "matches/live/deliveries/late-delivery"), { runs: 6 }));
  await assertFails(updateDoc(doc(ownerDb, "matches/live/deliveries/delivery-1"), { runs: 6 }));
  await assertFails(setDoc(doc(ownerDb, "matches/live/scorecard/batting/entries/innings-1"), { runs: 99 }));
});

test("requestMatchCorrection stores its reason only in the trusted audit record", {
  skip: !process.env.FIRESTORE_EMULATOR_HOST
}, async () => {
  const { requestMatchCorrection } = require("./index");
  const { getFirestore: getAdminFirestore } = require("firebase-admin/firestore");
  const adminDb = getAdminFirestore();
  const matchRef = adminDb.collection("matches").doc("correction-private");
  const previousFinalizedAt = new Date("2026-01-01T00:00:00.000Z");
  await matchRef.set({
    ownerId: "alice",
    status: "COMPLETED",
    finalization: { state: "FINALIZED", revision: 2, finalizedAt: previousFinalizedAt }
  });

  await requestMatchCorrection.run({
    auth: { uid: "alice" },
    data: { matchId: "correction-private", reason: "Correct the recorded score" }
  });

  const updatedMatch = await matchRef.get();
  assert.equal(updatedMatch.get("finalization.state"), "CORRECTION_OPEN");
  assert.equal(updatedMatch.get("finalization.revision"), 3);
  assert.equal(updatedMatch.get("finalization.correctionReason"), undefined);
  assert.equal(updatedMatch.get("finalization.correctionBy"), undefined);
  assert.equal(updatedMatch.get("finalization.correctionRequestedAt"), undefined);

  const audit = await matchRef.collection("correctionAudit").doc("3").get();
  assert.equal(audit.get("reason"), "Correct the recorded score");
  assert.equal(audit.get("requestedBy"), "alice");
  assert.equal(audit.get("state"), "OPEN");
  assert.ok(audit.get("requestedAt"));

  await matchRef.update({
    matchId: "correction-private",
    matchType: "FRIENDLY",
    sport: "CRICKET",
    format: "T20",
    ballType: "LEATHER",
    overs: 20,
    teamA: { teamId: "team-a", name: "Team A", shortName: "A" },
    teamB: { teamId: "team-b", name: "Team B", shortName: "B" },
    playingXI: {
      TEAM_A: { teamId: "team-a", side: "TEAM_A", selectedByUserId: "alice", playerIds: ["a-player"] },
      TEAM_B: { teamId: "team-b", side: "TEAM_B", selectedByUserId: "alice", playerIds: ["b-player"] }
    },
    innings: [
      { inningsId: "innings-1", number: 1, battingTeamId: "team-a", bowlingTeamId: "team-b", status: "COMPLETED", score: 0, wickets: 0, legalBalls: 0 },
      { inningsId: "innings-2", number: 2, battingTeamId: "team-b", bowlingTeamId: "team-a", status: "COMPLETED", score: 0, wickets: 0, legalBalls: 0 }
    ]
  });
  for (const [inningsId, inningsNumber] of [["innings-1", 1], ["innings-2", 2]]) {
    await matchRef.collection("scorecard").doc("summary").collection("entries").doc(inningsId).set({
      inningsId,
      innings: inningsNumber,
      totalScore: 0,
      wickets: 0,
      legalBalls: 0
    });
  }
  const { finalizeMatch } = require("./index");
  await finalizeMatch.run({ auth: { uid: "alice" }, data: { matchId: "correction-private" } });

  const finalizedMatch = await matchRef.get();
  assert.equal(finalizedMatch.get("status"), "COMPLETED");
  assert.equal(finalizedMatch.get("finalization.state"), "FINALIZED");
  assert.equal(finalizedMatch.get("finalization.correctionReason"), undefined);
  const finalizedAudit = await matchRef.collection("correctionAudit").doc("3").get();
  assert.equal(finalizedAudit.get("reason"), "Correct the recorded score");
  assert.equal(finalizedAudit.get("state"), "FINALIZED");
  assert.ok(finalizedAudit.get("finalizedAt"));
});
