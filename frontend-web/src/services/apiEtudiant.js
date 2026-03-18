import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/etudiant';

export const apiEtudiant = {
    /**
     * Fetch current student profile information
     */
    async getProfile() {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la recuperation du profil');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (getProfile):', error);
            throw error;
        }
    },

    /**
     * Update current student own profile
     * @param {Object} profileData - { tel } or { ancienPassword, nouveauPassword }
     */
    async updateProfile(profileData) {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi/modifier`, {
                method: 'PUT',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(profileData),
                credentials: 'include'
            });

            if (!response.ok) {
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
                }
                throw new Error(errorData.message || 'Erreur lors de la mise a jour du profil');
            }

            const contentType = response.headers.get('content-type');
            if (response.status === 204 || !contentType || !contentType.includes('application/json')) {
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

            const response = await authenticatedFetch(`${BASE_URL}/photo`, {
                method: 'PUT',
                body: formData,
                credentials: 'include'
            });

            if (!response.ok) {
                let errorData;
                try {
                    errorData = await response.json();
                } catch (e) {
                    errorData = { message: `Erreur ${response.status}: ${response.statusText}` };
                }
                throw new Error(errorData.message || 'Erreur lors de l\'envoi de la photo');
            }

            return await response.json();
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
            const response = await authenticatedFetch(`${BASE_URL}/tableau-de-bord`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la recuperation des donnees du tableau de bord');
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
            const response = await authenticatedFetch(`${BASE_URL}/annonces`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la recuperation des annonces');
            }

            return await response.json();
        } catch (error) {
            console.error('API Error (getAnnonces):', error);
            throw error;
        }
    },

    /**
     * Fetch student's timetable for current semester
     */
    async getTimetable(semestre) {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/emploi/${semestre}`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la recuperation de l\'emploi du temps');
            }

            const data = await response.json();
            return data.slots || [];
        } catch (error) {
            console.error('API Error (getTimetable):', error);
            throw error;
        }
    },

    /**
     * Fetch next session for the student
     */
    async getNextSession() {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/emploi/prochaine`, {
                method: 'GET',
                credentials: 'include'
            });

            if (!response.ok) {
                throw new Error('Erreur lors de la recuperation de la prochaine seance');
            }

            const data = await response.json();
            return data;
        } catch (error) {
            console.error('API Error (getNextSession):', error);
            throw error;
        }
    },
};
