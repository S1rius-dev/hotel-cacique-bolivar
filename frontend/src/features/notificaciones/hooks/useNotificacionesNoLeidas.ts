import { getNotificacionAdminController } from "@/api/generated/notificacion-admin-controller/notificacion-admin-controller";
import { useQuery } from "@tanstack/react-query";
import type { CantidadNoLeidasResponse } from "@/api/generated/openAPIDefinition.schemas";

const api = getNotificacionAdminController();

export const useNotificacionesNoLeidas = () => {
  return useQuery<CantidadNoLeidasResponse>({
    queryKey: ["notificaciones", "no-leidas"],
    queryFn: async () => {
      return await api.contarNoLeidas();
    },
    refetchInterval: 30_000,
  });
};
