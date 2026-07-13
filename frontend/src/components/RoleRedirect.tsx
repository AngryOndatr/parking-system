import { Navigate } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { UserRole } from '@/types/auth'

function getRoleDefaultRoute(role: UserRole): string {
  switch (role) {
    case 'ADMIN':
      return '/clients'
    case 'MANAGER':
      return '/management'
    case 'OPERATOR':
      return '/gate'
    default:
      return '/gate'
  }
}

export default function RoleRedirect() {
  const { isAuthenticated, role, _hasHydrated } = useAuthStore()

  if (!_hasHydrated) {
    return null
  }

  if (!isAuthenticated || !role) {
    return <Navigate to="/login" replace />
  }

  return <Navigate to={getRoleDefaultRoute(role)} replace />
}
