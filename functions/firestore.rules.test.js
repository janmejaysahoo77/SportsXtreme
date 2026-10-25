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
  getDocs,
  getDoc,
  query,
  serverTimestamp,
  setDoc,
  updateDoc,
  where
} = require("firebase/firestore");

const projectId = "demo-sportsxtreme-firestore-rules-test";
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
    await setDoc(doc(db, "admins/moderator"), { role: "Moderator" });
    await setDoc(doc(db, "admins/support"), { role: "Support Agent" });
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

  await assertSucceeds(getDoc(doc(ownerDb, "users/alice/private/account")));
  await assertFails(getDoc(doc(otherDb, "users/alice/private/account")));
  await assertFails(getDoc(doc(anonymousDb, "users/alice/private/account")));
  await assertSucceeds(getDoc(doc(moderatorDb, "users/alice/private/account")));
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

  await assertSucceeds(updateDoc(doc(moderatorDb, "users/alice"), {
    isEmailVerified: true
  }));
  await assertSucceeds(updateDoc(doc(supportDb, "users/alice"), {
    isPhoneVerified: true
  }));
  await assertSucceeds(updateDoc(doc(moderatorDb, "users/alice/private/account"), {
    reviewNote: "Reviewed"
  }));
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
  await assertFails(getDoc(doc(otherDb, memberContact)));
  await assertFails(getDoc(doc(managerDb, memberContact)));
  await assertFails(getDocs(collection(otherDb, "teams/harbor/privateMemberContacts")));
  await assertFails(setDoc(doc(managerDb, memberContact), { phoneNumber: "+919123456789" }));
  await assertFails(setDoc(doc(otherDb, "teams/harbor/privateMemberContacts/injected"), { phoneNumber: "+919999999999" }));

  const managerTeams = query(collection(managerDb, "teams"), where("memberIds", "array-contains", "alice"));
  await assertSucceeds(getDocs(managerTeams));
});
