import { useState, useEffect, useRef } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import {
  BadgeCheck, Plus, Trash2, Loader2, RefreshCw, Search,
  CalendarRange, ChevronDown, ChevronUp,
  Banknote, CreditCard, ArrowRight, ArrowLeft, CheckCircle2,
  Phone, User, Car, MapPin,
} from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { PageHeader } from '@/components/PageHeader'
import { useLanguage } from '@/store/languageContext'
import { getClients, searchClientByPlate, searchClientsByName } from '@/api/clients'
import type { Client } from '@/api/clients'
import {
  getSubscriptionsByClient,
  createSubscription,
  deactivateSubscription,
} from '@/api/subscriptions'
import type { Subscription, SubscriptionType } from '@/api/subscriptions'
import { getAvailableSpaces } from '@/api/spaces'
import type { ParkingSpace } from '@/api/spaces'
import { useAuthStore } from '@/store/authStore'

const SUBSCRIPTION_PRICES: Record<SubscriptionType, number> = {
  MONTHLY:   500,
  QUARTERLY: 1_200,
  ANNUAL:    4_000,
  CUSTOM:    0,
}
const CUSTOM_PRICE_PER_DAY = 20

function typeColor(type: string) {
  switch (type) {
    case 'MONTHLY':   return 'bg-blue-100 text-blue-700'
    case 'QUARTERLY': return 'bg-violet-100 text-violet-700'
    case 'ANNUAL':    return 'bg-green-100 text-green-700'
    default:          return 'bg-slate-100 text-slate-600'
  }
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString('uk-UA', { day: '2-digit', month: '2-digit', year: 'numeric' })
}

function toIsoLocal(dateStr: string): string {
  return new Date(dateStr + 'T00:00:00').toISOString()
}

function addDays(days: number): string {
  const d = new Date()
  d.setDate(d.getDate() + days)
  return d.toISOString().slice(0, 10)
}

function today(): string {
  return new Date().toISOString().slice(0, 10)
}

function calcPrice(type: SubscriptionType, startDate: string, endDate: string): number {
  if (type !== 'CUSTOM') return SUBSCRIPTION_PRICES[type]
  const ms = new Date(endDate).getTime() - new Date(startDate).getTime()
  const days = Math.max(1, Math.ceil(ms / 86_400_000))
  return days * CUSTOM_PRICE_PER_DAY
}

interface SubListProps {
  client: Client
  canWrite: boolean
  spaces: ParkingSpace[]
}

