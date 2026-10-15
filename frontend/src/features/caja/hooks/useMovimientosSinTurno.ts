import { useQuery } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type { CajaNombre } from "@/features/turnos/constants/turno.constants";

export function useMovimientosSinTurno(caja: CajaNombre) {
  return useQuery({
    queryKey: ["movimientos-sin-turno", caja],
    queryFn: async () => {
      const cajaCtrl = getCajaController();
      return cajaCtrl.listarMovimientosSinTurno({ caja });
    },
  });
}

export function useMovimientosSinTurnoResumen(caja: CajaNombre) {
  return useQuery({
    queryKey: ["movimientos-sin-turno-resumen", caja],
    queryFn: async () => {
      const cajaCtrl = getCajaController();
      return cajaCtrl.movimientosSinTurno({ caja });
    },
  });
}
