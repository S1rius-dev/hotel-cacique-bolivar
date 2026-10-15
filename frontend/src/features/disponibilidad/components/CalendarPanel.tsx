import { X } from "lucide-react";
import { Calendar } from "@/components/ui/calendar";
import { es } from "date-fns/locale";

interface Props {
  selectedDate: Date;
  onDateChange: (date: Date | undefined) => void;
  onClose: () => void;
  showCloseButton?: boolean;
}

function CalendarPanel({ selectedDate, onDateChange, onClose, showCloseButton = true }: Props) {
  return (
    <div className="w-full bg-white dark:bg-zinc-900/60 p-4 flex flex-col gap-4">
      <div className="flex items-center justify-between">
        <p className="text-xs font-semibold uppercase tracking-wide text-muted-foreground">
          Calendario
        </p>
        {showCloseButton && (
          <button
            onClick={onClose}
            className="p-1 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors">
            <X className="w-3.5 h-3.5 text-muted-foreground" />
          </button>
        )}
      </div>

      <Calendar
        mode="single"
        selected={selectedDate}
        onSelect={onDateChange}
        locale={es}
        className="mx-auto"
      />

      <div className="pt-2 border-t border-zinc-100 dark:border-zinc-800 space-y-1.5">
        <p className="text-[11px] text-muted-foreground">Referencias</p>
        <div className="space-y-1">
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-emerald-500" />
            <span className="text-muted-foreground">Disponible</span>
          </div>
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-amber-500" />
            <span className="text-muted-foreground">Ocupada</span>
          </div>
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-purple-500" />
            <span className="text-muted-foreground">Reservada</span>
          </div>
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-zinc-400" />
            <span className="text-muted-foreground">Mantenimiento</span>
          </div>
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-red-500" />
            <span className="text-muted-foreground">Inhabilitada</span>
          </div>
          <div className="flex items-center gap-2 text-xs">
            <span className="w-2.5 h-2.5 rounded-full bg-sky-500" />
            <span className="text-muted-foreground">En limpieza</span>
          </div>
        </div>
      </div>
    </div>
  );
}

export default CalendarPanel;
