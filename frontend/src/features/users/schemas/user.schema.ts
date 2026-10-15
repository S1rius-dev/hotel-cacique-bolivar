import { z } from "zod";

export const userSchema = z
  .object({
    email: z
      .string({
        error: "El email es obligatorio",
      })
      .email("Ingrese un email válido")
      .max(120, "El email no puede superar los 120 caracteres"),

    username: z
      .string({
        error: "El usuario es obligatorio",
      })
      .min(2, "El usuario debe tener al menos 2 caracteres")
      .max(100, "El usuario no puede superar los 100 caracteres"),

    password: z
      .string({
        error: "La contraseña es obligatoria",
      })
      .min(6, "La contraseña debe tener al menos 6 caracteres")
      .max(255, "La contraseña no puede superar los 255 caracteres"),

    confirmPassword: z
      .string({
        error: "La confirmación de contraseña es obligatoria",
      })
      .min(6, "La confirmación debe tener al menos 6 caracteres"),

    roles: z.array(z.string()),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Las contraseñas no coinciden",
    path: ["confirmPassword"],
  });

export const userUpdateSchema = z.object({
  email: z
    .string({
      error: "El email es obligatorio",
    })
    .email("Ingrese un email válido")
    .max(120, "El email no puede superar los 120 caracteres"),

  username: z
    .string({
      error: "El usuario es obligatorio",
    })
    .min(2, "El usuario debe tener al menos 2 caracteres")
    .max(100, "El usuario no puede superar los 100 caracteres"),

  password: z
    .string()
    .min(6, "La contraseña debe tener al menos 6 caracteres")
    .max(255, "La contraseña no puede superar los 255 caracteres")
    .optional()
    .or(z.literal("")),

  confirmPassword: z
    .string()
    .min(6, "La confirmación debe tener al menos 6 caracteres")
    .optional()
    .or(z.literal("")),

  roles: z.array(z.string()).min(1, "Selecciona al menos un rol"),

  activo: z.boolean(),
}).refine((data) => {
  if (data.password && data.confirmPassword) {
    return data.password === data.confirmPassword;
  }
  return true;
}, {
  message: "Las contraseñas no coinciden",
  path: ["confirmPassword"],
});

export type UserFormValues = z.infer<typeof userSchema>;
export type UserUpdateFormValues = z.infer<typeof userUpdateSchema>;
