import React from 'react';
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom';
import { AuthProvider } from './context/AuthContext';
import Navbar from './components/Navbar';
import ProtectedRoute from './components/ProtectedRoute';

import Login from './pages/auth/Login';
import SignUp from './pages/auth/SignUp';
import ForgotPassword from './pages/auth/ForgotPassword';

import Dashboard from './pages/student/Dashboard';
import ApplyEvent from './pages/student/ApplyEvent';
import MyApplications from './pages/student/MyApplications';
import ApplicationDetails from './pages/student/ApplicationDetails';

import './styles.css';

function App() {
  return (
    <AuthProvider>
      <BrowserRouter>
        <div className="app-layout">
          <Navbar />
          <main className="main-content">
            <Routes>
              {/* Public Routes */}
              <Route path="/login" element={<Login />} />
              <Route path="/signup" element={<SignUp />} />
              <Route path="/forgot-password" element={<ForgotPassword />} />

              {/* Default Route redirects to Dashboard */}
              <Route path="/" element={<Navigate to="/dashboard" replace />} />

              {/* Protected Student Routes */}
              <Route path="/dashboard" element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <Dashboard />
                </ProtectedRoute>
              } />

              <Route path="/apply" element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <ApplyEvent />
                </ProtectedRoute>
              } />
              
              <Route path="/my-applications" element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <MyApplications />
                </ProtectedRoute>
              } />
              
              <Route path="/applications/:code" element={
                <ProtectedRoute allowedRoles={['STUDENT']}>
                  <ApplicationDetails />
                </ProtectedRoute>
              } />

              {/* Catch all */}
              <Route path="*" element={<Navigate to="/dashboard" replace />} />
            </Routes>
          </main>
        </div>
      </BrowserRouter>
    </AuthProvider>
  );
}

export default App;
