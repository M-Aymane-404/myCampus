const BASE_URL = 'http://localhost:8080/api/announcements';
import { apiAuth } from './apiAuth';

export const apiAnnonces = {
  // Get all announcements
  async obtenirToutes() {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching announcements:', err);
      throw err;
    }
  },

  // Get unread count for professor
  async obtenirNombreNonLus() {
    try {
      const response = await fetch(`${BASE_URL}/unread/count`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return data.count || 0;
    } catch (err) {
      console.error('Error fetching unread count:', err);
      return 0;
    }
  },

  // Create a new announcement (Admin only)
  async creer(annonce) {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'POST',
        headers: apiAuth.getAuthHeaders(),
        body: JSON.stringify(annonce),
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({ message: 'Erreur lors de la création' }));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error creating announcement:', err);
      throw err;
    }
  },

  // Delete an announcement (Admin only)
  async supprimer(id) {
    try {
      const response = await fetch(`${BASE_URL}/${id}`, {
        method: 'DELETE',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return true;
    } catch (err) {
      console.error('Error deleting announcement:', err);
      throw err;
    }
  },

  // Mark announcement as read (Professor)
  async marquerCommeLu(id) {
    try {
      const response = await fetch(`${BASE_URL}/${id}/read`, {
        method: 'POST',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return true;
    } catch (err) {
      console.error('Error marking as read:', err);
      throw err;
    }
  },
};

export default apiAnnonces;
