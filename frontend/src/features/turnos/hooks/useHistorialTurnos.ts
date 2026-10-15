import { useQuery } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type { Pageable } from "@/api/generated/openAPIDefinition.schemas";
import type { CajaNombre } from "../constants/turno.constants";

export function useHistorialTurnos(caja: CajaNombre, pageable: Pageable) {
  return useQuery({
    queryKey: ["turno-historial", caja, pageable],
    queryFn: async () => {
      const cajaCtrl = getCajaController();
      return cajaCtrl.obtenerHistorial({
        caja,
        pageable,
      });
    },
  });
}
