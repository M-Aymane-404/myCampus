
import { apiAuth } from './apiAuth';

const BASE_URL = 'http://localhost:8080/api/etudiant';

const SEMESTRES = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9'];

export const apiGrades = {
    /**
     * Fetch grades for a specific semester
     * @param {string|number} semester - e.g., "S4" or 4
     * @returns {Promise<Object>} ReleveDeNoteEtudiantParSessionDTO
     */
    async fetchStudentGrades(semester) {
        const token = apiAuth.getToken();
        if (!token) throw new Error('Non authentifié');

        // Ensure proper format (e.g. S4)
        const semFormatted = typeof semester === 'number' ? `S${semester}` : semester;

        try {
            const response = await fetch(`${BASE_URL}/moi/releve-notes?semestre=${semFormatted}`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });

            if (response.status === 401) {
                console.warn("Session expired or unauthorized. Logging out...");
                // Optional: Clear token immediately so subsequent calls fail fast or redirect
                apiAuth.logout();
                return null;
            }

            if (!response.ok) {
                // If 404 or bad request, might mean semester doesn't exist or not started
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
        const token = apiAuth.getToken();
        if (!token) throw new Error('Non authentifié');

        try {
            const response = await fetch(`${BASE_URL}/moi`, {
                headers: apiAuth.getAuthHeaders(),
                credentials: 'include'
            });
            if (response.status === 401) {
                apiAuth.logout();
                throw new Error('Non authentifié');
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

            // Strategy: Fetch a broad range of semesters (e.g. S1 to S12) to ensure we find all available data in DB.
            // We ignore 404s/empty results in the fetch function.
            const dynamicSemesters = [];
            for (let i = 1; i <= 9; i++) {
                dynamicSemesters.push(`S${i}`);
            }

            const promises = dynamicSemesters.map(s => this.fetchStudentGrades(s));
            const results = await Promise.all(promises);

            const validSemesters = results.filter(r => r && r.modules);

            let totalModules = 0;
            let validatedModules = 0;
            let sumAverages = 0;
            let countAverages = 0;

            validSemesters.forEach(sem => {
                if (sem.moyenneSession) {
                    sumAverages += sem.moyenneSession;
                    countAverages++;
                }

                if (sem.modules) {
                    sem.modules.forEach(mod => {
                        totalModules++;
                        // Determine validation.
                        // Usually module is validated if average >= 10.
                        if (mod.moyenneDuModule >= 10) {
                            validatedModules++;
                        }
                    });
                }
            });

            const generalAverage = countAverages > 0 ? (sumAverages / countAverages) : 0;

            // Determine global mention
            let finalMention = 'Passable';
            if (generalAverage >= 16) finalMention = 'Très Bien';
            else if (generalAverage >= 14) finalMention = 'Bien';
            else if (generalAverage >= 12) finalMention = 'Assez Bien';
            else if (generalAverage < 10) finalMention = 'Ajourné';

            // Extract valid semester numbers
            // DTO has 'semestre' field which is enum "S1", "S2" etc.
            const availableSemesters = validSemesters
                .map(s => {
                    // s.semestre is likely "S1", "S2"
                    const match = s.semestre ? s.semestre.match(/S(\d+)/i) : null;
                    return match ? parseInt(match[1], 10) : null;
                })
                .filter(n => n !== null)
                .sort((a, b) => a - b);

            return {
                degree: "Diplôme d'Ingénieur",
                specialization: profile.codeClasse || "Génie Informatique",
                generalAverage: generalAverage.toFixed(2),
                validatedModules,
                totalModules,
                finalMention,
                currentSessionStatus: validSemesters.length > 0 ? validSemesters[validSemesters.length - 1].moyenneSession >= 10 ? "Admis" : "Ajourné" : "N/A",
                maxSemester: availableSemesters.length > 0 ? Math.max(...availableSemesters) : 1,
                availableSemesters: availableSemesters.length > 0 ? availableSemesters : [1] // Fallback
            };

        } catch (error) {
            console.error("Global Stats Error", error);
            // If handling error, keep the structure safe
            return {
                degree: "N/A",
                specialization: "N/A",
                generalAverage: "0.00",
                validatedModules: 0,
                totalModules: 0,
                finalMention: "-",
                currentSessionStatus: "-",
                maxSemester: 1,
                availableSemesters: [1]
            };
        }
    }
};
