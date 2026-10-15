import { Skeleton } from "@/components/ui/skeleton";

function CajaSkeleton() {
  return (
    <div className="p-3 sm:p-4 md:p-6 space-y-4 md:space-y-6 animate-pulse">
      <div className="flex items-center gap-4 p-4 rounded-xl border bg-card">
        <Skeleton className="size-10 rounded-full" />
        <div className="space-y-1.5">
          <Skeleton className="h-3.5 w-32" />
          <Skeleton className="h-3 w-48" />
        </div>
        <div className="ml-auto flex gap-2">
          <Skeleton className="h-8 w-24 rounded-lg" />
          <Skeleton className="h-8 w-24 rounded-lg" />
        </div>
      </div>

      <div className="flex flex-col xl:flex-row gap-6">
        <div className="flex-1 rounded-xl border bg-card">
          <div className="px-5 py-3 border-b">
            <Skeleton className="h-4 w-32" />
          </div>
          <div className="divide-y">
            {Array.from({ length: 5 }).map((_, i) => (
              <div
                key={i}
                className="flex items-center gap-4 px-5 py-3.5"
              >
                <Skeleton className="h-2 w-2 rounded-full shrink-0" />
                <div className="flex-1 grid grid-cols-4 gap-4">
                  <Skeleton className="h-3.5 w-24" />
                  <Skeleton className="h-3.5 w-20" />
                  <Skeleton className="h-3.5 w-16" />
                  <Skeleton className="h-3.5 w-16 ml-auto" />
                </div>
              </div>
            ))}
          </div>
        </div>

        <div className="w-full xl:w-72 shrink-0">
          <div className="rounded-xl border border-t-2 border-t-rose-800/70 bg-card overflow-hidden">
            <div className="px-4 py-3 border-b">
              <Skeleton className="h-4 w-20" />
            </div>
            <div className="p-4 space-y-3">
              {Array.from({ length: 4 }).map((_, i) => (
                <div key={i} className="flex justify-between">
                  <Skeleton className="h-3.5 w-24" />
                  <Skeleton className="h-3.5 w-16" />
                </div>
              ))}
              <Skeleton className="h-px w-full" />
              <div className="flex justify-between">
                <Skeleton className="h-4 w-16" />
                <Skeleton className="h-4 w-20" />
              </div>
            </div>
          </div>
        </div>
      </div>

      <div className="rounded-xl border bg-card">
        <div className="px-5 py-3 border-b">
          <Skeleton className="h-4 w-36" />
        </div>
        <div className="divide-y">
          {Array.from({ length: 3 }).map((_, i) => (
            <div
              key={i}
              className="flex items-center gap-4 px-5 py-3.5"
            >
              <div className="flex-1 grid grid-cols-5 gap-4">
                <Skeleton className="h-3.5 w-20" />
                <Skeleton className="h-3.5 w-24" />
                <Skeleton className="h-3.5 w-16" />
                <Skeleton className="h-3.5 w-20" />
                <Skeleton className="h-3.5 w-16 ml-auto" />
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

export default CajaSkeleton;
