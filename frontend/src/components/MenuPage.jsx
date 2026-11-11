import { useEffect, useState } from 'react'
import { api, rupiah } from '../api'

const CATEGORIES = ['ALL', 'FOOD', 'DRINK', 'DESSERT']

export default function MenuPage({ isAdmin, cart, onAdd, refreshKey }) {
  const [items, setItems] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [error, setError] = useState('')
  const [newItem, setNewItem] = useState({ name: '', price: '', category: 'FOOD' })
  const [showForm, setShowForm] = useState(false)

  async function load() {
    try {
      setItems(await api.menu())
    } catch (err) {
      setError(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function toggleAvailable(item) {
    try {
      await api.setAvailability(item.id, !item.available)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function changePrice(item) {
    const price = Number(prompt(`Harga baru untuk ${item.name}:`, item.price))
    if (!price || price <= 0) return
    try {
      await api.updatePrice(item.id, price)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function removeItem(item) {
    if (!confirm(`Hapus ${item.name} dari menu?`)) return
    try {
      await api.deleteMenuItem(item.id)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  async function addItem(e) {
    e.preventDefault()
    try {
      await api.createMenuItem({ ...newItem, price: Number(newItem.price), available: true })
      setNewItem({ name: '', price: '', category: 'FOOD' })
      setShowForm(false)
      load()
    } catch (err) {
      setError(err.message)
    }
  }

  const visible = items.filter((i) => filter === 'ALL' || i.category === filter)

  return (
    <div>
      {error && <div className="error">{error}</div>}
      <div className="row between" style={{ marginBottom: 12 }}>
        <div className="row">
          {CATEGORIES.map((c) => (
            <button
              key={c}
              className="ghost"
              style={filter === c ? { background: '#5c3d1e', color: '#fff' } : {}}
              onClick={() => setFilter(c)}
            >
              {c}
            </button>
          ))}
        </div>
        {isAdmin && (
          <button className="primary" onClick={() => setShowForm(!showForm)}>
            + Item
          </button>
        )}
      </div>

      {showForm && isAdmin && (
        <form className="card" onSubmit={addItem}>
          <div className="row">
            <input
              placeholder="Nama item"
              value={newItem.name}
              onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
              required
            />
            <input
              placeholder="Harga"
              type="number"
              min="1"
              value={newItem.price}
              onChange={(e) => setNewItem({ ...newItem, price: e.target.value })}
              required
            />
            <select
              value={newItem.category}
              onChange={(e) => setNewItem({ ...newItem, category: e.target.value })}
            >
              <option>FOOD</option>
              <option>DRINK</option>
              <option>DESSERT</option>
            </select>
            <button className="primary" type="submit">
              Tambah
            </button>
          </div>
        </form>
      )}

      <div className="grid">
        {visible.map((item) => (
          <div key={item.id} className={`card menu-card ${item.available ? '' : 'unavailable'}`}>
            <span className="cat">{item.category}</span>
            <h3>{item.name}</h3>
            <div className="price">{rupiah(item.price)}</div>
            {isAdmin ? (
              <div className="row">
                <button className="ghost" onClick={() => toggleAvailable(item)}>
                  {item.available ? 'Nonaktifkan' : 'Aktifkan'}
                </button>
                <button className="ghost" onClick={() => changePrice(item)}>
                  Harga
                </button>
                <button className="danger" onClick={() => removeItem(item)}>
                  Hapus
                </button>
              </div>
            ) : item.available ? (
              <button className="primary" onClick={() => onAdd(item)}>
                + Keranjang {cart[item.id] ? `(${cart[item.id].quantity})` : ''}
              </button>
            ) : (
              <span className="muted">Tidak tersedia</span>
            )}
          </div>
        ))}
      </div>
    </div>
  )
}
