const ContentSkeleton: React.FC = () => {
  return (
    <div className="p-6 space-y-6 animate-pulse">
      {/* Header */}
      <div className="flex items-center justify-between">
        <div className="h-7 w-48 rounded bg-muted" />
        <div className="h-9 w-32 rounded-lg bg-muted" />
      </div>

      {/* Filters bar */}
      <div className="flex gap-3">
        <div className="h-10 w-64 rounded-lg bg-muted" />
        <div className="h-10 w-40 rounded-lg bg-muted" />
        <div className="h-10 w-32 rounded-lg bg-muted" />
      </div>

      {/* Table */}
      <div className="rounded-xl border bg-background">
        {/* Table header */}
        <div className="grid grid-cols-5 gap-4 border-b px-4 py-3">
          {Array.from({ length: 5 }).map((_, i) => (
            <div key={i} className="h-4 w-full rounded bg-muted" />
          ))}
        </div>
        {/* Table rows */}
        {Array.from({ length: 8 }).map((_, i) => (
          <div
            key={i}
            className="grid grid-cols-5 gap-4 border-b px-4 py-4"
          >
            {Array.from({ length: 5 }).map((_, j) => (
              <div key={j} className="h-4 w-full rounded bg-muted" />
            ))}
          </div>
        ))}
      </div>

      {/* Pagination */}
      <div className="flex justify-between items-center">
        <div className="h-4 w-32 rounded bg-muted" />
        <div className="flex gap-2">
          <div className="h-8 w-8 rounded bg-muted" />
          <div className="h-8 w-8 rounded bg-muted" />
          <div className="h-8 w-8 rounded bg-muted" />
        </div>
      </div>
    </div>
  );
};

export default ContentSkeleton;
