import type { AlertaResponse } from "@/api/generated/openAPIDefinition.schemas";

export function getHasActiveAlerts(alertas: AlertaResponse[]): boolean {
  return alertas.some((a) => a.estado === true);
}
