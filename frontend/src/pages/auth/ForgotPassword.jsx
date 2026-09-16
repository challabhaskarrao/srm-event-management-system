import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import { Mail, CheckCircle } from 'lucide-react';

const ForgotPassword = () => {
  const [email, setEmail] = useState('');
  const [isSubmitted, setIsSubmitted] = useState(false);
  const [isLoading, setIsLoading] = useState(false);

  const handleSubmit = (e) => {
    e.preventDefault();
    setIsLoading(true);
    
    // Simulate API call for password reset
    setTimeout(() => {
      setIsLoading(false);
      setIsSubmitted(true);
    }, 1500);
  };

  return (
    <div className="auth-container">
      <div className="auth-card glass-panel">
        <h2 className="auth-title">Reset Password</h2>
        
        {isSubmitted ? (
          <div className="reset-success text-center py-4">
            <div className="success-icon-wrapper mb-4">
              <CheckCircle size={48} className="text-green-500 mx-auto" style={{color: '#10b981'}} />
            </div>
            <h3 className="text-xl font-bold text-navy mb-2">Check your email</h3>
            <p className="text-muted mb-6">
              We've sent a password recovery link to <strong>{email}</strong>. Please check your inbox.
            </p>
            <Link to="/login" className="btn btn-primary btn-block">Return to Login</Link>
          </div>
        ) : (
          <>
            <p className="auth-subtitle">Enter your SRM email address and we'll send you a link to reset your password.</p>
            
            <form onSubmit={handleSubmit} className="auth-form mt-4">
              <div className="form-group">
                <label>SRM Email ID</label>
                <div className="input-with-icon">
                  <Mail size={18} className="input-icon" />
                  <input 
                    type="email" 
                    value={email} 
                    onChange={(e) => setEmail(e.target.value)} 
                    required 
                    placeholder="email@srmist.edu.in" 
                    style={{paddingLeft: '2.5rem'}}
                  />
                </div>
              </div>
              
              <button type="submit" className="btn btn-primary btn-block" disabled={isLoading || !email}>
                {isLoading ? 'Sending Link...' : 'Send Recovery Link'}
              </button>
            </form>

            <div className="auth-footer mt-6 text-center">
              <Link to="/login" className="auth-link text-sm font-bold">Back to Sign In</Link>
            </div>
          </>
        )}
      </div>
    </div>
  );
};

export default ForgotPassword;
