import { FormProvider, useForm, useWatch } from "react-hook-form";
import {
  userUpdateSchema,
  type UserUpdateFormValues,
} from "../schemas/user.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import { toUserUpdateRequest } from "../utils/mapUser";

import { Button } from "@/components/ui/button";
import { useUpdateUser } from "../hooks/useUserMutations";
import FormGlobalError from "@/components/form/FormGlobalError";
import { useParams } from "react-router-dom";
import ConfirmPasswordField from "../fields/ConfirmPasswordField";
import EmailField from "../fields/EmailField";
import PasswordField from "../fields/PasswordField";
import RolesField from "../fields/RolesField";
import UsernameField from "../fields/UsernameField";

interface Props {
  isLoading?: boolean;
  defaultValues?: Partial<UserUpdateFormValues>;
}

function UserEditForm({ isLoading, defaultValues }: Props) {
  const { id } = useParams();
  const userId = Number(id);

  const form = useForm<UserUpdateFormValues>({
    resolver: zodResolver(userUpdateSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      email: defaultValues?.email ?? "",
      username: defaultValues?.username ?? "",
      password: "",
      confirmPassword: "",
      roles: defaultValues?.roles ?? [],
      activo: defaultValues?.activo ?? true,
    },
  });

  const activo = useWatch({ control: form.control, name: "activo" });

  const onSubmit = (formData: UserUpdateFormValues) => {
    updateUser.mutate(toUserUpdateRequest(formData));
  };

  const updateUser = useUpdateUser(form, userId);
  const globalError = form.formState.errors.root?.message;

  return (
    <FormProvider {...form}>
      <Form {...form}>
        <form onSubmit={form.handleSubmit(onSubmit)} className="space-y-8">
          <FormGlobalError message={globalError} />

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Información de cuenta
              </h2>

              <p className="text-sm text-muted-foreground">
                Datos de acceso del usuario.
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <EmailField />
              <UsernameField />
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Seguridad
              </h2>

              <p className="text-sm text-muted-foreground">
                Cambiar contraseña (opcional, dejar en blanco para mantener la
                actual).
              </p>
            </div>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2">
              <PasswordField />
              <ConfirmPasswordField />
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-6 shadow-sm">
            <div className="mb-6">
              <h2 className="text-lg font-semibold tracking-tight">
                Roles y estado
              </h2>

              <p className="text-sm text-muted-foreground">
                Permisos y estado del usuario en el sistema.
              </p>
            </div>

            <div className="space-y-4">
              <RolesField />

              <div className="flex items-center gap-3">
                <input
                  type="checkbox"
                  id="activo"
                  checked={activo}
                  onChange={(e) => form.setValue("activo", e.target.checked)}
                  className="size-4 rounded border border-input accent-rose-600"
                />
                <label
                  htmlFor="activo"
                  className="text-sm font-medium leading-none">
                  Usuario activo
                </label>
              </div>
            </div>
          </section>

          <div className="flex items-center justify-end gap-3">
            <Button
              type="submit"
              disabled={updateUser.isPending || isLoading}
              className="bg-rose-800 text-white">
              {updateUser.isPending || isLoading
                ? "Guardando..."
                : "Guardar cambios"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default UserEditForm;
