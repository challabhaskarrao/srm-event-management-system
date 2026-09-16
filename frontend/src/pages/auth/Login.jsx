import React, { useState } from 'react';
import { useNavigate, useLocation, Link } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { login as loginApi } from '../../services/authService';
import srmLogo from '../../assets/srm-logo.png';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();
  const location = useLocation();
  const { login } = useAuth();

  const from = location.state?.from?.pathname || '/dashboard';

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setIsLoading(true);

    try {
      const response = await loginApi(email, password);
      
      const userObj = response.user || { email };
      let assignedRole = null;
      if (response.token) {
        try {
          const payload = JSON.parse(atob(response.token.split('.')[1]));
          assignedRole = payload.role || userObj.role || null;
        } catch (e) {
          assignedRole = userObj.role || null;
        }
      }
      
      if (assignedRole && assignedRole !== 'STUDENT') {
        throw new Error('Access Denied: Faculty and Administrative staff must use the SRM Desktop Management Suite.');
      }

      login(response.token, userObj);
      navigate(from, { replace: true });
    } catch (err) {
      setError(err.message || 'Failed to login. Please check your credentials.');
    } finally {
      setIsLoading(false);
    }
  };

  const fillTestCredentials = () => {
    setEmail('student@srmist.edu.in');
    setPassword('Admin@123');
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
              required
            />
          </div>
          
          <button type="submit" className="btn btn-primary btn-block" disabled={isLoading}>
            {isLoading ? 'Signing in...' : 'Sign In'}
          </button>
        </form>

        <div className="auth-footer mt-4 text-center">
          <span className="text-muted text-sm">Don't have an account? </span>
          <Link to="/signup" className="auth-link text-sm font-bold">Sign Up</Link>
        </div>

        {import.meta.env.DEV && (
          <div className="test-credentials">
            <p className="test-creds-label">Dev Quick Access</p>
            <div className="test-creds-buttons">
              <button onClick={fillTestCredentials} type="button" className="btn btn-secondary btn-sm">
                Test Student
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};

export default Login;
