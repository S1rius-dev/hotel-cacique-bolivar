import { useCallback, useEffect, useRef } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogFooter } from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Select, SelectContent, SelectItem, SelectTrigger, SelectValue } from "@/components/ui/select";
import { ScanBarcode } from "lucide-react";
import { useActualizarProducto, useCategorias } from "../hooks/useTienda";
import { useBarcodeScanner } from "../hooks/useBarcodeScanner";
import type { Producto } from "../types";

const optionalString = (min = 1) =>
  z.preprocess(
    (v) => (typeof v === "string" && v.trim() === "" ? undefined : v),
    z.string().trim().min(min).optional(),
  );

const optionalPositiveNumber = z.preprocess(
  (v) => {
    if (v === "" || v === undefined || v === null) return undefined;
    const s = String(v).replace(/[^0-9]/g, "");
    if (s === "") return undefined;
    const n = Number(s);
    return Number.isNaN(n) ? undefined : n;
  },
  z.number().positive("Debe ser >0").optional(),
);

const optionalMinNumber = (min = 0) =>
  z.preprocess(
    (v) => {
      if (v === "" || v === undefined || v === null) return undefined;
      const s = String(v).replace(/[^0-9]/g, "");
      if (s === "") return undefined;
      const n = Number(s);
      return Number.isNaN(n) ? undefined : n;
    },
    z.number().min(min).optional(),
  );

const optionalIntMin = (min = 0) =>
  z.preprocess(
    (v) => {
      if (v === "" || v === undefined || v === null) return undefined;
      const s = String(v).replace(/[^0-9]/g, "");
      if (s === "") return undefined;
      const n = Number(s);
      return Number.isNaN(n) ? undefined : n;
    },
    z.number().int().min(min).optional(),
  );

const schema = z.object({
  nombre: optionalString(1),
  skuBarcode: optionalString(1),
  categoriaId: z.preprocess((v) => (v === "" ? undefined : v), z.string().optional()),
  precioVenta: optionalPositiveNumber,
  costo: optionalMinNumber(0),
  stockMinimo: optionalIntMin(0),
});

type FormValues = z.output<typeof schema>;
type FormInput = z.input<typeof schema>;

/** Formatea un monto para el input controlado, tolerando number o string. */
const fmtMonto = (v: unknown): string => {
  if (v == null) return "";
  const digits = String(v).replace(/[^0-9]/g, "");
  return digits ? Number(digits).toLocaleString("es-CO") : "";
};

/**
 * `z.preprocess` no declara el tipo de entrada, asi que el input de estos campos
 * llega como `{}`. En el DOM siempre son strings: el Select y el input de texto
 * los escriben asi, y el `reset` los inicializa con `String(...)`.
 */
const asTexto = (v: unknown): string => (typeof v === "string" ? v : "");

