import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { CreditCard, Search, Loader2, DollarSign, CheckCircle2 } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/PageHeader'
import { getBillingStatusByTicket, processPaymentTest } from '@/api/billing'
import type { BillingStatusResponse } from '@/api/billing'

export default function BillingPage() {
  // Ticket search state
  const [ticketCode, setTicketCode] = useState('')
  const [searchResult, setSearchResult] = useState<BillingStatusResponse | null>(null)
  const [searchError, setSearchError] = useState<string | null>(null)

  // Payment form state (shown when ticket is found and unpaid)
  const [payForm, setPayForm] = useState({
    amount: '',
    paymentMethod: 'CARD' as 'CARD' | 'CASH' | 'MOBILE_PAY',
  })
  const [paymentSuccess, setPaymentSuccess] = useState(false)

  const searchMutation = useMutation({
    mutationFn: (code: string) => getBillingStatusByTicket(code),
    onSuccess: (data) => {
      setSearchResult(data)
      setSearchError(null)
      setPaymentSuccess(false)
      // Pre-fill amount from the first unpaid payment if available
      if (!data.isPaid && data.payments.length > 0) {
        const totalAmount = data.payments.reduce((sum, p) => sum + p.amount, 0)
        setPayForm(prev => ({ ...prev, amount: String(totalAmount) }))
      }
    },
    onError: () => {
      setSearchError('Ticket not found or no billing data available')
      setSearchResult(null)
    },
  })

  const payMutation = useMutation({
    mutationFn: (amount: number) =>
      processPaymentTest({
        parkingEventId: searchResult!.parkingEventId,
        amount,
        paymentMethod: payForm.paymentMethod,
      }),
    onSuccess: () => setPaymentSuccess(true),
  })

  const handleSearch = () => {
    if (!ticketCode.trim()) return
    setPaymentSuccess(false)
    searchMutation.mutate(ticketCode)
  }

  const handlePayment = () => {
    if (!payForm.amount || !searchResult) return
    payMutation.mutate(Number(payForm.amount))
  }

  return (
    <div className="space-y-6">
      <PageHeader icon={<CreditCard size={24} />} title="Billing & Payments" />

      {/* ────── Ticket Search ────── */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Search size={18} /> Find Ticket
          </CardTitle>
          <CardDescription>Search for a ticket by code to check payment status</CardDescription>
        </CardHeader>
        <CardContent className="space-y-4">
          <div className="flex gap-2">
            <Input
              placeholder="e.g. TKT-20260101-001"
              value={ticketCode}
              onChange={(e) => setTicketCode(e.target.value)}
              disabled={searchMutation.isPending}
              onKeyDown={(e) => e.key === 'Enter' && handleSearch()}
            />
            <Button
              variant="outline"
              onClick={handleSearch}
              disabled={!ticketCode.trim() || searchMutation.isPending}
            >
              {searchMutation.isPending ? <Loader2 size={16} className="animate-spin" /> : <Search size={16} />}
            </Button>
          </div>

          {searchError && (
            <div className="rounded-md bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
              {searchError}
            </div>
          )}

          {searchResult && (
            <div className={`rounded-md border px-4 py-3 space-y-2 ${
              searchResult.isPaid ? 'bg-green-50 border-green-200' : 'bg-amber-50 border-amber-200'
            }`}>
              <div className="flex items-center justify-between">
                <span className="text-slate-600 font-medium">Event ID:</span>
                <span className="font-mono font-bold">{searchResult.parkingEventId}</span>
              </div>
              <div className="flex items-center justify-between">
                <span className="text-slate-600 font-medium">Status:</span>
                <span className={`inline-flex items-center gap-1 font-semibold ${
                  searchResult.isPaid ? 'text-green-600' : 'text-amber-600'
                }`}>
                  {searchResult.isPaid ? <CheckCircle2 size={16} /> : <DollarSign size={16} />}
                  {searchResult.isPaid ? 'PAID' : 'UNPAID'}
                </span>
              </div>
              {searchResult.payments.length > 0 && (
                <div className="border-t pt-2 mt-2 space-y-1">
                  <p className="text-xs text-slate-600 font-semibold">Payments:</p>
                  {searchResult.payments.map((p) => (
                    <div key={p.paymentId} className="flex justify-between text-xs text-slate-600">
                      <span>#{p.paymentId} · {p.paymentMethod}</span>
                      <span>
                        {p.amount} UAH ·{' '}
                        <span className={p.status === 'COMPLETED' ? 'text-green-600 font-semibold' : 'text-red-500 font-semibold'}>
                          {p.status}
                        </span>
                      </span>
                    </div>
                  ))}
                </div>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {/* ────── Payment Form (shown only when unpaid) ────── */}
      {searchResult && !searchResult.isPaid && !paymentSuccess && (
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <DollarSign size={18} /> Process Payment
            </CardTitle>
            <CardDescription>Record payment for this parking session</CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label htmlFor="amount">Amount (UAH)</Label>
              <Input
                id="amount"
                type="number"
                placeholder="100.00"
                value={payForm.amount}
                onChange={(e) => setPayForm(prev => ({ ...prev, amount: e.target.value }))}
                disabled={payMutation.isPending}
              />
            </div>

            <div className="space-y-2">
              <Label htmlFor="payment-method">Payment Method</Label>
              <select
                id="payment-method"
                className="w-full border rounded-md px-3 py-2 text-sm"
                value={payForm.paymentMethod}
                onChange={(e) => setPayForm(prev => ({ ...prev, paymentMethod: e.target.value as typeof payForm.paymentMethod }))}
                disabled={payMutation.isPending}
              >
                <option value="CARD">Card</option>
                <option value="CASH">Cash</option>
                <option value="MOBILE_PAY">Mobile Pay</option>
              </select>
            </div>

            <Button
              className="w-full"
              disabled={!payForm.amount || payMutation.isPending}
              onClick={handlePayment}
            >
              {payMutation.isPending && <Loader2 size={16} className="animate-spin mr-2" />}
              Process Payment
            </Button>

            {payMutation.isError && (
              <div className="rounded-md bg-destructive/10 border border-destructive/20 px-4 py-3 text-sm text-destructive">
                {(payMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Payment failed'}
              </div>
            )}
          </CardContent>
        </Card>
      )}

      {/* ────── Payment Success ────── */}
      {paymentSuccess && searchResult && (
        <Card className="border-green-200 bg-green-50">
          <CardContent className="pt-6 text-center space-y-4">
            <div className="flex justify-center">
              <CheckCircle2 size={48} className="text-green-600" />
            </div>
            <div>
              <p className="font-bold text-lg text-green-700">Payment Completed!</p>
              <p className="text-sm text-slate-600">
               Ticket: {ticketCode} · Event #{searchResult.parkingEventId}
              </p>
            </div>
            <Button
              variant="outline"
              onClick={() => {
                setTicketCode('')
                setSearchResult(null)
                setPaymentSuccess(false)
                setPayForm({ amount: '', paymentMethod: 'CARD' })
              }}
            >
              Search Another Ticket
            </Button>
          </CardContent>
        </Card>
      )}
    </div>
  )
}

