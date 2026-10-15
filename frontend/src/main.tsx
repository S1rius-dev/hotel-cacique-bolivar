import { StrictMode } from "react";
import ReactDOM from "react-dom/client";
import { BrowserRouter } from "react-router-dom";
import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { HelmetProvider } from "react-helmet-async";
import AppWithAuth from "@/app/AppWithAuth";
import { AuthProvider } from "@/app/auth/AuthContext";
import "./styles.css";
import { GoogleOAuthProvider } from "@react-oauth/google";

const queryClient = new QueryClient({
  defaultOptions: {
    queries: {
      staleTime: 30 * 1000,
      gcTime: 5 * 60 * 1000,
      refetchOnWindowFocus: true,
      refetchOnReconnect: true,
      retry: 1,
    },
  },
});

ReactDOM.createRoot(document.getElementById("root")!).render(
  <StrictMode>
    <HelmetProvider>
      <GoogleOAuthProvider clientId={import.meta.env.VITE_GOOGLE_CLIENT_ID}>
        <BrowserRouter>
          <QueryClientProvider client={queryClient}>
            <AuthProvider>
              <AppWithAuth />
            </AuthProvider>
          </QueryClientProvider>
        </BrowserRouter>
      </GoogleOAuthProvider>
    </HelmetProvider>
  </StrictMode>,
);
