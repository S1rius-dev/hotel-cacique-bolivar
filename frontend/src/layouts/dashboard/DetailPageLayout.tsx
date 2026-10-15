import { Pencil, Trash2 } from "lucide-react";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Separator } from "@/components/ui/separator";

interface DetailPageLayoutProps {
  identity: React.ReactNode;
  children: React.ReactNode;

  actions?: React.ReactNode;

  onEdit?: () => void;
  onDelete?: () => void;
}

export default function DetailPageLayout({
  identity,
  children,
  actions,
  onEdit,
  onDelete,
}: DetailPageLayoutProps) {
  return (
    <Card className="p-4 sm:p-6 space-y-4 sm:space-y-6 overflow-hidden">
      {/* HEADER */}
      <div className="flex flex-col sm:flex-row items-start justify-between gap-4 sm:gap-6">
        {/* IDENTITY (SOLO NOMBRE) */}
        <div className="min-w-0 w-full sm:w-auto">{identity}</div>

        {/* ACTIONS */}
        <div className="flex items-center gap-2 shrink-0 flex-wrap justify-end w-full sm:w-auto">
          {actions}

          {onEdit && (
            <Button variant="outline" size="sm" onClick={onEdit}>
              <Pencil data-icon="inline-start" />
              <span className="hidden sm:inline">Editar</span>
            </Button>
          )}

          {onDelete && (
            <Button variant="destructive" size="sm" onClick={onDelete}>
              <Trash2 data-icon="inline-start" />
              <span className="hidden sm:inline">Eliminar</span>
            </Button>
          )}
        </div>
      </div>

      <Separator />

      {/* CONTENT */}
      <div className="space-y-6">{children}</div>
    </Card>
  );
}
