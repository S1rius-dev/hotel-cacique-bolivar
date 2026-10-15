import { useQuery } from "@tanstack/react-query";
import { getDashboardController } from "@/api/generated/dashboard-controller/dashboard-controller";
import type {
  DashboardResumen,
  IngresosResponse,
  OcupacionResponse,
  UltimaReserva,
} from "@/api/generated/openAPIDefinition.schemas";

const api = getDashboardController();

export const useDashboardResumen = () =>
  useQuery<DashboardResumen>({
    queryKey: ["dashboard", "resumen"],
    queryFn: () => api.obtenerResumen(),
    refetchInterval: 60_000,
  });

export const useDashboardIngresos = (rango: string = "HOY") =>
  useQuery<IngresosResponse>({
    queryKey: ["dashboard", "ingresos", rango],
    queryFn: () => api.obtenerIngresos({ rango }),
    refetchInterval: 60_000,
  });

export const useDashboardOcupacion = (rango: string = "SEMANA") =>
  useQuery<OcupacionResponse>({
    queryKey: ["dashboard", "ocupacion", rango],
    queryFn: () => api.obtenerOcupacion({ rango }),
    refetchInterval: 60_000,
  });

export const useUltimasReservas = (limite: number = 5) =>
  useQuery<UltimaReserva[]>({
    queryKey: ["dashboard", "ultimas-reservas", limite],
    queryFn: () => api.obtenerUltimasReservas({ limite }),
    refetchInterval: 120_000,
  });
