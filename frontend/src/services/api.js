export const BASE_URL = (import.meta.env?.VITE_API_BASE_URL || 'http://localhost:8080/api').replace(/\/$/, '');

export class ApiError extends Error {
  constructor(message, status, details = null) {
    super(message);
    this.name = 'ApiError';
    this.status = status;
    this.details = details;
  }
}

const clearStoredSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('user');
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('auth:unauthorized'));
  }
};

export const downloadApi = async (endpoint) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`${BASE_URL}${endpoint}`, {
    headers: token ? { Authorization: `Bearer ${token}` } : {},
  });
  if (!response.ok) {
    if (response.status === 401) clearStoredSession();
    throw new ApiError(response.statusText || 'Unable to download document.', response.status);
  }
  return response.blob();
};

export const fetchApi = async (endpoint, options = {}) => {
  const token = localStorage.getItem('token');
  
  const headers = { ...options.headers };

  if (options.body && !(options.body instanceof FormData) && !headers['Content-Type']) {
    headers['Content-Type'] = 'application/json';
  }

  if (token) {
    headers['Authorization'] = `Bearer ${token}`;
  }

  const response = await fetch(`${BASE_URL}${endpoint}`, {
    ...options,
    headers,
  });

  if (!response.ok) {
    let errorMessage = 'An error occurred while fetching data.';
    let errorDetails = null;
    try {
      errorDetails = await response.json();
      errorMessage = errorDetails.message || errorDetails.detail || errorDetails.error || errorMessage;
    } catch {
      errorMessage = response.statusText || errorMessage;
    }
    if (response.status === 401) {
      clearStoredSession();
    }
    throw new ApiError(errorMessage, response.status, errorDetails);
  }

  const contentType = response.headers.get('content-type');
  if (contentType?.includes('application/json')) {
    return response.json();
  }
  
  return null;
};
