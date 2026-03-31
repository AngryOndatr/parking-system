import apiClient from '@/lib/apiClient'

export type SubscriptionType = 'MONTHLY' | 'QUARTERLY' | 'ANNUAL' | 'CUSTOM'

export interface Subscription {
  id: number
  clientId: number
  type: string
  startDate: string
  endDate: string
  isActive: boolean
  parkingSpaceId?: number | null
  spaceNumber?: string | null
}

export interface CreateSubscriptionRequest {
  type: SubscriptionType
  startDate: string   // ISO-8601 date-time, e.g. "2026-04-01T00:00:00Z"
  endDate: string
  parkingSpaceId?: number | null
}

export async function getSubscriptionsByClient(clientId: number): Promise<Subscription[]> {
  const res = await apiClient.get<Subscription[]>(`/clients/${clientId}/subscriptions`)
  return res.data
}

export async function createSubscription(
  clientId: number,
  data: CreateSubscriptionRequest
): Promise<Subscription> {
  const res = await apiClient.post<Subscription>(`/clients/${clientId}/subscriptions`, data)
  return res.data
}

export async function deactivateSubscription(id: number): Promise<void> {
  await apiClient.delete(`/clients/subscriptions/${id}`)
}

