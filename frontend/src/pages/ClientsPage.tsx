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

export default function ClientsPage() {
  const qc = useQueryClient()
  const { role } = useAuthStore()
  const [showForm, setShowForm] = useState(false)
  const [searchPhone, setSearchPhone] = useState('')
  const [searchResult, setSearchResult] = useState<Client | null | 'not-found'>(null)
  const [form, setForm] = useState({ fullName: '', phoneNumber: '', email: '' })
  const [formError, setFormError] = useState<string | null>(null)

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

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <Users size={24} className="text-primary" />
          <h1 className="text-2xl font-bold text-slate-800">Clients</h1>
        </div>
        <div className="flex gap-2">
          <Button variant="ghost" size="sm" onClick={() => refetch()}>
            <RefreshCw size={16} className="mr-1" /> Refresh
          </Button>
          {canWrite && (
            <Button size="sm" onClick={() => setShowForm(!showForm)}>
              <Plus size={16} className="mr-1" /> Add Client
            </Button>
          )}
        </div>
      </div>

      {/* Search */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Search by Phone</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex gap-2">
            <Input
              placeholder="+380501234567"
              value={searchPhone}
              onChange={(e) => { setSearchPhone(e.target.value); setSearchResult(null) }}
              className="max-w-xs"
            />
            <Button variant="outline" onClick={handleSearch} disabled={!searchPhone.trim()}>
              <Search size={16} className="mr-1" /> Search
            </Button>
          </div>
          {searchResult === 'not-found' && (
            <p className="mt-2 text-sm text-destructive">Client not found</p>
          )}
          {searchResult && searchResult !== 'not-found' && (
            <div className="mt-3 rounded-md bg-blue-50 border border-blue-200 px-4 py-3 text-sm">
              <p className="font-semibold">{searchResult.fullName}</p>
              <p className="text-slate-600">{searchResult.phoneNumber} · {searchResult.email}</p>
            </div>
          )}
        </CardContent>
      </Card>

      {/* Add form */}
      {showForm && (
        <Card>
          <CardHeader>
            <CardTitle className="text-base">New Client</CardTitle>
            <CardDescription>Fill in client details</CardDescription>
          </CardHeader>
          <CardContent>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-4">
              <div className="space-y-1">
                <Label>Full Name</Label>
                <Input value={form.fullName} onChange={(e) => setForm({ ...form, fullName: e.target.value })} placeholder="John Doe" />
              </div>
              <div className="space-y-1">
                <Label>Phone Number</Label>
                <Input value={form.phoneNumber} onChange={(e) => setForm({ ...form, phoneNumber: e.target.value })} placeholder="+380501234567" />
              </div>
              <div className="space-y-1">
                <Label>Email</Label>
                <Input value={form.email} onChange={(e) => setForm({ ...form, email: e.target.value })} placeholder="john@example.com" />
              </div>
            </div>
            {formError && <p className="mt-2 text-sm text-destructive">{formError}</p>}
            <div className="mt-4 flex gap-2">
              <Button onClick={() => createMutation.mutate(form)} disabled={createMutation.isPending || !form.fullName || !form.phoneNumber}>
                {createMutation.isPending ? <Loader2 size={16} className="animate-spin mr-1" /> : null} Save
              </Button>
              <Button variant="ghost" onClick={() => { setShowForm(false); setFormError(null) }}>Cancel</Button>
            </div>
          </CardContent>
        </Card>
      )}

      {/* Table */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Registered Clients ({clients.length})</CardTitle>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center gap-2 text-muted-foreground text-sm py-4">
              <Loader2 size={16} className="animate-spin" /> Loading…
            </div>
          ) : clients.length === 0 ? (
            <p className="text-sm text-muted-foreground py-4">No clients registered yet.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-slate-500">
                    <th className="pb-2 pr-4 font-medium">ID</th>
                    <th className="pb-2 pr-4 font-medium">Full Name</th>
                    <th className="pb-2 pr-4 font-medium">Phone</th>
                    <th className="pb-2 pr-4 font-medium">Email</th>
                    <th className="pb-2 pr-4 font-medium">Registered</th>
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
                      <td className="py-2 pr-4 text-slate-400">{new Date(c.registeredAt).toLocaleDateString()}</td>
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
          )}
        </CardContent>
      </Card>
    </div>
  )
}
