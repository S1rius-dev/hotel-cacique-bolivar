import { AlertTriangle } from "lucide-react";
import { Button } from "@/components/ui/button";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import DetailPageLayout from "@/layouts/dashboard/DetailPageLayout";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useParams, useNavigate } from "react-router-dom";
import { useDetailsCliente, useDeleteCliente } from "../hooks/useClientes";
import ClienteInfoHeader from "../components/details/ClienteInfoHeader";
import ClienteInfoSection from "../components/details/ClienteInfoSection";
import ClienteAlertsSection from "../components/details/ClienteAlertsSection";
import { getHasActiveAlerts } from "../utils/alerts";
import ClienteReservasSection from "../components/details/ClienteReservasSection";
import { useState } from "react";
import AlertForm from "@/features/alertas/components/AlertForm";
import { useAlertasPorCliente } from "@/features/alertas/hooks/useAlertasPorCliente";

function ClienteDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: client, isLoading } = useDetailsCliente(id);
  const [showAlertForm, setShowAlertForm] = useState(false);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const { data: alertasData, isLoading: isLoadingAlertas } =
    useAlertasPorCliente(client?.id);
  const alertas = alertasData?.content ?? [];
  const deleteCliente = useDeleteCliente();

  const hasActiveAlerts = getHasActiveAlerts(alertas);
  const fullName = [client?.nombreCompleto, client?.apellidoCompleto]
    .filter(Boolean)
    .join(" ");

  const handleEdit = () => {
    navigate(`/dashboard/clientes/${id}/editar`);
  };

  const handleDelete = () => {
    if (client?.id) {
      deleteCliente.mutate(client.id, {
        onSuccess: () => {
          navigate("/dashboard/clientes");
        },
      });
    }
  };

  if (isLoading || isLoadingAlertas) {
    return (
      <BackHeaderLayout title="Clientes" subtitle="Detalle del cliente">
        <DetailSkeleton />
      </BackHeaderLayout>
    );
  }

  if (!client) {
    return (
      <BackHeaderLayout title="Clientes" subtitle="Detalle del cliente">
        <NotFoundState
          title="Cliente no encontrado"
          description="El cliente que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  return (
    <BackHeaderLayout title="Clientes" subtitle="Detalle del cliente">
      <DetailPageLayout
        onEdit={handleEdit}
        onDelete={() => setShowDeleteDialog(true)}
        actions={
          <Button
            size="sm"
            className="gap-2"
            onClick={() => setShowAlertForm(true)}>
            <AlertTriangle className="w-4 h-4" />
            <span className="hidden sm:inline">Añadir alerta</span>
          </Button>
        }
        identity={
          <ClienteInfoHeader name={fullName} hasAlerts={hasActiveAlerts} />
        }>
        <div className="space-y-4">
          <ClienteInfoSection cliente={client} />
          <ClienteAlertsSection alertas={alertas} />
          <ClienteReservasSection reservaIds={client.reservasIds ?? []} />
        </div>
      </DetailPageLayout>

      {/* MODAL FLOTANTE */}
      {showAlertForm && (
        <div className="fixed inset-0 z-50 flex items-center justify-center">
          <div
            className="absolute inset-0 bg-black/40"
            onClick={() => setShowAlertForm(false)}
          />

          <div className="relative w-full max-w-md">
            <AlertForm
              clienteId={client.id!}
              onClose={() => setShowAlertForm(false)}
            />
          </div>
        </div>
      )}

      {/* DIALOGO DE CONFIRMACION */}
      <ConfirmDeleteDialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={handleDelete}
        isPending={deleteCliente.isPending}
        title="Eliminar cliente"
        description={`¿Estás seguro de eliminar al cliente "${fullName}"? Esta acción no se puede deshacer.`}
      />
    </BackHeaderLayout>
  );
}

export default ClienteDetailPage;
