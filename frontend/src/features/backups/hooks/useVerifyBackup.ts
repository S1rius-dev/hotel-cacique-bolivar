import { useMutation } from "@tanstack/react-query";
import { toast } from "sonner";
import { getBackups } from "@/api/generated/backups/backups";
import { extractErrorMessage } from "@/lib/extract-error";

/**
 * Verifica la integridad de una copia recalculando su SHA-256.
 *
 * <p>No invalida ninguna query: no cambia ningun estado del servidor, solo informa si el
 * archivo sigue intacto. Un checksum discrepante se reporta como error en pantalla y no
 * como excepcion, porque el operador necesita ver el detalle esperado frente al calculado
 * para decidir si descarta la copia.</p>
 */
export const useVerifyBackup = () => {
  const api = getBackups();

  return useMutation({
    mutationFn: async (id: number) => {
      return await api.verificarBackup(id);
    },
    onSuccess: (resultado) => {
      if (resultado.integro) {
        toast.success(`Copia #${resultado.backupId} integra`, {
          description: "El SHA-256 del archivo coincide con el registrado al crearla.",
        });
        return;
      }
      toast.error(`Copia #${resultado.backupId} danada`, {
        description: `Esperado ${resultado.checksumEsperado?.slice(0, 12)}..., `
          + `calculado ${resultado.checksumCalculado?.slice(0, 12)}...`,
        duration: 12000,
      });
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "No se pudo verificar la copia de seguridad"));
    },
  });
};
