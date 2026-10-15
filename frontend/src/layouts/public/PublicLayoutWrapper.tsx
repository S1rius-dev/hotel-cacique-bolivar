import { Outlet } from "react-router-dom";
import { PublicLayoutProvider } from "@/layouts/public/PublicLayoutContext";

export default function PublicLayoutWrapper() {
  return (
    <PublicLayoutProvider>
      <Outlet />
    </PublicLayoutProvider>
  );
}
