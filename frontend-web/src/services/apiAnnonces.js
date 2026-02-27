const BASE_URL = 'http://localhost:8080/api';
import { apiAuth } from './apiAuth';

export const apiAnnonces = {
  // Student specific endpoints
  etudiant: {
    // Get all announcements for student
    async obtenirToutes() {
      try {
        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const response = await fetch(`${BASE_URL}/etudiant/annonces`, {
          method: 'GET',
          headers: headers,
          credentials: 'include'
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        return await response.json();
      } catch (err) {
        console.error('Error fetching student announcements:', err);
        throw err;
      }
    },

    // Get announcement by ID for student
    async obtenirParId(id) {
      try {
        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const response = await fetch(`${BASE_URL}/etudiant/annonces/${id}`, {
          method: 'GET',
          headers: headers,
          credentials: 'include'
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        return await response.json();
      } catch (err) {
        console.error('Error fetching announcement by ID:', err);
        throw err;
      }
    }
  },

  // Get all announcements (admin/professor)
  async obtenirToutes() {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${BASE_URL}/admin/annonces`, {
        method: 'GET',
        headers: headers,
        credentials: 'include'
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
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${BASE_URL}/unread/count`, {
        method: 'GET',
        headers: headers,
        credentials: 'include'
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
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${BASE_URL}/admin/annonces`, {
        method: 'POST',
        headers: {
          ...headers,
          'Content-Type': 'application/json'
        },
        body: JSON.stringify(annonce),
        credentials: 'include'
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
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${BASE_URL}/admin/annonces/${id}`, {
        method: 'DELETE',
        headers: headers,
        credentials: 'include'
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
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${BASE_URL}/${id}/read`, {
        method: 'POST',
        headers: headers,
        credentials: 'include'
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
