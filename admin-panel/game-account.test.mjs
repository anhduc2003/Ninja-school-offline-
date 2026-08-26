import { describe, expect, it } from "vitest";
import { hashGamePassword, validateGameUsername, verifyGamePassword } from "./lib/game-account.mjs";

describe("game account credentials", () => {
  it("only accepts conservative game usernames", () => {
    expect(validateGameUsername("Ninja_2026")).toBe(true);
    expect(validateGameUsername("ab")).toBe(false);
    expect(validateGameUsername("ninja space")).toBe(false);
    expect(validateGameUsername("ninja<script>")).toBe(false);
  });

  it("creates a Java-compatible bcrypt $2y$ password hash", async () => {
    const hash = await hashGamePassword("MatKhauGame_2026");
    expect(hash.startsWith("$2y$")).toBe(true);
    await expect(verifyGamePassword("MatKhauGame_2026", hash)).resolves.toBe(true);
    await expect(verifyGamePassword("wrong-password", hash)).resolves.toBe(false);
  });
});
