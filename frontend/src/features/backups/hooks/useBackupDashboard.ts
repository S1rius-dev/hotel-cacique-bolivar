import { useQuery } from "@tanstack/react-query";
import { getBackups } from "@/api/generated/backups/backups";

/**
 * Resumen operativo del sistema de copias: ultimo exitoso, totales y proxima ejecucion.
 *
 * <p>Es una consulta aparte de la lista y por eso tiene su propio key, para poder invalidarla
 * por separado cuando una copia termina sin repreguntar el historial completo.</p>
 */
export const useBackupDashboard = () => {
  const api = getBackups();

  return useQuery({
    queryKey: ["backups", "dashboard"],
    queryFn: async () => await api.obtenerDashboardBackups(),
  });
};
