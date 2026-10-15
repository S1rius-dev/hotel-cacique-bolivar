import { useState, useEffect, useRef } from "react";
import { Wallet, Loader2, Plus } from "lucide-react";
import { Button } from "@/components/ui/button";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { toast } from "sonner";
import { useRegistrarPago, FORMAT_CURRENCY } from "@/features/pagos";
import { usePreviewDesglosePago } from "@/features/pagos/hooks/usePagosDesglose";
import DesglosePreview from "@/features/pagos/components/DesglosePreview";
import { useDebounce } from "@/lib/hooks/useDebounce";
import {
  useAgregarAjuste,
  useEliminarAjuste,
  useReservaDetalle,
} from "@/features/reservas/hooks/useReservaDetalle";
import { parseMonto } from "./utils";
import { extractErrorMessage } from "@/lib/extract-error";
import { useConsumos } from "@/features/tienda/hooks/useConsumos";
import FinancialSummary from "./FinancialSummary";
import HabitacionesIvaList from "./HabitacionesIvaList";
import AjustesAplicadosList from "./AjustesAplicadosList";
import AjusteSidePanel from "./AjusteSidePanel";
import PaymentFields from "./PaymentFields";
import MetodoPagoSelector from "./MetodoPagoSelector";
import ReferenciaObservaciones from "./ReferenciaObservaciones";
import { useTogglePrecioModo } from "@/features/reservas/hooks/usePrecioModo";
import type {
  AjusteResponse,
  AjusteRequest,
} from "@/api/generated/openAPIDefinition.schemas";

// En estos estados el backend rechaza el toggle de IVA con 400.
const ESTADOS_TERMINALES = ["CANCELADA", "FINALIZADA", "NO_SHOW"];

interface Props {
  open: boolean;
  reservaId: number;
  total: number;
  totalPagado: number;
  saldoPendiente: number;
  ajustes?: AjusteResponse[];
  onClose: () => void;
}

