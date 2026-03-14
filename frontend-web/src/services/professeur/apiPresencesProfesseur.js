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
};

export default apiPresencesProfesseur;