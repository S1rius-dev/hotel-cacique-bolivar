import { CalendarDays, Clock, DollarSign, FileText, Globe, Info, Moon } from "lucide-react";
import { formatCurrency as formatCurrencyUtil } from "@/utils/format";
import { ORIGEN_OPTIONS } from "../../constants/reserva.constants";
import { formatDate, formatDateTimeWithAlas, formatTimeOnly } from "@/utils/date";

interface Props {
  fechaCreacion?: string;
  fechaCheckIn?: string;
  fechaCheckOut?: string;
  horaCheckIn?: string;
  horaCheckOut?: string;
  noches?: number;
  total?: number;
  origen?: string;
  observaciones?: string;
}

function displayCurrency(value?: number) {
  if (value == null) return "—";
  return `${formatCurrencyUtil(value)} COP`;
}

const origenMap: Record<string, string> = Object.fromEntries(
  ORIGEN_OPTIONS.map((o) => [o.value, o.label]),
);

function ReservaGeneralInfo({
  fechaCreacion,
  fechaCheckIn,
  fechaCheckOut,
  horaCheckIn,
  horaCheckOut,
  noches,
  total,
  origen,
  observaciones,
}: Props) {
  const checkInTime = formatTimeOnly(horaCheckIn);
  const checkOutTime = formatTimeOnly(horaCheckOut);
  const fields = [
    { label: "Fecha de creación", value: formatDateTimeWithAlas(fechaCreacion), icon: Clock },
    { label: "Fecha check-in", value: formatDate(fechaCheckIn) + (checkInTime ? ` · ${checkInTime}` : ""), icon: CalendarDays },
    { label: "Fecha check-out", value: formatDate(fechaCheckOut) + (checkOutTime ? ` · ${checkOutTime}` : ""), icon: CalendarDays },
    { label: "Noches", value: noches != null ? `${noches} noche(s)` : "—", icon: Moon },
    { label: "Total", value: displayCurrency(total), icon: DollarSign },
    { label: "Origen", value: origen ? (origenMap[origen] ?? origen) : "—", icon: Globe },
  ];

  return (
    <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm border-l-4 border-l-emerald-500">
      <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
        <Info className="h-5 w-5 text-emerald-600" />
        Información general
      </h3>

      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-x-6 gap-y-4">
        {fields.map(
          (field) =>
            field.value !== "—" && (
              <div key={field.label} className="space-y-1">
                <p className="text-sm text-muted-foreground flex items-center gap-1.5">
                  <field.icon className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
                  {field.label}
                </p>
                <p className="font-medium text-zinc-900 dark:text-zinc-100 break-words">
                  {field.value}
                </p>
              </div>
            ),
        )}
      </div>

      {observaciones && (
        <div className="mt-4 pt-4 border-t border-border/50">
          <p className="text-sm text-muted-foreground flex items-center gap-1.5 mb-1">
            <FileText className="w-3.5 h-3.5 text-emerald-500 shrink-0" />
            Observaciones
          </p>
          <p className="text-sm text-zinc-900 dark:text-zinc-100 whitespace-pre-wrap break-words leading-relaxed">
            {observaciones}
          </p>
        </div>
      )}
    </section>
  );
}

export default ReservaGeneralInfo;
