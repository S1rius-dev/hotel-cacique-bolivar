import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getPrecioTemporalController } from "@/api/generated/precio-temporal-controller/precio-temporal-controller";
import type { UseFormReturn } from "react-hook-form";
import type { PrecioTemporalFormValues } from "../schemas/precioTemporal.schema";
import { toast } from "sonner";
import { extractErrorMessage } from "@/lib/extract-error";
import { applyServerFieldErrors } from "@/lib/form-server-errors";

export const useCreatePrecioTemporal = (
  form: UseFormReturn<PrecioTemporalFormValues>,
  tipoHabitacionId: number,
  onSuccess?: () => void,
) => {
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: PrecioTemporalFormValues) => {
      const api = getPrecioTemporalController();
      return await api.crearPrecioTemporal({
        tipoHabitacionId,
        fechaInicio: data.fechaInicio,
        fechaFin: data.fechaFin,
        precioNoche: data.precioNoche,
        precioPersonaExtra: data.precioPersonaExtra,
        precioNocheIva: data.precioNocheIva,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["precios-temporales"] });
      toast.success("Precio temporal creado correctamente");
      form.reset();
      onSuccess?.();
    },
    onError: (error: unknown) => {
      // `applyServerFieldErrors` cubre las dos formas que emite el backend
      // (array `errors` y mapa `fieldErrors`) sin lista blanca de campos.
      if (applyServerFieldErrors(error, form.setError)) return;
      const message = extractErrorMessage(
        error,
        "Error inesperado al crear precio temporal",
      );
      form.setError("root", {
        type: "server",
        message,
      });
      toast.error(message);
    },
  });
};
