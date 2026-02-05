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
    localStorage.setItem('mycampus_token', mockToken);
    localStorage.setItem('mycampus_user', JSON.stringify(mockUser));

    console.log('Mock login successful:', { email, role, mockToken });

    return {
      token: mockToken,
      user: mockUser
    };
  },

  async login(email, password) {
    // MOCK MODE: Always use mock login (bypasses backend)
    if (MOCK_MODE) {
      return this.mockLogin(email, password, 'admin');
    }
    try {
      const requestBody = {
        email: email,
        motDePasse: password // Matching backend ConnectionDTO field name
      };

      console.log('Attempting login with:', { email });

      const response = await fetch(`${BASE_URL}/connecter`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      if (!response.ok) {
        let errorMessage = 'Erreur de connexion';
        if (response.status === 401) {
          errorMessage = 'Email ou mot de passe incorrect';
        } else if (response.status === 500) {
          errorMessage = 'Erreur serveur. Veuillez réessayer plus tard.';
        }
        throw new Error(errorMessage);
      }

      const data = await response.json();

      // Store token
      if (data.access_token) {
        const token = data.access_token;
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
    // Clear all auth data
    localStorage.removeItem('mycampus_token');
    localStorage.removeItem('mycampus_user');
    sessionStorage.clear();

    // Redirect to login
    window.location.href = '/';
  },

  getToken() {
    return localStorage.getItem('mycampus_token');
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
      const headers = this.getAuthHeaders();
      console.log('Fetching backend profile from:', `${BASE_URL}/profile`);
      console.log('Request headers:', {
        ...headers,
        'Authorization': headers.Authorization ? 'Bearer [HIDDEN]' : 'None'
      });

      const response = await fetch(`${BASE_URL}/profile`, {
        method: 'GET',
        headers: headers,
      });

      if (!response.ok) {
        if (response.status === 401) {
          console.error('Authentication failed (401). logging out...');
          this.logout();
          throw new Error('Session expirée. Veuillez vous reconnecter.');
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

  // Helper function to add auth headers to requests
  getAuthHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
    };
  },
};
