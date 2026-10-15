import { useClientes } from "./useClientes";

export const useClientesStats = () => {
  const { data: totalData, isLoading: totalLoading } = useClientes({ page: 0, size: 1 });
  const { data: activeData, isLoading: activeLoading } = useClientes({ page: 0, size: 1, activo: true });

  const total = totalData?.totalElements ?? 0;
  const activos = activeData?.totalElements ?? 0;

  return {
    total,
    activos,
    inactivos: total - activos,
    isLoading: totalLoading || activeLoading,
  };
};
