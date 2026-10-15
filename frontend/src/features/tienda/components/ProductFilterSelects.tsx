import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { useCategorias } from "../hooks/useTienda";
import type { StockFiltro } from "../types";

interface Props {
  categoriaId: number | null | undefined;
  onCategoriaChange: (v: number | null) => void;
  stock: StockFiltro;
  onStockChange: (v: StockFiltro) => void;
}

export default function ProductFilterSelects({ categoriaId, onCategoriaChange, stock, onStockChange }: Props) {
  const { data: categorias } = useCategorias();

  return (
    <div className="flex gap-3">
      <Select value={categoriaId?.toString() ?? "todas"} onValueChange={(v) => onCategoriaChange(v === "todas" ? null : Number(v))}>
        <SelectTrigger className="w-[180px] h-9 bg-background border-border text-foreground">
          <SelectValue placeholder="Categoría" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="todas">Todas las categorías</SelectItem>
          {categorias?.map((c) => (
            <SelectItem key={c.id} value={String(c.id)}>
              {c.nombre}
            </SelectItem>
          ))}
        </SelectContent>
      </Select>

      <Select value={stock || "todas"} onValueChange={(v) => onStockChange(v as StockFiltro)}>
        <SelectTrigger className="w-[160px] h-9 bg-background border-border text-foreground">
          <SelectValue placeholder="Stock" />
        </SelectTrigger>
        <SelectContent>
          <SelectItem value="todas">Todo stock</SelectItem>
          <SelectItem value="OK">OK</SelectItem>
          <SelectItem value="BAJO">Bajo</SelectItem>
          <SelectItem value="AGOTADO">Agotado</SelectItem>
        </SelectContent>
      </Select>
    </div>
  );
}
