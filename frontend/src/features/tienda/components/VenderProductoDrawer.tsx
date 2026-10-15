import { useState, useMemo, useRef, useCallback } from "react";
import { useQueryClient } from "@tanstack/react-query";
import {
  Sheet,
  SheetContent,
  SheetHeader,
  SheetTitle,
} from "@/components/ui/sheet";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import {
  ShoppingBag,
  Plus,
  Minus,
  Trash2,
  Search,
  BedDouble,
  ScanBarcode,
} from "lucide-react";
import { toast } from "sonner";
import {
  useProductos,
  productoPorCodigoQuery,
  fetchProductoPorCodigo,
} from "../hooks/useTienda";
import { useResetOnKey } from "@/lib/useResetOnKey";
import { useAgregarConsumo } from "../hooks/useConsumos";
import { useReservas } from "@/features/reservas/hooks/useReservas";
import { useReservaDetalle } from "@/features/reservas/hooks/useReservaDetalle";
import type { Producto } from "../types";
import { FORMAT_CURRENCY } from "@/features/pagos";

type CartItem = {
  producto: Producto;
  cantidad: number;
  detalleReservaId?: number;
};

interface Props {
  open: boolean;
  onOpenChange: (v: boolean) => void;
  preselectedReservaId?: number | null;
  preselectedDetalleId?: number | null;
}

