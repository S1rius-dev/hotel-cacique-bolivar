import { useUsers } from "@/features/users/hooks/useUsers";
import type { UserResponse } from "@/api/generated/openAPIDefinition.schemas";
import { userColumns } from "@/features/users/components/userColumns";
import ManagementTablePage from "@/layouts/dashboard/ManagementTablePage";
import { useNavigate, useSearchParams } from "react-router-dom";
import { Shield } from "lucide-react";

const DEFAULT_SIZE = 15;

const UserPage = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const page = parseInt(searchParams.get("page") ?? "0", 10);
  const size = parseInt(searchParams.get("size") ?? String(DEFAULT_SIZE), 10);

  const { data, isLoading } = useUsers(page, size);
  const navigate = useNavigate();

  const handleRowClick = (user: UserResponse) => {
    navigate(`/dashboard/usuarios/${user.id}`);
  };

  const users = data?.content ?? [];
  const totalPages = data?.totalPages ?? 0;
  const totalElements = data?.totalElements ?? 0;
  const pageSize = data?.size ?? 0;
  const currentPage = data?.page ?? 0;

  return (
    <ManagementTablePage
      title="Gestión de Usuarios"
      titleIcon={<Shield className="w-6 h-6 text-rose-600" />}
      subtitle="Administra usuarios del sistema"
      stats={
        isLoading
          ? undefined
          : [{ label: "usuarios", value: totalElements.toLocaleString("es-CO") }]
      }
      data={users}
      columns={userColumns}
      loading={isLoading}
      createLabel="Agregar Usuario"
      onCreate={() => navigate("/dashboard/usuarios/nuevo")}
      onRowClick={handleRowClick}
      currentPage={currentPage + 1}
      totalPages={totalPages}
      totalItems={totalElements}
      pageSize={pageSize}
      onPageChange={(p) => setSearchParams({ page: String(p - 1), size: String(pageSize) })}
    />
  );
};

export default UserPage;
