import { useState } from 'react'
import { useMutation } from '@tanstack/react-query'
import { Car, LogIn, LogOut, Loader2, CheckCircle2, XCircle } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { gateEntry, gateExit } from '@/api/gate'
import type { GateEntryResponse, GateExitResponse } from '@/api/gate'

function StatusBadge({ status }: { status: 'OPENED' | 'DENIED' }) {
  return status === 'OPENED' ? (
    <span className="inline-flex items-center gap-1 text-green-600 font-semibold">
      <CheckCircle2 size={16} /> OPENED
    </span>
  ) : (
    <span className="inline-flex items-center gap-1 text-red-600 font-semibold">
      <XCircle size={16} /> DENIED
    </span>
  )
}

export default function GatePage() {
  const [entryPlate, setEntryPlate] = useState('')
  const [exitPlate, setExitPlate] = useState('')
  const [exitTicket, setExitTicket] = useState('')
  const [entryResult, setEntryResult] = useState<GateEntryResponse | null>(null)
  const [exitResult, setExitResult] = useState<GateExitResponse | null>(null)

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
      if (data.gateStatus === 'OPENED') { setExitPlate(''); setExitTicket('') }
    },
  })

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-2">
        <Car size={24} className="text-primary" />
        <h1 className="text-2xl font-bold text-slate-800">Gate Control</h1>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
        {/* Entry */}
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
              onClick={() => { setEntryResult(null); entryMutation.mutate({ licensePlate: entryPlate }) }}
              disabled={!entryPlate.trim() || entryMutation.isPending}
            >
              {entryMutation.isPending ? <Loader2 size={16} className="animate-spin mr-2" /> : null}
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

        {/* Exit */}
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
              <Label htmlFor="exit-ticket">Ticket Code <span className="text-slate-400">(optional for subscribers)</span></Label>
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
              onClick={() => { setExitResult(null); exitMutation.mutate({ licensePlate: exitPlate, ticketCode: exitTicket || undefined }) }}
              disabled={!exitPlate.trim() || exitMutation.isPending}
            >
              {exitMutation.isPending ? <Loader2 size={16} className="animate-spin mr-2" /> : null}
              Open Exit Gate
            </Button>

            {exitMutation.isError && (
              <div className="rounded-md bg-destructive/10 border border-destructive/20 px-3 py-2 text-sm text-destructive">
                {(exitMutation.error as { response?: { data?: { message?: string } } })?.response?.data?.message ?? 'Request failed'}
              </div>
            )}

            {exitResult && (
              <div className={`rounded-md border px-4 py-3 text-sm space-y-1 ${exitResult.gateStatus === 'OPENED' ? 'bg-green-50 border-green-200' : 'bg-red-50 border-red-200'}`}>
                <div className="flex justify-between">
                  <span className="text-slate-600">Gate:</span>
                  <StatusBadge status={exitResult.gateStatus} />
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-600">Payment required:</span>
                  <span className="font-medium">{exitResult.paymentRequired ? `💳 ${exitResult.amountDue ?? '?'} UAH` : '✅ No'}</span>
                </div>
                <p className="text-slate-500 text-xs pt-1">{exitResult.message}</p>
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
