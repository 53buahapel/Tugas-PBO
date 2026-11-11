import { useEffect, useState } from 'react'
import { api, rupiah } from '../api'

export default function AdminPage({ refreshKey }) {
  const [stats, setStats] = useState(null)
  const [orders, setOrders] = useState([])
  const [error, setError] = useState('')

  async function load() {
    try {
      const [d, o] = await Promise.all([api.dashboard(), api.allOrders()])
      setStats(d)
      setOrders(o)
    } catch (err) {
      setError(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function act(fn, orderId) {
    try {
      await fn(orderId)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  return (
    <div>
      {error && <div className="error">{error}</div>}
      {stats && (
        <div className="stats">
          <div className="stat"><div className="num">{stats.totalOrders}</div><div className="lbl">Total</div></div>
          <div className="stat"><div className="num">{stats.pendingOrders}</div><div className="lbl">Pending</div></div>
          <div className="stat"><div className="num">{stats.paidOrders}</div><div className="lbl">Paid</div></div>
          <div className="stat"><div className="num">{stats.processingOrders}</div><div className="lbl">Processing</div></div>
          <div className="stat"><div className="num">{stats.completedOrders}</div><div className="lbl">Completed</div></div>
          <div className="stat"><div className="num">{rupiah(stats.totalRevenue)}</div><div className="lbl">Revenue</div></div>
        </div>
      )}
      <div className="card">
        <h2>Semua Order</h2>
        <table>
          <thead>
            <tr>
              <th>Order</th>
              <th>Customer</th>
              <th>Items</th>
              <th>Total</th>
              <th>Status</th>
              <th></th>
            </tr>
          </thead>
          <tbody>
            {orders.map((o) => (
              <tr key={o.orderId}>
                <td>{o.orderId}</td>
                <td>{o.customerName}</td>
                <td>{o.items.map((i) => `${i.quantity}× ${i.itemName}`).join(', ')}</td>
                <td>{rupiah(o.total)}</td>
                <td><span className={`badge ${o.status}`}>{o.status}</span></td>
                <td>
                  {o.status === 'PAID' && (
                    <button className="ghost" onClick={() => act(api.markProcessing, o.orderId)}>
                      Proses
                    </button>
                  )}
                  {o.status === 'PROCESSING' && (
                    <button className="primary" onClick={() => act(api.markCompleted, o.orderId)}>
                      Selesai
                    </button>
                  )}
                </td>
              </tr>
            ))}
            {orders.length === 0 && (
              <tr><td colSpan={6} className="muted">Belum ada order.</td></tr>
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}
