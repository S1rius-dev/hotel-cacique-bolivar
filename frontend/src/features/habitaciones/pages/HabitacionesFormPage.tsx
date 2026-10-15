"use client";

import HabitacionForm from "../components/HabitacionForm";
import { FormCard } from "@/components/form/FormCard";
import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";

import { BedDouble } from "lucide-react";

const HabitacionesFormPage = () => {
  return (
    <BackHeaderLayout
      title="Nueva habitación"
      subtitle="Completa los datos para crear una nueva habitación">
      <FormCard
        title="Información de la habitación"
        description="Ingresa los datos de la nueva habitación"
        icon={<BedDouble className="h-5 w-5 text-rose-700" />}>
        <HabitacionForm />
      </FormCard>
    </BackHeaderLayout>
  );
};

export default HabitacionesFormPage;
