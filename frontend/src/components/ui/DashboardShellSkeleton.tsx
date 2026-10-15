import { Skeleton } from "@/components/ui/skeleton";

function DashboardShellSkeleton() {
  return (
    <div className="h-screen flex overflow-hidden animate-pulse">
      <aside className="hidden md:flex w-60 flex-col border-r bg-card shrink-0">
        <div className="flex items-center gap-2.5 h-16 px-5 border-b">
          <Skeleton className="size-8 rounded-lg" />
          <Skeleton className="h-4 w-24" />
        </div>

        <div className="flex-1 p-4 space-y-6">
          {Array.from({ length: 3 }).map((_, section) => (
            <div key={section} className="space-y-2">
              <Skeleton className="h-3 w-16" />
              {Array.from({ length: 3 }).map((_, item) => (
                <div key={item} className="flex items-center gap-3 px-2 py-2">
                  <Skeleton className="size-5 rounded" />
                  <Skeleton className="h-3.5 w-24" />
                </div>
              ))}
            </div>
          ))}
        </div>
      </aside>

      <div className="flex-1 flex flex-col min-h-0">
        <header className="h-16 shrink-0 border-b bg-background flex items-center justify-between px-4 sm:px-6">
          <div className="flex items-center gap-3">
            <Skeleton className="size-6 rounded md:hidden" />
            <Skeleton className="h-5 w-40 hidden sm:block" />
          </div>
          <div className="flex items-center gap-3">
            <Skeleton className="size-8 rounded-full" />
            <Skeleton className="h-4 w-24 hidden sm:block" />
          </div>
        </header>

        <main className="flex-1 overflow-y-auto overflow-x-hidden bg-card min-h-0 min-w-0">
          <div className="p-4 sm:p-6 space-y-6">
            <div className="flex items-center justify-between">
              <div className="space-y-1.5">
                <Skeleton className="h-5 w-36" />
                <Skeleton className="h-3.5 w-52" />
              </div>
              <div className="flex gap-2">
                <Skeleton className="h-8 w-28 rounded-lg" />
                <Skeleton className="size-8 rounded-lg" />
              </div>
            </div>

            <div className="rounded-xl border bg-background">
              <div className="grid grid-cols-5 gap-4 border-b px-4 py-3">
                {Array.from({ length: 5 }).map((_, i) => (
                  <Skeleton key={i} className="h-4 w-full" />
                ))}
              </div>
              {Array.from({ length: 6 }).map((_, i) => (
                <div
                  key={i}
                  className="grid grid-cols-5 gap-4 border-b px-4 py-4"
                >
                  {Array.from({ length: 5 }).map((_, j) => (
                    <Skeleton key={j} className="h-4 w-full" />
                  ))}
                </div>
              ))}
            </div>

            <div className="flex justify-between items-center">
              <Skeleton className="h-4 w-32" />
              <div className="flex gap-2">
                <Skeleton className="size-8 rounded" />
                <Skeleton className="size-8 rounded" />
                <Skeleton className="size-8 rounded" />
              </div>
            </div>
          </div>
        </main>
      </div>
    </div>
  );
}

export default DashboardShellSkeleton;
