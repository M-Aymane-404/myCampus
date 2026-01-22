const BASE_URL = 'http://localhost:8080/api/admin/structure/classes';
import { apiAuth } from './apiAuth';

export const apiClasses = {
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
      console.error('Error fetching classes:', err);
      throw err;
    }
  },

  async obtenirParFiliere(filiereId) {
    try {
      // Try both codeFiliere and id formats
      const codeFiliere = typeof filiereId === 'object' ? filiereId.codeFiliere : filiereId;
      const response = await fetch(`${BASE_URL}/filiere/${codeFiliere}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching classes by filiere:', err);
      throw err;
    }
  },

  async obtenirParId(classeId) {
    try {
      const response = await fetch(`${BASE_URL}/${classeId}`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching class:', err);
      throw err;
    }
  },
};

export default apiClasses;
