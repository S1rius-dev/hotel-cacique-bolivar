import { useState, useEffect, useRef, useCallback } from "react";
import { useNavigate, useLocation } from "react-router-dom";
import { useAuth } from "@/app/auth/AuthContext";
import { toast } from "sonner";
import { Dialog } from "@/components/ui/dialog";
import {
  PublicDialogContent,
  DialogTitle,
  DialogDescription,
} from "@/components/ui/public-dialog";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Pencil, UserRound } from "lucide-react";
import ProfileEditForm from "../components/ProfileEditForm";
import SecurityTab from "../components/SecurityTab";
import { GoogleIcon } from "@/features/dashboard/icons/GoogleIcon";
import ProfileHeader from "../components/ProfileHeader";

export type ProfileVariant = "public" | "dashboard";

interface ProfileModalProps {
  /** Public users only see their personal information. Dashboard users also see Security. */
  variant?: ProfileVariant;
}

const tabTriggerClass =
  "px-0 pb-3 text-sm text-[#8A7F76] data-active:text-[#241F1D] data-active:after:bg-[#7A1440]";

function InfoRow({
  label,
  children,
}: {
  label: string;
  children: React.ReactNode;
}) {
  return (
    <div className="flex items-center justify-between gap-3 border-b border-[#EFEAE3] py-3 text-sm last:border-b-0">
      <span className="text-[#8A7F76]">{label}</span>
      <span className="min-w-0 truncate text-[#241F1D]">{children}</span>
    </div>
  );
}

// TODO: conecta aquí tu servicio real (ej. authService.changePassword).
async function changePassword(_current: string, _next: string): Promise<void> {
  throw new Error("changePassword no está conectado a la API");
}

export default function ProfileModal({
  variant: variantProp,
}: ProfileModalProps) {
  const navigate = useNavigate();
  const location = useLocation();
  const { user } = useAuth();
  const [editing, setEditing] = useState(false);
  const redirecting = useRef(false);

  const open = location.pathname === "/mi-perfil";
  const variant =
    variantProp ??
    (location.state?.profileVariant === "dashboard" ? "dashboard" : "public");
  const hasSecurity = variant === "dashboard";
  const needsProfile = !!user?.roles.includes("CLIENTE") && !user?.clienteId;

  const handleClose = useCallback(() => {
    setEditing(false);
    navigate(location.state?.background?.pathname ?? "/", { replace: true });
  }, [location.state, navigate]);

  useEffect(() => {
    if (open && !user && !redirecting.current) {
      redirecting.current = true;
      toast.info("Debes iniciar sesión primero");
      handleClose();
    }
    if (!open) redirecting.current = false;
  }, [handleClose, open, user]);

  if (!user) return null;

  const name = user.username ?? user.email ?? "Mi cuenta";
  // TODO: ajusta al campo real de tu usuario (foto de Google, avatar propio, etc.).
  const avatarUrl =
    (user as { picture?: string; avatarUrl?: string }).picture ??
    (user as { avatarUrl?: string }).avatarUrl;

  const profileContent = editing ? (
    <div className="px-6 pb-6 pt-5">
      <ProfileEditForm
        onCancel={() => setEditing(false)}
        onSuccess={() => setEditing(false)}
      />
    </div>
  ) : needsProfile ? (
    <div className="flex flex-col items-center gap-4 px-6 pb-8 pt-6 text-center">
      <div className="flex size-14 items-center justify-center rounded-full bg-[#7A1440]/10">
        <UserRound className="size-7 text-[#7A1440]" aria-hidden="true" />
      </div>
      <p className="max-w-xs text-sm leading-relaxed text-[#6B6058]">
        Completa tu perfil de cliente para gestionar tus reservas.
      </p>
      <button
        type="button"
        onClick={() => setEditing(true)}
        className="cursor-pointer rounded-xl bg-[#7A1440] px-6 py-2.5 text-sm font-medium text-white transition hover:bg-[#5f0f32]">
        Completar perfil
      </button>
    </div>
  ) : (
    <div className="px-6 pb-6 pt-3">
      <InfoRow label="Usuario">{user.username ?? "—"}</InfoRow>
      <InfoRow label="Correo">{user.email}</InfoRow>
      {!hasSecurity && (
        <InfoRow label="Acceso">
          <span className="inline-flex items-center gap-2">
            <GoogleIcon className="size-4" />
            Google
          </span>
        </InfoRow>
      )}
      <button
        type="button"
        onClick={() => setEditing(true)}
        className="mt-4 inline-flex h-10 w-full cursor-pointer items-center justify-center gap-2 rounded-xl bg-[#7A1440] text-sm font-medium text-white transition hover:bg-[#5f0f32] focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-[#7A1440]">
        <Pencil className="size-4" aria-hidden="true" />
        Editar perfil
      </button>
    </div>
  );

  return (
    <Dialog open={open} onOpenChange={(v) => !v && handleClose()}>
      <PublicDialogContent
        showCloseButton={false}
        className={`w-[95vw] gap-0 overflow-hidden rounded-[18px] bg-[#FFFDFB] p-0 transition-[max-width] duration-200 ${
          editing ? "sm:max-w-145" : "sm:max-w-105"
        }`}>
        <DialogTitle className="sr-only">Mi perfil</DialogTitle>
        <DialogDescription className="sr-only">
          {hasSecurity
            ? "Datos de tu cuenta y seguridad"
            : "Tu información personal"}
        </DialogDescription>

        <div className="max-h-[85vh] overflow-y-auto">
          <ProfileHeader
            name={name}
            email={user.email}
            avatarUrl={avatarUrl}
            onClose={handleClose}
          />

          {hasSecurity ? (
            <Tabs
              defaultValue="perfil"
              onValueChange={(tab) => tab === "seguridad" && setEditing(false)}
              className="gap-0">
              <TabsList
                variant="line"
                className="h-10 w-full justify-start gap-6 border-b border-[#EFEAE3] px-6">
                <TabsTrigger value="perfil" className={tabTriggerClass}>
                  Perfil
                </TabsTrigger>
                <TabsTrigger value="seguridad" className={tabTriggerClass}>
                  Seguridad
                </TabsTrigger>
              </TabsList>
              <TabsContent value="perfil" className="mt-0">
                {profileContent}
              </TabsContent>
              <TabsContent value="seguridad" className="mt-0">
                <SecurityTab onChangePassword={changePassword} />
              </TabsContent>
            </Tabs>
          ) : (
            <div className="border-t border-[#EFEAE3]">{profileContent}</div>
          )}
        </div>
      </PublicDialogContent>
    </Dialog>
  );
}
