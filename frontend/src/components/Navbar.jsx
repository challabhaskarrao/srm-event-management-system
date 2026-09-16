import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useLocation } from 'react-router-dom';
import { useAuth } from '../context/AuthContext';
import { FileText, PlusCircle, LogOut, LayoutDashboard, User, Menu, X } from 'lucide-react';
import srmLogo from '../assets/srm-logo.png';

const Navbar = () => {
  const { user, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);

  useEffect(() => {
    setIsMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  if (!isAuthenticated) return null;

  const isActive = (path) => location.pathname === path ? 'active' : '';

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to="/dashboard" className="nav-left">
          <span className="navbar-logo-shell">
            <img src={srmLogo} alt="SRM Institute of Science and Technology" className="navbar-logo" />
          </span>
          <span className="nav-title">SRM Event Management System</span>
        </Link>

        <div className={`nav-links ${isMenuOpen ? 'is-open' : ''}`} id="primary-navigation">
          <Link to="/dashboard" className={`nav-link ${isActive('/dashboard')}`}>
            <LayoutDashboard size={18} />
            <span>Dashboard</span>
          </Link>
          <Link to="/apply" className={`nav-link ${isActive('/apply')}`}>
            <PlusCircle size={18} />
            <span>New Request</span>
          </Link>
          <Link to="/my-applications" className={`nav-link ${isActive('/my-applications')}`}>
            <FileText size={18} />
            <span>My Applications</span>
          </Link>
        </div>

        <div className="nav-right">
          <div className="navbar-user">
            <div className="user-badge" title={user?.name || user?.fullName || 'Student'}>
              <User size={16} />
              <span className="user-name">{user?.name || user?.fullName || 'Student'}</span>
              {user?.registrationNumber && <span className="role-pill">{user.registrationNumber}</span>}
            </div>
            <button onClick={handleLogout} className="logout-btn" title="Log out" aria-label="Log out">
              <LogOut size={18} />
            </button>
          </div>
          <button
            type="button"
            className="nav-toggle"
            aria-label={isMenuOpen ? 'Close navigation menu' : 'Open navigation menu'}
            aria-controls="primary-navigation"
            aria-expanded={isMenuOpen}
            onClick={() => setIsMenuOpen((open) => !open)}
          >
            {isMenuOpen ? <X size={22} /> : <Menu size={22} />}
          </button>
        </div>
      </div>
    </nav>
  );
};

export default Navbar;
