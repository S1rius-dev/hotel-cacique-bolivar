import { useCallback, useRef } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { z } from "zod";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogFooter,
} from "@/components/ui/dialog";
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
import { ScanBarcode } from "lucide-react";
import { useCategorias, useCrearProducto } from "../hooks/useTienda";
import { useBarcodeScanner } from "../hooks/useBarcodeScanner";

const schema = z.object({
  nombre: z.string().min(1, "Requerido"),
  skuBarcode: z.string().optional(),
  categoriaId: z.string().optional(),
  precioVenta: z.coerce.number().positive("Debe ser >0"),
  costo: z.coerce.number().min(0).optional(),
  stockActual: z.coerce.number().int().min(0).optional(),
  stockMinimo: z.coerce.number().int().min(0).optional(),
});

type FormValues = z.output<typeof schema>;
type FormInput = z.input<typeof schema>;

/** Formatea un monto para el input controlado, tolerando number o string. */
const fmtMonto = (v: unknown): string => {
  if (v == null) return "";
  const digits = String(v).replace(/[^0-9]/g, "");
  return digits ? Number(digits).toLocaleString("es-CO") : "";
};

export default function NewProductModal({
  open,
  onOpenChange,
}: {
  open: boolean;
  onOpenChange: (v: boolean) => void;
}) {
  const { data: categorias } = useCategorias();
  const crear = useCrearProducto();
  const {
    register,
    handleSubmit,
    setValue,
    control,
    reset,
    formState: { errors },
  } = useForm<FormInput, unknown, FormValues>({
    resolver: zodResolver(schema),
    defaultValues: { stockActual: 0, stockMinimo: 5 },
  });
  const categoriaId = useWatch({ control, name: "categoriaId" });
  const precioVenta = useWatch({ control, name: "precioVenta" });
  const costo = useWatch({ control, name: "costo" });
  const skuBarcode = useWatch({ control, name: "skuBarcode" });

  const skuInputRef = useRef<HTMLInputElement>(null);

  const handleScan = useCallback(
    (code: string) => {
      if (!open) return;
      setValue("skuBarcode", code, { shouldValidate: true, shouldDirty: true });
      // enfocar input para feedback visual inmediato
      skuInputRef.current?.focus();
    },
    [open, setValue],
  );

  const { onKeyDown: onBarcodeKeyDown } = useBarcodeScanner(handleScan);

  const onSubmit = (values: FormValues) => {
    crear.mutate(
      {
        nombre: values.nombre,
        skuBarcode: values.skuBarcode,
        categoriaId: values.categoriaId ? Number(values.categoriaId) : undefined,
        precioVenta: values.precioVenta,
        costo: values.costo ?? 0,
        stockActual: values.stockActual ?? 0,
        stockMinimo: values.stockMinimo ?? 5,
      },
      {
        onSuccess: () => {
          reset();
          onOpenChange(false);
        },
      },
    );
  };

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="bg-card border-border text-foreground max-w-lg">
        <DialogHeader>
          <DialogTitle className="text-foreground">Nuevo producto</DialogTitle>
        </DialogHeader>
        <form onSubmit={handleSubmit(onSubmit)} className="space-y-4">
          <div>
            <Label className="text-muted-foreground">Nombre</Label>
            <Input
              {...register("nombre")}
              className="bg-background border-border text-foreground"
            />
            {errors.nombre && (
              <p className="text-xs text-[#e0616a]">{errors.nombre.message}</p>
            )}
          </div>

          <div>
            <Label className="text-muted-foreground">SKU / Barcode</Label>
            <div className="flex gap-2">
              <Input
                value={skuBarcode ?? ""}
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
            {errors.skuBarcode && (
              <p className="text-xs text-[#e0616a]">
                {errors.skuBarcode.message}
              </p>
            )}
          </div>

          <div className="grid grid-cols-2 gap-4">
            <div>
              <Label className="text-muted-foreground">Categoría</Label>
              <Select
                value={categoriaId ?? ""}
                onValueChange={(v) => setValue("categoriaId", v)}>
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
                  className="bg-background border-border text-foreground pl-9 tabular-nums font-medium"
                />
              </div>
              {errors.precioVenta && (
                <p className="text-xs text-[#e0616a]">
                  {errors.precioVenta.message}
                </p>
              )}
            </div>
          </div>

          <div className="grid grid-cols-3 gap-4">
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
              {errors.costo && (
                <p className="text-xs text-[#e0616a]">{errors.costo.message}</p>
              )}
            </div>
            <div>
              <Label className="text-muted-foreground">Stock inicial</Label>
              <Input
                type="number"
                {...register("stockActual")}
                className="bg-background border-border"
              />
            </div>
            <div>
              <Label className="text-muted-foreground">Stock mínimo</Label>
              <Input
                type="number"
                {...register("stockMinimo")}
                className="bg-background border-border"
              />
            </div>
          </div>

          <DialogFooter>
            <Button
              type="button"
              variant="outline"
              onClick={() => onOpenChange(false)}
              className="border-border">
              Cancelar
            </Button>
            <Button
              type="submit"
              disabled={crear.isPending}
              className="bg-[#c81e3a] hover:bg-[#a0182f] text-white">
              {crear.isPending ? "Guardando..." : "Crear producto"}
            </Button>
          </DialogFooter>
        </form>
      </DialogContent>
    </Dialog>
  );
}
