import { useState } from 'react'
import { Coffee, LayoutDashboard, LogOut, ReceiptText, ShoppingCart, Store } from 'lucide-react'
import { Toaster } from 'sonner'

import { getToken, getUser, setToken, setUser } from './api'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
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
    return (
      <>
        <AuthPage onLogin={handleLogin} />
        <Toaster position="top-center" richColors />
      </>
    )
  }

  const isAdmin = user.role === 'ADMIN'
  const cartCount = Object.values(cart).reduce((s, e) => s + e.quantity, 0)

  return (
    <div className="mx-auto min-h-screen max-w-5xl px-4 pb-16">
      <header className="flex items-center gap-3 border-b py-4">
        <div className="bg-primary text-primary-foreground flex size-9 items-center justify-center rounded-lg">
          <Coffee className="size-4" />
        </div>
        <h1 className="text-lg font-semibold tracking-tight">Cafe Ordering</h1>
        <div className="ml-auto flex items-center gap-3">
          <span className="text-muted-foreground hidden text-sm sm:block">{user.name}</span>
          <Badge variant="secondary">{user.role}</Badge>
          <Button variant="ghost" size="icon" onClick={logout} title="Logout">
            <LogOut />
          </Button>
        </div>
      </header>

      <div className="py-5">
        <Tabs value={tab} onValueChange={setTab}>
          <TabsList>
            <TabsTrigger value="menu">
              <Store /> Menu
            </TabsTrigger>
            {isAdmin ? (
              <TabsTrigger value="admin">
                <LayoutDashboard /> Dashboard
              </TabsTrigger>
            ) : (
              <>
                <TabsTrigger value="cart">
                  <ShoppingCart /> Keranjang{cartCount > 0 ? ` (${cartCount})` : ''}
                </TabsTrigger>
                <TabsTrigger value="orders">
                  <ReceiptText /> Order Saya
                </TabsTrigger>
              </>
            )}
          </TabsList>
        </Tabs>
      </div>

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

      <Toaster position="top-center" richColors />
    </div>
  )
}

export default App
