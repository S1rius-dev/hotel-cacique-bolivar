import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getHabitacionController } from "@/api/generated/habitacion-controller/habitacion-controller";
import type { UseFormReturn } from "react-hook-form";
import type { HabitacionFormValues } from "../schemas/habitacion.schema";
import { toast } from "sonner";
import { applyServerFieldErrors } from "@/lib/form-server-errors";
import { extractErrorMessage } from "@/lib/extract-error";

export const useCreateHabitacion = (
  form: UseFormReturn<HabitacionFormValues>,
) => {
  const api = getHabitacionController();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (data: HabitacionFormValues) => {
      return await api.crearHabitacion({
        numero: data.numero,
        tipoId: data.tipoId,
        descripcion: data.descripcion,
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({
        queryKey: ["habitaciones"],
      });
      toast.success("Habitación creada correctamente");
      window.history.back();
    },
    onError: (error: unknown) => {
      if (applyServerFieldErrors(error, form.setError)) return;
      form.setError("root", {
        type: "server",
        message: extractErrorMessage(error, "Error inesperado al crear habitación"),
      });
    },
  });
};

