const BASE_URL = 'http://localhost:8080/api/logs';
import { apiAuth } from './apiAuth';

export const apiAudit = {
  async obtenirTous() {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      const data = await response.json();
      return data;
    } catch (err) {
      console.error('Error fetching audit logs:', err);
      throw err;
    }
  },

  async obtenirParUtilisateur(userId) {
    try {
      const response = await fetch(`${BASE_URL}/user/${userId}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching user logs:', err);
      throw err;
    }
  },

  async obtenirParDate(debut, fin) {
    try {
      const response = await fetch(`${BASE_URL}?debut=${debut}&fin=${fin}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching logs by date:', err);
      throw err;
    }
  },
};

export default apiAudit;
