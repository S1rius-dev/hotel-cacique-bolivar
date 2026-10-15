import { useQuery } from "@tanstack/react-query";
import { getCajaController } from "@/api/generated/caja-controller/caja-controller";
import type { ObtenerTurnoActivoParams } from "@/api/generated/openAPIDefinition.schemas";
import { turnoDesdeDTO } from "../constants/turno.constants";

export function useTurnoActivo(params?: ObtenerTurnoActivoParams) {
  return useQuery({
    queryKey: ["turno-activo", params],
    queryFn: async () => {
      const cajaCtrl = getCajaController();
      return cajaCtrl.obtenerTurnoActivo(params);
    },
    retry: false,
    refetchInterval: 60_000,
    refetchOnWindowFocus: true,
  });
}

export function useTurnoDisplay() {
  const { data, isLoading } = useTurnoActivo();
  const fromApi = turnoDesdeDTO(data);
  return { turno: fromApi, isLoading };
}
