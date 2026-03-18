import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/professeurs';
const PROFESSEUR_PROFILE_URL = 'http://localhost:8080/api/professeur';

export const apiProfesseurs = {
  /**
   * Get current professor's profile information
   */
  async getProfile() {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${PROFESSEUR_PROFILE_URL}/moi`, {
        method: 'GET',
        headers: headers,
        credentials: 'include'
      });

      if (!response.ok) {
        throw new Error('Erreur lors de la récupération du profil professeur');
      }

      return await response.json();
    } catch (error) {
      console.error('API Error (getProfile):', error);
      throw error;
    }
  },

  async updateProfile(profileData) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${PROFESSEUR_PROFILE_URL}/moi/modifier`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        body: JSON.stringify(profileData),
        credentials: 'include'
      });

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
        }

        throw new Error(errorData.message || 'Erreur lors de la mise a jour du profil professeur');
      }

      const contentType = response.headers.get('content-type');
      if (response.status === 204 || !contentType || !contentType.includes('application/json')) {
        return { success: true };
      }

      return await response.json();
    } catch (error) {
      console.error('API Error (updateProfile professeur):', error);
      throw error;
    }
  },

  async uploadPhoto(file) {
    try {
      const formData = new FormData();
      formData.append('photo', file);

      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const response = await fetch(`${PROFESSEUR_PROFILE_URL}/photo`, {
        method: 'PUT',
        headers,
        body: formData,
        credentials: 'include'
      });

      if (!response.ok) {
        let errorData;
        try {
          errorData = await response.json();
        } catch {
          errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
        }

        throw new Error(errorData.message || "Erreur lors de l'envoi de la photo");
      }

      const contentType = response.headers.get('content-type');
      if (!contentType || !contentType.includes('application/json')) {
        return { success: true };
      }

      return await response.json();
    } catch (error) {
      console.error('API Error (uploadPhoto professeur):', error);
      throw error;
    }
  },

  async obtenirTous() {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(BASE_URL, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération des professeurs:', erreur);
      throw erreur;
    }
  },

  async obtenirParPpr(ppr) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${ppr}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération du professeur:', erreur);
      throw erreur;
    }
  },

  async creer(professeur) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        body: JSON.stringify(professeur),
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          console.error('Réponse d\'erreur du serveur:', erreurData);
          if (erreurData.message) {
            messageErreur = erreurData.message;
          } else if (erreurData.error) {
            messageErreur = erreurData.error;
          }
        } catch (e) {
          const texteErreur = await reponse.text();
          console.error('Réponse d\'erreur (texte):', texteErreur);
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la création du professeur:', erreur);
      throw erreur;
    }
  },

  async modifier(ppr, professeur) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${ppr}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        body: JSON.stringify(professeur),
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          console.error('Réponse d\'erreur du serveur:', erreurData);
          
          // Check for detailed validation errors
          if (erreurData.details && typeof erreurData.details === 'object') {
            const detailsMessages = Object.entries(erreurData.details)
              .map(([field, message]) => `${field}: ${message}`)
              .join('\n');
            messageErreur = `Erreur de validation:\n${detailsMessages}`;
          } else if (erreurData.message) {
            messageErreur = erreurData.message;
          } else if (erreurData.error) {
            messageErreur = erreurData.error;
          }
        } catch (e) {
          const texteErreur = await reponse.text();
          console.error('Réponse d\'erreur (texte):', texteErreur);
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification du professeur:', erreur);
      throw erreur;
    }
  },

  async supprimer(ppr) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${ppr}`, {
        method: 'DELETE',
        headers: headers,
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression du professeur:', erreur);
      throw erreur;
    }
  },

  async activer(ppr) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${ppr}/activer`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de l\'activation du professeur:', erreur);
      throw erreur;
    }
  },

  async desactiver(ppr) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${ppr}/desactiver`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la désactivation du professeur:', erreur);
      throw erreur;
    }
  },
};
