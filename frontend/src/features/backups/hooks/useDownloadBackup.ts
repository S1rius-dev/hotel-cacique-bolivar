import axios from "axios";
import { toast } from "sonner";
import { getApiUrl } from "@/api/api-config";
import { extractErrorMessage } from "@/lib/extract-error";

const API_URL = getApiUrl();

/**
 * Descarga el .sql.gz de una copia de seguridad.
 *
 * <p>No usa el cliente de Orval a proposito: el endpoint devuelve un stream binario y
 * `StreamingResponseBody` no expresa `responseType: "blob"`. El patron replica
 * `features/registros/hooks/useExportGuestRecords.ts`.</p>
 *
 * <p>El .sql.gz contiene la base completa: datos personales de huespedes y saldos de caja.
 * Solo se ofrece cuando la copia esta EXITOSO, porque un FALLIDO deja un archivo parcial
 * que no restaura.</p>
 */
export const useDownloadBackup = () => {
  const descargar = async (id: number, nombreArchivo: string) => {
    try {
      const response = await axios.get(`${API_URL}/api/backups/${id}/download`, {
        withCredentials: true,
        responseType: "blob",
      });

      const contentType = response.headers["content-type"] as string | undefined;
      const blob = new Blob([response.data], {
        type: contentType ?? "application/gzip",
      });

      const url = URL.createObjectURL(blob);
      const link = document.createElement("a");
      link.href = url;
      link.download = nombreArchivo;
      document.body.appendChild(link);
      link.click();
      document.body.removeChild(link);
      URL.revokeObjectURL(url);
    } catch (error) {
      toast.error(extractErrorMessage(error, "No se pudo descargar la copia de seguridad"));
    }
  };

  return { descargar };
};
