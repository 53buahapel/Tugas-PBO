import { useState } from 'react'
import { Coffee } from 'lucide-react'

import { api } from '../api'
import { Button } from '@/components/ui/button'
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Tabs, TabsList, TabsTrigger } from '@/components/ui/tabs'

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
    <div className="flex min-h-screen items-center justify-center p-4">
      <Card className="w-full max-w-sm">
        <CardHeader className="items-center text-center">
          <div className="bg-primary text-primary-foreground mb-2 flex size-11 items-center justify-center rounded-xl">
            <Coffee className="size-5" />
          </div>
          <CardTitle className="text-xl">Cafe Ordering</CardTitle>
          <CardDescription>Sistem pemesanan cafe</CardDescription>
        </CardHeader>
        <CardContent>
          <Tabs value={mode} onValueChange={setMode} className="mb-5">
            <TabsList className="w-full">
              <TabsTrigger value="login" className="flex-1">
                Login
              </TabsTrigger>
              <TabsTrigger value="register" className="flex-1">
                Register
              </TabsTrigger>
            </TabsList>
          </Tabs>
          <form onSubmit={submit} className="flex flex-col gap-4">
            {mode === 'register' && (
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="name">Nama</Label>
                <Input id="name" value={form.name} onChange={set('name')} required />
              </div>
            )}
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="email">Email</Label>
              <Input
                id="email"
                type="email"
                value={form.email}
                onChange={set('email')}
                required
              />
            </div>
            <div className="flex flex-col gap-1.5">
              <Label htmlFor="password">Password</Label>
              <Input
                id="password"
                type="password"
                value={form.password}
                onChange={set('password')}
                required
                minLength={6}
              />
            </div>
            {mode === 'register' && (
              <div className="flex flex-col gap-1.5">
                <Label htmlFor="phone">No. HP</Label>
                <Input id="phone" value={form.phone} onChange={set('phone')} required />
              </div>
            )}
            {error && <p className="text-destructive text-sm">{error}</p>}
            <Button type="submit" disabled={busy} className="w-full">
              {mode === 'login' ? 'Masuk' : 'Daftar'}
            </Button>
            <p className="text-muted-foreground text-center text-xs">
              Demo admin: admin@cafe.local / admin123
            </p>
          </form>
        </CardContent>
      </Card>
    </div>
  )
}
