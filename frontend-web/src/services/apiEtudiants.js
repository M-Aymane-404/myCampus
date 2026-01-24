const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/etudiants';
import { apiAuth } from './apiAuth';

export const apiEtudiants = {
  async obtenirTous() {
    try {
      const reponse = await fetch(BASE_URL, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération des étudiants:', erreur);
      throw erreur;
    }
  },

  async obtenirParApogee(apogee) {
    try {
      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération de l\'étudiant:', erreur);
      throw erreur;
    }
  },

  async creer(etudiant) {
    try {
      const headers = {
        'Content-Type': 'application/json',
        ...apiAuth.getAuthHeaders()
      };

      const reponse = await fetch(BASE_URL, {
        method: 'POST',
        headers: headers,
        body: JSON.stringify(etudiant),
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
      console.error('Erreur lors de la création de l\'étudiant:', erreur);
      throw erreur;
    }
  },

  async modifier(apogee, etudiant) {
    try {
      const headers = {
        'Content-Type': 'application/json',
        ...apiAuth.getAuthHeaders()
      };

      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
        method: 'PUT',
        headers: headers,
        body: JSON.stringify(etudiant),
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
      console.error('Erreur lors de la modification de l\'étudiant:', erreur);
      throw erreur;
    }
  },

  async supprimer(apogee) {
    try {
      const headers = {
        'Content-Type': 'application/json',
        ...apiAuth.getAuthHeaders()
      };

      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
        method: 'DELETE',
        headers: headers,
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression de l\'étudiant:', erreur);
      throw erreur;
    }
  },

  async activer(apogee) {
    try {
      const reponse = await fetch(`${BASE_URL}/${apogee}/activer`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de l\'activation de l\'étudiant:', erreur);
      throw erreur;
    }
  },

  async desactiver(apogee) {
    try {
      const reponse = await fetch(`${BASE_URL}/${apogee}/desactiver`, {
        method: 'PATCH',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la désactivation de l\'étudiant:', erreur);
      throw erreur;
    }
  },
};
