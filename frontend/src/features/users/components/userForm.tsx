import { FormProvider, useForm } from "react-hook-form";
import {
  userSchema,
  type UserFormValues,
} from "../schemas/user.schema";

import { zodResolver } from "@hookform/resolvers/zod";
import { Form } from "@/components/ui/form";
import EmailField from "../fields/EmailField";
import UsernameField from "../fields/UsernameField";
import PasswordField from "../fields/PasswordField";
import ConfirmPasswordField from "../fields/ConfirmPasswordField";
import { Button } from "@/components/ui/button";
import { useCreateUser } from "../hooks/useCreateUser";
import { toUserRequest } from "../utils/mapUser";
import FormGlobalError from "@/components/form/FormGlobalError";
import RolesField from "../fields/RolesField";

interface Props {
  isLoading?: boolean;
}

function UserForm({ isLoading }: Props) {
  const form = useForm<UserFormValues>({
    resolver: zodResolver(userSchema),
    mode: "onChange",
    reValidateMode: "onChange",
    defaultValues: {
      email: "",
      username: "",
      password: "",
      confirmPassword: "",
      roles: [],
    },
  });

  const onSubmit = (formData: UserFormValues) => {
    createUser.mutate(toUserRequest(formData));
  };

  const createUser = useCreateUser(form);
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
                Contraseña para el usuario.
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
                Roles
              </h2>

              <p className="text-sm text-muted-foreground">
                Permisos del usuario en el sistema.
              </p>
            </div>

            <RolesField />
          </section>

          <div className="flex items-center justify-end gap-3">
            <Button
              type="submit"
              disabled={createUser.isPending || isLoading}
              className="bg-rose-800 text-white">
              {createUser.isPending || isLoading
                ? "Guardando..."
                : "Guardar usuario"}
            </Button>
          </div>
        </form>
      </Form>
    </FormProvider>
  );
}

export default UserForm;
