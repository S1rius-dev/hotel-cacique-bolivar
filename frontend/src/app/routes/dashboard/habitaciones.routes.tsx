import { lazy } from "react";
import { Route } from "react-router-dom";
import FormSkeleton from "@/components/ui/FormSkeleton";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const HabitacionPage = lazy(
  () => import("@/features/habitaciones/pages/HabitacionTablePage"),
);
const HabitacionDetailPage = lazy(
  () => import("@/features/habitaciones/pages/HabitacionDetailPage"),
);
const HabitacionEditPage = lazy(
  () => import("@/features/habitaciones/pages/HabitacionEditPage"),
);
const HabitacionesFormPage = lazy(
  () => import("@/features/habitaciones/pages/HabitacionesFormPage"),
);
const TipoHabitacionDetailPage = lazy(
  () => import("@/features/habitaciones/pages/TipoHabitacionDetailPage"),
);
const TipoHabitacionEditPage = lazy(
  () => import("@/features/habitaciones/pages/TipoHabitacionEditPage"),
);
const TipoHabitacionesFormPage = lazy(
  () => import("@/features/habitaciones/pages/TipoHabitacionesFormPage"),
);

export const habitacionesRoutes = (
  <Route path="habitaciones">
    <Route
      index
      element={withSuspense(<HabitacionPage />, <DataTableSkeleton />)}
    />
    <Route
      path="nuevo"
      element={withSuspense(<HabitacionesFormPage />, <FormSkeleton />)}
    />
    <Route path=":id">
      <Route
        index
        element={withSuspense(<HabitacionDetailPage />, <DetailSkeleton />)}
      />
      <Route
        path="editar"
        element={withSuspense(<HabitacionEditPage />, <FormSkeleton />)}
      />
    </Route>
    <Route
      path="tipos/nuevo"
      element={withSuspense(<TipoHabitacionesFormPage />, <FormSkeleton />)}
    />
    <Route path="tipos/:id">
      <Route
        index
        element={withSuspense(<TipoHabitacionDetailPage />, <DetailSkeleton />)}
      />
      <Route
        path="editar"
        element={withSuspense(<TipoHabitacionEditPage />, <FormSkeleton />)}
      />
    </Route>
  </Route>
);
