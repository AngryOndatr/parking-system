export type UserRole = 'ADMIN' | 'MANAGER' | 'OPERATOR'

export interface JwtPayload {
  sub: string
  role: UserRole
  exp: number
  iat: number
}

export interface AuthState {
  token: string | null
  role: UserRole | null
  username: string | null
  isAuthenticated: boolean
}

