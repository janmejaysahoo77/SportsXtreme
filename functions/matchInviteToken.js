"use strict";

const crypto = require("node:crypto");

const TOKEN_PATTERN = /^[A-Za-z0-9_-]{43}$/;
const INVITE_ID_PATTERN = /^[A-Za-z0-9_-]{1,200}$/;

function parseMatchInviteToken(value) {
  if (typeof value !== "string") return null;
  const token = value.trim();
  const separator = token.indexOf(".");
  if (separator <= 0 || separator !== token.lastIndexOf(".")) return null;
  const inviteId = token.slice(0, separator);
  const secret = token.slice(separator + 1);
  if (!INVITE_ID_PATTERN.test(inviteId) || !TOKEN_PATTERN.test(secret)) return null;
  return { inviteId, secret };
}

function hashMatchInviteSecret(secret) {
  return crypto.createHash("sha256").update(secret, "utf8").digest("hex");
}

function matchInviteUnavailableReason(status, expiresAtMs, nowMs = Date.now()) {
  if (status !== "OPEN") return "closed";
  if (!Number.isFinite(expiresAtMs) || expiresAtMs <= nowMs) return "expired";
  return null;
}

module.exports = { parseMatchInviteToken, hashMatchInviteSecret, matchInviteUnavailableReason };
