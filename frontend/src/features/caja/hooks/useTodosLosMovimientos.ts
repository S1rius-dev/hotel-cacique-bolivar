import { useQuery } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type { CajaNombre } from "@/features/turnos/constants/turno.constants";

export function useTodosLosMovimientos(caja: CajaNombre) {
  return useQuery({
    queryKey: ["todos-los-movimientos", caja],
    queryFn: async () => {
      const cajaCtrl = getCajaController();
      return cajaCtrl.listarTodosLosMovimientos({ caja });
    },
  });
}
