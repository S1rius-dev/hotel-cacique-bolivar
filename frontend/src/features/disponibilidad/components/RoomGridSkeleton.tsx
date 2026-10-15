import { BedDouble } from "lucide-react";

function RoomGridSkeleton() {
  return (
    <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 xl:grid-cols-4 gap-4">
      {Array.from({ length: 12 }).map((_, i) => (
        <div
          key={i}
          className="rounded-xl border bg-white dark:bg-zinc-900/60 overflow-hidden animate-pulse"
        >
          <div className="p-3 sm:p-4 flex flex-col gap-2 sm:gap-3 h-full">
            <div className="flex items-start justify-between gap-2">
              <div className="flex items-start gap-3">
                <div className="p-2 rounded-lg bg-zinc-200 dark:bg-zinc-700">
                  <BedDouble className="w-5 h-5 text-zinc-200 dark:text-zinc-700" />
                </div>
                <div className="space-y-2">
                  <div className="h-5 w-16 bg-zinc-200 dark:bg-zinc-700 rounded" />
                  <div className="h-3 w-24 bg-zinc-200 dark:bg-zinc-700 rounded" />
                </div>
              </div>
              <div className="h-5 w-20 bg-zinc-200 dark:bg-zinc-700 rounded-full shrink-0" />
            </div>
            <div className="ml-0 sm:ml-12 space-y-2 flex-1">
              <div className="h-3 w-32 bg-zinc-200 dark:bg-zinc-700 rounded" />
              <div className="h-3 w-24 bg-zinc-200 dark:bg-zinc-700 rounded" />
            </div>
          </div>
        </div>
      ))}
    </div>
  );
}

export default RoomGridSkeleton;
