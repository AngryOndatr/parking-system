import { useState } from 'react'
import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import {
  ParkingSquare,
  LogOut,
  Car,
  Users,
  BarChart3,
  CreditCard,
  Settings,
  LayoutDashboard,
  Menu,
  X,
} from 'lucide-react'
import { cn } from '@/lib/utils'
import { Button } from '@/components/ui/button'
import { useAuthStore } from '@/store/authStore'
import type { UserRole } from '@/types/auth'

interface NavItem {
  label: string
  to: string
  icon: React.ReactNode
  roles: UserRole[]
}

const NAV_ITEMS: NavItem[] = [
  {
    label: 'Gate Control',
    to: '/gate',
    icon: <Car size={18} />,
    roles: ['ADMIN', 'OPERATOR'],
  },
  {
    label: 'Clients',
    to: '/clients',
    icon: <Users size={18} />,
    roles: ['ADMIN', 'MANAGER', 'OPERATOR'],
  },
  {
    label: 'Management',
    to: '/management',
    icon: <Settings size={18} />,
    roles: ['ADMIN', 'MANAGER'],
  },
  {
    label: 'Billing',
    to: '/billing',
    icon: <CreditCard size={18} />,
    roles: ['ADMIN', 'OPERATOR'],
  },
  {
    label: 'Reports',
    to: '/reporting',
    icon: <BarChart3 size={18} />,
    roles: ['ADMIN', 'MANAGER', 'OPERATOR'],
  },
  {
    label: 'Dashboard',
    to: '/dashboard',
    icon: <LayoutDashboard size={18} />,
    roles: ['ADMIN', 'MANAGER'],
  },
]

export default function AppLayout() {
  const { role, username, logout } = useAuthStore()
  const navigate = useNavigate()
  const [mobileOpen, setMobileOpen] = useState(false)

  const visibleItems = NAV_ITEMS.filter(
    (item) => role && item.roles.includes(role)
  )

  const handleLogout = () => {
    logout()
    navigate('/login', { replace: true })
  }

  const roleBadgeColor: Record<UserRole, string> = {
    ADMIN: 'bg-red-500/20 text-red-300',
    MANAGER: 'bg-yellow-500/20 text-yellow-300',
    OPERATOR: 'bg-green-500/20 text-green-300',
  }

  return (
    <div className="flex min-h-screen bg-slate-100">
      {/* ── Mobile top bar (hidden on md+) ───────────────────────── */}
      <header className="md:hidden fixed top-0 inset-x-0 z-40 h-14 flex items-center justify-between px-4 bg-[hsl(var(--sidebar))] text-white shadow-lg">
        <button
          className="p-1.5 rounded-md text-slate-300 hover:text-white hover:bg-[hsl(var(--sidebar-accent))] transition-colors"
          onClick={() => setMobileOpen(true)}
          aria-label="Open navigation"
        >
          <Menu size={22} />
        </button>
        <div className="flex items-center gap-2">
          <ParkingSquare size={20} strokeWidth={1.5} className="text-blue-400" />
          <span className="font-bold text-sm tracking-tight">Parking System</span>
        </div>
        <button
          className="p-1.5 rounded-md text-slate-300 hover:text-white hover:bg-[hsl(var(--sidebar-accent))] transition-colors"
          onClick={handleLogout}
          aria-label="Logout"
        >
          <LogOut size={18} />
        </button>
      </header>

      {/* ── Overlay backdrop (mobile only) ───────────────────────── */}
      {mobileOpen && (
        <div
          className="md:hidden fixed inset-0 z-40 bg-black/50 backdrop-blur-sm"
          onClick={() => setMobileOpen(false)}
          aria-hidden="true"
        />
      )}

      {/* ── Sidebar ──────────────────────────────────────────────── */}
      <aside
        className={cn(
          'flex flex-col bg-[hsl(var(--sidebar))] text-[hsl(var(--sidebar-foreground))] shadow-lg',
          // Mobile: fixed drawer, slides in/out
          'fixed inset-y-0 left-0 z-50 w-64 transition-transform duration-300 ease-in-out',
          // Desktop: static, auto z-index, narrower
          'md:relative md:z-auto md:w-60 md:translate-x-0',
          mobileOpen ? 'translate-x-0' : '-translate-x-full'
        )}
      >
        {/* Logo + mobile close button */}
        <div className="flex items-center justify-between px-5 py-5 border-b border-[hsl(var(--sidebar-border))]">
          <div className="flex items-center gap-2">
            <ParkingSquare size={24} strokeWidth={1.5} className="text-blue-400" />
            <span className="font-bold text-base tracking-tight">Parking System</span>
          </div>
          <button
            className="md:hidden p-1 rounded text-slate-400 hover:text-white transition-colors"
            onClick={() => setMobileOpen(false)}
            aria-label="Close menu"
          >
            <X size={18} />
          </button>
        </div>

        {/* Nav links */}
        <nav className="flex-1 px-3 py-4 space-y-1 overflow-y-auto">
          {visibleItems.map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              onClick={() => setMobileOpen(false)}
              className={({ isActive }) =>
                cn(
                  'flex items-center gap-3 px-3 py-2 rounded-md text-sm font-medium transition-colors',
                  isActive
                    ? 'bg-[hsl(var(--sidebar-primary))] text-[hsl(var(--sidebar-primary-foreground))]'
                    : 'text-slate-300 hover:bg-[hsl(var(--sidebar-accent))] hover:text-white'
                )
              }
            >
              {item.icon}
              {item.label}
            </NavLink>
          ))}
        </nav>

        {/* User info + logout */}
        <div className="px-4 py-4 border-t border-[hsl(var(--sidebar-border))]">
          <div className="mb-3">
            <p className="text-sm font-medium text-white truncate">{username}</p>
            {role && (
              <span
                className={cn(
                  'text-xs px-2 py-0.5 rounded-full font-medium',
                  roleBadgeColor[role]
                )}
              >
                {role}
              </span>
            )}
          </div>
          <Button
            variant="ghost"
            size="sm"
            className="w-full justify-start text-slate-300 hover:text-white hover:bg-[hsl(var(--sidebar-accent))]"
            onClick={handleLogout}
          >
            <LogOut size={16} className="mr-2" />
            Logout
          </Button>
        </div>
      </aside>

      {/* ── Main content ─────────────────────────────────────────── */}
      <main className="flex-1 min-w-0 flex flex-col overflow-auto">
        {/* pt-14 on mobile clears the fixed top bar; md resets to normal padding */}
        <div className="flex-1 p-4 pt-[4.5rem] md:pt-6 md:p-6">
          <Outlet />
        </div>
      </main>
    </div>
  )
}
