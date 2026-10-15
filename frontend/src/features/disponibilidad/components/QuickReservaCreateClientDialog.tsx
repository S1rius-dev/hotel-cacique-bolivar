import { Dialog, DialogContent, DialogHeader, DialogTitle, DialogDescription } from "@/components/ui/dialog";
import ClientForm from "@/features/clientes/components/ClientForm";
import type { ClienteResponse } from "@/api/generated/openAPIDefinition.schemas";
import { UserPlus } from "lucide-react";

interface Props {
  open: boolean;
  onOpenChange: (open: boolean) => void;
  onClientCreated: (cliente: ClienteResponse) => void;
}

function QuickReservaCreateClientDialog({ open, onOpenChange, onClientCreated }: Props) {
  return (
    <Dialog open={open} onOpenChange={onOpenChange}>
      <DialogContent className="sm:max-w-2xl max-h-[90vh] overflow-y-auto">
        <DialogHeader>
          <DialogTitle className="flex items-center gap-2">
            <UserPlus className="w-5 h-5 text-emerald-600" />
            Registrar nuevo huésped
          </DialogTitle>
          <DialogDescription>
            Ingresa los datos del cliente para asociarlo automáticamente a la reserva.
          </DialogDescription>
        </DialogHeader>
        <ClientForm
          onSuccess={(cliente) => {
            onOpenChange(false);
            onClientCreated(cliente);
          }}
        />
      </DialogContent>
    </Dialog>
  );
}

export default QuickReservaCreateClientDialog;
