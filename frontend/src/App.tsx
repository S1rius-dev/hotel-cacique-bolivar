import AppRouter from "./app/AppRouter";
import { ThemeProvider } from "next-themes";
import { Toaster } from "@/components/ui/sonner";
import { useSSE } from "@/lib/hooks/useSSE";

const App: React.FC = () => {
  useSSE();

  return (
    <ThemeProvider attribute="class" defaultTheme="system" enableSystem>
      <AppRouter />
      <Toaster position="bottom-right" richColors closeButton />
    </ThemeProvider>
  );
};

export default App;
