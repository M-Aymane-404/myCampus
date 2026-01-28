const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/administrateurs';
import { apiAuth } from './apiAuth';

export const apiAdministrateurs = {
    async obtenirTous() {
        try {
            const reponse = await fetch(BASE_URL, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                },
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la récupération des administrateurs:', erreur);
            throw erreur;
        }
    },

    async creer(administrateur) {
        try {
            const headers = {
                'Content-Type': 'application/json',
                ...apiAuth.getAuthHeaders()
            };

            const reponse = await fetch(BASE_URL, {
                method: 'POST',
                headers: headers,
                body: JSON.stringify(administrateur),
            });

            if (!reponse.ok) {
                let messageErreur = `Erreur HTTP: ${reponse.status}`;
                try {
                    const erreurData = await reponse.json();
                    if (erreurData.message) messageErreur = erreurData.message;
                } catch (e) { }
                throw new Error(messageErreur);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la création de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async modifier(id, administrateur) {
        try {
            const headers = {
                'Content-Type': 'application/json',
                ...apiAuth.getAuthHeaders()
            };

            const reponse = await fetch(`${BASE_URL}/${id}`, {
                method: 'PUT',
                headers: headers,
                body: JSON.stringify(administrateur),
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la modification de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async supprimer(id) {
        try {
            const headers = {
                'Content-Type': 'application/json',
                ...apiAuth.getAuthHeaders()
            };

            const reponse = await fetch(`${BASE_URL}/${id}`, {
                method: 'DELETE',
                headers: headers,
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async activer(id) {
        try {
            const headers = {
                'Content-Type': 'application/json',
                ...apiAuth.getAuthHeaders()
            };

            const reponse = await fetch(`${BASE_URL}/${id}/activer`, {
                method: 'PATCH',
                headers: headers,
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de l\'activation de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async desactiver(id) {
        try {
            const headers = {
                'Content-Type': 'application/json',
                ...apiAuth.getAuthHeaders()
            };

            const reponse = await fetch(`${BASE_URL}/${id}/desactiver`, {
                method: 'PATCH',
                headers: headers,
            });

            if (!reponse.ok) {
                throw new Error(`Erreur HTTP: ${reponse.status}`);
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la désactivation de l\'administrateur:', erreur);
            throw erreur;
        }
    }
};
