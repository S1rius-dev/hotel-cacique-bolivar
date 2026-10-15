import { useState } from "react";
import { useMutation, useQueryClient } from "@tanstack/react-query";
import { getUserController } from "@/api/generated/user-controller/user-controller";
import type { UserResponse } from "@/api/generated/openAPIDefinition.schemas";
import {
  Dialog,
  DialogContent,
  DialogHeader,
  DialogTitle,
  DialogDescription,
  DialogFooter,
  DialogClose,
} from "@/components/ui/dialog";
import { Button } from "@/components/ui/button";
import { Loader2, ShieldCheck } from "lucide-react";
import { useRoles } from "../hooks/useRoles";
import { toApiRoles } from "../utils/mapUser";
import { toast } from "sonner";
import { cn } from "@/lib/utils";
import { extractErrorMessage } from "@/lib/extract-error";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  user: UserResponse;
}

function GestionRolesDialog({ open, onOpenChange, user }: Props) {
  const { data: roles = [], isLoading: rolesLoading } = useRoles();
  const queryClient = useQueryClient();
  const currentRoles = (user.roles ?? []) as string[];
  const [selectedRoles, setSelectedRoles] = useState<string[]>(currentRoles);

  const updateMutation = useMutation({
    mutationFn: async (newRoles: string[]) => {
      const api = getUserController();
      return await api.actualizarUsuario(user.id!, {
        email: user.email ?? "",
        username: user.username ?? "",
        roles: toApiRoles(newRoles),
      });
    },
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ["users"] });
      toast.success("Roles actualizados correctamente");
      onOpenChange(false);
    },
    onError: (error) => {
      toast.error(extractErrorMessage(error, "Error al actualizar los roles"));
    },
  });

  const toggleRole = (roleName: string) => {
    setSelectedRoles((prev) =>
      prev.includes(roleName)
        ? prev.filter((r) => r !== roleName)
        : [...prev, roleName],
    );
  };

  const handleSave = () => {
    updateMutation.mutate(selectedRoles);
  };

  const handleOpenChange = (open: boolean) => {
    if (!open) {
      setSelectedRoles(currentRoles);
    }
    onOpenChange(open);
  };

  return (
    <Dialog open={open} onOpenChange={handleOpenChange}>
      <DialogContent className="sm:max-w-md">
        <DialogHeader>
          <div className="flex items-center gap-2">
            <ShieldCheck className="size-5 text-rose-500" />
            <DialogTitle>Gestionar roles</DialogTitle>
          </div>
          <DialogDescription>
            Permisos de <span className="font-medium text-foreground">{user.username}</span> en el sistema.
          </DialogDescription>
        </DialogHeader>

        <div className="space-y-1">
          {rolesLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="size-5 animate-spin text-muted-foreground" />
            </div>
          ) : (
            roles.map((rol) => {
              const roleName = rol.nombre!;
              const isSelected = selectedRoles.includes(roleName);
              return (
                <button
                  key={roleName}
                  type="button"
                  onClick={() => toggleRole(roleName)}
                  className="flex items-center gap-3 w-full px-3 py-3 text-sm text-left rounded-lg hover:bg-muted/50 transition-colors border border-transparent hover:border-border"
                >
                  <div
                    className={cn(
                      "size-4 rounded border shrink-0 flex items-center justify-center transition-colors",
                      isSelected
                        ? "bg-rose-600 border-rose-600"
                        : "border-muted-foreground/30",
                    )}
                  >
                    {isSelected && (
                      <svg
                        className="size-2.5 text-white"
                        fill="none"
                        viewBox="0 0 24 24"
                        stroke="currentColor"
                        strokeWidth={3}
                      >
                        <path
                          strokeLinecap="round"
                          strokeLinejoin="round"
                          d="M5 13l4 4L19 7"
                        />
                      </svg>
                    )}
                  </div>
                  <span className="font-medium">{roleName}</span>
                </button>
              );
            })
          )}
        </div>

        <DialogFooter>
          <DialogClose asChild>
            <Button type="button" variant="ghost" disabled={updateMutation.isPending}>
              Cancelar
            </Button>
          </DialogClose>
          <Button
            type="button"
            onClick={handleSave}
            disabled={updateMutation.isPending || rolesLoading}
          >
            {updateMutation.isPending && (
              <Loader2 className="mr-2 size-4 animate-spin" />
            )}
            {updateMutation.isPending ? "Guardando..." : "Guardar cambios"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default GestionRolesDialog;
