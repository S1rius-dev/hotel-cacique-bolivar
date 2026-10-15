import { LayoutGrid } from "lucide-react";
import { useParams } from "react-router-dom";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { FormCard } from "@/components/form/FormCard";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useTipoHabitacion } from "../hooks/useRoom";
import EditTipoHabitacionForm from "../components/EditTipoHabitacionForm";

function TipoHabitacionEditPage() {
  const { id } = useParams();
  const { data: tipoHabitacion, isLoading } = useTipoHabitacion(id ? Number(id) : undefined);

  if (isLoading) {
    return (
      <BackHeaderLayout
        title="Editar Tipo de Habitación"
        subtitle="Actualizando información del tipo">
        <DetailSkeleton cards={1} />
      </BackHeaderLayout>
    );
  }

  if (!tipoHabitacion) {
    return (
      <BackHeaderLayout
        title="Editar Tipo de Habitación"
        subtitle="Tipo de habitación no encontrado">
        <NotFoundState
          title="Tipo de habitación no encontrado"
          description="El tipo de habitación que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout
      title={`Editar: ${tipoHabitacion.nombre}`}
      subtitle="Actualiza la información del tipo de habitación">
      <FormCard
        title="Información del tipo de habitación"
        description="Edita las características y precios del tipo"
        icon={<LayoutGrid className="h-5 w-5 text-rose-700" />}>
        <EditTipoHabitacionForm tipoHabitacion={tipoHabitacion} />
      </FormCard>
    </BackHeaderLayout>
  );
}

export default TipoHabitacionEditPage;