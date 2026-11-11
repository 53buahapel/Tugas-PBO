import { useState } from 'react'
import { Banknote, Wallet } from 'lucide-react'
import { toast } from 'sonner'

import { api, rupiah } from '../api'
import { Button } from '@/components/ui/button'
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

const PROVIDERS = ['DANA', 'OVO', 'GOPAY', 'SHOPEEPAY']

export default function PaymentPanel({ order, onPaid }) {
  const [method, setMethod] = useState('CASH')
  const [cashGiven, setCashGiven] = useState('')
  const [provider, setProvider] = useState('DANA')
  const [accountNumber, setAccountNumber] = useState('')
  const [busy, setBusy] = useState(false)

  async function pay(e) {
    e.preventDefault()
    setBusy(true)
    try {
      const payment =
        method === 'CASH'
          ? await api.payCash(order.orderId, Number(cashGiven))
          : await api.payEwallet(order.orderId, { provider, accountNumber })
      toast.success(
        `Pembayaran ${payment.method} berhasil` +
          (payment.change > 0 ? ` — kembalian ${rupiah(payment.change)}` : ''),
      )
      onPaid?.(payment)
    } catch (err) {
      toast.error(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <form onSubmit={pay} className="flex flex-col gap-4 border-t pt-4">
      <div className="flex items-center justify-between">
        <span className="text-muted-foreground text-sm">Total bayar</span>
        <span className="text-lg font-semibold">{rupiah(order.total)}</span>
      </div>
      <Tabs value={method} onValueChange={setMethod}>
        <TabsList className="w-full">
          <TabsTrigger value="CASH" className="flex-1">
            <Banknote /> Cash
          </TabsTrigger>
          <TabsTrigger value="EWALLET" className="flex-1">
            <Wallet /> E-Wallet
          </TabsTrigger>
        </TabsList>
      </Tabs>
      {method === 'CASH' ? (
        <div className="flex flex-col gap-1.5">
          <Label>Uang diterima</Label>
          <Input
            type="number"
            min="1"
            placeholder={`Minimal ${rupiah(order.total)}`}
            value={cashGiven}
            onChange={(e) => setCashGiven(e.target.value)}
            required
          />
        </div>
      ) : (
        <div className="flex gap-3">
          <div className="flex flex-col gap-1.5">
            <Label>Provider</Label>
            <Select value={provider} onValueChange={setProvider}>
              <SelectTrigger className="w-36">
                <SelectValue />
              </SelectTrigger>
              <SelectContent>
                {PROVIDERS.map((p) => (
                  <SelectItem key={p} value={p}>
                    {p}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
          </div>
          <div className="flex flex-1 flex-col gap-1.5">
            <Label>No. akun</Label>
            <Input
              placeholder="10-15 digit"
              value={accountNumber}
              onChange={(e) => setAccountNumber(e.target.value)}
              required
            />
          </div>
        </div>
      )}
      <Button type="submit" disabled={busy} className="w-full">
        Bayar {rupiah(order.total)}
      </Button>
    </form>
  )
}
