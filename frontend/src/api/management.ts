import apiClient from '@/lib/apiClient'

export interface ParkingSpot {
  id: number
  lotId: number
  spaceNumber: string
  spaceType: 'STANDARD' | 'VIP' | 'DISABLED' | 'ELECTRIC_CHARGING'
  status: 'AVAILABLE' | 'OCCUPIED' | 'RESERVED' | 'OUT_OF_SERVICE'
  level: number
}

export interface AvailableCountResponse {
  count: number
}

export async function getAllSpots(): Promise<ParkingSpot[]> {
  const res = await apiClient.get<ParkingSpot[]>('/management/spots')
  return res.data
}

export async function getAvailableSpots(): Promise<ParkingSpot[]> {
  const res = await apiClient.get<ParkingSpot[]>('/management/spots/available')
  return res.data
}

export async function getAvailableCount(): Promise<AvailableCountResponse> {
  const res = await apiClient.get<AvailableCountResponse>('/management/spots/available/count')
  return res.data
}

export async function searchSpots(params: { type?: string; status?: string }): Promise<ParkingSpot[]> {
  const res = await apiClient.get<ParkingSpot[]>('/management/spots/search', { params })
  return res.data
}

