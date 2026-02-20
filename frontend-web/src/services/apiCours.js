import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/etudiant';

export const apiCours = {
    // Hardcoded semesters based on backend Enum
    getSemesters() {
        return ['S1', 'S2', 'S3', 'S4', 'S5', 'S6'];
    },

    async getStudentProfile() {
        try {
            const response = await fetch(`${BASE_URL}/moi`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération du profil');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getStudentModules(semester = null) {
        try {
            let url = `${BASE_URL}/moi/modules`;
            if (semester && semester !== 'TOUS') {
                url += `?semestre=${semester}`;
            }
            const response = await fetch(url, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des modules');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getMatieres(codeModule) {
        try {
            const response = await fetch(`${BASE_URL}/moi/modules/${codeModule}/matieres`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des matières');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getSeances(codeMatiere) {
        try {
            const response = await fetch(`${BASE_URL}/moi/matieres/${codeMatiere}/seances`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des séances');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getRessources(seanceId) {
        try {
            const response = await fetch(`${BASE_URL}/seances/${seanceId}/ressources`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la récupération des ressources');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    /**
     * Aggregates resources for a specific module by traversing:
     * Module -> Matieres -> Seances -> Ressources
     */
    async getAggregatedResources(codeModule) {
        try {
            // 1. Get all matieres for the module
            const matieres = await this.getMatieres(codeModule);

            // 2. For each matiere, get all seances (concurrently)
            const matieresWithSeancesPromises = matieres.map(async (matiere) => {
                const seances = await this.getSeances(matiere.codeMatiere);
                return { ...matiere, seances };
            });
            const matieresWithSeances = await Promise.all(matieresWithSeancesPromises);

            // 3. For each seance, get all resources (concurrently)
            const allResources = [];
            const resourcePromises = [];

            matieresWithSeances.forEach(matiere => {
                matiere.seances.forEach(seance => {
                    const promise = this.getRessources(seance.id).then(ressources => {
                        ressources.forEach(res => {
                            allResources.push({
                                ...res,
                                matiereNom: matiere.nom,
                                codeMatiere: matiere.codeMatiere,
                                typeSeance: seance.typeSeance,
                                seanceId: seance.id
                            });
                        });
                    });
                    resourcePromises.push(promise);
                });
            });

            await Promise.all(resourcePromises);

            // Sort by upload date or name
            return allResources.sort((a, b) => new Date(b.dateUpload) - new Date(a.dateUpload));
        } catch (error) {
            console.error('Aggregation error:', error);
            throw error;
        }
    }
};
