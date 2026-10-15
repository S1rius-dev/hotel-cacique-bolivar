import type { ReactNode } from "react";
import { Navigate, useLocation } from "react-router-dom";
import { useAuth } from "@/app/auth/AuthContext";
import { Loader2, ShieldCheck } from "lucide-react";

interface Props {
  children: ReactNode;
  allowedRoles?: string[];
}

const ProtectedRoute: React.FC<Props> = ({ children, allowedRoles }) => {
  const { user, loading } = useAuth();
  const location = useLocation();

  if (loading) {
    return (
      <div className="min-h-screen flex items-center justify-center bg-linear-to-br from-zinc-50 to-rose-50 dark:from-zinc-950 dark:to-zinc-900">
        <div className="flex flex-col items-center gap-6 p-8 bg-card rounded-2xl shadow-xl border border-border/50">
          <div className="p-4 rounded-full bg-rose-100 dark:bg-rose-900/30">
            <ShieldCheck className="w-10 h-10 text-rose-600 dark:text-rose-400 animate-pulse" />
          </div>

          <div className="flex flex-col items-center gap-3">
            <Loader2 className="w-6 h-6 animate-spin text-rose-800" />

            <div className="flex items-center gap-1.5">
              <div
                className="h-2 w-2 rounded-full bg-rose-800 animate-bounce"
                style={{ animationDelay: "0s" }}
              />
              <div
                className="h-2 w-2 rounded-full bg-rose-800 animate-bounce"
                style={{ animationDelay: "0.15s" }}
              />
              <div
                className="h-2 w-2 rounded-full bg-rose-800 animate-bounce"
                style={{ animationDelay: "0.3s" }}
              />
            </div>

            <p className="text-sm text-muted-foreground font-medium">
              Verificando sesión...
            </p>
          </div>
        </div>
      </div>
    );
  }

  if (!user) {
    if (location.pathname.startsWith("/dashboard")) {
      return <Navigate to="/hotel/login" replace />;
    }
    return <Navigate to="/" replace />;
  }

  if (allowedRoles && !allowedRoles.some((role) => user.roles.includes(role))) {
    return <Navigate to="/" replace />;
  }

  return <>{children}</>;
};

export default ProtectedRoute;
