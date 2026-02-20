const BASE_URL = 'http://localhost:8080/api/notifications';
import { apiAuth } from './apiAuth';

export const apiNotifications = {
  // Create a notification (for announcements)
  async creer(notification) {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'POST',
        headers: apiAuth.getAuthHeaders(),
        body: JSON.stringify(notification),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Erreur lors de la création' }));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error creating notification:', err);
      throw err;
    }
  },

  // Get all notifications
  async obtenirToutes() {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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
};

export default apiNotifications;
