import { useQueries } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import type { ReservaResponse } from "@/api/generated/openAPIDefinition.schemas";

const api = getReservaController();

export const useReservasPorIds = (ids: number[]) => {
  const results = useQueries({
    queries: ids.map((id) => ({
      queryKey: ["reserva", id],
      queryFn: () => api.buscarReservaPorId(id),
      enabled: !!id,
    })),
  });

  return {
    reservas: results
      .filter((r) => r.data)
      .map((r) => r.data!) as ReservaResponse[],
    isLoading: results.some((r) => r.isLoading),
  };
};
