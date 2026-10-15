import { Construction } from "lucide-react";

interface WorkInProgressProps {
  title?: string;
  description?: string;
  icon?: React.ReactNode;
  badge?: string;
}

export const WorkInProgress = ({
  title = "Funcionalidad en desarrollo",
  description = "Estamos trabajando para ofrecerte esta funcionalidad lo antes posible. Por favor, vuelve más tarde.",
  icon = <Construction className="size-10 text-rose-700 dark:text-rose-400" />,
  badge = "Próximamente",
}: WorkInProgressProps) => {
  return (
    <div className="flex h-full items-center justify-center p-6">
      <div className="w-full max-w-xl rounded-3xl border bg-card p-10 text-center shadow-sm">
        <div className="mx-auto mb-6 flex size-20 items-center justify-center rounded-2xl bg-rose-100 dark:bg-rose-900/20">
          {icon}
        </div>

        <span className="inline-flex rounded-full border border-rose-200 bg-rose-50 px-3 py-1 text-xs font-medium text-rose-700 dark:border-rose-800 dark:bg-rose-950/40 dark:text-rose-400">
          {badge}
        </span>

        <h1 className="mt-5 text-3xl font-bold tracking-tight">
          {title}
        </h1>

        <p className="mt-3 text-muted-foreground">
          {description}
        </p>
      </div>
    </div>
  );
};
