import { Routes, Route, Navigate, useLocation } from "react-router-dom";
import ScrollToTop from "@/components/layout/ScrollToTop";
import { PublicRoutes } from "@/app/routes/public.routes";
import { StaffRoutes } from "@/app/routes/staff.routes";
import { DashboardRoutes } from "@/app/routes/dashboard";
import { ModalRoutes } from "@/app/routes/modal.routes";

const AppRouter: React.FC = () => {
  const location = useLocation();
  const backgroundLocation = location.state?.background;

  return (
    <>
      <ScrollToTop />
      <Routes location={backgroundLocation ?? location}>
        {PublicRoutes}
        {StaffRoutes}
        {DashboardRoutes}
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>

      {backgroundLocation && <Routes>{ModalRoutes}</Routes>}
    </>
  );
};

export default AppRouter;
