import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import {
  BarChart3, Loader2, RefreshCw, Filter,
  Shield, Car, User, Search, Clock,
} from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { PageHeader } from '@/components/PageHeader'
import { useLanguage } from '@/store/languageContext'
import { getLogs, getAuditLogs, getClientHistory, getVehicleHistory } from '@/api/reporting'
import type { LogEntry, AuditEntry } from '@/api/reporting'

const LEVEL_COLORS: Record<string, string> = {
  INFO:  'bg-blue-100 text-blue-700',
  WARN:  'bg-yellow-100 text-yellow-700',
  ERROR: 'bg-red-100 text-red-700',
  DEBUG: 'bg-slate-100 text-slate-600',
  TRACE: 'bg-purple-100 text-purple-600',
}

const ACTION_COLORS: Record<string, string> = {
  CLIENT_CREATED:           'bg-emerald-100 text-emerald-700',
  CLIENT_UPDATED:           'bg-blue-100 text-blue-700',
  CLIENT_DELETED:           'bg-red-100 text-red-700',
  VEHICLE_CREATED:          'bg-emerald-100 text-emerald-700',
  VEHICLE_UPDATED:          'bg-blue-100 text-blue-700',
  VEHICLE_DELETED:          'bg-red-100 text-red-700',
  SUBSCRIPTION_CREATED:     'bg-emerald-100 text-emerald-700',
  SUBSCRIPTION_DEACTIVATED: 'bg-orange-100 text-orange-700',
  GATE_ENTRY:               'bg-green-100 text-green-700',
  GATE_EXIT:                'bg-cyan-100 text-cyan-700',
  GATE_EXIT_DENIED:         'bg-red-100 text-red-700',
  GATE_MANUAL_CONTROL:      'bg-purple-100 text-purple-700',
  PAYMENT_PROCESSED:        'bg-teal-100 text-teal-700',
}

function actionColor(action: string | null) {
  if (!action) return 'bg-slate-100 text-slate-500'
  return ACTION_COLORS[action] ?? 'bg-slate-100 text-slate-600'
}

