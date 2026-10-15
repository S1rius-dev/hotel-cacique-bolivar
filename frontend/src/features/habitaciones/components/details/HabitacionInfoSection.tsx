import {
  BedDouble,
  Tag,
  DollarSign,
  Users,
  FileText,
  Info,
} from "lucide-react";
import { Badge } from "@/components/ui/badge";
import type { HabitacionResponse } from "@/api/generated/openAPIDefinition.schemas";
import { formatCurrency } from "@/utils/format";

interface Props {
  habitacion: HabitacionResponse;
}

const estadoStyles: Record<string, { label: string; className: string }> = {
  DISPONIBLE: {
    label: "Disponible",
    className: "bg-green-100 text-green-700 border-green-200",
  },
  RESERVADA: {
    label: "Reservada",
    className: "bg-yellow-100 text-yellow-700 border-yellow-200",
  },
  OCUPADA: {
    label: "Ocupada",
    className: "bg-red-100 text-red-700 border-red-200",
  },
  EN_LIMPIEZA: {
    label: "En limpieza",
    className: "bg-sky-100 text-sky-700 border-sky-200",
  },
  MANTENIMIENTO: {
    label: "Mantenimiento",
    className: "bg-gray-100 text-gray-700 border-gray-200",
  },
  INHABILITADA: {
    label: "Inhabilitada",
    className: "bg-zinc-100 text-zinc-700 border-zinc-200",
  },
};

export default function HabitacionInfoSection({ habitacion }: Props) {
  const estado =
    estadoStyles[habitacion.estado as string] ?? {
      label: habitacion.estado ?? "Desconocido",
      className: "bg-zinc-100 text-zinc-700 border-zinc-200",
    };

  const fields = [
    { label: "Número de habitación", value: habitacion.numero, icon: BedDouble },
    { label: "Tipo de habitación", value: habitacion.tipo?.nombre, icon: Tag },
    {
      label: "Precio base",
      value: habitacion.tipo?.precioBase
        ? `${formatCurrency(habitacion.tipo.precioBase)} COP`
        : null,
      icon: DollarSign,
    },
    {
      label: "Capacidad máxima",
      value: habitacion.tipo?.capacidadMaxima
        ? `${habitacion.tipo.capacidadMaxima} Personas`
        : null,
      icon: Users,
    },
  ];

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-sky-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <Info className="h-5 w-5 text-sky-600" />
        Información de la habitación
      </h3>

      <div className="flex items-center gap-2 mb-4">
        <span className="text-sm text-muted-foreground">Estado:</span>
        <Badge className={estado.className}>{estado.label}</Badge>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-4">
        {fields.map(
          (field) =>
            field.value !== null &&
            field.value !== undefined && (
              <div key={field.label} className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <field.icon className="w-3.5 h-3.5 text-sky-500 shrink-0" />
                  {field.label}
                </p>
                <p className="font-medium text-zinc-900 dark:text-zinc-100 break-words">
                  {field.value}
                </p>
              </div>
            ),
        )}
      </div>

      {habitacion.descripcion && (
        <div className="mt-4 pt-4 border-t border-border/50">
          <p className="text-sm text-muted-foreground flex items-center gap-1.5 mb-1">
            <FileText className="w-3.5 h-3.5 text-sky-500 shrink-0" />
            Descripción
          </p>
          <p className="text-sm text-zinc-900 dark:text-zinc-100 whitespace-pre-wrap break-words leading-relaxed">
            {habitacion.descripcion}
          </p>
        </div>
      )}
    </section>
  );
}
