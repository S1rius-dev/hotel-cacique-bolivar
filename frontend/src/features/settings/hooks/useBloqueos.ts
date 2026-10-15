import { useQuery } from "@tanstack/react-query";
import { getBloqueoReservaController } from "@/api/generated/bloqueo-reserva-controller/bloqueo-reserva-controller";
import type { BloqueoReservaResponse } from "@/api/generated/openAPIDefinition.schemas";

const mapBloqueo = (item: BloqueoReservaResponse) => ({
  id: item.id ?? 0,
  fechaInicio: item.fechaInicio ?? "",
  fechaFin: item.fechaFin ?? "",
  motivo: item.motivo ?? "",
  activo: item.activo ?? false,
  createdAt: item.createdAt,
});

export const useBloqueos = () => {
  const api = getBloqueoReservaController();

  return useQuery({
    queryKey: ["bloqueos"],
    queryFn: async () => {
      const data = await api.listarBloqueos();
      return (data ?? []).map(mapBloqueo);
    },
  });
};

export const useBloqueo = (id?: number) => {
  const api = getBloqueoReservaController();

  return useQuery({
    queryKey: ["bloqueo", id],
    queryFn: async () => {
      const data = await api.buscarBloqueoPorId(id!);
      return mapBloqueo(data);
    },
    enabled: !!id,
  });
};
