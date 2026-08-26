import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");

describe("visual Shop NPC contract", () => {
  it("provides store selection, catalog item choice, concise pricing and row actions", () => {
    expect(app).toContain("Cửa hàng NPC");
    expect(app).toContain("shop-store-picker");
    expect(app).toContain("shop-find-item");
    expect(app).toContain("shop-price-mode");
    expect(app).toContain("data-shop-row-edit");
    expect(app).toContain("data-shop-row-delete");
  });

  it("uses an option builder and server-side catalog validation before store_data writes", () => {
    expect(app).toContain("shop-option-add");
    expect(app).toContain("data-shop-option-remove");
    expect(server).toContain("normalizeShopOptions");
    expect(server).toContain("Option hàng hóa NPC không tồn tại");
    expect(server).toContain("optionCatalog: optionCatalog[0]");
  });
});
