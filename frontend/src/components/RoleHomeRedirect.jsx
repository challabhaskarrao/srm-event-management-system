import React from 'react';
import { Navigate } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { homeForRole } from '../utils/authRouting';

const RoleHomeRedirect = () => {
  const { isAuthenticated, role, loading } = useAuth();

  if (loading) {
    return <div className="loading-spinner">Restoring session...</div>;
  }

  return <Navigate to={isAuthenticated ? homeForRole(role) : '/login'} replace />;
};

export default RoleHomeRedirect;
