// src/components/ui/table/data-table/types.ts
import React from "react";

export type Column<T> = {
  key: keyof T | string;
  header: string;
  render?: (row: T) => React.ReactNode;
  className?: string;
  cellClassName?: string;
};

export interface DataTableProps<T> {
  data: T[];
  columns: Column<T>[];
  renderActions?: (row: T) => React.ReactNode;
  onRowClick?: (row: T) => void;
  loading?: boolean;
  currentPage?: number;
  totalPages?: number;
  totalItems?: number;
  pageSize?: number;
  onPageChange?: (page: number) => void;
}
