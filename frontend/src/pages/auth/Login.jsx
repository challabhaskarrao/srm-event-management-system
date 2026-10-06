import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { login as loginApi } from '../../services/authService';
import srmLogo from '../../assets/srm-logo.png';
import { homeForRole } from '../../utils/authRouting';

const Login = () => {
  const location = useLocation();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState(location.state?.message || '');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const { login } = useAuth();

  const from = location.state?.from?.pathname;

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await loginApi(email, password);

      if (response.role !== 'STUDENT') {
        throw new Error('Staff accounts must sign in through the Staff Web Portal.');
      }

      login(response);
      navigate(from?.startsWith('/student/') ? from : homeForRole(response.role), { replace: true });
    } catch (err) {
      setError(err.message || 'Failed to login. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card glass-panel">
        
        <div className="auth-brand text-center mb-6 flex flex-col items-center">
          <span className="auth-logo-shell">
            <img src={srmLogo} alt="SRM Institute of Science and Technology" className="auth-logo" />
          </span>
          <h2 className="auth-title mt-2">SRM Event Management System</h2>
          <p className="auth-subtitle">Sign in to the Student Portal</p>
        </div>
        
        {error && <div className="error-alert">{error}</div>}

        <form onSubmit={handleLogin} className="auth-form">
          <div className="form-group">
            <label htmlFor="email">SRM Email ID</label>
            <input
              type="email"
              id="email"
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              placeholder="e.g. ra23110@srmist.edu.in"
              autoComplete="username"
              required
            />
          </div>
          
          <div className="form-group">
            <div className="flex-between">
              <label htmlFor="password">Password</label>
              <Link to="/forgot-password" className="auth-link text-sm">Forgot Password?</Link>
            </div>
            <input
              type="password"
              id="password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              placeholder="Enter your password"
              autoComplete="current-password"
              required
            />
          </div>
          
          <button type="submit" className="btn btn-primary btn-block" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer mt-4 text-center">
          <span className="text-muted text-sm">Student accounts are provisioned from official institutional records.</span>
          <span className="auth-portal-switch">Don't have an account? <Link to="/signup" className="auth-link font-bold">Create Student Account</Link></span>
          <span className="auth-portal-switch">
            Staff member? <Link to="/staff/login" className="auth-link font-bold">Open Staff Portal</Link>
          </span>
        </div>
      </div>
    </div>
  );
};

export default Login;
