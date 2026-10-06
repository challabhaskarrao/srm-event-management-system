import React, { createContext, useContext, useState, useEffect } from 'react';
import { getCurrentUser } from '../services/authService';

const AuthContext = createContext();

export const useAuth = () => {
  return useContext(AuthContext);
};

export const AuthProvider = ({ children }) => {
  const [user, setUser] = useState(null);
  const [token, setToken] = useState(null);
  const [role, setRole] = useState(null);
  const [isAuthenticated, setIsAuthenticated] = useState(false);
  const [loading, setLoading] = useState(true);
  const [lastRole, setLastRole] = useState(() => {
    try { return JSON.parse(localStorage.getItem('user') || 'null')?.role || null; }
    catch { return null; }
  });

  const clearSession = (preserveAudience = true) => {
    if (preserveAudience && role) setLastRole(role);
    if (!preserveAudience) setLastRole(null);
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    setToken(null);
    setUser(null);
    setRole(null);
    setIsAuthenticated(false);
  };

  const storeSession = (authResponse) => {
    const sessionToken = authResponse.token || localStorage.getItem('token');
    if (!sessionToken) { clearSession(false); return; }
    const authenticatedUser = {
      userId: authResponse.userId,
      email: authResponse.email,
      role: authResponse.role,
    };
    localStorage.setItem('token', sessionToken);
    localStorage.setItem('user', JSON.stringify(authenticatedUser));
    setToken(sessionToken);
    setUser(authenticatedUser);
    setRole(authResponse.role);
    setLastRole(authResponse.role);
    setIsAuthenticated(true);
  };

  useEffect(() => {
    const storedToken = localStorage.getItem('token');
    const restoreSession = async () => {
      if (!storedToken) {
        setLoading(false);
        return;
      }
      try {
        const authenticatedUser = await getCurrentUser();
        storeSession(authenticatedUser);
      } catch {
        clearSession();
      } finally {
        setLoading(false);
      }
    };

    const handleUnauthorized = () => clearSession(true);
    window.addEventListener('auth:unauthorized', handleUnauthorized);
    restoreSession();
    return () => window.removeEventListener('auth:unauthorized', handleUnauthorized);
  }, []);

  const login = (authResponse) => storeSession(authResponse);

  const logout = () => clearSession(false);

  const value = {
    user,
    token,
    role,
    isAuthenticated,
    login,
    logout,
    loading,
    lastRole,
  };

  return (
    <AuthContext.Provider value={value}>
      {!loading && children}
    </AuthContext.Provider>
  );
};
