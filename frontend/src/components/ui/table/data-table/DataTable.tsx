import { cn } from "@/lib/utils";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";

import type { DataTableProps } from "./types";
import { DataTablePagination } from "./DataTablePagination";

/** Las filas son opacas para el generic, asi que se leen como registros sueltos. */
function readField(row: unknown, key: string): unknown {
  if (typeof row !== "object" || row === null) return undefined;
  return (row as Record<string, unknown>)[key];
}

export function DataTable<T>({
  data = [],
  columns = [],
  renderActions,
  onRowClick,
  currentPage = 1,
  totalPages = 1,
  totalItems = data.length,
  pageSize = 10,
  onPageChange,
}: DataTableProps<T>) {
  const handleRowClick = (row: T) => {
    onRowClick?.(row);
  };

  return (
    <div className="rounded-lg border border-zinc-200 dark:border-zinc-700 overflow-hidden">
      <Table>
        <TableHeader>
          <TableRow className="bg-gradient-to-r from-rose-800 to-rose-700 hover:from-rose-800 hover:to-rose-700 border-b-2 border-rose-900">
            {columns.map((col, i) => (
              <TableHead
                key={String(col.header)}
                className={cn(
                  "text-white/90 font-semibold tracking-wider uppercase text-[11px] py-3.5",
                  i === 0 && "pl-4",
                  i === columns.length - 1 && !renderActions && "pr-4",
                  col.className,
                )}>
                {col.header}
              </TableHead>
            ))}

            {renderActions && (
              <TableHead className="text-white/90 font-semibold tracking-wider uppercase text-[11px] py-3.5 pr-4">
                Acciones
              </TableHead>
            )}
          </TableRow>
        </TableHeader>

        <TableBody>
          {data.map((row, rowIndex) => (
            <TableRow
              key={String(readField(row, "id") ?? `row-${rowIndex}`)}
              onClick={() => handleRowClick(row)}
              className="cursor-pointer hover:bg-zinc-50 dark:hover:bg-zinc-900/50 transition-colors">
              {columns.map((col) => (
                <TableCell
                  key={String(col.key)}
                  className={cn(col.className, col.cellClassName)}>
                  {col.render
                    ? col.render(row)
                    : String(readField(row, String(col.key)) ?? "")}
                </TableCell>
              ))}

              {renderActions && <TableCell>{renderActions(row)}</TableCell>}
            </TableRow>
          ))}
        </TableBody>
      </Table>

      {onPageChange && (
        <DataTablePagination
          currentPage={currentPage}
          totalPages={totalPages}
          totalItems={totalItems}
          pageSize={pageSize}
          onPageChange={onPageChange}
        />
      )}
    </div>
  );
}
