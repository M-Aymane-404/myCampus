import apiProfesseur from './apiProfesseur';
import { apiAuth } from '../apiAuth';

const BASE_URL = 'http://localhost:8080/api/professeur';

const isNonEmptyString = (value) => typeof value === 'string' && value.trim().length > 0;

const dedupe = (items) => [...new Set(items)];

const collectStrings = (value, acc = []) => {
    if (value == null) return acc;
    if (isNonEmptyString(value)) {
        acc.push(value.trim());
        return acc;
    }
    if (Array.isArray(value)) {
        value.forEach((item) => collectStrings(item, acc));
        return acc;
    }
    if (typeof value === 'object') {
        Object.values(value).forEach((item) => collectStrings(item, acc));
    }
    return acc;
};

const extraireMessage = (data) => {
    if (data == null) return '';
    if (isNonEmptyString(data)) return data.trim();

    if (Array.isArray(data)) {
        const messages = dedupe(collectStrings(data));
        return messages.join(' | ');
    }

    if (typeof data === 'object') {
        const knownKeys = [
            'message',
            'detail',
            'error_description',
            'errorDescription',
            'error',
            'title',
            'reason',
        ];

        for (const key of knownKeys) {
            if (data[key] == null) continue;
            const nested = extraireMessage(data[key]);
            if (isNonEmptyString(nested)) return nested;
        }

        const errorsKeys = ['errors', 'violations', 'fieldErrors'];
        for (const key of errorsKeys) {
            if (data[key] == null) continue;
            const messages = dedupe(collectStrings(data[key]));
            if (messages.length > 0) return messages.join(' | ');
        }

        const anyStrings = dedupe(collectStrings(data));
        return anyStrings.join(' | ');
    }

    return '';
};

const lireErreur = async (response, fallback) => {
    let data = null;

    try {
        data = await response.clone().json();
    } catch {
        data = null;
    }

    const fromJson = extraireMessage(data);
    if (isNonEmptyString(fromJson)) return fromJson;

    try {
        const text = (await response.text())?.trim();
        if (isNonEmptyString(text)) return text;
    } catch {
        // ignore
    }

    return fallback;
};

const getHeaders = async (json = false) => {
    const headers = await apiAuth.getAuthHeadersWithRefresh();
    return json ? { ...headers, 'Content-Type': 'application/json' } : headers;
};

const apiSeancesProfesseur = {
    async listerEnseignements() {
        const response = await fetch(`${BASE_URL}/enseignements`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger les enseignements.'));
        }

        return response.json();
    },

    async rechercherSeances(payload) {
        const response = await fetch(`${BASE_URL}/seances/recherche`, {
            method: 'POST',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger les séances.'));
        }

        return response.json();
    },

    async obtenirSeance(seanceId) {
        const response = await fetch(`${BASE_URL}/seances/${seanceId}`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger la séance.'));
        }

        return response.json();
    },

    async creerSeance(payload) {
        const response = await fetch(`${BASE_URL}/seances`, {
            method: 'POST',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de créer la séance.'));
        }

        return response.json();
    },

    async modifierSeance(seanceId, payload) {
        const response = await fetch(`${BASE_URL}/seances/${seanceId}`, {
            method: 'PUT',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de modifier la séance.'));
        }

        return response.json();
    },

    async supprimerSeance(seanceId) {
        const response = await fetch(`${BASE_URL}/seances/${seanceId}`, {
            method: 'DELETE',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de supprimer la séance.'));
        }

        return true;
    },

    async listerFichiersDeSeance(seanceId) {
        const response = await fetch(`${BASE_URL}/seances/${seanceId}/fichiers`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger les fichiers.'));
        }

        return response.json();
    },

    async deposerFichierPourSeance(seanceId, fichier) {
        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const formData = new FormData();
        formData.append('fichier', fichier);

        const response = await fetch(`${BASE_URL}/seances/${seanceId}/fichiers`, {
            method: 'POST',
            headers,
            body: formData,
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de déposer le fichier.'));
        }

        return response.json();
    },

    async deposerFichier(seanceId, fichier) {
        return this.deposerFichierPourSeance(seanceId, fichier);
    },

    async supprimerFichierDeSeance(fichierId) {
        const response = await fetch(`${BASE_URL}/fichiers-seance/${fichierId}`, {
            method: 'DELETE',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de supprimer le fichier.'));
        }

        return true;
    },

    async supprimerFichier(fichierId) {
        return this.supprimerFichierDeSeance(fichierId);
    },

    async telechargerFichierBlob(fichierId) {
        return apiProfesseur.fetchBlob(`/fichiers-seance/${fichierId}/telecharger`, {
            method: 'GET',
        });
    },

    async telechargerFichierDeSeance(fichierId) {
        const { blob, filename } = await this.telechargerFichierBlob(fichierId);
        const url = window.URL.createObjectURL(blob);
        const a = document.createElement('a');
        a.href = url;
        a.download = filename || `fichier_seance_${fichierId}`;
        document.body.appendChild(a);
        a.click();
        a.remove();
        window.URL.revokeObjectURL(url);
        return true;
    },

    async telechargerFichier(fichierId) {
        return this.telechargerFichierDeSeance(fichierId);
    },
};

export default apiSeancesProfesseur;
