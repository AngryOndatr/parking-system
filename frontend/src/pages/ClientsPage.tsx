import { useState } from 'react'
import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query'
import { Users, Plus, Search, Loader2, Trash2, RefreshCw } from 'lucide-react'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { getClients, createClient, deleteClient, searchClientByPhone } from '@/api/clients'
import type { Client } from '@/api/clients'
import { useAuthStore } from '@/store/authStore'
import { useLanguage } from '@/store/languageContext'

export default function ClientsPage() {
  const qc = useQueryClient()
  const { role } = useAuthStore()
  const { t } = useLanguage()
  const [showForm, setShowForm] = useState(false)
  const [searchPhone, setSearchPhone] = useState('')
  const [searchResult, setSearchResult] = useState<Client | null | 'not-found'>(null)
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', email: '' })
  const [formError, setFormError] = useState<string | null>(null)
  const formatRegistrationDate = (iso: string) => new Date(iso).toLocaleDateString()

  const { data: clients = [], isLoading, refetch } = useQuery({
    queryKey: ['clients'],
    queryFn: getClients,
  })

  const createMutation = useMutation({
    mutationFn: createClient,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['clients'] })
      setForm({ fullName: '', phoneNumber: '', email: '' })
      setShowForm(false)
      setFormError(null)
    },
    onError: (e: { response?: { data?: { message?: string } } }) => {
      setFormError(e.response?.data?.message ?? 'Failed to create client')
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteClient,
    onSuccess: () => qc.invalidateQueries({ queryKey: ['clients'] }),
  })

  const handleSearch = async () => {
    if (!searchPhone.trim()) return
    try {
      const result = await searchClientByPhone(searchPhone.trim())
      setSearchResult(result)
    } catch {
      setSearchResult('not-found')
    }
  }

  const canWrite = role === 'ADMIN' || role === 'OPERATOR'
  const totalClients = clients.length

  return (
    <div className="space-y-6">
      <div className="flex flex-wrap items-start gap-4 rounded-xl border border-slate-200 bg-white/80 p-4 shadow-sm">
        <div className="flex flex-1 min-w-[240px] flex-col gap-1">
          <div className="flex items-center gap-2 text-slate-900">
            <Users size={26} className="text-primary" />
            <div>
              <h1 className="text-2xl font-semibold">{t('clients.title')}</h1>
              <p className="text-sm text-muted-foreground">{t('clients.description')}</p>
            </div>
          </div>
          <div className="flex flex-wrap gap-2 text-sm text-slate-600">
            <span className="rounded-full bg-slate-100 px-3 py-1 font-medium text-slate-900">{totalClients} {t('common.records')}</span>
            <span className="rounded-full bg-slate-100 px-3 py-1">
              {t('clients.access')}: {canWrite ? t('clients.read_write') : t('clients.read_only')}
            </span>
          </div>
        </div>
        <div className="flex flex-wrap gap-2">
          <Button variant="ghost" size="sm" onClick={() => refetch()}>
            <RefreshCw size={16} className="mr-1" /> {t('common.refresh')}
          </Button>
          {canWrite && (
            <Button size="sm" onClick={() => setShowForm(!showForm)}>
              <Plus size={16} className="mr-1" /> {showForm ? t('clients.hide_form') : t('clients.add_client')}
            </Button>
          )}
        </div>
      </div>

      <div className="grid gap-4 xl:grid-cols-2">
        <Card className="shadow-sm">
          <CardHeader>
            <CardTitle className="text-base">{t('clients.search_by_phone')}</CardTitle>
            <CardDescription>{t('clients.use_international_format')}</CardDescription>
          </CardHeader>
          <CardContent className="space-y-3">
            <div className="grid gap-2 sm:grid-cols-[minmax(0,1fr)_auto]">
              <Input
                placeholder={t('clients.phone_placeholder')}
                value={searchPhone}
                onChange={(e) => { setSearchPhone(e.target.value); setSearchResult(null) }}
              />
              <Button
                variant="outline"
                onClick={handleSearch}
                disabled={!searchPhone.trim()}
                className="w-full sm:w-auto"
              >
                <Search size={16} className="mr-1" /> {t('common.search')}
              </Button>
            </div>
            {searchResult === 'not-found' && (
              <p className="text-sm text-destructive">{t('common.no_data')}</p>
            )}
            {searchResult && searchResult !== 'not-found' && (
              <div className="rounded-md border border-blue-200 bg-blue-50 px-4 py-3 text-sm">
                <p className="font-semibold">{searchResult.fullName}</p>
                <p className="text-slate-600">{searchResult.phoneNumber} · {searchResult.email}</p>
              </div>
            )}
          </CardContent>
        </Card>

        {showForm && (
          <Card className="shadow-sm">
            <CardHeader>
              <CardTitle className="text-base">{t('clients.add_client')}</CardTitle>
              <CardDescription>{t('clients.add_client')}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="grid gap-4 sm:grid-cols-2">
                <div className="space-y-1">
                  <Label>{t('clients.full_name')}</Label>
                  <Input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="John Doe" />
                </div>
                <div className="space-y-1">
                  <Label>{t('clients.phone_number')}</Label>
                  <Input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} placeholder={t('clients.phone_placeholder')} />
                </div>
                <div className="space-y-1 sm:col-span-2">
                  <Label>{t('clients.email')}</Label>
                  <Input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="john@example.com" />
                </div>
              </div>
              {formError && <p className="mt-2 text-sm text-destructive">{formError}</p>}
              <div className="mt-4 flex flex-wrap gap-2">
                <Button onClick={() => createMutation.mutate(form)} disabled={createMutation.isPending || !form.fullName || !form.phoneNumber}>
                  {createMutation.isPending ? <Loader2 size={16} className="mr-1 animate-spin" /> : null} {t('common.save')}
                </Button>
                <Button variant="ghost" onClick={() => { setShowForm(false); setFormError(null) }}>{t('common.cancel')}</Button>
              </div>
            </CardContent>
          </Card>
        )}
      </div>

      <Card className="shadow-sm">
        <CardHeader>
          <CardTitle className="text-base">{t('clients.registered')} ({totalClients})</CardTitle>
          <CardDescription>{t('common.no_data')}</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center gap-2 py-6 text-sm text-muted-foreground">
              <Loader2 size={16} className="animate-spin" /> {t('common.loading')}
            </div>
          ) : clients.length === 0 ? (
            <p className="py-4 text-sm text-muted-foreground">{t('common.no_data')}</p>
          ) : (
            <div className="space-y-4">
              <div className="grid gap-3 lg:hidden">
                {clients.map((c) => (
                  <div key={c.id} className="rounded-lg border border-slate-200 bg-white p-4 shadow-sm">
                    <div className="flex items-start justify-between gap-3">
                      <div>
                        <p className="text-base font-semibold text-slate-900">{c.fullName}</p>
                        <p className="text-xs text-slate-500">Client #{c.id}</p>
                      </div>
                      {canWrite && (
                        <Button
                          variant="ghost"
                          size="icon"
                          className="text-red-500 hover:text-red-700"
                          onClick={() => deleteMutation.mutate(c.id)}
                          disabled={deleteMutation.isPending}
                          aria-label={`Delete ${c.fullName}`}
                        >
                          <Trash2 size={16} />
                        </Button>
                      )}
                    </div>
                    <dl className="mt-3 grid gap-2 text-sm text-slate-600 sm:grid-cols-2">
                      <div>
                        <dt className="font-medium text-slate-500">{t('clients.phone_number')}</dt>
                        <dd className="text-slate-900">{c.phoneNumber}</dd>
                      </div>
                      <div>
                        <dt className="font-medium text-slate-500">{t('clients.email')}</dt>
                        <dd className="text-slate-900">{c.email}</dd>
                      </div>
                      <div>
                        <dt className="font-medium text-slate-500">{t('clients.registered')}</dt>
                        <dd>{formatRegistrationDate(c.registeredAt)}</dd>
                      </div>
                    </dl>
                  </div>
                ))}
              </div>

              <div className="hidden lg:block overflow-x-auto">
                <table className="w-full text-sm">
                  <thead>
                    <tr className="border-b text-left text-slate-500">
                      <th className="pb-2 pr-4 font-medium">ID</th>
                      <th className="pb-2 pr-4 font-medium">{t('clients.full_name')}</th>
                      <th className="pb-2 pr-4 font-medium">{t('clients.phone_number')}</th>
                      <th className="pb-2 pr-4 font-medium">{t('clients.email')}</th>
                      <th className="pb-2 pr-4 font-medium">{t('clients.registered')}</th>
                      {canWrite && <th className="pb-2 font-medium"></th>}
                    </tr>
                  </thead>
                  <tbody>
                    {clients.map((c) => (
                      <tr key={c.id} className="border-b last:border-0 hover:bg-slate-50">
                        <td className="py-2 pr-4 text-slate-400">{c.id}</td>
                        <td className="py-2 pr-4 font-medium">{c.fullName}</td>
                        <td className="py-2 pr-4">{c.phoneNumber}</td>
                        <td className="py-2 pr-4 text-slate-600">{c.email}</td>
                        <td className="py-2 pr-4 text-slate-400">{formatRegistrationDate(c.registeredAt)}</td>
                        {canWrite && (
                          <td className="py-2">
                            <Button
                              variant="ghost"
                              size="sm"
                              className="text-red-500 hover:text-red-700"
                              onClick={() => deleteMutation.mutate(c.id)}
                              disabled={deleteMutation.isPending}
                            >
                              <Trash2 size={14} />
                            </Button>
                          </td>
                        )}
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
