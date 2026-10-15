/**
 * Google Identity Services se carga por el script de GIS y no viene con tipos.
 * Solo se declara la superficie que usa el login del cliente.
 */
interface GoogleCredentialResponse {
  credential?: string;
}

interface GoogleAccountsId {
  initialize: (config: {
    client_id: string;
    callback: (response: GoogleCredentialResponse) => void | Promise<void>;
  }) => void;
  renderButton: (parent: HTMLElement, options: {
    type: "standard" | "icon";
    shape: "pill" | "rectangular" | "circle" | "square";
    size: "large" | "medium" | "small";
  }) => void;
}

interface GoogleAccounts {
  id: GoogleAccountsId;
}

interface Window {
  google?: {
    accounts?: GoogleAccounts;
  };
}
