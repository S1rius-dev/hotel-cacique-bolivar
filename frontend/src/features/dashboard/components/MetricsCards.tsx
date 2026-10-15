import { motion } from "motion/react";
import { Card, CardContent } from "@/components/ui/card";
import { BedDouble, Calendar, DollarSign, Users, Loader2 } from "lucide-react";
import { FORMAT_CURRENCY } from "@/features/pagos";
import type { DashboardResumen } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  data?: DashboardResumen;
  isLoading?: boolean;
}

export const MetricsCards = ({ data, isLoading }: Props) => {
  const metrics = [
    {
      title: "Habitaciones ocupadas",
      value: data ? `${data.habitacionesOcupadas ?? 0} / ${data.totalHabitaciones ?? 0}` : "—",
      icon: BedDouble,
    },
    {
      title: "Reservas activas",
      value: data ? String(data.reservasActivas ?? 0) : "—",
      icon: Calendar,
    },
    {
      title: "Ingresos hoy",
      value: data ? FORMAT_CURRENCY.format(data.ingresosHoy ?? 0) : "—",
      icon: DollarSign,
    },
    {
      title: "Huéspedes hoy",
      value: data ? String(data.huespedesHoy ?? 0) : "—",
      icon: Users,
    },
  ];

  return (
    <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
      {metrics.map((item) => (
        <motion.div
          key={item.title}
          whileHover={{ y: -4, scale: 1.01, transition: { duration: 0.2 } }}
        >
          <Card className="group rounded-2xl border bg-white/70 dark:bg-zinc-900/60 backdrop-blur shadow-sm hover:shadow-lg transition-shadow duration-300">
            <CardContent className="flex items-center justify-between p-5">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">{item.title}</p>
                <h2 className="text-2xl font-bold tracking-tight">
                  {isLoading ? (
                    <Loader2 className="size-5 animate-spin text-muted-foreground" />
                  ) : (
                    item.value
                  )}
                </h2>
              </div>

              <motion.div
                className="flex items-center justify-center w-11 h-11 rounded-xl bg-rose-800/10 text-rose-800 group-hover:bg-rose-800/90 group-hover:text-white transition-colors duration-300"
                whileHover={{ rotate: 6, scale: 1.1 }}
                transition={{ type: "spring", stiffness: 300, damping: 12 }}
              >
                <item.icon className="w-5 h-5" />
              </motion.div>
            </CardContent>

            <div className="h-1 w-0 bg-rose-800/90 group-hover:w-full transition-all duration-300 rounded-b-2xl" />
          </Card>
        </motion.div>
      ))}
    </div>
  );
};
