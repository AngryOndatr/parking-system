import { Navigate, Outlet } from 'react-router-dom'
import { useAuthStore } from '@/store/authStore'
import type { UserRole } from '@/types/auth'

interface ProtectedRouteProps {
  allowedRoles?: UserRole[]
}

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

export default function ProtectedRoute({ allowedRoles }: ProtectedRouteProps) {
  const { isAuthenticated, role, _hasHydrated } = useAuthStore()

  // Ждём пока Zustand загрузит данные из localStorage
  if (!_hasHydrated) {
    return null
  }

  // Not authenticated → redirect to login
  if (!isAuthenticated || !role) {
    return <Navigate to="/login" replace />
  }

  // Role check: if allowed roles specified, verify user has one
  if (allowedRoles && allowedRoles.length > 0 && !allowedRoles.includes(role)) {
    // Redirect to user's default route instead of 403 page
    return <Navigate to={getRoleDefaultRoute(role)} replace />
  }

  return <Outlet />
}
