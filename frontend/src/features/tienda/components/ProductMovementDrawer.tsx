import {
  Sheet,
  SheetContent,
  SheetHeader,
} from "@/components/ui/sheet";
import { useState, useEffect, useRef, useMemo } from "react";
import { useQueryClient, useMutation } from "@tanstack/react-query";
import { getProductoController } from "@/api/generated/producto-controller/producto-controller";
import type { MovimientoRequestTipo } from "@/api/generated/openAPIDefinition.schemas";
import { useMovimientos } from "../hooks/useTienda";
import { useResetOnKey } from "@/lib/useResetOnKey";
import type { Producto } from "../types";

function formatHistorialDate(dateStr: string | undefined): string {
  if (!dateStr) return "—";
  const d = new Date(dateStr);
  if (Number.isNaN(d.getTime())) return String(dateStr);
  const fecha = d.toLocaleDateString("es-CO", {
    day: "numeric",
    month: "long",
    year: "numeric",
  });
  const hora = d.toLocaleTimeString("es-CO", {
    hour: "numeric",
    minute: "2-digit",
    hour12: true,
  });
  return `${fecha} · ${hora}`;
}

type TipoUI = "VENTA" | "COMPRA" | "AJUSTE";
const toBackendTipo: Record<TipoUI, MovimientoRequestTipo> = {
  VENTA: "VENTA",
  COMPRA: "ENTRADA",
  AJUSTE: "AJUSTE",
};
const oppositeTipo: Record<TipoUI, MovimientoRequestTipo> = {
  VENTA: "ENTRADA",
  COMPRA: "VENTA",
  AJUSTE: "VENTA",
};

const MOTIVO_CHIPS: Record<TipoUI, string[]> = {
  VENTA: ["Mostrador", "Habitación", "Cortesía"],
  COMPRA: ["Reposición", "Proveedor", "Devolución"],
  AJUSTE: ["Conteo", "Vencido", "Daño", "Merma"],
};

const QTY_CHIPS = [1, 5, 10, 20];

