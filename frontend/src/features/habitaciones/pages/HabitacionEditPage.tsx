import { BedDouble } from "lucide-react";
import { useParams } from "react-router-dom";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { FormCard } from "@/components/form/FormCard";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useHabitacion } from "../hooks/useRoom";
import EditHabitacionForm from "../components/EditHabitacionForm";

function HabitacionEditPage() {
  const { id } = useParams();
  const { data: habitacion, isLoading } = useHabitacion(id ? Number(id) : undefined);

  if (isLoading) {
    return (
      <BackHeaderLayout
        title="Editar Habitación"
        subtitle="Actualizando información de la habitación">
        <DetailSkeleton cards={1} />
      </BackHeaderLayout>
    );
  }

  if (!habitacion) {
    return (
      <BackHeaderLayout
        title="Editar Habitación"
        subtitle="Habitación no encontrada">
        <NotFoundState
          title="Habitación no encontrada"
          description="La habitación que buscas no existe o fue eliminada."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout
      title={`Editar: ${habitacion.numero}`}
      subtitle="Actualiza la información de la habitación">
      <FormCard
        title="Información de la habitación"
        description="Edita los datos de la habitación"
        icon={<BedDouble className="h-5 w-5 text-rose-700" />}>
        <EditHabitacionForm habitacion={habitacion} />
      </FormCard>
    </BackHeaderLayout>
  );
}

export default HabitacionEditPage;