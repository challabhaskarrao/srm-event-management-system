import React from 'react';
import { BrowserRouter, Navigate, Route, Routes } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';
import RoleHomeRedirect from './components/RoleHomeRedirect';

import Login from './pages/auth/Login';
import SignUp from './pages/auth/SignUp';
import VerifyAccount from './pages/auth/VerifyAccount';
import ForgotPassword from './pages/auth/ForgotPassword';
import ResetPassword from './pages/auth/ResetPassword';
import AccountSecurity from './pages/auth/AccountSecurity';
import Dashboard from './pages/student/Dashboard';
import Events from './pages/student/Events';
import ApplyEvent from './pages/student/ApplyEvent';
import MyApplications from './pages/student/MyApplications';
import CreateParticipation from './pages/student/CreateParticipation';
import CreateAchievement from './pages/student/CreateAchievement';
import MyAchievements from './pages/student/MyAchievements';
import AchievementDetails from './pages/student/AchievementDetails';
import ApplicationDetails from './pages/student/ApplicationDetails';
import MyPortfolio from './pages/student/MyPortfolio';
import StaffLogin from './pages/staff/StaffLogin';
import StaffWorkspace from './pages/staff/StaffWorkspace';
import HodAnalytics from './pages/staff/HodAnalytics';

const studentRoute = (component) => (
  <ProtectedRoute allowedRoles={['STUDENT']}>{component}</ProtectedRoute>
);

const staffRoute = (role) => (
  <ProtectedRoute allowedRoles={[role]}><StaffWorkspace /></ProtectedRoute>
);

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-layout">
          <Navbar />
          <main className="main-content">
            <Routes>
              <Route path="/login" element={<Login />} />
              <Route path="/signup" element={<SignUp />} />
              <Route path="/verify-account" element={<VerifyAccount />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />
              <Route path="/reset-password" element={<ResetPassword />} />
              <Route path="/staff/login" element={<StaffLogin />} />
              <Route path="/account/security" element={<ProtectedRoute allowedRoles={['STUDENT', 'MENTOR', 'HOD', 'ADMIN']}><AccountSecurity /></ProtectedRoute>} />

              <Route path="/student/dashboard" element={studentRoute(<Dashboard />)} />
              <Route path="/student/participations/new" element={studentRoute(<CreateParticipation />)} />
              <Route path="/student/achievements/new" element={studentRoute(<CreateAchievement />)} />
              <Route path="/student/achievements" element={studentRoute(<MyAchievements />)} />
              <Route path="/student/achievements/:id" element={studentRoute(<AchievementDetails />)} />
              <Route path="/student/events" element={studentRoute(<Events />)} />
              <Route path="/student/events/:eventId/apply" element={studentRoute(<ApplyEvent />)} />
              <Route path="/student/applications" element={studentRoute(<MyApplications />)} />
              <Route path="/student/applications/:code" element={studentRoute(<ApplicationDetails />)} />
              <Route path="/student/portfolio" element={studentRoute(<MyPortfolio />)} />

              <Route path="/staff" element={<RoleHomeRedirect />} />
              <Route path="/staff/mentor" element={staffRoute('MENTOR')} />
              <Route path="/staff/hod" element={staffRoute('HOD')} />
              <Route path="/staff/hod/analytics" element={<ProtectedRoute allowedRoles={['HOD']}><HodAnalytics /></ProtectedRoute>} />
              <Route path="/staff/admin" element={staffRoute('ADMIN')} />

              {/* Backward-compatible Student routes retained for existing bookmarks. */}
              <Route path="/dashboard" element={<Navigate to="/student/dashboard" replace />} />
              <Route path="/student/apply" element={<Navigate to="/student/events" replace />} />
              <Route path="/apply" element={<Navigate to="/student/events" replace />} />
              <Route path="/my-applications" element={<Navigate to="/student/applications" replace />} />
              <Route path="/applications/:code" element={studentRoute(<ApplicationDetails />)} />

              <Route path="/" element={<RoleHomeRedirect />} />
              <Route path="*" element={<RoleHomeRedirect />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
