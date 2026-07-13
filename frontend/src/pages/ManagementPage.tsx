import { useState } from 'react'
import { useQuery } from '@tanstack/react-query'
import { Settings, Loader2, RefreshCw, Filter } from 'lucide-react'
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card'
import { Button } from '@/components/ui/button'
import { PageHeader } from '@/components/PageHeader'
import { useLanguage } from '@/store/languageContext'
import { getAllSpots, getAvailableCount, searchSpots } from '@/api/management'
import type { ParkingSpot } from '@/api/management'

const STATUS_COLORS: Record<ParkingSpot['status'], string> = {
	AVAILABLE: 'bg-green-100 text-green-700',
	OCCUPIED: 'bg-red-100 text-red-700',
	RESERVED: 'bg-yellow-100 text-yellow-700',
	OUT_OF_SERVICE: 'bg-slate-100 text-slate-500',
}

const TYPE_LABELS: Record<ParkingSpot['spaceType'], string> = {
	STANDARD: 'Standard',
	VIP: 'VIP',
	DISABLED: 'Disabled',
	ELECTRIC_CHARGING: 'Electric',
}

export default function ManagementPage() {
	const { t } = useLanguage()
	const [filterStatus, setFilterStatus] = useState('')
	const [filterType, setFilterType] = useState('')

	const { data: countData } = useQuery({
		queryKey: ['spots-count'],
		queryFn: getAvailableCount,
	})

	const hasFilters = filterStatus || filterType
	const { data: spots = [], isLoading, refetch } = useQuery({
		queryKey: ['spots', filterStatus, filterType],
		queryFn: () =>
			hasFilters
				? searchSpots({ status: filterStatus || undefined, type: filterType || undefined })
				: getAllSpots(),
	})

	const statuses: ParkingSpot['status'][] = ['AVAILABLE', 'OCCUPIED', 'RESERVED', 'OUT_OF_SERVICE']
	const types: ParkingSpot['spaceType'][] = ['STANDARD', 'VIP', 'DISABLED', 'ELECTRIC_CHARGING']

	return (
		<div className="space-y-6">
			<PageHeader
				icon={<Settings size={24} />}
				title={t('management.title')}
				actions={
					<Button variant="ghost" size="sm" onClick={() => refetch()}>
						<RefreshCw size={16} className="mr-1" /> {t('common.refresh')}
					</Button>
				}
			/>

			{/* Stats */}
			<div className="grid grid-cols-2 md:grid-cols-4 gap-4">
				<Card>
					<CardContent className="pt-4">
						<p className="text-xs text-slate-500 uppercase tracking-wide">{t('management.available')}</p>
						<p className="text-3xl font-bold text-green-600">{countData?.count ?? '—'}</p>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="pt-4">
						<p className="text-xs text-slate-500 uppercase tracking-wide">{t('management.total_spaces')}</p>
						<p className="text-3xl font-bold text-slate-700">{spots.length || '—'}</p>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="pt-4">
						<p className="text-xs text-slate-500 uppercase tracking-wide">{t('management.occupied')}</p>
						<p className="text-3xl font-bold text-red-500">{spots.filter((s) => s.status === 'OCCUPIED').length}</p>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="pt-4">
						<p className="text-xs text-slate-500 uppercase tracking-wide">{t('management.reserved')}</p>
						<p className="text-3xl font-bold text-yellow-500">{spots.filter((s) => s.status === 'RESERVED').length}</p>
					</CardContent>
				</Card>
			</div>

			{/* Filters */}
			<Card>
				<CardHeader>
					<CardTitle className="text-base flex items-center gap-2">
						<Filter size={16} /> {t('common.search')}
					</CardTitle>
				</CardHeader>
				<CardContent>
					<div className="grid grid-cols-1 xs:grid-cols-2 sm:flex flex-wrap gap-3 items-center">
						<div className="flex items-center gap-2">
							<span className="text-sm text-slate-500 shrink-0">{t('management.status')}:</span>
							<select
								className="flex-1 border rounded px-2 py-1 text-sm min-w-0"
								value={filterStatus}
								onChange={(e) => setFilterStatus(e.target.value)}
							>
								<option value="">{t('common.no_data')}</option>
								{statuses.map((s) => (
									<option key={s} value={s}>
										{t(`management.${s.toLowerCase()}`)}
									</option>
								))}
							</select>
						</div>
						<div className="flex items-center gap-2">
							<span className="text-sm text-slate-500 shrink-0">{t('management.space_type')}:</span>
							<select
								className="flex-1 border rounded px-2 py-1 text-sm min-w-0"
								value={filterType}
								onChange={(e) => setFilterType(e.target.value)}
							>
								<option value="">{t('common.no_data')}</option>
								{types.map((t) => (
									<option key={t} value={t}>
										{TYPE_LABELS[t]}
									</option>
								))}
							</select>
						</div>
						{hasFilters && (
							<Button variant="ghost" size="sm" onClick={() => { setFilterStatus(''); setFilterType('') }}>
								{t('common.cancel')}
							</Button>
						)}
					</div>
				</CardContent>
			</Card>

			{/* Grid */}
			<Card>
				<CardHeader>
					<CardTitle className="text-base">{t('management.available_spaces')} ({spots.length})</CardTitle>
				</CardHeader>
				<CardContent>
					{isLoading ? (
						<div className="flex items-center gap-2 text-muted-foreground text-sm py-4">
							<Loader2 size={16} className="animate-spin" /> {t('common.loading')}
						</div>
					) : spots.length === 0 ? (
						<p className="text-sm text-muted-foreground py-4">{t('common.no_data')}</p>
					) : (
						<div className="grid grid-cols-2 xs:grid-cols-3 sm:grid-cols-4 md:grid-cols-5 lg:grid-cols-6 xl:grid-cols-8 gap-3">
							{spots.map((spot) => (
								<div
									key={spot.id}
									className="rounded-lg border p-3 text-center hover:shadow-sm transition-shadow"
								>
									<p className="font-mono font-bold text-sm">{spot.spaceNumber}</p>
									<p className="text-xs text-slate-400 mt-0.5">
										L{spot.level} · {TYPE_LABELS[spot.spaceType]}
									</p>
									<span
										className={`mt-1.5 inline-block text-xs px-2 py-0.5 rounded-full font-medium ${STATUS_COLORS[spot.status]}`}
									>
										{spot.status}
									</span>
								</div>
							))}
						</div>
					)}
				</CardContent>
			</Card>
		</div>
	)
}

