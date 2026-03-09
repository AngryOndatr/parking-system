import { create } from 'zustand'
import { persist } from 'zustand/middleware'
import type { UserRole, JwtPayload } from '@/types/auth'

interface AuthStore {
  token: string | null
  role: UserRole | null
  username: string | null
  isAuthenticated: boolean
  _hasHydrated: boolean
  setToken: (token: string) => void
  logout: () => void
  setHasHydrated: (v: boolean) => void
}

function decodeJwtPayload(token: string): JwtPayload | null {
  try {
    const parts = token.split('.')
    if (parts.length !== 3) return null
    const payload = atob(parts[1].replace(/-/g, '+').replace(/_/g, '/'))
    return JSON.parse(payload) as JwtPayload
  } catch {
    return null
  }
}

export function isTokenExpired(token: string): boolean {
  const payload = decodeJwtPayload(token)
  if (!payload?.exp) return true
  return Date.now() / 1000 > payload.exp
}

export const useAuthStore = create<AuthStore>()(
  persist(
    (set) => ({
      token: null,
      role: null,
      username: null,
      isAuthenticated: false,
      _hasHydrated: false,

      setHasHydrated: (v) => set({ _hasHydrated: v }),

      setToken: (token: string) => {
        const payload = decodeJwtPayload(token)
        if (!payload) return
        set({
          token,
          role: payload.role,
          username: payload.sub,
          isAuthenticated: true,
        })
      },

      logout: () => {
        set({
          token: null,
          role: null,
          username: null,
          isAuthenticated: false,
        })
      },
    }),
    {
      name: 'auth-storage',
      partialize: (state) => ({
        token: state.token,
        role: state.role,
        username: state.username,
        isAuthenticated: state.isAuthenticated,
      }),
      onRehydrateStorage: () => (state) => {
        if (state) {
          // Если токен истёк — сразу сбрасываем аутентификацию
          if (state.token && isTokenExpired(state.token)) {
            state.token = null
            state.role = null
            state.username = null
            state.isAuthenticated = false
          }
          state.setHasHydrated(true)
        }
      },
    }
  )
)

