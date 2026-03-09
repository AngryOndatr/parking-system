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

export interface LogsQuery {
  level?: string
  service?: string
  userId?: number
  fromDate?: string
  toDate?: string
  limit?: number
}

export async function getLogs(params?: LogsQuery): Promise<LogEntry[]> {
  const res = await apiClient.get<LogEntry[]>('/reporting/logs', { params })
  return res.data
}

