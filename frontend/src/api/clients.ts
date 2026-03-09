import apiClient from '@/lib/apiClient'

export interface Client {
  id: number
  fullName: string
  phoneNumber: string
  email: string
  registeredAt: string
}

export interface CreateClientRequest {
  fullName: string
  phoneNumber: string
  email: string
}

export interface Vehicle {
  id: number
  clientId: number
  licensePlate: string
  vehicleType: string
  model?: string
  color?: string
  registeredAt: string
}

export async function getClients(): Promise<Client[]> {
  const res = await apiClient.get<Client[]>('/clients')
  return res.data
}

export async function getClientById(id: number): Promise<Client> {
  const res = await apiClient.get<Client>(`/clients/${id}`)
  return res.data
}

export async function createClient(data: CreateClientRequest): Promise<Client> {
  const res = await apiClient.post<Client>('/clients', data)
  return res.data
}

export async function deleteClient(id: number): Promise<void> {
  await apiClient.delete(`/clients/${id}`)
}

export async function searchClientByPhone(phone: string): Promise<Client> {
  const res = await apiClient.get<Client>('/clients/search', { params: { phone } })
  return res.data
}

export async function getVehicles(): Promise<Vehicle[]> {
  const res = await apiClient.get<Vehicle[]>('/vehicles')
  return res.data
}

export async function checkSubscription(licensePlate: string): Promise<{ isAccessGranted: boolean; licensePlate: string; subscriptionId: number | null; message: string }> {
  const res = await apiClient.get('/v1/clients/subscriptions/check', { params: { licensePlate } })
  return res.data
}

