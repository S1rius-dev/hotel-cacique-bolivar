export default function RoomTypeCardSkeleton() {
  const bar = "rounded-full bg-stone-200 animate-pulse";

  return (
    <div className="flex flex-col sm:flex-row rounded-2xl overflow-hidden bg-white border border-stone-200">
      <div className="sm:w-[220px] lg:w-[260px] shrink-0 aspect-[4/3] sm:aspect-auto bg-stone-100 flex items-center justify-center">
        <div className="size-12 rounded-full bg-stone-200 animate-pulse" />
      </div>

      <div className="flex-1 flex flex-col p-5 lg:p-6 gap-3">
        <div className={`h-6 w-48 ${bar}`} />
        <div className={`h-4 w-36 ${bar}`} />

        <div className="h-px w-10 bg-stone-200 my-1" />

        <div className={`h-4 w-full ${bar}`} />
        <div className={`h-4 w-3/4 ${bar}`} />

        <div className="flex gap-5">
          {Array.from({ length: 4 }).map((_, i) => (
            <div key={i} className={`h-3 w-16 ${bar}`} />
          ))}
        </div>

        <div className="mt-auto flex items-center justify-between pt-3 border-t border-stone-100">
          <div className={`h-3 w-16 ${bar}`} />
          <div className={`h-8 w-24 rounded-lg ${bar}`} />
        </div>
      </div>
    </div>
  );
}
