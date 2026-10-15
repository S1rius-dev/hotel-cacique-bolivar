import { lazy } from "react";
import { Route } from "react-router-dom";
import ContentSkeleton from "@/components/ui/ContentSkeleton";
import { withSuspense } from "@/app/routes/helpers";

const SettingsPage = lazy(() => import("@/features/settings/pages/Settings"));

export const ajustesRoute = (
  <Route
    path="ajustes"
    element={withSuspense(<SettingsPage />, <ContentSkeleton />)}
  />
);
