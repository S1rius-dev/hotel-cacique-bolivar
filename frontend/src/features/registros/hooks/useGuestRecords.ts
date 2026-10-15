import { useQuery } from "@tanstack/react-query";
import { getHuespedRegistroController } from "@/api/generated/huesped-registro-controller/huesped-registro-controller";
import type {
  PageResponseHuespedRegistroDTO,
  ConsultarRegistrosParams,
} from "@/api/generated/openAPIDefinition.schemas";
import type { GuestFilters } from "../components/types";

const api = getHuespedRegistroController();

interface UseGuestRecordsFilters extends GuestFilters {
  page?: number;
  size?: number;
}

export const useGuestRecords = (filters?: UseGuestRecordsFilters) => {
  const {
    page = 0,
    size = 15,
    fechaDesde,
    fechaHasta,
    habitacion,
    tipoHabitacionId,
    busqueda,
    estado,
  } = filters ?? {};

  return useQuery<PageResponseHuespedRegistroDTO>({
    queryKey: [
      "huesped-registros",
      page,
      size,
      fechaDesde,
      fechaHasta,
      habitacion,
      tipoHabitacionId,
      busqueda,
      estado,
    ],
    queryFn: async () => {
      const params: ConsultarRegistrosParams = {
        pageable: { page, size },
      };
      if (fechaDesde) params.fechaDesde = fechaDesde;
      if (fechaHasta) params.fechaHasta = fechaHasta;
      if (habitacion) params.habitacion = habitacion;
      if (tipoHabitacionId != null) params.tipoHabitacionId = tipoHabitacionId;
      if (busqueda) params.busqueda = busqueda;
      if (estado) params.estado = estado;
      return await api.consultarRegistros(params);
    },
  });
};
