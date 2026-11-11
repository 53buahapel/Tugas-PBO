import { useState } from 'react'
import { api, rupiah } from '../api'

const PROVIDERS = ['DANA', 'OVO', 'GOPAY', 'SHOPEEPAY']

export default function PaymentPanel({ order, onPaid }) {
  const [method, setMethod] = useState('CASH')
  const [cashGiven, setCashGiven] = useState('')
  const [provider, setProvider] = useState('DANA')
  const [accountNumber, setAccountNumber] = useState('')
  const [error, setError] = useState('')
  const [result, setResult] = useState(null)
  const [busy, setBusy] = useState(false)

  async function pay(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const payment =
        method === 'CASH'
          ? await api.payCash(order.orderId, Number(cashGiven))
          : await api.payEwallet(order.orderId, { provider, accountNumber })
      setResult(payment)
      onPaid?.(payment)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (result) {
    return (
      <div className="success">
        Pembayaran {result.method} berhasil — {rupiah(result.amount)}
        {result.change != null && result.change > 0 && <> — kembalian {rupiah(result.change)}</>}
      </div>
    )
  }

  return (
    <form className="pay-panel" onSubmit={pay}>
      <div className="row" style={{ marginBottom: 10 }}>
        <strong>Bayar {rupiah(order.total)}</strong>
        <button
          type="button"
          className="ghost"
          style={method === 'CASH' ? { background: '#5c3d1e', color: '#fff' } : {}}
          onClick={() => setMethod('CASH')}
        >
          Cash
        </button>
        <button
          type="button"
          className="ghost"
          style={method === 'EWALLET' ? { background: '#5c3d1e', color: '#fff' } : {}}
          onClick={() => setMethod('EWALLET')}
        >
          E-Wallet
        </button>
      </div>
      {error && <div className="error">{error}</div>}
      {method === 'CASH' ? (
        <div className="row">
          <input
            type="number"
            min="1"
            placeholder={`Uang tunai (min ${rupiah(order.total)})`}
            value={cashGiven}
            onChange={(e) => setCashGiven(e.target.value)}
            required
          />
          <button className="primary" type="submit" disabled={busy}>
            Bayar
          </button>
        </div>
      ) : (
        <div className="row">
          <select value={provider} onChange={(e) => setProvider(e.target.value)}>
            {PROVIDERS.map((p) => (
              <option key={p}>{p}</option>
            ))}
          </select>
          <input
            placeholder="No. akun (10-15 digit)"
            value={accountNumber}
            onChange={(e) => setAccountNumber(e.target.value)}
            required
          />
          <button className="primary" type="submit" disabled={busy}>
            Bayar
          </button>
        </div>
      )}
    </form>
  )
}
