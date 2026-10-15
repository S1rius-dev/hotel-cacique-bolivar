import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import type { HabitacionFormValues } from "../schemas/habitacion.schema";
import type { HabitacionUpdateRequest } from "@/api/generated/openAPIDefinition.schemas";
import type { UseFormReturn } from "react-hook-form";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useUpdateHabitacion = (
  form: UseFormReturn<HabitacionFormValues>,
) => {
  const api = getHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      data,
    }: {
      id: number;
      data: HabitacionFormValues;
    }) => {
      const payload: HabitacionUpdateRequest = {
        numero: data.numero,
        tipoId: data.tipoId,
        descripcion: data.descripcion,
        estado: data.estado,
      };
      return await api.actualizarHabitacion(id, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["habitaciones"] });
      toast.success("Habitación actualizada correctamente");
      window.history.back();
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;
      form.setError("root", {
        type: "server",
        message: extractErrorMessage(error, "Error inesperado al actualizar habitación"),
      });
    },
  });
};

