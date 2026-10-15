import { lazy } from "react";
import { Route } from "react-router-dom";
import RoomGridSkeleton from "@/features/disponibilidad/components/RoomGridSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const DisponibilidadPage = lazy(
  () => import("@/features/disponibilidad/pages/DisponibilidadPage"),
);

export const ocupacionRoute = (
  <Route
    path="ocupacion"
    element={withSuspense(<DisponibilidadPage />, <RoomGridSkeleton />)}
  />
);
