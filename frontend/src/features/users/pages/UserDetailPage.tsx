import BackHeaderLayout from "@/layouts/dashboard/BackHeaderLayout";
import DetailPageLayout from "@/layouts/dashboard/DetailPageLayout";
import ConfirmDeleteDialog from "@/components/ui/confirm-dialog";
import { DetailSkeleton } from "@/components/ui/DetailSkeleton";
import { NotFoundState } from "@/components/ui/NotFoundState";
import { useParams, useNavigate } from "react-router-dom";
import { useDetailsUser, useDeleteUser } from "../hooks/useUserMutations";
import { useState } from "react";
import {
  User,
  Mail,
  Shield,
  ShieldCheck,
  Clock,
  Calendar,
  BadgeCheck,
  ExternalLink,
} from "lucide-react";
import { Button } from "@/components/ui/button";
import { formatDate } from "@/utils/date";
import GestionRolesDialog from "../components/GestionRolesDialog";

function UserDetailPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const { data: user, isLoading } = useDetailsUser(id);
  const [showDeleteDialog, setShowDeleteDialog] = useState(false);
  const [showRolesDialog, setShowRolesDialog] = useState(false);
  const deleteUser = useDeleteUser();

  const handleDelete = () => {
    if (user?.id) {
      deleteUser.mutate(user.id, {
        onSuccess: () => {
          navigate("/dashboard/usuarios");
        },
      });
    }
  };

  if (isLoading) {
    return (
      <BackHeaderLayout title="Usuarios" subtitle="Detalle del usuario">
        <DetailSkeleton cards={2} />
      </BackHeaderLayout>
    );
  }

  if (!user) {
    return (
      <BackHeaderLayout title="Usuarios" subtitle="Detalle del usuario">
        <NotFoundState
          title="Usuario no encontrado"
          description="El usuario que buscas no existe o fue eliminado."
        />
      </BackHeaderLayout>
    );
  }

  const roles = Array.isArray(user.roles) ? user.roles : [];

  return (
    <BackHeaderLayout title="Usuarios" subtitle="Detalle del usuario">
      <DetailPageLayout
        onDelete={() => setShowDeleteDialog(true)}
        actions={
          <Button variant="outline" size="sm" onClick={() => setShowRolesDialog(true)}>
            <ShieldCheck data-icon="inline-start" />
            <span className="hidden sm:inline">Gestionar roles</span>
          </Button>
        }
        identity={
          <div className="flex items-center gap-4">
            {user.profilePicture ? (
              <img
                src={user.profilePicture}
                alt=""
                className="h-16 w-16 rounded-full object-cover"
              />
            ) : (
              <div className="flex h-16 w-16 items-center justify-center rounded-full bg-rose-100 dark:bg-rose-900/30">
                <User className="h-8 w-8 text-rose-600" />
              </div>
            )}
            <div className="min-w-0">
              <h2 className="text-2xl font-bold truncate">{user.username}</h2>
              <p className="flex items-center gap-1 text-sm text-muted-foreground truncate">
                <Mail className="h-3 w-3 shrink-0" />
                {user.email}
              </p>
            </div>
          </div>
        }>
        <div className="space-y-6">
          <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm">
            <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
              <Shield className="h-5 w-5 text-rose-600" />
              Información de la cuenta
            </h3>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 lg:grid-cols-3 min-w-0">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Estado</p>
                <div className="flex items-center gap-2">
                  {user.activo ? (
                    <>
                      <BadgeCheck className="h-4 w-4 text-green-600" />
                      <span className="font-medium text-green-600">Activo</span>
                    </>
                  ) : (
                    <>
                      <span className="font-medium text-red-600">Inactivo</span>
                    </>
                  )}
                </div>
              </div>

              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Proveedor</p>
                <p className="font-medium break-words">
                  {user.provider || "LOCAL"}
                </p>
              </div>

              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Roles</p>
                <div className="flex flex-wrap gap-1">
                  {roles.map((role, index) => (
                    <span
                      key={index}
                      className="inline-flex items-center rounded-full bg-rose-100 px-2 py-0.5 text-xs font-medium text-rose-800 dark:bg-rose-900/30 dark:text-rose-400">
                      {role}
                    </span>
                  ))}
                </div>
              </div>

              {user.clienteId != null && (
                <div className="space-y-1">
                  <p className="text-sm text-muted-foreground">
                    Cliente vinculado
                  </p>
                  <button
                    onClick={() =>
                      navigate(`/dashboard/clientes/${user.clienteId}`)
                    }
                    className="cursor-pointer inline-flex items-center gap-1 text-sm font-medium text-rose-600 hover:text-rose-700 dark:text-rose-400 dark:hover:text-rose-300 transition-colors"
                    title="Ver detalle del cliente">
                    #{user.clienteId}
                    <ExternalLink className="w-3 h-3" />
                  </button>
                </div>
              )}

              {user.empleadoId != null && (
                <div className="space-y-1">
                  <p className="text-sm text-muted-foreground">
                    Empleado vinculado
                  </p>
                  <button
                    onClick={() =>
                      navigate(`/dashboard/clientes/${user.empleadoId}`)
                    }
                    className="cursor-pointer inline-flex items-center gap-1 text-sm font-medium text-rose-600 hover:text-rose-700 dark:text-rose-400 dark:hover:text-rose-300 transition-colors"
                    title="Ver detalle del cliente">
                    #{user.empleadoId}
                    <ExternalLink className="w-3 h-3" />
                  </button>
                </div>
              )}
            </div>
          </section>

          <section className="rounded-2xl border border-border/50 bg-card p-4 sm:p-6 shadow-sm">
            <h3 className="mb-4 flex items-center gap-2 text-lg font-semibold">
              <Clock className="h-5 w-5 text-rose-600" />
              Historial
            </h3>

            <div className="grid grid-cols-1 gap-4 md:grid-cols-2 min-w-0">
              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">Último acceso</p>
                <p className="flex items-center gap-2 font-medium break-words">
                  <Calendar className="h-4 w-4 text-muted-foreground shrink-0" />
                  {formatDate(user.lastLogin)}
                </p>
              </div>

              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">
                  Fecha de creación
                </p>
                <p className="flex items-center gap-2 font-medium break-words">
                  <Calendar className="h-4 w-4 text-muted-foreground shrink-0" />
                  {formatDate(user.createdAt)}
                </p>
              </div>

              <div className="space-y-1">
                <p className="text-sm text-muted-foreground">
                  Última actualización
                </p>
                <p className="flex items-center gap-2 font-medium break-words">
                  <Calendar className="h-4 w-4 text-muted-foreground shrink-0" />
                  {formatDate(user.updatedAt)}
                </p>
              </div>
            </div>
          </section>
        </div>
      </DetailPageLayout>

      <GestionRolesDialog
        open={showRolesDialog}
        onOpenChange={setShowRolesDialog}
        user={user}
      />

      <ConfirmDeleteDialog
        open={showDeleteDialog}
        onClose={() => setShowDeleteDialog(false)}
        onConfirm={handleDelete}
        isPending={deleteUser.isPending}
        title="Eliminar usuario"
        description={`¿Estás seguro de eliminar al usuario "${user.username}"? Esta acción no se puede deshacer.`}
      />
    </BackHeaderLayout>
  );
}

export default UserDetailPage;
