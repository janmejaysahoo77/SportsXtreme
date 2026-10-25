const { applicationDefault, initializeApp } = require("firebase-admin/app");
const { FieldPath, FieldValue, getFirestore } = require("firebase-admin/firestore");

const args = new Set(process.argv.slice(2));
const projectFlag = process.argv.find((arg) => arg.startsWith("--project="));
const projectId = projectFlag?.slice("--project=".length);
const applyChanges = args.has("--apply");

if (!projectId) {
  throw new Error("Pass an explicit --project=<firebase-project-id>. No data is changed by default; pass --apply to migrate.");
}

initializeApp({ credential: applicationDefault(), projectId });
const db = getFirestore();
const PAGE_SIZE = 200;
const CONTACT_FIELDS = ["email", "phoneNumber"];

async function migrateDocument(profileRef) {
  const profileSnapshot = await profileRef.get();
  if (!profileSnapshot.exists) return "missing";

  const profile = profileSnapshot.data();
  // Legacy generated-ID profiles cannot be safely associated with an Auth UID
  // from client-controlled profile data. Leave those for explicit review.
  if (profile.id != null && profile.id !== profileSnapshot.id) return "noncanonical";

  const legacyValues = {};
  for (const field of CONTACT_FIELDS) {
    if (Object.prototype.hasOwnProperty.call(profile, field)) {
      if (typeof profile[field] !== "string") return "invalid";
      legacyValues[field] = profile[field];
    }
  }
  if (Object.keys(legacyValues).length === 0) return "clean";

  const privateRef = profileRef.collection("private").doc("account");
  const privateSnapshot = await privateRef.get();
  const privateData = privateSnapshot.exists ? privateSnapshot.data() : {};
  const fieldsToCopy = {};

  for (const [field, legacyValue] of Object.entries(legacyValues)) {
    if (Object.prototype.hasOwnProperty.call(privateData, field)) {
      if (privateData[field] !== legacyValue) return "conflict";
    } else {
      fieldsToCopy[field] = legacyValue;
    }
  }

  if (applyChanges && Object.keys(fieldsToCopy).length > 0) {
    await privateRef.set({
      id: profileSnapshot.id,
      ...fieldsToCopy,
      updatedAt: FieldValue.serverTimestamp()
    }, { merge: true });
  }

  if (!applyChanges) return "ready";

  // Verify the durable private copy before considering removal of any public value.
  const verifiedPrivate = (await privateRef.get()).data() || {};
  for (const [field, legacyValue] of Object.entries(legacyValues)) {
    if (verifiedPrivate[field] !== legacyValue) return "verification-failed";
  }

  return db.runTransaction(async (transaction) => {
    const [currentProfile, currentPrivate] = await Promise.all([
      transaction.get(profileRef),
      transaction.get(privateRef)
    ]);
    if (!currentProfile.exists || !currentPrivate.exists) return "changed-during-migration";

    const currentSource = currentProfile.data();
    const currentTarget = currentPrivate.data();
    const deletions = {};
    for (const [field, legacyValue] of Object.entries(legacyValues)) {
      // Do not delete a value changed after it was copied, and do not delete
      // unless the transaction still sees the verified matching private value.
      if (currentSource[field] === legacyValue && currentTarget[field] === legacyValue) {
        deletions[field] = FieldValue.delete();
      }
    }
    if (Object.keys(deletions).length === 0) return "changed-during-migration";
    transaction.update(profileRef, deletions);
    return "migrated";
  });
}

async function main() {
  const counts = Object.create(null);
  let lastDocument = null;
  while (true) {
    let query = db.collection("users").orderBy(FieldPath.documentId()).limit(PAGE_SIZE);
    if (lastDocument) query = query.startAfter(lastDocument);
    const page = await query.get();
    if (page.empty) break;

    for (const profile of page.docs) {
      const result = await migrateDocument(profile.ref);
      counts[result] = (counts[result] || 0) + 1;
    }
    lastDocument = page.docs[page.docs.length - 1];
  }

  console.log(`${applyChanges ? "Migration" : "Dry run"} completed for ${projectId}.`);
  for (const [result, count] of Object.entries(counts).sort(([a], [b]) => a.localeCompare(b))) {
    console.log(`${result}: ${count}`);
  }
  if (!applyChanges) console.log("No data was changed. Review the counts, then rerun with --apply to migrate.");
}

main().catch((error) => {
  console.error("Legacy contact migration failed; no unverified public values were deleted.", error);
  process.exitCode = 1;
});
