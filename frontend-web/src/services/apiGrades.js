import { authenticatedFetch } from '../utils/apiInterceptor';

const BASE_URL = 'http://localhost:8080/api/etudiant';

const parseFormationDuration = (profile) => {
    if (typeof profile?.nombreSemestres === 'number' && profile.nombreSemestres > 0) {
        return Math.max(2, profile.nombreSemestres);
    }

    if (typeof profile?.dureeFormation === 'number' && profile.dureeFormation > 0) {
        return Math.max(2, profile.dureeFormation * 2);
    }

    if (typeof profile?.dureeFormation === 'string') {
        const match = profile.dureeFormation.match(/(\d+)/);
        if (match) {
            return Math.max(2, parseInt(match[1], 10) * 2);
        }
    }

    return 2;
};

const normalizeSemesterCode = (semester) => {
    if (typeof semester === 'number' && Number.isFinite(semester)) {
        const numeric = Math.max(1, Math.trunc(semester));
        return `S${numeric}`;
    }

    const raw = String(semester || '').trim().toUpperCase();
    if (!raw) return null;
    if (/^S\d+$/.test(raw)) return raw;
    if (/^\d+$/.test(raw)) return `S${parseInt(raw, 10)}`;
    return null;
};

const buildSemesterCandidates = (semester) => {
    const code = normalizeSemesterCode(semester) || 'S1';
    const numeric = code.replace(/^S/, '');
    return [code, numeric];
};

const getModulesFromPayload = (payload) => (
    Array.isArray(payload?.modules) ? payload.modules : []
);

const countEnteredNotes = (payload) => {
    const modules = getModulesFromPayload(payload);
    let count = 0;

    modules.forEach((moduleItem) => {
        const matieres = Array.isArray(moduleItem?.matieres) ? moduleItem.matieres : [];
        matieres.forEach((matiere) => {
            const examens = Array.isArray(matiere?.examens) ? matiere.examens : [];
            examens.forEach((exam) => {
                const note = exam?.note ?? exam?.noteExamen ?? exam?.noteFinale;
                if (note !== null && note !== undefined && note !== '') {
                    count += 1;
                }
            });
        });
    });

    return count;
};

const extractPayloadSemesterCode = (payload) => {
    const candidates = [
        payload?.semestre,
        payload?.codeSemestre,
        payload?.semestreActuel,
        payload?.session?.semestre,
    ];

    for (const candidate of candidates) {
        const normalized = normalizeSemesterCode(candidate);
        if (normalized) return normalized;
    }

    const modules = getModulesFromPayload(payload);
    const moduleSemesters = modules
        .map((moduleItem) => normalizeSemesterCode(moduleItem?.semestre))
        .filter(Boolean);

    if (moduleSemesters.length > 0) {
        return moduleSemesters[0];
    }

    return null;
};

const scoreSemesterPayload = (payload, requestedSemesterCode) => {
    const modules = getModulesFromPayload(payload);
    const noteCount = countEnteredNotes(payload);
    const payloadSemester = extractPayloadSemesterCode(payload);

    let score = modules.length * 10 + noteCount;

    if (payloadSemester && payloadSemester === requestedSemesterCode) {
        score += 1000;
    } else if (payloadSemester && payloadSemester !== requestedSemesterCode) {
        score -= 100;
    }

    return score;
};

export const apiGrades = {
    /**
     * Fetch grades for a specific semester
     * @param {string|number} semester - e.g. "S4" or 4
     * @returns {Promise<Object>} ReleveDeNoteEtudiantParSessionDTO
     */
    async fetchStudentGrades(semester) {
        const requestedSemesterCode = normalizeSemesterCode(semester) || 'S1';
        const semesterCandidates = buildSemesterCandidates(semester);
        const responses = [];

        try {
            for (const candidate of semesterCandidates) {
                const response = await authenticatedFetch(`${BASE_URL}/moi/releve-notes?semestre=${candidate}`, {
                    credentials: 'include'
                });

                if (response.status === 401) {
                    throw new Error('Non authentifie');
                }

                if (!response.ok) {
                    continue;
                }

                let payload = null;
                try {
                    payload = await response.json();
                } catch {
                    continue;
                }

                if (!payload || typeof payload !== 'object') {
                    continue;
                }

                responses.push(payload);
            }

            if (responses.length === 0) {
                console.warn(`Fetch grades failed for ${requestedSemesterCode}`);
                return null;
            }

            const sortedResponses = [...responses].sort(
                (left, right) =>
                    scoreSemesterPayload(right, requestedSemesterCode)
                    - scoreSemesterPayload(left, requestedSemesterCode)
            );

            return sortedResponses[0];
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
            const totalSemesters = parseFormationDuration(profile);

            const dynamicSemesters = Array.from(
                { length: totalSemesters },
                (_, index) => `S${index + 1}`
            );

            const promises = dynamicSemesters.map((s) => this.fetchStudentGrades(s));
            const results = await Promise.all(promises);

            const validSemesters = results.filter(
                (result) =>
                    result
                    && Array.isArray(result.modules)
                    && result.modules.length > 0
            );
            const hasCompleteTranscript = results.length === dynamicSemesters.length
                && results.every((sem) =>
                    sem
                    && Array.isArray(sem.modules)
                    && sem.modules.length > 0
                    && sem.moyenneSession !== null
                    && sem.moyenneSession !== undefined
                    && sem.modules.every(
                        (mod) => mod.moyenneDuModule !== null && mod.moyenneDuModule !== undefined
                    )
                );

            let totalModules = 0;
            let validatedModules = 0;
            let sumAverages = 0;
            let countAverages = 0;

            validSemesters.forEach((sem) => {
                if (Number.isFinite(Number(sem.moyenneSession))) {
                    sumAverages += Number(sem.moyenneSession);
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

            const generalAverage = hasCompleteTranscript && countAverages > 0
                ? (sumAverages / countAverages)
                : null;

            let finalMention = 'Passable';
            if (generalAverage === null) finalMention = '-';
            else if (generalAverage >= 16) finalMention = 'Tres Bien';
            else if (generalAverage >= 14) finalMention = 'Bien';
            else if (generalAverage >= 12) finalMention = 'Assez Bien';
            else if (generalAverage < 10) finalMention = 'Ajourne';

            const availableSemesters = Array.from(
                { length: totalSemesters },
                (_, index) => index + 1
            );

            return {
                degree: "Diplome d'Ingenieur",
                specialization: profile.codeClasse || 'Genie Informatique',
                studentName: `${profile.prenom || ''} ${profile.nom || ''}`.trim(),
                studentId: profile.apogee || 'N/A',
                generalAverage: generalAverage !== null ? generalAverage.toFixed(2) : null,
                hasCompleteTranscript,
                validatedModules,
                totalModules,
                finalMention,
                currentSessionStatus: validSemesters.length > 0
                    ? Number(validSemesters[validSemesters.length - 1].moyenneSession) >= 10 ? 'Admis' : 'Ajourne'
                    : 'N/A',
                maxSemester: availableSemesters.length > 0 ? Math.max(...availableSemesters) : 1,
                availableSemesters: availableSemesters.length > 0 ? availableSemesters : [1]
            };

        } catch (error) {
            console.error('Global Stats Error', error);
            return {
                degree: 'N/A',
                specialization: 'N/A',
                generalAverage: null,
                hasCompleteTranscript: false,
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
