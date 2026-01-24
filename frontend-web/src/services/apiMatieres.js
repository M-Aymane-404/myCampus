const BASE_URL = 'http://localhost:8080/api/admin/structure/matieres';
import { apiAuth } from './apiAuth';

export const apiMatieres = {
    async obtenirTous() {
        try {
            const reponse = await fetch(BASE_URL, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la récupération des matières:', erreur);
            throw erreur;
        }
    },

    async obtenirParModule(codeModule) {
        try {
            const reponse = await fetch(`${BASE_URL}/module/${codeModule}`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la récupération des matières par module:', erreur);
            throw erreur;
        }
    },

    async creer(matiere) {
        try {
            const reponse = await fetch(BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...apiAuth.getAuthHeaders(),
                },
                body: JSON.stringify(matiere),
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la création de la matière:', erreur);
            throw erreur;
        }
    },

    async modifier(codeMatiere, matiere) {
        try {
            const reponse = await fetch(`${BASE_URL}/${codeMatiere}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...apiAuth.getAuthHeaders(),
                },
                body: JSON.stringify(matiere),
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la modification de la matière:', erreur);
            throw erreur;
        }
    },

    async supprimer(codeMatiere) {
        try {
            const reponse = await fetch(`${BASE_URL}/${codeMatiere}`, {
                method: 'DELETE',
                headers: apiAuth.getAuthHeaders(),
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de la matière:', erreur);
            throw erreur;
        }
    },
};
