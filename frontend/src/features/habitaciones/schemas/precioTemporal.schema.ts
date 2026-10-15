import { z } from "zod";

export const precioTemporalSchema = z
  .object({
    fechaInicio: z.string().min(1, "La fecha de inicio es obligatoria"),
    fechaFin: z.string().min(1, "La fecha de fin es obligatoria"),
    precioNoche: z
      .number({ error: "El precio por noche es obligatorio" })
      .min(0.01, "El precio debe ser mayor a 0"),
    precioPersonaExtra: z
      .number({ error: "El precio persona extra es obligatorio" })
      .min(0.01, "El precio debe ser mayor a 0"),
    // Opcional: precio de la noche ya con IVA, usado cuando el detalle de la
    // reserva queda en modo IVA. Sin valor, el backend cae al precio base.
    precioNocheIva: z
      .number({ error: "El precio con IVA debe ser un número" })
      .min(0, "El precio con IVA no puede ser negativo")
      .optional(),
  })
  .refine((data) => data.fechaInicio <= data.fechaFin, {
    message: "La fecha de inicio debe ser anterior a la fecha de fin",
    path: ["fechaFin"],
  });

export type PrecioTemporalFormValues = z.infer<typeof precioTemporalSchema>;
