import { useQuery } from "@tanstack/react-query";
import { getPagoController } from "@/api/generated/pago-controller/pago-controller";
import type { PagePagoDashboardResponse, MetricasPagosResponse, PagoEventoResponse } from "@/api/generated/openAPIDefinition.schemas";
import { toPagoEvento } from "../utils/mapPago";

const pagoController = getPagoController();

export function usePagosDashboard(params: { q?: string; canal?: string; estado?: string; page: number; size: number }) {
  return useQuery({
    queryKey: ["pagos-dashboard", params],
    queryFn: () =>
      pagoController.listarPagosDashboard({
        q: params.q || undefined,
        canal: params.canal !== "todos" ? params.canal : undefined,
        estado: params.estado !== "todos" ? params.estado : undefined,
        pageable: { page: params.page, size: params.size },
      }),
    placeholderData: (prev) => prev,
  });
}

export function useMetricasPagos() {
  return useQuery({
    queryKey: ["pagos-metricas"],
    queryFn: () => pagoController.metricasPagos(),
  });
}

export function usePagoEventos(pagoId: number | undefined, enabled: boolean) {
  return useQuery({
    queryKey: ["pago-eventos", pagoId],
    queryFn: () => pagoController.listarEventosPago(pagoId!),
    enabled: !!pagoId && enabled,
    staleTime: 0,
    refetchOnWindowFocus: true,
    // Polling ligero mientras el drawer está abierto y el pago aún pendiente
    refetchInterval: enabled ? 20000 : false,
    select: (data) => data?.map(toPagoEvento) ?? [],
  });
}

export type { PagePagoDashboardResponse, MetricasPagosResponse, PagoEventoResponse };



