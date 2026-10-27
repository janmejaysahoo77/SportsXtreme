const assert = require("node:assert/strict");
const { test } = require("node:test");
const { projectPublicProfile } = require("./userProfileProjection");

test("public profile projection copies only approved fields", () => {
  const createdAt = new Date("2026-01-01T00:00:00.000Z");
  const projection = projectPublicProfile("user-123", {
    id: "different-id",
    name: "Example Player",
    profilePhotoUrl: "https://example.invalid/photo.png",
    location: "Local ground",
    joinedLabel: "Joined recently",
    gender: "Unspecified",
    role: "Batter",
    battingStyle: "Right hand",
    bowlingStyle: "",
    email: "private@example.invalid",
    phoneNumber: "+15555550123",
    authProvider: "PHONE",
    isEmailVerified: true,
    isPhoneVerified: true,
    isPro: true,
    createdAt,
    updatedAt: createdAt
  });

  assert.deepEqual(projection, {
    id: "user-123",
    name: "Example Player",
    profilePhotoUrl: "https://example.invalid/photo.png",
    location: "Local ground",
    joinedLabel: "Joined recently",
    gender: "Unspecified",
    role: "Batter",
    battingStyle: "Right hand",
    bowlingStyle: "",
    createdAt,
    updatedAt: createdAt
  });
});

test("missing public profile fields receive safe empty defaults", () => {
  assert.deepEqual(projectPublicProfile("user-456"), {
    id: "user-456",
    name: "",
    profilePhotoUrl: "",
    location: "",
    joinedLabel: "",
    gender: "",
    role: "",
    battingStyle: "",
    bowlingStyle: ""
  });
});
