import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { BarChart3, Loader2, RefreshCw, Filter } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle, CardDescription } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { getLogs } from '@/api/reporting'
import type { LogEntry } from '@/api/reporting'

const LEVEL_COLORS: Record<LogEntry['level'], string> = {
  INFO: 'bg-blue-100 text-blue-700',
  WARN: 'bg-yellow-100 text-yellow-700',
  ERROR: 'bg-red-100 text-red-700',
  DEBUG: 'bg-slate-100 text-slate-600',
  TRACE: 'bg-purple-100 text-purple-600',
}

export default function ReportingPage() {
  const [filterLevel, setFilterLevel] = useState('')
  const [filterService, setFilterService] = useState('')
  const [filterLimit, setFilterLimit] = useState('50')

  const { data: logs = [], isLoading, refetch, isFetching } = useQuery({
    queryKey: ['logs', filterLevel, filterService, filterLimit],
    queryFn: () => getLogs({
      level: filterLevel || undefined,
      service: filterService || undefined,
      limit: Number(filterLimit) || 50,
    }),
  })

  const levels: LogEntry['level'][] = ['INFO', 'WARN', 'ERROR', 'DEBUG', 'TRACE']
  const errorCount = logs.filter(l => l.level === 'ERROR').length
  const warnCount = logs.filter(l => l.level === 'WARN').length

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div className="flex items-center gap-2">
          <BarChart3 size={24} className="text-primary" />
          <h1 className="text-2xl font-bold text-slate-800">Reports & Logs</h1>
        </div>
        <Button variant="ghost" size="sm" onClick={() => refetch()} disabled={isFetching}>
          <RefreshCw size={16} className={`mr-1 ${isFetching ? 'animate-spin' : ''}`} /> Refresh
        </Button>
      </div>

      {/* Summary */}
      <div className="grid grid-cols-3 gap-4">
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Total Logs</p>
            <p className="text-3xl font-bold text-slate-700">{logs.length}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Errors</p>
            <p className="text-3xl font-bold text-red-600">{errorCount}</p>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="pt-4">
            <p className="text-xs text-slate-500 uppercase tracking-wide">Warnings</p>
            <p className="text-3xl font-bold text-yellow-500">{warnCount}</p>
          </CardContent>
        </Card>
      </div>

      {/* Filters */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2"><Filter size={16} /> Filters</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="flex flex-wrap gap-3 items-center">
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-500">Level:</span>
              <select className="border rounded px-2 py-1 text-sm" value={filterLevel}
                onChange={(e) => setFilterLevel(e.target.value)}>
                <option value="">All</option>
                {levels.map(l => <option key={l} value={l}>{l}</option>)}
              </select>
            </div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-500">Service:</span>
              <Input placeholder="e.g. client-service" className="w-40 h-8 text-sm"
                value={filterService} onChange={(e) => setFilterService(e.target.value)} />
            </div>
            <div className="flex items-center gap-2">
              <span className="text-sm text-slate-500">Limit:</span>
              <select className="border rounded px-2 py-1 text-sm" value={filterLimit}
                onChange={(e) => setFilterLimit(e.target.value)}>
                <option value="25">25</option>
                <option value="50">50</option>
                <option value="100">100</option>
                <option value="200">200</option>
              </select>
            </div>
            {(filterLevel || filterService) && (
              <Button variant="ghost" size="sm" onClick={() => { setFilterLevel(''); setFilterService('') }}>Clear</Button>
            )}
          </div>
        </CardContent>
      </Card>

      {/* Log table */}
      <Card>
        <CardHeader>
          <CardTitle className="text-base">Log Entries</CardTitle>
          <CardDescription>Recent system events from all services</CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center gap-2 text-muted-foreground text-sm py-4">
              <Loader2 size={16} className="animate-spin" /> Loading…
            </div>
          ) : logs.length === 0 ? (
            <p className="text-sm text-muted-foreground py-4">No logs found.</p>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b text-left text-slate-500">
                    <th className="pb-2 pr-3 font-medium">Time</th>
                    <th className="pb-2 pr-3 font-medium">Level</th>
                    <th className="pb-2 pr-3 font-medium">Service</th>
                    <th className="pb-2 font-medium">Message</th>
                  </tr>
                </thead>
                <tbody>
                  {logs.map((log) => (
                    <tr key={log.id} className="border-b last:border-0 hover:bg-slate-50">
                      <td className="py-1.5 pr-3 text-slate-400 text-xs whitespace-nowrap">
                        {new Date(log.timestamp).toLocaleString()}
                      </td>
                      <td className="py-1.5 pr-3">
                        <span className={`text-xs px-2 py-0.5 rounded-full font-medium ${LEVEL_COLORS[log.level]}`}>
                          {log.level}
                        </span>
                      </td>
                      <td className="py-1.5 pr-3 text-slate-600 text-xs font-mono">{log.service}</td>
                      <td className="py-1.5 text-slate-700 max-w-md truncate">{log.message}</td>
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
