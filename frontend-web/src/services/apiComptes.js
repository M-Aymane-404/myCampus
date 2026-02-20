import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs';

export const apiComptes = {
    /**
     * Récupère tous les comptes utilisateurs (professeurs, étudiants, administrateurs)
     * en combinant les résultats des 3 APIs existantes
     */
    async obtenirTous() {
        try {
            // Appeler les 3 APIs en parallèle pour optimiser les performances
            const [professeurs, etudiants, administrateurs] = await Promise.all([
                fetch(`${BASE_URL}/professeurs`, {
                    method: 'GET',
                    headers: apiAuth.getAuthHeaders(),
                    credentials: 'include'
                }).then(reponse => {
                    if (!reponse.ok) throw new Error(`Erreur professeurs: ${reponse.status}`);
                    return reponse.json();
                }),

                fetch(`${BASE_URL}/etudiants`, {
                    method: 'GET',
                    headers: apiAuth.getAuthHeaders(),
                    credentials: 'include'
                }).then(reponse => {
                    if (!reponse.ok) throw new Error(`Erreur étudiants: ${reponse.status}`);
                    return reponse.json();
                }),

                fetch(`${BASE_URL}/administrateurs`, {
                    method: 'GET',
                    headers: apiAuth.getAuthHeaders(),
                    credentials: 'include'
                }).then(reponse => {
                    if (!reponse.ok) throw new Error(`Erreur administrateurs: ${reponse.status}`);
                    return reponse.json();
                })
            ]);

            // Combiner tous les utilisateurs avec leur rôle respectif
            return [
                ...professeurs.map(p => ({ ...p, role: 'Professeur' })),
                ...etudiants.map(e => ({ ...e, role: 'Étudiant' })),
                ...administrateurs.map(a => ({ ...a, role: 'Administrateur' }))
            ];
        } catch (erreur) {
            console.error('Erreur lors de la récupération des comptes:', erreur);
            throw erreur;
        }
    }
};
