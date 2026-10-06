import React, { useState } from 'react';
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { login as loginApi } from '../../services/authService';
import { homeForRole, isStaffRole } from '../../utils/authRouting';
import srmLogo from '../../assets/srm-logo.png';

const StaffLogin = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const location = useLocation();
  const [error, setError] = useState(location.state?.message || '');
  const [isLoading, setIsLoading] = useState(false);
  const { isAuthenticated, role, login } = useAuth();
  const navigate = useNavigate();

  if (isAuthenticated) {
    return <Navigate to={homeForRole(role)} replace />;
  }

  const handleSubmit = async (event) => {
    event.preventDefault();
    setError('');
    setIsLoading(true);
    try {
      const response = await loginApi(email.trim(), password);
      if (!isStaffRole(response.role)) {
        throw new Error('Student accounts must use the Student Portal.');
      }
      login(response);
      navigate(homeForRole(response.role), { replace: true });
    } catch (requestError) {
      setError(requestError.message || 'Unable to sign in. Check your credentials and try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container staff-auth-container">
      <div className="auth-card glass-panel">
        <div className="auth-brand text-center mb-6 flex flex-col items-center">
          <span className="auth-logo-shell">
            <img src={srmLogo} alt="SRM Institute of Science and Technology" className="auth-logo" />
          </span>
          <h1 className="auth-title mt-2">Staff Web Portal</h1>
          <p className="auth-subtitle">Secure access for Mentors, HODs, and Administrators</p>
        </div>

        {error && <div className="error-alert" role="alert">{error}</div>}

        <form onSubmit={handleSubmit} className="auth-form">
          <div className="form-group">
            <label htmlFor="staff-email">SRM Email ID</label>
            <input
              id="staff-email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="username"
              required
            />
          </div>
          <div className="form-group">
            <div className="flex-between"><label htmlFor="staff-password">Password</label><Link to="/forgot-password" className="auth-link text-sm">Forgot Password?</Link></div>
            <input
              id="staff-password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </div>
          <button type="submit" className="btn btn-primary btn-block" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign in to Staff Portal'}
          </button>
        </form>

        <div className="auth-footer mt-6 text-center">
          <span className="auth-portal-switch">Staff accounts are provisioned by the institution. Contact the administrator if you need access.</span>
          <Link to="/login" className="auth-link font-bold">Return to Student Portal</Link>
        </div>
      </div>
    </div>
  );
};

export default StaffLogin;
