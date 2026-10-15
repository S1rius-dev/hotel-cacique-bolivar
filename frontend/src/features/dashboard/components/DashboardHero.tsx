import { motion } from "motion/react";
import { BedDouble, Calendar, DollarSign, Users, Loader2, LogIn, Sparkles, Clock } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import { useAuth } from "@/app/auth/AuthContext";
import { useTurnoDisplay } from "@/features/turnos/hooks/useTurnoActivo";
import type { DashboardResumen } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  data?: DashboardResumen;
  isLoading?: boolean;
}

const greeting = () => {
  const h = new Date().getHours();
  if (h < 6) return "Buenas noches";
  if (h < 12) return "Buenos días";
  if (h < 18) return "Buenas tardes";
  return "Buenas noches";
};

function DashboardHero({ data, isLoading }: Props) {
  const { user } = useAuth();
  const { turno } = useTurnoDisplay();
  const ocupadas = data?.habitacionesOcupadas ?? 0;
  const total = data?.totalHabitaciones ?? 0;
  const pct = total > 0 ? Math.round((ocupadas / total) * 100) : 0;
  const ingresosHoy = data?.ingresosHoy ?? 0;

  const activas = data?.reservasActivas ?? 0;
  const pctActivas = total > 0 ? Math.round((activas / total) * 100) : 0;

  const items = [
    { icon: BedDouble, label: "Ocupadas", value: `${ocupadas}/${total}`, sub: `${pct}%` },
    { icon: Calendar, label: "Reservas activas", value: `${activas}/${total}`, sub: `${pctActivas}%` },
    { icon: DollarSign, label: "Ingresos hoy", value: FORMAT_CURRENCY.format(ingresosHoy) },
    { icon: Users, label: "Huéspedes hoy", value: String(data?.huespedesHoy ?? 0) },
  ];

  return (
    <div className="relative overflow-hidden rounded-2xl bg-gradient-to-br from-rose-50 via-white to-rose-100/60 dark:from-zinc-900 dark:via-zinc-800/80 dark:to-rose-950/30 border border-rose-200/60 dark:border-rose-900/15 shadow-sm dark:shadow-[0_8px_32px_-8px_rgba(220,38,38,0.08)]">
      {/* Ornamental glows */}
      <div className="absolute -top-20 -right-20 w-80 h-80 rounded-full bg-rose-200/40 dark:bg-rose-700/8 blur-3xl pointer-events-none" />
      <div className="absolute -bottom-20 -left-20 w-60 h-60 rounded-full bg-rose-200/30 dark:bg-rose-800/8 blur-3xl pointer-events-none" />

      <div className="relative z-10 p-6 md:p-8 space-y-6">
        {/* Top row: greeting + occupancy badge */}
        <motion.div
          initial={{ opacity: 0, y: -10 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4 }}
          className="flex items-start justify-between gap-4 flex-wrap"
        >
          <div className="space-y-1">
            <h1 className="text-2xl md:text-3xl font-bold text-zinc-800 dark:text-white tracking-tight">
              {greeting()},{" "}
              <span className="text-rose-600 dark:text-rose-300">{user?.username ?? "Usuario"}</span>
              <span className="inline-block ml-1.5">👋</span>
            </h1>
            <p className="text-sm text-zinc-500 dark:text-zinc-400">
              Hotel operando con normalidad
            </p>
          </div>

          <div className="flex items-center gap-2 flex-wrap">
            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.3, delay: 0.15 }}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/70 dark:bg-white/5 border border-zinc-200 dark:border-white/10 backdrop-blur-sm"
            >
              <Sparkles className="w-4 h-4 text-emerald-500 dark:text-emerald-400" />
              <span className="text-sm font-medium text-zinc-700 dark:text-zinc-200">
                {isLoading ? "..." : `${pct}% ocupación`}
              </span>
            </motion.div>

            <motion.div
              initial={{ opacity: 0, scale: 0.9 }}
              animate={{ opacity: 1, scale: 1 }}
              transition={{ duration: 0.3, delay: 0.2 }}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-white/70 dark:bg-white/5 border border-zinc-200 dark:border-white/10 backdrop-blur-sm"
            >
              <Clock className="w-4 h-4 text-rose-500 dark:text-rose-400" />
              <span className="text-sm font-medium text-zinc-700 dark:text-zinc-200 whitespace-nowrap">
                {turno ? `${turno.info.label} (${turno.info.inicio} – ${turno.info.fin})` : "Sin turno activo"}
              </span>
            </motion.div>
          </div>
        </motion.div>

        {/* Quick stat chips */}
        <motion.div
          initial={{ opacity: 0, y: 12 }}
          animate={{ opacity: 1, y: 0 }}
          transition={{ duration: 0.4, delay: 0.1 }}
          className="flex flex-wrap gap-3"
        >
          <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1 rounded-full bg-emerald-100 dark:bg-emerald-500/15 text-emerald-700 dark:text-emerald-300 border border-emerald-200 dark:border-emerald-500/20 font-medium">
            <LogIn className="w-3 h-3" />
            Check-ins: {isLoading ? "—" : data?.reservasActivas ?? 0}
          </span>
          <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1 rounded-full bg-amber-100 dark:bg-amber-500/15 text-amber-700 dark:text-amber-300 border border-amber-200 dark:border-amber-500/20 font-medium">
            <Calendar className="w-3 h-3" />
            Pendientes: {isLoading ? "—" : data?.reservasActivas ?? 0}
          </span>
          <span className="inline-flex items-center gap-1.5 text-[11px] px-3 py-1 rounded-full bg-rose-100 dark:bg-rose-500/15 text-rose-700 dark:text-rose-300 border border-rose-200 dark:border-rose-500/20 font-medium">
            <Users className="w-3 h-3" />
            Alertas: —
          </span>
        </motion.div>

        {/* KPI grid */}
        <motion.div
          initial="hidden"
          animate="visible"
          variants={{
            hidden: {},
            visible: { transition: { staggerChildren: 0.07 } },
          }}
          className="grid grid-cols-2 md:grid-cols-4 gap-3"
        >
          {items.map((item) => (
            <motion.div
              key={item.label}
              variants={{
                hidden: { opacity: 0, y: 16 },
                visible: { opacity: 1, y: 0 },
              }}
              className="rounded-xl bg-white/60 dark:bg-white/[0.04] border border-zinc-200/80 dark:border-white/[0.06] p-4 hover:bg-white/90 dark:hover:bg-white/[0.07] transition-colors"
            >
              <div className="flex items-center gap-2 text-zinc-400 dark:text-zinc-500 mb-2">
                <item.icon className="w-4 h-4" />
                <span className="text-[11px] uppercase tracking-wider font-medium">
                  {item.label}
                </span>
              </div>
              {isLoading ? (
                <Loader2 className="size-5 animate-spin text-zinc-400 dark:text-zinc-500" />
              ) : (
                <div className="flex items-end gap-2">
                  <span className="text-xl md:text-2xl font-bold text-zinc-800 dark:text-white">
                    {item.value}
                  </span>
                  {item.sub && (
                    <span className="text-sm font-semibold text-rose-600 dark:text-rose-300 mb-0.5">
                      {item.sub}
                    </span>
                  )}
                </div>
              )}
            </motion.div>
          ))}
        </motion.div>
      </div>
    </div>
  );
}

export default DashboardHero;
