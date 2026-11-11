import { Badge } from '@/components/ui/badge'

const STYLES = {
  PENDING: 'border-amber-300 bg-amber-50 text-amber-700',
  PAID: 'border-blue-300 bg-blue-50 text-blue-700',
  PROCESSING: 'border-violet-300 bg-violet-50 text-violet-700',
  COMPLETED: 'border-emerald-300 bg-emerald-50 text-emerald-700',
  CANCELLED: 'border-red-300 bg-red-50 text-red-700',
  SUCCESS: 'border-emerald-300 bg-emerald-50 text-emerald-700',
  FAILED: 'border-red-300 bg-red-50 text-red-700',
}

export default function StatusBadge({ status }) {
  return (
    <Badge variant="outline" className={STYLES[status] ?? ''}>
      {status}
    </Badge>
  )
}
