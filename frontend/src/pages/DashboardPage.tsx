import { useQuery } from '@tanstack/react-query'
import { LayoutDashboard, Car, Users, CheckCircle2, AlertCircle, RefreshCw } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/store/authStore'
import { getAvailableCount, getAllSpots } from '@/api/management'
import { getClients } from '@/api/clients'
import { getLogs } from '@/api/reporting'

export default function DashboardPage() {
  const { username, role } = useAuthStore()

  const { data: countData, refetch: refetchCount } = useQuery({
    queryKey: ['spots-count'],
    queryFn: getAvailableCount,
  })

  const { data: spots = [], refetch: refetchSpots } = useQuery({
    queryKey: ['spots', '', ''],
    queryFn: getAllSpots,
  })

  const { data: clients = [], refetch: refetchClients } = useQuery({
    queryKey: ['clients'],
    queryFn: getClients,
  })

  const { data: recentLogs = [], refetch: refetchLogs } = useQuery({
    queryKey: ['logs', 'ERROR', '', '10'],
    queryFn: () => getLogs({ level: 'ERROR', limit: 5 }),
  })

  const handleRefresh = () => {
    refetchCount(); refetchSpots(); refetchClients(); refetchLogs()
  }

  const occupiedCount = spots.filter(s => s.status === 'OCCUPIED').length
  const totalCount = spots.length
  const occupancyPct = totalCount > 0 ? Math.round((occupiedCount / totalCount) * 100) : 0

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <LayoutDashboard size={24} className="text-primary" />
          <div>
            <h1 className="text-2xl font-bold text-slate-800">Dashboard</h1>
            <p className="text-sm text-slate-500">Welcome back, <span className="font-medium">{username}</span> · {role}</p>
          </div>
        </div>
        <Button variant="ghost" size="sm" onClick={handleRefresh}>
          <RefreshCw size={16} className="mr-1" /> Refresh
        </Button>
      </div>

      {/* KPI Cards */}
      <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
        <Card>
          <CardContent className="pt-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-slate-500 uppercase tracking-wide">Available</p>
                <p className="text-3xl font-bold text-green-600">{countData?.count ?? '—'}</p>
              </div>
              <CheckCircle2 size={32} className="text-green-200" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-slate-500 uppercase tracking-wide">Occupied</p>
                <p className="text-3xl font-bold text-red-500">{occupiedCount}</p>
              </div>
              <Car size={32} className="text-red-200" />
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-slate-500 uppercase tracking-wide">Occupancy</p>
                <p className="text-3xl font-bold text-slate-700">{occupancyPct}%</p>
              </div>
              <div className="w-10 h-10 rounded-full border-4 border-slate-200 flex items-center justify-center">
                <span className="text-xs font-bold text-slate-600">{occupancyPct}</span>
              </div>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <div className="flex items-center justify-between">
              <div>
                <p className="text-xs text-slate-500 uppercase tracking-wide">Clients</p>
                <p className="text-3xl font-bold text-blue-600">{clients.length}</p>
              </div>
              <Users size={32} className="text-blue-200" />
            </div>
          </CardContent>
        </Card>
      </div>

      {/* Occupancy bar */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Occupancy Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex items-center gap-4">
            <div className="flex-1 bg-slate-100 rounded-full h-4 overflow-hidden">
              <div
                className="h-4 rounded-full bg-gradient-to-r from-green-400 to-red-400 transition-all"
                style={{ width: `${occupancyPct}%` }}
              />
            </div>
            <span className="text-sm font-semibold text-slate-700 w-12 text-right">{occupancyPct}%</span>
          </div>
          <div className="flex justify-between text-xs text-slate-500 mt-2">
            <span>Available: {countData?.count ?? 0}</span>
            <span>Occupied: {occupiedCount}</span>
            <span>Total: {totalCount}</span>
          </div>
        </CardContent>
      </Card>

      {/* Recent Errors */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <AlertCircle size={16} className="text-red-500" /> Recent Errors
          </CardTitle>
        </CardHeader>
        <CardContent>
          {recentLogs.length === 0 ? (
            <p className="text-sm text-green-600 flex items-center gap-1">
              <CheckCircle2 size={14} /> No recent errors
            </p>
          ) : (
            <div className="space-y-2">
              {recentLogs.map((log) => (
                <div key={log.id} className="rounded-md bg-red-50 border border-red-200 px-3 py-2 text-xs">
                  <div className="flex justify-between text-slate-500 mb-0.5">
                    <span className="font-mono">{log.service}</span>
                    <span>{new Date(log.timestamp).toLocaleTimeString()}</span>
                  </div>
                  <p className="text-slate-700">{log.message}</p>
                </div>
              ))}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
