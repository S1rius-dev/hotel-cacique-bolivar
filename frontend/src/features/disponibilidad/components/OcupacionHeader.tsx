import { useState } from "react";
import { CalendarDays, ChevronLeft, ChevronRight, Clock } from "lucide-react";
import { useTurnoActivo } from "@/features/turnos/hooks/useTurnoActivo";
import { turnoDesdeDTO } from "@/features/turnos";
import AbrirTurnoModal from "@/features/turnos/components/AbrirTurnoModal";
import CerrarTurnoModal from "@/features/turnos/components/CerrarTurnoModal";
import { formatDate } from "@/utils/date";

function shortTipoName(name: string) {
  return name.replace(/^Habitación /i, "");
}

interface Props {
  selectedDate: Date;
  calendarOpen: boolean;
  onToggleCalendar: () => void;
  stats: {
    disponibles: number;
    ocupadas: number;
    reservadas: number;
    enLimpieza: number;
  };
  tipos: string[];
  tipoFilter: string;
  onTipoChange: (tipo: string) => void;
  readOnly?: boolean;
}

function OcupacionHeader({
  selectedDate,
  calendarOpen,
  onToggleCalendar,
  stats,
  tipos,
  tipoFilter,
  onTipoChange,
  readOnly,
}: Props) {
  const { data: turnoData } = useTurnoActivo();
  const [showAbrir, setShowAbrir] = useState(false);
  const [showCerrar, setShowCerrar] = useState(false);

  const turnoActual = turnoDesdeDTO(turnoData);
  const dateStr = formatDate(selectedDate);

  const handleTurnoClick = () => {
    if (turnoData && turnoActual) {
      setShowCerrar(true);
    } else {
      setShowAbrir(true);
    }
  };

  return (
    <>
      <div className="flex flex-wrap items-center justify-between gap-2 px-4 md:px-6 py-3 border-b border-zinc-100 dark:border-zinc-800 bg-white dark:bg-zinc-900/40">
        <div className="flex items-center gap-3 min-w-0">
          <button
            onClick={onToggleCalendar}
            className="p-1.5 rounded-lg hover:bg-zinc-100 dark:hover:bg-zinc-800 transition-colors shrink-0"
            title={calendarOpen ? "Ocultar calendario" : "Mostrar calendario"}>
            {calendarOpen ? (
              <ChevronLeft className="w-4 h-4 text-muted-foreground" />
            ) : (
              <ChevronRight className="w-4 h-4 text-muted-foreground" />
            )}
          </button>

          <CalendarDays className="w-4 h-4 text-muted-foreground shrink-0" />

          <p className="text-sm text-zinc-700 dark:text-zinc-300 capitalize truncate">
            {dateStr}
          </p>

          {readOnly && (
            <span className="shrink-0 inline-flex items-center gap-1 text-[11px] px-2 py-0.5 rounded-full border font-medium bg-zinc-100 text-zinc-500 border-zinc-200 dark:bg-zinc-800 dark:text-zinc-400 dark:border-zinc-700">
              Solo consulta
            </span>
          )}
        </div>

        <div className="flex items-center gap-1.5 shrink-0 overflow-x-auto max-w-full">
          <button
            onClick={() => onTipoChange("")}
            className={`px-3 py-1 rounded-full text-xs font-medium transition-colors whitespace-nowrap ${
              tipoFilter === ""
                ? "bg-rose-800/90 text-white"
                : "bg-zinc-100 dark:bg-zinc-800 text-muted-foreground hover:bg-zinc-200 dark:hover:bg-zinc-700"
            }`}>
            Todas
          </button>
          {tipos.map((tipo) => (
            <button
              key={tipo}
              onClick={() => onTipoChange(tipoFilter === tipo ? "" : tipo)}
              className={`px-3 py-1 rounded-full text-xs font-medium transition-colors whitespace-nowrap ${
                tipoFilter === tipo
                  ? "bg-rose-800/90 text-white"
                  : "bg-zinc-100 dark:bg-zinc-800 text-muted-foreground hover:bg-zinc-200 dark:hover:bg-zinc-700"
              }`}>
              {shortTipoName(tipo)}
            </button>
          ))}
        </div>

        <div className="flex items-center gap-2 md:gap-3 text-[11px] md:text-xs text-muted-foreground shrink-0 flex-wrap">
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-emerald-500" />
            {stats.disponibles} disponibles
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-amber-500" />
            {stats.ocupadas} ocupadas
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-purple-500" />
            {stats.reservadas} reservadas
          </span>
          <span className="flex items-center gap-1.5">
            <span className="w-2 h-2 rounded-full bg-sky-500" />
            {stats.enLimpieza} en limpieza
          </span>

          <button
            type="button"
            onClick={handleTurnoClick}
            className="hidden lg:inline-flex items-center gap-1.5 ml-2 pl-3 border-l border-zinc-200 dark:border-zinc-700 text-zinc-500 dark:text-zinc-400 hover:text-zinc-700 dark:hover:text-zinc-200 transition-colors cursor-pointer">
            <Clock className="size-3.5" />
            {turnoActual
              ? `${turnoActual.info.label} (${turnoActual.info.inicio} – ${turnoActual.info.fin})`
              : "Sin turno activo"}
          </button>
        </div>
      </div>

      <AbrirTurnoModal open={showAbrir} onOpenChange={setShowAbrir} />
      {turnoData && (
        <CerrarTurnoModal
          open={showCerrar}
          onOpenChange={setShowCerrar}
          turno={turnoData}
        />
      )}
    </>
  );
}

export default OcupacionHeader;
