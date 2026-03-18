import { apiAuth } from '../apiAuth';

const BASE_URL = 'http://localhost:8080/api/professeur/examens';

const lireErreur = async (response, fallback) => {
    try {
        const data = await response.json();
        return data?.message || data?.error || fallback;
    } catch {
        try {
            const text = await response.text();
            return text || fallback;
        } catch {
            return fallback;
        }
    }
};

const getHeaders = async (json = false) => {
    const headers = await apiAuth.getAuthHeadersWithRefresh();
    return json ? { ...headers, 'Content-Type': 'application/json' } : headers;
};

const lireNomFichier = (response, nomParDefaut) => {
    const disposition = response.headers.get('content-disposition') || '';
    const match = disposition.match(/filename="?([^"]+)"?/);
    return match ? match[1] : nomParDefaut;
};

const telechargerBlob = async (blob, nomFichier) => {
    const url = window.URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = nomFichier;
    document.body.appendChild(a);
    a.click();
    a.remove();
    window.URL.revokeObjectURL(url);
};

const apiExamensProfesseur = {
    async chargerFiltres() {
        const response = await fetch(`${BASE_URL}/filtres`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger les filtres examens.'));
        }

        return response.json();
    },

    async listerExamens(codeClasse, codeMatiere) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/matieres/${codeMatiere}`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger les examens.'));
        }

        return response.json();
    },

    async creerExamen(codeClasse, codeMatiere, payload) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/matieres/${codeMatiere}`, {
            method: 'POST',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, "Impossible de creer l'examen."));
        }

        return response.json();
    },

    async modifierExamen(examenId, payload) {
        const response = await fetch(`${BASE_URL}/${examenId}`, {
            method: 'PUT',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, "Impossible de modifier l'examen."));
        }

        return response.json();
    },

    async supprimerExamen(examenId) {
        const response = await fetch(`${BASE_URL}/${examenId}`, {
            method: 'DELETE',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, "Impossible de supprimer l'examen."));
        }

        return true;
    },

    async deposerCorrige(examenId, fichier) {
        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const formData = new FormData();
        formData.append('fichier', fichier);

        const response = await fetch(`${BASE_URL}/${examenId}/corrige`, {
            method: 'POST',
            headers,
            body: formData,
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de deposer le corrige.'));
        }

        return response.json();
    },

    async supprimerCorrige(examenId) {
        const response = await fetch(`${BASE_URL}/${examenId}/corrige`, {
            method: 'DELETE',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de supprimer le corrige.'));
        }

        return true;
    },

    async obtenirCorrigeBlob(examenId) {
        const response = await fetch(`${BASE_URL}/${examenId}/corrige`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de telecharger le corrige.'));
        }

        const blob = await response.blob();
        const filename = lireNomFichier(response, `corrige_examen_${examenId}.pdf`);
        return { blob, filename };
    },

    async telechargerCorrige(examenId) {
        const { blob, filename } = await this.obtenirCorrigeBlob(examenId);
        await telechargerBlob(blob, filename);
        return { blob, filename };
    },
};

export default apiExamensProfesseur;
