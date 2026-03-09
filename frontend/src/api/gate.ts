import apiClient from '@/lib/apiClient'

export interface GateEntryRequest {
  licensePlate: string
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
  licensePlate: string
  ticketCode?: string
}

export interface GateExitResponse {
  licensePlate: string
  gateStatus: 'OPENED' | 'DENIED'
  paymentRequired: boolean
  amountDue?: number
  message: string
}

export async function gateEntry(data: GateEntryRequest): Promise<GateEntryResponse> {
  const res = await apiClient.post<GateEntryResponse>('/v1/gate/entry', data)
  return res.data
}

export async function gateExit(data: GateExitRequest): Promise<GateExitResponse> {
  const res = await apiClient.post<GateExitResponse>('/v1/gate/exit', data)
  return res.data
}

