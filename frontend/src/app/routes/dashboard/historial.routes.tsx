import { lazy } from "react";
import { Route } from "react-router-dom";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const HistorialAccionesPage = lazy(
  () => import("@/features/historial-acciones/pages/HistorialAccionesPage"),
);

export const historialRoute = (
  <Route
    path="historial-acciones"
    element={withSuspense(<HistorialAccionesPage />, <DataTableSkeleton />)}
  />
);
