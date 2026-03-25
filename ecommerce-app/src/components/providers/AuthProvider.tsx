"use client";

import { login as loginRequest, signup as signupRequest } from "@/lib/api";
import type { AuthPayload, AuthUser } from "@/lib/types";
import {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from "react";

interface AuthContextValue {
  user: AuthUser | null;
  token: string | null;
  isAuthenticated: boolean;
  isLoading: boolean;
  login: (email: string, password: string) => Promise<void>;
  signup: (name: string, email: string, password: string) => Promise<void>;
  logout: () => void;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

const TOKEN_KEY = "vectortech.token";
const USER_KEY = "vectortech.user";

function mapAuthPayload(payload: AuthPayload): AuthUser {
  return {
    userId: payload.userId,
    name: payload.name,
    email: payload.email,
    role: payload.role,
  };
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [token, setToken] = useState<string | null>(null);
  const [user, setUser] = useState<AuthUser | null>(null);
  const [isLoading, setIsLoading] = useState(true);

  useEffect(() => {
    try {
      const storedToken = window.localStorage.getItem(TOKEN_KEY);
      const storedUser = window.localStorage.getItem(USER_KEY);

      if (storedToken) {
        setToken(storedToken);
      }

      if (storedUser) {
        setUser(JSON.parse(storedUser) as AuthUser);
      }
    } finally {
      setIsLoading(false);
    }
  }, []);

  const persistSession = useCallback((payload: AuthPayload) => {
    const nextUser = mapAuthPayload(payload);
    window.localStorage.setItem(TOKEN_KEY, payload.token);
    window.localStorage.setItem(USER_KEY, JSON.stringify(nextUser));
    setToken(payload.token);
    setUser(nextUser);
  }, []);

  const login = useCallback(async (email: string, password: string) => {
    const payload = await loginRequest({ email, password });
    persistSession(payload);
  }, [persistSession]);

  const signup = useCallback(async (name: string, email: string, password: string) => {
    const payload = await signupRequest({ name, email, password });
    persistSession(payload);
  }, [persistSession]);

  const logout = useCallback(() => {
    window.localStorage.removeItem(TOKEN_KEY);
    window.localStorage.removeItem(USER_KEY);
    setToken(null);
    setUser(null);
  }, []);

  const value = useMemo<AuthContextValue>(() => ({
    user,
    token,
    isAuthenticated: Boolean(token && user),
    isLoading,
    login,
    signup,
    logout,
  }), [user, token, isLoading, login, signup, logout]);

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

export function useAuth() {
  const context = useContext(AuthContext);

  if (!context) {
    throw new Error("useAuth must be used within AuthProvider");
  }

  return context;
}
