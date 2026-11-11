import { useState } from 'react'
import { Minus, Plus, ShoppingCart, Trash2 } from 'lucide-react'
import { toast } from 'sonner'

import { api, rupiah } from '../api'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import StatusBadge from './StatusBadge'
import PaymentPanel from './PaymentPanel'

export default function CartPage({ cart, onUpdate, onClear, onDone }) {
  const [order, setOrder] = useState(null)
  const [busy, setBusy] = useState(false)

  const entries = Object.values(cart)
  const total = entries.reduce((sum, e) => sum + e.item.price * e.quantity, 0)

  async function checkout() {
    setBusy(true)
    try {
      const created = await api.createOrder(
        entries.map((e) => ({ menuItemId: e.item.id, quantity: e.quantity })),
      )
      setOrder(created)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  if (order) {
    return (
      <Card className="mx-auto max-w-md">
        <CardHeader>
          <div className="flex items-center justify-between">
            <div>
              <CardTitle>{order.orderId}</CardTitle>
              <CardDescription>Order dibuat — silakan bayar</CardDescription>
            </div>
            <StatusBadge status={order.status} />
          </div>
        </CardHeader>
        <CardContent className="flex flex-col gap-4">
          <ul className="text-muted-foreground flex flex-col gap-1 text-sm">
            {order.items.map((i) => (
              <li key={i.menuItemId} className="flex justify-between">
                <span>
                  {i.quantity}× {i.itemName}
                </span>
                <span>{rupiah(i.subtotal)}</span>
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
        </CardContent>
      </Card>
    )
  }

  return (
    <Card className="mx-auto max-w-lg">
      <CardHeader>
        <CardTitle className="flex items-center gap-2">
          <ShoppingCart className="size-5" /> Keranjang
        </CardTitle>
        <CardDescription>{entries.length} jenis item</CardDescription>
      </CardHeader>
      <CardContent className="flex flex-col gap-4">
        {entries.length === 0 ? (
          <p className="text-muted-foreground py-6 text-center text-sm">
            Keranjang kosong. Pilih item dari menu.
          </p>
        ) : (
          <>
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Item</TableHead>
                  <TableHead>Qty</TableHead>
                  <TableHead className="text-right">Subtotal</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {entries.map(({ item, quantity }) => (
                  <TableRow key={item.id}>
                    <TableCell className="font-medium">{item.name}</TableCell>
                    <TableCell>
                      <div className="flex items-center gap-2">
                        <Button
                          variant="outline"
                          size="icon"
                          className="size-7"
                          onClick={() => onUpdate(item, -1)}
                        >
                          <Minus />
                        </Button>
                        <span className="w-5 text-center">{quantity}</span>
                        <Button
                          variant="outline"
                          size="icon"
                          className="size-7"
                          onClick={() => onUpdate(item, 1)}
                        >
                          <Plus />
                        </Button>
                      </div>
                    </TableCell>
                    <TableCell className="text-right">
                      {rupiah(item.price * quantity)}
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
            <div className="flex items-center justify-between border-t pt-4">
              <Button variant="ghost" className="text-destructive" onClick={onClear}>
                <Trash2 /> Kosongkan
              </Button>
              <div className="flex items-center gap-4">
                <span className="text-lg font-semibold">{rupiah(total)}</span>
                <Button onClick={checkout} disabled={busy}>
                  Buat Order
                </Button>
              </div>
            </div>
          </>
        )}
      </CardContent>
    </Card>
  )
}
