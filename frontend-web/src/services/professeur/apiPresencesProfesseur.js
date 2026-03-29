import apiProfesseur from './apiProfesseur';
import { buildJustificationRecordFromApi, getSessionStartDate } from '../../utils/presenceProfesseur';

const buildQueryString = (filters = {}) => {
    const params = new URLSearchParams();

    Object.entries(filters).forEach(([key, value]) => {
        if (value === undefined || value === null || value === '' || value === 'TOUS' || value === 'TOUTES' || value === 'ALL') {
            return;
        }

        params.set(key, String(value));
    });

    const queryString = params.toString();
    return queryString ? `?${queryString}` : '';
};

export const apiPresencesProfesseur = {
    async chargerFeuille(seanceId) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences`, { method: 'GET' });
    },

    async saisirPresence(seanceId, payload) {
        return apiProfesseur.fetch(`/seances/${seanceId}/presences`, {
            method: 'PUT',
            body: JSON.stringify(payload),
        });
    },

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

    async listerJustificatifsProfesseur(filters = {}) {
        const queryString = buildQueryString(filters);
        const response = await apiProfesseur.fetch(`/justificatifs${queryString}`, {
            method: 'GET',
        });

        return (Array.isArray(response) ? response : [])
            .map((item) => buildJustificationRecordFromApi(item))
            .filter(Boolean)
            .sort((left, right) => {
                const leftStatus = left?.status === 'EN_ATTENTE' ? 0 : left?.status === 'VALIDE' ? 1 : 2;
                const rightStatus = right?.status === 'EN_ATTENTE' ? 0 : right?.status === 'VALIDE' ? 1 : 2;

                if (leftStatus !== rightStatus) {
                    return leftStatus - rightStatus;
                }

                const leftDate = getSessionStartDate(left?.seance)?.getTime() || 0;
                const rightDate = getSessionStartDate(right?.seance)?.getTime() || 0;
                return rightDate - leftDate;
            });
    },
};

export default apiPresencesProfesseur;
