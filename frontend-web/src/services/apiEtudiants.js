const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/etudiants';
import { apiAuth } from './apiAuth';

export const apiEtudiants = {
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
      console.error('Erreur lors de la rÃ©cupÃ©ration des Ã©tudiants:', erreur);
      throw erreur;
    }
  },

  async obtenirParApogee(apogee) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
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
      console.error('Erreur lors de la rÃ©cupÃ©ration de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async creer(etudiant) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(BASE_URL, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        body: JSON.stringify(etudiant),
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          console.error('RÃ©ponse d\'erreur du serveur:', erreurData);
          if (erreurData.message) {
            messageErreur = erreurData.message;
          } else if (erreurData.error) {
            messageErreur = erreurData.error;
          }
        } catch (e) {
          const texteErreur = await reponse.text();
          console.error('RÃ©ponse d\'erreur (texte):', texteErreur);
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la crÃ©ation de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async modifier(apogee, etudiant) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...headers,
        },
        body: JSON.stringify(etudiant),
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          console.error('RÃ©ponse d\'erreur du serveur:', erreurData);
          
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
          console.error('RÃ©ponse d\'erreur (texte):', texteErreur);
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async supprimer(apogee) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${apogee}`, {
        method: 'DELETE',
        headers: headers,
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async activer(apogee) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${apogee}/activer`, {
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
      console.error('Erreur lors de l\'activation de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async desactiver(apogee) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL}/${apogee}/desactiver`, {
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
      console.error('Erreur lors de la dÃ©sactivation de l\'Ã©tudiant:', erreur);
      throw erreur;
    }
  },

  async importerEtudiants(fichier, codeClasse, modeImport = 'PARTIEL', motDePasseDefaut = null) {
    try {
      const formData = new FormData();
      formData.append('fichier', fichier);
      formData.append('codeClasse', codeClasse);
      formData.append('modeImport', modeImport);
      if (motDePasseDefaut) {
        formData.append('motDePasseDefaut', motDePasseDefaut);
      }

      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`${BASE_URL.replace('/utilisateurs/etudiants', '/etudiants')}/import-fichier`, {
        method: 'POST',
        headers: {
          ...headers,
        },
        body: formData,
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          if (erreurData.message) {
            messageErreur = erreurData.message;
          } else if (erreurData.error) {
            messageErreur = erreurData.error;
          }
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de l\'import des Ã©tudiants:', erreur);
      throw erreur;
    }
  },

  async telechargerModeleImport(type) {
    try {
      const headers = await apiAuth.getAuthHeadersWithRefresh();
      const reponse = await fetch(`http://localhost:8080/api/import/template/${type}`, {
        method: 'GET',
        headers: {
          ...headers,
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          if (erreurData.message) {
            messageErreur = erreurData.message;
          } else if (erreurData.error) {
            messageErreur = erreurData.error;
          }
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const blob = await reponse.blob();
      const disposition = reponse.headers.get('content-disposition') || '';
      const nomFichierDepuisHeader = disposition.match(/filename=\"?([^\";]+)\"?/i)?.[1];
      const nomFichierParDefaut = type === 'csv' ? 'import_etudiants.csv' : 'etudiants_import.xlsx';

      return {
        blob,
        fileName: nomFichierDepuisHeader || nomFichierParDefaut
      };
    } catch (erreur) {
      console.error('Erreur lors du téléchargement du modèle d\'import:', erreur);
      throw erreur;
    }
  },
};

