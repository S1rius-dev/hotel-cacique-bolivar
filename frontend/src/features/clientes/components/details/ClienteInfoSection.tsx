import {
  User,
  Phone,
  Calendar,
  CreditCard,
  FileText,
  MapPin,
  Info,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatDate } from "@/utils/date";

interface Props {
  cliente: ClienteResponse;
}

export default function ClienteInfoSection({ cliente }: Props) {
  const fullName =
    `${cliente.nombreCompleto ?? ""} ${cliente.apellidoCompleto ?? ""}`.trim();

  const fields = [
    { label: "Tipo de identificación", value: cliente.tipoIdentificacion?.nombre, icon: FileText },
    { label: "Cédula", value: cliente.cedula, icon: CreditCard },
    {
      label: "Ubicación de expedición",
      value: cliente.municipioExpedicion
        ? `${cliente.municipioExpedicion.nombre}`
        : null,
      icon: MapPin,
    },
    { label: "Nombre completo", value: fullName, icon: User },
    { label: "Teléfono", value: cliente.telefono, icon: Phone },
    {
      label: "Fecha de registro",
      value: cliente.createdAt ? formatDate(cliente.createdAt) : null,
      icon: Calendar,
    },
  ];

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-blue-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <Info className="h-5 w-5 text-blue-600" />
        Información general
      </h3>

      <div className="flex items-center gap-2 mb-4">
        <span className="text-sm text-muted-foreground">Estado:</span>
        <Badge
          className={
            cliente.activo
              ? "bg-emerald-50 text-emerald-700 border-emerald-200"
              : "bg-zinc-100 text-zinc-600 border-zinc-200"
          }>
          {cliente.activo ? "Activo" : "Inactivo"}
        </Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-4 min-w-0">
        {fields.map(
          (field) =>
            field.value !== null &&
            field.value !== undefined &&
            field.value !== "" && (
              <div key={field.label} className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <field.icon className="w-3.5 h-3.5 text-blue-500 shrink-0" />
                  {field.label}
                </p>
                <p className="font-medium text-zinc-900 dark:text-zinc-100 break-words">
                  {field.value}
                </p>
              </div>
            ),
        )}
      </div>

      {cliente.observaciones && (
        <div className="mt-4 pt-4 border-t border-border/50">
          <p className="text-sm text-muted-foreground flex items-center gap-1.5 mb-1">
            <FileText className="w-3.5 h-3.5 text-blue-500 shrink-0" />
            Observaciones
          </p>
          <p className="text-sm text-zinc-900 dark:text-zinc-100 whitespace-pre-wrap break-words leading-relaxed">
            {cliente.observaciones}
          </p>
        </div>
      )}
    </section>
  );
}
