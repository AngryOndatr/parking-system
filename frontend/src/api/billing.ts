import apiClient from '@/lib/apiClient'

export interface BillingCalculateRequest {
  parkingEventId: number
  entryTime: string
  exitTime: string
  tariffType: 'ONE_TIME' | 'DAILY' | 'NIGHT' | 'VIP'
  isSubscriber: boolean
  subscriptionId?: number | null
}

export interface BillingCalculateResponse {
  parkingEventId: number
  durationMinutes: number
  baseFee: number
  discount: number
  totalFee: number
  tariffApplied: string
  calculatedAt: string
}

export interface BillingPayRequest {
  parkingEventId: number
  amount: number
  paymentMethod: 'CARD' | 'CASH' | 'MOBILE_PAY'
  transactionId?: string | null
  operatorId?: number | null
}

export interface Payment {
  paymentId: number
  amount: number
  status: 'COMPLETED' | 'FAILED' | 'PENDING'
  paymentMethod: string
  transactionId: string | null
  paymentTime: string
}

export interface BillingPayResponse {
  paymentId: number
  parkingEventId: number
  amount: number
  status: string
  paymentMethod: string
  transactionId: string | null
  paymentTime: string
}

export interface BillingStatusResponse {
  parkingEventId: number
  isPaid: boolean
  payments: Payment[]
}

export async function calculateFee(data: BillingCalculateRequest): Promise<BillingCalculateResponse> {
  const res = await apiClient.post<BillingCalculateResponse>('/v1/billing/calculate', data)
  return res.data
}

export async function processPayment(data: BillingPayRequest): Promise<BillingPayResponse> {
  const res = await apiClient.post<BillingPayResponse>('/v1/billing/pay', data)
  return res.data
}

export async function getBillingStatus(parkingEventId: number): Promise<BillingStatusResponse> {
  const res = await apiClient.get<BillingStatusResponse>('/v1/billing/status', { params: { parkingEventId } })
  return res.data
}

export async function getBillingStatusByTicket(ticketCode: string): Promise<BillingStatusResponse> {
  const res = await apiClient.get<BillingStatusResponse>('/v1/billing/status-by-ticket', { params: { ticketCode } })
  return res.data
}

export async function processPaymentTest(data: BillingPayRequest): Promise<BillingPayResponse> {
  const res = await apiClient.post<BillingPayResponse>('/v1/billing/pay-test', data)
  return res.data
}

