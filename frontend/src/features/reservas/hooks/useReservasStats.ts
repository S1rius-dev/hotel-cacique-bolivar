import { useQueries } from "@tanstack/react-query";
import { getReservaController } from "@/api/generated/reserva-controller/reserva-controller";

const API = getReservaController();

const ESTADOS = ["PENDIENTE", "CONFIRMADA", "EN_CURSO", "FINALIZADA", "CANCELADA", "NO_SHOW"] as const;

export const useReservasStats = () => {
  const queries = useQueries({
    queries: [
      {
        queryKey: ["reservas-stats", "total"],
        queryFn: () => API.listarReservas({ pageable: { page: 0, size: 1 } }),
        staleTime: 30_000,
      },
      ...ESTADOS.map((estado) => ({
        queryKey: ["reservas-stats", estado],
        queryFn: () => API.listarReservas({ pageable: { page: 0, size: 1 }, estado }),
        staleTime: 30_000,
      })),
    ],
  });

  const [totalQuery, pendienteQuery, confirmadaQuery, enCursoQuery, finalizadaQuery, canceladaQuery, noShowQuery] = queries;

  return {
    total: totalQuery.data?.totalElements ?? 0,
    pendientes: pendienteQuery.data?.totalElements ?? 0,
    confirmadas: confirmadaQuery.data?.totalElements ?? 0,
    enCurso: enCursoQuery.data?.totalElements ?? 0,
    finalizadas: finalizadaQuery.data?.totalElements ?? 0,
    canceladas: canceladaQuery.data?.totalElements ?? 0,
    noShow: noShowQuery.data?.totalElements ?? 0,
    isLoading: queries.some((q) => q.isLoading),
  };
};
