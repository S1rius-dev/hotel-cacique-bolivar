import { z } from "zod";

export const empleadoSchema = z.object({
  nombres: z.string().min(1, "El nombre es obligatorio").max(100),
  apellidos: z.string().min(1, "Los apellidos son obligatorios").max(100),
  telefono: z.string().regex(/^[0-9]{10}$/, "El teléfono debe tener 10 dígitos").optional().or(z.literal("")),
  cargo: z.enum(["GERENTE", "RECEPCIONISTA", "LIMPIEZA"]),
  fechaIngreso: z.string().min(1, "La fecha de ingreso es obligatoria"),
  activo: z.boolean().optional(),
  usuarioId: z.number().optional(),
});

export type EmpleadoFormValues = z.infer<typeof empleadoSchema>;
