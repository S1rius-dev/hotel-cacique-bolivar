import { lazy } from "react";
import { Route } from "react-router-dom";
import DashboardHomeSkeleton from "@/components/ui/DashboardHomeSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const DashboardPage = lazy(
  () => import("@/features/dashboard/pages/DashboardPage"),
);

export const homeRoute = (
  <Route
    path="home"
    element={withSuspense(<DashboardPage />, <DashboardHomeSkeleton />)}
  />
);
