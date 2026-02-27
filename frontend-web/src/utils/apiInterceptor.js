import { apiAuth } from '../services/apiAuth';

// Enhanced fetch wrapper with automatic token refresh
export async function authenticatedFetch(url, options = {}) {
  try {
    // Get auth headers with automatic refresh
    const headers = await apiAuth.getAuthHeadersWithRefresh();
    
    // Merge with any additional headers provided
    const finalHeaders = {
      ...headers,
      ...options.headers,
    };

    const response = await fetch(url, {
      ...options,
      headers: finalHeaders,
      credentials: 'include',
    });

    // If we get a 401, try to refresh the token once more
    if (response.status === 401) {
      console.log('Received 401, attempting token refresh...');
      
      try {
        await apiAuth.refreshAccessToken();
        
        // Retry the request with the new token
        const newHeaders = await apiAuth.getAuthHeadersWithRefresh();
        const retryResponse = await fetch(url, {
          ...options,
          headers: {
            ...newHeaders,
            ...options.headers,
          },
          credentials: 'include',
        });

        return retryResponse;
      } catch (refreshError) {
        console.error('Token refresh failed during retry:', refreshError);
        // Refresh failed, logout user (already handled in refreshAccessToken)
        throw refreshError;
      }
    }

    // For 500 errors, don't try to refresh - it's a backend issue
    if (response.status === 500) {
      console.warn('Backend internal server error (500) - not attempting token refresh');
      return response; // Let the caller handle the 500 error
    }

    return response;
  } catch (error) {
    console.error('API request failed:', error);
    throw error;
  }
}

// Utility function for GET requests
export async function authenticatedGet(url, options = {}) {
  return authenticatedFetch(url, {
    method: 'GET',
    ...options,
  });
}

// Utility function for POST requests
export async function authenticatedPost(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'POST',
    body: JSON.stringify(data),
    ...options,
  });
}

// Utility function for PUT requests
export async function authenticatedPut(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'PUT',
    body: JSON.stringify(data),
    ...options,
  });
}

// Utility function for DELETE requests
export async function authenticatedDelete(url, options = {}) {
  return authenticatedFetch(url, {
    method: 'DELETE',
    ...options,
  });
}

// Utility function for PATCH requests
export async function authenticatedPatch(url, data, options = {}) {
  return authenticatedFetch(url, {
    method: 'PATCH',
    body: JSON.stringify(data),
    ...options,
  });
}
