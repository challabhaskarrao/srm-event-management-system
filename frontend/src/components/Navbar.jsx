import React, { useEffect, useRef, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import { FileText, PlusCircle, LogOut, LayoutDashboard, User, Menu, X, ShieldCheck, Bell, Check } from 'lucide-react';
import { useAuth } from '../context/AuthContext';
import { homeForRole, isStaffRole } from '../utils/authRouting';
import { resolveNotificationPath } from '../utils/notificationRoutes';
import { getNotifications, getUnreadNotificationCount, markAllNotificationsRead, markNotificationRead } from '../services/notificationService';
import srmLogo from '../assets/srm-logo.png';

const Navbar = () => {
  const { user, role, logout, isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [isMenuOpen, setIsMenuOpen] = useState(false);
  const [isNotificationOpen, setIsNotificationOpen] = useState(false);
  const [notifications, setNotifications] = useState([]);
  const [unreadCount, setUnreadCount] = useState(0);
  const [notificationLoading, setNotificationLoading] = useState(false);
  const [notificationError, setNotificationError] = useState('');
  const [notificationRefreshToken, setNotificationRefreshToken] = useState(0);
  const notificationShellRef = useRef(null);

  useEffect(() => {
    setIsMenuOpen(false);
    setIsNotificationOpen(false);
  }, [location.pathname]);
  useEffect(() => {
    if (!isAuthenticated) return undefined;
    let active = true;
    const loadNotifications = async () => {
      if (active) { setNotificationLoading(true); setNotificationError(''); }
      try {
        const [items, count] = await Promise.all([getNotifications(), getUnreadNotificationCount()]);
        if (active) {
          setNotifications(Array.isArray(items) ? items : []);
          setUnreadCount(Number(count?.count || 0));
        }
      } catch {
        if (active) setNotificationError('Unable to load notifications.');
      } finally {
        if (active) setNotificationLoading(false);
      }
    };
    loadNotifications(); const timer = window.setInterval(loadNotifications, 60000);
    return () => { active = false; window.clearInterval(timer); };
  }, [isAuthenticated, location.pathname, notificationRefreshToken]);

  useEffect(() => {
    if (!isNotificationOpen) return undefined;
    const closeOnOutsideClick = (event) => {
      if (!notificationShellRef.current?.contains(event.target)) setIsNotificationOpen(false);
    };
    const closeOnEscape = (event) => {
      if (event.key === 'Escape') setIsNotificationOpen(false);
    };
    document.addEventListener('mousedown', closeOnOutsideClick);
    document.addEventListener('keydown', closeOnEscape);
    return () => {
      document.removeEventListener('mousedown', closeOnOutsideClick);
      document.removeEventListener('keydown', closeOnEscape);
    };
  }, [isNotificationOpen]);

  if (!isAuthenticated) return null;

  const staffUser = isStaffRole(role);
  const homePath = homeForRole(role);
  const links = staffUser
    ? [{ to: homePath, label: `${role} Workspace`, icon: LayoutDashboard }, { to: '/account/security', label: 'Account Security', icon: ShieldCheck }]
    : [
        { to: '/student/dashboard', label: 'Dashboard', icon: LayoutDashboard },
        { to: '/student/participations/new', label: 'Create Participation', icon: PlusCircle },
        { to: '/student/applications', label: 'My Applications', icon: FileText },
        { to: '/account/security', label: 'Account Security', icon: ShieldCheck },
      ];

  const handleLogout = () => {
    logout();
    navigate(staffUser ? '/staff/login' : '/login');
  };

  const openNotification = async (notification) => {
    const path = resolveNotificationPath(notification);
    if (!path) return;
    setIsNotificationOpen(false);
    navigate(path);
    if (!notification.read) {
      setNotifications((items) => items.map((item) => item.notificationId === notification.notificationId ? { ...item, read: true } : item));
      setUnreadCount((value) => Math.max(0, value - 1));
      void markNotificationRead(notification.notificationId).catch(() => { /* navigation is independent of persistence */ });
    }
  };
  const readAll = async () => {
    try {
      await markAllNotificationsRead();
      setNotifications((items) => items.map((item) => ({ ...item, read: true })));
      setUnreadCount(0);
    } catch { /* keep current state if persistence is unavailable */ }
  };

  return (
    <nav className="navbar">
      <div className="navbar-container">
        <Link to={homePath} className="nav-left">
          <span className="navbar-logo-shell">
            <img src={srmLogo} alt="SRM Institute of Science and Technology" className="navbar-logo" />
          </span>
          <span className="nav-title">SRM Event Management System</span>
        </Link>

        <div className={`nav-links ${isMenuOpen ? 'is-open' : ''}`} id="primary-navigation">
          {links.map(({ to, label, icon: Icon }) => (
            <Link key={to} to={to} className={`nav-link ${location.pathname === to ? 'active' : ''}`}>
              <Icon size={18} />
              <span>{label}</span>
            </Link>
          ))}
        </div>

        <div className="nav-right">
          <div className="notification-shell" ref={notificationShellRef}>
            <button type="button" className="notification-bell" aria-label="Notifications" aria-expanded={isNotificationOpen} onClick={() => setIsNotificationOpen((open) => !open)}><Bell size={19} />{unreadCount > 0 && <span className="notification-badge">{unreadCount > 99 ? '99+' : unreadCount}</span>}</button>
            {isNotificationOpen && <div className="notification-panel" role="dialog" aria-label="Notifications">
              <div className="notification-panel-heading"><strong>Notifications</strong><button type="button" className="notification-mark-all" onClick={readAll} disabled={unreadCount === 0}><Check size={14} /> Mark all read</button></div>
              {notificationLoading ? <p className="notification-state">Loading notifications…</p> : notificationError ? <div className="notification-state notification-error"><p>{notificationError}</p><button type="button" onClick={() => setNotificationRefreshToken((value) => value + 1)}>Retry</button></div> : notifications.length === 0 ? <p className="notification-empty">No notifications yet.</p> : <div className="notification-list">{notifications.map((notification) => <button type="button" className={`notification-item ${notification.read ? '' : 'is-unread'}`} key={notification.notificationId} onClick={() => openNotification(notification)}><span className="notification-item-title">{!notification.read && <i />} {notification.title}</span><span>{notification.message}</span><small>{notification.createdAt ? new Date(notification.createdAt).toLocaleString() : 'Recently'}</small>{resolveNotificationPath(notification) && <em>View</em>}</button>)}</div>}
            </div>}
          </div>
          <div className="navbar-user">
            <div className="user-badge" title={user?.email || role}>
              <User size={16} />
              <span className="user-name">{user?.email || 'Authenticated user'}</span>
              <span className="role-pill">{role}</span>
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
