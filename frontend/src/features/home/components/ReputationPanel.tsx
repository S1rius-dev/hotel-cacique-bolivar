import type { ResenaResponse } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  reviews: ResenaResponse[];
  totalElements?: number;
  isLoading?: boolean;
}

function computeAverage(reviews: ResenaResponse[]): string {
  if (!reviews.length) return "0.0";
  const sum = reviews.reduce((acc, r) => acc + (r.calificacion ?? 0), 0);
  return (sum / reviews.length).toFixed(1);
}

function getDistribution(reviews: ResenaResponse[]): number[] {
  const dist = [0, 0, 0, 0, 0];
  reviews.forEach((r) => {
    const cal = r.calificacion ?? 0;
    if (cal >= 1 && cal <= 5) dist[cal - 1]++;
  });
  return dist;
}

function StarText({ filled }: { filled: number }) {
  return (
    <span className="flex items-center gap-0.5 shrink-0 w-16 text-sm leading-none">
      {[1, 2, 3, 4, 5].map((i) => (
        <span key={i} className={i <= filled ? "text-rose-500" : "text-stone-200"}>
          ★
        </span>
      ))}
    </span>
  );
}

function ProgressBar({ percentage }: { percentage: number }) {
  return (
    <div className="flex-1 h-2.5 rounded-full bg-stone-100 overflow-hidden">
      <div
        className="h-full rounded-full bg-linear-to-r from-rose-400 to-rose-600 transition-all duration-700 ease-out"
        style={{ width: `${percentage}%` }}
      />
    </div>
  );
}

function PanelSkeleton() {
  return (
    <div className="rounded-2xl border border-stone-100 bg-linear-to-b from-white via-rose-50/20 to-white p-6 lg:p-8 animate-pulse shadow-sm">
      <div className="lg:flex lg:gap-8 lg:items-center">
        <div className="lg:w-44 lg:shrink-0 space-y-3 text-center">
          <div className="h-12 w-24 mx-auto rounded bg-rose-100" />
          <div className="h-4 w-32 mx-auto rounded bg-stone-100" />
          <div className="h-3 w-24 mx-auto rounded bg-stone-100" />
        </div>
        <div className="hidden lg:block w-px self-stretch bg-stone-100" />
        <div className="flex-1 min-w-0">
          <div className="my-5 lg:mt-0 h-px bg-stone-100 lg:hidden" />
          <div className="space-y-3">
            {[1, 2, 3, 4, 5].map((i) => (
              <div key={i} className="flex items-center gap-3">
                <div className="h-4 w-16 rounded bg-stone-100" />
                <div className="flex-1 h-2.5 rounded-full bg-stone-100" />
                <div className="h-4 w-8 rounded bg-stone-100" />
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}

export default function ReputationPanel({
  reviews,
  totalElements,
  isLoading,
}: Props) {
  if (isLoading) return <PanelSkeleton />;

  const average = computeAverage(reviews);
  const total = totalElements ?? reviews.length;
  const distribution = getDistribution(reviews);

  return (
    <div className="rounded-2xl border border-stone-100 bg-linear-to-b from-white via-rose-50/20 to-white p-6 lg:p-8 shadow-[0_8px_24px_-12px_rgba(28,25,23,0.12),0_2px_4px_rgba(244,63,94,0.03)] hover:shadow-[0_25px_50px_-20px_rgba(244,63,94,0.3)] transition-shadow duration-500">
      <div className="lg:flex lg:gap-8 lg:items-center">
        <div className="lg:w-44 lg:text-center lg:shrink-0">
          <div className="flex items-baseline justify-center gap-1.5">
            <span className="text-4xl lg:text-5xl font-bold text-stone-900">
              {average}
            </span>
            <span className="text-2xl lg:text-3xl text-rose-500">★</span>
          </div>
          <p className="mt-1 text-sm font-medium text-stone-600">
            Calificación promedio
          </p>
          <p className="mt-0.5 text-xs text-stone-400">
            Basado en {total} reseña{total !== 1 ? "s" : ""}
          </p>
        </div>

        <div className="hidden lg:block w-px self-stretch bg-linear-to-b from-transparent via-rose-400/20 to-transparent" />

        <div className="flex-1 min-w-0">
          <div className="my-5 lg:mt-0 h-px bg-linear-to-r from-transparent via-rose-400/20 to-transparent lg:hidden" />
          <div className="space-y-2.5">
            {[5, 4, 3, 2, 1].map((stars) => {
              const count = distribution[stars - 1];
              const pct = total > 0 ? (count / total) * 100 : 0;
              return (
                <div key={stars} className="flex items-center gap-2.5">
                  <StarText filled={stars} />
                  <ProgressBar percentage={pct} />
                  <span className="w-10 text-right text-sm text-stone-500 font-medium tabular-nums">
                    {Math.round(pct)}%
                  </span>
                </div>
              );
            })}
          </div>
        </div>
      </div>
    </div>
  );
}
