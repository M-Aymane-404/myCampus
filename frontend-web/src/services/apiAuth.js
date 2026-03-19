const BASE_URL = `${import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080'}/auth`;
const MOCK_MODE = false;
const USER_STORAGE_KEY = 'mycampus_user';

let accessTokenMemo = null;

function normalizeRoleFromAuthorities(authorities = []) {
  const joined = Array.isArray(authorities) ? authorities.join(' ') : String(authorities || '');
  if (joined.includes('ROLE_ADMIN')) return 'admin';
  if (joined.includes('ROLE_PROFESSEUR')) return 'professor';
  if (joined.includes('ROLE_ETUDIANT')) return 'student';
  return 'student';
}

function readStoredUser() {
  const userStr = sessionStorage.getItem(USER_STORAGE_KEY);
  return userStr ? JSON.parse(userStr) : null;
}

function storeUser(user) {
  if (!user) {
    sessionStorage.removeItem(USER_STORAGE_KEY);
    return null;
  }
  sessionStorage.setItem(USER_STORAGE_KEY, JSON.stringify(user));
  return user;
}

export const apiAuth = {
  async mockLogin(email, password, role) {
    await new Promise(resolve => setTimeout(resolve, 300));
    accessTokenMemo = `mock_token_${Date.now()}`;
    const mockUser = {
      email,
      role,
      id: Date.now(),
      nom: role === 'admin' ? 'Admin' : role === 'professor' ? 'Professor' : 'Student',
      prenom: 'User',
      authorities: [`ROLE_${role === 'admin' ? 'ADMIN' : role === 'professor' ? 'PROFESSEUR' : 'ETUDIANT'}`],
    };
    storeUser(mockUser);
    return { token: accessTokenMemo, user: mockUser };
  },

  async login(email, password, recaptchaToken) {
    if (MOCK_MODE) {
      return this.mockLogin(email, password, 'admin');
    }

    const response = await fetch(`${BASE_URL}/connecter`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      body: JSON.stringify({
        email,
        motDePasse: password,
        recaptchaToken,
      }),
      credentials: 'include',
    });

    if (!response.ok) {
      let errorMessage = 'Erreur de connexion';
      try {
        const errorData = await response.json();
        errorMessage = errorData.message || errorData.error || errorMessage;
      } catch {
        if (response.status === 401) errorMessage = 'Email ou mot de passe incorrect';
      }
      throw new Error(errorMessage);
    }

    const data = await response.json();
    if (!data.access_token) {
      throw new Error('Reponse du serveur invalide');
    }

    accessTokenMemo = data.access_token;
    const user = await this.fetchCurrentUser();
    return { token: accessTokenMemo, user };
  },

  logout() {
    fetch(`${BASE_URL}/deconnexion`, {
      method: 'POST',
      credentials: 'include',
      keepalive: true,
    }).catch(() => {});

    accessTokenMemo = null;
    sessionStorage.removeItem(USER_STORAGE_KEY);
    window.location.href = '/';
  },

  getToken() {
    return accessTokenMemo;
  },

  getRefreshToken() {
    return null;
  },

  setTokens(accessToken) {
    accessTokenMemo = accessToken || null;
  },

  getUser() {
    return readStoredUser();
  },

  isAuthenticated() {
    return !!accessTokenMemo;
  },

  updateUser(updatedData) {
    const currentUser = readStoredUser();
    if (!currentUser) return null;
    const newUser = { ...currentUser, ...updatedData };
    return storeUser(newUser);
  },

  async fetchCurrentUser() {
    const headers = await this.getAuthHeadersWithRefresh();
    const response = await fetch(`${BASE_URL}/me`, {
      method: 'GET',
      headers,
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error('Impossible de recuperer la session courante');
    }

    const data = await response.json();
    const user = {
      id: data.id,
      email: data.email,
      nom: data.nom || '',
      prenom: data.prenom || '',
      role: normalizeRoleFromAuthorities(data.authorities || data.role),
      authorities: data.authorities || [],
      active: data.active,
    };
    return storeUser(user);
  },

  async initializeSession() {
    if (MOCK_MODE) {
      return readStoredUser();
    }

    try {
      if (!accessTokenMemo) {
        await this.refreshAccessToken();
      }
      return await this.fetchCurrentUser();
    } catch {
      accessTokenMemo = null;
      sessionStorage.removeItem(USER_STORAGE_KEY);
      return null;
    }
  },

  async getBackendProfile() {
    const headers = await this.getAuthHeadersWithRefresh();
    const response = await fetch(`${BASE_URL}/profile`, {
      method: 'GET',
      headers,
      credentials: 'include',
    });

    if (!response.ok) {
      if (response.status === 401) {
        this.logout();
        throw new Error('Session expiree. Veuillez vous reconnecter.');
      }
      throw new Error(`Erreur lors de la recuperation du profil (${response.status})`);
    }

    return response.json();
  },

  async refreshAccessToken() {
    const response = await fetch(`${BASE_URL}/rafraichir-jeton`, {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json',
      },
      credentials: 'include',
      body: JSON.stringify({}),
    });

    if (!response.ok) {
      accessTokenMemo = null;
      sessionStorage.removeItem(USER_STORAGE_KEY);
      throw new Error('Session expiree. Veuillez vous reconnecter.');
    }

    const data = await response.json();
    if (!data.access_token) {
      throw new Error('Reponse de rafraichissement invalide');
    }

    accessTokenMemo = data.access_token;
    return accessTokenMemo;
  },

  isTokenExpired(token) {
    try {
      if (!token) return true;
      const payloadBase64 = token.split('.')[1];
      const decodedPayload = JSON.parse(atob(payloadBase64));
      const currentTime = Math.floor(Date.now() / 1000);
      return decodedPayload.exp < (currentTime + 30);
    } catch {
      return true;
    }
  },

  getAuthHeaders() {
    const token = this.getToken();
    return {
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  },

  async getAuthHeadersWithRefresh() {
    let token = this.getToken();
    if (!token || this.isTokenExpired(token)) {
      token = await this.refreshAccessToken();
    }

    return {
      ...(token && { Authorization: `Bearer ${token}` }),
    };
  },
};
