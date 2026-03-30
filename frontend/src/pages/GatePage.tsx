import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Car, LogIn, LogOut, Loader2, CheckCircle2, XCircle, Banknote, CreditCard, RotateCcw } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/PageHeader'
import { gateEntry, gateExit } from '@/api/gate'
import { processPayment } from '@/api/billing'
import type { GateEntryResponse, GateExitResponse } from '@/api/gate'

function StatusBadge({ status }: { status: 'OPENED' | 'CLOSED' | 'DENIED' | 'ERROR' }) {
  if (status === 'OPENED') {
    return <span className="inline-flex items-center gap-1 text-green-600 font-semibold"><CheckCircle2 size={16} /> OPENED</span>
  }
  return <span className="inline-flex items-center gap-1 text-red-600 font-semibold"><XCircle size={16} /> {status}</span>
}

const PAYMENT_METHODS = [
  { value: 'CASH',       label: 'Cash',        icon: <Banknote size={14} /> },
  { value: 'CARD',       label: 'Card',         icon: <CreditCard size={14} /> },
  { value: 'MOBILE_PAY', label: 'Mobile Pay',   icon: <CreditCard size={14} /> },
] as const

export default function GatePage() {
  const [entryPlate, setEntryPlate] = useState('')
  const [exitPlate, setExitPlate]   = useState('')
  const [exitTicket, setExitTicket] = useState('')
  const [entryResult, setEntryResult] = useState<GateEntryResponse | null>(null)
  const [exitResult,  setExitResult]  = useState<GateExitResponse  | null>(null)

  // cash-payment panel state
  const [payMethod,  setPayMethod]  = useState<'CASH' | 'CARD' | 'MOBILE_PAY'>('CASH')
  const [payAmount,  setPayAmount]  = useState('')
  const [paySuccess, setPaySuccess] = useState(false)

  const entryMutation = useMutation({
    mutationFn: gateEntry,
    onSuccess: (data) => {
      setEntryResult(data)
      if (data.gateStatus === 'OPENED') setEntryPlate('')
    },
  })

  const exitMutation = useMutation({
    mutationFn: gateExit,
    onSuccess: (data) => {
      setExitResult(data)
      setPaySuccess(false)
      if (data.fee != null) setPayAmount(String(data.fee))
      if (data.gateStatus === 'OPENED') { setExitPlate(''); setExitTicket('') }
    },
  })

  const payMutation = useMutation({
    mutationFn: processPayment,
    onSuccess: () => setPaySuccess(true),
  })

  const doExit = () => {
    setExitResult(null)
    setPaySuccess(false)
    exitMutation.mutate({
      licensePlate: exitPlate || undefined,
      ticketCode:   exitTicket || undefined,
      exitMethod:   'MANUAL',
      gateId:       'GATE-EXIT-1',
    })
  }

  return (
    <div className="space-y-6">
      <PageHeader icon={<Car size={24} />} title="Gate Control" />

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">

        {/* ── Entry ─────────────────────────────────── */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <LogIn size={18} className="text-green-600" /> Entry
            </CardTitle>
            <CardDescription>Register vehicle entry</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="entry-plate">License Plate</Label>
              <Input
                id="entry-plate"
                placeholder="e.g. AA1234BB"
                value={entryPlate}
                onChange={(e) => setEntryPlate(e.target.value.toUpperCase())}
                disabled={entryMutation.isPending}
              />
            </div>
            <Button
              className="w-full"
              onClick={() => {
                setEntryResult(null)
                entryMutation.mutate({ licensePlate: entryPlate, entryMethod: 'MANUAL', gateId: 'GATE-ENTRY-1' })
              }}
              disabled={!entryPlate.trim() || entryMutation.isPending}
            >
              {entryMutation.isPending && <Loader2 size={16} className="animate-spin mr-2" />}
              Open Entry Gate
            </Button>

            {entryMutation.isError && (
              <div className="rounded-md bg-destructive/10 border border-destructive/20 px-3 py-2 text-sm text-destructive">
                {(entryMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Request failed'}
              </div>
            )}

            {entryResult && (
              <div className={`rounded-md border px-4 py-3 text-sm space-y-1 ${entryResult.gateStatus === 'OPENED' ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
                <div className="flex justify-between">
                  <span className="text-slate-600">Gate:</span>
                  <StatusBadge status={entryResult.gateStatus} />
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600">Subscriber:</span>
                  <span className="font-medium">{entryResult.isSubscriber ? '✅ Yes' : '❌ No'}</span>
                </div>
                {entryResult.ticketCode && (
                  <div className="flex justify-between">
                    <span className="text-slate-600">Ticket:</span>
                    <span className="font-mono font-bold">{entryResult.ticketCode}</span>
                  </div>
                )}
                <p className="text-slate-500 text-xs pt-1">{entryResult.message}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {/* ── Exit ──────────────────────────────────── */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <LogOut size={18} className="text-blue-600" /> Exit
            </CardTitle>
            <CardDescription>Register vehicle exit</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="exit-plate">License Plate</Label>
              <Input
                id="exit-plate"
                placeholder="e.g. AA1234BB"
                value={exitPlate}
                onChange={(e) => setExitPlate(e.target.value.toUpperCase())}
                disabled={exitMutation.isPending}
              />
            </div>
            <div className="space-y-2">
              <Label htmlFor="exit-ticket">
                Ticket Code <span className="text-slate-400 font-normal">(optional)</span>
              </Label>
              <Input
                id="exit-ticket"
                placeholder="e.g. TKT-20260101-001"
                value={exitTicket}
                onChange={(e) => setExitTicket(e.target.value)}
                disabled={exitMutation.isPending}
              />
            </div>
            <Button
              className="w-full"
              onClick={doExit}
              disabled={(!exitPlate.trim() && !exitTicket.trim()) || exitMutation.isPending}
            >
              {exitMutation.isPending && <Loader2 size={16} className="animate-spin mr-2" />}
              Open Exit Gate
            </Button>

            {exitMutation.isError && (
              <div className="rounded-md bg-destructive/10 border border-destructive/20 px-3 py-2 text-sm text-destructive">
                {(exitMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Request failed'}
              </div>
            )}

            {exitResult && (
              <div className={`rounded-md border px-4 py-3 text-sm space-y-2 ${exitResult.gateStatus === 'OPENED' ? 'bg-green-50 border-green-200' : 'bg-amber-50 border-amber-200'}`}>
                {/* Status row */}
                <div className="flex justify-between">
                  <span className="text-slate-600">Gate:</span>
                  <StatusBadge status={exitResult.gateStatus} />
                </div>
                {exitResult.durationMinutes > 0 && (
                  <div className="flex justify-between">
                    <span className="text-slate-600">Duration:</span>
                    <span className="font-medium">{exitResult.durationMinutes} min</span>
                  </div>
                )}
                <div className="flex justify-between">
                  <span className="text-slate-600">Fee:</span>
                  <span className="font-semibold">{exitResult.fee} UAH</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600">Status:</span>
                  <span className={exitResult.isPaid ? 'text-green-600 font-medium' : 'text-amber-600 font-medium'}>
                    {exitResult.isPaid ? '✅ Paid' : '⏳ Unpaid'}
                  </span>
                </div>
                <p className="text-slate-500 text-xs">{exitResult.message}</p>

                {/* ── Inline payment panel ── */}
                {exitResult.paymentRequired && !paySuccess && (
                  <div className="mt-3 pt-3 border-t border-amber-200 space-y-3">
                    <p className="text-sm font-semibold text-amber-700 flex items-center gap-1">
                      <Banknote size={15} /> Record Payment
                    </p>

                    {/* Payment method selector */}
                    <div className="grid grid-cols-3 gap-1">
                      {PAYMENT_METHODS.map((m) => (
                        <button
                          key={m.value}
                          type="button"
                          onClick={() => setPayMethod(m.value)}
                          className={`flex items-center justify-center gap-1 px-2 py-1.5 rounded-md border text-xs font-medium transition-colors
                            ${payMethod === m.value
                              ? 'bg-primary text-white border-primary'
                              : 'bg-white text-slate-600 border-slate-200 hover:border-primary'}`}
                        >
                          {m.icon} {m.label}
                        </button>
                      ))}
                    </div>

                    {/* Amount */}
                    <div className="grid grid-cols-[1fr_auto] gap-2 items-end">
                      <div className="space-y-1">
                        <Label className="text-xs">Amount (UAH)</Label>
                        <Input
                          type="number"
                          value={payAmount}
                          onChange={(e) => setPayAmount(e.target.value)}
                          className="h-9 text-sm"
                        />
                      </div>
                      <Button
                        size="sm"
                        disabled={!payAmount || payMutation.isPending}
                        onClick={() =>
                          payMutation.mutate({
                            parkingEventId: exitResult.parkingEventId,
                            amount: Number(payAmount),
                            paymentMethod: payMethod,
                          })
                        }
                      >
                        {payMutation.isPending
                          ? <Loader2 size={14} className="animate-spin" />
                          : 'Pay'}
                      </Button>
                    </div>

                    {payMutation.isError && (
                      <p className="text-xs text-destructive">
                        {(payMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Payment failed'}
                      </p>
                    )}
                  </div>
                )}

                {/* ── Payment confirmed → retry exit ── */}
                {paySuccess && (
                  <div className="mt-3 pt-3 border-t border-green-200 space-y-2">
                    <p className="text-sm text-green-700 font-semibold">✅ Payment recorded!</p>
                    <Button
                      variant="outline"
                      size="sm"
                      className="w-full"
                      onClick={doExit}
                      disabled={exitMutation.isPending}
                    >
                      <RotateCcw size={14} className="mr-1" />
                      Retry Exit Gate
                    </Button>
                  </div>
                )}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
