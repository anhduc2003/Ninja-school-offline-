import { describe, expect, it } from "vitest";
import { existingItemIds, validateInventory, validatePlayerStats } from "./lib/player-state.mjs";

describe("player stat and inventory guards", () => {
  it("allowlists valid player stat fields and canonical potential", () => {
    const patch = validatePlayerStats({ point: "100", spoint: 5, potential: "[15,6,7,8]" }, new Set(["point", "spoint", "potential"]));
    expect(patch).toEqual({ point: 100, spoint: 5, potential: [15, 6, 7, 8] });
    expect(() => validatePlayerStats({ numberCellBag: 31 }, new Set(["point"]))).toThrow("numberCellBag");
  });

  it("normalizes valid inventory and rejects unknown fields or duplicate indexes", () => {
    const input = { bag: JSON.stringify([{ id: 10, index: 0, isLock: true, quantity: 2 }]), box: "[]", equiped: "[]", fashion: "[]" };
    const result = validateInventory(input, { bag: 30, box: 30 });
    expect(result.itemIds.has(10)).toBe(true);
    expect(JSON.parse(result.payload.bag)[0]).toMatchObject({ id: 10, index: 0, isLock: true, quantity: 2 });
    expect(() => validateInventory({ ...input, bag: '[{"id":10,"index":0,"hack":1}]' })).toThrow("field không được");
    expect(() => validateInventory({ ...input, bag: '[{"id":10,"index":0},{"id":11,"index":0}]' })).toThrow("index trùng");
  });

  it("allows only pre-existing unknown item ids to remain in a legacy inventory", () => {
    expect(existingItemIds({ bag: '[{"id":7}]', box: "[]", equiped: "[]", fashion: "[]" })).toEqual(new Set([7]));
  });
});
