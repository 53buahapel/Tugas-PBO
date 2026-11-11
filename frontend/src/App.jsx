import { useState } from 'react'
import './App.css'
import { getToken, getUser, setToken, setUser } from './api'
import AuthPage from './components/AuthPage'
import MenuPage from './components/MenuPage'
import CartPage from './components/CartPage'
import OrdersPage from './components/OrdersPage'
import AdminPage from './components/AdminPage'

function App() {
  const [user, setUserState] = useState(getUser())
  const [tab, setTab] = useState('menu')
  const [cart, setCart] = useState({})
  const [refreshKey, setRefreshKey] = useState(0)

  const refresh = () => setRefreshKey((k) => k + 1)

  function handleLogin(auth) {
    setToken(auth.token)
    setUser(auth)
    setUserState(auth)
    setTab('menu')
  }

  function logout() {
    setToken(null)
    setUser(null)
    setUserState(null)
    setCart({})
    setTab('menu')
  }

  function addToCart(item) {
    setCart((c) => ({
      ...c,
      [item.id]: { item, quantity: (c[item.id]?.quantity || 0) + 1 },
    }))
  }

  function updateCart(item, delta) {
    setCart((c) => {
      const qty = (c[item.id]?.quantity || 0) + delta
      const next = { ...c }
      if (qty <= 0) delete next[item.id]
      else next[item.id] = { item, quantity: qty }
      return next
    })
  }

  if (!getToken() || !user) {
    return <AuthPage onLogin={handleLogin} />
  }

  const isAdmin = user.role === 'ADMIN'
  const cartCount = Object.values(cart).reduce((s, e) => s + e.quantity, 0)
  const tabs = isAdmin
    ? [
        ['menu', 'Menu'],
        ['admin', 'Dashboard'],
      ]
    : [
        ['menu', 'Menu'],
        ['cart', `Keranjang (${cartCount})`],
        ['orders', 'Order Saya'],
      ]

  return (
    <div className="app">
      <div className="topbar">
        <h1>☕ Cafe Ordering</h1>
        <span className="user">
          {user.name} · {user.role}
        </span>
        <button className="ghost" onClick={logout}>
          Logout
        </button>
      </div>
      <nav className="tabs">
        {tabs.map(([key, label]) => (
          <button
            key={key}
            className={tab === key ? 'active' : ''}
            onClick={() => setTab(key)}
          >
            {label}
          </button>
        ))}
      </nav>

      {tab === 'menu' && (
        <MenuPage isAdmin={isAdmin} cart={cart} onAdd={addToCart} refreshKey={refreshKey} />
      )}
      {tab === 'cart' && (
        <CartPage
          cart={cart}
          onUpdate={updateCart}
          onClear={() => setCart({})}
          onDone={() => {
            refresh()
            setTab('orders')
          }}
        />
      )}
      {tab === 'orders' && <OrdersPage refreshKey={refreshKey} />}
      {tab === 'admin' && isAdmin && <AdminPage refreshKey={refreshKey} />}
    </div>
  )
}

export default App
