import { useState } from "react";
import { useSearchParams, useNavigate } from "react-router-dom";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { useEmpleados, useDeleteEmpleado } from "../hooks/useEmpleados";
import { empleadoColumns } from "../components/empleadoColumns";
import type { EmpleadoResponse } from "@/api/generated/openAPIDefinition.schemas";
import ConfirmDialog from "@/components/ui/confirm-dialog";
import { Users } from "lucide-react";

const DEFAULT_SIZE = 14;

export default function EmpleadosTablePage() {
  const [searchParams, setSearchParams] = useSearchParams();
  const navigate = useNavigate();
  const page = parseInt(searchParams.get("page") ?? "0", 10);

  const { data, isLoading } = useEmpleados(page, DEFAULT_SIZE);
  const deleteMutation = useDeleteEmpleado();

  const [deleteTarget, setDeleteTarget] = useState<EmpleadoResponse | null>(null);

  const empleados = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;

  const handleDelete = async () => {
    if (!deleteTarget?.id) return;
    try {
      await deleteMutation.mutateAsync(deleteTarget.id);
      setDeleteTarget(null);
    } catch { /* handled */ }
  };

  return (
    <>
      <ManagementTablePage
        title="Empleados"
        titleIcon={<Users className="w-6 h-6 text-rose-600" />}
        subtitle="Gestiona el personal del hotel"
        data={empleados}
        columns={empleadoColumns}
        loading={isLoading}
        currentPage={page + 1}
        totalPages={totalPages}
        totalItems={totalElements}
        pageSize={DEFAULT_SIZE}
        createLabel="Nuevo Empleado"
        onCreate={() => navigate("/dashboard/empleados/nuevo")}
        onPageChange={(p) => setSearchParams({ page: String(p - 1) })}
        onRowClick={(emp) => navigate(`/dashboard/empleados/${emp.id}/editar`)}
      />
      <ConfirmDialog
        open={!!deleteTarget}
        onClose={() => setDeleteTarget(null)}
        title="Eliminar empleado"
        description={`¿Estás seguro de eliminar a ${deleteTarget?.nombres} ${deleteTarget?.apellidos}?`}
        onConfirm={handleDelete}
        isPending={deleteMutation.isPending}
      />
    </>
  );
}
