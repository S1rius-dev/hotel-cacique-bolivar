import { lazy } from "react";
import { Route } from "react-router-dom";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const PqrsAdminPage = lazy(() => import("@/features/pqrs/pages/PqrsAdminPage"));

export const pqrsRoute = (
  <Route
    path="pqrs"
    element={withSuspense(<PqrsAdminPage />, <DataTableSkeleton />)}
  />
);
