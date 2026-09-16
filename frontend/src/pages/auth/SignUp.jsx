import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { register } from '../../services/authService';

const SignUp = () => {
  const [formData, setFormData] = useState({
    fullName: '',
    registerNumber: '',
    email: '',
    password: '',
    confirmPassword: '',
    department: 'CSE',
    year: '1',
    section: ''
  });
  const [error, setError] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const navigate = useNavigate();

  const handleChange = (e) => {
    setFormData(prev => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSignUp = async (e) => {
    e.preventDefault();
    setError('');

    if (formData.password !== formData.confirmPassword) {
      setError('Passwords do not match.');
      return;
    }

    if (!formData.email.endsWith('@srmist.edu.in')) {
      setError('Please use a valid @srmist.edu.in email address.');
      return;
    }

    setIsLoading(true);

    try {
      // Omit confirmPassword from payload
      const { confirmPassword, ...payload } = formData;
      payload.role = 'STUDENT'; // Explicitly state role for registration
      
      await register(payload);
      
      // Navigate to login on success
      navigate('/login', { state: { message: 'Registration successful! Please sign in.' } });
    } catch (err) {
      setError(err.message || 'Failed to register. Please try again.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="auth-container">
      <div className="auth-card auth-card-large glass-panel">
        <h2 className="auth-title">Create Account</h2>
        <p className="auth-subtitle">Join the SRM Event Management System</p>
        
        {error && <div className="error-alert">{error}</div>}

        <form onSubmit={handleSignUp} className="auth-form">
          <div className="form-row">
            <div className="form-group">
              <label>Full Name</label>
              <input type="text" name="fullName" value={formData.fullName} onChange={handleChange} required placeholder="John Doe" />
            </div>
            <div className="form-group">
              <label>Register Number</label>
              <input type="text" name="registerNumber" value={formData.registerNumber} onChange={handleChange} required placeholder="RA2311003010001" />
            </div>
          </div>

          <div className="form-group">
            <label>SRM Email ID</label>
            <input type="email" name="email" value={formData.email} onChange={handleChange} required placeholder="email@srmist.edu.in" />
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Password</label>
              <input type="password" name="password" value={formData.password} onChange={handleChange} required />
            </div>
            <div className="form-group">
              <label>Confirm Password</label>
              <input type="password" name="confirmPassword" value={formData.confirmPassword} onChange={handleChange} required />
            </div>
          </div>

          <div className="form-row">
            <div className="form-group">
              <label>Department</label>
              <select name="department" value={formData.department} onChange={handleChange}>
                <option value="CSE">Computer Science (CSE)</option>
                <option value="IT">Information Technology (IT)</option>
                <option value="ECE">Electronics (ECE)</option>
                <option value="MECH">Mechanical (MECH)</option>
                <option value="CIVIL">Civil</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
            <div className="form-group">
              <label>Year</label>
              <select name="year" value={formData.year} onChange={handleChange}>
                <option value="1">1st Year</option>
                <option value="2">2nd Year</option>
                <option value="3">3rd Year</option>
                <option value="4">4th Year</option>
              </select>
            </div>
          </div>

          <div className="form-group">
            <label>Section</label>
            <input type="text" name="section" value={formData.section} onChange={handleChange} required placeholder="e.g. A, B, C" maxLength="2" />
          </div>
          
          <button type="submit" className="btn btn-primary btn-block mt-4" disabled={isLoading}>
            {isLoading ? 'Creating Account...' : 'Sign Up'}
          </button>
        </form>

        <div className="auth-footer mt-4 text-center">
          <span className="text-muted text-sm">Already have an account? </span>
          <Link to="/login" className="auth-link text-sm font-bold">Sign In</Link>
        </div>
      </div>
    </div>
  );
};

export default SignUp;
