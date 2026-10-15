import { useEffect } from "react";
import { useForm, useWatch } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  Form,
  FormField,
  FormItem,
  FormLabel,
  FormControl,
  FormMessage,
} from "@/components/ui/form";
import FormGlobalError from "@/components/form/FormGlobalError";
import { BadgeDollarSign, Trash2, CalendarRange, Loader2 } from "lucide-react";
import { formatCurrency, formatNumber } from "@/utils/format";
import { formatDateShort } from "@/utils/date";
import { DatePicker } from "@/components/ui/date-picker";
import {
  precioTemporalSchema,
  type PrecioTemporalFormValues,
} from "../schemas/precioTemporal.schema";
import { usePreciosTemporales } from "../hooks/usePreciosTemporales";
import { useCreatePrecioTemporal } from "../hooks/useCreatePrecioTemporal";
import { useDeletePrecioTemporal } from "../hooks/useDeletePrecioTemporal";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  tipoHabitacionId: number;
  tipoNombre?: string;
}

export default function PrecioTemporalDialog({
  open,
  onOpenChange,
  tipoHabitacionId,
  tipoNombre,
}: Props) {
  const form = useForm<PrecioTemporalFormValues>({
    resolver: zodResolver(precioTemporalSchema),
    mode: "onChange",
    defaultValues: {
      fechaInicio: "",
      fechaFin: "",
      precioNoche: 0,
      precioPersonaExtra: 0,
      precioNocheIva: undefined,
    },
  });

  const { data: precios, isLoading: isLoadingPrecios } =
    usePreciosTemporales(tipoHabitacionId);

  const createMutation = useCreatePrecioTemporal(form, tipoHabitacionId, () => {
    // keep dialog open to show list; reset is done in hook
  });

  const deleteMutation = useDeletePrecioTemporal();

  const globalError = form.formState.errors.root?.message as string | undefined;
  const fechaInicioValue = useWatch({ control: form.control, name: "fechaInicio" });

  const handleOpenChange = (nextOpen: boolean) => {
    if (!nextOpen) {
      form.reset();
      form.clearErrors();
    }
    onOpenChange(nextOpen);
  };

  useEffect(() => {
    if (open) {
      form.reset({
        fechaInicio: "",
        fechaFin: "",
        precioNoche: 0,
        precioPersonaExtra: 0,
        precioNocheIva: undefined,
      });
      form.clearErrors();
    }
  }, [open, form]);

  const parseISOToDate = (iso: string): Date | undefined => {
    if (!iso) return undefined;
    if (/^\d{4}-\d{2}-\d{2}$/.test(iso)) {
      const [y, m, d] = iso.split("-").map(Number);
      return new Date(y, m - 1, d);
    }
    const parsed = new Date(iso);
    return Number.isNaN(parsed.getTime()) ? undefined : parsed;
  };

  const formatDateToISO = (date: Date | undefined): string => {
    if (!date) return "";
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, "0");
    const d = String(date.getDate()).padStart(2, "0");
    return `${y}-${m}-${d}`;
  };

  const onSubmit = (values: PrecioTemporalFormValues) => {
    createMutation.mutate(values);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-lg max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <BadgeDollarSign className="size-5 text-violet-600" />
            <DialogTitle>Precio temporal</DialogTitle>
          </div>
          <DialogDescription>
            {tipoNombre
              ? `Configura un precio especial para "${tipoNombre}" en un rango de fechas.`
              : "Configura un precio especial por rango de fechas."}
          </DialogDescription>
        </DialogHeader>

        <Form {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-4">
            <FormGlobalError message={globalError} />

            <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
              <FormField
                control={form.control}
                name="fechaInicio"
                render={({ field }) => (
                  <FormItem className="grid gap-2">
                    <FormLabel>Fecha inicio *</FormLabel>
                    <FormControl>
                      <DatePicker
                        value={parseISOToDate(field.value)}
                        onChange={(date) => field.onChange(formatDateToISO(date))}
                        placeholder="Seleccionar inicio"
                        className="h-10 w-full justify-start rounded-xl"
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
              <FormField
                control={form.control}
                name="fechaFin"
                render={({ field }) => (
                  <FormItem className="grid gap-2">
                    <FormLabel>Fecha fin *</FormLabel>
                    <FormControl>
                      <DatePicker
                        value={parseISOToDate(field.value)}
                        onChange={(date) => field.onChange(formatDateToISO(date))}
                        placeholder="Seleccionar fin"
                        className="h-10 w-full justify-start rounded-xl"
                        minDate={parseISOToDate(fechaInicioValue)}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                )}
              />
            </div>

            <FormField
              control={form.control}
              name="precioNoche"
              render={({ field }) => {
                const rawValue =
                  typeof field.value === "number"
                    ? field.value
                    : Number(String(field.value).replace(/[^\d]/g, "") || 0);
                return (
                  <FormItem>
                    <FormLabel>Precio por noche *</FormLabel>
                    <FormControl>
                      <Input
                        type="text"
                        placeholder="Ej: 150.000"
                        value={rawValue > 0 ? formatNumber(rawValue) : ""}
                        onChange={(e) => {
                          const onlyNumbers = e.target.value.replace(/[^\d]/g, "");
                          const num = onlyNumbers ? parseInt(onlyNumbers, 10) : 0;
                          field.onChange(num);
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />

            <FormField
              control={form.control}
              name="precioPersonaExtra"
              render={({ field }) => {
                const rawValue =
                  typeof field.value === "number"
                    ? field.value
                    : Number(String(field.value).replace(/[^\d]/g, "") || 0);
                return (
                  <FormItem>
                    <FormLabel>Precio persona extra *</FormLabel>
                    <FormControl>
                      <Input
                        type="text"
                        placeholder="Ej: 30.000"
                        value={rawValue > 0 ? formatNumber(rawValue) : ""}
                        onChange={(e) => {
                          const onlyNumbers = e.target.value.replace(/[^\d]/g, "");
                          const num = onlyNumbers ? parseInt(onlyNumbers, 10) : 0;
                          field.onChange(num);
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                  </FormItem>
                );
              }}
            />

            <FormField
              control={form.control}
              name="precioNocheIva"
              render={({ field }) => {
                const rawValue = parseInt(String(field.value ?? ""), 10);
                return (
                  <FormItem>
                    <FormLabel>Precio por noche con IVA</FormLabel>
                    <FormControl>
                      <Input
                        type="text"
                        placeholder="Ej: 178.500"
                        value={
                          Number.isFinite(rawValue) && rawValue > 0
                            ? formatNumber(rawValue)
                            : ""
                        }
                        onChange={(e) => {
                          const onlyNumbers = e.target.value.replace(/[^\d]/g, "");
                          const num = onlyNumbers
                            ? parseInt(onlyNumbers, 10)
                            : undefined;
                          field.onChange(num);
                        }}
                      />
                    </FormControl>
                    <FormMessage />
                    <p className="text-xs text-muted-foreground">
                      Opcional. Se usa cuando la habitación cobra con IVA.
                    </p>
                  </FormItem>
                );
              }}
            />

            <DialogFooter>
              <Button
                type="button"
                variant="ghost"
                onClick={() => handleOpenChange(false)}
                disabled={createMutation.isPending}
              >
                Cancelar
              </Button>
              <Button
                type="submit"
                disabled={createMutation.isPending}
                className="bg-violet-600 hover:bg-violet-700 text-white"
              >
                {createMutation.isPending && (
                  <Loader2 className="mr-2 size-4 animate-spin" />
                )}
                Guardar precio
              </Button>
            </DialogFooter>
          </form>
        </Form>

        <div className="pt-4 border-t">
          <h4 className="flex items-center gap-2 text-sm font-semibold mb-3">
            <CalendarRange className="size-4 text-violet-600" />
            Precios temporales vigentes
          </h4>

          {isLoadingPrecios ? (
            <div className="flex items-center justify-center py-6">
              <Loader2 className="size-5 animate-spin text-muted-foreground" />
            </div>
          ) : !precios || precios.length === 0 ? (
            <p className="text-sm text-muted-foreground py-2">
              No hay precios temporales configurados para este tipo.
            </p>
          ) : (
            <ul className="space-y-2 max-h-48 overflow-y-auto pr-1">
              {precios.map((p) => (
                <li
                  key={p.id}
                  className="flex items-center justify-between gap-3 rounded-lg border border-border/50 bg-muted/20 px-3 py-2.5"
                >
                  <div className="min-w-0 space-y-0.5">
                    <p className="text-sm font-medium">
                      {formatDateShort(p.fechaInicio)} → {formatDateShort(p.fechaFin)}
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {p.precioNoche != null
                        ? `${formatCurrency(p.precioNoche)} COP / noche`
                        : ""}
                      {p.precioNocheIva != null
                        ? ` · Con IVA ${formatCurrency(p.precioNocheIva)} COP`
                        : ""}
                      {p.precioPersonaExtra != null
                        ? ` · Extra ${formatCurrency(p.precioPersonaExtra)} COP`
                        : ""}
                    </p>
                  </div>
                  <Button
                    variant="ghost"
                    size="icon-sm"
                    onClick={() => p.id != null && deleteMutation.mutate(p.id)}
                    disabled={deleteMutation.isPending}
                    className="shrink-0 text-muted-foreground hover:text-destructive"
                    aria-label="Eliminar precio temporal"
                  >
                    {deleteMutation.isPending ? (
                      <Loader2 className="size-4 animate-spin" />
                    ) : (
                      <Trash2 className="size-4" />
                    )}
                  </Button>
                </li>
              ))}
            </ul>
          )}
        </div>
      </DialogContent>
    </Dialog>
  );
}
