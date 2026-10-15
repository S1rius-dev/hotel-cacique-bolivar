import { useNavigate } from "react-router-dom";
import { motion } from "motion/react";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Loader2, LogIn, PlusCircle, CalendarDays, ExternalLink } from "lucide-react";
import type { UltimaReserva } from "@/api/generated/openAPIDefinition.schemas";
import { parseISO, format } from "date-fns";
import { es } from "date-fns/locale";

interface Props {
  data?: UltimaReserva[];
  isLoading?: boolean;
}

const TIPO_LABELS: Record<string, string> = {
  CREACION: "Creada",
  CHECK_IN: "Check-in",
  CHECK_OUT: "Check-out",
  PAGO: "Pago",
};

const TIPO_STYLES: Record<string, { bg: string; text: string }> = {
  CREACION: { bg: "bg-emerald-100 dark:bg-emerald-900/30", text: "text-emerald-700 dark:text-emerald-400" },
  CHECK_IN: { bg: "bg-sky-100 dark:bg-sky-900/30", text: "text-sky-700 dark:text-sky-400" },
  CHECK_OUT: { bg: "bg-amber-100 dark:bg-amber-900/30", text: "text-amber-700 dark:text-amber-400" },
  PAGO: { bg: "bg-violet-100 dark:bg-violet-900/30", text: "text-violet-700 dark:text-violet-400" },
};

const formatFecha = (fecha?: string) => {
  if (!fecha) return "";
  try {
    const d = parseISO(fecha);
    return format(d, "d MMM · HH:mm", { locale: es });
  } catch {
    return "";
  }
};

export const RecentActivity = ({ data, isLoading }: Props) => {
  const navigate = useNavigate();

  return (
    <Card className="rounded-2xl shadow-sm hover:shadow-md transition-all">
      <CardHeader>
        <CardTitle className="text-base font-semibold">
          Últimas reservas
        </CardTitle>
      </CardHeader>

      <CardContent className="space-y-3">
        {isLoading ? (
          <div className="flex justify-center py-6">
            <Loader2 className="size-5 animate-spin text-muted-foreground" />
          </div>
        ) : data && data.length > 0 ? (
          <div className="space-y-3">
            {data.map((item, i) => {
              const tipoStyle = TIPO_STYLES[item.tipo ?? ""] ?? {
                bg: "bg-zinc-100 dark:bg-zinc-800",
                text: "text-zinc-500",
              };
              const tipoLabel = TIPO_LABELS[item.tipo ?? ""] ?? item.tipo;

              return (
                <motion.div
                  key={item.id ?? i}
                  whileHover={{ x: 4, transition: { duration: 0.15 } }}
                  onClick={() => item.id && navigate(`/dashboard/reservas/${item.id}`)}
                  className="flex items-center gap-4 p-3 rounded-xl hover:bg-muted/50 transition-colors cursor-pointer"
                >
                  <motion.div
                    className={`size-10 rounded-lg flex items-center justify-center shrink-0 ${tipoStyle.bg} ${tipoStyle.text}`}
                    whileHover={{ scale: 1.12, rotate: 4 }}
                    transition={{ type: "spring", stiffness: 300, damping: 14 }}
                  >
                    {item.tipo === "CHECK_IN" ? (
                      <LogIn className="size-4" />
                    ) : (
                      <PlusCircle className="size-4" />
                    )}
                  </motion.div>

                  <div className="min-w-0 flex-1">
                    <div className="flex items-center gap-2">
                      <p className="font-medium text-sm truncate">
                        {item.titularNombre ?? "—"}
                      </p>
                      <span className={`text-[10px] px-1.5 py-0.5 rounded-full font-medium shrink-0 ${tipoStyle.bg} ${tipoStyle.text}`}>
                        {tipoLabel}
                      </span>
                    </div>
                    <div className="flex items-center gap-2 text-xs text-muted-foreground mt-0.5">
                      {item.habitacionNumero && (
                        <span>
                          Hab. {item.habitacionNumero}{item.tipoHabitacion ? ` · ${item.tipoHabitacion}` : ""}
                        </span>
                      )}
                      {item.codigoReserva && (
                        <span className="font-mono text-[10px] bg-zinc-100 dark:bg-zinc-800 px-1 py-0.5 rounded">
                          {item.codigoReserva}
                        </span>
                      )}
                    </div>
                    <div className="flex items-center gap-1 text-[11px] text-muted-foreground/70 mt-0.5">
                      <CalendarDays className="w-3 h-3" />
                      {formatFecha(item.fecha)}
                    </div>
                  </div>

                  <ExternalLink className="w-4 h-4 text-muted-foreground/40 shrink-0" />
                </motion.div>
              );
            })}
          </div>
        ) : (
          <p className="text-sm text-muted-foreground text-center py-6">
            Sin actividad reciente
          </p>
        )}
      </CardContent>
    </Card>
  );
};
