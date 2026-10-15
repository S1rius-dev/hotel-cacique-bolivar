import { useEffect, useCallback } from "react";
import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import { resenaSchema, type ResenaFormValues } from "../schema/resena.schema";
import { Dialog, DialogClose } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
} from "@/components/ui/public-dialog";
import {
  FormControl,
  FormField,
  FormItem,
  FormLabel,
  FormMessage,
} from "@/components/ui/form";
import { Button } from "@/components/ui/button";
import { Textarea } from "@/components/ui/textarea";
import { Rating } from "@/components/ui/rating";
import { MessageCircle, Loader2 } from "lucide-react";
import { useCreateResena } from "../hooks/useResenas";
import { reverseGeocode } from "../utils/geocode";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
}

function ResenaFormDialog({ open, onOpenChange }: Props) {
  const createResena = useCreateResena();

  const form = useForm<ResenaFormValues>({
    resolver: zodResolver(resenaSchema),
    mode: "onChange",
    defaultValues: {
      calificacion: 0,
      comentario: "",
      ubicacion: "",
    },
  });

  useEffect(() => {
    if (!open) {
      form.reset({
        calificacion: 0,
        comentario: "",
        ubicacion: "",
      });
      return;
    }

    if ("geolocation" in navigator) {
      navigator.geolocation.getCurrentPosition(
        async (position) => {
          const { latitude, longitude } = position.coords;
          const ubicacion = await reverseGeocode(latitude, longitude);
          if (ubicacion) {
            form.setValue("ubicacion", ubicacion);
          }
        },
        () => {},
        { timeout: 5000 },
      );
    }
  }, [open, form]);

  const onSubmit = useCallback(
    async (data: ResenaFormValues) => {
      try {
        await createResena.mutateAsync({
          comentario: data.comentario,
          calificacion: data.calificacion,
          ...(data.ubicacion ? { ubicacion: data.ubicacion } : {}),
        });
        form.reset();
        onOpenChange(false);
      } catch {
        // error toast handled by the mutation
      }
    },
    [createResena, form, onOpenChange],
  );

  const isSubmitting = createResena.isPending;

  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <PublicDialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <MessageCircle className="size-5 text-rose-500" />
            <DialogTitle>Deja tu reseña</DialogTitle>
          </div>
          <DialogDescription>
            Cuéntanos sobre tu experiencia en el hotel.
          </DialogDescription>
        </DialogHeader>

        <FormProvider {...form}>
          <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-5">
            <FormField
              control={form.control}
              name="calificacion"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Calificación *</FormLabel>
                  <Rating
                    value={field.value ?? 0}
                    onValueChange={field.onChange}
                    variant="rose"
                    size={28}
                  />
                  <FormMessage />
                </FormItem>
              )}
            />

            <FormField
              control={form.control}
              name="comentario"
              render={({ field }) => (
                <FormItem>
                  <FormLabel>Comentario *</FormLabel>
                  <FormControl>
                    <Textarea
                      {...field}
                      placeholder="Comparte tu experiencia..."
                      className="min-h-30 resize-none"
                    />
                  </FormControl>
                  <FormMessage />
                </FormItem>
              )}
            />

            <DialogFooter>
              <DialogClose asChild>
                <Button type="button" variant="ghost" disabled={isSubmitting}>
                  Cancelar
                </Button>
              </DialogClose>
              <Button type="submit" disabled={isSubmitting}>
                {isSubmitting && (
                  <Loader2 className="mr-2 size-4 animate-spin" />
                )}
                {isSubmitting ? "Enviando..." : "Enviar reseña"}
              </Button>
            </DialogFooter>
          </form>
        </FormProvider>
      </PublicDialogContent>
    </Dialog>
  );
}

export default ResenaFormDialog;
