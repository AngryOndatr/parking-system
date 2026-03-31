import axios from 'axios'
import { useAuthStore, isTokenExpired } from '@/store/authStore'

// Utility to unwrap JsonNullable and recurse
function unwrapJsonNullables(obj: any): any {
  if (obj === null || typeof obj !== 'object') return obj
  if (Array.isArray(obj)) return obj.map(unwrapJsonNullables)
  if (obj && typeof obj === 'object' && 'present' in obj) {
    return obj.present ? unwrapJsonNullables(obj.value) : null
  }
  const result: any = {}
  for (const key in obj) {
    result[key] = unwrapJsonNullables(obj[key])
  }
  return result
}

const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
})

// Attach JWT token to every request
apiClient.interceptors.request.use((config) => {
  const token = useAuthStore.getState().token
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

// Handle 401/403 — auto logout if token expired
apiClient.interceptors.response.use(
  (response) => {
    // Unwrap JsonNullable in response data
    response.data = unwrapJsonNullables(response.data)
    return response
  },
  (error) => {
    if (error.response?.status === 401 || error.response?.status === 403) {
      const token = useAuthStore.getState().token
      // Логаут только если токен истёк, иначе показываем ошибку пользователю
      if (!token || isTokenExpired(token)) {
        useAuthStore.getState().logout()
        window.location.href = '/login'
      }
    }
    return Promise.reject(error)
  }
)

export default apiClient
