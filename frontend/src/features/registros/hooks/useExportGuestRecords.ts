import axios from "axios";
import { getApiUrl } from "@/api/api-config";
import type { GuestFilters } from "../components/types";

const API_URL = getApiUrl();

interface UseExportGuestRecordsFilters extends GuestFilters {
  format: "pdf" | "word";
}

function buildQueryString(filters: GuestFilters): string {
  const params = new URLSearchParams();
  if (filters.fechaDesde) params.append("fechaDesde", filters.fechaDesde);
  if (filters.fechaHasta) params.append("fechaHasta", filters.fechaHasta);
  if (filters.habitacion) params.append("habitacion", filters.habitacion);
  if (filters.tipoHabitacionId != null)
    params.append("tipoHabitacionId", String(filters.tipoHabitacionId));
  if (filters.busqueda) params.append("busqueda", filters.busqueda);
  if (filters.estado) params.append("estado", filters.estado);
  return params.toString();
}

function triggerDownload(blob: Blob, filename: string) {
  const url = URL.createObjectURL(blob);
  const link = document.createElement("a");
  link.href = url;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  URL.revokeObjectURL(url);
}

export const useExportGuestRecords = () => {
  const exportDocument = async (filters: UseExportGuestRecordsFilters) => {
    const { format, ...filterParams } = filters;
    const queryString = buildQueryString(filterParams);
    const url = `${API_URL}/api/registros/exportar/${format}${queryString ? `?${queryString}` : ""}`;

    const response = await axios.get(url, {
      withCredentials: true,
      responseType: "blob",
    });

    const extension = format === "pdf" ? "pdf" : "docx";
    const contentType = response.headers["content-type"] as string | undefined;
    const blob = new Blob([response.data], { type: contentType ?? "application/octet-stream" });
    triggerDownload(blob, `registro-huespedes.${extension}`);
  };

  return { exportDocument };
};
