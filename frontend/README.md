# 🖥️ Parking System — React Frontend

React SPA для системы управления парковкой. Часть микросервисного проекта [Parking System](../README.md).

## Tech Stack

| Technology | Version | Purpose |
|-----------|---------|---------|
| **React** | 19 | UI framework |
| **TypeScript** | — | Type safety |
| **Vite** | — | Build tool + dev proxy |
| **Tailwind CSS** | 3 | Styling |
| **Radix UI** | — | shadcn/ui components (Dialog, DropdownMenu, Separator) |
| **React Router** | 6 | Client-side routing |
| **TanStack Query** | 5 | Server state management |
| **Zustand** | 4 | Client state (auth store) |
| **Axios** | — | HTTP client |
| **Lucide React** | — | Icons |

## Project Structure

```
src/
├── api/              # Axios service clients
│   ├── auth.ts       # POST /api/auth/login, /refresh, /logout
│   ├── clients.ts    # /api/clients, /api/vehicles
│   ├── gate.ts       # /api/v1/gate/entry, /exit, /control
│   ├── billing.ts    # /api/v1/billing/*
│   ├── management.ts # /api/management/spots/*
│   └── reporting.ts  # /api/reporting/*
├── pages/            # Route pages
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── GatePage.tsx
│   ├── ClientsPage.tsx
│   ├── BillingPage.tsx
│   ├── ManagementPage.tsx
│   └── ReportingPage.tsx
├── components/       # Shared UI components
├── layouts/          # AppLayout with role-based sidebar
├── store/            # Zustand stores
│   └── authStore.ts  # JWT in localStorage, role decoded from token
└── types/            # TypeScript types
```

## Quick Start

```bash
# Install dependencies
npm install

# Start dev server (http://localhost:5173)
npm run dev

# Build for production
npm run build
```

> Dev server proxies all `/api/*` requests → `http://localhost:8086` (API Gateway).  
> Backend must be running: `docker-compose up -d`

## Authentication

JWT stored in `localStorage`. Role decoded from token payload (`atob`).

| Username | Password | Role | Redirect after login |
|----------|----------|------|----------------------|
| `admin` | `parking123` | ADMIN | `/clients` |
| `operator` | `parking123` | OPERATOR | `/gate` |
| `manager` | `manager123` | MANAGER | `/management` |

## Role-Based Navigation

| Role | Accessible pages |
|------|-----------------|
| ADMIN | All pages |
| OPERATOR | Gate, Billing |
| MANAGER | Clients, Management, Reporting |

## Vite Proxy Config

```typescript
// vite.config.ts
server: {
  proxy: {
    '/api': 'http://localhost:8086'
  }
}
```

## Available Scripts

```bash
npm run dev      # Dev server with HMR
npm run build    # Production build (tsc + vite build)
npm run preview  # Preview production build
npm run lint     # ESLint
```

## Notes

- `allowedHosts: 'all'` in vite.config.ts allows access from LAN (mobile testing at `http://192.168.1.X:5173`)
- CORS on API Gateway covers `http://192.168.*` wildcard — no config change needed for DHCP IP changes
