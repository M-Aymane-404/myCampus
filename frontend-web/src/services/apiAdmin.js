import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/admin/utilisateurs/administrateurs';

export const apiAdmin = {
    /**
     * Get current administrator's profile information
     * Uses existing admin endpoint and filters by email
     */
    async getProfile() {
        try {
            // Get all admins and find the current one by email
            const response = await fetch(BASE_URL, {
                method: 'GET',
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la récupération des administrateurs');
            }

            const allAdmins = await response.json();
            const currentUser = apiAuth.getUser();
            const currentAdmin = allAdmins.find(admin => admin.email === currentUser.email);
            
            if (!currentAdmin) {
                throw new Error('Administrateur non trouvé');
            }

            return currentAdmin;
        } catch (error) {
            console.error('API Error (getProfile):', error);
            throw error;
        }
    },

    /**
     * Update current administrator's profile information
     * @param {Object} profileData - { nom, prenom, tel, email, fonction }
     */
    async updateProfile(profileData) {
        try {
            // Get current admin to find their PPR
            const currentAdmin = await this.getProfile();
            
            const response = await fetch(`${BASE_URL}/${currentAdmin.ppr}`, {
                method: 'PUT',
                headers: {
                    ...apiAuth.getAuthHeaders(),
                    'Content-Type': 'application/json',
                },
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

            // Get current admin to find their email
            const currentAdmin = await this.getProfile();
            
            const token = apiAuth.getToken();
            const response = await fetch(`${BASE_URL.replace('/administrateurs', '')}/${currentAdmin.email}/photo`, {
                method: 'PUT',
                headers: {
                    'Authorization': `Bearer ${token}`,
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
    }
};
