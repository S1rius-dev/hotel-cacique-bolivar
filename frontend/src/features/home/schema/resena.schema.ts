import { z } from "zod";

export const resenaSchema = z.object({
  calificacion: z
    .number()
    .min(1, "Debes seleccionar al menos 1 estrella")
    .max(5, "La calificación máxima es 5 estrellas"),
  comentario: z
    .string()
    .min(10, "El comentario debe tener al menos 10 caracteres")
    .max(500, "El comentario no puede exceder 500 caracteres"),
  ubicacion: z
    .string()
    .max(255, "La ubicación no puede exceder 255 caracteres")
    .optional(),
});

export type ResenaFormValues = z.infer<typeof resenaSchema>;
