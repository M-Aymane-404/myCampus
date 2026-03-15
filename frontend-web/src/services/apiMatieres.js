import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/admin/structure/matieres';

export const apiMatieres = {
    async obtenirTous() {
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
            console.error('Erreur lors de la recuperation des matieres:', erreur);
            throw erreur;
        }
    },

    async obtenirParModule(codeModule) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/module/${codeModule}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la recuperation des matieres par module:', erreur);
            throw erreur;
        }
    },

    async creer(matiere) {
        try {
            const reponse = await authenticatedFetch(BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(matiere),
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la creation de la matiere:', erreur);
            throw erreur;
        }
    },

    async modifier(codeMatiere, matiere) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/${codeMatiere}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                },
                body: JSON.stringify(matiere),
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la modification de la matiere:', erreur);
            throw erreur;
        }
    },

    async obtenirParModuleEtClasse(codeModule, codeClasse) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/module/${codeModule}/classe/${codeClasse}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!reponse.ok) {
                console.warn(`Failed to get matieres with class filter, status: ${reponse.status}`);
                const fallbackReponse = await authenticatedFetch(`${BASE_URL}/module/${codeModule}`, {
                    method: 'GET',
                    credentials: 'include'
                });

                if (!fallbackReponse.ok) {
                    throw new Error(`Erreur HTTP: ${fallbackReponse.status}`);
                }

                const fallbackData = await fallbackReponse.json();
                console.log('Fallback successful, got data without class filter');
                return fallbackData;
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la recuperation des matieres par module et classe:', erreur);
            throw erreur;
        }
    },

    async supprimer(codeMatiere) {
        try {
            const reponse = await authenticatedFetch(`${BASE_URL}/${codeMatiere}`, {
                method: 'DELETE',
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de la matiere:', erreur);
            throw erreur;
        }
    },
};
