import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
} from "react";
import { getAuthController } from "@/api/generated/auth-controller/auth-controller";
import { toSessionUser } from "./session-user";

const authApi = getAuthController();

export interface SessionUser {
  id?: number;
  email: string;
  username?: string;
  roles: string[];
  clienteId?: number;
  profilePicture?: string;
}

interface AuthContextType {
  user: SessionUser | null;
  loading: boolean;
  login: (user: SessionUser) => void;
  logout: () => Promise<void>;
  clearUser: () => void;
  refreshUser: () => Promise<void>;
}

const AuthContext = createContext<AuthContextType | undefined>(undefined);

export const AuthProvider = ({ children }: { children: React.ReactNode }) => {
  const [user, setUser] = useState<SessionUser | null>(null);
  const [loading, setLoading] = useState(true);

  const fetchSession = async (isActive: () => boolean) => {
    try {
      const res = await authApi.me();
      return { user: toSessionUser(res), isActive };
    } catch {
      return { user: null, isActive };
    }
  };

  const refreshUser = useCallback(async () => {
    const { user, isActive } = await fetchSession(() => true);
    if (isActive()) {
      setUser(user);
      setLoading(false);
    }
  }, []);

  useEffect(() => {
    let active = true;
    const isActive = () => active;
    fetchSession(isActive).then(({ user, isActive: check }) => {
      if (!check()) return;
      setUser(user);
      setLoading(false);
    });
    return () => {
      active = false;
    };
  }, []);

  const login = useCallback((user: SessionUser) => {
    setUser(user);
    setLoading(false);
  }, []);

  const clearUser = useCallback(() => {
    setUser(null);
    setLoading(false);
  }, []);

  const logout = useCallback(async () => {
    try {
      await authApi.logout();
    } catch {
      // Ignorar errores al cerrar sesión
    }
    setUser(null);
  }, []);

  const value = useMemo(
    () => ({ user, loading, login, logout, clearUser, refreshUser }),
    [user, loading, login, logout, clearUser, refreshUser],
  );

  return (
      <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
  );
};

export const useAuth = () => {
  const context = useContext(AuthContext);
  if (!context) throw new Error("useAuth debe usarse dentro de AuthProvider");
  return context;
};
