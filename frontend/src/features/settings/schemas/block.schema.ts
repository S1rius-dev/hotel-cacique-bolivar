import { z } from "zod";

export const blockSchema = z
  .object({
    fechaInicio: z.date({ message: "La fecha de inicio es obligatoria" }),
    fechaFin: z.date({ message: "La fecha de fin es obligatoria" }),
    motivo: z
      .string({ message: "El motivo es obligatorio" })
      .min(3, "El motivo debe tener al menos 3 caracteres"),
    activo: z.boolean(),
  })
  .refine((data) => data.fechaFin >= data.fechaInicio, {
    message: "La fecha de fin debe ser posterior o igual a la fecha de inicio",
    path: ["fechaFin"],
  });

export type BlockFormValues = z.infer<typeof blockSchema>;

export const reservationBlockSchema = z
  .object({
    fechaInicio: z.date({ message: "La fecha de inicio es obligatoria" }),
    fechaFin: z.date({ message: "La fecha de fin es obligatoria" }),
    tipoMotivo: z.string({ message: "El motivo es obligatorio" }),
    customMotivo: z.string(),
    activo: z.boolean(),
  })
  .refine((data) => {
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return data.fechaInicio.getTime() >= hoy.getTime();
  }, {
    message: "La fecha de inicio no puede ser en el pasado",
    path: ["fechaInicio"],
  })
  .refine((data) => data.fechaFin >= data.fechaInicio, {
    message: "La fecha de fin debe ser posterior o igual a la fecha de inicio",
    path: ["fechaFin"],
  })
  .refine(
    (data) =>
      data.tipoMotivo !== "Otro" ||
      data.customMotivo.trim().length >= 3,
    {
      message: "Especifica el motivo (mínimo 3 caracteres)",
      path: ["customMotivo"],
    },
  )
  .refine((data) => {
    if (!data.activo) return true;
    const hoy = new Date();
    hoy.setHours(0, 0, 0, 0);
    return data.fechaFin.getTime() >= hoy.getTime();
  }, {
    message: "No se puede reactivar un bloqueo con fechas ya finalizadas",
    path: ["activo"],
  });

export type ReservationBlockFormValues = z.infer<typeof reservationBlockSchema>;

export function resolveBlockMotivo(values: ReservationBlockFormValues): string {
  return values.tipoMotivo === "Otro" ? values.customMotivo.trim() : values.tipoMotivo;
}

export const reservationRulesSchema = z.object({
  minAnticipationDays: z.number().min(1, "Debe ser al menos 1"),
  maxAnticipationDays: z
    .number()
    .min(1, "Debe ser al menos 1")
    .max(365, "No puede superar 365 días"),
  hourLimit: z.string().regex(/^([01]\d|2[0-3]):[0-5]\d$/, "Hora inválida"),
  allowSameDay: z.boolean(),
  pendingExpirationMinutes: z
    .number()
    .min(1, "Debe ser al menos 1")
    .max(1440, "No puede superar 1440 minutos"),
});

export type ReservationRulesValues = z.infer<typeof reservationRulesSchema>;