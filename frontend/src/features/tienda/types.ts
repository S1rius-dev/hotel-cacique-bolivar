export interface Categoria {
  id: number;
  nombre: string;
}

export interface Producto {
  id: number;
  nombre: string;
  skuBarcode: string;
  categoria: Categoria | null;
  precioVenta: number;
  costo: number;
  stockActual: number;
  stockMinimo: number;
  activo: boolean;
  estadoStock?: EstadoStock;
  createdAt?: string;
  updatedAt?: string;
}

export type EstadoStock = "ok" | "bajo" | "agotado";
export type TipoMovimiento = "ENTRADA" | "SALIDA" | "VENTA" | "AJUSTE";

export interface MovimientoInventario {
  id: number;
  productoId?: number;
  productoNombre?: string;
  tipo: TipoMovimiento;
  cantidad: number;
  stockResultante: number;
  motivo?: string;
  usuario: string;
  referenciaId?: number | null;
  createdAt: string;
}

export interface TiendaMetricas {
  totalProductos: number;
  valorInventario: number;
  stockBajoCount: number;
  vendidosHoyCount: number;
  vendidosHoyMonto: number;
}

export type StockFiltro = "BAJO" | "AGOTADO" | "OK" | "";

export interface TiendaFilters {
  search?: string;
  categoriaId?: number | null;
  stock?: StockFiltro;
  page?: number;
  size?: number;
}

export function getEstadoStock(p: Pick<Producto, "stockActual" | "stockMinimo">): EstadoStock {
  if (p.stockActual == null || p.stockActual === 0) return "agotado";
  if (p.stockMinimo != null && p.stockActual <= p.stockMinimo) return "bajo";
  return "ok";
}
