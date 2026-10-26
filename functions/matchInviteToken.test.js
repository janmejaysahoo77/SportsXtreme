"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const {
  parseMatchInviteToken,
  hashMatchInviteSecret,
  matchInviteUnavailableReason
} = require("./matchInviteToken");

const inviteId = "match_123-team_a";
const validSecret = "A".repeat(43);

test("parses the existing inviteId.secret link contract and hashes only the secret", () => {
  const parsed = parseMatchInviteToken(`${inviteId}.${validSecret}`);
  assert.deepEqual(parsed, { inviteId, secret: validSecret });
  assert.equal(hashMatchInviteSecret(parsed.secret), hashMatchInviteSecret(validSecret));
});

test("rejects malformed composite match invite tokens", () => {
  for (const token of ["", `${validSecret}`, `.${validSecret}`, `${inviteId}.short`, `${inviteId}.${validSecret}.extra`, "bad/id.secret"]) {
    assert.equal(parseMatchInviteToken(token), null);
  }
});

test("a wrong secret does not match the stored invite hash", () => {
  const storedHash = hashMatchInviteSecret(validSecret);
  const wrongSecret = `${validSecret.slice(0, -1)}B`;
  assert.notEqual(hashMatchInviteSecret(wrongSecret), storedHash);
});

test("invite availability rejects expired and already-claimed invitations", () => {
  assert.equal(matchInviteUnavailableReason("OPEN", 999, 1000), "expired");
  assert.equal(matchInviteUnavailableReason("CLAIMED", 2000, 1000), "closed");
  assert.equal(matchInviteUnavailableReason("OPEN", 2000, 1000), null);
});
