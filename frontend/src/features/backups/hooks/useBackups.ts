import { useQuery } from "@tanstack/react-query";
import { getBackups } from "@/api/generated/backups/backups";
import type { BackupFilters } from "../types";

/**
 * Listado paginado de copias, con filtros opcionales por tipo y estado.
 *
 * <p>El key incluye el objeto de filtros completo para que cambiar de pagina no reutilice el
 * resultado de la anterior: son consultas distintas en el backend.</p>
 */
export const useBackups = (filters: BackupFilters) => {
  const api = getBackups();

  return useQuery({
    queryKey: ["backups", filters],
    queryFn: async () =>
      await api.listarBackups({
        pageable: { page: filters.page, size: filters.size },
        tipo: filters.tipo,
        estado: filters.estado,
      }),
  });
};
