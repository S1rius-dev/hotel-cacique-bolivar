import { useQuery } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { getTipoHabitacionController } from "@/api/generated/tipo-habitacion-controller/tipo-habitacion-controller";
import type {
  PageResponseHabitacionResponse,
  PageResponseTipoHabitacionResponse,
} from "@/api/generated/openAPIDefinition.schemas";

export const useRoom = (page = 0, size?: number) => {
  return useQuery<PageResponseHabitacionResponse>({
    queryKey: ["habitaciones", page, size ?? "default"],
    queryFn: () => {
      const params: { page: number; size?: number } = { page };
      if (size != null && size > 0) params.size = size;
      return getHabitacionController().listarHabitaciones({ pageable: params });
    },

  });
};

export const useTypeRoom = (page = 0, size?: number) => {
  return useQuery<PageResponseTipoHabitacionResponse>({
    queryKey: ["tiposHabitacion", page, size ?? "default"],
    queryFn: () => {
      const params: { page: number; size?: number } = { page };
      if (size != null && size > 0) params.size = size;
      return getTipoHabitacionController().listarTiposHabitacion({ pageable: params });
    },

  });
};

export const useHabitacion = (id?: number) => {
  return useQuery({
    queryKey: ["habitacion", id],
    queryFn: () => getHabitacionController().obtenerHabitacion(id!),
    enabled: !!id,
  });
};

export const useTipoHabitacion = (id?: number) => {
  return useQuery({
    queryKey: ["tipoHabitacion", id],
    queryFn: () => getTipoHabitacionController().buscarTipoHabitacion(id!),
    enabled: !!id,
  });
};


