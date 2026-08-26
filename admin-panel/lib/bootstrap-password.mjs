export const DEFAULT_BOOTSTRAP_PASSWORD = "1";

export function resolveBootstrapPassword(override) {
  const value = String(override || "");
  return value || DEFAULT_BOOTSTRAP_PASSWORD;
}
