import { Link } from "react-router-dom";
import { ArrowLeft, Home } from "lucide-react";
import { Button } from "@/components/ui/button";

export default function DashboardNotFoundPage() {
  return (
    <div className="flex flex-col items-center justify-center h-full px-6">
      <div className="text-center max-w-md">
        <div className="text-8xl font-bold text-rose-800/30 mb-4 select-none">
          404
        </div>

        <h1 className="text-2xl font-semibold text-zinc-900 dark:text-zinc-100 mb-2">
          Página no encontrada
        </h1>

        <p className="text-muted-foreground mb-8">
          La página que estás buscando no existe o ha sido movida. Verifica la
          URL o regresa al inicio del panel.
        </p>

        <div className="flex items-center justify-center gap-4">
          <Button variant="outline" asChild>
            <Link to=".." relative="path">
              <ArrowLeft className="size-4" />
              Volver atrás
            </Link>
          </Button>

          <Button variant="hotel" asChild>
            <Link to="/dashboard/home">
              <Home className="size-4" />
              Ir al inicio
            </Link>
          </Button>
        </div>
      </div>
    </div>
  );
}
