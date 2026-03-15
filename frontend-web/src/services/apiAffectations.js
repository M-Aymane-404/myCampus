import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/admin/structure/affectations';

export const apiAffectations = {
    async obtenirToutes() {
        try {
            const reponse = await authenticatedFetch(BASE_URL, {
                method: 'GET',
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la recuperation des affectations:', erreur);
            throw erreur;
        }
    },

    async obtenirParClasse(classeId) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/classe/${classeId}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!reponse.ok) {
                console.error(`GET Affectations failed for class ${classeId}: ${reponse.status}`);
                return [];
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la recuperation des affectations par classe:', erreur);
            return [];
        }
    },

    async assigner(affectation) {
        try {
            const reponse = await authenticatedFetch(BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(affectation),
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de l\'affectation:', erreur);
            throw erreur;
        }
    },

    async modifier(id, affectation) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(affectation),
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la modification de l\'affectation:', erreur);
            throw erreur;
        }
    },

    async supprimer(id) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/${id}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de l\'affectation:', erreur);
            throw erreur;
        }
    }
};
