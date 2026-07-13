import apiClient from '../lib/apiClient';

export interface GateResponse {
    status: 'OPENED' | 'DENIED';
    gateStatus?: 'OPENED' | 'CLOSED';
    ticketCode?: string;
    message: string;
    parkingEventId?: number;
    fee?: number;
    isPaid?: boolean;
    paymentRequired?: boolean;
}

// frontend/src/api/gateSim.ts

export const gateSimApi = {
    vehicleEntry: async (licensePlate: string, isSubscriber: boolean) => {
        // Подставьте сюда ТЕ КЛЮЧИ, которые увидели в Java / YAML файле:
        const entryMethod = isSubscriber ? 'SCAN' : 'SCAN';

        const response = await apiClient.post('/gate/entry', {
            licensePlate,
            gateId: 'GATE_IN_1',
            entryMethod: entryMethod
        });
        return response.data;
    },

    vehicleExit: async (licensePlate: string, ticketCode?: string, isSubscriber?: boolean) => {
        // То же самое для выезда  SCAN, MANUAL, AUTO
        const exitMethod = isSubscriber ? 'SCAN' : 'AUTO';

        const response = await apiClient.post('/gate/exit', {
            licensePlate,
            ticketCode,
            gateId: 'GATE_OUT_1',
            exitMethod: exitMethod
        });
        return response.data;
    }
};
