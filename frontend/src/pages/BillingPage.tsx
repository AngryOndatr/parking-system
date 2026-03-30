import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { CreditCard, Calculator, DollarSign, Search, Loader2 } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/PageHeader'
import { calculateFee, processPayment, getBillingStatus } from '@/api/billing'
import type { BillingCalculateResponse, BillingStatusResponse } from '@/api/billing'

export default function BillingPage() {
  // Calculate fee state
  const [calcForm, setCalcForm] = useState({
    parkingEventId: '',
    entryTime: '',
    exitTime: '',
    tariffType: 'ONE_TIME' as 'ONE_TIME' | 'DAILY' | 'NIGHT' | 'VIP',
    isSubscriber: false,
  })
  const [calcResult, setCalcResult] = useState<BillingCalculateResponse | null>(null)

  // Payment state
  const [payForm, setPayForm] = useState({
    parkingEventId: '',
    amount: '',
    paymentMethod: 'CARD' as 'CARD' | 'CASH' | 'MOBILE_PAY',
  })
  const [payResult, setPayResult] = useState<{ paymentId: number; status: string } | null>(null)

  // Status state
  const [statusEventId, setStatusEventId] = useState('')
  const [statusResult, setStatusResult] = useState<BillingStatusResponse | null>(null)
  const [statusError, setStatusError] = useState<string | null>(null)

  const calcMutation = useMutation({
    mutationFn: calculateFee,
    onSuccess: (data) => setCalcResult(data),
  })

  const payMutation = useMutation({
    mutationFn: processPayment,
    onSuccess: (data) => setPayResult({ paymentId: data.paymentId, status: data.status }),
  })

  const handleStatus = async () => {
    if (!statusEventId) return
    setStatusError(null)
    setStatusResult(null)
    try {
      const res = await getBillingStatus(Number(statusEventId))
      setStatusResult(res)
    } catch {
      setStatusError('Event not found or no billing data')
    }
  }

  return (
    <div className="space-y-6">
      <PageHeader icon={<CreditCard size={24} />} title="Billing & Payments" />

      <div className="grid grid-cols-1 lg:grid-cols-2 gap-6">
        {/* Calculate fee */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><Calculator size={18} /> Calculate Fee</CardTitle>
            <CardDescription>Calculate parking fee for an event</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-1 xs:grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Event ID</Label>
                <Input type="number" placeholder="12345" value={calcForm.parkingEventId}
                  onChange={(e) => setCalcForm({ ...calcForm, parkingEventId: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label>Tariff</Label>
                <select className="w-full border rounded px-2 py-2 text-sm"
                  value={calcForm.tariffType}
                  onChange={(e) => setCalcForm({ ...calcForm, tariffType: e.target.value as typeof calcForm.tariffType })}>
                  <option value="ONE_TIME">One Time</option>
                  <option value="DAILY">Daily</option>
                  <option value="NIGHT">Night</option>
                  <option value="VIP">VIP</option>
                </select>
              </div>
            </div>
            <div className="grid grid-cols-1 xs:grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Entry Time</Label>
                <Input type="datetime-local" value={calcForm.entryTime}
                  onChange={(e) => setCalcForm({ ...calcForm, entryTime: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label>Exit Time</Label>
                <Input type="datetime-local" value={calcForm.exitTime}
                  onChange={(e) => setCalcForm({ ...calcForm, exitTime: e.target.value })} />
              </div>
            </div>
            <label className="flex items-center gap-2 text-sm cursor-pointer">
              <input type="checkbox" checked={calcForm.isSubscriber}
                onChange={(e) => setCalcForm({ ...calcForm, isSubscriber: e.target.checked })} />
              Subscriber discount
            </label>
            <Button className="w-full" disabled={calcMutation.isPending || !calcForm.parkingEventId}
              onClick={() => calcMutation.mutate({
                parkingEventId: Number(calcForm.parkingEventId),
                entryTime: new Date(calcForm.entryTime).toISOString(),
                exitTime: new Date(calcForm.exitTime).toISOString(),
                tariffType: calcForm.tariffType,
                isSubscriber: calcForm.isSubscriber,
              })}>
              {calcMutation.isPending ? <Loader2 size={16} className="animate-spin mr-1" /> : null} Calculate
            </Button>
            {calcMutation.isError && (
              <p className="text-sm text-destructive">{(calcMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed'}</p>
            )}
            {calcResult && (
              <div className="rounded-md bg-blue-50 border border-blue-200 px-4 py-3 text-sm space-y-1">
                <div className="flex justify-between"><span className="text-slate-600">Duration:</span><span className="font-medium">{calcResult.durationMinutes} min</span></div>
                <div className="flex justify-between"><span className="text-slate-600">Base fee:</span><span>{calcResult.baseFee} UAH</span></div>
                <div className="flex justify-between"><span className="text-slate-600">Discount:</span><span className="text-green-600">-{calcResult.discount} UAH</span></div>
                <div className="flex justify-between font-bold border-t pt-1 mt-1"><span>Total:</span><span className="text-primary">{calcResult.totalFee} UAH</span></div>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Process payment */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2"><DollarSign size={18} /> Process Payment</CardTitle>
            <CardDescription>Register payment for a parking event</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid grid-cols-1 xs:grid-cols-2 gap-3">
              <div className="space-y-1">
                <Label>Event ID</Label>
                <Input type="number" placeholder="12345" value={payForm.parkingEventId}
                  onChange={(e) => setPayForm({ ...payForm, parkingEventId: e.target.value })} />
              </div>
              <div className="space-y-1">
                <Label>Amount (UAH)</Label>
                <Input type="number" placeholder="100.00" value={payForm.amount}
                  onChange={(e) => setPayForm({ ...payForm, amount: e.target.value })} />
              </div>
            </div>
            <div className="space-y-1">
              <Label>Payment Method</Label>
              <select className="w-full border rounded px-2 py-2 text-sm"
                value={payForm.paymentMethod}
                onChange={(e) => setPayForm({ ...payForm, paymentMethod: e.target.value as typeof payForm.paymentMethod })}>
                <option value="CARD">Card</option>
                <option value="CASH">Cash</option>
                <option value="MOBILE_PAY">Mobile Pay</option>
              </select>
            </div>
            <Button className="w-full" disabled={payMutation.isPending || !payForm.parkingEventId || !payForm.amount}
              onClick={() => payMutation.mutate({
                parkingEventId: Number(payForm.parkingEventId),
                amount: Number(payForm.amount),
                paymentMethod: payForm.paymentMethod,
              })}>
              {payMutation.isPending ? <Loader2 size={16} className="animate-spin mr-1" /> : null} Pay
            </Button>
            {payMutation.isError && (
              <p className="text-sm text-destructive">{(payMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Failed'}</p>
            )}
            {payResult && (
              <div className="rounded-md bg-green-50 border border-green-200 px-4 py-3 text-sm">
                <p className="font-semibold text-green-700">✅ Payment #{payResult.paymentId} — {payResult.status}</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Status check */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2"><Search size={18} /> Payment Status</CardTitle>
          <CardDescription>Check payment status by parking event ID</CardDescription>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="grid grid-cols-[1fr_auto] xs:flex gap-2">
            <Input type="number" placeholder="Event ID" value={statusEventId}
              onChange={(e) => { setStatusEventId(e.target.value); setStatusResult(null); setStatusError(null) }} />
            <Button variant="outline" onClick={handleStatus} disabled={!statusEventId}>
              <Search size={16} className="mr-1" /> Check
            </Button>
          </div>
          {statusError && <p className="text-sm text-destructive">{statusError}</p>}
          {statusResult && (
            <div className={`rounded-md border px-4 py-3 text-sm ${statusResult.isPaid ? 'bg-green-50 border-green-200' : 'bg-yellow-50 border-yellow-200'}`}>
              <p className="font-semibold mb-2">Event #{statusResult.parkingEventId} — {statusResult.isPaid ? '✅ Paid' : '⏳ Unpaid'}</p>
              {statusResult.payments.map((p) => (
                <div key={p.paymentId} className="flex justify-between text-xs text-slate-600 py-0.5">
                  <span>#{p.paymentId} · {p.paymentMethod}</span>
                  <span>{p.amount} UAH · <span className={p.status === 'COMPLETED' ? 'text-green-600' : 'text-red-500'}>{p.status}</span></span>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}

