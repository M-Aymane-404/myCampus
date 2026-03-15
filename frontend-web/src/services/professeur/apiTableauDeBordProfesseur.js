import apiProfesseur from './apiProfesseur';

export const apiTableauDeBordProfesseur = {
    async obtenirTableauDeBord() {
        return apiProfesseur.fetch('/tableau-de-bord', { method: 'GET' });
    },

    async obtenirProchaineSeance() {
        return apiProfesseur.fetch('/prochaine', { method: 'GET' });
    },
};

export default apiTableauDeBordProfesseur;