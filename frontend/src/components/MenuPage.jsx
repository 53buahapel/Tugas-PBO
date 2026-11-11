import { useEffect, useState } from 'react'
import {
  CakeSlice,
  Coffee,
  Pencil,
  Plus,
  Trash2,
  UtensilsCrossed,
} from 'lucide-react'
import { toast } from 'sonner'

import { api, rupiah } from '../api'
import { Badge } from '@/components/ui/badge'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from '@/components/ui/dialog'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from '@/components/ui/select'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'
import ConfirmDialog from './ConfirmDialog'

const CATEGORY_ICON = {
  FOOD: UtensilsCrossed,
  DRINK: Coffee,
  DESSERT: CakeSlice,
}

export default function MenuPage({ isAdmin, cart, onAdd, refreshKey }) {
  const [items, setItems] = useState([])
  const [filter, setFilter] = useState('ALL')
  const [addOpen, setAddOpen] = useState(false)
  const [newItem, setNewItem] = useState({ name: '', price: '', category: 'FOOD' })

  async function load() {
    try {
      setItems(await api.menu())
    } catch (err) {
      toast.error(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function run(fn, successMsg) {
    try {
      await fn()
      if (successMsg) toast.success(successMsg)
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  const visible = items.filter((i) => filter === 'ALL' || i.category === filter)

  return (
    <div className="flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <Tabs value={filter} onValueChange={setFilter}>
          <TabsList>
            {['ALL', 'FOOD', 'DRINK', 'DESSERT'].map((c) => (
              <TabsTrigger key={c} value={c}>
                {c === 'ALL' ? 'Semua' : c.charAt(0) + c.slice(1).toLowerCase()}
              </TabsTrigger>
            ))}
          </TabsList>
        </Tabs>
        {isAdmin && (
          <Dialog open={addOpen} onOpenChange={setAddOpen}>
            <DialogTrigger asChild>
              <Button size="sm">
                <Plus /> Item
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Tambah Menu Item</DialogTitle>
                <DialogDescription>Item baru langsung tersedia.</DialogDescription>
              </DialogHeader>
              <form
                className="flex flex-col gap-4"
                onSubmit={(e) => {
                  e.preventDefault()
                  run(
                    () =>
                      api.createMenuItem({
                        ...newItem,
                        price: Number(newItem.price),
                        available: true,
                      }),
                    'Item ditambahkan',
                  ).then(() => setAddOpen(false))
                  setNewItem({ name: '', price: '', category: 'FOOD' })
                }}
              >
                <div className="flex flex-col gap-1.5">
                  <Label>Nama</Label>
                  <Input
                    value={newItem.name}
                    onChange={(e) => setNewItem({ ...newItem, name: e.target.value })}
                    required
                  />
                </div>
                <div className="flex gap-3">
                  <div className="flex flex-1 flex-col gap-1.5">
                    <Label>Harga</Label>
                    <Input
                      type="number"
                      min="1"
                      value={newItem.price}
                      onChange={(e) => setNewItem({ ...newItem, price: e.target.value })}
                      required
                    />
                  </div>
                  <div className="flex flex-1 flex-col gap-1.5">
                    <Label>Kategori</Label>
                    <Select
                      value={newItem.category}
                      onValueChange={(v) => setNewItem({ ...newItem, category: v })}
                    >
                      <SelectTrigger className="w-full">
                        <SelectValue />
                      </SelectTrigger>
                      <SelectContent>
                        <SelectItem value="FOOD">Food</SelectItem>
                        <SelectItem value="DRINK">Drink</SelectItem>
                        <SelectItem value="DESSERT">Dessert</SelectItem>
                      </SelectContent>
                    </Select>
                  </div>
                </div>
                <DialogFooter>
                  <Button type="submit">Tambah</Button>
                </DialogFooter>
              </form>
            </DialogContent>
          </Dialog>
        )}
      </div>

      <div className="grid grid-cols-1 gap-3 sm:grid-cols-2 lg:grid-cols-3">
        {visible.map((item) => (
          <MenuCard
            key={item.id}
            item={item}
            isAdmin={isAdmin}
            cartQty={cart[item.id]?.quantity || 0}
            onAdd={() => onAdd(item)}
            onRefresh={load}
            onError={(msg) => toast.error(msg)}
          />
        ))}
      </div>
      {visible.length === 0 && (
        <p className="text-muted-foreground py-10 text-center text-sm">
          Tidak ada item pada kategori ini.
        </p>
      )}
    </div>
  )
}

function MenuCard({ item, isAdmin, cartQty, onAdd, onRefresh, onError }) {
  const [priceOpen, setPriceOpen] = useState(false)
  const [price, setPrice] = useState(item.price)
  const Icon = CATEGORY_ICON[item.category] ?? Coffee

  async function run(fn, msg) {
    try {
      await fn()
      if (msg) toast.success(msg)
      onRefresh()
    } catch (err) {
      onError(err.message)
    }
  }

  return (
    <Card className={item.available ? '' : 'opacity-60'}>
      <CardContent className="flex flex-col gap-3">
        <div className="flex items-start justify-between">
          <div className="bg-secondary text-secondary-foreground flex size-9 items-center justify-center rounded-lg">
            <Icon className="size-4" />
          </div>
          {!item.available && <Badge variant="outline">Habis</Badge>}
        </div>
        <div>
          <p className="font-medium">{item.name}</p>
          <p className="text-muted-foreground text-xs tracking-wide uppercase">
            {item.category}
          </p>
        </div>
        <p className="text-lg font-semibold">{rupiah(item.price)}</p>
        {isAdmin ? (
          <div className="flex gap-1">
            <Button
              variant="ghost"
              size="sm"
              onClick={() =>
                run(
                  () => api.setAvailability(item.id, !item.available),
                  item.available ? 'Item dinonaktifkan' : 'Item diaktifkan',
                )
              }
            >
              {item.available ? 'Nonaktifkan' : 'Aktifkan'}
            </Button>
            <Dialog open={priceOpen} onOpenChange={setPriceOpen}>
              <DialogTrigger asChild>
                <Button variant="ghost" size="icon" onClick={() => setPrice(item.price)}>
                  <Pencil />
                </Button>
              </DialogTrigger>
              <DialogContent className="sm:max-w-xs">
                <DialogHeader>
                  <DialogTitle>Ubah Harga</DialogTitle>
                  <DialogDescription>{item.name}</DialogDescription>
                </DialogHeader>
                <form
                  className="flex flex-col gap-4"
                  onSubmit={(e) => {
                    e.preventDefault()
                    run(() => api.updatePrice(item.id, Number(price)), 'Harga diubah').then(
                      () => setPriceOpen(false),
                    )
                  }}
                >
                  <Input
                    type="number"
                    min="1"
                    value={price}
                    onChange={(e) => setPrice(e.target.value)}
                    required
                  />
                  <DialogFooter>
                    <Button type="submit">Simpan</Button>
                  </DialogFooter>
                </form>
              </DialogContent>
            </Dialog>
            <ConfirmDialog
              trigger={
                <Button variant="ghost" size="icon" className="text-destructive">
                  <Trash2 />
                </Button>
              }
              title="Hapus item?"
              description={`${item.name} akan dihapus dari menu. Order yang sudah ada tidak terpengaruh.`}
              onConfirm={() => run(() => api.deleteMenuItem(item.id), 'Item dihapus')}
            />
          </div>
        ) : (
          item.available && (
            <Button variant="secondary" size="sm" onClick={onAdd}>
              <Plus /> Tambah{cartQty > 0 ? ` (${cartQty})` : ''}
            </Button>
          )
        )}
      </CardContent>
    </Card>
  )
}
