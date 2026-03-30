import type { ReactNode } from 'react'
import { cn } from '@/lib/utils'

interface PageHeaderProps {
  icon: ReactNode
  title: string
  description?: string
  /** Buttons / controls rendered on the right (wrap below the title on narrow screens) */
  actions?: ReactNode
  className?: string
}

/**
 * Responsive page-level heading used across all admin pages.
 * On wide screens: icon+title left, actions right.
 * On narrow screens: icon+title full-width, actions wrap below.
 */
export function PageHeader({ icon, title, description, actions, className }: PageHeaderProps) {
  return (
    <div className={cn('flex flex-wrap items-start justify-between gap-3', className)}>
      <div className="flex items-center gap-2 min-w-0">
        <span className="text-primary shrink-0">{icon}</span>
        <div className="min-w-0">
          <h1 className="text-xl sm:text-2xl font-bold text-slate-800 leading-tight">{title}</h1>
          {description && (
            <p className="text-sm text-muted-foreground truncate">{description}</p>
          )}
        </div>
      </div>
      {actions && (
        <div className="flex flex-wrap items-center gap-2 shrink-0">
          {actions}
        </div>
      )}
    </div>
  )
}