function RegistrarPagoModal({
  open,
  reservaId,
  total,
  totalPagado,
  saldoPendiente,
  ajustes,
  onClose,
}: Props) {
  const mutation = useRegistrarPago();
  const agregarAjuste = useAgregarAjuste();
  const eliminarAjuste = useEliminarAjuste();
  const togglePrecioModo = useTogglePrecioModo();
  // El desglose de precios lo calcula el backend (PricingBreakdown) y ya viene
  // incluido en la respuesta de la reserva, consumos de tienda incluidos. El front
  // no replica esa aritmetica: solo la consume.
  const { data: reserva } = useReservaDetalle(open ? reservaId : undefined);
  const actualAjustes = (ajustes ?? reserva?.ajustes ?? []).filter(
    (a) => a.activo !== false,
  );
  const desglose = reserva?.desglose;
  const { data: consumos } = useConsumos(open ? reservaId : undefined);
  const totalConsumos = (consumos ?? []).reduce(
    (acc, c) => acc + (c.montoTotal ?? 0),
    0,
  );

  // Los props vienen del padre y quedan desfasados cuando un toggle de IVA
  // cambia el total: la reserva cacheada manda en cuanto aterriza el fetch.
  const totalEfectivo = reserva?.total ?? total;
  const totalPagadoEfectivo = reserva?.totalPagado ?? totalPagado;
  const saldoEfectivo =
    reserva?.saldoPendiente ?? Math.max(0, totalEfectivo - totalPagadoEfectivo);
  const detalles = reserva?.detalles ?? [];
  const reservaTerminal =
    reserva?.estado != null && ESTADOS_TERMINALES.includes(reserva.estado);
  const [pendingDetalleId, setPendingDetalleId] = useState<number | null>(null);

  const [monto, setMonto] = useState(saldoPendiente);
  const [tipoPago, setTipoPago] = useState(
    saldoPendiente >= total ? "TOTAL" : "PARCIAL",
  );
  const [metodoPago, setMetodoPago] = useState("EFECTIVO");
  const [referencia, setReferencia] = useState("");
  const [observaciones, setObservaciones] = useState("");

  const prevOpen = useRef(open);
  useEffect(() => {
    if (open && !prevOpen.current) {
      setMonto(saldoEfectivo);
      setTipoPago(saldoEfectivo >= totalEfectivo ? "TOTAL" : "PARCIAL");
    }
    prevOpen.current = open;
  }, [open, saldoEfectivo, totalEfectivo]);

  // Tras un toggle de IVA el saldo cambia: si el monto seguia al saldo anterior
  // (o lo superaba) se realinea, para no dejar un cobro invalido en pantalla.
  const prevSaldoRef = useRef(saldoEfectivo);
  useEffect(() => {
    const saldoAnterior = prevSaldoRef.current;
    prevSaldoRef.current = saldoEfectivo;
    if (!open || saldoAnterior === saldoEfectivo) return;

    const montoSincronizado =
      monto >= saldoAnterior ? saldoEfectivo : Math.min(monto, saldoEfectivo);
    setMonto(montoSincronizado);
    if (montoSincronizado >= totalEfectivo) setTipoPago("TOTAL");
    else if (tipoPago === "TOTAL") setTipoPago("PARCIAL");
  }, [open, saldoEfectivo, totalEfectivo, monto, tipoPago]);

  const [showSidePanel, setShowSidePanel] = useState(false);
  const [nuevoTipoAjuste, setNuevoTipoAjuste] = useState<string>("CARGO");
  const [nuevoModoCalculo, setNuevoModoCalculo] = useState<string>("FIJO");
  const [nuevoConcepto, setNuevoConcepto] = useState("");
  const [nuevoValor, setNuevoValor] = useState("20000");

  const esValido = monto > 0 && monto <= saldoEfectivo;

  // Solo el efectivo genera movimiento de caja, asi que es el unico metodo con
  // reparto que valga la pena mostrar.
  const esEfectivo = metodoPago === "EFECTIVO";
  const debouncedMonto = useDebounce(monto, 400);
  const previewDesglose = usePreviewDesglosePago({
    reservaId: open ? reservaId : undefined,
    monto: debouncedMonto,
  });
  const mostrarPreviewDesglose = esEfectivo && esValido && monto > 0;

  const subtotal = desglose?.subtotal ?? totalEfectivo;
  const finalTotal = desglose?.totalFinal ?? totalEfectivo;
  const sumaAjustes =
    (desglose?.totalCargos ?? 0) - (desglose?.totalDescuentos ?? 0);

  const handleClose = () => {
    setMonto(saldoEfectivo);
    setTipoPago(saldoEfectivo >= finalTotal ? "TOTAL" : "PARCIAL");
    setMetodoPago("EFECTIVO");
    setReferencia("");
    setObservaciones("");
    setShowSidePanel(false);
    resetInlineForm();
    onClose();
  };

  const resetInlineForm = () => {
    setNuevoTipoAjuste("CARGO");
    setNuevoModoCalculo("FIJO");
    setNuevoConcepto("");
    setNuevoValor("20000");
  };

  const handleAddAjuste = () => {
    if (!nuevoConcepto.trim()) return;
    const valorNum = parseMonto(nuevoValor);
    if (nuevoTipoAjuste !== "CORTESIA" && valorNum <= 0) return;

    const data: AjusteRequest = {
      tipoAjuste: nuevoTipoAjuste as AjusteRequest["tipoAjuste"],
      modoCalculo: nuevoModoCalculo as AjusteRequest["modoCalculo"],
      concepto: nuevoConcepto.trim(),
    };
    if (nuevoTipoAjuste !== "CORTESIA") {
      data.valor = valorNum;
    }

    agregarAjuste.mutate(
      { reservaId, data },
      {
        onSuccess: () => {
          resetInlineForm();
          setShowSidePanel(false);
        },
      },
    );
  };

  const handleDeleteAjuste = (ajuste: AjusteResponse) => {
    if (!ajuste.id) return;
    eliminarAjuste.mutate({ reservaId, ajusteId: ajuste.id });
  };

  const handleToggleIva = (detalleId: number, incluyeIva: boolean) => {
    setPendingDetalleId(detalleId);
    togglePrecioModo.mutate(
      { reservaId, detalleId, incluyeIva },
      { onSettled: () => setPendingDetalleId(null) },
    );
  };

  const handleSubmit = async () => {
    if (!esValido) return;
    await mutation.mutateAsync(
      {
        reservaId,
        monto,
        tipoPago: tipoPago as
          | "TOTAL"
          | "PARCIAL"
          | "ANTICIPO"
          | "DEPOSITO_GARANTIA",
        metodoPago: metodoPago as
          | "EFECTIVO"
          | "TARJETA_CREDITO"
          | "TARJETA_DEBITO"
          | "TRANSFERENCIA"
          | "CHEQUE"
          | "OTRO",
        referenciaExterna: referencia.trim() || undefined,
        observaciones: observaciones.trim() || undefined,
      },
      {
        onSuccess: () => {
          toast.success("Pago registrado exitosamente");
          handleClose();
        },
        onError: (error) => {
          toast.error(extractErrorMessage(error, "Error al registrar el pago"));
        },
      },
    );
  };

  const inlineFormValido =
    nuevoConcepto.trim().length > 0 &&
    (nuevoTipoAjuste === "CORTESIA" || parseMonto(nuevoValor) > 0);

  const handleTipoAjusteChange = (tipo: string) => {
    setNuevoTipoAjuste(tipo);
    if (tipo === "CORTESIA") setNuevoModoCalculo("FIJO");
  };

  const handleMontoChange = (val: number) => {
    setMonto(val);
    if (val >= totalEfectivo) setTipoPago("TOTAL");
    else if (tipoPago === "TOTAL") setTipoPago("PARCIAL");
  };

  const footer = (
    <>
      <Button
        variant="ghost"
        onClick={handleClose}
        disabled={mutation.isPending}
        className="text-muted-foreground hover:text-foreground text-sm h-10 px-4">
        Cancelar
      </Button>
      <Button
        onClick={handleSubmit}
        disabled={!esValido || mutation.isPending}
        className="gap-2 bg-emerald-700 hover:bg-emerald-600 active:scale-95 text-white text-sm h-10 px-5 shadow-sm transition-all duration-150">
        {mutation.isPending && <Loader2 className="w-4 h-4 animate-spin" />}
        {mutation.isPending
          ? "Registrando..."
          : `Pagar ${FORMAT_CURRENCY.format(monto)}`}
      </Button>
    </>
  );

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <DialogContent className="w-[calc(100vw-2rem)] h-[calc(100dvh-2rem)] overflow-hidden flex flex-col gap-4 p-4 sm:h-auto sm:max-h-[calc(100vh-2rem)] sm:p-5 sm:max-w-230">
        <DialogHeader className="shrink-0">
          <DialogTitle className="flex items-center gap-2">
            <Wallet className="w-5 h-5 text-emerald-600" />
            Registrar pago
          </DialogTitle>
        </DialogHeader>

        <div className="min-h-0 flex-1 overflow-hidden relative">
          {/* Formulario de pago */}
          <div
            className={`h-full flex flex-col transition-all duration-300 ${showSidePanel ? "opacity-40 pointer-events-none" : ""}`}>
            <div className="flex-1 min-h-0 overflow-y-auto overscroll-contain py-1 pr-1 thin-scrollbar">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-5 md:gap-6 items-stretch">
                {/* Columna izquierda: contexto del cobro */}
                <div className="flex flex-col gap-5 min-w-0">
                  {totalConsumos > 0 && (
                    <div className="rounded-lg border border-rose-200 bg-rose-50/50 dark:border-rose-900/30 dark:bg-rose-950/20 p-4 text-sm">
                      <p className="font-semibold flex items-center gap-1.5 text-xs text-rose-700 dark:text-rose-300">
                        <span className="size-2 rounded-full bg-rose-500 shrink-0" />{" "}
                        Consumos de tienda añadidos
                      </p>
                      <div className="mt-2 max-h-32 overflow-y-auto pr-1 thin-scrollbar">
                        <div className="space-y-1.5">
                          {(consumos ?? []).map((c) => (
                            <div
                              key={c.id}
                              className="flex justify-between gap-2 text-xs">
                              <span className="text-muted-foreground truncate">
                                {c.productoNombre} ×{c.cantidad}{" "}
                                {c.detalleHabitacion
                                  ? `· Hab ${c.detalleHabitacion}`
                                  : ""}{" "}
                                @{" "}
                                {FORMAT_CURRENCY.format(c.precioUnitario ?? 0)}
                              </span>
                              <span className="font-medium shrink-0 tabular-nums">
                                +{FORMAT_CURRENCY.format(c.montoTotal ?? 0)}
                              </span>
                            </div>
                          ))}
                        </div>
                      </div>
                      <div className="flex justify-between font-bold text-sm border-t border-rose-200 dark:border-rose-900/30 mt-2.5 pt-2">
                        <span>Total consumos</span>
                        <span className="tabular-nums">
                          +{FORMAT_CURRENCY.format(totalConsumos)}
                        </span>
                      </div>
                      <p className="text-xs text-muted-foreground mt-1">
                        Se suma al total de la reserva.
                      </p>
                    </div>
                  )}

                  <HabitacionesIvaList
                    detalles={detalles}
                    disabled={reservaTerminal}
                    pendingDetalleId={pendingDetalleId ?? undefined}
                    onToggle={handleToggleIva}
                  />

                  <FinancialSummary
                    desglose={desglose}
                    subtotal={subtotal}
                    sumaAjustes={sumaAjustes}
                    total={finalTotal}
                    totalPagado={totalPagadoEfectivo}
                    saldoPendiente={saldoEfectivo}
                    ajustes={actualAjustes}
                  />

                  <div className="flex flex-col gap-3 min-h-0">
                    <div className="max-h-40 overflow-y-auto pr-1 thin-scrollbar">
                      <AjustesAplicadosList
                        ajustes={actualAjustes}
                        onDelete={handleDeleteAjuste}
                      />
                    </div>

                    <div className="grid grid-cols-1 sm:grid-cols-2 gap-2.5">
                      <button
                        type="button"
                        onClick={() => {
                          setNuevoTipoAjuste("CARGO");
                          setNuevoModoCalculo("FIJO");
                          setNuevoConcepto("");
                          setShowSidePanel(true);
                        }}
                        className="flex w-full min-w-0 items-center justify-center gap-2 rounded-lg border border-zinc-200/50 bg-zinc-50/50 px-3 py-2.5 text-[13px] font-medium text-muted-foreground shadow-sm transition-colors hover:text-foreground dark:border-zinc-800/50 dark:bg-zinc-900/30">
                        <Plus className="w-4 h-4 text-emerald-600 shrink-0" />
                        Añadir ajuste
                      </button>
                      <button
                        type="button"
                        onClick={() => {
                          setNuevoTipoAjuste("CARGO");
                          setNuevoModoCalculo("TOTAL");
                          setNuevoConcepto("Total definido manualmente");
                          setNuevoValor(String(finalTotal));
                          setShowSidePanel(true);
                        }}
                        className="flex w-full min-w-0 items-center justify-center gap-2 rounded-lg border border-dashed border-emerald-200/70 bg-emerald-50/30 px-3 py-2.5 text-[13px] font-medium text-emerald-700 shadow-sm transition-colors hover:bg-emerald-50 hover:text-emerald-800 dark:border-emerald-800/40 dark:bg-emerald-950/20 dark:text-emerald-400">
                        <Wallet className="w-4 h-4 shrink-0" />
                        Modificar total manualmente
                      </button>
                    </div>
                  </div>

                  <ReferenciaObservaciones
                    referencia={referencia}
                    observaciones={observaciones}
                    onReferenciaChange={setReferencia}
                    onObservacionesChange={setObservaciones}
                  />
                </div>

                {/* Columna derecha: datos del pago */}
                <div className="flex flex-col gap-5 min-w-0">
                  <PaymentFields
                    monto={monto}
                    saldoPendiente={saldoEfectivo}
                    tipoPago={tipoPago}
                    onMontoChange={handleMontoChange}
                    onTipoPagoChange={setTipoPago}
                  />

                  <MetodoPagoSelector
                    value={metodoPago}
                    onChange={setMetodoPago}
                  />

                  {mostrarPreviewDesglose && (
                    <DesglosePreview
                      lineas={previewDesglose.data}
                      isLoading={previewDesglose.isFetching}
                      titulo="A dónde entra este pago"
                    />
                  )}
                </div>
              </div>
            </div>

            <div className="shrink-0 flex flex-col-reverse gap-2.5 border-t border-zinc-200/50 pt-4 sm:flex-row sm:justify-end dark:border-zinc-800/50">
              {footer}
            </div>
          </div>

          {/* Backdrop */}
          {showSidePanel && (
            <div
              className="absolute inset-0 bg-black/10 z-10 transition-opacity duration-300"
              onClick={() => setShowSidePanel(false)}
            />
          )}

          {/* Drawer de ajustes */}
          <div
            className={`absolute top-0 right-0 bottom-0 w-full sm:w-110 z-20 transition-transform duration-300 ease-out ${
              showSidePanel ? "translate-x-0" : "translate-x-full"
            }`}>
            <AjusteSidePanel
              onClose={() => setShowSidePanel(false)}
              tipoAjuste={nuevoTipoAjuste}
              onTipoAjusteChange={handleTipoAjusteChange}
              modoCalculo={nuevoModoCalculo}
              onModoCalculoChange={setNuevoModoCalculo}
              valor={nuevoValor}
              onValorChange={setNuevoValor}
              concepto={nuevoConcepto}
              onConceptoChange={setNuevoConcepto}
              isValid={inlineFormValido}
              isPending={agregarAjuste.isPending}
              onAdd={handleAddAjuste}
              onCancel={() => {
                resetInlineForm();
                setShowSidePanel(false);
              }}
            />
          </div>
        </div>
      </DialogContent>
    </Dialog>
  );
}

export default RegistrarPagoModal;
