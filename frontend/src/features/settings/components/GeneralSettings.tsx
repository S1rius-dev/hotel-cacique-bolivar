import {
  Building2,
  Globe,
  CircleDollarSign,
  Languages,
  Clock,
} from "lucide-react";
import {
  Card,
  CardContent,
  CardDescription,
  CardHeader,
  CardTitle,
} from "@/components/ui/card";
import { GENERAL_SETTINGS } from "./constants";

const iconMap: Record<string, typeof Building2> = {
  Building2,
  Globe,
  CircleDollarSign,
  Languages,
  Clock,
};

export function GeneralSettings() {
  return (
    <div className="space-y-6">
      <div>
        <h2 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100">
          Configuración general
        </h2>
        <p className="mt-1 text-sm text-muted-foreground">
          Parámetros generales del hotel. Estas opciones estarán disponibles
          próximamente.
        </p>
      </div>

      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {GENERAL_SETTINGS.map((setting) => {
          const Icon = iconMap[setting.icon] ?? Building2;

          return (
            <Card key={setting.id} size="sm">
              <CardHeader>
                <div className="flex items-center gap-3">
                  <div className="flex size-9 shrink-0 items-center justify-center rounded-lg bg-rose-100 dark:bg-rose-900/30">
                    <Icon className="size-4 text-rose-700 dark:text-rose-400" />
                  </div>
                  <div className="min-w-0">
                    <CardTitle className="text-sm font-medium">
                      {setting.label}
                    </CardTitle>
                    {setting.description && (
                      <CardDescription className="text-xs leading-tight">
                        {setting.description}
                      </CardDescription>
                    )}
                  </div>
                </div>
              </CardHeader>
              <CardContent>
                <p className="text-base font-semibold text-zinc-900 dark:text-zinc-100">
                  {setting.value}
                </p>
              </CardContent>
            </Card>
          );
        })}
      </div>
    </div>
  );
}