export default function VenderProductoDrawer({
  open,
  onOpenChange,
  preselectedReservaId,
  preselectedDetalleId,
}: Props) {
  const [reservaId, setReservaId] = useState<number | null>(
    preselectedReservaId ?? null,
  );
  const [detalleId, setDetalleId] = useState<number | null>(
    preselectedDetalleId ?? null,
  );
  const [search, setSearch] = useState("");
  const [cart, setCart] = useState<CartItem[]>([]);
  const [observaciones, setObservaciones] = useState("");
  const searchInputRef = useRef<HTMLInputElement>(null);
  const qc = useQueryClient();

  const { data: reservasPage } = useReservas(0, 50, { estado: "EN_CURSO" });
  const reservasEnCurso = reservasPage?.content ?? [];

  const { data: reservaDetalle } = useReservaDetalle(reservaId ?? undefined);
  const detalles = useMemo(() => reservaDetalle?.detalles ?? [], [reservaDetalle]);

  const { data: productosPage } = useProductos({
    search: search || undefined,
    page: 0,
    size: 20,
  });
  const productos = productosPage?.content ?? [];

  const agregarMut = useAgregarConsumo();

  // Cada apertura arranca con la reserva pre-seleccionada y el carrito vacio:
  // conservar el carrito anterior cobraria productos de otra reserva.
  useResetOnKey(`${open}|${preselectedReservaId ?? ""}|${preselectedDetalleId ?? ""}`, () => {
    if (!open) return;
    setReservaId(preselectedReservaId ?? null);
    setDetalleId(preselectedDetalleId ?? null);
    setCart([]);
    setObservaciones("");
    setSearch("");
  });

  // Al cambiar de reserva, el detalle se recalcula: null si la reserva todavia
  // no tiene detalles, o el unico existente si tiene exactamente uno.
  useResetOnKey(
    `${reservaId ?? ""}|${detalles.map((d) => d.id ?? 0).join(",")}`,
    () => {
      if (!reservaId) return;
      if (detalles.length === 0) setDetalleId(null);
      else if (detalles.length === 1) setDetalleId(detalles[0].id ?? null);
    },
  );

  const addToCart = useCallback(
    (p: Producto) => {
      if (!reservaId) {
        toast.error("Selecciona una reserva en curso");
        return;
      }
      setCart((prev) => {
        const existing = prev.find(
          (c) => c.producto.id === p.id && c.detalleReservaId === detalleId,
        );
        if (existing) {
          if (existing.cantidad + 1 > p.stockActual) {
            toast.error("Stock insuficiente");
            return prev;
          }
          return prev.map((c) =>
            c.producto.id === p.id && c.detalleReservaId === detalleId
              ? { ...c, cantidad: c.cantidad + 1 }
              : c,
          );
        }
        if (p.stockActual < 1) {
          toast.error("Sin stock");
          return prev;
        }
        return [
          ...prev,
          {
            producto: p,
            cantidad: 1,
            detalleReservaId: detalleId ?? undefined,
          },
        ];
      });
    },
    [reservaId, detalleId],
  );

  /**
   * El codigo escaneado se resuelve desde el propio evento de teclado, no desde
   * un efecto que reaccione a la respuesta. Asi el producto entra al carrito en
   * la misma interaccion y no hace falta un estado intermedio para el codigo.
   */
  const handleBarcodeKeyDown = async (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    const value = (e.target as HTMLInputElement).value.trim();
    if (!value) return;

    const producto = await qc.fetchQuery({
      queryKey: productoPorCodigoQuery(value),
      queryFn: () => fetchProductoPorCodigo(value),
    });
    if (!producto) {
      toast.error(`No se encontro el producto con el codigo ${value}`);
      return;
    }
    addToCart(producto);
    setSearch("");
  };

  const updateQty = (idx: number, delta: number) => {
    setCart((prev) =>
      prev
        .map((c, i) => {
          if (i !== idx) return c;
          const next = c.cantidad + delta;
          if (next < 1) return c;
          if (next > c.producto.stockActual) {
            toast.error("Stock insuficiente");
            return c;
          }
          return { ...c, cantidad: next };
        })
        .filter((c) => c.cantidad >= 1),
    );
  };

  const removeItem = (idx: number) =>
    setCart((prev) => prev.filter((_, i) => i !== idx));

  const total = useMemo(
    () => cart.reduce((acc, c) => acc + c.producto.precioVenta * c.cantidad, 0),
    [cart],
  );

  const handleSubmit = async () => {
    if (!reservaId || cart.length === 0) return;
    let ok = 0;
    for (const item of cart) {
      try {
        await agregarMut.mutateAsync({
          reservaId,
          data: {
            productoId: item.producto.id,
            cantidad: item.cantidad,
            detalleReservaId: item.detalleReservaId ?? undefined,
            observaciones: observaciones || undefined,
          },
        });
        ok++;
      } catch {
        // error handled by hook toast
        break;
      }
    }
    if (ok > 0) {
      toast.success(
        `✓ ${ok} producto(s) cargados · ${FORMAT_CURRENCY.format(total)}`,
      );
      setCart([]);
      onOpenChange(false);
    }
  };

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side="right"
        className="bg-card border-border text-foreground w-full sm:max-w-120 overflow-hidden p-0 gap-0 [&>button]:hidden flex flex-col">
        <div className="flex flex-col h-full min-w-0">
          <button
            onClick={() => onOpenChange(false)}
            className="absolute top-4 right-4 size-8 rounded-full bg-muted border border-border flex items-center justify-center text-muted-foreground hover:text-foreground z-10">
            ✕
          </button>

          <div className="flex-1 overflow-y-auto px-5 py-6 space-y-5 min-w-0">
            <SheetHeader className="p-0 text-left">
              <SheetTitle className="flex items-center gap-2 text-base">
                <ShoppingBag className="size-5 text-[#c81e3a]" /> Vender
                producto a reserva
              </SheetTitle>
              <p className="text-xs text-muted-foreground">
                Solo reservas <b>EN_CURSO</b> • Elige habitación
              </p>
            </SheetHeader>

            {/* Reserva selector */}
            <div className="space-y-2">
              <Label className="text-muted-foreground">Reserva</Label>
              {preselectedReservaId ? (
                <div className="rounded-lg border border-border bg-muted/30 px-3 py-2 text-sm">
                  Reserva #
                  {reservaDetalle?.codigoReserva ?? preselectedReservaId} ·{" "}
                  {reservaDetalle?.titular?.nombreCompleto ?? ""} · Hab{" "}
                  {detalles.find((d) => d.id === detalleId)?.habitacionNumero ??
                    "-"}
                </div>
              ) : (
                <Select
                  value={reservaId ? String(reservaId) : ""}
                  onValueChange={(v) => setReservaId(Number(v))}>
                  <SelectTrigger className="bg-background border-border">
                    <SelectValue placeholder="Seleccionar reserva en curso" />
                  </SelectTrigger>
                  <SelectContent>
                    {reservasEnCurso.map((r) => (
                      <SelectItem key={r.id} value={String(r.id)}>
                        {r.codigoReserva} ·{" "}
                        {r.titular?.nombreCompleto ?? r.titular?.cedula ?? ""} ·{" "}
                        {r.detalles?.map((d) => d.habitacionNumero).join(", ")}
                      </SelectItem>
                    ))}
                    {reservasEnCurso.length === 0 && (
                      <div className="px-3 py-2 text-xs text-muted-foreground">
                        Sin reservas en curso
                      </div>
                    )}
                  </SelectContent>
                </Select>
              )}
              {reservaDetalle && detalles.length > 1 && (
                <div className="pt-1">
                  <Label className="text-muted-foreground text-xs">
                    Habitación (detalle)
                  </Label>
                  <Select
                    value={detalleId ? String(detalleId) : ""}
                    onValueChange={(v) => setDetalleId(v ? Number(v) : null)}>
                    <SelectTrigger className="bg-background border-border h-9 mt-1">
                      <SelectValue placeholder="Cargo global (toda la reserva)" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="">Cargo global</SelectItem>
                      {detalles.map((d) => (
                        <SelectItem key={d.id} value={String(d.id)}>
                          Hab {d.habitacionNumero} · {d.habitacionTipo} ·{" "}
                          {d.estado}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                  <p className="text-[11px] text-muted-foreground mt-1">
                    Si no eliges, se carga global a la reserva. Si eliges, se
                    asocia a esa habitación.
                  </p>
                </div>
              )}
              {reservaDetalle && detalles.length === 1 && (
                <p className="text-xs text-muted-foreground flex items-center gap-1.5">
                  <BedDouble className="size-3.5" /> Hab{" "}
                  {detalles[0].habitacionNumero} · {detalles[0].habitacionTipo}
                </p>
              )}
            </div>

            {/* Producto search */}
            <div className="space-y-2">
              <Label className="text-muted-foreground">Producto</Label>
              <div className="flex gap-2">
                <div className="relative flex-1">
                  <Search className="absolute left-3 top-1/2 -translate-y-1/2 size-4 text-muted-foreground pointer-events-none z-10" />
                  <Input
                    ref={searchInputRef}
                    placeholder="Buscar por nombre o SKU..."
                    value={search}
                    onChange={(e) => setSearch(e.target.value)}
                    onKeyDown={handleBarcodeKeyDown}
                    className="pl-9 bg-background border-border"
                  />
                </div>
                <Button
                  type="button"
                  variant="outline"
                  onClick={() => {
                    searchInputRef.current?.focus();
                  }}
                  className="border-border text-foreground hover:bg-accent hover:text-accent-foreground shrink-0"
                  title="Enfoca el campo y escanea con el lector USB">
                  <ScanBarcode className="size-4" /> Escanear
                </Button>
              </div>
              <div className="grid grid-cols-1 gap-2 max-h-48 overflow-y-auto">
                {productos.map((p) => (
                  <button
                    key={p.id}
                    type="button"
                    onClick={() => addToCart(p)}
                    className="flex items-center justify-between rounded-lg border border-border bg-card hover:border-[#c81e3a]/30 px-3 py-2 text-left">
                    <div className="min-w-0">
                      <p className="text-sm font-medium truncate">{p.nombre}</p>
                      <p className="text-xs text-muted-foreground font-mono truncate">
                        {p.skuBarcode} · Stock {p.stockActual} · $
                        {Number(p.precioVenta).toLocaleString("es-CO")}
                      </p>
                    </div>
                    <span className="ml-2 size-7 rounded-full bg-[#c81e3a] text-white flex items-center justify-center">
                      <Plus className="size-4" />
                    </span>
                  </button>
                ))}
                {productos.length === 0 && (
                  <p className="text-xs text-muted-foreground text-center py-3">
                    Sin productos
                  </p>
                )}
              </div>
            </div>

            {/* Carrito */}
            {cart.length > 0 && (
              <div className="rounded-xl border border-border bg-muted/20 p-3 space-y-2">
                <p className="text-sm font-semibold">
                  Carrito · {cart.length} item(s)
                </p>
                <div className="space-y-2">
                  {cart.map((c, idx) => (
                    <div
                      key={idx}
                      className="flex items-center gap-2 rounded-lg border border-border bg-card px-3 py-2">
                      <div className="flex-1 min-w-0">
                        <p className="text-sm font-medium truncate">
                          {c.producto.nombre}
                        </p>
                        <p className="text-xs text-muted-foreground">
                          $
                          {Number(c.producto.precioVenta).toLocaleString(
                            "es-CO",
                          )}{" "}
                          × {c.cantidad} ={" "}
                          <b className="text-foreground">
                            {FORMAT_CURRENCY.format(
                              c.producto.precioVenta * c.cantidad,
                            )}
                          </b>
                          {c.detalleReservaId
                            ? ` · Hab ${detalles.find((d) => d.id === c.detalleReservaId)?.habitacionNumero ?? c.detalleReservaId}`
                            : " · Global"}
                        </p>
                      </div>
                      <div className="flex items-center gap-1">
                        <Button
                          variant="outline"
                          size="icon"
                          className="size-7"
                          onClick={() => updateQty(idx, -1)}>
                          <Minus className="size-3" />
                        </Button>
                        <span className="w-6 text-center text-sm font-bold">
                          {c.cantidad}
                        </span>
                        <Button
                          variant="outline"
                          size="icon"
                          className="size-7"
                          onClick={() => updateQty(idx, 1)}>
                          <Plus className="size-3" />
                        </Button>
                        <Button
                          variant="ghost"
                          size="icon"
                          className="size-7 text-red-500"
                          onClick={() => removeItem(idx)}>
                          <Trash2 className="size-3.5" />
                        </Button>
                      </div>
                    </div>
                  ))}
                </div>
                <div className="flex items-center justify-between pt-2 border-t border-border text-sm">
                  <span className="text-muted-foreground">Total a cargar</span>
                  <span className="font-bold text-lg">
                    {FORMAT_CURRENCY.format(total)}
                  </span>
                </div>
                <div className="space-y-1">
                  <Label className="text-xs text-muted-foreground">
                    Observaciones (opcional)
                  </Label>
                  <Input
                    value={observaciones}
                    onChange={(e) => setObservaciones(e.target.value)}
                    placeholder="Ej: Minibar, cortesía..."
                    className="bg-background h-8"
                  />
                </div>
              </div>
            )}
          </div>

          <div className="px-5 py-4 border-t border-border bg-card shrink-0">
            <Button
              onClick={handleSubmit}
              disabled={!reservaId || cart.length === 0 || agregarMut.isPending}
              className="w-full bg-[#c81e3a] hover:bg-[#a0182f] text-white h-11">
              {agregarMut.isPending
                ? "Cargando..."
                : cart.length === 0
                  ? "Añade productos"
                  : `Cargar ${cart.length} producto(s) · ${FORMAT_CURRENCY.format(total)}`}
            </Button>
            <p className="text-[11px] text-muted-foreground text-center mt-1.5">
              Se descontará stock y el saldo de la reserva se actualizará
              automáticamente.
            </p>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
