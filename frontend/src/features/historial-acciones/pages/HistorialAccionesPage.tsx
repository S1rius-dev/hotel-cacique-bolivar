import { useState } from "react";
import { useAuditLogs } from "@/features/historial-acciones/hooks/useAuditLogs";
import { auditLogColumns } from "@/features/historial-acciones/components/auditLogColumns";
import AuditLogDetailModal from "@/features/historial-acciones/components/AuditLogDetailModal";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { useSearchParams } from "react-router-dom";
import type { AuditLogResponse } from "@/api/generated/openAPIDefinition.schemas";

const DEFAULT_SIZE = 15;

const HistorialAccionesPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const size = parseInt(searchParams.get("size") ?? String(DEFAULT_SIZE), 10);

  const { data, isLoading } = useAuditLogs(page, size);

  const [selectedLog, setSelectedLog] = useState<AuditLogResponse | null>(null);

  const logs = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;
  const currentPage = data?.page ?? 0;

  return (
    <>
      <ManagementTablePage
        title="Historial de Acciones"
        data={logs}
        columns={auditLogColumns}
        loading={isLoading}
        currentPage={currentPage + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={pageSize}
        onPageChange={(p) => setSearchParams({ page: String(p - 1), size: String(pageSize) })}
        onRowClick={(log) => setSelectedLog(log)}
      />
      <AuditLogDetailModal
        log={selectedLog}
        open={selectedLog != null}
        onClose={() => setSelectedLog(null)}
      />
    </>
  );
};

export default HistorialAccionesPage;
