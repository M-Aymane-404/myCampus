const BASE_URL = 'http://localhost:8080/api/auth';

// MOCK MODE: Set to true to bypass backend authentication
const MOCK_MODE = true;

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

  async login(email, password, role = null) {
    // MOCK MODE: Always use mock login (bypasses backend)
    if (MOCK_MODE) {
      // If role is provided, use it; otherwise default to admin
      const selectedRole = role || 'admin';
      return this.mockLogin(email, password, selectedRole);
    }
    try {
      // First try with just email and password (most common format)
      const requestBody = {
        email: email,
        password: password
      };

      console.log('Attempting login with:', { email, passwordLength: password.length });

      const response = await fetch(`${BASE_URL}/login`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(requestBody),
      });

      console.log('Login response status:', response.status);

      // Handle different response statuses
      if (!response.ok) {
        let errorMessage = `Erreur HTTP: ${response.status}`;

        try {
          const errorData = await response.json();
          errorMessage = errorData.message || errorData.error || errorMessage;

          // Handle specific error cases
          if (response.status === 500) {
            errorMessage = 'Erreur interne du serveur';
          } else if (response.status === 401) {
            errorMessage = 'Email ou mot de passe incorrect';
          } else if (response.status === 404) {
            errorMessage = 'Service d\'authentification non disponible';
          }
        } catch (parseError) {
          // If JSON parsing fails, use status-based message
          if (response.status === 500) {
            errorMessage = 'Erreur interne du serveur';
          }
        }

        throw new Error(errorMessage);
      }

      const data = await response.json();

      // Store token and user data
      if (data.token) {
        localStorage.setItem('mycampus_token', data.token);
      }
      if (data.user) {
        localStorage.setItem('mycampus_user', JSON.stringify(data.user));
      } else if (data) {
        // If the entire response is user data
        localStorage.setItem('mycampus_user', JSON.stringify(data));
      }

      return data;
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

  // Helper function to add auth headers to requests
  getAuthHeaders() {
    const token = this.getToken();
    return {
      'Content-Type': 'application/json',
      ...(token && { 'Authorization': `Bearer ${token}` }),
    };
  },
};
