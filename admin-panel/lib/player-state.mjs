const ITEM_KEYS = new Set(["id", "expire", "new", "updated_at", "created_at", "index", "isLock", "yen", "sys", "upgrade", "options", "gems", "quantity", "material"]);
const STAT_LIMITS = {
  point: [0, 2_000_000_000],
  spoint: [0, 2_000_000_000],
  numberCellBag: [1, 200],
  numberCellBox: [1, 200],
  exp: [0, 9_000_000_000_000_000],
};

function integer(value, label, min, max) {
  const number = Number(value);
  if (!Number.isSafeInteger(number) || number < min || number > max) throw new Error(`${label} không hợp lệ.`);
  return number;
}

function jsonArray(value, label) {
  let parsed;
  try { parsed = typeof value === "string" ? JSON.parse(value) : value; } catch { throw new Error(`${label} phải là JSON hợp lệ.`); }
  if (!Array.isArray(parsed)) throw new Error(`${label} phải là JSON array.`);
  return parsed;
}

function optionList(value, label) {
  const options = jsonArray(value, label);
  if (options.length > 32) throw new Error(`${label} vượt quá 32 option.`);
  return options.map((entry, index) => {
    if (!Array.isArray(entry) || entry.length !== 2) throw new Error(`${label}[${index}] phải có hai số.`);
    return [integer(entry[0], `${label}[${index}][0]`, 0, 100_000), integer(entry[1], `${label}[${index}][1]`, -2_000_000_000, 2_000_000_000)];
  });
}

function normalizeItem(raw, label, ids, depth = 0) {
  if (!raw || typeof raw !== "object" || Array.isArray(raw)) throw new Error(`${label} phải là object item.`);
  if (depth > 1) throw new Error(`${label} chỉ hỗ trợ tối đa một lớp gems.`);
  for (const key of Object.keys(raw)) if (!ITEM_KEYS.has(key)) throw new Error(`${label} chứa field không được game contract cho phép: ${key}.`);
  const normalized = {
    id: integer(raw.id, `${label}.id`, 0, 100_000),
    expire: raw.expire === undefined ? -1 : integer(raw.expire, `${label}.expire`, -1, Number.MAX_SAFE_INTEGER),
    index: raw.index === undefined ? 0 : integer(raw.index, `${label}.index`, 0, 200),
    isLock: raw.isLock === undefined ? false : Boolean(raw.isLock),
    yen: raw.yen === undefined ? 0 : integer(raw.yen, `${label}.yen`, 0, 2_000_000_000),
  };
  ids.add(normalized.id);
  if (raw.new !== undefined) { if (typeof raw.new !== "boolean") throw new Error(`${label}.new phải là boolean.`); normalized.new = raw.new; }
  for (const key of ["created_at", "updated_at"]) if (raw[key] !== undefined) normalized[key] = integer(raw[key], `${label}.${key}`, 0, Number.MAX_SAFE_INTEGER);
  for (const key of ["sys", "upgrade"]) if (raw[key] !== undefined) normalized[key] = integer(raw[key], `${label}.${key}`, 0, 127);
  if (raw.options !== undefined) normalized.options = optionList(raw.options, `${label}.options`);
  if (raw.quantity !== undefined) normalized.quantity = integer(raw.quantity, `${label}.quantity`, 1, 2_000_000_000);
  if (raw.material !== undefined) {
    const material = jsonArray(raw.material, `${label}.material`);
    if (material.length !== 7) throw new Error(`${label}.material phải gồm 7 số.`);
    normalized.material = material.map((number, index) => integer(number, `${label}.material[${index}]`, 0, 2_000_000_000));
  }
  if (raw.gems !== undefined) {
    const gems = jsonArray(raw.gems, `${label}.gems`);
    if (gems.length > 4) throw new Error(`${label}.gems vượt quá 4 slot.`);
    normalized.gems = gems.map((gem, index) => normalizeItem(gem, `${label}.gems[${index}]`, ids, depth + 1));
  }
  return normalized;
}

export function validatePlayerStats(input, existingColumns) {
  const patch = {};
  for (const [field, [min, max]] of Object.entries(STAT_LIMITS)) {
    if (input[field] === undefined || input[field] === "") continue;
    const storageColumn = field === "exp" ? "data" : field;
    if (!existingColumns.has(storageColumn)) throw new Error(`Schema hiện tại không có cột ${storageColumn}.`);
    patch[field] = integer(input[field], field, min, max);
  }
  if (input.potential !== undefined && input.potential !== "") {
    if (!existingColumns.has("potential")) throw new Error("Schema hiện tại không có cột potential.");
    const potential = jsonArray(input.potential, "potential");
    if (potential.length !== 4) throw new Error("potential phải gồm đúng 4 chỉ số.");
    patch.potential = potential.map((number, index) => integer(number, `potential[${index}]`, 0, 2_000_000_000));
  }
  if (Object.keys(patch).length === 0) throw new Error("Cần cung cấp ít nhất một chỉ số hợp lệ để cập nhật.");
  return patch;
}

export function validateInventory(input, capacities = {}) {
  const itemIds = new Set();
  const normalized = {};
  const limits = { bag: Math.min(Number(capacities.bag) || 200, 200), box: Math.min(Number(capacities.box) || 200, 200), equiped: 16, fashion: 16 };
  for (const section of ["bag", "box", "equiped", "fashion"]) {
    const entries = jsonArray(input[section], section);
    if (entries.length > limits[section]) throw new Error(`${section} vượt quá giới hạn ${limits[section]} slot.`);
    const indexes = new Set();
    normalized[section] = entries.map((entry, index) => {
      const item = normalizeItem(entry, `${section}[${index}]`, itemIds);
      if (indexes.has(item.index)) throw new Error(`${section} có index trùng: ${item.index}.`);
      indexes.add(item.index);
      return item;
    });
  }
  return { payload: Object.fromEntries(Object.entries(normalized).map(([key, value]) => [key, JSON.stringify(value)])), itemIds };
}

export function existingItemIds(rawInventory) {
  const ids = new Set();
  for (const raw of Object.values(rawInventory)) {
    try {
      for (const item of jsonArray(raw || "[]", "inventory")) {
        if (Number.isSafeInteger(Number(item?.id))) ids.add(Number(item.id));
      }
    } catch { /* Existing malformed legacy JSON cannot authorize new template IDs. */ }
  }
  return ids;
}
