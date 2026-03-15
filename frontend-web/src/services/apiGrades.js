import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/etudiant';

const SEMESTRES = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9'];

export const apiGrades = {
    /**
     * Fetch grades for a specific semester
     * @param {string|number} semester - e.g. "S4" or 4
     * @returns {Promise<Object>} ReleveDeNoteEtudiantParSessionDTO
     */
    async fetchStudentGrades(semester) {
        const semFormatted = typeof semester === 'number' ? `S${semester}` : semester;

        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi/releve-notes?semestre=${semFormatted}`, {
                credentials: 'include'
            });

            if (response.status === 401) {
                throw new Error('Non authentifie');
            }

            if (!response.ok) {
                console.warn(`Fetch grades failed for ${semFormatted}:`, response.status);
                return null;
            }

            return await response.json();
        } catch (error) {
            console.error('API Grades Error:', error);
            throw error;
        }
    },

    /**
     * Fetch basic student info (for class/specialization)
     */
    async fetchStudentProfile() {
        try {
            const response = await authenticatedFetch(`${BASE_URL}/moi`, {
                credentials: 'include'
            });
            if (response.status === 401) {
                throw new Error('Non authentifie');
            }
            if (!response.ok) throw new Error('Erreur profile');
            return await response.json();
        } catch (error) {
            console.error('API Profile Error:', error);
            throw error;
        }
    },

    /**
     * Aggregate stats from all valid semesters
     */
    async fetchGlobalStats() {
        try {
            const profile = await this.fetchStudentProfile();

            const dynamicSemesters = [];
            for (let i = 1; i <= SEMESTRES.length; i++) {
                dynamicSemesters.push(`S${i}`);
            }

            const promises = dynamicSemesters.map((s) => this.fetchStudentGrades(s));
            const results = await Promise.all(promises);

            const validSemesters = results.filter((r) => r && r.modules);

            let totalModules = 0;
            let validatedModules = 0;
            let sumAverages = 0;
            let countAverages = 0;

            validSemesters.forEach((sem) => {
                if (sem.moyenneSession) {
                    sumAverages += sem.moyenneSession;
                    countAverages++;
                }

                if (sem.modules) {
                    sem.modules.forEach((mod) => {
                        totalModules++;
                        if (mod.moyenneDuModule >= 10) {
                            validatedModules++;
                        }
                    });
                }
            });

            const generalAverage = countAverages > 0 ? (sumAverages / countAverages) : 0;

            let finalMention = 'Passable';
            if (generalAverage >= 16) finalMention = 'Tres Bien';
            else if (generalAverage >= 14) finalMention = 'Bien';
            else if (generalAverage >= 12) finalMention = 'Assez Bien';
            else if (generalAverage < 10) finalMention = 'Ajourne';

            const availableSemesters = validSemesters
                .map((s) => {
                    const match = s.semestre ? s.semestre.match(/S(\d+)/i) : null;
                    return match ? parseInt(match[1], 10) : null;
                })
                .filter((n) => n !== null)
                .sort((a, b) => a - b);

            return {
                degree: "Diplome d'Ingenieur",
                specialization: profile.codeClasse || 'Genie Informatique',
                studentName: `${profile.prenom || ''} ${profile.nom || ''}`.trim(),
                studentId: profile.apogee || 'N/A',
                generalAverage: generalAverage.toFixed(2),
                validatedModules,
                totalModules,
                finalMention,
                currentSessionStatus: validSemesters.length > 0
                    ? validSemesters[validSemesters.length - 1].moyenneSession >= 10 ? 'Admis' : 'Ajourne'
                    : 'N/A',
                maxSemester: availableSemesters.length > 0 ? Math.max(...availableSemesters) : 1,
                availableSemesters: availableSemesters.length > 0 ? availableSemesters : [1]
            };

        } catch (error) {
            console.error('Global Stats Error', error);
            return {
                degree: 'N/A',
                specialization: 'N/A',
                generalAverage: '0.00',
                validatedModules: 0,
                totalModules: 0,
                finalMention: '-',
                currentSessionStatus: '-',
                maxSemester: 1,
                availableSemesters: [1]
            };
        }
    }
};
