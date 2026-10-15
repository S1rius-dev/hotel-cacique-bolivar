import { useQuery, useMutation, useQueryClient } from "@tanstack/react-query";
import { toast } from "sonner";
import { getProductoController } from "@/api/generated/producto-controller/producto-controller";
import type { ProductoResponseDTO } from "@/api/generated/openAPIDefinition.schemas";
import { getEstadoStock, type Producto } from "../types";
import { extractErrorMessage } from "@/lib/extract-error";

const api = getProductoController();

/**
 * El DTO generado marca todos los campos como opcionales, pero el backend
 * serializa la entidad completa. Aqui se fija el contrato que el resto de la
 * feature asume, en vez de propagar `undefined` por toda la app.
 *
 * `estadoStock` se deriva con el helper propio: el backend lo manda como string
 * libre y nadie lo lee (ProductCard ya lo recalcula desde el stock).
 */
const toProducto = (dto: ProductoResponseDTO): Producto => {
  const producto: Producto = {
    id: dto.id ?? 0,
    nombre: dto.nombre ?? "",
    skuBarcode: dto.skuBarcode ?? "",
    categoria: dto.categoria
      ? { id: dto.categoria.id ?? 0, nombre: dto.categoria.nombre ?? "" }
      : null,
    precioVenta: dto.precioVenta ?? 0,
    costo: dto.costo ?? 0,
    stockActual: dto.stockActual ?? 0,
    stockMinimo: dto.stockMinimo ?? 0,
    activo: dto.activo ?? true,
    createdAt: dto.createdAt,
    updatedAt: dto.updatedAt,
  };

  return { ...producto, estadoStock: getEstadoStock(producto) };
};

/** Clave de cache compartida por el hook y por las llamadas imperativas. */
export const productoPorCodigoQuery = (codigo: string) =>
  ["producto-codigo", codigo] as const;

export const fetchProductoPorCodigo = (codigo: string) =>
  api.buscarProductoPorCodigo(codigo).then((dto) =>
    dto ? toProducto(dto) : undefined,
  );

export const useProductos = (filters: { search?: string; categoriaId?: number; stock?: string; page?: number; size?: number } = {}) => {
  const { search, categoriaId, stock, page = 0, size = 12 } = filters;
  return useQuery({
    queryKey: ["productos", search, categoriaId, stock, page, size],
    queryFn: async () => {
      const result = await api.listarProductos({ search: search || undefined, categoriaId: categoriaId ?? undefined, stock: stock || undefined, pageable: { page, size } });
      return { ...result, content: (result.content ?? []).map(toProducto) };
    },
    placeholderData: (prev) => prev,
  });
};

export const useProductoPorCodigo = (codigo: string | undefined, enabled = true) =>
  useQuery({
    queryKey: productoPorCodigoQuery(codigo ?? ""),
    queryFn: () => fetchProductoPorCodigo(codigo!),
    enabled: !!codigo && enabled,
  });

export const useMetricas = () =>
  useQuery({
    queryKey: ["tienda-metricas"],
    queryFn: () => api.metricasTienda(),
  });

export const useCategorias = () =>
  useQuery({
    queryKey: ["categorias"],
    queryFn: () => api.listarCategorias(),
  });

export const useCrearProducto = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (data: Parameters<typeof api.crearProducto>[0]) => api.crearProducto(data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
      toast.success("Producto creado");
    },
    onError: (e: unknown) => {
      toast.error(extractErrorMessage(e, "Error al crear producto"));
    },
  });
};

export const useActualizarProducto = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Parameters<typeof api.actualizarProducto>[1] }) => api.actualizarProducto(id, data),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["productos"] });
      toast.success("Producto actualizado");
    },
    onError: (e: unknown) =>
      toast.error(extractErrorMessage(e, "Error al actualizar")),
  });
};

export const useEliminarProducto = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: (id: number) => api.eliminarProducto(id),
    onSuccess: () => {
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
      toast.success("Producto eliminado");
    },
    onError: (e: unknown) => toast.error(extractErrorMessage(e, "Error al eliminar")),
  });
};

export const useRegistrarMovimiento = () => {
  const qc = useQueryClient();
  return useMutation({
    mutationFn: ({ id, data }: { id: number; data: Parameters<typeof api.registrarMovimientoProducto>[1] }) => api.registrarMovimientoProducto(id, data),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["movimientos", vars.id] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
      toast.success("Movimiento registrado");
    },
    onError: (e: unknown) =>
      toast.error(extractErrorMessage(e, "Error al registrar movimiento")),
  });
};

export const useMovimientos = (productoId: number | undefined, enabled = true) =>
  useQuery({
    queryKey: ["movimientos", productoId],
    queryFn: () => api.listarMovimientosProducto(productoId!),
    enabled: !!productoId && enabled,
  });
