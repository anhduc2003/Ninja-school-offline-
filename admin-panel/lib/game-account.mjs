import bcrypt from "bcryptjs";
import { timingSafeEqual } from "node:crypto";

export function validateGameUsername(username) {
  return /^[A-Za-z0-9_]{3,30}$/.test(username);
}

export async function hashGamePassword(password) {
  const hash = await bcrypt.hash(password, 12);
  return hash.replace(/^\$2a\$/, "$2y$");
}

export async function verifyGamePassword(password, hash) {
  const stored = String(hash || "");
  const candidate = String(password || "");
  if (!stored || !candidate) return false;
  if (/^\$2[aby]\$/.test(stored)) return bcrypt.compare(candidate, stored);
  const expected = Buffer.from(stored, "utf8");
  const actual = Buffer.from(candidate, "utf8");
  return expected.length === actual.length && timingSafeEqual(expected, actual);
}
