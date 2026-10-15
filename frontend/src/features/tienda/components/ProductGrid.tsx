import type { Producto } from "../types";
import ProductCard from "./ProductCard";

export default function ProductGrid({ productos, onSelect, onEdit }: { productos: Producto[]; onSelect?: (p: Producto) => void; onEdit?: (p: Producto) => void }) {
  if (productos.length === 0) {
    return (
      <div className="rounded-xl border border-dashed bg-card border-border p-10 text-center">
        <p className="text-sm text-muted-foreground">No se encontraron productos</p>
      </div>
    );
  }
  return (
    <div className="grid grid-cols-2 sm:grid-cols-3 lg:grid-cols-4 gap-4">
      {productos.map((p) => (
        <ProductCard key={p.id} producto={p} onClick={() => onSelect?.(p)} onEdit={onEdit} />
      ))}
    </div>
  );
}
