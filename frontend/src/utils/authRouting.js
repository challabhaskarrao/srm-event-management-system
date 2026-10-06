export const STAFF_ROLES = Object.freeze(['MENTOR', 'HOD', 'ADMIN']);

export const isStaffRole = (role) => STAFF_ROLES.includes(role);

export const canAccessRole = (role, allowedRoles = []) => allowedRoles.includes(role);

export const homeForRole = (role) => {
  switch (role) {
    case 'STUDENT':
      return '/student/dashboard';
    case 'MENTOR':
      return '/staff/mentor';
    case 'HOD':
      return '/staff/hod';
    case 'ADMIN':
      return '/staff/admin';
    default:
      return '/login';
  }
};
