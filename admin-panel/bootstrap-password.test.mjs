import { describe, expect, it } from "vitest";
import { DEFAULT_BOOTSTRAP_PASSWORD, resolveBootstrapPassword } from "./lib/bootstrap-password.mjs";

describe("panel bootstrap password", () => {
  it("uses the requested default only when no local override exists", () => {
    expect(DEFAULT_BOOTSTRAP_PASSWORD).toBe("1");
    expect(resolveBootstrapPassword(undefined)).toBe("1");
    expect(resolveBootstrapPassword("local-override")).toBe("local-override");
  });
});