export default function ProductMovementDrawer({
  producto,
  open,
  onOpenChange,
  onEdit,
}: {
  producto: Producto | null;
  open: boolean;
  onOpenChange: (v: boolean) => void;
  onEdit?: () => void;
}) {
  const { data: movimientos, isLoading } = useMovimientos(
    producto?.id,
    open && !!producto,
  );
  const qc = useQueryClient();
  const api = getProductoController();

  const [tipoUI, setTipoUI] = useState<TipoUI>("VENTA");
  const [cantidadStr, setCantidadStr] = useState("1");
  const [motivo, setMotivo] = useState("");
  const [undo, setUndo] = useState<{
    show: boolean;
    tipo: TipoUI;
    cantidad: number;
    motivo: string;
  } | null>(null);
  const undoTimerRef = useRef<number | null>(null);
  const qtyInputRef = useRef<HTMLInputElement>(null);

  const cantidad = useMemo(() => {
    const n = Number(String(cantidadStr).replace(/[^0-9]/g, ""));
    return isNaN(n) ? 0 : n;
  }, [cantidadStr]);

  const stockActual = producto?.stockActual ?? 0;
  const resultante = useMemo(() => {
    if (tipoUI === "VENTA") return stockActual - cantidad;
    return stockActual + cantidad;
  }, [stockActual, cantidad, tipoUI]);

  const isNegative = tipoUI === "VENTA" && resultante < 0;
  const isCantidadInvalid = cantidad <= 0;

  useEffect(() => {
    if (open) {
      const t = setTimeout(() => qtyInputRef.current?.focus(), 100);
      return () => clearTimeout(t);
    }
  }, [open]);

  // Cada apertura (o producto distinto) arranca en VENTA x1, sin motivo y sin
  // el aviso de deshacer del intento anterior.
  useResetOnKey(`${open}|${producto?.id ?? ""}`, () => {
    if (!open) return;
    setTipoUI("VENTA");
    setCantidadStr("1");
    setMotivo("");
    setUndo(null);
  });

  useEffect(() => {
    return () => {
      if (undoTimerRef.current) window.clearTimeout(undoTimerRef.current);
    };
  }, []);

  const registrarMut = useMutation({
    mutationFn: ({
      id,
      data,
    }: {
      id: number;
      data: Parameters<typeof api.registrarMovimientoProducto>[1];
    }) => api.registrarMovimientoProducto(id, data),
    onSuccess: (_data, vars) => {
      qc.invalidateQueries({ queryKey: ["productos"] });
      qc.invalidateQueries({ queryKey: ["movimientos", vars.id] });
      qc.invalidateQueries({ queryKey: ["tienda-metricas"] });
    },
  });

  const handleRegistrar = () => {
    if (!producto || isCantidadInvalid || isNegative) return;
    const backendTipo = toBackendTipo[tipoUI];
    registrarMut.mutate(
      {
        id: producto.id,
        data: {
          tipo: backendTipo,
          cantidad,
          motivo: motivo || undefined,
        },
      },
      {
        onSuccess: () => {
          setUndo({ show: true, tipo: tipoUI, cantidad, motivo });
          if (undoTimerRef.current) window.clearTimeout(undoTimerRef.current);
          undoTimerRef.current = window.setTimeout(
            () => setUndo(null),
            5000,
          ) as unknown as number;
          setCantidadStr("1");
        },
      },
    );
  };

  const handleDeshacer = () => {
    if (!producto || !undo) return;
    const opposite = oppositeTipo[undo.tipo];
    registrarMut.mutate(
      {
        id: producto.id,
        data: {
          tipo: opposite,
          cantidad: undo.cantidad,
          motivo: `Deshacer: ${undo.motivo || undo.tipo}`,
        },
      },
      {
        onSuccess: () => {
          setUndo(null);
          if (undoTimerRef.current) window.clearTimeout(undoTimerRef.current);
        },
      },
    );
  };

  const adjustQty = (delta: number) => {
    const next = Math.max(1, cantidad + delta);
    setCantidadStr(String(next));
  };

  if (!producto) return null;

  const ctaLabel =
    tipoUI === "VENTA"
      ? "Registrar venta"
      : tipoUI === "COMPRA"
        ? "Registrar compra"
        : "Registrar ajuste";

  const ctaColor =
    tipoUI === "VENTA"
      ? "bg-[#e23f56] hover:bg-[#c93046] text-white"
      : tipoUI === "COMPRA"
        ? "bg-[#3fbf6f] hover:bg-[#35a85f] text-white"
        : "bg-[#6b6b72] hover:bg-[#5a5a60] text-white";

  return (
    <Sheet open={open} onOpenChange={onOpenChange}>
      <SheetContent
        side="right"
        className="bg-card border-border text-foreground w-full sm:max-w-[440px] overflow-y-auto overflow-x-hidden p-0 gap-0 [&>button]:hidden">
        <div className="flex flex-col h-full min-w-0 w-full overflow-x-hidden bg-card">
          <button
            onClick={() => onOpenChange(false)}
            className="absolute top-4 right-4 size-8 rounded-full bg-muted border border-border flex items-center justify-center text-muted-foreground hover:text-foreground transition-colors z-10"
            aria-label="Cerrar">
            ✕
          </button>

          <div className="flex-1 overflow-y-auto overflow-x-hidden px-5 py-6 max-w-[420px] w-full mx-auto min-w-0 bg-card">
            <div className="pb-3">
              <SheetHeader className="p-0 gap-1 text-left">
                <div className="pt-2 space-y-2">
                  <div className="flex items-start justify-between gap-3 pr-8">
                    <div className="min-w-0 flex-1">
                      <p className="text-[15px] font-semibold truncate">
                        {producto.nombre}
                      </p>
                      <p className="text-xs font-mono text-muted-foreground truncate">
                        {producto.skuBarcode} · Stock {producto.stockActual}
                      </p>
                    </div>
                    {onEdit && (
                      <button
                        type="button"
                        onClick={onEdit}
                        className="shrink-0 inline-flex items-center gap-1.5 px-3 py-1.5 rounded-full bg-card border border-border text-xs font-medium text-foreground hover:bg-accent hover:text-accent-foreground transition-colors">
                        <span className="size-3.5 flex items-center justify-center">✎</span>
                        Editar
                      </button>
                    )}
                  </div>
                </div>
              </SheetHeader>
            </div>

            {/* Panel principal */}
            <div className="bg-muted/40 border border-border rounded-[14px] p-4 w-full min-w-0 overflow-x-hidden">
              <p className="text-[15px] font-semibold mb-4">Registrar movimiento</p>

              <span className="text-[13px] text-muted-foreground mb-1.5 block">
                Tipo de movimiento
              </span>
              <div className="flex bg-muted rounded-[10px] p-[3px] gap-[3px] mb-4 w-full min-w-0">
                {(["VENTA", "COMPRA", "AJUSTE"] as TipoUI[]).map((t) => {
                  const isActive = tipoUI === t;
                  const activeClass =
                    t === "VENTA"
                      ? "bg-[#e23f56] text-white"
                      : t === "COMPRA"
                        ? "bg-[#3fbf6f] text-white"
                        : "bg-[#6b6b72] text-white";
                  return (
                    <button
                      key={t}
                      type="button"
                      onClick={() => setTipoUI(t)}
                      className={`flex-1 py-[9px] text-[13.5px] font-semibold rounded-[8px] transition-colors duration-150 ${isActive ? activeClass : "bg-transparent text-muted-foreground hover:text-foreground"}`}>
                      {t === "VENTA" ? "Venta" : t === "COMPRA" ? "Compra" : "Ajuste"}
                    </button>
                  );
                })}
              </div>

              <span className="text-[13px] text-muted-foreground mb-1.5 block">
                Cantidad
              </span>
              <div className="flex items-stretch gap-2 mb-2 w-full min-w-0">
                <button
                  type="button"
                  onClick={() => adjustQty(-1)}
                  className="w-11 shrink-0 bg-muted border border-border rounded-[10px] text-foreground text-[20px] flex items-center justify-center hover:bg-accent transition-colors"
                  aria-label="Disminuir">
                  −
                </button>
                <input
                  ref={qtyInputRef}
                  type="text"
                  inputMode="numeric"
                  autoFocus
                  value={cantidadStr}
                  onChange={(e) => {
                    const digits = e.target.value.replace(/[^0-9]/g, "");
                    if (digits === "") setCantidadStr("");
                    else setCantidadStr(String(Number(digits)));
                  }}
                  onBlur={() => {
                    if (cantidadStr === "" || cantidad <= 0) setCantidadStr("1");
                  }}
                  className="flex-1 min-w-0 w-full bg-background border border-input rounded-[10px] text-foreground text-[20px] font-semibold text-center py-2 focus:outline-none focus:border-ring focus:ring-0"
                />
                <button
                  type="button"
                  onClick={() => adjustQty(1)}
                  className="w-11 shrink-0 bg-muted border border-border rounded-[10px] text-foreground text-[20px] flex items-center justify-center hover:bg-accent transition-colors"
                  aria-label="Aumentar">
                  +
                </button>
              </div>
              <div className="flex gap-1.5 mb-3.5 w-full min-w-0 flex-wrap">
                {QTY_CHIPS.map((n) => (
                  <button
                    key={n}
                    type="button"
                    onClick={() => setCantidadStr(String(n))}
                    className={`px-3 py-[5px] rounded-full text-[12.5px] border transition-colors ${cantidad === n ? "bg-background border-[#e23f56] text-foreground" : "bg-muted border-border text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground"}`}>
                    {n}
                  </button>
                ))}
              </div>

              <div
                className={`flex items-center justify-between bg-background border rounded-[10px] px-3.5 py-3 mb-4 transition-colors duration-200 w-full min-w-0 ${isNegative ? "border-[#e23f56] bg-[#e23f56]/10 dark:bg-[#3a1c22]/40" : "border-border"}`}>
                <div className="flex flex-col">
                  <div className="flex items-center gap-2 text-[13.5px]">
                    <span className="text-muted-foreground">Stock {stockActual}</span>
                    <span className="text-muted-foreground">→</span>
                    <span className={`font-bold ${isNegative ? "text-[#e23f56]" : "text-[#3fbf6f]"}`}>
                      {resultante}
                    </span>
                  </div>
                  {isNegative && (
                    <span className="text-[11.5px] text-[#e23f56] mt-1 flex items-center gap-1 animate-in fade-in-0 duration-200">
                      ⚠ No hay suficiente stock para esta venta
                    </span>
                  )}
                </div>
              </div>

              <span className="text-[13px] text-muted-foreground mb-1.5 block">
                Motivo (opcional)
              </span>
              <input
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder={
                  tipoUI === "VENTA"
                    ? "Ej: Habitación 204..."
                    : tipoUI === "COMPRA"
                      ? "Ej: Proveedor, factura..."
                      : "Ej: Conteo, vencido..."
                }
                className="w-full min-w-0 bg-background border border-input rounded-[10px] text-foreground placeholder:text-muted-foreground/60 px-3 py-[11px] text-[14px] focus:outline-none focus:border-ring mb-2"
              />
              <div className="flex flex-wrap gap-1.5 mb-[18px] w-full min-w-0">
                {MOTIVO_CHIPS[tipoUI].map((chip) => (
                  <button
                    key={chip}
                    type="button"
                    onClick={() => setMotivo(chip)}
                    className={`px-3 py-1.5 rounded-full text-[12.5px] border transition-colors ${motivo === chip ? "bg-background border-[#e23f56] text-foreground" : "bg-muted border-border text-muted-foreground hover:border-muted-foreground/30 hover:text-foreground"}`}>
                    {chip}
                  </button>
                ))}
              </div>
            </div>

            <p className="text-[12px] tracking-wide text-muted-foreground mt-5 mb-2.5 ml-0.5 uppercase">
              Historial
            </p>
            {isLoading ? (
              <div className="space-y-2">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="h-[64px] rounded-[12px] bg-muted border border-border animate-pulse" />
                ))}
              </div>
            ) : !movimientos?.length ? (
              <p className="text-sm text-muted-foreground text-center py-6 bg-muted/30 border border-border rounded-[12px]">
                Sin movimientos
              </p>
            ) : (
              <div className="space-y-2.5">
                {movimientos.map((m) => {
                  const tipoRaw = String(m.tipo).toUpperCase();
                  const isEntrada = tipoRaw === "ENTRADA" || tipoRaw === "COMPRA";
                  const isVenta = tipoRaw === "VENTA" || tipoRaw === "SALIDA";
                  const iconBg = isEntrada
                    ? "bg-emerald-500/15 dark:bg-[#173a26] text-emerald-600 dark:text-[#3fbf6f] border border-emerald-500/20 dark:border-0"
                    : isVenta
                      ? "bg-red-500/15 dark:bg-[#3a1c22] text-red-600 dark:text-[#e23f56] border border-red-500/20 dark:border-0"
                      : "bg-muted text-muted-foreground border border-border";
                  const arrow = isEntrada ? "↑" : isVenta ? "↓" : "↔";
                  const label = isEntrada ? "Compra" : isVenta ? "Venta" : "Ajuste";
                  return (
                    <div key={m.id} className="flex items-center gap-3 bg-card border border-border rounded-[12px] px-3.5 py-3">
                      <span
                        className={`size-[34px] rounded-[9px] flex items-center justify-center text-[15px] font-bold shrink-0 ${iconBg}`}>
                        {arrow}
                      </span>
                      <div className="flex-1 min-w-0">
                        <p className="text-[14px] font-semibold truncate">
                          {label} · {m.cantidad} uds{" "}
                          <span className="text-muted-foreground font-normal">→ {m.stockResultante}</span>
                        </p>
                        <p className="text-xs text-muted-foreground truncate mt-0.5">
                          {formatHistorialDate(m.createdAt)} {m.motivo ? `· ${m.motivo}` : ""}
                        </p>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>

          <div className="px-5 py-4 border-t border-border bg-card shrink-0">
            <div className="max-w-[420px] w-full mx-auto">
              <button
                type="button"
                onClick={handleRegistrar}
                disabled={registrarMut.isPending || isCantidadInvalid || isNegative}
                className={`w-full rounded-[12px] py-3.5 text-[15.5px] font-bold transition-all duration-150 active:scale-[0.99] disabled:opacity-50 disabled:cursor-not-allowed ${ctaColor}`}>
                {registrarMut.isPending ? "Guardando..." : ctaLabel}
              </button>

              {undo?.show && (
                <div className="flex items-center justify-between bg-muted border border-border rounded-[10px] px-3.5 py-2.5 mt-2.5 text-[13.5px] text-muted-foreground animate-in fade-in-0 slide-in-from-top-1 duration-200">
                  <span>
                    ✓{" "}
                    <b className="text-emerald-600 dark:text-[#3fbf6f] font-semibold">
                      {undo.tipo === "VENTA" ? "Venta" : undo.tipo === "COMPRA" ? "Compra" : "Ajuste"} registrada
                    </b>{" "}
                    · {undo.cantidad} uds
                  </span>
                  <button
                    type="button"
                    onClick={handleDeshacer}
                    disabled={registrarMut.isPending}
                    className="text-[#e23f56] font-semibold hover:text-[#ff6b80] transition-colors">
                    Deshacer
                  </button>
                </div>
              )}
            </div>
          </div>
        </div>
      </SheetContent>
    </Sheet>
  );
}
