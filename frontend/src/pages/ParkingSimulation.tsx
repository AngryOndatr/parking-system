import { useState } from 'react';
import { gateSimApi } from '@/api/gateSim';
import apiClient from "@/lib/apiClient"
import { useLanguage } from '@/store/languageContext';

const TEST_VEHICLES = [
    { plate: 'AA1234BB', typeKey: 'sim.subscriber' as const, isSubscriber: true },
    { plate: 'BB5678CC', typeKey: 'sim.visitor' as const, isSubscriber: false }
];

type SimStep = 'IDLE' | 'APPROACHING_ENTRY' | 'AT_ENTRY' | 'PARKED' | 'APPROACHING_EXIT' | 'AT_EXIT' | 'CHECKING_EXIT' | 'DONE';

const UkrainianLicensePlate: React.FC<{ plate: string }> = ({ plate }) => {
    return (
        <div className="flex items-center justify-center w-full h-full bg-white border border-black" style={{ borderWidth: '2px' }}>
            <div className="flex items-center h-full" style={{ width: '24%', backgroundColor: '#4B7FD7', borderRight: '1px solid black' }}>
                <div className="w-full h-full flex flex-col">
                    <div className="flex-1 bg-blue-600"></div>
                    <div className="flex-1 bg-yellow-400"></div>
                </div>
            </div>
            <div className="flex-1 flex items-center justify-center">
                <span className="font-bold text-black" style={{ fontSize: '12px', letterSpacing: '2px', fontFamily: 'monospace' }}>
                    {plate}
                </span>
            </div>
        </div>
    );
};

