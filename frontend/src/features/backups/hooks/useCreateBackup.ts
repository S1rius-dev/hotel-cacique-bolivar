import { useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getBackups } from "@/api/generated/backups/backups";
import { extractErrorMessage } from "@/lib/extract-error";
import type { ApiError } from "@/api/custom-instance";

/**
 * Crea una copia de seguridad manual.
 *
 * <p>La peticion es sincrona y bloqueante en el backend: mysqldump puede tardar de 10 a 60
 * segundos. El boton queda deshabilitado durante todo ese tiempo via `isPending`, y el mensaje
 * de error distingue los tres fallos que el operador puede realmente encontrar:</p>
 *
 * <ul>
 *   <li>409 — ya hay una copia en curso (dos mysqldump simultaneos se pelearian el disco)</li>
 *   <li>503 — el sistema esta deshabilitado por configuracion en el servidor</li>
 * </ul>
 */
export const useCreateBackup = () => {
  const api = getBackups();
  const queryClient = useQueryClient();

  return useMutation({
    mutationFn: async () => await api.crearBackup(),
    onSuccess: (data) => {
      queryClient.invalidateQueries({ queryKey: ["backups"] });
      toast.success(
        `Copia creada: ${data.archivoNombre ?? "sin nombre"} (${formatearBytes(data.tamanoBytes)})`,
      );
    },
    onError: (error) => {
      const status = (error as ApiError)?.status;

      if (status === 409) {
        toast.error("Ya hay una copia de seguridad en ejecución. Espera a que termine.");
        return;
      }
      if (status === 503) {
        toast.error("El sistema de copias de seguridad está deshabilitado en el servidor.");
        return;
      }
      toast.error(extractErrorMessage(error, "No se pudo crear la copia de seguridad"));
    },
  });
};

export function formatearBytes(bytes?: number | null): string {
  if (bytes == null) return "—";
  if (bytes === 0) return "0 B";

  const unidades = ["B", "KB", "MB", "GB", "TB"];
  const exp = Math.min(Math.floor(Math.log(bytes) / Math.log(1024)), unidades.length - 1);
  const valor = bytes / Math.pow(1024, exp);
  return `${valor.toFixed(exp === 0 ? 0 : 1)} ${unidades[exp]}`;
}
