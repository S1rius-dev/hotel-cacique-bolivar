import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getBackups } from "@/api/generated/backups/backups";
import { extractErrorMessage } from "@/lib/extract-error";

/** Base creada por el clon, tal y como la devuelve el backend. */
export type ResultadoClon = {
  backupId: number;
  baseDestino: string;
  mensaje: string;
};

/**
 * Restaura una copia en una base de datos nueva.
 *
 * <p>La peticion es sincrona y puede tardar varios minutos en una base grande, asi que el
 * backend aplica su propio timeout (mas amplio que el del dump). La UI no necesita un
 *mutacion pendiente aparte: el estado de carga de react-query ya deshabilita el boton.</p>
 */
export const useCloneBackup = () => {
  const api = getBackups();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async ({ id, baseDestino }: { id: number; baseDestino: string }) => {
      return (await api.clonarBackup(id, { baseDestino })) as ResultadoClon;
    },
    onSuccess: (resultado) => {
      // El clon no altera el historial, pero el panel muestra el conteo de bases vivas.
      queryClient.invalidateQueries({ queryKey: ["backups"] });
      toast.success(`Copia restaurada en "${resultado.baseDestino}"`, {
        description: "La base en uso no se modifico. Conectate a la nueva para inspeccionarla.",
        duration: 10000,
      });
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "No se pudo restaurar la copia de seguridad"), {
        duration: 10000,
      });
    },
  });
};
