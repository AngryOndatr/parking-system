import apiClient from '@/lib/apiClient'

export interface ParkingSpace {
  spaceId: number
  lotId: number
  spaceNumber: string
  level: number
  section?: string
  type: string
  status: string
  hasCharger: boolean
  chargerType?: string
}

export async function getAvailableSpaces(): Promise<ParkingSpace[]> {
  const res = await apiClient.get<ParkingSpace[]>('/management/spots/available')
  return res.data
}

