import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/notifications';

export const apiNotifications = {
  async creer(notification) {
    try {
      const response = await authenticatedFetch(`${BASE_URL}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(notification),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Erreur lors de la creation' }));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error creating notification:', err);
      throw err;
    }
  },

  async obtenirToutes() {
    try {
      const response = await authenticatedFetch(`${BASE_URL}`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching notifications:', err);
      throw err;
    }
  },

  async obtenirPage({ page = 0, size = 20 } = {}) {
    try {
      const response = await authenticatedFetch(
        `${BASE_URL}?page=${page}&size=${size}`,
        {
          method: 'GET',
          credentials: 'include',
        }
      );

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching paged notifications:', err);
      throw err;
    }
  },

  async obtenirCompteurNonLues() {
    try {
      const response = await authenticatedFetch(
        `${BASE_URL}/non-lues/compteur`,
        {
          method: 'GET',
          credentials: 'include',
        }
      );

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return Number(data.compteur || 0);
    } catch (err) {
      console.error('Error fetching unread notifications count:', err);
      return 0;
    }
  },

  async marquerCommeLue(id) {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/${id}/lire`, {
        method: 'PATCH',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error marking notification as read:', err);
      throw err;
    }
  },

  async marquerToutesCommeLues() {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/lire-tout`, {
        method: 'PATCH',
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error marking all notifications as read:', err);
      throw err;
    }
  },
};

export default apiNotifications;
