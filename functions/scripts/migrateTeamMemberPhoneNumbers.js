"use strict";

const { initializeApp, getApps } = require("firebase-admin/app");
const { getFirestore } = require("firebase-admin/firestore");

const args = new Set(process.argv.slice(2));
const applyChanges = args.has("--apply");
const verifyOnly = args.has("--verify");
if ([...args].some((arg) => !["--apply", "--verify"].includes(arg)) || args.size > 1 || (applyChanges && verifyOnly)) {
  throw new Error("Usage: node scripts/migrateTeamMemberPhoneNumbers.js [--apply|--verify]");
}

const projectId = process.env.GCLOUD_PROJECT || process.env.GOOGLE_CLOUD_PROJECT;
if (!projectId) {
  throw new Error("Set GCLOUD_PROJECT explicitly to the intended Firebase project.");
}
if (getApps().length === 0) initializeApp({ projectId });
const db = getFirestore();

function hasPhoneNumber(member) {
  return member && typeof member === "object" && !Array.isArray(member)
    && Object.prototype.hasOwnProperty.call(member, "phoneNumber");
}

async function main() {
  const teams = await db.collection("teams").get();
  let teamsWithContacts = 0;
  let contactsMoved = 0;
  let teamsMissingMemberIds = 0;

  for (const teamDocument of teams.docs) {
    const members = Array.isArray(teamDocument.get("members")) ? teamDocument.get("members") : [];
    const contacts = members.filter(hasPhoneNumber);
    if (contacts.length === 0) continue;

    const invalidContact = contacts.find((member) =>
      typeof member.userId !== "string" || !member.userId || member.userId.includes("/"));
    if (invalidContact) {
      teamsMissingMemberIds += 1;
      console.error(`Cannot migrate team ${teamDocument.id}: a member with a phoneNumber has no usable userId.`);
      continue;
    }

    teamsWithContacts += 1;
    contactsMoved += contacts.length;
    if (!applyChanges) continue;

    const batch = db.batch();
    const sanitizedMembers = members.map((member) => {
      if (!hasPhoneNumber(member)) return member;
      const { phoneNumber, ...publicMember } = member;
      if (phoneNumber !== null && phoneNumber !== undefined && phoneNumber !== "") {
        batch.set(teamDocument.ref.collection("privateMemberContacts").doc(member.userId), { phoneNumber }, { merge: true });
      }
      return publicMember;
    });
    batch.update(teamDocument.ref, { members: sanitizedMembers });
    await batch.commit();
  }

  const mode = verifyOnly ? "verification" : applyChanges ? "migration" : "dry run";
  console.log(`${mode}: ${teamsWithContacts} team(s), ${contactsMoved} member contact field(s) found.`);
  if (teamsMissingMemberIds > 0) {
    console.error(`${teamsMissingMemberIds} team(s) need manual review; no changes were made to those teams.`);
    process.exitCode = 2;
  }
  if (verifyOnly && contactsMoved > 0) {
    console.error("Inline team member phoneNumber fields remain.");
    process.exitCode = 2;
  }
}

main().catch((error) => {
  console.error("Team contact migration failed:", error.message);
  process.exitCode = 1;
});
