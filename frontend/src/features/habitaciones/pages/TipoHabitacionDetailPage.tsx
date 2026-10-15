import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import DetailPageLayout from "@/layouts/dashboard/DetailPageLayout";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useParams, useNavigate } from "react-router-dom";
import { useTipoHabitacion } from "../hooks/useRoom";
import TipoHabitacionInfoHeader from "../components/details/TipoHabitacionInfoHeader";
import TipoHabitacionInfoSection from "../components/details/TipoHabitacionInfoSection";
import { useState } from "react";
import { useDeleteTipoHabitacion } from "../hooks/useDeleteTipoHabitacion";

import PrecioTemporalDialog from "../components/PrecioTemporalDialog";
import PreciosTemporalesSection from "../components/details/PreciosTemporalesSection";

function TipoHabitacionDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: tipoHabitacion, isLoading } = useTipoHabitacion(
    id ? Number(id) : undefined,
  );
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showTemporalPriceDialog, setShowTemporalPriceDialog] = useState(false);

  const deleteTipoHabitacion = useDeleteTipoHabitacion();

  const handleEdit = () => {
    navigate(`/dashboard/habitaciones/tipos/${id}/editar`);
  };

  const handleDelete = () => {
    if (id) {
      deleteTipoHabitacion.mutate(Number(id), {
        onSuccess: () => {
          navigate("/dashboard/habitaciones");
        },
      });
    }
  };

  if (isLoading) {
    return (
      <BackHeaderLayout title="Tipos de habitación" subtitle="Detalle del tipo">
        <DetailSkeleton />
      </BackHeaderLayout>
    );
  }

  if (!tipoHabitacion) {
    return (
      <BackHeaderLayout title="Tipos de habitación" subtitle="Detalle del tipo">
        <NotFoundState
          title="Tipo de habitación no encontrado"
          description="El tipo de habitación que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout title="Tipos de habitación" subtitle="Detalle del tipo">
      <DetailPageLayout
        onEdit={handleEdit}
        onDelete={() => setShowDeleteDialog(true)}
        identity={
          <TipoHabitacionInfoHeader
            nombre={tipoHabitacion.nombre ?? ""}
            capacidad={tipoHabitacion.capacidadMaxima}
          />
        }>
        <TipoHabitacionInfoSection tipoHabitacion={tipoHabitacion} />
        {id && (
          <PreciosTemporalesSection
            tipoId={Number(id)}
            onAdd={() => setShowTemporalPriceDialog(true)}
          />
        )}
      </DetailPageLayout>

      <ConfirmDeleteDialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={handleDelete}
        isPending={deleteTipoHabitacion.isPending}
        title="Eliminar tipo de habitación"
        description={`¿Estás seguro de eliminar el tipo "${tipoHabitacion.nombre}"? Esta acción no se puede deshacer.`}
      />

      {id && (
        <PrecioTemporalDialog
          open={showTemporalPriceDialog}
          onOpenChange={setShowTemporalPriceDialog}
          tipoHabitacionId={Number(id)}
          tipoNombre={tipoHabitacion.nombre ?? undefined}
        />
      )}
    </BackHeaderLayout>
  );
}

export default TipoHabitacionDetailPage;
