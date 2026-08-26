import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const root = new URL("..", import.meta.url);
const read = path => readFileSync(new URL(path, root), "utf8");

describe("Gift Code lifecycle contract", () => {
  it("defines lifecycle columns for clean installs and existing database migration", () => {
    const schema = read("SQL/nsoz.sql");
    const migration = read("scripts/migrate-gift-code-lifecycle.sh");
    for (const column of ["starts_at", "max_redemptions", "redemption_count", "disabled"]) {
      expect(schema).toContain(`\`${column}\``);
      expect(migration).toContain(`has_column ${column}`);
    }
    expect(migration).toContain("gift_codes_lifecycle_idx");
    expect(migration).toContain("gift_codes_redemption_idx");
  });

  it("enforces runtime lifecycle and row-lock redemption before reward delivery", () => {
    const statements = read("src/main/java/Exe_Z/constants/SQLStatement.java");
    const runtime = read("src/main/java/Exe_Z/model/GiftCode.java");
    expect(statements).toContain("FOR UPDATE");
    expect(statements).toContain("`disabled` = 0");
    expect(statements).toContain("starts_at IS NULL OR starts_at <= now()");
    expect(statements).toContain("`redemption_count` = `redemption_count` + 1");
    expect(runtime).toContain("connection.setAutoCommit(false)");
    expect(runtime).toContain("connection.commit()");
    expect(runtime).toContain("item.expire = System.currentTimeMillis() + expireDays * 86_400_000L");
  });

  it("exposes panel lifecycle management and canonical reward validation", () => {
    const server = read("admin-panel/server.mjs");
    expect(server).toContain("normalizeGiftRewards");
    expect(server).toContain("/api/gift-code-control");
    expect(server).toContain("/api/gift-code-item-search");
    expect(server).toContain("/api/actions/gift-code-save");
    expect(server).toContain("/api/actions/gift-code-state");
    expect(server).toContain("/api/actions/gift-code-delete");
  });
});
