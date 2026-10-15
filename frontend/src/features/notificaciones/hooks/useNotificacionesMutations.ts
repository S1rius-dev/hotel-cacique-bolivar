import { getNotificacionAdminController } from "@/api/generated/notificacion-admin-controller/notificacion-admin-controller";
import { useMutation, useQueryClient } from "@tanstack/react-query";

const api = getNotificacionAdminController();

export const useMarcarNotificacionLeida = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      await api.marcarLeida(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notificaciones"] });
    },
  });
};

export const useMarcarTodasLeidas = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => {
      await api.marcarTodasLeidas();
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["notificaciones"] });
    },
  });
};
