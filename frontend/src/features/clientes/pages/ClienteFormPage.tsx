"use client";

import ClientForm from "../components/ClientForm";
import { FormCard } from "@/components/form/FormCard";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { UserPlus } from "lucide-react";

const ClienteFormPage = () => {
  return (
    <BackHeaderLayout
      title="Crear Cliente"
      subtitle="Completa la información del cliente">
      <FormCard
        title="Información del cliente"
        description="Ingresa los datos del nuevo cliente"
        icon={<UserPlus className="h-5 w-5 text-rose-700" />}>
        <ClientForm></ClientForm>
      </FormCard>
    </BackHeaderLayout>
  );
};

export default ClienteFormPage;
