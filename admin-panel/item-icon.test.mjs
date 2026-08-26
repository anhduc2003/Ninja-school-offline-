import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const styles = readFileSync(new URL("./public/styles.css", import.meta.url), "utf8");

describe("item icon panel contract", () => {
  it("serves only bounded Small sprites through a validated local endpoint", () => {
    expect(server).toContain('url.pathname === "/api/item-icon"');
    expect(server).toContain('resolve(ROOT_DIR, "Data", "Img", "Small")');
    expect(server).toContain('zoom < 1 || zoom > 4');
    expect(server).toContain('`Small${icon}.png`');
    expect(server).toContain('file.startsWith(`${ITEM_SPRITE_DIR}/`)');
  });

  it("uses one thumbnail helper and integrates it into inventory, Shop NPC, Gift Code and item tables", () => {
    expect(app).toContain("const itemThumb");
    expect(app).toContain("inventoryItemCard");
    expect(app).toContain("itemThumb(template?.icon");
    expect(app).toContain("itemThumb(row.item_icon");
    expect(app).toContain("itemThumb(item.icon");
    expect(app).toContain('table(rows, ["icon","id","name","type"]');
    expect(styles).toContain(".item-thumb");
    expect(styles).toContain("image-rendering: pixelated");
  });
});
