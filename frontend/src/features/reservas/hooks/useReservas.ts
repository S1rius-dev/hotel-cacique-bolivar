import { useQuery } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";
import type { PageResponseReservaResponse } from "@/api/generated/openAPIDefinition.schemas";
import type { ListarReservasParams } from "@/api/generated/openAPIDefinition.schemas";

export interface ReservaFilters {
  codigo?: string;
  cliente?: string;
  estado?: string;
  origen?: string;
  checkInDesde?: string;
  checkInHasta?: string;
  checkOutDesde?: string;
  checkOutHasta?: string;
}

export const useReservas = (
  page = 0,
  size?: number,
  filters?: ReservaFilters,
) => {
  const api = getReservaController();

  return useQuery<PageResponseReservaResponse>({
    queryKey: ["reservas", page, size ?? "default", filters],
    queryFn: () => {
      const params: ListarReservasParams = {
        pageable: { page },
      };
      if (size != null && size > 0) params.pageable.size = size;
      if (filters?.codigo) params.codigo = filters.codigo;
      if (filters?.cliente) params.cliente = filters.cliente;
      if (filters?.estado)
        params.estado = filters.estado as ListarReservasParams["estado"];
      if (filters?.origen)
        params.origen = filters.origen as ListarReservasParams["origen"];
      if (filters?.checkInDesde) params.checkInDesde = filters.checkInDesde;
      if (filters?.checkInHasta) params.checkInHasta = filters.checkInHasta;
      if (filters?.checkOutDesde) params.checkOutDesde = filters.checkOutDesde;
      if (filters?.checkOutHasta) params.checkOutHasta = filters.checkOutHasta;
      return api.listarReservas(params);
    },
  });
};
