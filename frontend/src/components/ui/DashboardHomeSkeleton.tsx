import { Skeleton } from "@/components/ui/skeleton";

function DashboardHomeSkeleton() {
  return (
    <div className="p-3 sm:p-4 md:p-6 space-y-4 md:space-y-6 animate-pulse">
      <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
        {Array.from({ length: 4 }).map((_, i) => (
          <div
            key={i}
            className="rounded-xl border bg-card p-4 space-y-2"
          >
            <Skeleton className="h-3 w-20" />
            <Skeleton className="h-6 w-28" />
            <Skeleton className="h-3 w-16" />
          </div>
        ))}
      </div>

      <div className="rounded-xl border bg-card p-4 space-y-4">
        <div className="flex gap-2">
          <Skeleton className="h-7 w-16 rounded-md" />
          <Skeleton className="h-7 w-20 rounded-md" />
          <Skeleton className="h-7 w-16 rounded-md" />
        </div>
        <Skeleton className="h-48 w-full rounded-lg" />
      </div>

      <div className="rounded-xl border bg-card">
        <div className="px-5 py-3 border-b">
          <Skeleton className="h-4 w-36" />
        </div>
        <div className="divide-y">
          {Array.from({ length: 4 }).map((_, i) => (
            <div
              key={i}
              className="flex items-center gap-4 px-5 py-4"
            >
              <Skeleton className="size-9 rounded-full shrink-0" />
              <div className="flex-1 space-y-1.5">
                <Skeleton className="h-3.5 w-48" />
                <Skeleton className="h-3 w-32" />
              </div>
              <Skeleton className="h-3 w-24" />
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default DashboardHomeSkeleton;
