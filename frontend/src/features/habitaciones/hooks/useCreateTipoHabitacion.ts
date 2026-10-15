import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getTipoHabitacionController } from "@/api/generated/tipo-habitacion-controller/tipo-habitacion-controller";
import type { UseFormReturn } from "react-hook-form";
import type { TipoHabitacionFormValues } from "../schemas/tipoHabitacion.schema";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCreateTipoHabitacion = (
  form: UseFormReturn<TipoHabitacionFormValues>,
) => {
  const api = getTipoHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: TipoHabitacionFormValues) => {
      return await api.crearTipoHabitacion({
        nombre: data.nombre,
        descripcion: data.descripcion,
        capacidadBase: data.capacidad,
        capacidadMaxima: data.capacidad,
        precioBase: data.precioBase,
        precioPersonaExtra: data.precioPersonaExtra,
        precioConIva: data.precioConIva,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["tiposHabitacion"],
      });
      toast.success("Tipo de habitación creado correctamente");
      window.history.back();
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;
      form.setError("root", {
        type: "server",
        message: extractErrorMessage(
          error,
          "Error inesperado al crear tipo de habitación",
        ),
      });
    },
  });
};