# Private team-member phone migration

Manually entered player phone numbers are stored at
`teams/{teamId}/privateMemberContacts/{memberId}`. The public team `members`
array keeps roster and role fields but no longer contains `phoneNumber`.
Team-level `captainMobile` remains on the public team document by design.
Client rules deny reads and writes to team subcollections; existing
team-management callable functions use the Admin SDK and continue to access
private contacts server-side.

## Before deploying the code and rules

1. Pause team-member changes while migrating so no team updates race the
   migration.
2. Take a Firestore backup or export that can restore the original `teams`
   collection and its document shapes.
3. Set `GCLOUD_PROJECT` explicitly to the intended project and run the default
   dry run from `functions/`:

   ```powershell
   $env:GCLOUD_PROJECT = "your-project-id"
   node scripts/migrateTeamMemberPhoneNumbers.js
   ```

   The script reports team and contact counts only; it never prints phone
   numbers. Teams with a contact but no usable `userId` are reported and left
   untouched for manual review.
4. After reviewing the counts and backup, run the migration:

   ```powershell
   node scripts/migrateTeamMemberPhoneNumbers.js --apply
   node scripts/migrateTeamMemberPhoneNumbers.js --verify
   ```

   `--apply` atomically copies each team's inline member contacts to private
   documents and removes `phoneNumber` from that team's public `members` array.
   `--verify` must report zero inline contact fields before deploying the new
   rules. The script is idempotent and can be rerun after interruption.
5. Deploy the updated Functions and Firestore rules together, then resume team
   changes.

Do not deploy the rules before the migration: Firestore rules cannot redact a
field from an otherwise readable team document. The new rules protect private
subcollections, while the migration removes legacy phone fields from readable
team documents.

## Rollback

Keep the migrated private contact documents when rolling back app releases;
they are an additive data location. Keep the rules that deny client access to
team subcollections, and keep the patched `addTeamPlayersByMobile` callable (or
disable team-member additions while rolling back that callable). Do not restore
phone numbers to public team documents or restore the old nested read rule.
If database restoration is necessary, restore the backup to a controlled
environment first, re-run the dry-run and migration, and verify before exposing
the restored team documents to clients.

The migration intentionally preserves existing member IDs so historical match
and scorecard references remain stable. Newly added placeholders use random
IDs instead of deriving IDs from phone numbers. Existing `pending_` IDs created
from phone hashes remain a residual offline-guessing risk until a separate,
reference-aware migration can update historical scorecards and leaderboard
records without breaking player history.
