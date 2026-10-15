import { Barcode, Package, Pencil } from "lucide-react";
import { getEstadoStock, type Producto } from "../types";

const badgeStyles: Record<string, string> = {
  ok: "bg-[#7bd88f]/15 text-[#7bd88f] border-[#7bd88f]/30",
  bajo: "bg-[#e8b13a]/15 text-[#e8b13a] border-[#e8b13a]/30",
  agotado: "bg-[#e0616a]/15 text-[#e0616a] border-[#e0616a]/30",
};

const badgeLabel: Record<string, string> = {
  ok: "OK",
  bajo: "Bajo",
  agotado: "Agotado",
};

export default function ProductCard({
  producto,
  onClick,
  onEdit,
}: {
  producto: Producto;
  onClick?: () => void;
  onEdit?: (p: Producto) => void;
}) {
  const estado = getEstadoStock(producto);
  const progress = producto.stockMinimo
    ? Math.min(
        100,
        Math.max(
          0,
          (producto.stockActual / Math.max(1, producto.stockMinimo * 2)) * 100,
        ),
      )
    : 100;
  const progressColor =
    estado === "agotado"
      ? "bg-[#e0616a]"
      : estado === "bajo"
        ? "bg-[#e8b13a]"
        : "bg-[#7bd88f]";

  return (
    <div className="group relative rounded-xl border bg-card border-border p-4 flex flex-col gap-3 hover:border-[#c81e3a]/40 transition-colors w-full">
      <button
        type="button"
        onClick={onClick}
        className="absolute inset-0 rounded-xl focus:outline-none focus-visible:ring-2 focus-visible:ring-ring"
        aria-label={`Ver ${producto.nombre}`}
      />
      <div className="flex items-start justify-between gap-2 relative z-10 pointer-events-none">
        <div className="flex items-center gap-2 min-w-0">
          <span className="size-8 rounded-lg bg-muted border border-border flex items-center justify-center shrink-0">
            <Package className="size-4 text-muted-foreground" />
          </span>
          <p className="text-sm font-semibold text-foreground truncate">
            {producto.nombre}
          </p>
        </div>
        <div className="flex items-center gap-1.5 shrink-0">
          <span
            className={`inline-flex px-2 py-0.5 rounded-full text-[11px] font-medium border ${badgeStyles[estado]}`}>
            {badgeLabel[estado]}
          </span>
          {onEdit && (
            <button
              type="button"
              onClick={(e) => {
                e.stopPropagation();
                onEdit(producto);
              }}
              className="size-7 rounded-full bg-card border border-border flex items-center justify-center text-muted-foreground hover:text-foreground hover:bg-accent hover:border-border/80 transition-colors shadow-sm pointer-events-auto"
              aria-label={`Editar ${producto.nombre}`}
              title="Editar producto">
              <Pencil className="size-3.5" />
            </button>
          )}
        </div>
      </div>

      <div className="flex items-center gap-1.5 text-xs font-mono text-muted-foreground truncate relative z-10 pointer-events-none">
        <Barcode className="size-3.5" /> {producto.skuBarcode}
      </div>

      <div className="flex items-baseline justify-between relative z-10 pointer-events-none">
        <span className="text-lg font-bold text-foreground">
          ${Number(producto.precioVenta).toLocaleString("es-CO")}
        </span>
        <span className="text-xs text-muted-foreground">
          {producto.stockActual}/{producto.stockMinimo} unidades
        </span>
      </div>

      <div className="w-full h-1.5 rounded-full bg-muted overflow-hidden relative z-10 pointer-events-none">
        <div
          className={`h-full rounded-full transition-all ${progressColor}`}
          style={{ width: `${progress}%` }}
        />
      </div>
    </div>
  );
}
