import { Skeleton } from "@/components/ui/skeleton";

interface DetailSkeletonProps {
  cards?: number;
}

function DetailSkeleton({ cards = 2 }: DetailSkeletonProps) {
  const clampedCards = Math.min(Math.max(cards, 1), 3);

  return (
    <div className="p-6 space-y-4 animate-in fade-in duration-300">
      <div className="rounded-2xl border border-zinc-200/60 dark:border-zinc-800/60 bg-card p-6 shadow-sm">
        <div className="flex items-center gap-4">
          <Skeleton className="h-10 w-10 rounded-full shrink-0" />
          <div className="flex-1 space-y-2">
            <Skeleton className="h-4 w-48" />
            <Skeleton className="h-3 w-32" />
          </div>
          <div className="flex gap-2">
            <Skeleton className="h-8 w-20 rounded-lg" />
            <Skeleton className="h-8 w-8 rounded-lg" />
          </div>
        </div>
      </div>

      {Array.from({ length: clampedCards }).map((_, i) => (
        <div
          key={i}
          className="rounded-2xl border border-l-4 border-l-zinc-300 dark:border-l-zinc-700 border-zinc-200/60 dark:border-zinc-800/60 bg-card p-6 shadow-sm"
        >
          <Skeleton className="h-4 w-36 mb-4" />
          <div className="space-y-3">
            <div className="flex justify-between">
              <Skeleton className="h-3 w-28" />
              <Skeleton className="h-3 w-40" />
            </div>
            <div className="flex justify-between">
              <Skeleton className="h-3 w-32" />
              <Skeleton className="h-3 w-36" />
            </div>
            <div className="flex justify-between">
              <Skeleton className="h-3 w-24" />
              <Skeleton className="h-3 w-44" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export { DetailSkeleton };