export default function ParkingSimulation() {
    const { t } = useLanguage();
    const [selectedVehicle, setSelectedVehicle] = useState(TEST_VEHICLES[0]);
    const [simStep, setSimStep] = useState<SimStep>('IDLE');
    const [logs, setLogs] = useState<string[]>([]);
    const [gateOpen, setGateOpen] = useState(false);
    const [ticketCode, setTicketCode] = useState<string | null>(null);
    const [isPaid, setIsPaid] = useState(false);

    const addLog = (msg: string) => {
        setLogs((prev) => [`[${new Date().toLocaleTimeString()}] ${msg}`, ...prev]);
    };

    const handleStartSimulation = () => {
        setSimStep('APPROACHING_ENTRY');
        setGateOpen(false);
        setTicketCode(null);
        setIsPaid(false);
        addLog(t('sim.moving_entry'));

        setTimeout(() => {
            setSimStep('AT_ENTRY');
            addLog(t('sim.stopped_entry'));
            setTimeout(() => {
                triggerEntryScan();
            }, 1000);
        }, 2000);
    };

    const triggerEntryScan = async () => {
        addLog(t('sim.scanning', { plate: selectedVehicle.plate }));
        try {
            const res = await gateSimApi.vehicleEntry(selectedVehicle.plate, selectedVehicle.isSubscriber);
            if (res.ticketCode) {
                setTicketCode(res.ticketCode);
                addLog(t('sim.visitor_ticket', { ticket: res.ticketCode }));
            } else {
                addLog(t('sim.subscriber_detected'));
            }

            addLog(t('sim.gate_opening', { msg: res.message }));
            setGateOpen(true);

            setTimeout(() => {
                setGateOpen(false);
                setSimStep('PARKED');
                addLog(t('sim.parked'));
            }, 2500);

        } catch (err: unknown) {
            const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Access denied';
            addLog(t('sim.exit_error', { msg }));
            setSimStep('AT_ENTRY');
        }
    };

    const handlePayment = async () => {
        if (!ticketCode) return;
        addLog(t('sim.paying', { ticket: ticketCode }));
        try {
            await apiClient.post('/billing/pay-test', { ticketCode, amount: 50.0 });
            setIsPaid(true);
            addLog(t('sim.payment_success'));
        } catch {
            addLog(t('sim.payment_error'));
        }
    };

    const handleGoToExit = () => {
        setSimStep('APPROACHING_EXIT');
        addLog(t('sim.moving_exit'));

        setTimeout(() => {
            setSimStep('AT_EXIT');
            addLog(t('sim.stopped_exit'));
            setTimeout(() => {
                triggerExitScan();
            }, 1000);
        }, 2000);
    };

    const triggerExitScan = async () => {
        addLog(t('sim.checking_exit', { plate: selectedVehicle.plate }));
        setSimStep('CHECKING_EXIT');
        try {
            const res = await gateSimApi.vehicleExit(selectedVehicle.plate, ticketCode || undefined);

            if (res.gateStatus === 'OPENED') {
                addLog(t('sim.exit_confirmed', { msg: res.message }));
                addLog(t('sim.exit_opening'));
                setGateOpen(true);

                setTimeout(() => {
                    setGateOpen(false);
                    setSimStep('DONE');
                    addLog(t('sim.exit_success'));
                }, 2500);
            } else {
                setGateOpen(false);
                addLog(t('sim.exit_denied', { msg: res.message }));
                addLog(t('sim.exit_fee', { fee: res.fee ? '(' + res.fee + ' UAH)' : '' }));
                addLog(t('sim.returning'));

                setTimeout(() => {
                    setSimStep('PARKED');
                    addLog(t('sim.parked'));
                }, 2000);
            }
        } catch (err: unknown) {
            setGateOpen(false);
            setSimStep('AT_EXIT');
            const msg = (err as { response?: { data?: { message?: string } } })?.response?.data?.message || 'Payment not found';
            addLog(t('sim.exit_error_msg', { msg }));
        }
    };

    return (
        <div className="p-6 grid grid-cols-3 gap-6 bg-slate-900 text-white min-h-screen">

            {/* Control panel */}
            <div className="bg-slate-800 p-4 rounded-xl shadow-lg border border-slate-700 flex flex-col justify-between overflow-y-auto">
                <div className="flex-1 overflow-y-auto">
                    <h2 className="text-xl font-bold mb-4">{t('sim.title')}</h2>

                    <label className="block text-sm font-medium text-slate-400 mb-2">{t('sim.select_vehicle')}</label>
                    <select
                        disabled={simStep !== 'IDLE' && simStep !== 'DONE'}
                        className="w-full p-2 rounded bg-slate-700 text-white border border-slate-600 mb-6"
                        onChange={(e) => setSelectedVehicle(TEST_VEHICLES[parseInt(e.target.value)])}
                    >
                        {TEST_VEHICLES.map((v, idx) => (
                            <option key={v.plate} value={idx}>
                                {v.plate} — {t(v.typeKey)}
                            </option>
                        ))}
                    </select>

                    <div className="space-y-3">
                        {simStep === 'IDLE' || simStep === 'DONE' ? (
                            <button onClick={handleStartSimulation} className="w-full bg-emerald-600 hover:bg-emerald-500 py-2 rounded font-semibold transition">
                                {t('sim.start')}
                            </button>
                        ) : null}

                        {simStep === 'PARKED' && (
                            <div className="space-y-3 pt-2 border-t border-slate-700">
                                <div className="text-xs text-slate-400 font-medium mb-1">{t('sim.actions')}</div>

                                {!selectedVehicle.isSubscriber && !isPaid && (
                                    <button
                                        onClick={handlePayment}
                                        className="w-full bg-amber-600 hover:bg-amber-500 text-white py-2 rounded font-semibold transition flex items-center justify-center gap-2 shadow-md"
                                    >
                                        <span>{t('sim.pay')}</span>
                                    </button>
                                )}

                                <button
                                    onClick={handleGoToExit}
                                    className={`w-full py-2 rounded font-semibold transition shadow-md ${
                                        !selectedVehicle.isSubscriber && !isPaid
                                            ? 'bg-rose-700 hover:bg-rose-600 text-white border border-rose-500 animate-pulse'
                                            : 'bg-blue-600 hover:bg-blue-500 text-white'
                                    }`}
                                >
                                    {!selectedVehicle.isSubscriber && !isPaid
                                        ? t('sim.exit_unpaid')
                                        : t('sim.exit')}
                                </button>
                            </div>
                        )}

                        {simStep === 'AT_EXIT' && !gateOpen && (
                            <button onClick={triggerExitScan} className="w-full bg-red-600 hover:bg-red-500 py-2 rounded font-semibold transition">
                                {t('sim.retry_exit')}
                            </button>
                        )}
                    </div>
                </div>

                <div className="mt-6 p-3 bg-slate-700 rounded border border-slate-600 text-sm space-y-1 flex-shrink-0">
                    <div><strong>{t('sim.status')}</strong> {simStep}</div>
                    {ticketCode && <div><strong>{t('sim.ticket')}</strong> <span className="font-mono text-amber-400">{ticketCode}</span></div>}
                    {selectedVehicle.isSubscriber && <div className="text-emerald-400 font-semibold">{t('sim.subscription')}</div>}
                    {isPaid && <div className="text-emerald-400 font-semibold">{t('sim.paid')}</div>}
                </div>
            </div>

            {/* Parking map */}
            <div className="bg-slate-950 p-4 rounded-xl border border-slate-800 relative flex flex-col justify-between overflow-hidden min-h-[400px]">
                <h3 className="text-center font-bold text-slate-400">{t('sim.map')}</h3>

                <div className="absolute inset-x-0 top-1/2 h-16 bg-slate-800 -translate-y-1/2 flex items-center justify-between px-10">
                    <div className="relative ml-32">
                        <div className="text-xs text-center text-slate-400 mb-1">{t('sim.entry')}</div>
                        <div className={`w-2 h-12 bg-orange-500 origin-bottom transition-transform duration-500 ${gateOpen && (simStep === 'AT_ENTRY' || simStep === 'APPROACHING_ENTRY') ? '-rotate-90' : 'rotate-0'}`} />
                    </div>

                    <div className={`w-20 h-12 border-2 border-dashed flex items-center justify-center transition-colors ${simStep === 'PARKED' ? 'border-red-500 bg-red-950/30' : 'border-emerald-500 bg-emerald-950/30'}`}>
                        <span className="text-xs text-slate-500">{simStep === 'PARKED' ? t('sim.occupied') : t('sim.free')}</span>
                    </div>

                    <div className="relative">
                        <div className="text-xs text-center text-slate-400 mb-1">{t('sim.exit_gate')}</div>
                        <div className={`w-2 h-12 bg-orange-500 origin-bottom transition-transform duration-500 ${gateOpen && (simStep === 'AT_EXIT' || simStep === 'APPROACHING_EXIT' || simStep === 'CHECKING_EXIT') ? '-rotate-90' : 'rotate-0'}`} />
                    </div>
                </div>

                <div
                    className="absolute flex flex-col items-center justify-center shadow-lg transition-all duration-[2000ms] ease-in-out"
                    style={{
                        top: '50%',
                        transform: 'translateY(-50%)',
                        left:
                            simStep === 'IDLE' ? '-10%' :
                                simStep === 'APPROACHING_ENTRY' ? '12%' :
                                    simStep === 'AT_ENTRY' ? '20%' :
                                        simStep === 'PARKED' ? '50%' :
                                            simStep === 'APPROACHING_EXIT' ? '72%' :
                                                simStep === 'AT_EXIT' || simStep === 'CHECKING_EXIT' ? '78%' :
                                                    '110%'
                    }}
                >
                    <div className="w-20 h-7 bg-white border-2 border-black rounded" style={{ borderWidth: '1.5px' }}>
                        <UkrainianLicensePlate plate={selectedVehicle.plate} />
                    </div>
                </div>
            </div>

            {/* Log panel */}
            <div className="bg-slate-850 p-4 rounded-xl border border-slate-700 flex flex-col">
                <h3 className="font-bold text-slate-400 mb-2">{t('sim.logs')}</h3>
                <div className="flex-1 bg-black p-3 rounded font-mono text-xs text-green-400 overflow-y-auto h-[350px] space-y-1">
                    {logs.length === 0 && <span className="text-slate-600">{t('sim.waiting')}</span>}
                    {logs.map((log, idx) => (
                        <div key={idx} className="border-b border-slate-900 pb-1">{log}</div>
                    ))}
                </div>
            </div>

        </div>
    );
}
