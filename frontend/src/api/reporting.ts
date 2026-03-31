import apiClient from '@/lib/apiClient'

export interface LogEntry {
  id: number
  timestamp: string
  level: 'INFO' | 'WARN' | 'ERROR' | 'DEBUG' | 'TRACE'
  service: string
  userId?: number
  message: string
  meta?: Record<string, unknown>
}

export interface AuditEntry {
  id: number
  timestamp: string
  level: string
  service: string
  action: string | null
  entityType: string | null
  entityId: number | null
  clientId: number | null
  licensePlate: string | null
  message: string
  meta?: Record<string, unknown>
}

export interface LogsQuery {
  level?: string
  service?: string
  userId?: number
  fromDate?: string
  toDate?: string
  limit?: number
}

export interface AuditQuery {
  service?: string
  from?: string
  to?: string
  limit?: number
}

export async function getLogs(params?: LogsQuery): Promise<LogEntry[]> {
  const res = await apiClient.get<LogEntry[]>('/reporting/logs', { params })
  return res.data
}

export async function getAuditLogs(params?: AuditQuery): Promise<AuditEntry[]> {
  const res = await apiClient.get<AuditEntry[]>('/reporting/audit', { params })
  return res.data
}

export async function getClientHistory(
  clientId: number,
  params?: AuditQuery
): Promise<AuditEntry[]> {
  const res = await apiClient.get<AuditEntry[]>(`/reporting/audit/client/${clientId}`, { params })
  return res.data
}

export async function getVehicleHistory(
  licensePlate: string,
  params?: AuditQuery
): Promise<AuditEntry[]> {
  const res = await apiClient.get<AuditEntry[]>(
    `/reporting/audit/vehicle/${encodeURIComponent(licensePlate.toUpperCase())}`,
    { params }
  )
  return res.data
}
