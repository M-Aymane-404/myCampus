import { apiAuth } from '../services/apiAuth';

export async function authenticatedFetch(url, options = {}) {
  try {
    const headers = await apiAuth.getAuthHeadersWithRefresh();

    const finalHeaders = {
      ...headers,
      ...options.headers,
    };

    const response = await fetch(url, {
      ...options,
      headers: finalHeaders,
      credentials: 'include',
    });

    if (response.status === 401) {
      try {
        await apiAuth.refreshAccessToken();

        const newHeaders = await apiAuth.getAuthHeadersWithRefresh();
        return await fetch(url, {
          ...options,
          headers: {
            ...newHeaders,
            ...options.headers,
          },
          credentials: 'include',
        });
      } catch (refreshError) {
        throw refreshError;
      }
    }

    return response;
  } catch (error) {
    throw error;
  }
}

export async function authenticatedGet(url, options = {}) {
  return authenticatedFetch(url, {
    method: 'GET',
    ...options,
  });
}

export async function authenticatedPost(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'POST',
    body: JSON.stringify(data),
    ...options,
  });
}

export async function authenticatedPut(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'PUT',
    body: JSON.stringify(data),
    ...options,
  });
}

export async function authenticatedDelete(url, options = {}) {
  return authenticatedFetch(url, {
    method: 'DELETE',
    ...options,
  });
}

export async function authenticatedPatch(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'PATCH',
    body: JSON.stringify(data),
    ...options,
  });
}
