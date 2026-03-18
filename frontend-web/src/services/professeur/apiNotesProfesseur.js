import { apiAuth } from '../apiAuth';

const BASE_URL = 'http://localhost:8080/api/professeur/notes';

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

const apiNotesProfesseur = {
    async chargerFeuille(codeClasse, examenId) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/examens/${examenId}/feuille`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de charger la feuille de notes.'));
        }

        return response.json();
    },

    async autosaveNote(codeClasse, examenId, payload) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/examens/${examenId}/resultats`, {
            method: 'PUT',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, "Impossible d'enregistrer la note."));
        }

        return response.json();
    },

    async publierNotes(codeClasse, examenId) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/examens/${examenId}/publier`, {
            method: 'POST',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de publier les resultats.'));
        }

        return response.json();
    },

    async deposerCopieEtudiant(codeClasse, examenId, etudiantId, fichier) {
        const headers = await apiAuth.getAuthHeadersWithRefresh();
        const formData = new FormData();
        formData.append('fichier', fichier);

        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/examens/${examenId}/etudiants/${etudiantId}/copie`, {
            method: 'POST',
            headers,
            body: formData,
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de deposer la copie corrigee.'));
        }

        return response.json();
    },

    async obtenirCopieEtudiantBlob(codeClasse, examenId, etudiantId) {
        const response = await fetch(`${BASE_URL}/classes/${codeClasse}/examens/${examenId}/etudiants/${etudiantId}/copie`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(await lireErreur(response, 'Impossible de telecharger la copie corrigee.'));
        }

        const blob = await response.blob();
        const filename = lireNomFichier(response, `copie_corrigee_examen_${examenId}.pdf`);
        return { blob, filename };
    },

    async telechargerCopieEtudiant(codeClasse, examenId, etudiantId) {
        const { blob, filename } = await this.obtenirCopieEtudiantBlob(codeClasse, examenId, etudiantId);
        await telechargerBlob(blob, filename);
        return { blob, filename };
    },

    async obtenirFeuille(codeClasse, examenId) {
        return this.chargerFeuille(codeClasse, examenId);
    },

    async enregistrerNote(codeClasse, examenId, payload) {
        return this.autosaveNote(codeClasse, examenId, payload);
    },
};

export default apiNotesProfesseur;
