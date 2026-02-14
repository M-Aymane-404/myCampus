import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/etudiant';

export const apiEtudiant = {
    /**
     * Fetch current student profile information
     */
    async getProfile() {
        try {
            const response = await fetch(`${BASE_URL}/moi`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la récupération du profil');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (getProfile):', error);
            throw error;
        }
    },

    /**
     * Update student profile information
     * @param {Object} profileData - { nom, prenom, dateNaissance, tel, email }
     */
    async updateProfile(profileData) {
        try {
            const response = await fetch(`${BASE_URL}/moi/modifier`, {
                method: 'PUT',
                headers: apiAuth.getAuthHeaders(),
                body: JSON.stringify(profileData),
                credentials: 'include'
            });

            if (!response.ok) {
                // Try to get error message from body if available
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
                }
                throw new Error(errorData.message || 'Erreur lors de la mise à jour du profil');
            }

            // Handle 204 No Content or empty bodies
            const contentType = response.headers.get("content-type");
            if (response.status === 204 || !contentType || !contentType.includes("application/json")) {
                return { success: true };
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (updateProfile):', error);
            throw error;
        }
    },

    /**
     * Upload profile photo
     * @param {File} file - The image file to upload
     */
    async uploadPhoto(file) {
        try {
            const formData = new FormData();
            formData.append('photo', file);

            const token = apiAuth.getToken();
            const response = await fetch(`${BASE_URL}/photo`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
                    // Note: Do not set Content-Type for FormData, the browser will set it with the boundary
                },
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                // Try to get error message from body if available
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
                }
                throw new Error(errorData.message || 'Erreur lors de l\'envoi de la photo');
            }

            return await response.json(); // Expected to return the updated user or photo URL
        } catch (error) {
            console.error('API Error (uploadPhoto):', error);
            throw error;
        }
    },

    /**
     * Fetch dashboard data (header, latest grades, attendance)
     */
    async getDashboardData() {
        try {
            const response = await fetch(`${BASE_URL}/tableau-de-bord`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la récupération des données du tableau de bord');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (getDashboardData):', error);
            throw error;
        }
    },

    /**
     * Fetch announcements for the student
     */
    async getAnnonces() {
        try {
            const response = await fetch(`${BASE_URL}/annonces`, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la récupération des annonces');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (getAnnonces):', error);
            throw error;
        }
    }
};
