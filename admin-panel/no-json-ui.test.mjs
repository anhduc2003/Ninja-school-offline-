import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const styles = readFileSync(new URL("./public/styles.css", import.meta.url), "utf8");

describe("active panel UI without JSON controls", () => {
  it("routes inventory, events, options and jobs to visual renderers", () => {
    const dispatcher = app.slice(app.lastIndexOf("async function openModule(id)"));
    expect(dispatcher).toContain('await inventoryModuleVisual()');
    expect(dispatcher).toContain('await eventModuleVisual()');
    expect(dispatcher).toContain('await optionsModuleVisual("rates")');
    expect(dispatcher).toContain('await jobsModuleVisual()');
  });

  it("uses summary cards and row builders instead of raw JSON fields in active visual renderers", () => {
    const visual = app.slice(app.indexOf("function inventoryBlockSummary"), app.lastIndexOf("async function openModule(id)"));
    expect(visual).toContain("inventory-summary-card");
    expect(visual).toContain("event-drop-builder");
    expect(visual).toContain("data-event-drop-choose");
    expect(visual).not.toContain("Drop JSON");
    expect(visual).not.toContain("json-view");
    expect(styles).toContain(".event-drop-row");
    expect(styles).toContain(".inventory-summary-grid");
  });
});
