"use client";

import TipoHabitacionForm from "../components/TipoHabitacionForm";
import { FormCard } from "@/components/form/FormCard";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";

import { LayoutGrid } from "lucide-react";

const TipoHabitacionesFormPage = () => {
  return (
    <BackHeaderLayout
      title="Nuevo tipo de habitación"
      subtitle="Completa los datos para crear un nuevo tipo de habitación">
      <FormCard
        title="Información del tipo de habitación"
        description="Define las características y precios"
        icon={<LayoutGrid className="h-5 w-5 text-rose-700" />}>
        <TipoHabitacionForm />
      </FormCard>
    </BackHeaderLayout>
  );
};

export default TipoHabitacionesFormPage;
