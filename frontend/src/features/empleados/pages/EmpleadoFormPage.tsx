import { useNavigate, useParams } from "react-router-dom";
import {
  useEmpleado,
  useCreateEmpleado,
  useUpdateEmpleado,
} from "../hooks/useEmpleados";
import EmpleadoForm from "../components/EmpleadoForm";
import type { EmpleadoFormValues } from "../schemas/empleado.schema";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";

export default function EmpleadoFormPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const isEditing = !!id;

  const { data: empleado, isLoading } = useEmpleado(
    isEditing ? Number(id) : null,
  );
  const createMutation = useCreateEmpleado();
  const updateMutation = useUpdateEmpleado();

  if (isEditing && isLoading) return <DetailSkeleton />;

  const defaultValues = empleado
    ? {
        nombres: empleado.nombres ?? "",
        apellidos: empleado.apellidos ?? "",
        telefono: empleado.telefono ?? "",
        cargo:
          (empleado.cargo as EmpleadoFormValues["cargo"]) || "RECEPCIONISTA",
        fechaIngreso: empleado.fechaIngreso?.split("T")[0] ?? "",
        activo: empleado.activo ?? true,
      }
    : undefined;

  const handleSubmit = async (data: EmpleadoFormValues) => {
    try {
      if (isEditing) {
        await updateMutation.mutateAsync({ id: Number(id), data });
      } else {
        await createMutation.mutateAsync(data);
      }
      navigate("/dashboard/empleados");
    } catch {
      /* handled by mutation */
    }
  };

  return (
    <BackHeaderLayout
      title="Crear Usuario"
      subtitle="Completa la información del usuario">
      <EmpleadoForm
        defaultValues={defaultValues}
        onSubmit={handleSubmit}
        isPending={createMutation.isPending || updateMutation.isPending}
        submitLabel={isEditing ? "Actualizar empleado" : "Crear empleado"}
      />
    </BackHeaderLayout>
  );
}

