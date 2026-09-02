export async function listBots() {
  const response = await fetch("/api/bots");
  if (!response.ok) throw new Error("Không thể tải danh sách bot.");
  return response.json();
}

export async function createBot(charId, name, confirmation) {
  const body = { charId, name };
  if (confirmation !== undefined && confirmation !== null) body.confirmation = confirmation;
  const response = await fetch("/api/actions/bot-create", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-nso-csrf": window.csrf },
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error("Không thể tạo bot.");
  return response.json();
}

export async function toggleBot(id, enabled, confirmation) {
  const body = { id, enabled };
  if (confirmation !== undefined && confirmation !== null) body.confirmation = confirmation;
  const response = await fetch("/api/actions/bot-toggle", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-nso-csrf": window.csrf },
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error("Không thể thay đổi trạng thái bot.");
  return response.json();
}

export async function deleteBot(id, confirmation) {
  const body = { id };
  if (confirmation !== undefined && confirmation !== null) body.confirmation = confirmation;
  const response = await fetch("/api/actions/bot-delete", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-nso-csrf": window.csrf },
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error("Không thể xoá bot.");
  return response.json();
}

export async function controlBot(action, charId, confirmation) {
  const body = { action, charId };
  if (confirmation !== undefined && confirmation !== null) body.confirmation = confirmation;
  const response = await fetch("/api/actions/bot-control", {
    method: "POST",
    headers: { "Content-Type": "application/json", "x-nso-csrf": window.csrf },
    body: JSON.stringify(body),
  });
  if (!response.ok) throw new Error("Không thể điều khiển bot.");
  return response.json();
}
