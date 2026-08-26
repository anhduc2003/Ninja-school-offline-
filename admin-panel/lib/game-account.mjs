import bcrypt from "bcryptjs";

export function validateGameUsername(username) {
  return /^[A-Za-z0-9_]{3,30}$/.test(username);
}

export async function hashGamePassword(password) {
  const hash = await bcrypt.hash(password, 12);
  return hash.replace(/^\$2a\$/, "$2y$");
}

export async function verifyGamePassword(password, hash) {
  return bcrypt.compare(password, hash);
}
