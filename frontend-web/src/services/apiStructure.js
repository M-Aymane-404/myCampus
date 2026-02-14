const BASE_URL = 'http://localhost:8080/api/admin/structure';
import { apiAuth } from './apiAuth';

export const apiStructure = {
  async obtenirToutesFilieres() {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
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

  // Department CRUD Operations
  async creerDepartement(departement) {
    try {
      const reponse = await fetch(`${BASE_URL}/departements`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(departement),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la création du département:', erreur);
      throw erreur;
    }
  },

  async modifierDepartement(codeDepartement, departement) {
    try {
      const reponse = await fetch(`${BASE_URL}/departements/${codeDepartement}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(departement),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification du département:', erreur);
      throw erreur;
    }
  },

  async supprimerDepartement(codeDepartement) {
    try {
      const reponse = await fetch(`${BASE_URL}/departements/${codeDepartement}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression du département:', erreur);
      throw erreur;
    }
  },

  // Filière CRUD Operations
  async creerFiliere(filiere) {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(filiere),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la création de la filière:', erreur);
      throw erreur;
    }
  },

  async modifierFiliere(codeFiliere, filiere) {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres/${codeFiliere}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(filiere),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification de la filière:', erreur);
      throw erreur;
    }
  },

  async supprimerFiliere(codeFiliere) {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres/${codeFiliere}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression de la filière:', erreur);
      throw erreur;
    }
  },

  async obtenirFilieresParDepartement(codeDepartement) {
    try {
      const reponse = await fetch(`${BASE_URL}/filieres/departement/${codeDepartement}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération des filières par département:', erreur);
      throw erreur;
    }
  },

  // Classes CRUD Operations
  async creerClasse(classe) {
    try {
      const reponse = await fetch(`${BASE_URL}/classes`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(classe),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la création de la classe:', erreur);
      throw erreur;
    }
  },

  async modifierClasse(codeClasse, classe) {
    try {
      const reponse = await fetch(`${BASE_URL}/classes/${codeClasse}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(classe),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification de la classe:', erreur);
      throw erreur;
    }
  },

  async supprimerClasse(codeClasse) {
    try {
      const reponse = await fetch(`${BASE_URL}/classes/${codeClasse}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression de la classe:', erreur);
      throw erreur;
    }
  },

  // Modules CRUD Operations
  async creerModule(module) {
    try {
      const reponse = await fetch(`${BASE_URL}/modules`, {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(module),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la création du module:', erreur);
      throw erreur;
    }
  },

  async modifierModule(codeModule, module) {
    try {
      const reponse = await fetch(`${BASE_URL}/modules/${codeModule}`, {
        method: 'PUT',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        body: JSON.stringify(module),
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la modification du module:', erreur);
      throw erreur;
    }
  },

  async supprimerModule(codeModule) {
    try {
      const reponse = await fetch(`${BASE_URL}/modules/${codeModule}`, {
        method: 'DELETE',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        const erreurData = await reponse.json().catch(() => ({}));
        throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
      }

      return true;
    } catch (erreur) {
      console.error('Erreur lors de la suppression du module:', erreur);
      throw erreur;
    }
  },

  async obtenirModulesParClasse(codeClasse) {
    try {
      const reponse = await fetch(`${BASE_URL}/modules/classe/${codeClasse}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération des modules par classe:', erreur);
      throw erreur;
    }
  },

  async obtenirModulesParFiliere(filiereId) {
    try {
      const reponse = await fetch(`${BASE_URL}/modules/filiere/${filiereId}`, {
        method: 'GET',
        headers: {
          'Content-Type': 'application/json',
          ...apiAuth.getAuthHeaders(),
        },
        credentials: 'include'
      });

      if (!reponse.ok) {
        throw new Error(`Erreur HTTP: ${reponse.status}`);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la récupération des modules par filière:', erreur);
      throw erreur;
    }
  },
};
