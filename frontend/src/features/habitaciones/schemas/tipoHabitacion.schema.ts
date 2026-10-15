import { z } from "zod";

export const tipoHabitacionSchema = z.object({
  nombre: z
    .string({
      error: "El nombre es obligatorio",
    })
    .min(2, "El nombre debe tener al menos 2 caracteres")
    .max(50, "El nombre no puede superar los 50 caracteres"),

  descripcion: z
    .string()
    .max(255, "La descripción no puede superar los 255 caracteres")
    .optional(),

  capacidad: z
    .number({
      error: "La capacidad es obligatoria",
    })
    .min(1, "La capacidad mínima es 1"),

  precioBase: z
    .number({
      error: "El precio base es obligatorio",
    })
    .min(0.01, "El precio base debe ser mayor a 0"),

  precioPersonaExtra: z
    .number({
      error: "El precio por persona extra es obligatorio",
    })
    .min(0, "El precio no puede ser negativo"),

  // Opcional: alimenta el precio "con IVA" que se aplica cuando un detalle de
  // reserva queda en modo IVA. Sin valor, el backend cae al precio base.
  precioConIva: z
    .number({ error: "El precio con IVA debe ser un número" })
    .min(0, "El precio con IVA no puede ser negativo")
    .optional(),
});

export type TipoHabitacionFormValues = z.infer<typeof tipoHabitacionSchema>;