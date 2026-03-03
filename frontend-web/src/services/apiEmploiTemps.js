const BASE_URL = 'http://localhost:8080/api/etudiant/emploi';
import { apiAuth } from './apiAuth';

export const apiEmploiTemps = {
    async obtenirParClasseEtSemestre(codeClasse, semestre) {
        try {
            const reponse = await fetch(`${BASE_URL}/${codeClasse}/${semestre}`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!reponse.ok) {
                // If not found or empty, return empty array instead of throwing
                if (reponse.status === 404) return [];
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            const response = await reponse.json();
            // Extract slots array from backend response
            return response.slots || response || [];
        } catch (erreur) {
            console.error('Erreur lors de la récupération de l\'emploi du temps:', erreur);
            return [];
        }
    },

    async obtenirParProfesseur(profId) {
        try {
            const reponse = await fetch(`${BASE_URL}/professeur/${profId}`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!reponse.ok) {
                if (reponse.status === 404) return [];
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la récupération de l\'emploi du temps par professeur:', erreur);
            return [];
        }
    },

    async sauvegarderSeance(seance) {
        try {
            const reponse = await fetch(`${BASE_URL}`, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...apiAuth.getAuthHeaders(),
                },
                body: JSON.stringify(seance),
                credentials: 'include'
            });

            if (!reponse.ok) {
                const erreurData = await reponse.json().catch(() => ({}));
                throw new Error(erreurData.message || `Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la sauvegarde de la séance:', erreur);
            throw erreur;
        }
    },

    async supprimerSeance(id) {
        try {
            const reponse = await fetch(`${BASE_URL}/${id}`, {
                method: 'DELETE',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de la séance:', erreur);
            throw erreur;
        }
    }
};

export default apiEmploiTemps;
