import { useEffect, useState } from 'react'
import { api, rupiah } from '../api'
import PaymentPanel from './PaymentPanel'

export default function OrdersPage({ refreshKey }) {
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')
  const [payingId, setPayingId] = useState(null)

  async function load() {
    try {
      setOrders(await api.myOrders())
    } catch (err) {
      setError(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function cancel(orderId) {
    if (!confirm('Batalkan order ini?')) return
    try {
      await api.cancelOrder(orderId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      {error && <div className="error">{error}</div>}
      {orders.length === 0 && <p className="muted">Belum ada order.</p>}
      {orders.map((o) => (
        <div key={o.orderId} className="card">
          <div className="row between">
            <strong>{o.orderId}</strong>
            <span className={`badge ${o.status}`}>{o.status}</span>
          </div>
          <ul className="items-list">
            {o.items.map((i) => (
              <li key={i.menuItemId}>
                {i.quantity}× {i.itemName} — {rupiah(i.subtotal)}
              </li>
            ))}
          </ul>
          <div className="row between">
            <strong>{rupiah(o.total)}</strong>
            {o.status === 'PENDING' && (
              <div className="row">
                <button className="ghost" onClick={() => setPayingId(payingId === o.orderId ? null : o.orderId)}>
                  Bayar
                </button>
                <button className="danger" onClick={() => cancel(o.orderId)}>
                  Batal
                </button>
              </div>
            )}
          </div>
          {o.payment && (
            <p className="muted">
              {o.payment.method} · <span className={`badge ${o.payment.status}`}>{o.payment.status}</span>
              {o.payment.change > 0 && ` · kembalian ${rupiah(o.payment.change)}`}
            </p>
          )}
          {payingId === o.orderId && o.status === 'PENDING' && (
            <PaymentPanel order={o} onPaid={() => { setPayingId(null); load() }} />
          )}
        </div>
      ))}
    </div>
  )
}
