import { useQuery } from "@tanstack/react-query";
import { getAuditLogController } from "@/api/generated/audit-log-controller/audit-log-controller";
import type { PageResponseAuditLogResponse } from "@/api/generated/openAPIDefinition.schemas";

export const useAuditLogs = (page = 0, size?: number) => {
  const api = getAuditLogController();

  return useQuery<PageResponseAuditLogResponse>({
    queryKey: ["audit-logs", page, size ?? "default"],
    queryFn: () => {
      const params: { page: number; size?: number } = { page };
      if (size != null && size > 0) params.size = size;
      return api.listarAuditLogs({ pageable: params });
    },
  });
};


