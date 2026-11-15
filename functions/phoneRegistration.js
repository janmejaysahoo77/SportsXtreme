"use strict";

const crypto = require("crypto");
const { HttpsError } = require("firebase-functions/v2/https");

const SHORT_WINDOW_MS = 15 * 60 * 1000;
const SHORT_WINDOW_LIMIT = 10;
const LONG_WINDOW_MS = 60 * 60 * 1000;
const LONG_WINDOW_LIMIT = 30;
const PHONE_CHECK_ERROR = "Could not verify this phone number. Try again.";

function normalizePhoneNumber(value) {
  if (typeof value !== "string") return null;
  let normalized = value.trim().replace(/[\s().-]/g, "");
  if (normalized.startsWith("00")) normalized = `+${normalized.slice(2)}`;
  if (!/^\+[1-9]\d{7,14}$/.test(normalized)) return null;
  return normalized;
}

function hashSourceIp(ip) {
  return crypto.createHash("sha256").update(ip).digest("hex");
}

function logEvent(logger, level, event, fields = {}) {
  logger[level](JSON.stringify({ event, ...fields }));
}

/**
 * Applies fixed-window IP limits in a Firestore transaction. A single document
 * per source keeps the limiter bounded even when a client sends many requests.
 */
async function consumePhoneRegistrationRateLimit(db, sourceHash, now = Date.now()) {
  const ref = db.collection("phoneRegistrationRateLimits").doc(sourceHash);
  const shortWindowStartMs = Math.floor(now / SHORT_WINDOW_MS) * SHORT_WINDOW_MS;
  const longWindowStartMs = Math.floor(now / LONG_WINDOW_MS) * LONG_WINDOW_MS;

  return db.runTransaction(async (transaction) => {
    const snapshot = await transaction.get(ref);
    const prior = snapshot.exists ? snapshot.data() : {};
    const shortCount = prior.shortWindowStartMs === shortWindowStartMs
      ? (prior.shortCount || 0)
      : 0;
    const longCount = prior.longWindowStartMs === longWindowStartMs
      ? (prior.longCount || 0)
      : 0;

    if (shortCount >= SHORT_WINDOW_LIMIT || longCount >= LONG_WINDOW_LIMIT) {
      return false;
    }

    transaction.set(ref, {
      shortWindowStartMs,
      shortCount: shortCount + 1,
      longWindowStartMs,
      longCount: longCount + 1,
      updatedAt: new Date(now)
    });
    return true;
  });
}

function createPhoneRegistrationHandler({ lookupPhone, checkRateLimit, logger = console }) {
  return async (request) => {
    const appId = request.app?.appId;
    if (!appId) {
      logEvent(logger, "warn", "phone_registration_app_check_rejected");
      throw new HttpsError("failed-precondition", PHONE_CHECK_ERROR);
    }

    // Firebase's trusted Express IP is used; caller-provided forwarding headers
    // are deliberately ignored so clients cannot choose their own rate-limit key.
    const ip = request.rawRequest?.ip;
    if (typeof ip !== "string" || !ip.trim()) {
      logEvent(logger, "warn", "phone_registration_source_unavailable", { appId });
      throw new HttpsError("unavailable", PHONE_CHECK_ERROR);
    }
    const sourceHash = hashSourceIp(ip.trim());

    let allowed;
    try {
      allowed = await checkRateLimit(sourceHash);
    } catch (error) {
      logEvent(logger, "error", "phone_registration_rate_limit_failed", {
        sourceHash,
        errorCode: error?.code || "unknown"
      });
      throw new HttpsError("internal", PHONE_CHECK_ERROR);
    }
    if (!allowed) {
      logEvent(logger, "warn", "phone_registration_rate_limited", { sourceHash, appId });
      throw new HttpsError("resource-exhausted", "Too many attempts. Please try again later.");
    }

    const phoneNumber = normalizePhoneNumber(request.data?.phoneNumber);
    if (!phoneNumber) {
      logEvent(logger, "warn", "phone_registration_invalid_input", { sourceHash, appId });
      throw new HttpsError("invalid-argument", PHONE_CHECK_ERROR);
    }

    try {
      const registered = await lookupPhone(phoneNumber);
      logEvent(logger, "info", "phone_registration_check_completed", { sourceHash, appId });
      // Keep the existing response contract. The app needs this boolean to
      // block registered numbers before navigating to the OTP screen.
      return { registered: Boolean(registered) };
    } catch (error) {
      logEvent(logger, "error", "phone_registration_lookup_failed", {
        sourceHash,
        appId,
        errorCode: error?.code || "unknown"
      });
      throw new HttpsError("internal", PHONE_CHECK_ERROR);
    }
  };
}

module.exports = {
  SHORT_WINDOW_LIMIT,
  SHORT_WINDOW_MS,
  LONG_WINDOW_LIMIT,
  LONG_WINDOW_MS,
  consumePhoneRegistrationRateLimit,
  createPhoneRegistrationHandler,
  normalizePhoneNumber
};
