const BASE_URL = 'http://localhost:8080/api/admin/structure';

export const apiStructure = {
  async obtenirToutesFilieres() {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          messageErreur = erreurData.message || erreurData.error || messageErreur;
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const donnees = await reponse.json();
      console.log('API Structure - Filieres reçues:', donnees);
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération des filières:', erreur);
      throw erreur;
    }
  },

  async obtenirToutesClasses() {
    try {
      const reponse = await fetch(`${BASE_URL}/classes`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          messageErreur = erreurData.message || erreurData.error || messageErreur;
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const donnees = await reponse.json();
      console.log('API Structure - Classes reçues:', donnees);
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération des classes:', erreur);
      throw erreur;
    }
  },

  async obtenirClassesParFiliere(codeFiliere) {
    try {
      const reponse = await fetch(`${BASE_URL}/classes/filiere/${codeFiliere}`, {
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
      console.error('Erreur lors de la récupération des classes par filière:', erreur);
      throw erreur;
    }
  },

  async obtenirTousDepartements() {
    try {
      const reponse = await fetch(`${BASE_URL}/departements`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          messageErreur = erreurData.message || erreurData.error || messageErreur;
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const donnees = await reponse.json();
      console.log('API Structure - Départements reçus:', donnees);
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération des départements:', erreur);
      throw erreur;
    }
  },

  async obtenirToutesMatieres() {
    try {
      const reponse = await fetch(`${BASE_URL}/matieres`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          messageErreur = erreurData.message || erreurData.error || messageErreur;
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const donnees = await reponse.json();
      console.log('API Structure - Matières reçues:', donnees);
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération des matières:', erreur);
      throw erreur;
    }
  },

  async obtenirTousModules() {
    try {
      const reponse = await fetch(`${BASE_URL}/modules`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
        },
      });

      if (!reponse.ok) {
        let messageErreur = `Erreur HTTP: ${reponse.status}`;
        try {
          const erreurData = await reponse.json();
          messageErreur = erreurData.message || erreurData.error || messageErreur;
        } catch (e) {
          const texteErreur = await reponse.text();
          if (texteErreur) {
            messageErreur += ': ' + texteErreur;
          }
        }
        throw new Error(messageErreur);
      }

      const donnees = await reponse.json();
      console.log('API Structure - Modules reçus:', donnees);
      return donnees;
    } catch (erreur) {
      console.error('Erreur lors de la récupération des modules:', erreur);
      throw erreur;
    }
  },
};
