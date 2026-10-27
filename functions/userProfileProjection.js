const PUBLIC_PROFILE_FIELDS = [
  "name",
  "profilePhotoUrl",
  "location",
  "joinedLabel",
  "gender",
  "role",
  "battingStyle",
  "bowlingStyle"
];

function projectPublicProfile(userId, source = {}) {
  const projection = { id: userId };
  for (const field of PUBLIC_PROFILE_FIELDS) {
    projection[field] = typeof source[field] === "string" ? source[field] : "";
  }
  for (const field of ["createdAt", "updatedAt"]) {
    if (source[field] !== undefined && source[field] !== null) projection[field] = source[field];
  }
  return projection;
}

module.exports = { projectPublicProfile };
