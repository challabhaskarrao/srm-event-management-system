import React from 'react';
import { Navigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { canAccessRole, homeForRole } from '../utils/authRouting';
import { isStaffRole } from '../utils/authRouting';

const ProtectedRoute = ({ children, allowedRoles }) => {
  const { isAuthenticated, role, loading, lastRole } = useAuth();
  const location = useLocation();

  if (loading) {
    return <div className="loading-spinner">Loading...</div>;
  }

  if (!isAuthenticated) {
    const loginPath = location.pathname.startsWith('/staff') || isStaffRole(lastRole) ? '/staff/login' : '/login';
    return <Navigate to={loginPath} state={{ from: location, message: 'Your session expired. Please sign in again.' }} replace />;
  }

  if (allowedRoles && !canAccessRole(role, allowedRoles)) {
    return <Navigate to={homeForRole(role)} replace />;
  }

  return children;
};

export default ProtectedRoute;
