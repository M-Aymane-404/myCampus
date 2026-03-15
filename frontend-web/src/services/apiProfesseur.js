import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/professor';

export const apiProfesseur = {
  async obtenirCours() {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/courses`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching courses:', err);
      throw err;
    }
  },

  async obtenirEtudiantsParCours(courseId) {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/courses/${courseId}/students`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching students:', err);
      throw err;
    }
  },

  async obtenirNotes(courseId, assessmentId = null) {
    try {
      const url = assessmentId
        ? `${BASE_URL}/courses/${courseId}/assessments/${assessmentId}/grades`
        : `${BASE_URL}/courses/${courseId}/grades`;

      const response = await authenticatedFetch(url, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching grades:', err);
      throw err;
    }
  },

  async mettreAJourNotes(courseId, assessmentId, notes) {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/courses/${courseId}/assessments/${assessmentId}/grades`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ grades: notes }),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error updating grades:', err);
      throw err;
    }
  },

  async obtenirPresence(courseId, sessionId = null) {
    try {
      const url = sessionId
        ? `${BASE_URL}/courses/${courseId}/sessions/${sessionId}/attendance`
        : `${BASE_URL}/courses/${courseId}/attendance`;

      const response = await authenticatedFetch(url, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching attendance:', err);
      throw err;
    }
  },

  async mettreAJourPresence(courseId, sessionId, presence) {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/courses/${courseId}/sessions/${sessionId}/attendance`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({ attendance: presence }),
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error updating attendance:', err);
      throw err;
    }
  },

  async obtenirCalendrier() {
    try {
      const response = await authenticatedFetch(`${BASE_URL}/calendar`, {
        method: 'GET',
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error(`Erreur HTTP: ${response.status}`);
      }

      return await response.json();
    } catch (err) {
      console.error('Error fetching calendar:', err);
      throw err;
    }
  },
};

export default apiProfesseur;
