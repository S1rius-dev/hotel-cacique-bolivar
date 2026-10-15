import React from "react";
import { Button } from "@/components/ui/button";
import { DataTable } from "@/components/ui/table/data-table/DataTable";
import { DataTableSkeleton } from "@/components/ui/table/data-table/DataTableSkeleton";
import type { Column } from "@/components/ui/table/data-table/types";
import { PlusCircle, Loader2 } from "lucide-react";

interface Stat {
  label: string;
  value: React.ReactNode;
}

interface Props<T> {
  title: string;
  titleIcon?: React.ReactNode;
  subtitle?: string;
  stats?: Stat[];
  data: T[];
  columns: Column<T>[];
  loading?: boolean;
  createLabel?: string;
  onCreate?: () => void;
  createPending?: boolean;
  renderActions?: (row: T) => React.ReactNode;
  onRowClick?: (row: T) => void;
  toolbar?: React.ReactNode;
  filterBar?: React.ReactNode;
  currentPage?: number;
  totalPages?: number;
  totalItems?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
  tableTitle?: string;
  tableSubtitle?: string;
  emptyMessage?: string;
}

export default function ManagementTablePage<T>({
  title,
  titleIcon,
  subtitle = "Gestión de registros",
  stats,
  data,
  columns,
  loading,
  createLabel = "Nuevo",
  onCreate,
  createPending,
  renderActions,
  onRowClick,
  toolbar,
  filterBar,
  currentPage,
  totalPages,
  totalItems,
  pageSize,
  onPageChange,
  tableTitle = "Registros",
  tableSubtitle = "datos del sistema",
  emptyMessage = "No hay registros disponibles",
}: Props<T>) {
  const hasData = data && data.length > 0;

  return (
    <div className="p-4 sm:p-6 space-y-6 min-h-0 bg-card">
      {/* HEADER */}
      <div className="flex items-start justify-between gap-4">
        <div className="space-y-1 min-w-0">
          <div className="flex items-center gap-2">
            {titleIcon && (
              <span className="text-xl sm:text-2xl shrink-0">{titleIcon}</span>
            )}
            <h1 className="text-xl sm:text-2xl font-semibold text-zinc-900 dark:text-zinc-100">
              {title}
            </h1>
          </div>

          <p className="text-sm text-muted-foreground">{subtitle}</p>

          {stats && (
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1.5 pt-1">
              {stats.map((stat) => (
                <div key={stat.label} className="flex items-baseline gap-1.5">
                  <span className="text-base sm:text-lg font-bold tabular-nums text-zinc-900 dark:text-zinc-100 leading-none">
                    {stat.value}
                  </span>
                  <span className="text-xs sm:text-sm text-muted-foreground whitespace-nowrap">
                    {stat.label}
                  </span>
                </div>
              ))}
            </div>
          )}
        </div>

        {onCreate && (
          <Button
            onClick={onCreate}
            disabled={createPending}
            variant="hotel"
            className="shrink-0
              border-rose-800
              bg-rose-800
              text-white
              hover:bg-rose-900
              hover:text-white
              aria-expanded:bg-rose-900
              aria-expanded:text-white
              cursor-pointer
            disabled:cursor-not-allowed disabled:opacity-70
            ">
            {createPending && <Loader2 className="size-4 animate-spin" />}
            {createLabel}
            {!createPending && <PlusCircle data-icon="inline-end" />}
          </Button>
        )}
      </div>

      {/* TOOLBAR (legacy, outside table card) */}
      {toolbar && <div>{toolbar}</div>}

      {/* TABLE CARD */}
      <div
        className="
          rounded-2xl
          border border-zinc-200 dark:border-zinc-800
          shadow-sm
        ">
        {/* TABLE HEADER INFO */}
        <div className="flex items-center justify-between px-5 py-3 border-b border-zinc-100 dark:border-zinc-800">
          <div className="flex items-center gap-2">
            <span className="h-2 w-2 rounded-full bg-rose-800 animate-pulse" />

            <p className="text-sm font-semibold text-zinc-800 dark:text-zinc-200">
              {tableTitle}
            </p>

            <span className="text-xs text-muted-foreground">
              • {tableSubtitle}
            </span>
          </div>

          {!loading && hasData && (
            <span className="text-xs text-muted-foreground">
              {totalItems ?? data.length} elementos
            </span>
          )}
        </div>

        {/* FILTER BAR */}
        {filterBar && (
          <div className="px-5 py-2.5 border-b border-zinc-100 dark:border-zinc-800 bg-zinc-50/50 dark:bg-zinc-900/30">
            {filterBar}
          </div>
        )}

        {/* CONTENT */}
        <div className="p-4">
          {loading ? (
            <DataTableSkeleton columns={columns.length} rows={6} />
          ) : !hasData ? (
            <div className="flex flex-col items-center justify-center h-40 text-sm text-muted-foreground">
              <span className="text-lg">📭</span>
              {emptyMessage}
            </div>
          ) : (
            <DataTable
              data={data}
              columns={columns}
              renderActions={renderActions}
              onRowClick={onRowClick}
              currentPage={currentPage}
              totalPages={totalPages}
              totalItems={totalItems}
              pageSize={pageSize}
              onPageChange={onPageChange}
            />
          )}
        </div>
      </div>
    </div>
  );
}
