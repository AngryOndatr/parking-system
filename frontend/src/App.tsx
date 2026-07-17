import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import { QueryClient, QueryClientProvider } from '@tanstack/react-query'
import { LanguageProvider } from '@/store/languageContext'
import LoginPage from '@/pages/LoginPage'
import AppLayout from '@/layouts/AppLayout'
import ProtectedRoute from '@/components/ProtectedRoute'
import RoleRedirect from '@/components/RoleRedirect'
import GatePage from '@/pages/GatePage'
import ClientsPage from '@/pages/ClientsPage'
import ClientEditPage from '@/pages/ClientEditPage'
import ManagementPage from '@/pages/ManagementPage'
import BillingPage from '@/pages/BillingPage'
import ReportingPage from '@/pages/ReportingPage'
import DashboardPage from '@/pages/DashboardPage'
import SubscriptionsPage from '@/pages/SubscriptionsPage'
import ParkingSimulation from '@/pages/ParkingSimulation'

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      retry: 1,
      staleTime: 30_000,
    },
  },
})

export default function App() {
  return (
    <LanguageProvider>
      <QueryClientProvider client={queryClient}>
        <BrowserRouter>
          <Routes>
            {/* Public */}
            <Route path="/login" element={<LoginPage />} />

            {/* Protected — all authenticated users */}
            <Route element={<ProtectedRoute />}>
              <Route element={<AppLayout />}>
                {/* Root → role-based redirect */}
                <Route index element={<RoleRedirect />} />

                {/* OPERATOR + ADMIN */}
                <Route
                  path="gate"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']} />
                  }
                >
                  <Route index element={<GatePage />} />
                </Route>

                {/* ИНТЕРАКТИВНЫЙ СИМУЛЯТОР: ДОСТУПЕН ДЛЯ ADMIN + OPERATOR */}
                <Route
                  path="simulation"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']} />
                  }
                >
                  <Route index element={<ParkingSimulation />} />
                </Route>

                {/* ADMIN + MANAGER + OPERATOR */}
                <Route
                  path="clients"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'OPERATOR']} />
                  }
                >
                  <Route index element={<ClientsPage />} />
                  <Route path=":id/edit" element={<ClientEditPage />} />
                </Route>

                {/* ADMIN + MANAGER */}
                <Route
                  path="management"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />
                  }
                >
                  <Route index element={<ManagementPage />} />
                </Route>

                {/* ADMIN + OPERATOR */}
                <Route
                  path="billing"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'OPERATOR']} />
                  }
                >
                  <Route index element={<BillingPage />} />
                </Route>

                {/* ADMIN + MANAGER + OPERATOR */}
                <Route
                  path="reporting"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'OPERATOR']} />
                  }
                >
                  <Route index element={<ReportingPage />} />
                </Route>

                {/* ADMIN + MANAGER + OPERATOR — Subscriptions */}
                <Route
                  path="subscriptions"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER', 'OPERATOR']} />
                  }
                >
                  <Route index element={<SubscriptionsPage />} />
                </Route>

                {/* ADMIN + MANAGER */}
                <Route
                  path="dashboard"
                  element={
                    <ProtectedRoute allowedRoles={['ADMIN', 'MANAGER']} />
                  }
                >
                  <Route index element={<DashboardPage />} />
                </Route>
              </Route>
            </Route>

            {/* Catch-all */}
            <Route path="*" element={<Navigate to="/" replace />} />
          </Routes>
        </BrowserRouter>
      </QueryClientProvider>
    </LanguageProvider>
  )
}