function SubscriptionList({ client, canWrite, spaces }: SubListProps) {
  const qc = useQueryClient()
  const { t } = useLanguage()

  const TYPE_OPTIONS: { value: SubscriptionType; label: string; days: number }[] = [
    { value: 'MONTHLY',   label: t('subscriptions.type_monthly'),   days: 30  },
    { value: 'QUARTERLY', label: t('subscriptions.type_quarterly'), days: 90  },
    { value: 'ANNUAL',    label: t('subscriptions.type_annual'),    days: 365 },
    { value: 'CUSTOM',    label: t('subscriptions.type_custom'),    days: 0   },
  ]

  const PAYMENT_METHODS = [
    { value: 'CASH'  as const, label: t('subscriptions.payment_cash'),   icon: <Banknote size={14} />  },
    { value: 'CARD'  as const, label: t('subscriptions.payment_card'),   icon: <CreditCard size={14} /> },
    { value: 'MOBILE_PAY' as const, label: t('subscriptions.payment_mobile'), icon: <CreditCard size={14} /> },
  ]

  const [showForm, setShowForm]   = useState(false)
  const [step, setStep]           = useState<'details' | 'payment'>('details')

  const [subType, setSubType]     = useState<SubscriptionType>('MONTHLY')
  const [startDate, setStartDate] = useState(today())
  const [endDate, setEndDate]     = useState(addDays(30))
  const [selectedSpaceId, setSelectedSpaceId] = useState<number | null>(null)

  const [payMethod, setPayMethod] = useState<'CASH' | 'CARD' | 'MOBILE_PAY'>('CASH')
  const [payAmount, setPayAmount] = useState(String(SUBSCRIPTION_PRICES.MONTHLY))

  const [formError, setFormError] = useState<string | null>(null)
  const [lastCreated, setLastCreated] = useState<Subscription | null>(null)

  const { data: subs = [], isLoading, refetch } = useQuery({
    queryKey: ['subscriptions', client.id],
    queryFn: () => getSubscriptionsByClient(client.id),
  })

  const createMut = useMutation({
    mutationFn: () =>
      createSubscription(client.id, {
        type: subType,
        startDate: toIsoLocal(startDate),
        endDate:   toIsoLocal(endDate),
        parkingSpaceId: selectedSpaceId ?? undefined,
      }),
    onSuccess: (created) => {
      qc.invalidateQueries({ queryKey: ['subscriptions', client.id] })
      setLastCreated(created)
      setFormError(null)
    },
    onError: (e: { response?: { data?: { message?: string } } }) => {
      setFormError(e.response?.data?.message ?? t('subscriptions.date_error'))
    },
  })

  const deactivateMut = useMutation({
    mutationFn: deactivateSubscription,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['subscriptions', client.id] }),
  })

  const handleTypeChange = (type: SubscriptionType) => {
    setSubType(type)
    const opt = TYPE_OPTIONS.find(o => o.value === type)!
    const start = today()
    const end   = opt.days > 0 ? addDays(opt.days) : addDays(30)
    setStartDate(start)
    setEndDate(end)
    setPayAmount(String(calcPrice(type, start, end)))
  }

  const handleDateChange = (field: 'start' | 'end', val: string) => {
    const newStart = field === 'start' ? val : startDate
    const newEnd   = field === 'end'   ? val : endDate
    if (field === 'start') setStartDate(val)
    else setEndDate(val)
    if (subType === 'CUSTOM') {
      setPayAmount(String(calcPrice('CUSTOM', newStart, newEnd)))
    }
  }

  const openForm = () => {
    setShowForm(true)
    setStep('details')
    setSubType('MONTHLY')
    setStartDate(today())
    setEndDate(addDays(30))
    setSelectedSpaceId(null)
    setPayAmount(String(SUBSCRIPTION_PRICES.MONTHLY))
    setPayMethod('CASH')
    setFormError(null)
    setLastCreated(null)
  }

  const closeForm = () => {
    setShowForm(false)
    setStep('details')
    setSelectedSpaceId(null)
    setLastCreated(null)
    setFormError(null)
  }

  const goToPayment = () => {
    if (!startDate || !endDate) return
    if (new Date(endDate) <= new Date(startDate)) {
      setFormError(t('subscriptions.date_error'))
      return
    }
    setFormError(null)
    setPayAmount(String(calcPrice(subType, startDate, endDate)))
    setStep('payment')
  }

  const activeCount = subs.filter(s => s.isActive).length

  return (
    <div className="space-y-3">
      <div className="flex flex-wrap items-center justify-between gap-2">
        <div className="flex items-center gap-2">
          <span className="text-sm font-medium text-slate-700">
            {subs.length === 0
              ? t('subscriptions.no_subs')
              : t('subscriptions.active_of_total', { active: activeCount, total: subs.length })}
          </span>
          <Button variant="ghost" size="icon" className="h-7 w-7" onClick={() => refetch()}>
            <RefreshCw size={13} />
          </Button>
        </div>
        {canWrite && !showForm && (
          <Button size="sm" variant="outline" onClick={openForm}>
            <Plus size={14} className="mr-1" /> {t('subscriptions.add_subscription')}
          </Button>
        )}
      </div>

      {showForm && (
        <div className="rounded-lg border border-blue-200 bg-blue-50/60 p-4 space-y-4">
          <div className="flex items-center gap-2 text-xs font-medium">
            <span className={`rounded-full px-2.5 py-0.5 ${step === 'details' ? 'bg-primary text-white' : 'bg-slate-200 text-slate-500'}`}>
              {t('subscriptions.step1')}
            </span>
            <div className="h-px flex-1 bg-slate-200" />
            <span className={`rounded-full px-2.5 py-0.5 ${step === 'payment' ? 'bg-primary text-white' : 'bg-slate-200 text-slate-500'}`}>
              {t('subscriptions.step2')}
            </span>
          </div>

          {step === 'details' && (
            <>
              <p className="text-sm font-semibold text-blue-800 flex items-center gap-1">
                <CalendarRange size={15} /> {t('subscriptions.choose_plan')}
              </p>

              <div className="grid grid-cols-2 sm:grid-cols-4 gap-2">
                {TYPE_OPTIONS.map(opt => (
                  <button
                    key={opt.value}
                    type="button"
                    onClick={() => handleTypeChange(opt.value)}
                    className={`rounded-md border px-3 py-2.5 text-xs font-medium text-center transition-colors
                      ${subType === opt.value
                        ? 'bg-primary text-white border-primary shadow-sm'
                        : 'bg-white text-slate-600 border-slate-200 hover:border-primary'}`}
                  >
                    <div>{opt.label}</div>
                    {opt.days > 0 && (
                      <div className={`mt-0.5 font-semibold ${subType === opt.value ? 'text-white/80' : 'text-slate-400'}`}>
                        {SUBSCRIPTION_PRICES[opt.value].toLocaleString()} ₴
                      </div>
                    )}
                  </button>
                ))}
              </div>

              <div className="grid gap-3 sm:grid-cols-2">
                <div className="space-y-1">
                  <Label className="text-xs">{t('subscriptions.start_date')}</Label>
                  <Input type="date" value={startDate} className="h-9 text-sm bg-white"
                    onChange={e => handleDateChange('start', e.target.value)} />
                </div>
                <div className="space-y-1">
                  <Label className="text-xs">{t('subscriptions.end_date')}</Label>
                  <Input type="date" value={endDate} className="h-9 text-sm bg-white"
                    onChange={e => handleDateChange('end', e.target.value)} />
                </div>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs flex items-center gap-1">
                  <MapPin size={12} /> {t('subscriptions.reserve_space_label')}
                  <span className="text-slate-400 font-normal">{t('subscriptions.optional_label')}</span>
                </Label>
                {spaces.length === 0 ? (
                  <p className="text-xs text-slate-400 italic">{t('subscriptions.no_spaces_available')}</p>
                ) : (
                  <div className="grid grid-cols-2 sm:grid-cols-3 gap-1.5 max-h-36 overflow-y-auto pr-0.5">
                    <button
                      type="button"
                      onClick={() => setSelectedSpaceId(null)}
                      className={`rounded-md border px-2.5 py-1.5 text-xs font-medium text-center transition-colors
                        ${selectedSpaceId === null
                          ? 'bg-slate-600 text-white border-slate-600'
                          : 'bg-white text-slate-500 border-slate-200 hover:border-slate-400'}`}
                    >
                      {t('subscriptions.no_specific_space')}
                    </button>
                    {spaces.map(sp => (
                      <button
                        key={sp.spaceId}
                        type="button"
                        onClick={() => setSelectedSpaceId(sp.spaceId)}
                        className={`rounded-md border px-2.5 py-1.5 text-xs font-medium text-center transition-colors
                          ${selectedSpaceId === sp.spaceId
                            ? 'bg-primary text-white border-primary'
                            : 'bg-white text-slate-600 border-slate-200 hover:border-primary'}`}
                      >
                        <div className="font-semibold">{sp.spaceNumber}</div>
                        {sp.section && <div className="text-[10px] opacity-75">{sp.section}</div>}
                        {sp.type !== 'STANDARD' && (
                          <div className="text-[10px] opacity-75">{sp.type}</div>
                        )}
                      </button>
                    ))}
                  </div>
                )}
                {selectedSpaceId !== null && (
                  <p className="text-xs text-primary font-medium flex items-center gap-1">
                    <MapPin size={11} />
                    {t('subscriptions.space_reserved_msg', { space: spaces.find(s => s.spaceId === selectedSpaceId)?.spaceNumber ?? selectedSpaceId })}
                  </p>
                )}
              </div>

              {subType === 'CUSTOM' && startDate && endDate && new Date(endDate) > new Date(startDate) && (
                <p className="text-xs text-slate-600 bg-white/70 rounded px-3 py-1.5 border border-slate-200">
                  {t('subscriptions.custom_days_price', {
                    days: Math.ceil((new Date(endDate).getTime() - new Date(startDate).getTime()) / 86_400_000),
                    price: calcPrice('CUSTOM', startDate, endDate).toLocaleString(),
                    rate: CUSTOM_PRICE_PER_DAY,
                  })}
                </p>
              )}

              {formError && <p className="text-xs text-destructive">{formError}</p>}

              <div className="flex flex-wrap gap-2">
                <Button size="sm" onClick={goToPayment} disabled={!startDate || !endDate}>
                  {t('subscriptions.next_payment')} <ArrowRight size={14} className="ml-1" />
                </Button>
                <Button size="sm" variant="ghost" onClick={closeForm}>{t('common.cancel')}</Button>
              </div>
            </>
          )}

          {step === 'payment' && !lastCreated && (
            <>
              <p className="text-sm font-semibold text-blue-800 flex items-center gap-1">
                <Banknote size={15} /> {t('subscriptions.collect_payment')}
              </p>

              <div className="rounded-md bg-white border border-slate-200 px-4 py-3 text-sm space-y-1.5">
                <div className="flex justify-between">
                  <span className="text-slate-500">{t('subscriptions.plan_label')}</span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${typeColor(subType)}`}>{subType}</span>
                </div>
                <div className="flex justify-between">
                  <span className="text-slate-500">{t('subscriptions.period_label')}</span>
                  <span className="text-slate-700">{formatDate(toIsoLocal(startDate))} – {formatDate(toIsoLocal(endDate))}</span>
                </div>
                {selectedSpaceId !== null && (
                  <div className="flex justify-between">
                    <span className="text-slate-500 flex items-center gap-1"><MapPin size={11} /> {t('subscriptions.space_label')}</span>
                    <span className="text-slate-700 font-medium">
                      {spaces.find(s => s.spaceId === selectedSpaceId)?.spaceNumber ?? `#${selectedSpaceId}`}
                    </span>
                  </div>
                )}
                <div className="flex justify-between font-semibold text-base border-t border-slate-100 pt-2 mt-1">
                  <span>{t('subscriptions.total_label')}</span>
                  <span className="text-primary">{Number(payAmount).toLocaleString()} ₴</span>
                </div>
              </div>

              <div className="space-y-1.5">
                <Label className="text-xs">{t('subscriptions.payment_method_label')}</Label>
                <div className="grid grid-cols-3 gap-2">
                  {PAYMENT_METHODS.map(m => (
                    <button
                      key={m.value}
                      type="button"
                      onClick={() => setPayMethod(m.value)}
                      className={`flex items-center justify-center gap-1.5 rounded-md border px-2 py-2 text-xs font-medium transition-colors
                        ${payMethod === m.value
                          ? 'bg-primary text-white border-primary'
                          : 'bg-white text-slate-600 border-slate-200 hover:border-primary'}`}
                    >
                      {m.icon} {m.label}
                    </button>
                  ))}
                </div>
              </div>

              <div className="space-y-1">
                <Label className="text-xs">{t('subscriptions.amount_received')}</Label>
                <Input
                  type="number"
                  value={payAmount}
                  onChange={e => setPayAmount(e.target.value)}
                  className="h-9 text-sm bg-white"
                />
              </div>

              {formError && <p className="text-xs text-destructive">{formError}</p>}

              <div className="flex flex-wrap gap-2">
                <Button
                  size="sm"
                  disabled={createMut.isPending || !payAmount || Number(payAmount) <= 0}
                  onClick={() => createMut.mutate()}
                >
                  {createMut.isPending
                    ? <Loader2 size={14} className="animate-spin mr-1" />
                    : <CheckCircle2 size={14} className="mr-1" />}
                  {t('subscriptions.confirm_and_activate')}
                </Button>
                <Button size="sm" variant="ghost" onClick={() => { setStep('details'); setFormError(null) }}>
                  <ArrowLeft size={14} className="mr-1" /> {t('common.back')}
                </Button>
              </div>
            </>
          )}

          {lastCreated && (
            <div className="rounded-md border border-green-200 bg-green-50 px-4 py-4 text-sm space-y-3">
              <p className="font-semibold text-green-700 flex items-center gap-1.5">
                <CheckCircle2 size={16} /> {t('subscriptions.activated_msg')}
              </p>
              <div className="space-y-1 text-slate-600">
                <div className="flex justify-between">
                  <span>{t('subscriptions.plan_label')}</span>
                  <span className={`rounded-full px-2 py-0.5 text-xs font-semibold ${typeColor(lastCreated.type)}`}>{lastCreated.type}</span>
                </div>
                <div className="flex justify-between">
                  <span>{t('subscriptions.period_label')}</span>
                  <span>{formatDate(lastCreated.startDate)} – {formatDate(lastCreated.endDate)}</span>
                </div>
                {lastCreated.spaceNumber && typeof lastCreated.spaceNumber === 'string' && (
                  <div className="flex justify-between">
                    <span className="flex items-center gap-1"><MapPin size={11} /> {t('subscriptions.reserved_space_label')}</span>
                    <span className="font-semibold text-primary">{lastCreated.spaceNumber}</span>
                  </div>
                )}
                <div className="flex justify-between font-semibold">
                  <span>{t('subscriptions.paid_via', { method: payMethod })}</span>
                  <span className="text-green-700">{Number(payAmount).toLocaleString()} ₴</span>
                </div>
              </div>
              <Button size="sm" variant="outline" onClick={closeForm}>{t('subscriptions.done')}</Button>
            </div>
          )}
        </div>
      )}

      {isLoading ? (
        <div className="flex items-center gap-2 py-3 text-sm text-muted-foreground">
          <Loader2 size={14} className="animate-spin" /> {t('common.loading')}
        </div>
      ) : subs.length === 0 ? (
        <p className="text-sm text-muted-foreground py-2">{t('subscriptions.no_subs_client')}</p>
      ) : (
        <div className="space-y-2">
          {subs.map((sub: Subscription) => (
            <div
              key={sub.id}
              className={`flex flex-wrap items-center justify-between gap-3 rounded-lg border px-4 py-3 text-sm
                ${sub.isActive ? 'bg-white border-slate-200' : 'bg-slate-50 border-slate-100 opacity-60'}`}
            >
              <div className="flex items-center gap-3 min-w-0 flex-wrap">
                <span className={`rounded-full px-2.5 py-0.5 text-xs font-semibold ${typeColor(sub.type)}`}>
                  {sub.type}
                </span>
                <span className="text-slate-500 text-xs">
                  {formatDate(sub.startDate)} → {formatDate(sub.endDate)}
                </span>
                {sub.spaceNumber && typeof sub.spaceNumber === 'string' && (
                  <span className="flex items-center gap-1 rounded-full bg-indigo-50 border border-indigo-200 px-2 py-0.5 text-xs text-indigo-700 font-medium">
                    <MapPin size={10} /> {sub.spaceNumber}
                  </span>
                )}
              </div>
              <div className="flex items-center gap-2 shrink-0">
                <span className={`text-xs font-medium ${sub.isActive ? 'text-green-600' : 'text-slate-400'}`}>
                  {sub.isActive ? t('subscriptions.status_active') : t('subscriptions.status_inactive')}
                </span>
                {canWrite && sub.isActive && (
                  <Button
                    variant="ghost"
                    size="icon"
                    className="h-7 w-7 text-red-400 hover:text-red-600"
                    onClick={() => deactivateMut.mutate(sub.id)}
                    disabled={deactivateMut.isPending}
                    title={t('subscriptions.deactivate')}
                  >
                    <Trash2 size={13} />
                  </Button>
                )}
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  )
}

type QueryMode = 'phone' | 'plate' | 'name'

function detectMode(q: string): QueryMode {
  const trimmed = q.trim()
  if (/^[+\d\s\-()]+$/.test(trimmed)) return 'phone'
  if (/^[A-ZА-ЯІЇЄ0-9]{3,10}$/i.test(trimmed) && /[A-ZА-ЯІЇЄ]/i.test(trimmed) && /\d/.test(trimmed)) return 'plate'
  return 'name'
}

interface UniversalSearchProps {
  clients: Client[]
  onSelect: (c: Client) => void
  selectedId?: number
}

function UniversalClientSearch({ clients, onSelect, selectedId }: UniversalSearchProps) {
  const { t } = useLanguage()

  const MODE_META: Record<QueryMode, { icon: React.ReactNode; hint: string; color: string }> = {
    phone: { icon: <Phone size={12} />,  hint: t('subscriptions.search_mode_phone'), color: 'text-blue-600 bg-blue-50 border-blue-200'   },
    plate: { icon: <Car  size={12} />,   hint: t('subscriptions.search_mode_plate'), color: 'text-amber-700 bg-amber-50 border-amber-200' },
    name:  { icon: <User size={12} />,   hint: t('subscriptions.search_mode_name'),  color: 'text-violet-700 bg-violet-50 border-violet-200' },
  }

  const [query, setQuery]           = useState('')
  const [results, setResults]       = useState<Client[]>([])
  const [loading, setLoading]       = useState(false)
  const [open, setOpen]             = useState(false)
  const [noResult, setNoResult]     = useState(false)
  const inputRef                    = useRef<HTMLInputElement>(null)
  const debounceRef                 = useRef<ReturnType<typeof setTimeout> | null>(null)

  const mode = detectMode(query)
  const meta = MODE_META[mode]

  useEffect(() => {
    const q = query.trim()
    if (q.length < 2) { setResults([]); setOpen(false); setNoResult(false); return }

    if (debounceRef.current) clearTimeout(debounceRef.current)
    debounceRef.current = setTimeout(async () => {
      setLoading(true)
      setNoResult(false)
      try {
        if (mode === 'phone') {
          const hits = clients.filter(c => c.phoneNumber.replace(/\D/g, '').includes(q.replace(/\D/g, '')))
          setResults(hits)
          setNoResult(hits.length === 0)
        } else if (mode === 'plate') {
          const hit = await searchClientByPlate(q)
          setResults([hit])
          setNoResult(false)
        } else {
          const local = clients.filter(c =>
            c.fullName.toLowerCase().includes(q.toLowerCase())
          )
          if (local.length > 0) {
            setResults(local)
            setNoResult(false)
          } else {
            const remote = await searchClientsByName(q)
            setResults(remote)
            setNoResult(remote.length === 0)
          }
        }
      } catch {
        setResults([])
        setNoResult(true)
      } finally {
        setLoading(false)
        setOpen(true)
      }
    }, 280)
  // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [query])

  const handleSelect = (c: Client) => {
    setQuery(c.fullName)
    setOpen(false)
    onSelect(c)
  }

  return (
    <div className="relative">
      <div className="relative">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400 pointer-events-none" />
        <Input
          ref={inputRef}
          value={query}
          onChange={e => { setQuery(e.target.value); setOpen(false) }}
          onFocus={() => results.length > 0 && setOpen(true)}
          placeholder={t('subscriptions.search_placeholder')}
          className="pl-8 pr-24 h-10"
          autoComplete="off"
        />
        {query.trim().length >= 2 && (
          <span className={`absolute right-2 top-1/2 -translate-y-1/2 flex items-center gap-1 rounded-full border px-2 py-0.5 text-[11px] font-medium ${meta.color}`}>
            {meta.icon} {meta.hint}
          </span>
        )}
        {loading && (
          <Loader2 size={13} className="absolute right-2 top-1/2 -translate-y-1/2 animate-spin text-slate-400" />
        )}
      </div>

      {query.trim().length === 0 && (
        <p className="mt-1 text-[11px] text-slate-400">
          {t('subscriptions.search_hint')}
        </p>
      )}

      {open && (
        <div className="absolute z-50 mt-1 w-full rounded-lg border border-slate-200 bg-white shadow-lg overflow-hidden">
          {noResult ? (
            <p className="px-4 py-3 text-sm text-slate-400">{t('subscriptions.no_clients_found')}</p>
          ) : (
            <ul className="max-h-56 overflow-y-auto divide-y divide-slate-100">
              {results.map(c => (
                <li key={c.id}>
                  <button
                    type="button"
                    onMouseDown={e => { e.preventDefault(); handleSelect(c) }}
                    className={`w-full text-left px-4 py-2.5 hover:bg-slate-50 transition-colors
                      ${selectedId === c.id ? 'bg-primary/5' : ''}`}
                  >
                    <p className="text-sm font-medium text-slate-800">{c.fullName}</p>
                    <p className="text-xs text-slate-400">{c.phoneNumber} · {c.email}</p>
                  </button>
                </li>
              ))}
            </ul>
          )}
        </div>
      )}
    </div>
  )
}

export default function SubscriptionsPage() {
  const { role } = useAuthStore()
  const { t } = useLanguage()
  const canWrite = role === 'ADMIN' || role === 'MANAGER' || role === 'OPERATOR'

  const [selectedClient, setSelectedClient] = useState<Client | null>(null)
  const [expandedId, setExpandedId]         = useState<number | null>(null)

  const { data: clients = [], isLoading: clientsLoading } = useQuery({
    queryKey: ['clients'],
    queryFn: getClients,
  })

  const { data: availableSpaces = [] } = useQuery({
    queryKey: ['parking-spaces-available'],
    queryFn: getAvailableSpaces,
    staleTime: 30_000,
  })

  const handleSelectClient = (c: Client) => {
    setSelectedClient(c)
    setExpandedId(c.id)
  }

  return (
    <div className="space-y-6">
      <PageHeader
        icon={<BadgeCheck size={24} />}
        title={t('subscriptions.title')}
        description={t('subscriptions.page_desc')}
      />

      <div className="grid gap-4 xl:grid-cols-[1fr_2fr]">

        <div className="space-y-4">
          <Card className="shadow-sm">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">{t('subscriptions.find_client_title')}</CardTitle>
              <CardDescription>{t('subscriptions.find_client_desc')}</CardDescription>
            </CardHeader>
            <CardContent>
              <UniversalClientSearch
                clients={clients}
                onSelect={handleSelectClient}
                selectedId={selectedClient?.id}
              />
            </CardContent>
          </Card>

          <Card className="shadow-sm">
            <CardHeader className="pb-3">
              <CardTitle className="text-base">{t('subscriptions.all_clients_title')}</CardTitle>
            </CardHeader>
            <CardContent className="p-0">
              {clientsLoading ? (
                <div className="flex items-center gap-2 px-4 py-3 text-sm text-muted-foreground">
                  <Loader2 size={14} className="animate-spin" /> {t('common.loading')}
                </div>
              ) : clients.length === 0 ? (
                <p className="px-4 py-3 text-sm text-muted-foreground">{t('subscriptions.no_clients')}</p>
              ) : (
                <ul className="divide-y divide-slate-100 max-h-[360px] overflow-y-auto">
                  {clients.map(c => (
                    <li key={c.id}>
                      <button
                        type="button"
                        onClick={() => handleSelectClient(c)}
                        className={`w-full text-left px-4 py-2.5 text-sm transition-colors
                          ${selectedClient?.id === c.id
                            ? 'bg-primary/5 text-primary font-medium'
                            : 'hover:bg-slate-50 text-slate-700'}`}
                      >
                        <span className="font-medium">{c.fullName}</span>
                        <span className="ml-2 text-xs text-slate-400">{c.phoneNumber}</span>
                      </button>
                    </li>
                  ))}
                </ul>
              )}
            </CardContent>
          </Card>
        </div>

        <div>
          {!selectedClient ? (
            <div className="flex flex-col items-center justify-center rounded-xl border-2 border-dashed border-slate-200 bg-white py-16 text-center">
              <BadgeCheck size={40} className="text-slate-300 mb-3" />
              <p className="text-slate-500 text-sm">{t('subscriptions.select_client_prompt')}</p>
            </div>
          ) : (
            <Card className="shadow-sm">
              <CardHeader>
                <div className="flex flex-wrap items-start justify-between gap-2">
                  <div>
                    <CardTitle className="text-base flex items-center gap-2">
                      <BadgeCheck size={16} className="text-primary" />
                      {selectedClient.fullName}
                    </CardTitle>
                    <CardDescription>
                      #{selectedClient.id} · {selectedClient.phoneNumber} · {selectedClient.email}
                    </CardDescription>
                  </div>
                  <button
                    type="button"
                    className="text-slate-400 hover:text-slate-600 text-lg leading-none"
                    onClick={() => setSelectedClient(null)}
                    title={t('common.close')}
                  >
                    ✕
                  </button>
                </div>
              </CardHeader>
              <CardContent>
                <SubscriptionList client={selectedClient} canWrite={canWrite} spaces={availableSpaces} />
              </CardContent>
            </Card>
          )}

          {!selectedClient && clients.length > 0 && (
            <div className="mt-4 space-y-2">
              <p className="text-xs text-slate-500 font-medium uppercase tracking-wide px-1">{t('subscriptions.quick_overview')}</p>
              {clients.map(c => (
                <div key={c.id} className="rounded-lg border border-slate-200 bg-white shadow-sm overflow-hidden">
                  <button
                    type="button"
                    onClick={() => setExpandedId(expandedId === c.id ? null : c.id)}
                    className="w-full flex items-center justify-between px-4 py-3 text-sm hover:bg-slate-50 transition-colors"
                  >
                    <span className="font-medium text-slate-800">{c.fullName}</span>
                    <span className="flex items-center gap-2 text-slate-400">
                      <span className="text-xs">{c.phoneNumber}</span>
                      {expandedId === c.id ? <ChevronUp size={14} /> : <ChevronDown size={14} />}
                    </span>
                  </button>
                  {expandedId === c.id && (
                    <div className="border-t border-slate-100 px-4 py-4">
                      <SubscriptionList client={c} canWrite={canWrite} spaces={availableSpaces} />
                    </div>
                  )}
                </div>
              ))}
            </div>
          )}
        </div>
      </div>
    </div>
  )
}
