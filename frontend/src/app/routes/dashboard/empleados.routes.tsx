import { lazy } from "react";
import { Route } from "react-router-dom";
import FormSkeleton from "@/components/ui/FormSkeleton";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const EmpleadosPage = lazy(
  () => import("@/features/empleados/pages/EmpleadosTablePage"),
);
const EmpleadoFormPage = lazy(
  () => import("@/features/empleados/pages/EmpleadoFormPage"),
);

export const empleadosRoutes = (
  <Route path="empleados">
    <Route
      index
      element={withSuspense(<EmpleadosPage />, <DataTableSkeleton />)}
    />
    <Route
      path="nuevo"
      element={withSuspense(<EmpleadoFormPage />, <FormSkeleton />)}
    />
    <Route
      path=":id/editar"
      element={withSuspense(<EmpleadoFormPage />, <FormSkeleton />)}
    />
  </Route>
);
