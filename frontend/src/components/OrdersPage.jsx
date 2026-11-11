import { useEffect, useState } from 'react'
import { toast } from 'sonner'

import { api, rupiah } from '../api'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import StatusBadge from './StatusBadge'
import ConfirmDialog from './ConfirmDialog'
import PaymentPanel from './PaymentPanel'

export default function OrdersPage({ refreshKey }) {
  const [orders, setOrders] = useState([])
  const [payingId, setPayingId] = useState(null)

  async function load() {
    try {
      setOrders(await api.myOrders())
    } catch (err) {
      toast.error(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function cancel(orderId) {
    try {
      await api.cancelOrder(orderId)
      toast.success('Order dibatalkan')
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  if (orders.length === 0) {
    return (
      <p className="text-muted-foreground py-16 text-center text-sm">
        Belum ada order. Pesan dari menu dulu yuk.
      </p>
    )
  }

  return (
    <div className="mx-auto flex max-w-lg flex-col gap-3">
      {orders.map((o) => (
        <Card key={o.orderId}>
          <CardContent className="flex flex-col gap-3">
            <div className="flex items-center justify-between">
              <span className="font-mono text-sm font-medium">{o.orderId}</span>
              <StatusBadge status={o.status} />
            </div>
            <ul className="text-muted-foreground flex flex-col gap-1 text-sm">
              {o.items.map((i) => (
                <li key={i.menuItemId} className="flex justify-between">
                  <span>
                    {i.quantity}× {i.itemName}
                  </span>
                  <span>{rupiah(i.subtotal)}</span>
                </li>
              ))}
            </ul>
            <div className="flex items-center justify-between">
              <span className="font-semibold">{rupiah(o.total)}</span>
              {o.status === 'PENDING' && (
                <div className="flex gap-2">
                  <Button
                    variant="outline"
                    size="sm"
                    onClick={() => setPayingId(payingId === o.orderId ? null : o.orderId)}
                  >
                    {payingId === o.orderId ? 'Tutup' : 'Bayar'}
                  </Button>
                  <ConfirmDialog
                    trigger={
                      <Button variant="ghost" size="sm" className="text-destructive">
                        Batal
                      </Button>
                    }
                    title="Batalkan order?"
                    description={`Order ${o.orderId} akan dibatalkan dan tidak bisa dikembalikan.`}
                    onConfirm={() => cancel(o.orderId)}
                  />
                </div>
              )}
            </div>
            {o.payment && (
              <p className="text-muted-foreground flex items-center gap-2 text-xs">
                {o.payment.method} · <StatusBadge status={o.payment.status} />
                {o.payment.change > 0 && `· kembalian ${rupiah(o.payment.change)}`}
              </p>
            )}
            {payingId === o.orderId && o.status === 'PENDING' && (
              <PaymentPanel
                order={o}
                onPaid={() => {
                  setPayingId(null)
                  load()
                }}
              />
            )}
          </CardContent>
        </Card>
      ))}
    </div>
  )
}
