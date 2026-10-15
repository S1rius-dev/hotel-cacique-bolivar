import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getBackups } from "@/api/generated/backups/backups";
import { extractErrorMessage } from "@/lib/extract-error";

/**
 * Elimina una copia: borra el archivo almacenado y su registro del historial.
 *
 * <p>La confirmacion vive en la pagina, en un ConfirmDeleteDialog, porque es irreversible y
 * ademas arrastra datos personales de huespedes dentro del archivo.</p>
 */
export const useDeleteBackup = () => {
  const api = getBackups();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async (id: number) => await api.eliminarBackup(id),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["backups"] });
      toast.success("Copia de seguridad eliminada");
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "No se pudo eliminar la copia de seguridad"));
    },
  });
};
