import { z } from "zod";

export const habitacionSchema = z.object({
  numero: z
    .string({
      error: "El número es obligatorio",
    })
    .min(1, "El número debe tener al menos 1 carácter")
    .max(20, "El número no puede superar los 20 caracteres"),

  tipoId: z
    .number({
      error: "Debe seleccionar un tipo de habitación",
    })
    .min(1, "Debe seleccionar un tipo de habitación"),

  descripcion: z
    .string()
    .max(255, "La descripción no puede superar los 255 caracteres")
    .optional(),

  estado: z.enum(["DISPONIBLE", "RESERVADA", "OCUPADA", "EN_LIMPIEZA", "MANTENIMIENTO", "INHABILITADA"], {
    error: "Debe seleccionar un estado",
  }),
});

export type HabitacionFormValues = z.infer<typeof habitacionSchema>;