export default function EditProductModal({ producto, open, onOpenChange }: { producto: Producto | null; open: boolean; onOpenChange: (v: boolean) => void }) {
  const { data: categorias } = useCategorias();
  const actualizar = useActualizarProducto();
  const {
    register,
    handleSubmit,
    setValue,
    control,
    reset,
    formState: { errors },
  } = useForm<FormInput, unknown, FormValues>({ resolver: zodResolver(schema) });
  const categoriaId = useWatch({ control, name: "categoriaId" });
  const precioVenta = useWatch({ control, name: "precioVenta" });
  const costo = useWatch({ control, name: "costo" });
  const skuBarcode = useWatch({ control, name: "skuBarcode" });

  const skuInputRef = useRef<HTMLInputElement>(null);

  const handleScan = useCallback(
    (code: string) => {
      if (!open) return;
      setValue("skuBarcode", code, { shouldValidate: true, shouldDirty: true });
      skuInputRef.current?.focus();
    },
    [open, setValue],
  );

  const { onKeyDown: onBarcodeKeyDown } = useBarcodeScanner(handleScan);

  useEffect(() => {
    if (producto && open) {
      reset({
        nombre: producto.nombre,
        skuBarcode: producto.skuBarcode ?? undefined,
        categoriaId: producto.categoria?.id ? String(producto.categoria.id) : undefined,
        precioVenta: producto.precioVenta,
        costo: producto.costo,
        stockMinimo: producto.stockMinimo,
      });
    }
  }, [producto, open, reset]);

  // permite cambios parciales: solo envía lo que el usuario tocó / tiene valor
  const buildPayload = (values: FormValues) => {
    const data: Record<string, unknown> = {};
    const nombre = values.nombre?.trim();
    if (nombre) data.nombre = nombre;
    const sku = values.skuBarcode?.trim();
    if (sku) data.skuBarcode = sku;
    if (values.categoriaId !== undefined && values.categoriaId !== "") data.categoriaId = Number(values.categoriaId);
    if (values.precioVenta !== undefined) data.precioVenta = Number(values.precioVenta);
    if (values.costo !== undefined) data.costo = Number(values.costo);
    if (values.stockMinimo !== undefined) data.stockMinimo = Number(values.stockMinimo);
    return data as Parameters<typeof actualizar.mutate>[0]["data"];
  };

  if (!producto) return null;

  const onSubmit = (values: FormValues) => {
    const data = buildPayload(values);
    if (Object.keys(data).length === 0) {
      onOpenChange(false);
      return;
    }
    actualizar.mutate(
      {
        id: producto.id,
        data,
      },
      { onSuccess: () => onOpenChange(false) },
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-card border-border text-foreground max-w-lg">
        <DialogHeader>
          <DialogTitle>Editar producto</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <Label className="text-muted-foreground">Nombre</Label>
            <Input {...register("nombre")} className="bg-background border-border text-foreground" placeholder={producto.nombre} />
            {errors.nombre && <p className="text-xs text-[#e0616a] mt-1">{errors.nombre.message as string}</p>}
          </div>
          <div>
            <Label className="text-muted-foreground">SKU</Label>
            <div className="flex gap-2">
              <Input
                value={asTexto(skuBarcode)}
                onChange={(e) => setValue("skuBarcode", e.target.value, { shouldValidate: true, shouldDirty: true })}
                onKeyDown={onBarcodeKeyDown}
                ref={skuInputRef}
                className="bg-background border-border text-foreground flex-1"
                placeholder="Escanea o escribe"
              />
              <Button
                type="button"
                variant="outline"
                onClick={() => skuInputRef.current?.focus()}
                className="border-border text-foreground hover:bg-accent hover:text-accent-foreground"
                title="Enfoca el campo y escanea con el lector USB">
                <ScanBarcode className="size-4" /> Escanear
              </Button>
            </div>
            {errors.skuBarcode && <p className="text-xs text-[#e0616a] mt-1">{errors.skuBarcode.message as string}</p>}
          </div>
          <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
            <div>
              <Label className="text-muted-foreground">Categoría</Label>
              <Select value={asTexto(categoriaId)} onValueChange={(v) => setValue("categoriaId", v)}>
                <SelectTrigger className="bg-background border-border text-foreground">
                  <SelectValue placeholder="Seleccionar" />
                </SelectTrigger>
                <SelectContent>
                  {categorias?.map((c) => (
                    <SelectItem key={c.id} value={String(c.id)}>
                      {c.nombre}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div>
              <Label className="text-muted-foreground">Precio venta</Label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-base font-bold text-[#c81e3a] z-10 pointer-events-none">
                  $
                </span>
                <Input
                  type="text"
                  inputMode="numeric"
                  placeholder="0"
                  value={fmtMonto(precioVenta)}
                  onChange={(e) => {
                    const digits = e.target.value.replace(/[^0-9]/g, "");
                    setValue("precioVenta", digits ? Number(digits) : undefined, { shouldValidate: true, shouldDirty: true });
                  }}
                  className="bg-background border-border pl-9 tabular-nums font-medium"
                />
              </div>
              {errors.precioVenta && <p className="text-xs text-[#e0616a] mt-1">{errors.precioVenta.message as string}</p>}
            </div>
            <div>
              <Label className="text-muted-foreground">Costo</Label>
              <div className="relative">
                <span className="absolute left-3.5 top-1/2 -translate-y-1/2 text-base font-bold text-[#c81e3a] z-10 pointer-events-none">
                  $
                </span>
                <Input
                  type="text"
                  inputMode="numeric"
                  placeholder="0"
                  value={fmtMonto(costo)}
                  onChange={(e) => {
                    const digits = e.target.value.replace(/[^0-9]/g, "");
                    setValue("costo", digits ? Number(digits) : undefined, { shouldValidate: true, shouldDirty: true });
                  }}
                  className="bg-background border-border pl-9 tabular-nums font-medium"
                />
              </div>
              {errors.costo && <p className="text-xs text-[#e0616a] mt-1">{errors.costo.message as string}</p>}
            </div>
          </div>
          <DialogFooter>
            <Button type="button" variant="outline" onClick={() => onOpenChange(false)} className="border-border">
              Cancelar
            </Button>
            <Button type="submit" disabled={actualizar.isPending} className="bg-[#c81e3a] hover:bg-[#a0182f]">
              Guardar
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
