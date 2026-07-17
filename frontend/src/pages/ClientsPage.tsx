import { useEffect, useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { Car, Loader2, Pencil, Plus, Search, Trash2, Users } from 'lucide-react'
import { useNavigate } from 'react-router-dom'
import { PageHeader } from '@/components/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { addVehicleToClient, createClient, deleteClient, getClients, getVehicles, updateVehicle } from '@/api/clients'
import type { Vehicle } from '@/api/clients'
import { useAuthStore } from '@/store/authStore'
import { useLanguage } from '@/store/languageContext'

type QueryMode = 'phone' | 'plate' | 'name'

function detectMode(q: string): QueryMode {
  const trimmed = q.trim()
  if (!trimmed) return 'name'
  if (/^[+\d\s\-()]+$/.test(trimmed)) return 'phone'
  if (/^[A-ZА-ЯІЇЄ0-9]{3,10}$/i.test(trimmed) && /[A-ZА-ЯІЇЄ]/i.test(trimmed) && /\d/.test(trimmed)) return 'plate'
  return 'name'
}

function formatDate(iso: string) {
  return new Date(iso).toLocaleDateString()
}

interface EditableVehicle {
  id?: number
  licensePlate: string
  isAllowed?: boolean
}

export default function ClientsPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const { role } = useAuthStore()
  const { t } = useLanguage()
  const canWrite = role === 'ADMIN' || role === 'MANAGER' || role === 'OPERATOR'

  const [showCreateForm, setShowCreateForm] = useState(false)
  const [query, setQuery] = useState('')
  const [createForm, setCreateForm] = useState({ fullName: '', phoneNumber: '', email: '' })
  const [newVehiclePlate, setNewVehiclePlate] = useState('')
  const [selectedExistingVehicleId, setSelectedExistingVehicleId] = useState<number | ''>('')
  const [createVehicles, setCreateVehicles] = useState<EditableVehicle[]>([])
  const [createError, setCreateError] = useState<string | null>(null)
  const [openMenuClientId, setOpenMenuClientId] = useState<number | null>(null)

  useEffect(() => {
    const handleOutsideClick = (event: MouseEvent) => {
      const target = event.target as HTMLElement | null
      if (target?.closest('[data-client-menu]')) return
      setOpenMenuClientId(null)
    }

    document.addEventListener('mousedown', handleOutsideClick)
    return () => {
      document.removeEventListener('mousedown', handleOutsideClick)
    }
  }, [])

  const { data: clients = [], isLoading: clientsLoading } = useQuery({
    queryKey: ['clients'],
    queryFn: getClients,
  })

  const { data: vehicles = [], isLoading: vehiclesLoading } = useQuery({
    queryKey: ['vehicles'],
    queryFn: getVehicles,
  })

  const createMutation = useMutation({
    mutationFn: async (payload: { form: { fullName: string; phoneNumber: string; email: string }; vehiclesForm: EditableVehicle[] }) => {
      const created = await createClient(payload.form)
      const clientId = created.id
      if (!clientId) throw new Error('Failed to create client')

      const vehiclesById = new Map(vehicles.map((v) => [v.id, v]))
      for (const vehicle of payload.vehiclesForm) {
        if (vehicle.id) {
          const source = vehiclesById.get(vehicle.id)
          await updateVehicle(vehicle.id, {
            licensePlate: vehicle.licensePlate.trim().toUpperCase(),
            clientId,
            isAllowed: source?.isAllowed ?? vehicle.isAllowed ?? true,
          })
          continue
        }

        await addVehicleToClient(clientId, {
          licensePlate: vehicle.licensePlate.trim().toUpperCase(),
        })
      }
    },
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['clients'] })
      qc.invalidateQueries({ queryKey: ['vehicles'] })
      setCreateForm({ fullName: '', phoneNumber: '', email: '' })
      setCreateVehicles([])
      setNewVehiclePlate('')
      setSelectedExistingVehicleId('')
      setCreateError(null)
      setShowCreateForm(false)
    },
    onError: (e: { response?: { data?: { message?: string } } }) => {
      setCreateError(e.response?.data?.message ?? t('clients.failed_to_create'))
    },
  })

  const deleteMutation = useMutation({
    mutationFn: deleteClient,
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ['clients'] })
      qc.invalidateQueries({ queryKey: ['vehicles'] })
    },
  })

  const vehiclesByClient = useMemo(() => {
    const map = new Map<number, Vehicle[]>()
    for (const v of vehicles) {
      const current = map.get(v.clientId) ?? []
      current.push(v)
      map.set(v.clientId, current)
    }
    return map
  }, [vehicles])

  const availableCreateVehicles = useMemo(() => {
    const selectedIds = new Set(createVehicles.map((v) => v.id).filter(Boolean) as number[])
    return vehicles.filter((v) => !selectedIds.has(v.id))
  }, [vehicles, createVehicles])

  const addExistingVehicleToCreate = () => {
    if (!selectedExistingVehicleId) return
    const selected = vehicles.find((v) => v.id === selectedExistingVehicleId)
    if (!selected) return
    setCreateVehicles([...createVehicles, { id: selected.id, licensePlate: selected.licensePlate, isAllowed: selected.isAllowed }])
    setSelectedExistingVehicleId('')
  }

  const addNewVehicleToCreate = () => {
    const plate = newVehiclePlate.trim().toUpperCase()
    if (!plate) return
    if (createVehicles.some((v) => v.licensePlate.toUpperCase() === plate)) return
    setCreateVehicles([...createVehicles, { licensePlate: plate, isAllowed: true }])
    setNewVehiclePlate('')
  }

  const updateCreateVehiclePlate = (index: number, value: string) => {
    setCreateVehicles(createVehicles.map((vehicle, i) => (i === index ? { ...vehicle, licensePlate: value } : vehicle)))
  }

  const removeCreateVehicle = (index: number) => {
    setCreateVehicles(createVehicles.filter((_, i) => i !== index))
  }

  const filteredClients = useMemo(() => {
    const trimmed = query.trim()
    if (!trimmed) return clients

    const mode = detectMode(trimmed)
    if (mode === 'phone') {
      const qDigits = trimmed.replace(/\D/g, '')
      return clients.filter((c) => c.phoneNumber.replace(/\D/g, '').includes(qDigits))
    }

    if (mode === 'plate') {
      const upper = trimmed.toUpperCase()
      return clients.filter((c) => {
        const list = vehiclesByClient.get(c.id) ?? []
        return list.some((v) => v.licensePlate.toUpperCase().includes(upper))
      })
    }

    const lower = trimmed.toLowerCase()
    return clients.filter((c) => c.fullName.toLowerCase().includes(lower))
  }, [clients, query, vehiclesByClient])

  return (
    <div className="space-y-4">
      <PageHeader
        icon={<Users size={24} />}
        title={t('clients.title')}
        description={t('clients.description')}
        actions={
          canWrite ? (
            <Button size="sm" onClick={() => setShowCreateForm((v) => !v)}>
              <Plus size={16} className="mr-1" />
              {t('clients.add_client')}
            </Button>
          ) : null
        }
      />

      {showCreateForm && canWrite && (
        <Card>
          <CardContent className="pt-6">
            <div className="grid gap-3 sm:grid-cols-3">
              <div className="space-y-1">
                <Label>{t('clients.full_name')}</Label>
                <Input
                  value={createForm.fullName}
                  onChange={(e) => setCreateForm((prev) => ({ ...prev, fullName: e.target.value }))}
                />
              </div>
              <div className="space-y-1">
                <Label>{t('clients.phone_number')}</Label>
                <Input
                  value={createForm.phoneNumber}
                  onChange={(e) => setCreateForm((prev) => ({ ...prev, phoneNumber: e.target.value }))}
                  placeholder={t('clients.phone_placeholder')}
                />
              </div>
              <div className="space-y-1">
                <Label>{t('clients.email')}</Label>
                <Input
                  value={createForm.email}
                  onChange={(e) => setCreateForm((prev) => ({ ...prev, email: e.target.value }))}
                  placeholder="name@example.com"
                />
              </div>
            </div>
            <div className="mt-4 space-y-3">
              <div className="flex items-center gap-1 text-sm font-medium">
                <Car size={16} />
                {t('clients.vehicles')}
              </div>
              <div className="flex flex-col gap-2 sm:flex-row">
                <Input
                  value={newVehiclePlate}
                  onChange={(e) => setNewVehiclePlate(e.target.value)}
                  placeholder={t('gate.license_plate')}
                />
                <Button type="button" variant="outline" onClick={addNewVehicleToCreate}>
                  {t('common.add')}
                </Button>
              </div>

              <div className="flex flex-col gap-2 sm:flex-row">
                <select
                  className="h-10 rounded-md border border-input bg-background px-3 py-2 text-sm"
                  value={selectedExistingVehicleId}
                  onChange={(e) => setSelectedExistingVehicleId(e.target.value ? Number(e.target.value) : '')}
                >
                  <option value="">{t('common.search')}</option>
                  {availableCreateVehicles.map((vehicle) => (
                    <option key={vehicle.id} value={vehicle.id}>
                      {vehicle.licensePlate} (#{vehicle.clientId})
                    </option>
                  ))}
                </select>
                <Button type="button" variant="outline" onClick={addExistingVehicleToCreate} disabled={!selectedExistingVehicleId}>
                  {t('common.add')}
                </Button>
              </div>

              {createVehicles.length === 0 ? (
                <p className="text-sm text-muted-foreground">{t('common.no_data')}</p>
              ) : (
                <div className="space-y-2">
                  {createVehicles.map((vehicle, index) => (
                    <div key={`${vehicle.id ?? 'new'}-${index}`} className="flex gap-2">
                      <Input
                        value={vehicle.licensePlate}
                        onChange={(e) => updateCreateVehiclePlate(index, e.target.value)}
                        placeholder={t('gate.license_plate')}
                      />
                      <Button type="button" variant="ghost" onClick={() => removeCreateVehicle(index)}>
                        {t('common.delete')}
                      </Button>
                    </div>
                  ))}
                </div>
              )}
            </div>
            {createError && <p className="mt-3 text-sm text-destructive">{createError}</p>}
            <div className="mt-4 flex gap-2">
              <Button
                size="sm"
                disabled={createMutation.isPending || !createForm.fullName.trim() || !createForm.phoneNumber.trim()}
                onClick={() => createMutation.mutate({ form: createForm, vehiclesForm: createVehicles })}
              >
                {createMutation.isPending && <Loader2 size={14} className="mr-1 animate-spin" />}
                {t('common.save')}
              </Button>
              <Button
                size="sm"
                variant="ghost"
                onClick={() => {
                  setShowCreateForm(false)
                  setCreateError(null)
                  setCreateVehicles([])
                  setNewVehiclePlate('')
                  setSelectedExistingVehicleId('')
                }}
              >
                {t('common.cancel')}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}

      <div className="relative">
        <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-slate-400" />
        <Input
          className="pl-8"
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder={t('subscriptions.search_placeholder')}
        />
      </div>

      <Card>
        <CardContent className="pt-6">
          {clientsLoading || vehiclesLoading ? (
            <div className="flex items-center gap-2 text-sm text-muted-foreground">
              <Loader2 size={14} className="animate-spin" /> {t('common.loading')}
            </div>
          ) : filteredClients.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t('common.no_data')}</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-slate-500">
                    <th className="pb-2 pr-4 font-medium">{t('clients.full_name')}</th>
                    <th className="pb-2 pr-4 font-medium">{t('clients.email')}</th>
                    <th className="pb-2 pr-4 font-medium">{t('clients.phone_number')}</th>
                    <th className="pb-2 pr-4 font-medium">{t('clients.vehicles')}</th>
                    <th className="pb-2 pr-4 font-medium">{t('clients.registered')}</th>
                    {canWrite && <th className="pb-2 font-medium">{t('clients.actions')}</th>}
                  </tr>
                </thead>
                <tbody>
                  {filteredClients.map((client, index) => {
                    const clientVehicles = vehiclesByClient.get(client.id) ?? []
                    const openUp = index >= Math.max(0, filteredClients.length - 2)
                    return (
                      <tr key={client.id} className="border-b last:border-0 align-top">
                        <td className="py-2 pr-4 font-medium">{client.fullName}</td>
                        <td className="py-2 pr-4 text-slate-600">{client.email}</td>
                        <td className="py-2 pr-4">{client.phoneNumber}</td>
                        <td className="py-2 pr-4">
                          {clientVehicles.length === 0
                            ? '—'
                            : clientVehicles.map((v) => v.licensePlate).join(', ')}
                        </td>
                        <td className="py-2 pr-4 text-slate-500">{formatDate(client.registeredAt)}</td>
                        {canWrite && (
                          <td className="py-2">
                            <div className="relative" data-client-menu>
                              <button
                                type="button"
                                className="rounded px-2 py-1 text-slate-500 hover:bg-slate-100 hover:text-slate-700"
                                onClick={() => setOpenMenuClientId((prev) => (prev === client.id ? null : client.id))}
                                aria-label={t('clients.actions')}
                              >
                                ⋯
                              </button>
                              {openMenuClientId === client.id && (
                                <div
                                  className={`absolute right-0 z-20 min-w-[140px] rounded-md border bg-white shadow ${
                                    openUp ? 'bottom-full mb-1' : 'top-full mt-1'
                                  }`}
                                >
                                  <button
                                    type="button"
                                    className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm hover:bg-slate-50"
                                    onClick={() => {
                                      setOpenMenuClientId(null)
                                      navigate(`/clients/${client.id}/edit`)
                                    }}
                                  >
                                    <Pencil size={14} />
                                    {t('common.edit')}
                                  </button>
                                  <button
                                    type="button"
                                    className="flex w-full items-center gap-2 px-3 py-2 text-left text-sm text-red-600 hover:bg-red-50"
                                    onClick={() => {
                                      setOpenMenuClientId(null)
                                      deleteMutation.mutate(client.id)
                                    }}
                                    disabled={deleteMutation.isPending}
                                  >
                                    <Trash2 size={14} />
                                    {t('common.delete')}
                                  </button>
                                </div>
                              )}
                            </div>
                          </td>
                        )}
                      </tr>
                    )
                  })}
                </tbody>
              </table>
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
