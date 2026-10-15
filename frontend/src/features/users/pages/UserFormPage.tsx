"use client";

import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import { Card } from "@/components/ui/card";
import UserForm from "../components/userForm";

const userFormPage = () => {
  return (
    <BackHeaderLayout
      title="Crear Usuario"
      subtitle="Completa la información del usuario">
      <Card className="p-6">
        <UserForm />
      </Card>
    </BackHeaderLayout>
  );
};

export default userFormPage;


