const BASE_URL = 'http://localhost:8080/auth';

// MOCK MODE: Set to false to use real backend
const MOCK_MODE = false;

export const apiAuth = {
  // Mock login - accepts any credentials and returns success based on selected role
  async mockLogin(email, password, role) {
    // Simulate API delay
    await new Promise(resolve => setTimeout(resolve, 300));

    // Create mock user data based on selected role
    const mockUser = {
      email: email,
      role: role,
      id: Date.now(),
      nom: role === 'admin' ? 'Admin' : role === 'professor' ? 'Professor' : 'Student',
      prenom: 'User'
    };

    // Store mock token and user data
    const mockToken = 'mock_token_' + Date.now();
    // In mock mode, we don't have refresh tokens since backend handles them via cookies
    localStorage.setItem('mycampus_token', mockToken);
    localStorage.setItem('mycampus_user', JSON.stringify(mockUser));

    console.log('Mock login successful:', { email, role, mockToken });

    return {
      token: mockToken,
      user: mockUser
    };
  },

  async login(email, password, recaptchaToken) {
    // MOCK MODE: Always use mock login (bypasses backend)
    if (MOCK_MODE) {
      return this.mockLogin(email, password, 'admin');
    }
    try {
      const requestBody = {
        email: email,
        motDePasse: password, // Matching backend ConnectionDTO field name
        recaptchaToken: recaptchaToken
      };

      console.log('Attempting login with:', { email, recaptchaToken: recaptchaToken ? 'present' : 'missing' });
      console.log('Request body:', JSON.stringify(requestBody, null, 2));

      const response = await fetch(`${BASE_URL}/connecter`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
        credentials: 'include'
      });

      if (!response.ok) {
        let errorMessage = 'Erreur de connexion';
        let errorData = null;
        try {
          errorData = await response.json();
          console.error('Backend error response:', errorData);
          errorMessage = errorData.message || errorData.error || errorMessage;
        } catch (e) {
          console.error('Could not parse error response:', response.status, response.statusText);
        }
        
        if (response.status === 400) {
          errorMessage = errorData?.message || 'Requete invalide. Verifiez les informations saisies.';
        } else if (response.status === 401) {
          errorMessage = 'Email ou mot de passe incorrect';
        } else if (response.status === 500) {
          errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();

      // Store tokens
      if (data.access_token) {
        const token = data.access_token;
        // Refresh token is stored in HTTP-only cookie by backend
        localStorage.setItem('mycampus_token', token);

        // Decode JWT to extract role from 'scope' claim
        try {
          const payloadBase64 = token.split('.')[1];
          const decodedPayload = JSON.parse(atob(payloadBase64));
          const scope = decodedPayload.scope || '';

          // Normalize roles from backend (e.g., "ROLE_ADMIN ROLE_USER" -> "admin")
          let role = 'student'; // default
          if (scope.includes('ROLE_ADMIN')) role = 'admin';
          else if (scope.includes('ROLE_PROFESSEUR')) role = 'professor';
          else if (scope.includes('ROLE_ETUDIANT')) role = 'student';

          const user = {
            email: email,
            role: role,
            nom: '', // Will be populated from backend user profile
            prenom: '', // Will be populated from backend user profile
            nomUtilisateur: decodedPayload.sub || email // Store username/email separately
          };

          localStorage.setItem('mycampus_user', JSON.stringify(user));
          return { token, user };
        } catch (e) {
          console.error('Error decoding token:', e);
          throw new Error('Erreur lors du traitement du jeton de connexion');
        }
      }

      throw new Error('Réponse du serveur invalide');
    } catch (erreur) {
      console.error('Erreur lors de la connexion:', erreur);
      throw erreur;
    }
  },

  logout() {
    // Clear all auth data including refresh token
    localStorage.removeItem('mycampus_token');
    localStorage.removeItem('mycampus_refresh_token');
    localStorage.removeItem('mycampus_user');
    sessionStorage.clear();

    // Redirect to login
    window.location.href = '/';
  },

  getToken() {
    return localStorage.getItem('mycampus_token');
  },

  getRefreshToken() {
    return localStorage.getItem('mycampus_refresh_token');
  },

  setTokens(accessToken, refreshToken) {
    localStorage.setItem('mycampus_token', accessToken);
    if (refreshToken) {
      localStorage.setItem('mycampus_refresh_token', refreshToken);
    }
  },

  getUser() {
    const userStr = localStorage.getItem('mycampus_user');
    return userStr ? JSON.parse(userStr) : null;
  },

  isAuthenticated() {
    return !!this.getToken();
  },

  updateUser(updatedData) {
    const currentUser = this.getUser();
    if (currentUser) {
      const newUser = { ...currentUser, ...updatedData };
      localStorage.setItem('mycampus_user', JSON.stringify(newUser));
      return newUser;
    }
    return null;
  },

  async getBackendProfile() {
    try {
      const headers = await this.getAuthHeadersWithRefresh();
      console.log('Fetching backend profile from:', `${BASE_URL}/profile`);
      console.log('Request headers:', {
        ...headers,
        'Authorization': headers.Authorization ? 'Bearer [HIDDEN]' : 'None'
      });

      const response = await fetch(`${BASE_URL}/profile`, {
        method: 'GET',
        headers: headers,
        credentials: 'include'
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.error('Authentication failed (401). Attempting token refresh...');
          
          try {
            await this.refreshAccessToken();
            const newHeaders = await this.getAuthHeadersWithRefresh();
            
            // Retry the request with new token
            const retryResponse = await fetch(`${BASE_URL}/profile`, {
              method: 'GET',
              headers: newHeaders,
              credentials: 'include'
            });

            if (!retryResponse.ok) {
              if (retryResponse.status === 401) {
                console.error('Authentication failed after refresh. logging out...');
                this.logout();
                throw new Error('Session expirée. Veuillez vous reconnecter.');
              }
              const errorText = await retryResponse.text();
              console.error(`Profile fetch error after retry (${retryResponse.status}):`, errorText);
              throw new Error(`Erreur lors de la récupération du profil (${retryResponse.status})`);
            }

            return await retryResponse.json();
          } catch (refreshError) {
            console.error('Token refresh failed:', refreshError);
            // Error is already handled in refreshAccessToken (logout happened there)
            throw refreshError;
          }
        } else if (response.status === 500) {
          // Backend internal error - don't try to refresh, just report the error
          const errorText = await response.text();
          console.error('Backend internal server error (500):', errorText);
          throw new Error('Erreur serveur temporaire. Veuillez réessayer plus tard.');
        }
        const errorText = await response.text();
        console.error(`Profile fetch error (${response.status}):`, errorText);
        throw new Error(`Erreur lors de la récupération du profil (${response.status})`);
      }

      return await response.json();
    } catch (error) {
      console.error('API Error (getBackendProfile):', error);
      throw error;
    }
  },

  async refreshAccessToken() {
    try {
      // Your backend uses HTTP-only cookies for refresh tokens
      // No need to send refreshToken in body - it's automatically sent in cookies
      
      const response = await fetch(`${BASE_URL}/rafraichir-jeton`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include', // Important: include cookies
        body: JSON.stringify({}) // Empty body since refresh token is in cookie
      });

      if (!response.ok) {
        if (response.status === 401) {
          // Refresh token is invalid or expired, logout user
          this.logout();
          throw new Error('Session expirée. Veuillez vous reconnecter.');
        } else if (response.status === 500) {
          // Backend has an internal error - handle gracefully
          console.warn('Backend token refresh error (500), logging out for security...');
          this.logout();
          throw new Error('Le service de rafraîchissement est temporairement indisponible. Veuillez vous reconnecter.');
        }
        throw new Error('Erreur lors du rafraîchissement du token');
      }

      const data = await response.json();
      
      if (data.access_token) {
        // Update access token, refresh token is handled by cookies
        localStorage.setItem('mycampus_token', data.access_token);
        return data.access_token;
      }

      throw new Error('Réponse de rafraîchissement invalide');
    } catch (error) {
      console.error('Token refresh error:', error);
      // Ensure logout on any refresh error to prevent stuck sessions
      if (this.getToken()) {
        this.logout();
      }
      throw error;
    }
  },

  // Helper function to check if token is expired
  isTokenExpired(token) {
    try {
      if (!token) return true;
      
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      const currentTime = Math.floor(Date.now() / 1000);
      
      // Add 30 seconds buffer to account for clock skew
      return decodedPayload.exp < (currentTime + 30);
    } catch (error) {
      console.error('Error checking token expiration:', error);
      return true; // Assume expired if we can't decode
    }
  },

  // Helper function to add auth headers to requests (legacy)
  getAuthHeaders() {
    const token = this.getToken();
    return {
      ...(token && { 'Authorization': `Bearer ${token}` }),
    };
  },

  // Enhanced auth headers with automatic token refresh
  async getAuthHeadersWithRefresh() {
    let token = this.getToken();
    
    // Check if token is expired and refresh if needed
    if (token && this.isTokenExpired(token)) {
      try {
        console.log('Token expired, attempting refresh...');
        token = await this.refreshAccessToken();
      } catch (error) {
        console.error('Failed to refresh token:', error);
        // Token refresh failed, user will need to login again
        throw error;
      }
    }

    return {
      ...(token && { 'Authorization': `Bearer ${token}` }),
    };
  },
};
