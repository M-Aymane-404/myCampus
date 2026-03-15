import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/admin/tableau-de-bord';

export const apiTableauDeBord = {
  async obtenirTableauDeBord() {
    try {
      const reponse = await authenticatedFetch(BASE_URL, {
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
            messageErreur += `: ${texteErreur}`;
          }
        }
        throw new Error(messageErreur);
      }

      return await reponse.json();
    } catch (erreur) {
      console.error('Erreur lors de la recuperation du tableau de bord:', erreur);
      throw erreur;
    }
  },
};
