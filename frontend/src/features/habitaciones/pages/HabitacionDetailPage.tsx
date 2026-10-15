import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import DetailPageLayout from "@/layouts/dashboard/DetailPageLayout";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useParams, useNavigate } from "react-router-dom";
import { useHabitacion } from "../hooks/useRoom";
import HabitacionInfoHeader from "../components/details/HabitacionInfoHeader";
import HabitacionInfoSection from "../components/details/HabitacionInfoSection";
import { useState } from "react";
import { useDeleteHabitacion } from "../hooks/useDeleteHabitacion";

function HabitacionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: habitacion, isLoading } = useHabitacion(
    id ? Number(id) : undefined,
  );
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const deleteHabitacion = useDeleteHabitacion();

  const handleEdit = () => {
    navigate(`/dashboard/habitaciones/${id}/editar`);
  };

  const handleDelete = () => {
    if (id) {
      deleteHabitacion.mutate(Number(id), {
        onSuccess: () => {
          navigate("/dashboard/habitaciones");
        },
      });
    }
  };

  if (isLoading) {
    return (
      <BackHeaderLayout title="Habitaciones" subtitle="Detalle de habitación">
        <DetailSkeleton />
      </BackHeaderLayout>
    );
  }

  if (!habitacion) {
    return (
      <BackHeaderLayout title="Habitaciones" subtitle="Detalle de habitación">
        <NotFoundState
          title="Habitación no encontrada"
          description="La habitación que buscas no existe o fue eliminada."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout title="Habitaciones" subtitle="Detalle de habitación">
      <DetailPageLayout
        onEdit={handleEdit}
        onDelete={() => setShowDeleteDialog(true)}
        identity={
          <HabitacionInfoHeader
            numero={habitacion.numero ?? ""}
            tipoNombre={habitacion.tipo?.nombre}
          />
        }>
        <HabitacionInfoSection habitacion={habitacion} />
      </DetailPageLayout>

      <ConfirmDeleteDialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={handleDelete}
        isPending={deleteHabitacion.isPending}
        title="Eliminar habitación"
        description={`¿Estás seguro de eliminar la habitación "${habitacion.numero}"? Esta acción no se puede deshacer.`}
      />
    </BackHeaderLayout>
  );
}

export default HabitacionDetailPage;
