const BASE = '/api'

export function getToken() {
  return localStorage.getItem('token')
}

export function setToken(token) {
  if (token) localStorage.setItem('token', token)
  else localStorage.removeItem('token')
}

export function getUser() {
  const raw = localStorage.getItem('user')
  return raw ? JSON.parse(raw) : null
}

export function setUser(user) {
  if (user) localStorage.setItem('user', JSON.stringify(user))
  else localStorage.removeItem('user')
}

async function req(method, path, body) {
  const headers = { 'Content-Type': 'application/json' }
  const token = getToken()
  if (token) headers.Authorization = `Bearer ${token}`
  const res = await fetch(BASE + path, {
    method,
    headers,
    body: body === undefined ? undefined : JSON.stringify(body),
  })
  if (res.status === 204) return null
  const payload = await res.json().catch(() => null)
  if (!res.ok) throw new Error(payload?.message || `Request failed (${res.status})`)
  return payload?.data ?? payload
}

export const api = {
  register: (b) => req('POST', '/auth/register', b),
  login: (b) => req('POST', '/auth/login', b),
  menu: () => req('GET', '/menu'),
  createMenuItem: (b) => req('POST', '/menu', b),
  updatePrice: (id, price) => req('PATCH', `/menu/${id}/price`, { price }),
  setAvailability: (id, available) => req('PATCH', `/menu/${id}/availability`, { available }),
  deleteMenuItem: (id) => req('DELETE', `/menu/${id}`),
  createOrder: (items) => req('POST', '/orders', { items }),
  myOrders: () => req('GET', '/orders'),
  addItem: (orderId, b) => req('POST', `/orders/${orderId}/items`, b),
  removeItem: (orderId, menuItemId) => req('DELETE', `/orders/${orderId}/items/${menuItemId}`),
  cancelOrder: (orderId) => req('POST', `/orders/${orderId}/cancel`),
  payCash: (orderId, cashGiven) => req('POST', `/orders/${orderId}/payments/cash`, { cashGiven }),
  payEwallet: (orderId, b) => req('POST', `/orders/${orderId}/payments/ewallet`, b),
  allOrders: () => req('GET', '/admin/orders'),
  markProcessing: (orderId) => req('PATCH', `/admin/orders/${orderId}/processing`),
  markCompleted: (orderId) => req('PATCH', `/admin/orders/${orderId}/complete`),
  dashboard: () => req('GET', '/admin/dashboard'),
}

export const rupiah = (n) => `Rp ${Number(n).toLocaleString('id-ID')}`
