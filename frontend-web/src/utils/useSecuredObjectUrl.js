import { useEffect, useState } from 'react';

import { apiAuth } from '../services/apiAuth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080';

function resolveUrl(pathOrUrl) {
  if (!pathOrUrl) return null;
  const trimmed = String(pathOrUrl).trim();
  if (!trimmed) return null;
  return trimmed.startsWith('http') ? trimmed : `${API_BASE_URL}${trimmed}`;
}

export function useSecuredObjectUrl(pathOrUrl, { enabled = true } = {}) {
  const [url, setUrl] = useState(null);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState(null);

  useEffect(() => {
    if (!enabled) return undefined;

    const absoluteUrl = resolveUrl(pathOrUrl);
    if (!absoluteUrl) {
      setUrl(null);
      setLoading(false);
      setError(null);
      return undefined;
    }

    const abortController = new AbortController();
    let objectUrlToRevoke = null;

    (async () => {
      try {
        setLoading(true);
        setError(null);

        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const response = await fetch(absoluteUrl, {
          method: 'GET',
          headers,
          credentials: 'include',
          signal: abortController.signal,
        });

        if (!response.ok) {
          throw new Error(`Erreur photo (${response.status})`);
        }

        const blob = await response.blob();
        if (abortController.signal.aborted) return;

        objectUrlToRevoke = URL.createObjectURL(blob);
        setUrl(objectUrlToRevoke);
      } catch (e) {
        if (abortController.signal.aborted) return;
        console.error('Erreur chargement photo securisee:', e);
        setUrl(null);
        setError(e);
      } finally {
        if (!abortController.signal.aborted) {
          setLoading(false);
        }
      }
    })();

    return () => {
      abortController.abort();
      if (objectUrlToRevoke) {
        URL.revokeObjectURL(objectUrlToRevoke);
      }
    };
  }, [pathOrUrl, enabled]);

  return { url, loading, error };
}

