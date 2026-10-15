import { z } from "zod";

export const pqrsSchema = z.object({
  tipo: z.enum([
    "QUEJA",
    "PETICION",
    "RECLAMO",
    "SUGERENCIA",
    "FELICITACION",
    "REPORTE_TECNICO",
  ]),
  asunto: z
    .string()
    .min(5, "Describe brevemente el asunto (mín. 5 caracteres)")
    .max(100, "Máximo 100 caracteres"),
  descripcion: z
    .string()
    .min(20, "Cuéntanos con más detalle (mín. 20 caracteres)")
    .max(2000, "Máximo 2000 caracteres"),
  reservaId: z.string().optional(),
  nombreContacto: z.string().optional(),
  emailContacto: z.string().email("Ingresa un email válido").optional().or(z.literal("")),
  telefonoContacto: z.string().optional(),
});

export type PQRSFormValues = z.infer<typeof pqrsSchema>;
