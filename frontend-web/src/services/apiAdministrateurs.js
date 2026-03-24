const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/administrateurs';
import { apiAuth } from './apiAuth';

const buildErrorMessage = async (reponse) => {
    let messageErreur = `Erreur HTTP: ${reponse.status}`;
    try {
        const erreurData = await reponse.json();

        if (erreurData?.details && typeof erreurData.details === 'object') {
            const detailsMessages = Object.entries(erreurData.details)
                .map(([field, message]) => `${field}: ${message}`)
                .join('\n');
            return `Erreur de validation:\n${detailsMessages}`;
        }

        if (erreurData?.message) return erreurData.message;
        if (erreurData?.error) return erreurData.error;
    } catch (e) {
        try {
            const texteErreur = await reponse.text();
            if (texteErreur) {
                messageErreur += ': ' + texteErreur;
            }
        } catch {
            // ignore
        }
    }

    return messageErreur;
};

export const apiAdministrateurs = {
    async obtenirTous() {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(BASE_URL, {
                method: 'GET',
                headers: {
                    'Content-Type': 'application/json',
                    ...headers,
                },
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la récupération des administrateurs:', erreur);
            throw erreur;
        }
    },

    async creer(administrateur) {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(BASE_URL, {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json',
                    ...headers,
                },
                body: JSON.stringify(administrateur),
                credentials: 'include'
            });
            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la création de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async modifier(id, administrateur) {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(`${BASE_URL}/${id}`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json',
                    ...headers,
                },
                body: JSON.stringify(administrateur),
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return await reponse.json();
        } catch (erreur) {
            console.error('Erreur lors de la modification de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async supprimer(id) {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(`${BASE_URL}/${id}`, {
                method: 'DELETE',
                headers: headers,
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la suppression de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async activer(id) {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(`${BASE_URL}/${id}/activer`, {
                method: 'PATCH',
                headers: headers,
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de l\'activation de l\'administrateur:', erreur);
            throw erreur;
        }
    },

    async desactiver(id) {
        try {
            const headers = await apiAuth.getAuthHeadersWithRefresh();
            const reponse = await fetch(`${BASE_URL}/${id}/desactiver`, {
                method: 'PATCH',
                headers: headers,
                credentials: 'include'
            });

            if (!reponse.ok) {
                throw new Error(await buildErrorMessage(reponse));
            }

            return true;
        } catch (erreur) {
            console.error('Erreur lors de la désactivation de l\'administrateur:', erreur);
            throw erreur;
        }
    }
};
