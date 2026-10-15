import {
  CalendarCheck,
  CalendarClock,
  CalendarRange,
  Clock,
  Hourglass,
  Save,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Switch } from "@/components/ui/switch";
import type { OnlineReservationRules } from "../types";

interface Props {
  rules: OnlineReservationRules;
  dirty: boolean;
  onUpdateRule: (
    key: keyof OnlineReservationRules,
    value: number | string | boolean,
  ) => void;
  onSave: () => void;
}

interface RowProps {
  icon: React.ElementType;
  title: string;
  description: string;
  children: React.ReactNode;
}

function RuleRow({ icon: Icon, title, description, children }: RowProps) {
  return (
    <div className="flex items-center gap-3 px-5 py-3">
      <div className="flex size-8 shrink-0 items-center justify-center rounded-lg bg-zinc-100 dark:bg-zinc-800">
        <Icon className="size-4 text-muted-foreground" />
      </div>
      <div className="min-w-0 flex-1">
        <p className="text-sm font-medium text-foreground">{title}</p>
        <p className="text-xs text-muted-foreground">{description}</p>
      </div>
      <div className="flex shrink-0 items-center gap-2">{children}</div>
    </div>
  );
}

export function ReservationBookingRules({
  rules,
  dirty,
  onUpdateRule,
  onSave,
}: Props) {
  return (
    <section className="flex h-full flex-col overflow-hidden rounded-xl border border-zinc-200 bg-card shadow-sm dark:border-zinc-800">
      <div className="border-b border-zinc-100 px-5 py-4 dark:border-zinc-800">
        <h3 className="text-sm font-semibold text-zinc-900 dark:text-zinc-100">
          Configuración general
        </h3>
        <p className="mt-0.5 text-xs text-muted-foreground">
          Define las reglas y políticas para las reservas online.
        </p>
      </div>

      <div className="flex-1 divide-y divide-zinc-100 dark:divide-zinc-800">
        <RuleRow
          icon={CalendarClock}
          title="Anticipación mínima"
          description="Días mínimos antes de la fecha de ingreso"
        >
          <Input
            type="number"
            min={1}
            value={rules.minAnticipationDays}
            onChange={(e) =>
              onUpdateRule("minAnticipationDays", Number(e.target.value))
            }
            className="h-8 w-16 text-center text-xs"
          />
          <span className="text-xs text-muted-foreground">
            día{rules.minAnticipationDays !== 1 ? "s" : ""}
          </span>
        </RuleRow>

        <RuleRow
          icon={CalendarRange}
          title="Anticipación máxima"
          description="Días máximos para realizar una reserva"
        >
          <Input
            type="number"
            min={1}
            max={365}
            value={rules.maxAnticipationDays}
            onChange={(e) =>
              onUpdateRule("maxAnticipationDays", Number(e.target.value))
            }
            className="h-8 w-16 text-center text-xs"
          />
          <span className="text-xs text-muted-foreground">días</span>
        </RuleRow>

        <RuleRow
          icon={Clock}
          title="Hora límite para reservar"
          description="Hora máxima para realizar reservas"
        >
          <Input
            type="time"
            value={rules.hourLimit}
            onChange={(e) => onUpdateRule("hourLimit", e.target.value)}
            className="h-8 w-28 text-xs"
          />
        </RuleRow>

        <RuleRow
          icon={CalendarCheck}
          title="Permitir reservas para el mismo día"
          description="Los huéspedes pueden reservar para hoy"
        >
          <Switch
            checked={rules.allowSameDay}
            onCheckedChange={(value) => onUpdateRule("allowSameDay", value)}
            className="cursor-pointer"
          />
        </RuleRow>

        <RuleRow
          icon={Hourglass}
          title="Expiración de reserva pendiente"
          description="Tiempo para confirmar una reserva pendiente"
        >
          <Input
            type="number"
            min={1}
            max={1440}
            value={rules.pendingExpirationMinutes}
            onChange={(e) =>
              onUpdateRule("pendingExpirationMinutes", Number(e.target.value))
            }
            className="h-8 w-16 text-center text-xs"
          />
          <span className="text-xs text-muted-foreground">min</span>
        </RuleRow>
      </div>

      <div className="flex justify-end border-t border-zinc-100 bg-zinc-50/50 px-5 py-3 dark:border-zinc-800 dark:bg-zinc-900/50">
        <Button
          variant="hotel"
          onClick={onSave}
          disabled={!dirty}
          className="cursor-pointer"
        >
          <Save className="size-4" />
          Guardar cambios
        </Button>
      </div>
    </section>
  );
}