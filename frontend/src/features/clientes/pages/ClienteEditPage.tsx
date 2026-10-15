import { UserCog, AlertTriangle } from "lucide-react";
import { useParams } from "react-router-dom";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { FormCard } from "@/components/form/FormCard";
import { Tabs, TabsList, TabsTrigger, TabsContent } from "@/components/ui/tabs";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useDetailsCliente } from "../hooks/useClientes";
import EditClientForm from "../components/EditClientForm";
import ClienteAlertsEditSection from "../components/details/ClienteAlertsEditSection";
import { Badge } from "@/components/ui/badge";
import { useAlertasPorCliente } from "@/features/alertas/hooks/useAlertasPorCliente";

function ClienteEditPage() {
  const { id } = useParams();
  const { data: cliente, isLoading } = useDetailsCliente(id);
  const { data: alertasData } = useAlertasPorCliente(cliente?.id);
  const alertas = alertasData?.content ?? [];

  if (isLoading) {
    return (
      <BackHeaderLayout
        title="Editar Cliente"
        subtitle="Actualizando información del cliente">
        <DetailSkeleton cards={1} />
      </BackHeaderLayout>
    );
  }

  if (!cliente) {
    return (
      <BackHeaderLayout title="Editar Cliente" subtitle="Cliente no encontrado">
        <NotFoundState
          title="Cliente no encontrado"
          description="El cliente que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  const fullName = [cliente.nombreCompleto, cliente.apellidoCompleto]
    .filter(Boolean)
    .join(" ");

  return (
    <BackHeaderLayout
      title={`Editar: ${fullName}`}
      subtitle="Actualiza la información del cliente">
      <Tabs defaultValue="datos" className="space-y-6">
        <TabsList
          className="
            flex gap-1 rounded-xl
            border border-zinc-200 dark:border-zinc-800
            bg-white/50 dark:bg-zinc-900/50
            p-1 shadow-sm
          ">
          <TabsTrigger
            value="datos"
            className="
              rounded-lg px-4 py-2 text-sm font-medium
              transition-all duration-200
              text-muted-foreground
              hover:text-rose-800
              data-[state=active]:bg-rose-800
              data-[state=active]:text-white
              data-[state=active]:shadow-md
            ">
            <UserCog className="w-4 h-4 mr-2" />
            Datos del cliente
          </TabsTrigger>

          <TabsTrigger
            value="alertas"
            className="
              rounded-lg px-4 py-2 text-sm font-medium
              transition-all duration-200
              text-muted-foreground
              hover:text-rose-800
              data-[state=active]:bg-rose-800
              data-[state=active]:text-white
              data-[state=active]:shadow-md
              relative
            ">
            <AlertTriangle className="w-4 h-4 mr-2" />
            Alertas
            {alertas.length > 0 && (
              <Badge
                variant="outline"
                className="ml-2 text-xs border-rose-300 text-rose-700 dark:border-rose-600 dark:text-rose-400 data-[state=active]/trigger:border-rose-200 data-[state=active]/trigger:text-rose-200">
                {alertas.length}
              </Badge>
            )}
          </TabsTrigger>
        </TabsList>

        <TabsContent value="datos">
          <FormCard
            title="Información del cliente"
            description="Edita los datos del cliente (la cédula no es modificable)"
            icon={<UserCog className="h-5 w-5 text-rose-700" />}>
            <EditClientForm cliente={cliente} />
          </FormCard>
        </TabsContent>

        <TabsContent value="alertas">
          {cliente.id && <ClienteAlertsEditSection clienteId={cliente.id} />}
        </TabsContent>
      </Tabs>
    </BackHeaderLayout>
  );
}

export default ClienteEditPage;
