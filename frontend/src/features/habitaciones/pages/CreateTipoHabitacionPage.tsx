import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import TipoHabitacionForm from "../components/TipoHabitacionForm";
const CreateTipoHabitacionPage = () => {
  return (
    <BackHeaderLayout
      title="Nuevo tipo de habitación"
      subtitle="Completa los datos para crear un nuevo tipo de habitación">
      <TipoHabitacionForm />
    </BackHeaderLayout>
  );
};

export default CreateTipoHabitacionPage;
