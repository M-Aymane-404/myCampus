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
    }
};
