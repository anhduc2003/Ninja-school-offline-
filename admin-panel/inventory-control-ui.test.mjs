import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");

describe("visual player inventory control contract", () => {
  it("renders row-based inventory controls instead of raw JSON textareas", () => {
    expect(app).toContain("Hành trang trực quan");
    expect(app).toContain("data-inventory-add");
    expect(app).toContain("data-inventory-choose-item");
    expect(app).toContain("data-inventory-option-add");
    expect(app).toContain("async function playerStateModule(playerId, state = {})");
    expect(app).not.toContain("Bag JSON");
  });

  it("serializes visual fields through the existing inventory update endpoint", () => {
    expect(app).toContain("readVisualInventory");
    expect(app).toContain('"/api/actions/player-inventory-update"');
    expect(app).toContain("APPLY INVENTORY ${row.id}");
    expect(server).toContain("validateInventory(body.inventory || {}, { bag: before.numberCellBag, box: before.numberCellBox })");
    expect(server).toContain("inventoryOptionIds(payload)");
    expect(server).toContain("Option item không tồn tại trong catalog");
  });
});
