import { useState } from 'react'
import { useNavigate, Navigate } from 'react-router-dom'
import { useMutation } from '@tanstack/react-query'
import { ParkingSquare, Loader2 } from 'lucide-react'
import { Button } from '@/components/ui/button'
import { Input } from '@/components/ui/input'
import { Label } from '@/components/ui/label'
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card'
import { login } from '@/api/auth'
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

export default function LoginPage() {
  const navigate = useNavigate()
  const { setToken, isAuthenticated, role, _hasHydrated } = useAuthStore()

  // ВСЕ хуки — до любого условного return
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState<string | null>(null)

  const mutation = useMutation({
    mutationFn: login,
    onSuccess: (data) => {
      const token = data.accessToken
      if (!token) {
        setErrorMsg('No token received from server')
        return
      }
      setToken(token)
      // небольшая задержка чтобы Zustand persist успел записать состояние
      setTimeout(() => {
        const currentRole = useAuthStore.getState().role
        navigate(getRoleDefaultRoute(currentRole ?? 'OPERATOR'), { replace: true })
      }, 50)
    },
    onError: (error: unknown) => {
      const err = error as { response?: { data?: { message?: string }; status?: number } }
      if (err.response?.status === 401 || err.response?.status === 403) {
        setErrorMsg('Invalid username or password')
      } else if (err.response?.data?.message) {
        setErrorMsg(err.response.data.message)
      } else {
        setErrorMsg('Login failed. Please try again.')
      }
    },
  })

  // Условный return — только ПОСЛЕ всех хуков
  if (_hasHydrated && isAuthenticated && role) {
    return <Navigate to={getRoleDefaultRoute(role)} replace />
  }

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault()
    setErrorMsg(null)
    mutation.mutate({ username, password })
  }

  return (
    <div className="min-h-screen flex items-center justify-center bg-slate-50">
      <div className="w-full max-w-md px-4">
        {/* Logo */}
        <div className="flex flex-col items-center mb-8">
          <div className="flex items-center gap-2 text-primary mb-2">
            <ParkingSquare size={40} strokeWidth={1.5} />
          </div>
          <h1 className="text-2xl font-bold text-slate-800">Parking System</h1>
          <p className="text-slate-500 text-sm mt-1">Management Portal</p>
        </div>

        <Card>
          <CardHeader>
            <CardTitle className="text-xl">Sign in</CardTitle>
            <CardDescription>Enter your credentials to access the system</CardDescription>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="space-y-4">
              <div className="space-y-2">
                <Label htmlFor="username">Username</Label>
                <Input
                  id="username"
                  type="text"
                  autoComplete="username"
                  placeholder="admin / operator"
                  value={username}
                  onChange={(e) => setUsername(e.target.value)}
                  required
                  disabled={mutation.isPending}
                />
              </div>

              <div className="space-y-2">
                <Label htmlFor="password">Password</Label>
                <Input
                  id="password"
                  type="password"
                  autoComplete="current-password"
                  placeholder="••••••••"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  required
                  disabled={mutation.isPending}
                />
              </div>

              {errorMsg && (
                <div className="rounded-md bg-destructive/10 border border-destructive/20 px-3 py-2 text-sm text-destructive">
                  {errorMsg}
                </div>
              )}

              <Button
                type="submit"
                className="w-full"
                disabled={mutation.isPending}
              >
                {mutation.isPending ? (
                  <>
                    <Loader2 className="mr-2 h-4 w-4 animate-spin" />
                    Signing in…
                  </>
                ) : (
                  'Sign in'
                )}
              </Button>
            </form>
          </CardContent>
        </Card>

        <p className="text-center text-xs text-slate-400 mt-6">
          Parking System © {new Date().getFullYear()}
        </p>
      </div>
    </div>
  )
}
