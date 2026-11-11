import { useEffect, useState } from 'react'
import { Check, CookingPot } from 'lucide-react'
import { toast } from 'sonner'

import { api, rupiah } from '../api'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from '@/components/ui/table'
import StatusBadge from './StatusBadge'

export default function AdminPage({ refreshKey }) {
  const [stats, setStats] = useState(null)
  const [orders, setOrders] = useState([])

  async function load() {
    try {
      const [d, o] = await Promise.all([api.dashboard(), api.allOrders()])
      setStats(d)
      setOrders(o)
    } catch (err) {
      toast.error(err.message)
    }
  }

  useEffect(() => {
    load()
  }, [refreshKey])

  async function act(fn, orderId, msg) {
    try {
      await fn(orderId)
      toast.success(msg)
      load()
    } catch (err) {
      toast.error(err.message)
    }
  }

  const statCards = stats
    ? [
        ['Total Order', stats.totalOrders],
        ['Pending', stats.pendingOrders],
        ['Paid', stats.paidOrders],
        ['Processing', stats.processingOrders],
        ['Completed', stats.completedOrders],
        ['Revenue', rupiah(stats.totalRevenue)],
      ]
    : []

  return (
    <div className="flex flex-col gap-4">
      <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-6">
        {statCards.map(([label, value]) => (
          <Card key={label}>
            <CardContent className="flex flex-col gap-1">
              <span className="text-muted-foreground text-xs tracking-wide uppercase">
                {label}
              </span>
              <span className="text-xl font-semibold">{value}</span>
            </CardContent>
          </Card>
        ))}
      </div>

      <Card>
        <CardHeader>
          <CardTitle>Semua Order</CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Order</TableHead>
                <TableHead>Customer</TableHead>
                <TableHead>Items</TableHead>
                <TableHead>Total</TableHead>
                <TableHead>Status</TableHead>
                <TableHead className="text-right">Aksi</TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {orders.map((o) => (
                <TableRow key={o.orderId}>
                  <TableCell className="font-mono text-xs">{o.orderId}</TableCell>
                  <TableCell>{o.customerName}</TableCell>
                  <TableCell className="text-muted-foreground max-w-48 truncate">
                    {o.items.map((i) => `${i.quantity}× ${i.itemName}`).join(', ')}
                  </TableCell>
                  <TableCell>{rupiah(o.total)}</TableCell>
                  <TableCell>
                    <StatusBadge status={o.status} />
                  </TableCell>
                  <TableCell className="text-right">
                    {o.status === 'PAID' && (
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => act(api.markProcessing, o.orderId, 'Order diproses')}
                      >
                        <CookingPot /> Proses
                      </Button>
                    )}
                    {o.status === 'PROCESSING' && (
                      <Button
                        size="sm"
                        onClick={() => act(api.markCompleted, o.orderId, 'Order selesai')}
                      >
                        <Check /> Selesai
                      </Button>
                    )}
                  </TableCell>
                </TableRow>
              ))}
              {orders.length === 0 && (
                <TableRow>
                  <TableCell colSpan={6} className="text-muted-foreground py-8 text-center">
                    Belum ada order masuk.
                  </TableCell>
                </TableRow>
              )}
            </TableBody>
          </Table>
        </CardContent>
      </Card>
    </div>
  )
}
