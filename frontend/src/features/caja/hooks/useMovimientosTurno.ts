import { useTurnoActivo } from "@/features/turnos";
import type { CajaNombre } from "@/features/turnos/constants/turno.constants";
import type { MovimientoResponse } from "@/api/generated/openAPIDefinition.schemas";

export function useMovimientosTurno(caja: CajaNombre) {
  const { data: turno, isLoading, isError, error } = useTurnoActivo({ caja });

  const movimientos: MovimientoResponse[] = turno?.movimientos ?? [];

  const ingresos = turno?.totalIngresosEfectivo ?? 0;
  const gastos = turno?.totalEgresos ?? 0;

  const cantidadIngresos = movimientos.filter(
    (m) => m.tipo === "INGRESO" || m.tipo === "PAGO",
  ).length;
  const cantidadGastos = movimientos.filter((m) => m.tipo === "EGRESO").length;

  return {
    data: movimientos,
    ingresos,
    gastos,
    cantidadIngresos,
    cantidadGastos,
    totalMovimientos: movimientos.length,
    isLoading,
    isError,
    error,
  };
}
