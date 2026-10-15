"use client";

import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import UserEditForm from "../components/UserEditForm";
import { useDetailsUser } from "../hooks/useUserMutations";
import { useParams } from "react-router-dom";
import { User } from "lucide-react";
import { FormCard } from "@/components/form/FormCard";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";

const UserEditPage = () => {
  const { id } = useParams();
  const { data: user, isLoading } = useDetailsUser(id);

  if (isLoading) {
    return (
      <BackHeaderLayout title="Usuarios" subtitle="Editar usuario">
        <DetailSkeleton cards={1} />
      </BackHeaderLayout>
    );
  }

  if (!user) {
    return (
      <BackHeaderLayout title="Usuarios" subtitle="Editar usuario">
        <NotFoundState
          title="Usuario no encontrado"
          description="El usuario que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  const rolesArray = Array.isArray(user.roles) ? user.roles : [];

  return (
    <BackHeaderLayout
      title="Editar Usuario"
      subtitle={`Editando usuario: ${user.username}`}>
      <FormCard
        title="Información del usuario"
        description="Edita los datos del usuario"
        icon={<User className="h-5 w-5 text-rose-700" />}>
        <UserEditForm
          isLoading={isLoading}
          defaultValues={{
            email: user.email,
            username: user.username,
            roles: rolesArray,
            activo: user.activo,
          }}
        />
      </FormCard>
    </BackHeaderLayout>
  );
};

export default UserEditPage;
