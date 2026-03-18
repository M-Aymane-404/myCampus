import { authenticatedFetch } from './apiInterceptor';

const API_HOST = 'http://localhost:8080';

export const readFilenameFromResponse = (response, fallback = 'document.pdf') => {
  const disposition = response.headers.get('content-disposition') || '';
  const match = disposition.match(/filename\*?=(?:UTF-8''|")?([^\";]+)/i);
  return decodeURIComponent((match?.[1] || fallback).replace(/"/g, '').trim());
};

export const openBlobInNewTab = (blob) => {
  const objectUrl = window.URL.createObjectURL(blob);
  window.open(objectUrl, '_blank', 'noopener,noreferrer');
  window.setTimeout(() => window.URL.revokeObjectURL(objectUrl), 60000);
};

export const downloadBlob = (blob, filename) => {
  const objectUrl = window.URL.createObjectURL(blob);
  const link = document.createElement('a');
  link.href = objectUrl;
  link.download = filename;
  document.body.appendChild(link);
  link.click();
  document.body.removeChild(link);
  window.setTimeout(() => window.URL.revokeObjectURL(objectUrl), 60000);
};

export const normalizeStoredFileUrl = (path) => {
  if (!path) return null;
  if (/^https?:\/\//i.test(path)) return path;

  const normalizedPath = String(path).trim().replace(/\\/g, '/');

  if (normalizedPath.startsWith('/files/')) {
    return `${API_HOST}${normalizedPath}`;
  }

  if (normalizedPath.startsWith('/uploads/')) {
    return `${API_HOST}${normalizedPath.replace(/^\/uploads/, '/files')}`;
  }

  if (
    normalizedPath.startsWith('/copies/') ||
    normalizedPath.startsWith('/corriges/') ||
    normalizedPath.startsWith('/examens/')
  ) {
    return `${API_HOST}/files${normalizedPath}`;
  }

  if (normalizedPath.startsWith('uploads/')) {
    return `${API_HOST}/files/${normalizedPath.slice('uploads/'.length)}`;
  }

  return `${API_HOST}/files/${normalizedPath.replace(/^\/+/, '')}`;
};

export const fetchAuthenticatedBlob = async (url) => {
  const response = await authenticatedFetch(url, { credentials: 'include' });
  if (!response.ok) {
    const errorText = await response.text().catch(() => '');
    throw new Error(errorText || 'Impossible de recuperer le fichier.');
  }
  return response;
};
