import { getNotificacionAdminController } from "@/api/generated/notificacion-admin-controller/notificacion-admin-controller";
import { useQuery } from "@tanstack/react-query";
import type { Notificacion } from "../types";

const api = getNotificacionAdminController();

export const useNotificaciones = (page = 0, size = 20) => {
  return useQuery<Notificacion[]>({
    queryKey: ["notificaciones", "lista", page, size],
    queryFn: async () => {
      const response = await api.listarNotificaciones({
        pageable: { page, size },
      });
      return (response.content ?? []).map((n) => ({
        id: n.id!,
        tipo: n.tipo!,
        mensaje: n.mensaje!,
        origen: n.origen ?? null,
        origenNombre: n.origenNombre ?? null,
        detalles: n.detalles ?? null,
        referenciaId: n.referenciaId ?? null,
        referenciaTipo: n.referenciaTipo ?? null,
        leida: n.leida ?? false,
        fechaCreacion: n.fechaCreacion!,
        fechaLectura: n.fechaLectura ?? null,
      }));
    },
  });
};
