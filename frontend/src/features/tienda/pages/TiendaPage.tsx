import { useState } from "react";
import { useDebounce } from "use-debounce";
import { Plus, Package, DollarSign, AlertTriangle, ShoppingBag } from "lucide-react";
import { Button } from "@/components/ui/button";
import MetricCard from "../components/MetricCard";
import ProductSearchBar from "../components/ProductSearchBar";
import ProductFilterSelects from "../components/ProductFilterSelects";
import ProductGrid from "../components/ProductGrid";
import NewProductModal from "../components/NewProductModal";
import EditProductModal from "../components/EditProductModal";
import ProductMovementDrawer from "../components/ProductMovementDrawer";
import { useProductos, useMetricas } from "../hooks/useTienda";
import type { Producto, StockFiltro } from "../types";

export default function TiendaPage() {
  const [search, setSearch] = useState("");
  const [debouncedSearch] = useDebounce(search, 300);
  const [categoriaId, setCategoriaId] = useState<number | null>(null);
  const [stock, setStock] = useState<StockFiltro>("");
  const [page] = useState(0);
  const size = 12;
  const [newOpen, setNewOpen] = useState(false);
  const [selected, setSelected] = useState<Producto | null>(null);
  const [editOpen, setEditOpen] = useState(false);
  const [drawerOpen, setDrawerOpen] = useState(false);

  const { data: pageData, isLoading } = useProductos({ search: debouncedSearch, categoriaId: categoriaId ?? undefined, stock: stock || undefined, page, size });
  const { data: metricas } = useMetricas();

  const productos = pageData?.content ?? [];

  const handleSelect = (p: Producto) => {
    setSelected(p);
    setDrawerOpen(true);
  };

  const handleEditFromDrawer = () => {
    setDrawerOpen(false);
    setEditOpen(true);
  };

  const handleEditFromCard = (p: Producto) => {
    setSelected(p);
    setEditOpen(true);
  };

  return (
    <div className="min-h-full bg-card p-4 md:p-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4">
        <div>
          <h1 className="text-2xl font-heading font-semibold text-foreground">Tienda</h1>
          <p className="text-sm text-muted-foreground mt-1">Productos e inventario · borde escaneo rápido</p>
        </div>
        <Button onClick={() => setNewOpen(true)} className="bg-[#c81e3a] hover:bg-[#a0182f] text-white">
          <Plus className="size-4" /> Nuevo producto
        </Button>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 xl:grid-cols-4 gap-4">
        <MetricCard label="Total productos" value={metricas?.totalProductos ?? "-"} sublabel={`${productos.length} mostrados`} icon={Package} tone="default" />
        <MetricCard label="Valor inventario" value={metricas ? `$${Number(metricas.valorInventario).toLocaleString("es-CO")}` : "-"} sublabel="Precio venta × stock" icon={DollarSign} tone="success" />
        <MetricCard label="Stock bajo" value={metricas?.stockBajoCount ?? "-"} sublabel="Requiere reposición" icon={AlertTriangle} tone="warning" />
        <MetricCard label="Vendidos hoy" value={metricas?.vendidosHoyCount ?? "-"} sublabel={metricas ? `$${Number(metricas.vendidosHoyMonto).toLocaleString("es-CO")}` : ""} icon={ShoppingBag} tone="accent" />
      </div>

      <div className="rounded-xl border bg-card border-border p-3 flex flex-col lg:flex-row gap-3">
        <ProductSearchBar value={search} onChange={setSearch} onScan={(code) => setSearch(code)} />
        <ProductFilterSelects categoriaId={categoriaId} onCategoriaChange={setCategoriaId} stock={stock} onStockChange={setStock} />
      </div>

      {isLoading ? (
        <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
          {[1, 2, 3, 4, 5, 6, 7, 8].map((i) => (
            <div key={i} className="h-44 rounded-xl bg-card border border-border animate-pulse" />
          ))}
        </div>
      ) : (
        <ProductGrid productos={productos} onSelect={handleSelect} onEdit={handleEditFromCard} />
      )}

      <NewProductModal open={newOpen} onOpenChange={setNewOpen} />
      <EditProductModal producto={selected} open={editOpen} onOpenChange={setEditOpen} />
      <ProductMovementDrawer producto={selected} open={drawerOpen} onOpenChange={setDrawerOpen} onEdit={handleEditFromDrawer} />
    </div>
  );
}
