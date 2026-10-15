import { Skeleton } from "@/components/ui/skeleton";

interface FormSkeletonProps {
  sections?: number;
}

function FormSkeleton({ sections = 1 }: FormSkeletonProps) {
  return (
    <div className="p-4 sm:p-6 space-y-6 animate-pulse">
      <div className="flex items-center gap-3">
        <Skeleton className="size-8 rounded-lg" />
        <div className="space-y-1.5">
          <Skeleton className="h-5 w-36" />
          <Skeleton className="h-3.5 w-56" />
        </div>
      </div>

      {Array.from({ length: sections }).map((_, i) => (
        <div
          key={i}
          className="rounded-xl border bg-card shadow-sm"
        >
          <div className="flex items-center gap-3 border-b px-6 py-4">
            <Skeleton className="size-8 rounded-lg" />
            <div className="space-y-1">
              <Skeleton className="h-4 w-40" />
              <Skeleton className="h-3 w-56" />
            </div>
          </div>

          <div className="p-6">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              {Array.from({ length: 4 }).map((_, j) => (
                <div key={j} className="space-y-1.5">
                  <Skeleton className="h-3 w-20" />
                  <Skeleton className="h-9 w-full rounded-md" />
                </div>
              ))}
            </div>
          </div>
        </div>
      ))}

      <div className="flex justify-end">
        <Skeleton className="h-9 w-32 rounded-lg" />
      </div>
    </div>
  );
}

export default FormSkeleton;
