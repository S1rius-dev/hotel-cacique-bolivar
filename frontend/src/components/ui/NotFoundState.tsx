import type { LucideIcon } from "lucide-react";
import { SearchX } from "lucide-react";

interface NotFoundStateProps {
  title: string;
  description?: string;
  icon?: LucideIcon;
}

function NotFoundState({
  title,
  description,
  icon: Icon = SearchX,
}: NotFoundStateProps) {
  return (
    <div className="flex flex-col items-center justify-center py-16 px-6 text-center animate-in fade-in duration-300">
      <div className="rounded-full bg-zinc-100 dark:bg-zinc-800 p-5 mb-5">
        <Icon className="h-10 w-10 text-zinc-400 dark:text-zinc-500" />
      </div>
      <h3 className="text-lg font-semibold text-zinc-900 dark:text-zinc-100 mb-1.5">
        {title}
      </h3>
      {description && (
        <p className="text-sm text-muted-foreground max-w-sm">
          {description}
        </p>
      )}
    </div>
  );
}

export { NotFoundState };
