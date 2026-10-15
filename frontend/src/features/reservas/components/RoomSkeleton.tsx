export const RoomSkeleton: React.FC = () => (
  <div className="space-y-4">
    <div className="h-5 w-48 bg-zinc-200 dark:bg-zinc-800 rounded animate-pulse" />
    <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
      {[1, 2, 3, 4].map((i) => (
        <div key={i} className="bg-card border rounded-2xl overflow-hidden animate-pulse">
          <div className="h-32 bg-zinc-200 dark:bg-zinc-800" />
          <div className="p-4 space-y-3">
            <div className="h-4 bg-zinc-200 dark:bg-zinc-800 rounded w-1/2" />
            <div className="h-3 bg-zinc-200 dark:bg-zinc-800 rounded w-3/4" />
            <div className="h-3 bg-zinc-200 dark:bg-zinc-800 rounded w-1/4" />
          </div>
        </div>
      ))}
    </div>
  </div>
);