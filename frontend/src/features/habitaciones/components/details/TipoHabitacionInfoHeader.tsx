import { LayoutGrid } from "lucide-react";

interface Props {
  nombre: string;
  capacidad?: number;
}

function TipoHabitacionInfoHeader({ nombre, capacidad }: Props) {
  return (
    <div className="flex items-start gap-3 min-w-0">
      <div className="w-10 h-10 rounded-full bg-zinc-100 flex items-center justify-center border mt-1 shrink-0">
        <LayoutGrid className="w-5 h-5 text-zinc-500" />
      </div>

      <div className="flex flex-col min-w-0">
        <h1 className="text-xl sm:text-2xl font-semibold tracking-tight leading-tight truncate">
          {nombre}
        </h1>

        <div className="mt-1">
          {capacidad ? (
            <span
              className="
                inline-flex items-center gap-1
                text-[11px] px-2 py-0.5
                rounded-full border font-medium
                bg-sky-50 text-sky-600 border-sky-200
              ">
              {capacidad} Personas
            </span>
          ) : (
            <span
              className="
                inline-flex items-center gap-1
                text-[11px] px-2 py-0.5
                rounded-full border font-medium
                bg-zinc-100 text-zinc-500 border-zinc-200
              ">
              Sin capacidad
            </span>
          )}
        </div>
      </div>
    </div>
  );
}

export default TipoHabitacionInfoHeader;