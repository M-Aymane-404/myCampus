import apiProfesseur from './apiProfesseur';

function formatHeure(value) {
    if (!value) return '';

    if (typeof value === 'string') {
        if (value.includes('T')) {
            return value.substring(11, 16);
        }
        if (value.length >= 5 && value.includes(':')) {
            return value.substring(0, 5);
        }
    }

    return value;
}

function formatJourDepuisDate(dateValue) {
    if (!dateValue) return '';

    try {
        return new Intl.DateTimeFormat('fr-FR', { weekday: 'long' })
            .format(new Date(dateValue))
            .toUpperCase();
    } catch {
        return '';
    }
}

function normaliserSeance(seance) {
    return {
        ...seance,
        jour:
            seance.jour ||
            seance.dayOfWeek ||
            formatJourDepuisDate(seance.date),
        heureDebut:
            formatHeure(seance.heureDebut) ||
            formatHeure(seance.start),
        matiereNom:
            seance.matiereNom ||
            seance.nomMatiere ||
            seance.matiere ||
            seance.nom ||
            '',
        typeSeance:
            seance.typeSeance ||
            seance.type ||
            'COURS',
        professeurNom:
            seance.professeurNom ||
            seance.professeur ||
            '',
        salle:
            seance.salle || '',
    };
}

export const apiEmploiTempsProfesseur = {
    async obtenirEmploiDuTemps() {
        const data = await apiProfesseur.fetch('/emploi', { method: 'GET' });

        let parsed = data;

        if (typeof parsed === 'string') {
            try {
                parsed = JSON.parse(parsed);
            } catch {
                return [];
            }
        }

        const liste = Array.isArray(parsed)
            ? parsed
            : Array.isArray(parsed?.slots)
                ? parsed.slots
                : Array.isArray(parsed?.seances)
                    ? parsed.seances
                    : Array.isArray(parsed?.emploi)
                        ? parsed.emploi
                        : [];

        return liste.map(normaliserSeance);
    },

    async obtenirProchaineSeance() {
        return apiProfesseur.fetch('/prochaine', { method: 'GET' });
    },
};

export default apiEmploiTempsProfesseur;