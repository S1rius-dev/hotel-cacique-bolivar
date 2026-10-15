import { lazy } from "react";
import { Route } from "react-router-dom";
import CajaSkeleton from "@/components/ui/CajaSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const CajaPage = lazy(() => import("@/features/caja/pages/CajaPage"));

export const cajaRoute = (
  <Route path="caja" element={withSuspense(<CajaPage />, <CajaSkeleton />)} />
);
