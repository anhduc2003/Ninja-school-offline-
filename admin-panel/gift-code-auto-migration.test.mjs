import { readFileSync } from "node:fs";
import { describe, expect, it } from "vitest";

const launcher = readFileSync(new URL("../run-server.sh", import.meta.url), "utf8");
const migration = readFileSync(new URL("../scripts/migrate-gift-code-lifecycle.sh", import.meta.url), "utf8");
const app = readFileSync(new URL("./public/app.js", import.meta.url), "utf8");

describe("Gift Code lifecycle auto-migration", () => {
  it("runs the idempotent schema check after MariaDB and before Java/panel startup", () => {
    const startDb = launcher.indexOf('bash "${ROOT_DIR}/scripts/start-db.sh"');
    const migrate = launcher.indexOf('bash "${ROOT_DIR}/scripts/migrate-gift-code-lifecycle.sh"');
    const startJava = launcher.indexOf('bash "${ROOT_DIR}/scripts/start-server.sh"');
    const startPanel = launcher.indexOf('bash "${ROOT_DIR}/admin-panel/start-panel.sh"');
    expect(startDb).toBeGreaterThan(-1);
    expect(migrate).toBeGreaterThan(startDb);
    expect(migrate).toBeLessThan(startJava);
    expect(migrate).toBeLessThan(startPanel);
    expect(migration).toContain("SELECT 1 FROM information_schema.statistics");
    expect(migration).toContain("LIMIT 1;");
    expect(migration).toContain("Không import/reset dữ liệu cũ");
  });

  it("shows the migration log path when the panel detects an old schema", () => {
    expect(app).toContain("logs/gift-code-migration.log");
    expect(app).toContain("bash run-server.sh");
  });
});
