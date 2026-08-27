import { describe, expect, it } from "vitest";
import { readFileSync } from "node:fs";

const server = readFileSync(new URL("./server.mjs", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");

describe("Monster icon rendering", () => {
  it("serves mob sprites from the game Data/Img/Mob asset tree", () => {
    expect(server).toContain('const MOB_SPRITE_DIR = resolve(ROOT_DIR, "Data", "Img", "Mob");');
    expect(server).toContain("/api/monster-icon");
    expect(server).toContain("${id}_${frame}.png");
    expect(server).toContain("X-Content-Type-Options");
  });

  it("renders monster thumbnails in Quái & Boss instead of item thumbnails", () => {
    expect(app).toContain("const mobThumb =");
    expect(app).toContain("/api/monster-icon?id=${id}");
    expect(app).toContain('table(rows, ["icon","id","name","level","boss","hp","range_move","speed"]');
    expect(app).toContain("row.mob_icon !== undefined");
  });
});
