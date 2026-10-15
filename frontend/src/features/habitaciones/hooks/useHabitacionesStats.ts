import { useQuery } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import { getTipoHabitacionController } from "@/api/generated/tipo-habitacion-controller/tipo-habitacion-controller";

export const useHabitacionesStats = () => {
  const { data: habitacionesData, isLoading: loadingHabitaciones } = useQuery({
    queryKey: ["habitaciones-stats"],
    queryFn: () =>
      getHabitacionController().listarHabitaciones({ pageable: { page: 0, size: 1 } }),
    staleTime: 30_000,
  });

  const { data: tiposData, isLoading: loadingTipos } = useQuery({
    queryKey: ["tiposHabitacion-stats"],
    queryFn: () =>
      getTipoHabitacionController().listarTiposHabitacion({ pageable: { page: 0, size: 1 } }),
    staleTime: 30_000,
  });

  return {
    totalHabitaciones: habitacionesData?.totalElements ?? 0,
    totalTipos: tiposData?.totalElements ?? 0,
    isLoading: loadingHabitaciones || loadingTipos,
  };
};


