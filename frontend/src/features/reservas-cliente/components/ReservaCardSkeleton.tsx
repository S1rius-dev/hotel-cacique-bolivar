export default function ReservaCardSkeleton() {
  return (
    <div className="animate-pulse rounded-xl border border-stone-200 bg-white p-5">
      <div className="flex items-start justify-between">
        <div className="h-4 w-28 rounded bg-stone-200" />
        <div className="h-5 w-24 rounded-full bg-stone-200" />
      </div>
      <div className="mt-4 space-y-2.5">
        <div className="h-3.5 w-48 rounded bg-stone-100" />
        <div className="h-3.5 w-36 rounded bg-stone-100" />
        <div className="h-3.5 w-40 rounded bg-stone-100" />
      </div>
      <div className="mt-4 flex gap-2">
        <div className="h-9 w-24 rounded-xl bg-stone-200" />
        <div className="h-9 w-28 rounded-xl bg-stone-200" />
      </div>
    </div>
  );
}
