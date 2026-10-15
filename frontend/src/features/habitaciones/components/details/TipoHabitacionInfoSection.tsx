import {
  LayoutGrid,
  Users,
  DollarSign,
  FileText,
  Info,
} from "lucide-react";
import type { TipoHabitacionResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatCurrency } from "@/utils/format";

interface Props {
  tipoHabitacion: TipoHabitacionResponse;
}

export default function TipoHabitacionInfoSection({ tipoHabitacion }: Props) {
  const fields = [
    { label: "Nombre", value: tipoHabitacion.nombre, icon: LayoutGrid },
    {
      label: "Capacidad máxima",
      value: tipoHabitacion.capacidadMaxima
        ? `${tipoHabitacion.capacidadMaxima} Personas`
        : null,
      icon: Users,
    },
    {
      label: "Precio base",
      value: tipoHabitacion.precioBase
        ? `${formatCurrency(tipoHabitacion.precioBase)} COP`
        : null,
      icon: DollarSign,
    },
    {
      label: "Precio persona extra",
      value: tipoHabitacion.precioPersonaExtra
        ? `${formatCurrency(tipoHabitacion.precioPersonaExtra)} COP`
        : null,
      icon: DollarSign,
    },
  ];

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-violet-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <Info className="h-5 w-5 text-violet-600" />
        Información del tipo
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-4">
        {fields.map(
          (field) =>
            field.value !== null &&
            field.value !== undefined && (
              <div key={field.label} className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <field.icon className="w-3.5 h-3.5 text-violet-500 shrink-0" />
                  {field.label}
                </p>
                <p className="font-medium text-zinc-900 dark:text-zinc-100 break-words">
                  {field.value}
                </p>
              </div>
            ),
        )}
      </div>

      {tipoHabitacion.descripcion && (
        <div className="mt-4 pt-4 border-t border-border/50">
          <p className="text-sm text-muted-foreground flex items-center gap-1.5 mb-1">
            <FileText className="w-3.5 h-3.5 text-violet-500 shrink-0" />
            Descripción
          </p>
          <p className="text-sm text-zinc-900 dark:text-zinc-100 whitespace-pre-wrap break-words leading-relaxed">
            {tipoHabitacion.descripcion}
          </p>
        </div>
      )}
    </section>
  );
}
