const BASE_URL = 'http://localhost:8080/api';

export const apiTableauDeBord = {
  async obtenirTableauDeBord() {
    try {
      const reponse = await fetch(`${BASE_URL}/tableau-de-bord`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      const donnees = await reponse.json();
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération du tableau de bord:', erreur);
      throw erreur;
    }
  },

  async approuverUtilisateur(idUtilisateur) {
    try {
      const reponse = await fetch(`${BASE_URL}/utilisateurs/${idUtilisateur}/approuver`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de l\'approbation de l\'utilisateur:', erreur);
      throw erreur;
    }
  },

  async rejeterUtilisateur(idUtilisateur) {
    try {
      const reponse = await fetch(`${BASE_URL}/utilisateurs/${idUtilisateur}/rejeter`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors du rejet de l\'utilisateur:', erreur);
      throw erreur;
    }
  },
};

