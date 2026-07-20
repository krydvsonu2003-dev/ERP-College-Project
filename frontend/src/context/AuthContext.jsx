import { createContext, useContext, useState, useCallback } from 'react';
import axiosClient from '../api/axiosClient';

const AuthContext = createContext(null);

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => {
    const stored = localStorage.getItem('userInfo');
    return stored ? JSON.parse(stored) : null;
  });

  const login = useCallback(async (username, password) => {
    const { data } = await axiosClient.post('/auth/login', { username, password });
    const payload = data.data;
    localStorage.setItem('accessToken', payload.accessToken);
    localStorage.setItem('refreshToken', payload.refreshToken);
    const info = {
      userId: payload.userId,
      username: payload.username,
      fullName: payload.fullName,
      roles: payload.roles,
      privileges: payload.privileges,
      mustChangePassword: payload.mustChangePassword,
    };
    localStorage.setItem('userInfo', JSON.stringify(info));
    setUser(info);
    return info;
  }, []);

  const logout = useCallback(() => {
    localStorage.removeItem('accessToken');
    localStorage.removeItem('refreshToken');
    localStorage.removeItem('userInfo');
    setUser(null);
  }, []);

  const hasRole = useCallback((role) => !!user?.roles?.includes(role), [user]);
  const hasPrivilege = useCallback((priv) => !!user?.privileges?.includes(priv), [user]);

  const primaryRole = user?.roles?.find((r) =>
    ['SUPER_ADMIN', 'PRINCIPAL', 'HOD', 'FACULTY', 'STUDENT', 'ACCOUNTANT', 'ADMISSION_OFFICE_STAFF'].includes(r)
  ) || user?.roles?.[0];

  return (
    <AuthContext.Provider value={{ user, login, logout, hasRole, hasPrivilege, primaryRole }}>
      {children}
    </AuthContext.Provider>
  );
}

export function useAuth() {
  const ctx = useContext(AuthContext);
  if (!ctx) throw new Error('useAuth must be used within AuthProvider');
  return ctx;
}
