import { describe, expect, it } from "vitest";
import { hashPassword, hasRole, tokenHash, verifyPassword } from "./lib/security.mjs";

describe("offline panel security", () => {
  it("stores only a salted password verifier and rejects an incorrect password", () => {
    const hash = hashPassword("local-admin-password", "f00dbabe");
    expect(hash).not.toContain("local-admin-password");
    expect(verifyPassword("local-admin-password", hash)).toBe(true);
    expect(verifyPassword("incorrect-password", hash)).toBe(false);
  });

  it("enforces role ordering for protected write operations", () => {
    expect(hasRole({ role: "operator" }, "moderator")).toBe(true);
    expect(hasRole({ role: "moderator" }, "operator")).toBe(false);
    expect(hasRole({ role: "admin" }, "admin")).toBe(true);
  });

  it("hashes session tokens deterministically without retaining raw tokens", () => {
    expect(tokenHash("session-token")).toHaveLength(64);
    expect(tokenHash("session-token")).toBe(tokenHash("session-token"));
  });
});
