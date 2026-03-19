import apiProfesseur from './apiProfesseur';

export const apiPresencesProfesseur = {
    async chargerFeuille(seanceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences`, { method: 'GET' });
    },

    // Alias saisirPresence (utilisé par GestionPresenceProfesseur)
    async saisirPresence(seanceId, payload) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },

    // Ancien nom gardé pour compatibilité
    async enregistrerLigne(seanceId, payload) {
        return this.saisirPresence(seanceId, payload);
    },

    async validerJustificatif(seanceId, presenceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences/${presenceId}/valider-justificatif`, {
            method: 'PATCH',
        });
    },

    async refuserJustificatif(seanceId, presenceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences/${presenceId}/refuser-justificatif`, {
            method: 'PATCH',
        });
    },

    async telechargerJustificatif(seanceId, presenceId) {
        return apiProfesseur.fetchBlob(`/seances/${seanceId}/presences/${presenceId}/justificatif`, {
            method: 'GET',
        });
    },
};

export default apiPresencesProfesseur;