function AuditTable({ entries }: { entries: AuditEntry[] }) {
  const { t } = useLanguage()
  if (entries.length === 0)
    return <p className="text-sm text-muted-foreground py-4">{t('reporting.no_audit_events')}</p>
  return (
    <>
      <div className="space-y-2 lg:hidden">
        {entries.map(e => (
          <div key={e.id} className="rounded-lg border border-slate-200 bg-white p-3 space-y-1">
            <div className="flex items-center gap-2 flex-wrap">
              {e.action && (
                <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${actionColor(e.action)}`}>
                  {e.action}
                </span>
              )}
              {e.entityType && <span className="text-xs text-slate-500 font-mono shrink-0">{e.entityType}</span>}
              {e.licensePlate && <span className="text-xs font-mono bg-slate-100 px-1 rounded">{e.licensePlate}</span>}
              <span className="text-xs text-slate-400 ml-auto shrink-0">{new Date(e.timestamp).toLocaleString()}</span>
            </div>
            <p className="text-sm text-slate-700 line-clamp-2">{e.message}</p>
            <p className="text-xs text-slate-400">{e.service}</p>
          </div>
        ))}
      </div>
      <div className="hidden lg:block overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-slate-500">
              <th className="pb-2 pr-3 font-medium">{t('reporting.col_time')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.col_action')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.col_entity')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.col_plate')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.filter_service')}</th>
              <th className="pb-2 font-medium">{t('reporting.message')}</th>
            </tr>
          </thead>
          <tbody>
            {entries.map(e => (
              <tr key={e.id} className="border-b last:border-0 hover:bg-slate-50">
                <td className="py-1.5 pr-3 text-slate-400 text-xs whitespace-nowrap">{new Date(e.timestamp).toLocaleString()}</td>
                <td className="py-1.5 pr-3">
                  {e.action
                    ? <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${actionColor(e.action)}`}>{e.action}</span>
                    : '—'}
                </td>
                <td className="py-1.5 pr-3 text-xs font-mono text-slate-600">{e.entityType}{e.entityId ? `#${e.entityId}` : ''}</td>
                <td className="py-1.5 pr-3 text-xs font-mono">{e.licensePlate ?? '—'}</td>
                <td className="py-1.5 pr-3 text-xs text-slate-500">{e.service}</td>
                <td className="py-1.5 text-slate-700 max-w-sm truncate">{e.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}

function LogTable({ logs }: { logs: LogEntry[] }) {
  const { t } = useLanguage()
  if (logs.length === 0)
    return <p className="text-sm text-muted-foreground py-4">{t('reporting.no_logs')}</p>
  return (
    <>
      <div className="space-y-2 lg:hidden">
        {logs.map(l => (
          <div key={l.id} className="rounded-lg border border-slate-200 bg-white p-3">
            <div className="flex items-center justify-between gap-2 mb-1">
              <span className={`text-xs px-2 py-0.5 rounded-full font-medium shrink-0 ${LEVEL_COLORS[l.level] ?? ''}`}>{l.level}</span>
              <span className="text-xs text-slate-400 truncate">{l.service}</span>
              <span className="text-xs text-slate-400 shrink-0 ml-auto">{new Date(l.timestamp).toLocaleTimeString()}</span>
            </div>
            <p className="text-sm text-slate-700 line-clamp-2">{l.message}</p>
            <p className="text-xs text-slate-400 mt-1">{new Date(l.timestamp).toLocaleDateString()}</p>
          </div>
        ))}
      </div>
      <div className="hidden lg:block overflow-x-auto">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b text-left text-slate-500">
              <th className="pb-2 pr-3 font-medium">{t('reporting.col_time')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.filter_level')}</th>
              <th className="pb-2 pr-3 font-medium">{t('reporting.filter_service')}</th>
              <th className="pb-2 font-medium">{t('reporting.message')}</th>
            </tr>
          </thead>
          <tbody>
            {logs.map(l => (
              <tr key={l.id} className="border-b last:border-0 hover:bg-slate-50">
                <td className="py-1.5 pr-3 text-slate-400 text-xs whitespace-nowrap">{new Date(l.timestamp).toLocaleString()}</td>
                <td className="py-1.5 pr-3">
                  <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${LEVEL_COLORS[l.level] ?? ''}`}>{l.level}</span>
                </td>
                <td className="py-1.5 pr-3 text-slate-600 text-xs font-mono">{l.service}</td>
                <td className="py-1.5 text-slate-700 max-w-md truncate">{l.message}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  )
}

function DateRange({ from, to, onFrom, onTo }: { from: string; to: string; onFrom: (v: string) => void; onTo: (v: string) => void }) {
  const { t } = useLanguage()
  return (
    <div className="flex items-center gap-2 flex-wrap">
      <Clock size={14} className="text-slate-400 shrink-0" />
      <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_from')}:</span>
      <input type="datetime-local" className="border rounded px-2 py-1 text-sm" value={from} onChange={e => onFrom(e.target.value)} />
      <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_to')}:</span>
      <input type="datetime-local" className="border rounded px-2 py-1 text-sm" value={to} onChange={e => onTo(e.target.value)} />
      {(from || to) && <Button variant="ghost" size="sm" onClick={() => { onFrom(''); onTo('') }}>{t('reporting.filter_clear')}</Button>}
    </div>
  )
}

type Tab = 'general' | 'audit' | 'client' | 'vehicle'

export default function ReportingPage() {
  const { t } = useLanguage()
  const [activeTab, setActiveTab] = useState<Tab>('audit')

  const [filterLevel, setFilterLevel]     = useState('')
  const [filterService, setFilterService] = useState('')
  const [filterLimit, setFilterLimit]     = useState('50')

  const [auditService, setAuditService] = useState('')
  const [auditFrom, setAuditFrom]       = useState('')
  const [auditTo, setAuditTo]           = useState('')
  const [auditLimit, setAuditLimit]     = useState('200')

  const [clientIdInput, setClientIdInput] = useState('')
  const [clientFrom, setClientFrom]       = useState('')
  const [clientTo, setClientTo]           = useState('')
  const [clientSearch, setClientSearch]   = useState(false)

  const [plateInput, setPlateInput]       = useState('')
  const [vehicleFrom, setVehicleFrom]     = useState('')
  const [vehicleTo, setVehicleTo]         = useState('')
  const [vehicleSearch, setVehicleSearch] = useState(false)

  const levels: LogEntry['level'][] = ['INFO', 'WARN', 'ERROR', 'DEBUG', 'TRACE']

  const logsQuery = useQuery({
    queryKey: ['logs', filterLevel, filterService, filterLimit],
    queryFn: () => getLogs({ level: filterLevel || undefined, service: filterService || undefined, limit: Number(filterLimit) || 50 }),
    enabled: activeTab === 'general',
  })

  const auditQuery = useQuery({
    queryKey: ['audit', auditService, auditFrom, auditTo, auditLimit],
    queryFn: () => getAuditLogs({
      service: auditService || undefined,
      from: auditFrom ? new Date(auditFrom).toISOString() : undefined,
      to:   auditTo   ? new Date(auditTo).toISOString()   : undefined,
      limit: Number(auditLimit) || 200,
    }),
    enabled: activeTab === 'audit',
  })

  const clientId = Number(clientIdInput)
  const clientQuery = useQuery({
    queryKey: ['audit-client', clientId, clientFrom, clientTo],
    queryFn: () => getClientHistory(clientId, {
      from: clientFrom ? new Date(clientFrom).toISOString() : undefined,
      to:   clientTo   ? new Date(clientTo).toISOString()   : undefined,
      limit: 500,
    }),
    enabled: activeTab === 'client' && clientSearch && !isNaN(clientId) && clientId > 0,
  })

  const vehicleQuery = useQuery({
    queryKey: ['audit-vehicle', plateInput, vehicleFrom, vehicleTo],
    queryFn: () => getVehicleHistory(plateInput, {
      from: vehicleFrom ? new Date(vehicleFrom).toISOString() : undefined,
      to:   vehicleTo   ? new Date(vehicleTo).toISOString()   : undefined,
      limit: 500,
    }),
    enabled: activeTab === 'vehicle' && vehicleSearch && plateInput.trim().length >= 2,
  })

  const auditEntries = auditQuery.data ?? []
  const actionCounts = auditEntries.reduce<Record<string, number>>((acc, e) => {
    if (e.action) acc[e.action] = (acc[e.action] ?? 0) + 1
    return acc
  }, {})

  function refetchCurrent() {
    if (activeTab === 'general') logsQuery.refetch()
    if (activeTab === 'audit')   auditQuery.refetch()
    if (activeTab === 'client')  clientQuery.refetch()
    if (activeTab === 'vehicle') vehicleQuery.refetch()
  }

  const isFetching = logsQuery.isFetching || auditQuery.isFetching || clientQuery.isFetching || vehicleQuery.isFetching

  const tabs: { id: Tab; label: string; icon: React.ReactNode }[] = [
    { id: 'audit',   label: t('reporting.tab_audit'),   icon: <Shield size={15} /> },
    { id: 'client',  label: t('reporting.tab_client'),  icon: <User size={15} /> },
    { id: 'vehicle', label: t('reporting.tab_vehicle'), icon: <Car size={15} /> },
    { id: 'general', label: t('reporting.tab_general'), icon: <BarChart3 size={15} /> },
  ]

  return (
    <div className="space-y-6">
      <PageHeader
        icon={<Shield size={24} />}
        title={t('reporting.page_title')}
        actions={
          <Button variant="ghost" size="sm" onClick={refetchCurrent} disabled={isFetching}>
            <RefreshCw size={16} className={`mr-1 ${isFetching ? 'animate-spin' : ''}`} /> {t('common.refresh')}
          </Button>
        }
      />

      {/* Tab bar */}
      <div className="flex gap-1 border-b border-slate-200 overflow-x-auto">
        {tabs.map(tab => (
          <button key={tab.id} onClick={() => setActiveTab(tab.id)}
            className={`flex items-center gap-1.5 px-4 py-2 text-sm font-medium border-b-2 whitespace-nowrap transition-colors ${
              activeTab === tab.id ? 'border-blue-600 text-blue-600' : 'border-transparent text-slate-500 hover:text-slate-700'
            }`}>
            {tab.icon} {tab.label}
          </button>
        ))}
      </div>

      {/* Audit Trail */}
      {activeTab === 'audit' && (
        <div className="space-y-4">
          {Object.keys(actionCounts).length > 0 && (
            <div className="flex flex-wrap gap-2">
              {Object.entries(actionCounts).sort((a, b) => b[1] - a[1]).slice(0, 10).map(([action, count]) => (
                <span key={action} className={`text-xs px-2 py-1 rounded-full font-medium ${actionColor(action)}`}>
                  {action}: {count}
                </span>
              ))}
            </div>
          )}
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base flex items-center gap-2"><Filter size={15}/> {t('reporting.filters')}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-3 items-center">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_service')}:</span>
                  <Input placeholder="e.g. gate-control-service" className="w-48 h-8 text-sm"
                    value={auditService} onChange={e => setAuditService(e.target.value)} />
                </div>
                <DateRange from={auditFrom} to={auditTo} onFrom={setAuditFrom} onTo={setAuditTo} />
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_limit')}:</span>
                  <select className="border rounded px-2 py-1 text-sm" value={auditLimit}
                    onChange={e => setAuditLimit(e.target.value)}>
                    {['100','200','500','1000'].map(v => <option key={v} value={v}>{v}</option>)}
                  </select>
                </div>
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">{t('reporting.all_events_title')}</CardTitle>
              <CardDescription>{t('reporting.all_events_desc')}</CardDescription>
            </CardHeader>
            <CardContent>
              {auditQuery.isLoading
                ? <div className="flex items-center gap-2 text-muted-foreground text-sm py-4"><Loader2 size={16} className="animate-spin"/> {t('common.loading')}</div>
                : <AuditTable entries={auditQuery.data ?? []} />}
            </CardContent>
          </Card>
        </div>
      )}

      {/* Client History */}
      {activeTab === 'client' && (
        <div className="space-y-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base flex items-center gap-2"><User size={15}/> {t('reporting.tab_client')}</CardTitle>
              <CardDescription>{t('reporting.client_history_desc')}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-3 items-end">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.client_id_filter')}:</span>
                  <Input type="number" placeholder="e.g. 1" className="w-28 h-8 text-sm"
                    value={clientIdInput}
                    onChange={e => { setClientIdInput(e.target.value); setClientSearch(false) }}
                    onKeyDown={e => { if (e.key === 'Enter') setClientSearch(true) }} />
                </div>
                <DateRange from={clientFrom} to={clientTo} onFrom={setClientFrom} onTo={setClientTo} />
                <Button size="sm" onClick={() => setClientSearch(true)} disabled={!clientIdInput}>
                  <Search size={14} className="mr-1"/> {t('common.search')}
                </Button>
              </div>
            </CardContent>
          </Card>
          {clientSearch && clientId > 0 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">{t('reporting.history_client', { id: clientId })}</CardTitle>
                <CardDescription>
                  {clientQuery.data?.length ?? 0} {t('reporting.logs')}
                  {(clientFrom || clientTo) && ` ${t('reporting.events_in_period')}`}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {clientQuery.isLoading
                  ? <div className="flex items-center gap-2 text-muted-foreground text-sm py-4"><Loader2 size={16} className="animate-spin"/> {t('common.loading')}</div>
                  : <AuditTable entries={clientQuery.data ?? []} />}
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* Vehicle History */}
      {activeTab === 'vehicle' && (
        <div className="space-y-4">
          <Card>
            <CardHeader className="pb-2">
              <CardTitle className="text-base flex items-center gap-2"><Car size={15}/> {t('reporting.tab_vehicle')}</CardTitle>
              <CardDescription>{t('reporting.vehicle_history_desc')}</CardDescription>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-3 items-end">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.col_plate')}:</span>
                  <Input placeholder="e.g. ABC123" className="w-36 h-8 text-sm uppercase"
                    value={plateInput}
                    onChange={e => { setPlateInput(e.target.value.toUpperCase()); setVehicleSearch(false) }}
                    onKeyDown={e => { if (e.key === 'Enter') setVehicleSearch(true) }} />
                </div>
                <DateRange from={vehicleFrom} to={vehicleTo} onFrom={setVehicleFrom} onTo={setVehicleTo} />
                <Button size="sm" onClick={() => setVehicleSearch(true)} disabled={plateInput.trim().length < 2}>
                  <Search size={14} className="mr-1"/> {t('common.search')}
                </Button>
              </div>
            </CardContent>
          </Card>
          {vehicleSearch && plateInput.trim().length >= 2 && (
            <Card>
              <CardHeader>
                <CardTitle className="text-base">{t('reporting.history_vehicle', { plate: plateInput })}</CardTitle>
                <CardDescription>
                  {vehicleQuery.data?.length ?? 0} {t('reporting.logs')}
                  {(vehicleFrom || vehicleTo) && ` ${t('reporting.events_in_period')}`}
                </CardDescription>
              </CardHeader>
              <CardContent>
                {vehicleQuery.isLoading
                  ? <div className="flex items-center gap-2 text-muted-foreground text-sm py-4"><Loader2 size={16} className="animate-spin"/> {t('common.loading')}</div>
                  : <AuditTable entries={vehicleQuery.data ?? []} />}
              </CardContent>
            </Card>
          )}
        </div>
      )}

      {/* System Logs */}
      {activeTab === 'general' && (
        <div className="space-y-4">
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <Card>
              <CardContent className="pt-4">
                <p className="text-xs text-slate-500 uppercase tracking-wide">{t('reporting.total_logs')}</p>
                <p className="text-3xl font-bold text-slate-700">{logsQuery.data?.length ?? 0}</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-4">
                <p className="text-xs text-slate-500 uppercase tracking-wide">{t('reporting.stat_errors')}</p>
                <p className="text-3xl font-bold text-red-600">{(logsQuery.data ?? []).filter(l => l.level === 'ERROR').length}</p>
              </CardContent>
            </Card>
            <Card>
              <CardContent className="pt-4">
                <p className="text-xs text-slate-500 uppercase tracking-wide">{t('reporting.stat_warnings')}</p>
                <p className="text-3xl font-bold text-yellow-500">{(logsQuery.data ?? []).filter(l => l.level === 'WARN').length}</p>
              </CardContent>
            </Card>
          </div>
          <Card>
            <CardHeader>
              <CardTitle className="text-base flex items-center gap-2"><Filter size={16}/> {t('reporting.filters')}</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap gap-3 items-center">
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_level')}:</span>
                  <select className="border rounded px-2 py-1 text-sm" value={filterLevel} onChange={e => setFilterLevel(e.target.value)}>
                    <option value="">{t('reporting.all_levels')}</option>
                    {levels.map(l => <option key={l} value={l}>{l}</option>)}
                  </select>
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_service')}:</span>
                  <Input placeholder="e.g. client-service" className="w-40 h-8 text-sm"
                    value={filterService} onChange={e => setFilterService(e.target.value)} />
                </div>
                <div className="flex items-center gap-2">
                  <span className="text-sm text-slate-500 shrink-0">{t('reporting.filter_limit')}:</span>
                  <select className="border rounded px-2 py-1 text-sm" value={filterLimit} onChange={e => setFilterLimit(e.target.value)}>
                    {['25','50','100','200'].map(v => <option key={v} value={v}>{v}</option>)}
                  </select>
                </div>
                {(filterLevel || filterService) && (
                  <Button variant="ghost" size="sm" onClick={() => { setFilterLevel(''); setFilterService('') }}>{t('reporting.filter_clear')}</Button>
                )}
              </div>
            </CardContent>
          </Card>
          <Card>
            <CardHeader>
              <CardTitle className="text-base">{t('reporting.log_entries_title')}</CardTitle>
              <CardDescription>{t('reporting.log_entries_desc')}</CardDescription>
            </CardHeader>
            <CardContent>
              {logsQuery.isLoading
                ? <div className="flex items-center gap-2 text-muted-foreground text-sm py-4"><Loader2 size={16} className="animate-spin"/> {t('common.loading')}</div>
                : <LogTable logs={logsQuery.data ?? []} />}
            </CardContent>
          </Card>
        </div>
      )}
    </div>
  )
}
