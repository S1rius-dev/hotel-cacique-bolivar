import { useQuery } from "@tanstack/react-query";
import { getPrecioTemporalController } from "@/api/generated/precio-temporal-controller/precio-temporal-controller";

export const usePreciosTemporales = (tipoId?: number) => {
  return useQuery({
    queryKey: ["precios-temporales", tipoId],
    queryFn: () => getPrecioTemporalController().listarPreciosTemporales({ tipoId }),
    enabled: tipoId != null,
  });
};
