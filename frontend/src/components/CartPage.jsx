import { useState } from 'react'
import { api, rupiah } from '../api'
import PaymentPanel from './PaymentPanel'

export default function CartPage({ cart, onUpdate, onClear, onDone }) {
  const [order, setOrder] = useState(null)
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const entries = Object.values(cart)
  const total = entries.reduce((sum, e) => sum + e.item.price * e.quantity, 0)

  async function checkout() {
    setError('')
    setBusy(true)
    try {
      const created = await api.createOrder(
        entries.map((e) => ({ menuItemId: e.item.id, quantity: e.quantity })),
      )
      setOrder(created)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (order) {
    return (
      <div className="card">
        <div className="row between">
          <h2>Order {order.orderId}</h2>
          <span className={`badge ${order.status}`}>{order.status}</span>
        </div>
        <ul className="items-list">
          {order.items.map((i) => (
            <li key={i.menuItemId}>
              {i.quantity}× {i.itemName} — {rupiah(i.subtotal)}
            </li>
          ))}
        </ul>
        <PaymentPanel
          order={order}
          onPaid={() => {
            onClear()
            onDone()
          }}
        />
      </div>
    )
  }

  return (
    <div className="card">
      <h2>Keranjang</h2>
      {error && <div className="error">{error}</div>}
      {entries.length === 0 ? (
        <p className="muted">Keranjang kosong. Pilih item dari menu.</p>
      ) : (
        <>
          <table>
            <tbody>
              {entries.map(({ item, quantity }) => (
                <tr key={item.id}>
                  <td>{item.name}</td>
                  <td>
                    <span className="qty">
                      <button type="button" onClick={() => onUpdate(item, -1)}>-</button>
                      {quantity}
                      <button type="button" onClick={() => onUpdate(item, 1)}>+</button>
                    </span>
                  </td>
                  <td>{rupiah(item.price * quantity)}</td>
                </tr>
              ))}
              <tr>
                <td colSpan={2}>
                  <strong>Total</strong>
                </td>
                <td>
                  <strong>{rupiah(total)}</strong>
                </td>
              </tr>
            </tbody>
          </table>
          <div className="row between" style={{ marginTop: 12 }}>
            <button className="danger" onClick={onClear}>
              Kosongkan
            </button>
            <button className="primary" onClick={checkout} disabled={busy}>
              Buat Order
            </button>
          </div>
        </>
      )}
    </div>
  )
}
