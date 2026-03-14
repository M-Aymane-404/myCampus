import apiProfesseur from './apiProfesseur';

export const apiSeancesProfesseur = {
    async rechercherSeances(payload) {
        return apiProfesseur.fetch('/seances/recherche', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },

    async obtenirSeance(seanceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}`, {
            method: 'GET',
        });
    },

    async creerSeance(payload) {
        return apiProfesseur.fetch('/seances', {
            method: 'POST',
            body: JSON.stringify(payload),
        });
    },

    async modifierSeance(seanceId, payload) {
        return apiProfesseur.fetch(`/seances/${seanceId}`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },

    async supprimerSeance(seanceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}`, {
            method: 'DELETE',
        });
    },

    async listerFichiers(seanceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}/fichiers`, {
            method: 'GET',
        });
    },

    async uploaderFichier(seanceId, fichier) {
        const formData = new FormData();
        formData.append('fichier', fichier);

        return apiProfesseur.fetch(`/seances/${seanceId}/fichiers`, {
            method: 'POST',
            body: formData,
        });
    },

    async supprimerFichier(fichierId) {
        return apiProfesseur.fetch(`/fichiers-seance/${fichierId}`, {
            method: 'DELETE',
        });
    },

    async telechargerFichier(fichierId) {
        return apiProfesseur.fetchBlob(`/fichiers-seance/${fichierId}/telecharger`, {
            method: 'GET',
        });
    },
};

export default apiSeancesProfesseur;