import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { alertaSchema, type AlertaFormValues } from "../schema/alert.schema";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import TipoAlertaField from "../fields/TipoAlertaField";
import MensajeAlertaField from "../fields/MensajeField";
import { AlertTriangle } from "lucide-react";
import { useCreateAlert } from "../hooks/useCreateAlert";

interface Props {
  clienteId: number;
  onClose?: () => void;
}

function AlertForm({ clienteId, onClose }: Props) {
  const form = useForm<AlertaFormValues>({
    resolver: zodResolver(alertaSchema),
    mode: "onChange",
    defaultValues: {
      clienteId,
      tipo: undefined,
      mensaje: "",
    },
  });

  const createAlert = useCreateAlert(form, clienteId);

  const onSubmit = (data: AlertaFormValues) => {
    createAlert.mutate(data);
  };

  return (
    <Card className="p-5 space-y-5 shadow-xl border bg-background">
      {/* CONTEXTO */}
      <div className="rounded-lg border bg-muted/40 px-3 py-2">
        <div className="flex items-center gap-3">
          {/* icono */}
          <div className="p-1.5 rounded-md bg-rose-500/10 text-rose-600">
            <AlertTriangle className="w-4 h-4" />
          </div>

          {/* texto */}
          <div className="leading-tight">
            <p className="text-[11px] uppercase tracking-wide text-muted-foreground">
              Creando alerta
            </p>

            <p className="text-sm font-semibold">Cliente #{clienteId}</p>
          </div>
        </div>
      </div>

      <FormProvider {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
          <TipoAlertaField />
          <MensajeAlertaField />

          <div className="flex justify-end gap-2 pt-2">
            <Button
              type="button"
              variant="ghost"
              onClick={onClose}
              className="text-muted-foreground hover:text-foreground">
              Cancelar
            </Button>

            <Button type="submit">Crear alerta</Button>
          </div>
        </form>
      </FormProvider>
    </Card>
  );
}

export default AlertForm;
