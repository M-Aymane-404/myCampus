import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/etudiant';

const formatSeanceDate = (date) => {
    if (!date) return 'Date inconnue';
    return new Date(date).toLocaleDateString('fr-FR');
};

const normalizeSemesterCode = (value) => {
    if (typeof value === 'number' && Number.isFinite(value)) {
        return `S${Math.max(1, Math.trunc(value))}`;
    }

    const raw = String(value || '').trim().toUpperCase();
    if (!raw) return null;
    if (/^S\d+$/.test(raw)) return raw;
    if (/^\d+$/.test(raw)) return `S${parseInt(raw, 10)}`;
    return null;
};

const buildSemesterCandidates = (semester) => {
    const normalized = normalizeSemesterCode(semester);
    if (!normalized) return [semester];

    const numeric = normalized.replace(/^S/, '');
    return Array.from(new Set([normalized, numeric]));
};

export const apiCours = {
    getSemesters() {
        return ['S1', 'S2', 'S3', 'S4', 'S5', 'S6'];
    },

    async getStudentProfile() {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi`, {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la recuperation du profil');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getStudentModules(semester = null) {
        try {
            if (!semester || semester === 'TOUS') {
                const response = await authenticatedFetch(`${BASE_URL}/moi/modules`, {
                    credentials: 'include'
                });
                if (!response.ok) throw new Error('Erreur lors de la recuperation des modules');
                return await response.json();
            }

            const candidates = buildSemesterCandidates(semester);
            let lastError = null;

            for (const candidate of candidates) {
                try {
                    const response = await authenticatedFetch(`${BASE_URL}/moi/modules?semestre=${candidate}`, {
                        credentials: 'include'
                    });
                    if (!response.ok) {
                        continue;
                    }

                    const payload = await response.json();
                    if (Array.isArray(payload) && payload.length > 0) {
                        return payload;
                    }

                    if (Array.isArray(payload)) {
                        lastError = null;
                    }
                } catch (error) {
                    lastError = error;
                }
            }

            if (lastError) {
                throw lastError;
            }

            return [];
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getMatieres(codeModule) {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi/modules/${codeModule}/matieres`, {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la recuperation des matieres');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getSeances(codeMatiere) {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi/matieres/${codeMatiere}/seances`, {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la recuperation des seances');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async getRessources(seanceId) {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/seances/${seanceId}/ressources`, {
                credentials: 'include'
            });
            if (!response.ok) throw new Error('Erreur lors de la recuperation des ressources');
            return await response.json();
        } catch (error) {
            console.error(error);
            throw error;
        }
    },

    async downloadRessource(fichierId) {
        const response = await authenticatedFetch(`${BASE_URL}/fichiers-seance/${fichierId}/telecharger`, {
            credentials: 'include'
        });

        if (!response.ok) {
            throw new Error('Erreur lors du telechargement de la ressource');
        }

        return response;
    },

    async getAggregatedResources(codeModule) {
        try {
            const matieres = await this.getMatieres(codeModule);

            const matieresWithSeancesPromises = matieres.map(async (matiere) => {
                const seances = await this.getSeances(matiere.codeMatiere);
                return { ...matiere, seances };
            });
            const matieresWithSeances = await Promise.all(matieresWithSeancesPromises);

            const allResources = [];
            const resourcePromises = [];

            matieresWithSeances.forEach((matiere) => {
                matiere.seances.forEach((seance) => {
                    const promise = this.getRessources(seance.id).then((ressources) => {
                        ressources.forEach((res) => {
                            const typeSeance = seance.type || seance.typeSeance || 'SEANCE';
                            allResources.push({
                                ...res,
                                matiereNom: matiere.nom,
                                codeMatiere: matiere.codeMatiere,
                                typeSeance,
                                seanceId: seance.id,
                                seanceDate: seance.date || null,
                                seanceLabel: `${matiere.nom} - ${typeSeance} - ${formatSeanceDate(seance.date)}`
                            });
                        });
                    });
                    resourcePromises.push(promise);
                });
            });

            await Promise.all(resourcePromises);

            return allResources.sort((a, b) => new Date(b.dateUpload) - new Date(a.dateUpload));
        } catch (error) {
            console.error('Aggregation error:', error);
            throw error;
        }
    }
};
