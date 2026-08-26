export function availableColumns(columns, candidates) {
  return candidates.filter(column => columns.has(column));
}
