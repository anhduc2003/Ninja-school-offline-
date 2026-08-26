import { describe, expect, it } from "vitest";
import { availableColumns } from "./lib/schema.mjs";

describe("schema column allowlist", () => {
  it("keeps only approved fields that exist in the connected game schema", () => {
    const databaseColumns = new Set(["id", "name", "bag"]);
    expect(availableColumns(databaseColumns, ["id", "user_id", "name", "bag", "arbitrary_sql"]))
      .toEqual(["id", "name", "bag"]);
  });
});
