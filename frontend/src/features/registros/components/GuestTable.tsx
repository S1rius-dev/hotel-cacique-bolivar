import { DataTable } from "@/components/ui/table/data-table/DataTable";
import { DataTableActions } from "@/components/ui/table/data-table/DataTableActions";
import { GuestEmptyState } from "./GuestEmptyState";
import { getColumns, getActions } from "./GuestColumns";
import type { HuespedRegistroDTO } from "@/api/generated/openAPIDefinition.schemas";

interface Props {
  records: HuespedRegistroDTO[];
  isLoading?: boolean;
  totalItems?: number;
  totalPages?: number;
  currentPage?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  onViewDetail: (record: HuespedRegistroDTO) => void;
  onRefresh?: () => void;
}

export function GuestTable({
  records,
  isLoading,
  totalItems = 0,
  totalPages = 1,
  currentPage = 1,
  pageSize = 15,
  onPageChange,
  onViewDetail,
  onRefresh,
}: Props) {
  if (!isLoading && records.length === 0) {
    return <GuestEmptyState onRefresh={onRefresh} />;
  }

  const columns = getColumns();
  const actions = getActions(onViewDetail);

  return (
    <div className="rounded-xl border border-zinc-200 shadow-sm dark:border-zinc-800">
      <div className="flex items-center justify-between border-b border-zinc-100 px-5 py-3 dark:border-zinc-800">
        <div className="flex items-center gap-2">
          <span className="h-2 w-2 animate-pulse rounded-full bg-rose-800" />
          <p className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">
            Registro de huéspedes
          </p>
          {!isLoading && (
            <span className="text-xs text-muted-foreground">
              • {totalItems} registro{totalItems !== 1 ? "s" : ""}
            </span>
          )}
        </div>
      </div>
      <div className="p-4">
        <DataTable
          data={records}
          columns={columns}
          renderActions={(row) => (
            <DataTableActions row={row} actions={actions} />
          )}
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalItems}
          pageSize={pageSize}
          onPageChange={onPageChange}
        />
      </div>
    </div>
  );
}
