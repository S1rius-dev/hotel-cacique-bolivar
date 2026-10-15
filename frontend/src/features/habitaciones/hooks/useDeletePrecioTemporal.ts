import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getPrecioTemporalController } from "@/api/generated/precio-temporal-controller/precio-temporal-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";

export const useDeletePrecioTemporal = () => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => {
      return await getPrecioTemporalController().eliminarPrecioTemporal(id);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["precios-temporales"] });
      toast.success("Precio temporal eliminado");
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al eliminar precio temporal"));
    },
  });
};
