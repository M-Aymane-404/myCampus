const BASE_URL = 'http://localhost:8080/api/admin/structure/classes';
import { apiAuth } from './apiAuth';

export const apiClasses = {
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
        credentials: 'include'
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
        credentials: 'include'
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

  async creer(classe) {
    try {
      const response = await fetch(`${BASE_URL}`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(classe),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error creating class:', err);
      throw err;
    }
  },

  async modifier(codeClasse, classe) {
    try {
      const response = await fetch(`${BASE_URL}/${codeClasse}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(classe),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error updating class:', err);
      throw err;
    }
  },

  async supprimer(codeClasse) {
    try {
      const response = await fetch(`${BASE_URL}/${codeClasse}`, {
        method: 'DELETE',
        headers: apiAuth.getAuthHeaders(),
        credentials: 'include'
      });

      if (!response.ok) {
        const errorData = await response.json().catch(() => ({}));
        throw new Error(errorData.message || `Erreur HTTP: ${response.status}`);
      }

      return true;
    } catch (err) {
      console.error('Error deleting class:', err);
      throw err;
    }
  },
};

export default apiClasses;
