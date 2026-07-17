import { useMemo, useState } from 'react'
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query'
import { ArrowLeft, Car, Loader2, Save, User } from 'lucide-react'
import { useNavigate, useParams } from 'react-router-dom'
import { PageHeader } from '@/components/PageHeader'
import { Button } from '@/components/ui/button'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import {
  addVehicleToClient,
  deleteVehicle,
  getClientById,
  getVehicles,
  updateClient,
  updateVehicle,
} from '@/api/clients'
import { useLanguage } from '@/store/languageContext'

interface EditableVehicle {
  id?: number
  licensePlate: string
  isAllowed?: boolean
}

export default function ClientEditPage() {
  const qc = useQueryClient()
  const navigate = useNavigate()
  const { id } = useParams()
  const clientId = Number(id)
  const { t } = useLanguage()

  const [clientDraft, setClientDraft] = useState<{ fullName: string; phoneNumber: string; email: string } | null>(null)
  const [vehiclesDraft, setVehiclesDraft] = useState<EditableVehicle[] | null>(null)
  const [newVehiclePlate, setNewVehiclePlate] = useState('')
  const [selectedExistingVehicleId, setSelectedExistingVehicleId] = useState<number | ''>('')
  const [error, setError] = useState<string | null>(null)

  const { data: client, isLoading: clientLoading } = useQuery({
    queryKey: ['client', clientId],
    queryFn: () => getClientById(clientId),
    enabled: Number.isFinite(clientId),
  })

  const { data: allVehicles = [], isLoading: vehiclesLoading } = useQuery({
    queryKey: ['vehicles'],
    queryFn: getVehicles,
  })

  const clientForm = useMemo(() => {
    if (clientDraft) return clientDraft
    return {
      fullName: client?.fullName ?? '',
      phoneNumber: client?.phoneNumber ?? '',
      email: client?.email ?? '',
    }
  }, [client, clientDraft])

  const initialLinkedVehicles = useMemo(
    () => allVehicles.filter((v) => v.clientId === clientId).map((v) => ({ id: v.id, licensePlate: v.licensePlate, isAllowed: v.isAllowed })),
    [allVehicles, clientId]
  )
  const initialVehicleIds = useMemo(() => initialLinkedVehicles.map((v) => v.id as number), [initialLinkedVehicles])
  const vehiclesForm = vehiclesDraft ?? initialLinkedVehicles

  const availableVehicles = useMemo(() => {
    const selectedIds = new Set(vehiclesForm.map((v) => v.id).filter(Boolean) as number[])
    return allVehicles.filter((v) => !selectedIds.has(v.id))
  }, [allVehicles, vehiclesForm])

  const saveMutation = useMutation({
    mutationFn: async () => {
      if (!clientId) throw new Error('Invalid client id')

      await updateClient(clientId, {
        fullName: clientForm.fullName.trim(),
        phoneNumber: clientForm.phoneNumber.trim(),
        email: clientForm.email.trim(),
      })

      const currentById = new Map(allVehicles.map((v) => [v.id, v]))
      const currentIds = new Set(vehiclesForm.map((v) => v.id).filter(Boolean) as number[])
      const removedIds = initialVehicleIds.filter((vid) => !currentIds.has(vid))

      // Remove vehicles that were previously linked but removed in the form.
      for (const vehicleId of removedIds) {
        await deleteVehicle(vehicleId)
      }

      // Update or link existing vehicles.
      for (const vehicle of vehiclesForm) {
        if (!vehicle.id) continue
        const source = currentById.get(vehicle.id)
        await updateVehicle(vehicle.id, {
          licensePlate: vehicle.licensePlate.trim().toUpperCase(),
          clientId,
          isAllowed: source?.isAllowed ?? vehicle.isAllowed ?? true,
        })
      }

      // Add newly created vehicle rows.
      const newVehicles = vehiclesForm.filter((v) => !v.id)
      for (const vehicle of newVehicles) {
        await addVehicleToClient(clientId, {
          licensePlate: vehicle.licensePlate.trim().toUpperCase(),
        })
      }
    },
    onSuccess: async () => {
      await qc.invalidateQueries({ queryKey: ['clients'] })
      await qc.invalidateQueries({ queryKey: ['vehicles'] })
      await qc.invalidateQueries({ queryKey: ['client', clientId] })
      navigate('/clients')
    },
    onError: (e: { response?: { data?: { message?: string } }; message?: string }) => {
      setError(e.response?.data?.message ?? e.message ?? t('common.error'))
    },
  })

  const addExistingVehicle = () => {
    if (!selectedExistingVehicleId) return
    const selected = allVehicles.find((v) => v.id === selectedExistingVehicleId)
    if (!selected) return
    setVehiclesDraft([...vehiclesForm, { id: selected.id, licensePlate: selected.licensePlate, isAllowed: selected.isAllowed }])
    setSelectedExistingVehicleId('')
  }

  const addNewVehicle = () => {
    const plate = newVehiclePlate.trim().toUpperCase()
    if (!plate) return
    if (vehiclesForm.some((v) => v.licensePlate.toUpperCase() === plate)) return
    setVehiclesDraft([...vehiclesForm, { licensePlate: plate, isAllowed: true }])
    setNewVehiclePlate('')
  }

  const updateVehiclePlate = (index: number, value: string) => {
    setVehiclesDraft(vehiclesForm.map((v, i) => (i === index ? { ...v, licensePlate: value } : v)))
  }

  const removeVehicleAt = (index: number) => {
    setVehiclesDraft(vehiclesForm.filter((_, i) => i !== index))
  }

  if (clientLoading || vehiclesLoading) {
    return (
      <div className="flex items-center gap-2 text-sm text-muted-foreground">
        <Loader2 size={14} className="animate-spin" /> {t('common.loading')}
      </div>
    )
  }

  if (!client) {
    return <p className="text-sm text-muted-foreground">{t('common.no_data')}</p>
  }

  return (
    <div className="space-y-4">
      <PageHeader
        icon={<User size={24} />}
        title={`${t('common.edit')}: ${client.fullName}`}
        actions={
          <Button variant="ghost" onClick={() => navigate('/clients')}>
            <ArrowLeft size={14} className="mr-1" /> {t('common.back')}
          </Button>
        }
      />

      <Card>
        <CardHeader>
          <CardTitle className="text-base">{t('clients.full_name')}</CardTitle>
        </CardHeader>
        <CardContent className="grid gap-3 sm:grid-cols-3">
          <div className="space-y-1">
            <Label>{t('clients.full_name')}</Label>
            <Input
              value={clientForm.fullName}
              onChange={(e) => setClientDraft({ ...clientForm, fullName: e.target.value })}
            />
          </div>
          <div className="space-y-1">
            <Label>{t('clients.phone_number')}</Label>
            <Input
              value={clientForm.phoneNumber}
              onChange={(e) => setClientDraft({ ...clientForm, phoneNumber: e.target.value })}
            />
          </div>
          <div className="space-y-1">
            <Label>{t('clients.email')}</Label>
            <Input
              value={clientForm.email}
              onChange={(e) => setClientDraft({ ...clientForm, email: e.target.value })}
            />
          </div>
        </CardContent>
      </Card>

      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-1">
            <Car size={16} />
            {t('clients.vehicles')}
          </CardTitle>
        </CardHeader>
        <CardContent className="space-y-3">
          <div className="flex flex-col gap-2 sm:flex-row">
            <Input
              value={newVehiclePlate}
              onChange={(e) => setNewVehiclePlate(e.target.value)}
              placeholder={t('gate.license_plate')}
            />
            <Button type="button" variant="outline" onClick={addNewVehicle}>
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
              {availableVehicles.map((vehicle) => (
                <option key={vehicle.id} value={vehicle.id}>
                  {vehicle.licensePlate} (#{vehicle.clientId})
                </option>
              ))}
            </select>
            <Button type="button" variant="outline" onClick={addExistingVehicle} disabled={!selectedExistingVehicleId}>
              {t('common.add')}
            </Button>
          </div>

          {vehiclesForm.length === 0 ? (
            <p className="text-sm text-muted-foreground">{t('common.no_data')}</p>
          ) : (
            <div className="space-y-2">
              {vehiclesForm.map((vehicle, index) => (
                <div key={`${vehicle.id ?? 'new'}-${index}`} className="flex gap-2">
                  <Input
                    value={vehicle.licensePlate}
                    onChange={(e) => updateVehiclePlate(index, e.target.value)}
                    placeholder={t('gate.license_plate')}
                  />
                  <Button type="button" variant="ghost" onClick={() => removeVehicleAt(index)}>
                    {t('common.delete')}
                  </Button>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>

      {error && <p className="text-sm text-destructive">{error}</p>}

      <div className="flex gap-2">
        <Button
          onClick={() => saveMutation.mutate()}
          disabled={saveMutation.isPending || !clientForm.fullName.trim() || !clientForm.phoneNumber.trim()}
        >
          {saveMutation.isPending ? <Loader2 size={14} className="mr-1 animate-spin" /> : <Save size={14} className="mr-1" />}
          {t('common.save')}
        </Button>
        <Button variant="ghost" onClick={() => navigate('/clients')}>
          {t('common.cancel')}
        </Button>
      </div>
    </div>
  )
}
