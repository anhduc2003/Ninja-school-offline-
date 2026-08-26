import { describe, expect, it } from "vitest";
import { isCronDue, runKey } from "./lib/cron.mjs";

describe("offline scheduler cron allowlist", () => {
  const date = new Date(Date.UTC(2026, 7, 26, 6, 30, 0));
  it("matches six-field UTC intervals without accepting malformed cron", () => {
    expect(isCronDue("0 */5 * * * *", date)).toBe(true);
    expect(isCronDue("0 */7 * * * *", date)).toBe(false);
    expect(isCronDue("*/5 * * * *", date)).toBe(false);
  });
  it("uses a stable key to avoid running a due job twice in one tick", () => {
    expect(runKey(date)).toBe("2026-8-26-6-30-0");
  });
});
