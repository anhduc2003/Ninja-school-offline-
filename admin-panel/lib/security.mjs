import { createHash, randomBytes, scryptSync, timingSafeEqual } from "node:crypto";

export const roleRank = { viewer: 1, analyst: 2, moderator: 3, operator: 4, admin: 5 };

export function hashPassword(password, salt = randomBytes(16).toString("hex")) {
  const digest = scryptSync(password, salt, 64).toString("hex");
  return `${salt}:${digest}`;
}

export function verifyPassword(password, stored) {
  const [salt, digest] = String(stored).split(":");
  if (!salt || !digest) return false;
  const expected = Buffer.from(digest, "hex");
  const actual = scryptSync(password, salt, 64);
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}

export function tokenHash(token) {
  return createHash("sha256").update(token).digest("hex");
}

export function randomToken(bytes = 32) {
  return randomBytes(bytes).toString("base64url");
}

export function hasRole(user, minimum) {
  return Boolean(user && roleRank[user.role] >= roleRank[minimum]);
}
