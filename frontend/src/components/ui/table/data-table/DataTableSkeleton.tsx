import { Skeleton } from "@/components/ui/skeleton";

interface Props {
  columns?: number;
  rows?: number;
  showActions?: boolean;
}

export function DataTableSkeleton({
  columns = 5,
  rows = 6,
  showActions = true,
}: Props) {
  return (
    <div className="rounded-xl border bg-background">
      {/* Header */}
      <div
        className="grid border-b px-4 py-3"
        style={{
          gridTemplateColumns: `repeat(${columns + (showActions ? 1 : 0)}, minmax(0,1fr))`,
        }}>
        {Array.from({ length: columns + (showActions ? 1 : 0) }).map((_, i) => (
          <Skeleton key={i} className="h-4 w-20" />
        ))}
      </div>

      {/* Rows */}
      <div className="divide-y">
        {Array.from({ length: rows }).map((_, rowIndex) => (
          <div
            key={rowIndex}
            className="grid items-center gap-4 px-4 py-4"
            style={{
              gridTemplateColumns: `repeat(${columns + (showActions ? 1 : 0)}, minmax(0,1fr))`,
            }}>
            {Array.from({
              length: columns + (showActions ? 1 : 0),
            }).map((_, colIndex) => (
              <Skeleton key={colIndex} className="h-4 w-full max-w-[120px]" />
            ))}
          </div>
        ))}
      </div>
    </div>
  );
}
