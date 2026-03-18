import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api';

const lireErreur = async (response, fallbackMessage) => {
  try {
    const data = await response.json();
    return data?.message || data?.error || data?.details || fallbackMessage;
  } catch {
    try {
      const text = await response.text();
      return text || fallbackMessage;
    } catch {
      return fallbackMessage;
    }
  }
};

const getHeaders = async (json = false) => {
  const headers = await apiAuth.getAuthHeadersWithRefresh();

  if (json) {
    return {
      ...headers,
      'Content-Type': 'application/json',
    };
  }

  return headers;
};

const extraireNomFichier = (contentDisposition, fallbackName) => {
  if (!contentDisposition) {
    return fallbackName;
  }

  const match = contentDisposition.match(/filename="?([^"]+)"?/i);
  return match?.[1] || fallbackName;
};

const declencherTelechargement = async (response, fallbackName) => {
  const blob = await response.blob();
  const url = window.URL.createObjectURL(blob);
  const lien = document.createElement('a');
  lien.href = url;
  lien.download = extraireNomFichier(
    response.headers.get('content-disposition'),
    fallbackName
  );
  document.body.appendChild(lien);
  lien.click();
  lien.remove();
  window.URL.revokeObjectURL(url);
};

export const apiAnnonces = {
  etudiant: {
    async obtenirToutes() {
      const response = await fetch(`${BASE_URL}/etudiant/annonces`, {
        method: 'GET',
        headers: await getHeaders(),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(
          await lireErreur(response, 'Erreur lors du chargement des annonces.')
        );
      }

      return response.json();
    },

    async obtenirParId(id) {
      const response = await fetch(`${BASE_URL}/etudiant/annonces/${id}`, {
        method: 'GET',
        headers: await getHeaders(),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(
          await lireErreur(response, "Erreur lors du chargement de l'annonce.")
        );
      }

      return response.json();
    },

    async listerRessources(annonceId) {
      const response = await fetch(`${BASE_URL}/etudiant/annonces/${annonceId}/ressources`, {
        method: 'GET',
        headers: await getHeaders(),
        credentials: 'include',
      });

      if (!response.ok) {
        throw new Error(
          await lireErreur(response, 'Erreur lors du chargement des ressources.')
        );
      }

      return response.json();
    },

    async telechargerRessource(ressourceId) {
      const response = await fetch(
        `${BASE_URL}/etudiant/annonces/ressources/${ressourceId}/telecharger`,
        {
          method: 'GET',
          headers: await getHeaders(),
          credentials: 'include',
        }
      );

      if (!response.ok) {
        throw new Error(
          await lireErreur(response, 'Erreur lors du telechargement de la ressource.')
        );
      }

      await declencherTelechargement(response, 'ressource-annonce');
      return true;
    },
  },

  async obtenirToutes() {
    const response = await fetch(`${BASE_URL}/admin/annonces`, {
      method: 'GET',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, 'Erreur lors du chargement des annonces.')
      );
    }

    return response.json();
  },

  async obtenirNombreNonLus() {
    const response = await fetch(`${BASE_URL}/unread/count`, {
      method: 'GET',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      return 0;
    }

    const data = await response.json();
    return data.count || 0;
  },

  async creer(annonce) {
    const response = await fetch(`${BASE_URL}/admin/annonces`, {
      method: 'POST',
      headers: await getHeaders(true),
      body: JSON.stringify(annonce),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, "Erreur lors de la creation de l'annonce.")
      );
    }

    return response.json();
  },

  async supprimer(id) {
    const response = await fetch(`${BASE_URL}/admin/annonces/${id}`, {
      method: 'DELETE',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, "Erreur lors de la suppression de l'annonce.")
      );
    }

    return true;
  },

  async publier(id) {
    const response = await fetch(`${BASE_URL}/admin/annonces/${id}/publier`, {
      method: 'PUT',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, "Erreur lors de la publication de l'annonce.")
      );
    }

    return response.json();
  },

  async depublier(id) {
    const response = await fetch(`${BASE_URL}/admin/annonces/${id}/depublier`, {
      method: 'PUT',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, "Erreur lors de la depublication de l'annonce.")
      );
    }

    return response.json();
  },

  async ajouterRessource(annonceId, fichier) {
    const formData = new FormData();
    formData.append('fichier', fichier);

    const response = await fetch(`${BASE_URL}/admin/annonces/${annonceId}/ressources`, {
      method: 'POST',
      headers: await getHeaders(),
      body: formData,
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, "Erreur lors de l'envoi de la ressource.")
      );
    }

    return response.json();
  },

  async listerRessources(annonceId) {
    const response = await fetch(`${BASE_URL}/admin/annonces/${annonceId}/ressources`, {
      method: 'GET',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, 'Erreur lors du chargement des ressources.')
      );
    }

    return response.json();
  },

  async telechargerRessource(ressourceId) {
    const response = await fetch(
      `${BASE_URL}/admin/annonces/ressources/${ressourceId}/telecharger`,
      {
        method: 'GET',
        headers: await getHeaders(),
        credentials: 'include',
      }
    );

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, 'Erreur lors du telechargement de la ressource.')
      );
    }

    await declencherTelechargement(response, 'ressource-annonce');
    return true;
  },

  async marquerCommeLu(id) {
    const response = await fetch(`${BASE_URL}/${id}/read`, {
      method: 'POST',
      headers: await getHeaders(),
      credentials: 'include',
    });

    if (!response.ok) {
      throw new Error(
        await lireErreur(response, 'Erreur lors du marquage comme lu.')
      );
    }

    return true;
  },
};

export default apiAnnonces;
