const BASE_URL = 'http://localhost:8080/api/professor';
import { apiAuth } from './apiAuth';

export const apiProfesseur = {
  // Get professor's courses
  async obtenirCours() {
    try {
      const response = await fetch(`${BASE_URL}/courses`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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

  // Get students for a course
  async obtenirEtudiantsParCours(courseId) {
    try {
      const response = await fetch(`${BASE_URL}/courses/${courseId}/students`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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

  // Get grades for a course/assessment
  async obtenirNotes(courseId, assessmentId = null) {
    try {
      const url = assessmentId 
        ? `${BASE_URL}/courses/${courseId}/assessments/${assessmentId}/grades`
        : `${BASE_URL}/courses/${courseId}/grades`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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

  // Update grades
  async mettreAJourNotes(courseId, assessmentId, notes) {
    try {
      const response = await fetch(`${BASE_URL}/courses/${courseId}/assessments/${assessmentId}/grades`, {
        method: 'PUT',
        headers: apiAuth.getAuthHeaders(),
        body: JSON.stringify({ grades: notes }),
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

  // Get attendance for a course/session
  async obtenirPresence(courseId, sessionId = null) {
    try {
      const url = sessionId
        ? `${BASE_URL}/courses/${courseId}/sessions/${sessionId}/attendance`
        : `${BASE_URL}/courses/${courseId}/attendance`;
      
      const response = await fetch(url, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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

  // Update attendance
  async mettreAJourPresence(courseId, sessionId, presence) {
    try {
      const response = await fetch(`${BASE_URL}/courses/${courseId}/sessions/${sessionId}/attendance`, {
        method: 'PUT',
        headers: apiAuth.getAuthHeaders(),
        body: JSON.stringify({ attendance: presence }),
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

  // Get academic calendar
  async obtenirCalendrier() {
    try {
      const response = await fetch(`${BASE_URL}/calendar`, {
        method: 'GET',
        headers: apiAuth.getAuthHeaders(),
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
