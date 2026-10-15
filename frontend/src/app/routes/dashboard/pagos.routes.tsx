import { lazy } from "react";
import { Route } from "react-router-dom";
import CajaSkeleton from "@/components/ui/CajaSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const PagosDashboardPage = lazy(
  () => import("@/features/pagos/pages/PagosDashboardPage"),
);

export const pagosRoute = (
  <Route
    path="pagos"
    element={withSuspense(<PagosDashboardPage />, <CajaSkeleton />)}
  />
);
