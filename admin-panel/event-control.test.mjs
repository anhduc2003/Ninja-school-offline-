import { describe, expect, it } from "vitest";
import { EVENT_CATALOG, eventConfigFromPlan, findEvent, updateProperties, validateDropTable } from "./lib/event-control.mjs";

describe("event control", () => {
  it("publishes a safe-off option and the new Star Festival catalog entry", () => {
    expect(findEvent("off")?.className).toBe("Exe_Z.event.OFF");
    expect(findEvent("star_festival")?.eventId).toBe(9);
    expect(EVENT_CATALOG.some(event => event.key === "noel" && event.assetPath)).toBe(true);
  });

  it("accepts bounded drop tables and rejects unsafe rows", () => {
    expect(validateDropTable('[{"id":570,"percent":25}]')).toEqual([{ id: 570, percent: 25 }]);
    expect(() => validateDropTable('[{"id":-1,"percent":1}]')).toThrow(/id nguyên/);
    expect(() => validateDropTable("not-json")).toThrow(/JSON/);
  });

  it("updates only the event config keys while preserving unrelated properties", () => {
    const plan = { className: "Exe_Z.event.Noel", endAt: "2027-12-31T23:59:58" };
    const result = updateProperties("db.host=127.0.0.1\ngame.event=Exe_Z.event.OFF\n# keep\n", eventConfigFromPlan(plan));
    expect(result).toContain("db.host=127.0.0.1");
    expect(result).toContain("game.event=Exe_Z.event.Noel");
    expect(result).toContain("event.year=2027");
    expect(result).toContain("# keep");
  });
});
