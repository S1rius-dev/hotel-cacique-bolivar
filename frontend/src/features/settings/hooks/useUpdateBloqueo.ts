import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getBloqueoReservaController } from "@/api/generated/bloqueo-reserva-controller/bloqueo-reserva-controller";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";
import type { BlockFormValues } from "../schemas/block.schema";

interface UpdateInput {
  id: number;
  data: BlockFormValues;
}

export const useUpdateBloqueo = () => {
  const api = getBloqueoReservaController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, data }: UpdateInput) => {
      return await api.actualizarBloqueo(id, {
        fechaInicio: data.fechaInicio.toISOString().split("T")[0],
        fechaFin: data.fechaFin.toISOString().split("T")[0],
        motivo: data.motivo,
        activo: data.activo,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["bloqueos"] });
      toast.success("Bloqueo actualizado correctamente");
    },
    onError: (error: unknown) => {
      toast.error(extractErrorMessage(error, "Error al actualizar bloqueo"));
    },
  });
};
