import { z } from "zod";
import { AlertaRequestTipo } from "@/api/generated/openAPIDefinition.schemas";

const tipoEnum = z.enum(AlertaRequestTipo);

export const alertaSchema = z.object({
  clienteId: z.number({
    error: "El cliente es obligatorio",
  }),

  tipo: tipoEnum,

  mensaje: z
    .string()
    .min(1, "El mensaje es obligatorio")
    .max(255, "El mensaje no puede exceder 255 caracteres"),
});

export type AlertaFormValues = z.infer<typeof alertaSchema>;
