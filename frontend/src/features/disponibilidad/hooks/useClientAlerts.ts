import { useQuery } from "@tanstack/react-query";
import { getAlertaController } from "@/api/generated/alerta-controller/alerta-controller";
export const useClientAlerts = (clienteId: number | undefined) => {
  return useQuery({
    queryKey: ["alertas", "cliente", clienteId],
    queryFn: async () => {
      const res = await getAlertaController().listarAlertasPorCliente(clienteId!, {
        pageable: { page: 0, size: 20 },
      });
      return (res.content ?? []).filter((a) => a.estado !== false);
    },
    enabled: !!clienteId,
    staleTime: 60_000,
  });
};
