import { useState } from 'react'
import { api } from '../api'

export default function AuthPage({ onLogin }) {
  const [mode, setMode] = useState('login')
  const [form, setForm] = useState({ name: '', email: '', password: '', phone: '' })
  const [error, setError] = useState('')
  const [busy, setBusy] = useState(false)

  const set = (k) => (e) => setForm({ ...form, [k]: e.target.value })

  async function submit(e) {
    e.preventDefault()
    setError('')
    setBusy(true)
    try {
      const data =
        mode === 'login'
          ? await api.login({ email: form.email, password: form.password })
          : await api.register(form)
      onLogin(data)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <div className="auth-box">
      <div className="card">
        <h1>Cafe Ordering</h1>
        <p className="muted">
          {mode === 'login' ? 'Masuk ke akun kamu' : 'Daftar akun customer baru'}
        </p>
        {error && <div className="error">{error}</div>}
        <form onSubmit={submit}>
          {mode === 'register' && (
            <>
              <label>Nama</label>
              <input value={form.name} onChange={set('name')} required />
            </>
          )}
          <label>Email</label>
          <input type="email" value={form.email} onChange={set('email')} required />
          <label>Password</label>
          <input
            type="password"
            value={form.password}
            onChange={set('password')}
            required
            minLength={6}
          />
          {mode === 'register' && (
            <>
              <label>No. HP</label>
              <input value={form.phone} onChange={set('phone')} required />
            </>
          )}
          <div style={{ marginTop: 16 }}>
            <button className="primary" type="submit" disabled={busy}>
              {mode === 'login' ? 'Login' : 'Register'}
            </button>
          </div>
        </form>
        <p className="muted" style={{ marginTop: 16 }}>
          {mode === 'login' ? 'Belum punya akun? ' : 'Sudah punya akun? '}
          <a
            href="#"
            onClick={(e) => {
              e.preventDefault()
              setMode(mode === 'login' ? 'register' : 'login')
            }}
          >
            {mode === 'login' ? 'Register' : 'Login'}
          </a>
        </p>
        <p className="muted" style={{ fontSize: '0.75rem' }}>
          Admin demo: admin@cafe.local / admin123
        </p>
      </div>
    </div>
  )
}
