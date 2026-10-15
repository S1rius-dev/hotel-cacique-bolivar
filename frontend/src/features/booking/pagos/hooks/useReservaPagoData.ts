import { useQuery } from "@tanstack/react-query";
import { misReservasApi, type MisReservasItem } from "@/features/reservas-cliente/api/mis-reservas.api";

export function useReservaPagoData(reservaId: number | undefined, enabled = true) {
  return useQuery({
    queryKey: ["mis-reservas-pago", reservaId],
    queryFn: async (): Promise<MisReservasItem | null> => {
      const data = await misReservasApi.listar();
      if (!Array.isArray(data)) return null;
      return data.find((item) => item.id === reservaId) ?? null;
    },
    enabled: enabled && reservaId != null,
    staleTime: 30 * 1000,
    retry: false,
  });
}
