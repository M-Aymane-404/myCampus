import { apiAuth } from '../apiAuth';

const BASE_URL = 'http://localhost:8080/api/professeur';

const lireErreur = async (response, fallbackMessage) => {
    try {
        const data = await response.json();
        return (
            data?.message ||
            data?.error ||
            data?.details ||
            fallbackMessage
        );
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

const trierParNomClasse = (a, b) =>
    (a.nomClasse || a.codeClasse || '').localeCompare(
        b.nomClasse || b.codeClasse || '',
        'fr',
        { sensitivity: 'base' }
    );

export const apiAnnoncesProfesseur = {
    async listerMesAnnonces() {
        const response = await fetch(`${BASE_URL}/annonces/mes-annonces`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    'Erreur lors du chargement de vos annonces.'
                )
            );
        }

        return response.json();
    },

    async listerAnnoncesDeMesClasses() {
        const response = await fetch(`${BASE_URL}/annonces/mes-classes`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    'Erreur lors du chargement des annonces de vos classes.'
                )
            );
        }

        return response.json();
    },

    async listerAnnoncesMesClasses() {
        return apiAnnoncesProfesseur.listerAnnoncesDeMesClasses();
    },

    async listerMesProfAnnonces() {
        const response = await fetch(`${BASE_URL}/annonces/mes-prof-annonces`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    'Erreur lors du chargement des annonces professeurs.'
                )
            );
        }

        return response.json();
    },

    async listerMesClasses() {
        const response = await fetch(`${BASE_URL}/enseignements`, {
            method: 'GET',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    'Erreur lors du chargement des classes du professeur.'
                )
            );
        }

        const enseignements = await response.json();

        const classesMap = new Map();

        (Array.isArray(enseignements) ? enseignements : []).forEach((item) => {
            const codeClasse = item.codeClasse || item.classeCode;
            if (!codeClasse) return;

            if (!classesMap.has(codeClasse)) {
                classesMap.set(codeClasse, {
                    id: codeClasse,
                    codeClasse,
                    nomClasse: item.classeNom || item.nomClasse || codeClasse,
                    codeMatiere: item.codeMatiere || null,
                    matiereNom: item.matiereNom || null,
                });
            }
        });

        return Array.from(classesMap.values()).sort(trierParNomClasse);
    },

    async creerAnnonce(payload) {
        const response = await fetch(`${BASE_URL}/annonces`, {
            method: 'POST',
            headers: await getHeaders(true),
            body: JSON.stringify(payload),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    "Erreur lors de la création de l'annonce."
                )
            );
        }

        return response.json();
    },

    async publierAnnonce(id) {
        const response = await fetch(`${BASE_URL}/annonces/${id}/publier`, {
            method: 'PUT',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    "Erreur lors de la publication de l'annonce."
                )
            );
        }

        return response.json();
    },

    async depublierAnnonce(id) {
        const response = await fetch(`${BASE_URL}/annonces/${id}/depublier`, {
            method: 'PUT',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    "Erreur lors de la dépublication de l'annonce."
                )
            );
        }

        return response.json();
    },

    async supprimerAnnonce(id) {
        const response = await fetch(`${BASE_URL}/annonces/${id}`, {
            method: 'DELETE',
            headers: await getHeaders(),
            credentials: 'include',
        });

        if (!response.ok) {
            throw new Error(
                await lireErreur(
                    response,
                    "Erreur lors de la suppression de l'annonce."
                )
            );
        }

        return true;
    },
};

export default apiAnnoncesProfesseur;
