import { z } from "zod";

export const clienteSchema = z.object({
  cedula: z
    .string({
      error: "La cédula es obligatoria",
    })
    .min(6, "La cédula debe tener al menos 6 caracteres")
    .max(20, "La cédula no puede superar los 20 caracteres")
    .regex(/^[0-9]+$/, "La cédula solo puede contener números"),

  tipoIdentificacion: z
    .number({
      error: "Debe seleccionar un tipo de identificación",
    })
    .min(1, "Debe seleccionar un tipo de identificación"),

  municipioExpedicion: z
    .number({
      error: "Debe seleccionar un municipio de expedición",
    })
    .min(1, "Debe seleccionar un municipio de expedición"),

  nombreCompleto: z
    .string({
      error: "El nombre es obligatorio",
    })
    .min(2, "El nombre debe tener al menos 2 caracteres")
    .max(100, "El nombre no puede superar los 100 caracteres"),

  apellidoCompleto: z
    .string({
      error: "El apellido es obligatorio",
    })
    .min(2, "El apellido debe tener al menos 2 caracteres")
    .max(100, "El apellido no puede superar los 100 caracteres"),

  telefono: z
    .string()
    .regex(/^\d{10}$/, "Ingrese un número de teléfono válido de 10 dígitos")
    .optional()
    .or(z.literal("")),
});

export type ClienteFormValues = z.infer<typeof clienteSchema>;
