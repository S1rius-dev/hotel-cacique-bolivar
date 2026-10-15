import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import HabitacionForm from "../components/HabitacionForm";

const CreateHabitacionPage = () => {
  return (
    <BackHeaderLayout
      title="Nueva habitación"
      subtitle="Completa los datos para crear una nueva habitación">
      <HabitacionForm />
    </BackHeaderLayout>
  );
};

export default CreateHabitacionPage;
