"use strict";

const test = require("node:test");
const assert = require("node:assert/strict");
const {
  consumePhoneRegistrationRateLimit,
  createPhoneRegistrationHandler,
  SHORT_WINDOW_LIMIT,
  SHORT_WINDOW_MS,
  LONG_WINDOW_LIMIT,
  LONG_WINDOW_MS
} = require("./phoneRegistration");

function memoryFirestore() {
  const documents = new Map();
  return {
    documents,
    collection(name) {
      return { doc: (id) => ({ key: `${name}/${id}` }) };
    },
    runTransaction(callback) {
      const writes = [];
      return callback({
        get: async (ref) => {
          const value = documents.get(ref.key);
          return { exists: value !== undefined, data: () => value };
        },
        set: (ref, value) => writes.push([ref.key, value])
      }).then((result) => {
        for (const [key, value] of writes) documents.set(key, value);
        return result;
      });
    }
  };
}

function request({ phoneNumber, appId = "1:app:web:android", ip = "203.0.113.7" } = {}) {
  return {
    app: appId ? { appId } : undefined,
    data: { phoneNumber },
    rawRequest: ip ? { ip } : undefined
  };
}

function codeIs(code) {
  return (error) => error.code === code;
}

test("legitimate registered and unregistered checks preserve the response contract", async () => {
  const lookedUp = [];
  const handler = createPhoneRegistrationHandler({
    checkRateLimit: async () => true,
    lookupPhone: async (phoneNumber) => {
      lookedUp.push(phoneNumber);
      return phoneNumber === "+919876543210";
    },
    logger: { info() {}, warn() {}, error() {} }
  });

  assert.deepEqual(await handler(request({ phoneNumber: "00 91 (98765) 43210" })), { registered: true });
  assert.deepEqual(await handler(request({ phoneNumber: "+1 202-555-0147" })), { registered: false });
  assert.deepEqual(lookedUp, ["+919876543210", "+12025550147"]);
});

test("invalid numbers are rejected generically and never reach Auth lookup", async () => {
  let lookupCount = 0;
  const handler = createPhoneRegistrationHandler({
    checkRateLimit: async () => true,
    lookupPhone: async () => { lookupCount += 1; return false; },
    logger: { info() {}, warn() {}, error() {} }
  });

  await assert.rejects(handler(request({ phoneNumber: "not a number" })), codeIs("invalid-argument"));
  await assert.rejects(handler(request({ phoneNumber: "+123" })), codeIs("invalid-argument"));
  assert.equal(lookupCount, 0);
});

test("App Check and missing trusted IP are rejected before processing", async () => {
  let lookupCount = 0;
  const handler = createPhoneRegistrationHandler({
    checkRateLimit: async () => true,
    lookupPhone: async () => { lookupCount += 1; return false; },
    logger: { info() {}, warn() {}, error() {} }
  });

  await assert.rejects(handler(request({ phoneNumber: "+12025550147", appId: null })), codeIs("failed-precondition"));
  await assert.rejects(handler(request({ phoneNumber: "+12025550147", ip: null })), codeIs("unavailable"));
  assert.equal(lookupCount, 0);
});

test("rate-limited abuse is blocked and logs only a hashed source, not the phone", async () => {
  const logs = [];
  let lookupCount = 0;
  const logger = Object.fromEntries(["info", "warn", "error"].map((level) => [level, (line) => logs.push(line)]));
  const handler = createPhoneRegistrationHandler({
    checkRateLimit: async () => false,
    lookupPhone: async () => { lookupCount += 1; return false; },
    logger
  });

  await assert.rejects(handler(request({ phoneNumber: "+12025550147" })), codeIs("resource-exhausted"));
  assert.equal(lookupCount, 0);
  assert.match(logs[0], /phone_registration_rate_limited/);
  assert.doesNotMatch(logs[0], /\+12025550147|203\.0\.113\.7/);
});

test("Firestore rate limiter enforces short and hourly windows, then resets", async () => {
  const db = memoryFirestore();
  const key = "a".repeat(64);
  const start = Math.floor(1_800_000_000_000 / LONG_WINDOW_MS) * LONG_WINDOW_MS;

  for (let count = 0; count < SHORT_WINDOW_LIMIT; count += 1) {
    assert.equal(await consumePhoneRegistrationRateLimit(db, key, start), true);
  }
  assert.equal(await consumePhoneRegistrationRateLimit(db, key, start), false);

  const secondShortWindow = start + SHORT_WINDOW_MS;
  for (let count = 0; count < SHORT_WINDOW_LIMIT; count += 1) {
    assert.equal(await consumePhoneRegistrationRateLimit(db, key, secondShortWindow), true);
  }
  const thirdShortWindow = start + (2 * SHORT_WINDOW_MS);
  for (let count = 0; count < SHORT_WINDOW_LIMIT; count += 1) {
    assert.equal(await consumePhoneRegistrationRateLimit(db, key, thirdShortWindow), true);
  }
  assert.equal(await consumePhoneRegistrationRateLimit(db, key, start + (3 * SHORT_WINDOW_MS)), false);

  const nextHour = Math.floor(start / LONG_WINDOW_MS) * LONG_WINDOW_MS + LONG_WINDOW_MS;
  assert.equal(await consumePhoneRegistrationRateLimit(db, key, nextHour), true);
});
