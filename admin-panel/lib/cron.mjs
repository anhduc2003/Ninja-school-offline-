function fieldMatches(field, value) {
  return field.split(",").some(part => {
    if (part === "*") return true;
    const step = part.match(/^\*\/(\d+)$/);
    if (step) return value % Number(step[1]) === 0;
    const range = part.match(/^(\d+)-(\d+)(?:\/(\d+))?$/);
    if (range) {
      const start = Number(range[1]); const end = Number(range[2]); const increment = Number(range[3] || 1);
      return value >= start && value <= end && (value - start) % increment === 0;
    }
    return Number(part) === value;
  });
}

export function isCronDue(expression, date = new Date()) {
  const fields = String(expression).trim().split(/\s+/);
  if (fields.length !== 6) return false;
  const values = [date.getUTCSeconds(), date.getUTCMinutes(), date.getUTCHours(), date.getUTCDate(), date.getUTCMonth() + 1, date.getUTCDay()];
  return fields.every((field, index) => fieldMatches(field, values[index]));
}

export function runKey(date = new Date()) {
  return [date.getUTCFullYear(), date.getUTCMonth() + 1, date.getUTCDate(), date.getUTCHours(), date.getUTCMinutes(), date.getUTCSeconds()].join("-");
}
