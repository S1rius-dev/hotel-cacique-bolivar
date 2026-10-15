import { User } from "lucide-react";

interface Props {
  name: string;
  hasAlerts?: boolean;
}

function ClienteInfoHeader({ name, hasAlerts }: Props) {
  return (
    <div className="flex items-start gap-3 min-w-0">
      {/* avatar */}
      <div className="w-8 h-8 sm:w-10 sm:h-10 rounded-full bg-zinc-100 flex items-center justify-center border mt-1 shrink-0">
        <User className="w-4 h-4 sm:w-5 sm:h-5 text-zinc-500" />
      </div>

      {/* content */}
      <div className="flex flex-col min-w-0">
        {/* nombre */}
        <h1 className="text-xl sm:text-2xl font-semibold tracking-tight leading-tight truncate">
          {name}
        </h1>

        {/* badge */}
        <div className="mt-1">
          {hasAlerts ? (
            <span
              className="
                inline-flex items-center gap-1
                text-[11px] px-2 py-0.5
                rounded-full border font-medium
                bg-rose-50 text-rose-600 border-rose-200
              ">
              <span className="w-1.5 h-1.5 rounded-full bg-rose-500" />
              Con alertas
            </span>
          ) : (
            <span
              className="
                inline-flex items-center gap-1
                text-[11px] px-2 py-0.5
                rounded-full border font-medium
                bg-emerald-50 text-emerald-600 border-emerald-200
              ">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-500" />
              Sin alertas
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export default ClienteInfoHeader;
