import { BedDoubleIcon } from "lucide-react";
import { SearchIcon } from "lucide-react";

interface RoomEmptyStateProps {
  type: "no-data" | "no-results";
}

export const RoomEmptyState: React.FC<RoomEmptyStateProps> = ({ type }) => {
  return (
    <div className="text-center py-12 bg-zinc-50 dark:bg-zinc-800/50 rounded-2xl border border-dashed border-zinc-300 dark:border-zinc-700 flex-1">
      {type === "no-data" ? (
        <>
          <BedDoubleIcon className="size-12 mx-auto text-zinc-300 dark:text-zinc-600 mb-3" />
          <p className="text-sm text-muted-foreground">
            No hay habitaciones registradas
          </p>
        </>
      ) : (
        <>
          <SearchIcon className="size-8 mx-auto text-zinc-300 dark:text-zinc-600 mb-3" />
          <p className="text-sm text-muted-foreground">
            No hay habitaciones que coincidan con los filtros
          </p>
        </>
      )}
    </div>
  );
};