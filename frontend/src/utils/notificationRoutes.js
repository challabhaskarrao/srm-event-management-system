const isStaffWorkspace = (path) => path === '/staff/mentor' || path === '/staff/hod';

export const isSafeNotificationPath = (path) => (
  typeof path === 'string' &&
  path.startsWith('/') &&
  !path.startsWith('//') &&
  !path.includes('..') &&
  !path.includes('\\') &&
  !path.includes('\r') &&
  !path.includes('\n')
);

export const resolveNotificationPath = (notification) => {
  const path = notification?.actionPath;
  if (!isSafeNotificationPath(path)) return null;

  if (isStaffWorkspace(path) && notification.recordId && notification.recordType === 'APPLICATION') {
    return `${path}?applicationId=${encodeURIComponent(notification.recordId)}`;
  }
  if (isStaffWorkspace(path) && notification.recordId && notification.recordType === 'ACHIEVEMENT') {
    return `${path}?achievementId=${encodeURIComponent(notification.recordId)}`;
  }
  return path;
};
