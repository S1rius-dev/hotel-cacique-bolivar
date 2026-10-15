import { useState } from "react";
import DashboardHero from "../components/DashboardHero";
import TurnoSection from "@/features/turnos/components/TurnoSection";
import { ChartsSection } from "../components/ChartsSection";
import { RecentActivity } from "../components/RecenActivity";
import {
  useDashboardResumen,
  useDashboardIngresos,
  useDashboardOcupacion,
  useUltimasReservas,
} from "../hooks/useDashboard";

type Rango = "HOY" | "SEMANA" | "MES";

const DashboardPage = () => {
  const [rangoIngresos, setRangoIngresos] = useState<Rango>("HOY");
  const [rangoOcupacion, setRangoOcupacion] = useState<Rango>("SEMANA");

  const { data: resumen, isLoading: loadingResumen } = useDashboardResumen();
  const { data: ingresos, isLoading: loadingIngresos } =
    useDashboardIngresos(rangoIngresos);
  const { data: ocupacion, isLoading: loadingOcupacion } =
    useDashboardOcupacion(rangoOcupacion);
  const { data: ultimasReservas, isLoading: loadingUltimas } =
    useUltimasReservas();

  return (
    <div className="p-3 sm:p-4 md:p-6 space-y-4 md:space-y-6">
      <DashboardHero data={resumen} isLoading={loadingResumen} />

      <TurnoSection />

      <ChartsSection
        ingresos={ingresos}
        ocupacion={ocupacion}
        isLoading={loadingIngresos || loadingOcupacion}
        rangoIngresos={rangoIngresos}
        rangoOcupacion={rangoOcupacion}
        onRangoIngresosChange={setRangoIngresos}
        onRangoOcupacionChange={setRangoOcupacion}
      />

      <RecentActivity data={ultimasReservas} isLoading={loadingUltimas} />
    </div>
  );
};

export default DashboardPage;
