import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");
const accountStart = app.indexOf("async function accountModule()");
const accountEnd = app.indexOf("async function shopModuleLegacy", accountStart);
const accountModule = app.slice(accountStart, accountEnd);

describe("account creation UI", () => {
  it("keeps the form reference across the async confirmation callback", () => {
    expect(accountModule).toContain("const form = event.currentTarget;");
    expect(accountModule).toContain("new FormData(form)");
    expect(accountModule).toContain("form.reset(); await accountModule();");
    expect(accountModule).not.toContain("event.currentTarget.reset();");
  });
});
