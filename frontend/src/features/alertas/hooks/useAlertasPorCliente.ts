import { getAlertaController } from "@/api/generated/alerta-controller/alerta-controller";
import { useQuery } from "@tanstack/react-query";
import type { PageResponseAlertaResponse } from "@/api/generated/openAPIDefinition.schemas";

const api = getAlertaController();

export const useAlertasPorCliente = (clienteId?: number, page = 0, size = 50) => {
  return useQuery<PageResponseAlertaResponse>({
    queryKey: ["alertas", "cliente", clienteId, page, size],
    enabled: !!clienteId,
    queryFn: async () => {
      return await api.listarAlertasPorCliente(clienteId!, { pageable: { page, size } });
    },
  });
};