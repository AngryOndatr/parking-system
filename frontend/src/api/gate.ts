import apiClient from '@/lib/apiClient'

export interface GateEntryRequest {
  licensePlate: string
  entryMethod: 'SCAN' | 'MANUAL'
  gateId: string
}

export interface GateEntryResponse {
  eventId?: number
  licensePlate: string
  isSubscriber: boolean
  gateStatus: 'OPENED' | 'DENIED'
  ticketCode?: string
  message: string
}

export interface GateExitRequest {
  licensePlate?: string
  ticketCode?: string
  exitMethod: 'SCAN' | 'MANUAL' | 'AUTO'
  gateId: string
}

export interface GateExitResponse {
  parkingEventId: number
  licensePlate: string
  entryTime: string
  exitTime: string
  durationMinutes: number
  fee: number
  isPaid: boolean
  paymentRequired: boolean
  gateStatus: 'OPENED' | 'CLOSED' | 'ERROR'
  message: string
}

export async function gateEntry(data: GateEntryRequest): Promise<GateEntryResponse> {
  const res = await apiClient.post<GateEntryResponse>('/gate/entry', data)
  return res.data
}

export async function gateExit(data: GateExitRequest): Promise<GateExitResponse> {
  const res = await apiClient.post<GateExitResponse>('/gate/exit', data)
  return res.data
}
