import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getTipoHabitacionController } from "@/api/generated/tipo-habitacion-controller/tipo-habitacion-controller";
import type { TipoHabitacionFormValues } from "../schemas/tipoHabitacion.schema";
import type { TipoHabitacionRequest } from "@/api/generated/openAPIDefinition.schemas";
import type { UseFormReturn } from "react-hook-form";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useUpdateTipoHabitacion = (
  form: UseFormReturn<TipoHabitacionFormValues>,
) => {
  const api = getTipoHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({
      id,
      data,
    }: {
      id: number;
      data: TipoHabitacionFormValues;
    }) => {
      const payload: TipoHabitacionRequest = {
        nombre: data.nombre,
        descripcion: data.descripcion,
        capacidadBase: data.capacidad,
        capacidadMaxima: data.capacidad,
        precioBase: data.precioBase,
        precioPersonaExtra: data.precioPersonaExtra,
        precioConIva: data.precioConIva,
      };
      return await api.actualizarTipoHabitacion(id, payload);
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["tiposHabitacion"] });
      toast.success("Tipo de habitación actualizado correctamente");
      window.history.back();
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;
      form.setError("root", {
        type: "server",
        message: extractErrorMessage(
          error,
          "Error inesperado al actualizar tipo de habitación",
        ),
      });
    },
  });
};