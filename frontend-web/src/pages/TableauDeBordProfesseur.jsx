import { useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  BarChart3,
  Bell,
  BookOpen,
  Calendar,
  ClipboardList,
  Clock,
  FileWarning,
  Layers3,
  Loader2,
  MapPin,
} from 'lucide-react';
import {
  Bar,
  BarChart,
  CartesianGrid,
  Cell,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts';
import { apiTableauDeBordProfesseur } from '../services/professeur/apiTableauDeBordProfesseur';
import apiExamensProfesseur from '../services/professeur/apiExamensProfesseur';
import apiNotesProfesseur from '../services/professeur/apiNotesProfesseur';
import { apiEnseignementsProfesseur } from '../services/professeur/apiEnseignementsProfesseur';
import apiSeancesProfesseur from '../services/professeur/apiSeancesProfesseur';
import { apiPresencesProfesseur } from '../services/professeur/apiPresencesProfesseur';
import './TableauDeBordProfesseur.css';

const TYPE_COLOR = { COURS: 'blue', TD: 'grey', TP: 'purple' };

const NOTE_TABS = [
  { id: 'classe', label: 'Par classe', icon: Layers3 },
  { id: 'matiere', label: 'Par matiere', icon: BookOpen },
];

const CHART_COLORS = ['#5c54ed', '#3d7ea6', '#60a5fa', '#8b5cf6', '#f4a261', '#64748b'];

const EMPTY_NOTE_STATS = {
  parClasse: [],
  parMatiere: [],
  prochainExamen: null,
  resumeGlobal: {
    moyenneGenerale: null,
    noteMaximale: null,
    noteMinimale: null,
    tauxReussite: null,
    notesSaisies: 0,
    totalAttendu: 0,
  },
  meta: {
    uniqueStudentCount: 0,
    totalClasses: 0,
    totalMatieres: 0,
    examensProgrammes: 0,
    notesPubliees: 0,
    notesEnAttente: 0,
    examensSansPublication: 0,
    copiesACorriger: 0,
    feuillesNotesACompleter: 0,
    groupesEnDifficulte: 0,
  },
};

const parseDateValue = (value) => {
  if (!value) return null;

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

const combineDateAndTime = (dateValue, timeValue) => {
  const datePart =
    typeof dateValue === 'string' && dateValue.length >= 10
      ? dateValue.slice(0, 10)
      : parseDateValue(dateValue)?.toISOString().slice(0, 10);

  if (!datePart || !timeValue) return null;

  const rawTime =
    typeof timeValue === 'string'
      ? timeValue.includes('T')
        ? timeValue.slice(11, 19)
        : timeValue
      : parseDateValue(timeValue)?.toISOString().slice(11, 19);

  if (!rawTime) return null;

  const normalizedTime =
    rawTime.length === 5 ? `${rawTime}:00` : rawTime.length >= 8 ? rawTime.slice(0, 8) : null;

  if (!normalizedTime) return null;
  return parseDateValue(`${datePart}T${normalizedTime}`);
};

const extractTimeLabel = (value) => {
  if (!value) return '';
  if (typeof value === 'string') {
    if (/^\d{2}:\d{2}/.test(value)) return value.slice(0, 5);
    const parsed = parseDateValue(value);
    if (parsed) return formatHour(parsed);
  }

  return formatHour(value);
};

const formatDate = (value) => {
  if (!value) return '--';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '--';
  return date.toLocaleDateString('fr-FR', { weekday: 'short', day: '2-digit', month: 'short' });
};

const formatHour = (value) => {
  if (!value) return '';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';
  return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

const formatFloat = (value, digits = 2) => (Number.isFinite(value) ? Number(value).toFixed(digits) : '--');
const formatPercent = (value) => (Number.isFinite(value) ? `${Math.round(value)}%` : '--');
const formatCompactNumber = (value) => new Intl.NumberFormat('fr-FR').format(value ?? 0);
const formaterDerniereMiseAJour = (date) =>
  date
    ? `Mis a jour a ${date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}`
    : '';

const pickFirstValue = (...values) => values.find((value) => value !== undefined && value !== null && value !== '');

const getNestedValue = (source, paths) => {
  for (const path of paths) {
    const segments = path.split('.');
    let current = source;

    for (const segment of segments) {
      current = current?.[segment];
      if (current === undefined || current === null || current === '') break;
    }

    if (current !== undefined && current !== null && current !== '') {
      return current;
    }
  }

  return null;
};

const createAggregate = (label) => ({
  label,
  somme: 0,
  count: 0,
  max: null,
  min: null,
  successCount: 0,
  totalAttendu: 0,
});

const updateAggregate = (aggregate, notes, totalAttendu) => {
  const validNotes = notes.filter((note) => Number.isFinite(note));
  aggregate.totalAttendu += totalAttendu;
  if (validNotes.length === 0) return aggregate;

  const localMax = Math.max(...validNotes);
  const localMin = Math.min(...validNotes);
  aggregate.somme += validNotes.reduce((sum, note) => sum + note, 0);
  aggregate.count += validNotes.length;
  aggregate.max = aggregate.max == null ? localMax : Math.max(aggregate.max, localMax);
  aggregate.min = aggregate.min == null ? localMin : Math.min(aggregate.min, localMin);
  aggregate.successCount += validNotes.filter((note) => note >= 10).length;
  return aggregate;
};

const getSessionStartDate = (session) =>
  combineDateAndTime(session?.date, session?.heureDebut) ||
  parseDateValue(session?.start) ||
  parseDateValue(session?.heureDebut) ||
  parseDateValue(session?.date);

const getSessionEndDate = (session) =>
  combineDateAndTime(session?.date, session?.heureFin) ||
  parseDateValue(session?.end) ||
  parseDateValue(session?.heureFin);

const getExamDate = (exam) =>
  parseDateValue(exam?.dateExamen) ||
  parseDateValue(exam?.date) ||
  parseDateValue(exam?.heureDebut);

const isCancelledExam = (exam) => ['ANNULE', 'ANNULEE'].includes(String(exam?.etat || '').trim().toUpperCase());
const isExamCompleted = (exam) => ['PASSE', 'CORRIGE', 'ANNULE', 'ANNULEE'].includes(String(exam?.etat || '').trim().toUpperCase());

const isCancelledSession = (session) => ['ANNULEE', 'ANNULE'].includes(String(session?.etat || '').trim().toUpperCase());

const hasEnteredNote = (value) => {
  if (value === null || value === undefined) return false;
  if (typeof value === 'string' && value.trim() === '') return false;

  const numeric = typeof value === 'string' ? Number(value.replace(',', '.')) : Number(value);
  return Number.isFinite(numeric);
};

const pickNearestUpcomingSession = (...sessions) => {
  const now = Date.now();
  const validUpcoming = sessions
    .filter(Boolean)
    .map((session) => ({ session, startDate: getSessionStartDate(session) }))
    .filter(({ startDate }) => startDate && startDate.getTime() >= now)
    .sort((a, b) => a.startDate.getTime() - b.startDate.getTime());

  if (validUpcoming.length > 0) return validUpcoming[0].session;

  return sessions.find(Boolean) || null;
};

const finalizeAggregate = (aggregate, suffix = '') => ({
  id: `${aggregate.label}-${suffix || 'stats'}`,
  label: aggregate.label,
  moyenneGenerale: aggregate.count > 0 ? aggregate.somme / aggregate.count : null,
  noteMaximale: aggregate.max,
  noteMinimale: aggregate.min,
  tauxReussite: aggregate.count > 0 ? (aggregate.successCount / aggregate.count) * 100 : null,
  notesSaisies: aggregate.count,
  totalAttendu: aggregate.totalAttendu,
});

const buildNoteStatistics = async () => {
  const filters = await apiExamensProfesseur.chargerFiltres();
  const classes = Array.isArray(filters?.classes) ? filters.classes : [];
  if (classes.length === 0) return EMPTY_NOTE_STATS;

  const classMap = new Map();
  const subjectMap = new Map();
  const globalAggregate = createAggregate('global');
  const studentIds = new Set();
  const upcomingExams = [];
  const now = Date.now();
  let notesPubliees = 0;
  let notesEnAttente = 0;
  let examensSansPublication = 0;
  let copiesACorriger = 0;
  let feuillesNotesACompleter = 0;
  let examensProgrammes = 0;

  await Promise.all(
    classes.map(async (classe) => {
      const codeClasse = classe.codeClasse || classe.nomClasse;
      const classeLabel = classe.nomClasse || codeClasse || 'Classe';
      const matieres = Array.isArray(classe.matieres) ? classe.matieres : [];
      if (!codeClasse || matieres.length === 0) return;

      await Promise.all(
        matieres.map(async (matiere) => {
          const codeMatiere = matiere.codeMatiere;
          const matiereLabel = matiere.nomMatiere || codeMatiere || 'Matiere';
          if (!codeMatiere) return;

          const examens = await apiExamensProfesseur.listerExamens(codeClasse, codeMatiere);
          const examensList = Array.isArray(examens) ? examens : [];
          if (examensList.length === 0) return;

          examensProgrammes += examensList.filter((examen) => !isCancelledExam(examen)).length;

          examensList.forEach((examen) => {
            const examDate = getExamDate(examen);
            if (!examDate || examDate.getTime() < now || isExamCompleted(examen)) return;

            upcomingExams.push({
              ...examen,
              nomClasse: examen.nomClasse || classeLabel,
              codeClasse: examen.codeClasse || codeClasse,
              nomMatiere: examen.nomMatiere || matiereLabel,
              codeMatiere: examen.codeMatiere || codeMatiere,
            });
          });

          await Promise.all(
            examensList.map(async (examen) => {
              const examenId = examen.idExamen || examen.id;
              if (!examenId) return;

              const feuille = await apiNotesProfesseur.chargerFeuille(codeClasse, examenId);
              const lignes = Array.isArray(feuille?.lignes) ? feuille.lignes : [];
              const notes = [];
              const examDate = getExamDate(examen);
              let notesSaisiesCount = 0;

              lignes.forEach((ligne) => {
                const value = ligne.note;
                const numeric = typeof value === 'string' ? Number(value.replace(',', '.')) : Number(value);
                if (Number.isFinite(numeric)) notes.push(numeric);
                if (hasEnteredNote(value)) notesSaisiesCount += 1;

                const studentId = ligne.idEtudiant || ligne.apogee;
                if (studentId) studentIds.add(studentId);

                if (ligne.publiee) notesPubliees += 1;
                else notesEnAttente += 1;
              });

              if (lignes.some((ligne) => !ligne.publiee)) examensSansPublication += 1;
              if (examen.etat === 'PASSE') copiesACorriger += 1;
              if (
                lignes.length > 0 &&
                notesSaisiesCount < lignes.length &&
                (['PASSE', 'CORRIGE'].includes(String(examen.etat || '').trim().toUpperCase()) ||
                  (examDate && examDate.getTime() <= now))
              ) {
                feuillesNotesACompleter += 1;
              }

              if (!classMap.has(classeLabel)) classMap.set(classeLabel, createAggregate(classeLabel));
              if (!subjectMap.has(matiereLabel)) subjectMap.set(matiereLabel, createAggregate(matiereLabel));

              updateAggregate(classMap.get(classeLabel), notes, lignes.length);
              updateAggregate(subjectMap.get(matiereLabel), notes, lignes.length);
              updateAggregate(globalAggregate, notes, lignes.length);
            }),
          );
        }),
      );
    }),
  );

  const parClasse = Array.from(classMap.values())
    .map((aggregate) => finalizeAggregate(aggregate, 'classe'))
    .sort((a, b) => (b.moyenneGenerale || 0) - (a.moyenneGenerale || 0));

  const parMatiere = Array.from(subjectMap.values())
    .map((aggregate) => finalizeAggregate(aggregate, 'matiere'))
    .sort((a, b) => (b.moyenneGenerale || 0) - (a.moyenneGenerale || 0));

  const sortedUpcomingExams = [...upcomingExams].sort((a, b) => {
    const left = getExamDate(a)?.getTime() || Number.MAX_SAFE_INTEGER;
    const right = getExamDate(b)?.getTime() || Number.MAX_SAFE_INTEGER;
    return left - right;
  });

  return {
    parClasse,
    parMatiere,
    prochainExamen: sortedUpcomingExams[0] || null,
    resumeGlobal: finalizeAggregate(globalAggregate, 'global'),
    meta: {
      uniqueStudentCount: studentIds.size,
      totalClasses: parClasse.length,
      totalMatieres: parMatiere.length,
      examensProgrammes,
      notesPubliees,
      notesEnAttente,
      examensSansPublication,
      copiesACorriger,
      feuillesNotesACompleter,
      groupesEnDifficulte: parClasse.filter((item) => (item.tauxReussite ?? 100) < 55 || (item.moyenneGenerale ?? 20) < 10).length,
    },
  };
};

const DashboardTooltip = ({ active, payload }) => {
  if (!active || !payload?.length) return null;
  const point = payload[0]?.payload;
  if (!point) return null;

  return (
    <div className="prof-chart-tooltip">
      <strong>{point.label}</strong>
      <span>Moyenne: {formatFloat(point.moyenneGenerale)}/20</span>
      <span>Reussite: {formatPercent(point.tauxReussite)}</span>
      <span>Notes saisies: {point.notesSaisies}/{point.totalAttendu}</span>
    </div>
  );
};

const normalizeSession = (payload) => {
  const session = payload?.prochaineSeance || payload?.nextSession || payload || null;
  if (!session || typeof session !== 'object') return null;
  const startDate = getSessionStartDate(session);
  const endDate = getSessionEndDate(session);

  const subject = pickFirstValue(
    session.matiereNom,
    session.nomMatiere,
    session.matiere,
    session.matiereName,
    session.moduleName,
    session.nomModule,
    session.enseignement?.matiereNom,
    session.enseignement?.nomMatiere,
    session.enseignement?.matiere?.nomMatiere,
    session.enseignement?.matiere?.nom,
    session.module?.nom,
    session.nom,
  );

  const className = pickFirstValue(
    session.nomClasse,
    session.classeNom,
    session.classe,
    session.codeClasse,
    session.groupeNom,
    session.enseignement?.classe?.nomClasse,
    session.enseignement?.classe?.nom,
    session.enseignement?.classe?.codeClasse,
  );

  const room = pickFirstValue(
    session.salle,
    session.room,
    session.nomSalle,
    session.local,
    session.enseignement?.salle,
  );

  const type = pickFirstValue(session.typeSeance, session.type, session.categorie, 'COURS');
  const start = extractTimeLabel(session.start) || formatHour(startDate) || extractTimeLabel(session.heureDebut);
  const end = extractTimeLabel(session.end) || formatHour(endDate) || extractTimeLabel(session.heureFin);
  const day = pickFirstValue(session.dayOfWeek, session.jour, formatDate(startDate || session.date));

  return {
    ...session,
    displaySubject: subject || 'Seance a confirmer',
    displayClass: className || 'Classe',
    displayRoom: room || 'Salle a definir',
    displayType: type,
    displayStart: start || '',
    displayEnd: end || '',
    displayDay: day || '--',
  };
};

const buildAbsenceInsights = (dashboardData) => {
  const stats = dashboardData?.stats || {};
  const alerts = dashboardData?.alertes || dashboardData?.absences || {};

  const todayCount = pickFirstValue(
    getNestedValue(dashboardData, ['stats.nbAbsencesAujourdhui', 'stats.absencesAujourdhui', 'stats.nbAbsentsJour', 'absences.nbAujourdhui']),
    0,
  );

  const pendingCount = pickFirstValue(
    getNestedValue(dashboardData, ['stats.nbJustificationsEnAttente', 'stats.justificationsEnAttente', 'absences.justificationsEnAttente', 'alertes.justificationsEnAttente']),
    0,
  );

  const highRiskCount = pickFirstValue(
    getNestedValue(dashboardData, ['stats.nbEtudiantsARisqueAbsence', 'stats.etudiantsARisqueAbsence', 'absences.etudiantsARisque', 'alertes.absencesCritiques']),
    0,
  );

  const attendanceRate = pickFirstValue(
    getNestedValue(dashboardData, ['stats.tauxPresence', 'stats.tauxAssiduite', 'absences.tauxPresence']),
    null,
  );

  return {
    todayCount: Number(todayCount) || 0,
    pendingCount: Number(pendingCount) || 0,
    highRiskCount: Number(highRiskCount) || 0,
    attendanceRate: Number.isFinite(Number(attendanceRate)) ? Number(attendanceRate) : null,
    hasDetails: Object.keys(stats).length > 0 || Object.keys(alerts).length > 0,
  };
};

const isSameDay = (left, right) => {
  const a = new Date(left);
  const b = new Date(right);
  if (Number.isNaN(a.getTime()) || Number.isNaN(b.getTime())) return false;
  return (
    a.getFullYear() === b.getFullYear() &&
    a.getMonth() === b.getMonth() &&
    a.getDate() === b.getDate()
  );
};

const normalizePresenceStatus = (status) => {
  const normalized = String(status || '').trim().toUpperCase();
  if (['PRESENT', 'PRESENTE', 'P', 'PR'].includes(normalized)) return 'PRESENT';
  if (['ABSENT', 'ABSENTE', 'A', 'ABS'].includes(normalized)) return 'ABSENT';
  return normalized;
};

const hasJustification = (ligne) =>
  Boolean(ligne?.justificationSoumise || ligne?.motif || ligne?.motifUrl || ligne?.motifNom || ligne?.typeMotif);

const isPendingJustification = (ligne) => {
  const normalized = String(ligne?.statutJustificatif || '').trim().toUpperCase();

  if (normalized === 'EN_ATTENTE') return true;
  if (normalized === 'VALIDE' || normalized === 'REFUSE') return false;

  return hasJustification(ligne) && normalizePresenceStatus(ligne?.status) === 'ABSENT' && !ligne?.justifiee;
};

const buildPresenceInsightsFromApp = async () => {
  const enseignements = await apiEnseignementsProfesseur.listerEnseignements();
  const items = Array.isArray(enseignements) ? enseignements : [];
  if (items.length === 0) {
    return {
      prochaineSeance: null,
      enseignementsActifs: 0,
      absencesJour: 0,
      feuillesACompleter: 0,
      etudiantsARisque: 0,
      justificatifsAConsulter: 0,
      coursSansSupport: 0,
      tauxPresence: null,
    };
  }

  const enseignementsActifs = new Set(
    items.map((enseignement) => `${enseignement.codeClasse || ''}-${enseignement.codeMatiere || ''}`)
  ).size;

  const seancesParEnseignement = await Promise.all(
    items.map(async (enseignement) => {
      try {
        const seances = await apiSeancesProfesseur.rechercherSeances({
          codeClasse: enseignement.codeClasse,
          codeMatiere: enseignement.codeMatiere,
        });

        return Array.isArray(seances)
          ? seances.map((seance) => ({
              ...seance,
              nomMatiere: enseignement.nomMatiere || enseignement.matiereNom,
              nomClasse: enseignement.nomClasse || enseignement.classeNom || enseignement.codeClasse,
            }))
          : [];
      } catch {
        return [];
      }
    }),
  );

  const allSeances = Array.from(
    seancesParEnseignement
      .flat()
      .filter((seance) => !isCancelledSession(seance))
      .reduce((map, seance) => {
        const key = seance?.id || `${seance?.codeClasse || ''}-${seance?.codeMatiere || ''}-${seance?.date || ''}-${seance?.heureDebut || ''}`;
        if (!map.has(key)) {
          map.set(key, seance);
        }
        return map;
      }, new Map())
      .values(),
  );
  const now = new Date();
  const prochaineSeance = [...allSeances]
    .map((seance) => ({ seance, startDate: getSessionStartDate(seance) }))
    .filter(({ startDate }) => startDate && startDate.getTime() >= now.getTime())
    .sort((a, b) => a.startDate.getTime() - b.startDate.getTime())[0]?.seance || null;

  const upcomingSeances = [...allSeances]
    .map((seance) => ({ seance, startDate: getSessionStartDate(seance) }))
    .filter(({ startDate }) => startDate && startDate.getTime() >= now.getTime())
    .map(({ seance }) => seance);

  const seancesPasseesOuEnCours = [...allSeances]
    .map((seance) => ({ seance, startDate: getSessionStartDate(seance) }))
    .filter(({ startDate }) => startDate && startDate.getTime() <= now.getTime())
    .sort((a, b) => b.startDate.getTime() - a.startDate.getTime())
    .map(({ seance }) => seance);

  let absencesJour = 0;
  let feuillesACompleter = 0;
  let presents = 0;
  let totalStatuses = 0;
  let justificatifsAConsulter = 0;
  const absencesParEtudiant = new Map();

  await Promise.all(
    seancesPasseesOuEnCours.map(async (seance) => {
      try {
        const feuille = await apiPresencesProfesseur.chargerFeuille(seance.id);
        const lignes = Array.isArray(feuille?.lignes) ? feuille.lignes : [];
        let seanceComplete = lignes.length > 0;

        lignes.forEach((ligne) => {
          const status = normalizePresenceStatus(ligne.status);
          if (status === 'PRESENT' || status === 'ABSENT') {
            totalStatuses += 1;
            if (status === 'PRESENT') presents += 1;
            if (status === 'ABSENT') {
              const studentKey = ligne.apogee || ligne.idEtudiant;
              if (studentKey) {
                absencesParEtudiant.set(studentKey, (absencesParEtudiant.get(studentKey) || 0) + 1);
              }

              if (isSameDay(getSessionStartDate(seance) || seance.date, now)) {
                absencesJour += 1;
              }
            }

            if (isPendingJustification(ligne)) {
              justificatifsAConsulter += 1;
            }
          } else {
            seanceComplete = false;
          }
        });

        if (!seanceComplete) {
          feuillesACompleter += 1;
        }
      } catch (err) {
        console.warn('Impossible de charger la feuille de presence pour la seance', seance?.id, err);
      }
    }),
  );

  const supportsChecks = await Promise.all(
    upcomingSeances.map(async (seance) => {
      try {
        const fichiers = await apiSeancesProfesseur.listerFichiersDeSeance(seance.id);
        return Array.isArray(fichiers) && fichiers.length > 0 ? 0 : 1;
      } catch (err) {
        console.warn('Impossible de charger les ressources de la seance', seance?.id, err);
        return 0;
      }
    }),
  );

  return {
    prochaineSeance,
    enseignementsActifs,
    absencesJour,
    feuillesACompleter,
    etudiantsARisque: Array.from(absencesParEtudiant.values()).filter((count) => count >= 2).length,
    justificatifsAConsulter,
    coursSansSupport: supportsChecks.reduce((sum, count) => sum + count, 0),
    tauxPresence: totalStatuses > 0 ? (presents / totalStatuses) * 100 : null,
  };
};

const TableauDeBordProfesseur = () => {
  const [dashboard, setDashboard] = useState(null);
  const [prochaineSeance, setProchaineSeance] = useState(null);
  const [presenceInsights, setPresenceInsights] = useState({
    prochaineSeance: null,
    enseignementsActifs: 0,
    absencesJour: 0,
    feuillesACompleter: 0,
    etudiantsARisque: 0,
    justificatifsAConsulter: 0,
    coursSansSupport: 0,
    tauxPresence: null,
  });
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeNoteTab, setActiveNoteTab] = useState('classe');
  const [notesState, setNotesState] = useState({ loading: true, error: '', data: EMPTY_NOTE_STATS });
  const [derniereMiseAJour, setDerniereMiseAJour] = useState(null);

  const loadNotesStats = async (showLoader = true) => {
    try {
      setNotesState((current) => ({ ...current, loading: showLoader ? true : current.loading, error: '' }));
      const noteStats = await buildNoteStatistics();
      setNotesState({ loading: false, error: '', data: noteStats });
    } catch (err) {
      console.error('Chargement statistiques des notes impossible:', err);
      setNotesState({
        loading: false,
        error: err.message || 'Impossible de charger les statistiques des notes.',
        data: EMPTY_NOTE_STATS,
      });
    }
  };

  const loadDashboardData = async (showLoader = true) => {
    try {
      if (showLoader) {
        setLoading(true);
      }
      setError('');
      const dashboardResponse = await apiTableauDeBordProfesseur.obtenirTableauDeBord();
      setDashboard(dashboardResponse);

      const [nextSessionResult, presenceResult] = await Promise.allSettled([
        apiTableauDeBordProfesseur.obtenirProchaineSeance(),
        buildPresenceInsightsFromApp(),
      ]);

      const nextSessionApi =
        nextSessionResult.status === 'fulfilled' ? nextSessionResult.value?.prochaineSeance || null : null;
      const presenceData =
        presenceResult.status === 'fulfilled'
          ? presenceResult.value
          : {
              prochaineSeance: null,
              enseignementsActifs: 0,
              absencesJour: 0,
              feuillesACompleter: 0,
              etudiantsARisque: 0,
              justificatifsAConsulter: 0,
              coursSansSupport: 0,
              tauxPresence: null,
            };

      setPresenceInsights(presenceData);
      setProchaineSeance(pickNearestUpcomingSession(presenceData.prochaineSeance, nextSessionApi));
      setDerniereMiseAJour(new Date());
    } catch (err) {
      console.error('Chargement dashboard professeur impossible:', err);
      setError(err.message || 'Impossible de charger le tableau de bord professeur.');
    } finally {
      if (showLoader) {
        setLoading(false);
      }
    }
  };

  useEffect(() => {
    loadDashboardData();
    loadNotesStats();

    const interval = setInterval(() => {
      loadDashboardData(false);
      loadNotesStats(false);
    }, 60000);

    return () => clearInterval(interval);
  }, []);

  const reloadNoteStats = async () => {
    await loadNotesStats(true);
  };

  const dashboardData = dashboard || {};
  const entete = dashboardData.entete || {};
  const stats = dashboardData.stats || {};
  const sessionCard = normalizeSession(prochaineSeance);
  const dashboardAbsenceInsights = useMemo(() => buildAbsenceInsights(dashboardData), [dashboardData]);
  const prochainExamen = useMemo(() => {
    const dashboardUpcoming = Array.isArray(dashboardData.examensAVenir)
      ? dashboardData.examensAVenir
          .filter((examen) => {
            const examDate = getExamDate(examen);
            return examDate && examDate.getTime() >= Date.now() && !isExamCompleted(examen);
          })
          .sort((a, b) => (getExamDate(a)?.getTime() || Number.MAX_SAFE_INTEGER) - (getExamDate(b)?.getTime() || Number.MAX_SAFE_INTEGER))
      : [];

    return notesState.data.prochainExamen || dashboardUpcoming[0] || null;
  }, [dashboardData.examensAVenir, notesState.data.prochainExamen]);
  const absenceInsights = {
    ...dashboardAbsenceInsights,
    todayCount: presenceInsights.absencesJour,
    pendingCount: presenceInsights.justificatifsAConsulter,
    highRiskCount: presenceInsights.etudiantsARisque,
    attendanceRate:
      presenceInsights.tauxPresence != null
        ? presenceInsights.tauxPresence
        : dashboardAbsenceInsights.attendanceRate,
    hasDetails: true,
  };
  const noteMeta = notesState.data.meta || EMPTY_NOTE_STATS.meta;
  const activeNoteItems = useMemo(
    () => (activeNoteTab === 'classe' ? notesState.data.parClasse : notesState.data.parMatiere),
    [activeNoteTab, notesState.data.parClasse, notesState.data.parMatiere],
  );

  const activeSummary = useMemo(() => {
    const aggregate = activeNoteItems.reduce(
      (acc, item) => {
        if (Number.isFinite(item.moyenneGenerale)) {
          acc.moyenneSomme += item.moyenneGenerale * item.notesSaisies;
          acc.notesSaisies += item.notesSaisies;
        }
        if (Number.isFinite(item.noteMaximale)) acc.noteMaximale = acc.noteMaximale == null ? item.noteMaximale : Math.max(acc.noteMaximale, item.noteMaximale);
        if (Number.isFinite(item.noteMinimale)) acc.noteMinimale = acc.noteMinimale == null ? item.noteMinimale : Math.min(acc.noteMinimale, item.noteMinimale);
        acc.successCount += ((item.tauxReussite || 0) / 100) * item.notesSaisies;
        acc.totalAttendu += item.totalAttendu || 0;
        return acc;
      },
      { moyenneSomme: 0, notesSaisies: 0, noteMaximale: null, noteMinimale: null, successCount: 0, totalAttendu: 0 },
    );

    return {
      moyenneGenerale: aggregate.notesSaisies > 0 ? aggregate.moyenneSomme / aggregate.notesSaisies : notesState.data.resumeGlobal.moyenneGenerale,
      noteMaximale: aggregate.noteMaximale,
      noteMinimale: aggregate.noteMinimale,
      tauxReussite: aggregate.notesSaisies > 0 ? (aggregate.successCount / aggregate.notesSaisies) * 100 : notesState.data.resumeGlobal.tauxReussite,
      notesSaisies: aggregate.notesSaisies,
      totalAttendu: aggregate.totalAttendu,
    };
  }, [activeNoteItems, notesState.data.resumeGlobal]);

  const chartData = useMemo(
    () => activeNoteItems.slice(0, 5).map((item, index) => ({ ...item, fill: CHART_COLORS[index % CHART_COLORS.length] })),
    [activeNoteItems],
  );

  const bestPerformer = activeNoteItems.find((item) => Number.isFinite(item.moyenneGenerale)) || null;
  const weakPerformer =
    [...activeNoteItems].filter((item) => Number.isFinite(item.tauxReussite)).sort((a, b) => (a.tauxReussite || 0) - (b.tauxReussite || 0))[0] || null;

  const strategicKpis = [
    { label: 'Enseignements actifs', value: presenceInsights.enseignementsActifs, accent: 'indigo', icon: BookOpen },
    { label: 'Examens programmes', value: noteMeta.examensProgrammes, accent: 'amber', icon: ClipboardList },
    { label: 'Copies a corriger', value: noteMeta.copiesACorriger, accent: 'rose', icon: FileWarning },
    { label: 'Feuilles a finaliser', value: noteMeta.feuillesNotesACompleter, accent: 'teal', icon: ClipboardList },
    { label: 'Cours sans support', value: presenceInsights.coursSansSupport, accent: 'emerald', icon: AlertCircle },
  ];

  const alertEntries = [
    {
      key: 'today',
      title: 'Absences du jour',
      value: formatCompactNumber(absenceInsights.todayCount),
      detail:
        absenceInsights.todayCount > 0
          ? `${formatCompactNumber(absenceInsights.todayCount)} absence${absenceInsights.todayCount > 1 ? 's' : ''} enregistree${absenceInsights.todayCount > 1 ? 's' : ''} aujourd hui.`
          : 'Aucune absence detectee aujourd hui.',
      tone: absenceInsights.todayCount > 0 ? 'danger' : 'soft',
    },
    {
      key: 'notes',
      title: 'Feuilles de notes a completer',
      value: formatCompactNumber(noteMeta.feuillesNotesACompleter),
      detail:
        noteMeta.feuillesNotesACompleter > 0
          ? `${formatCompactNumber(noteMeta.feuillesNotesACompleter)} liste${noteMeta.feuillesNotesACompleter > 1 ? 's' : ''} ont encore des notes manquantes.`
          : 'Toutes les feuilles de notes sont completes.',
      tone: noteMeta.feuillesNotesACompleter > 0 ? 'danger' : 'soft',
    },
    {
      key: 'justifications',
      title: 'Justificatifs a consulter',
      value: formatCompactNumber(absenceInsights.pendingCount),
      detail:
        absenceInsights.pendingCount > 0
          ? `${formatCompactNumber(absenceInsights.pendingCount)} justificatif${absenceInsights.pendingCount > 1 ? 's' : ''} restent en attente de consultation.`
          : 'Aucun justificatif en attente.',
      tone: absenceInsights.pendingCount > 0 ? 'warning' : 'soft',
    },
  ];

  if (loading) {
    return (
      <div className="prof-dashboard">
        <div className="prof-feedback-block">
          <Loader2 size={30} className="prof-spin" />
          <p>Chargement du tableau de bord professeur...</p>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="prof-dashboard">
        <div className="prof-feedback-block prof-feedback-error">
          <AlertCircle size={28} />
          <p>{error}</p>
          <button type="button" onClick={() => window.location.reload()}>
            Recharger
          </button>
        </div>
      </div>
    );
  }

  return (
    <div className="prof-dashboard">
      <header className="prof-header-card">
        <div className="prof-header-main">
          <div className="prof-header-copy">
            <h1>Espace Professeur</h1>
            <p>{[entete?.specialite, entete?.departement].filter(Boolean).join(' | ') || 'Suivi academique et pedagogique en cours.'}</p>
            {derniereMiseAJour ? (
              <span className="dashboard-refresh-meta">
                {formaterDerniereMiseAJour(derniereMiseAJour)}
              </span>
            ) : null}
          </div>

          <div className="prof-session-status">
            <div className="prof-status-dot"></div>
            <span>{entete?.statut || 'SESSION ACTIVE'}</span>
          </div>
        </div>

      </header>

        <section className="prof-kpi-section">
      <div className="prof-kpi-grid">
        {strategicKpis.map((item) => {
          const Icon = item.icon;
          return (
            <article key={item.label} className={`prof-kpi-card accent-${item.accent}`}>
              <div className="prof-kpi-icon">
                <Icon size={18} />
              </div>
              <div className="prof-kpi-content">
                <span className="prof-kpi-label">{item.label}</span>
                <strong className="prof-kpi-value">{typeof item.value === 'string' ? item.value : formatCompactNumber(item.value)}</strong>
              </div>
            </article>
          );
        })}
      </div>
    </section>


      <section className="prof-main-layout">
        <div className="prof-analytics-column">
          <article className="prof-card prof-performance-card">
            <div className="prof-card-head">
              <div>
                <p className="prof-card-eyebrow">Performance academique</p>
                <h2>Performance academique</h2>
                <p className="prof-card-description">Vue synthetique des resultats par classe ou par matiere.</p>
              </div>

              <div className="prof-tab-group" role="tablist" aria-label="Vues statistiques">
                {NOTE_TABS.map((tab) => {
                  const Icon = tab.icon;
                  const isActive = activeNoteTab === tab.id;
                  return (
                    <button key={tab.id} type="button" className={`prof-tab ${isActive ? 'active' : ''}`} onClick={() => setActiveNoteTab(tab.id)}>
                      <Icon size={15} />
                      <span>{tab.label}</span>
                    </button>
                  );
                })}
              </div>
            </div>

            {notesState.loading ? (
              <div className="prof-performance-state">
                <Loader2 size={24} className="prof-spin" />
                <p>Chargement des statistiques des notes...</p>
              </div>
            ) : notesState.error ? (
              <div className="prof-performance-state error">
                <AlertCircle size={24} />
                <p>{notesState.error}</p>
                <button type="button" onClick={reloadNoteStats}>Reessayer</button>
              </div>
            ) : activeNoteItems.length === 0 ? (
              <div className="prof-performance-state">
                <BarChart3 size={24} />
                <p>Aucune note exploitable pour afficher une synthese.</p>
              </div>
            ) : (
              <>
                <div className="prof-performance-metrics">
                  <div className="prof-metric-card">
                    <span>Moyenne generale</span>
                    <strong>{formatFloat(activeSummary.moyenneGenerale)}/20</strong>
                  </div>
                  <div className="prof-metric-card">
                    <span>Meilleure performance</span>
                    <strong>{bestPerformer?.label || '--'}</strong>
                    <small>{bestPerformer ? `${formatFloat(bestPerformer.moyenneGenerale)}/20` : 'Aucune donnee'}</small>
                  </div>
                  <div className="prof-metric-card">
                    <span>Plus faible performance</span>
                    <strong>{weakPerformer?.label || '--'}</strong>
                    <small>{weakPerformer ? `${formatPercent(weakPerformer.tauxReussite)} de reussite` : 'Aucune donnee'}</small>
                  </div>
                  <div className="prof-metric-card">
                    <span>Taux de reussite</span>
                    <strong>{formatPercent(activeSummary.tauxReussite)}</strong>
                    <small>{activeSummary.notesSaisies}/{activeSummary.totalAttendu} notes saisies</small>
                  </div>
                </div>

                <div className="prof-performance-body">
                  <div className="prof-chart-panel">
                    <div className="prof-chart-shell">
                      <ResponsiveContainer width="100%" height="100%">
                        <BarChart data={chartData} layout="vertical" margin={{ top: 2, right: 4, left: 6, bottom: 2 }}>
                          <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="rgba(120, 137, 153, 0.16)" />
                          <XAxis type="number" domain={[0, 20]} axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12 }} />
                          <YAxis dataKey="label" type="category" width={102} axisLine={false} tickLine={false} tick={{ fill: '#334155', fontSize: 12 }} />
                          <Tooltip cursor={{ fill: 'rgba(92, 84, 237, 0.06)' }} content={<DashboardTooltip />} />
                          <Bar dataKey="moyenneGenerale" radius={[0, 12, 12, 0]} barSize={18}>
                            {chartData.map((entry) => <Cell key={entry.id} fill={entry.fill} />)}
                          </Bar>
                        </BarChart>
                      </ResponsiveContainer>
                    </div>
                  </div>
                </div>
              </>
            )}
          </article>
        </div>

        <div className="prof-side-column">
          <article className="prof-card prof-focus-card">
            <div className="prof-card-head">
              <div>
                <p className="prof-card-eyebrow">Maintenant</p>
                <h2>Prochaine seance</h2>
              </div>
            </div>

            {sessionCard ? (
              <div className="prof-session-body prof-session-body-student">
                <div className="prof-session-info-main">
                  <div className="prof-session-time">
                    {sessionCard.displayStart || '--:--'}
                    {sessionCard.displayEnd ? ` - ${sessionCard.displayEnd}` : ''}
                  </div>
                  <div className="prof-session-details-text">
                    <div className="prof-session-top">
                      <span className={`prof-type-pill tone-${TYPE_COLOR[sessionCard.displayType] || 'grey'}`}>{sessionCard.displayType}</span>
                      <span className="prof-class-pill">{sessionCard.displayClass}</span>
                    </div>
                    <h3>{sessionCard.displaySubject}</h3>
                    <div className="prof-session-meta-row">
                      <div className="prof-meta-chip">
                        <Calendar size={14} />
                        <span>{sessionCard.displayDay}</span>
                      </div>
                      <div className="prof-meta-chip">
                        <MapPin size={14} />
                        <span>{sessionCard.displayRoom}</span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
            ) : (
              <div className="prof-empty-state compact">
                <Clock size={24} />
                <p>Aucune seance a venir pour le moment.</p>
              </div>
            )}
          </article>

          <article className="prof-card prof-alert-card">
            <div className="prof-card-head">
              <div>
                <p className="prof-card-eyebrow">Suivi</p>
                <h2>Priorites du jour</h2>
              </div>
              <Bell size={18} className="prof-alert-icon" />
            </div>

            <div className="prof-priority-list">
              {alertEntries.map((entry) => (
                <div key={entry.key} className={`prof-priority-item tone-${entry.tone}`}>
                  <div className="prof-priority-main">
                    <div className="prof-priority-copy">
                      <div className="prof-priority-title-row">
                        <span className="prof-priority-dot"></span>
                        <strong>{entry.title}</strong>
                      </div>
                      <p>{entry.detail}</p>
                    </div>
                    <div className="prof-priority-value">{entry.value}</div>
                  </div>
                </div>
              ))}
            </div>

            <div className="prof-alert-list">
              <div className="prof-alert-item tone-danger">
                <div className="prof-alert-item-head">
                  <span className="prof-alert-dot"></span>
                  <strong>Absences du jour</strong>
                </div>
                <span className="prof-alert-value">{formatCompactNumber(absenceInsights.todayCount)}</span>
                <p>Nombre reel d'absences enregistrees sur les seances du jour.</p>
              </div>

              <div className="prof-alert-item tone-warning">
                <div className="prof-alert-item-head">
                  <span className="prof-alert-dot"></span>
                  <strong>Feuilles a completer</strong>
                </div>
                <span className="prof-alert-value">{formatCompactNumber(absenceInsights.pendingCount)}</span>
                <p>Seances avec presences encore incomplètes ou non chargees.</p>
              </div>

              <div className="prof-alert-item tone-neutral">
                <div className="prof-alert-item-head">
                  <span className="prof-alert-dot"></span>
                  <strong>Taux de presence global</strong>
                </div>
                <span className="prof-alert-value">{absenceInsights.attendanceRate != null ? `${Math.round(absenceInsights.attendanceRate)}%` : '--'}</span>
                <p>
                  {absenceInsights.attendanceRate != null
                    ? 'Moyenne calculee sur les feuilles de presence deja renseignees.'
                    : 'Le taux de presence sera visible des que des feuilles seront renseignees.'}
                </p>
              </div>
            </div>
          </article>

          <article className="prof-card prof-compact-card">
            <div className="prof-card-head prof-card-head--tight">
              <div>
                <p className="prof-card-eyebrow">A venir</p>
                <h2>Prochain examen</h2>
              </div>
              <ClipboardList size={18} />
            </div>
            {prochainExamen ? (
              <div className="prof-exam-body">
                <div className="prof-exam-title">
                  <strong>{prochainExamen.nomMatiere || 'Examen'}</strong>
                  <span>
                    {prochainExamen.nomClasse || prochainExamen.codeClasse || 'Classe'}
                    {prochainExamen.numeroExamen ? ` • n°${prochainExamen.numeroExamen}` : ''}
                  </span>
                </div>
                <div className="prof-exam-meta-row">
                  <div className="prof-meta-chip">
                    <Calendar size={14} />
                    <span>
                      {formatDate(prochainExamen.dateExamen || prochainExamen.date || prochainExamen.heureDebut)}{' '}
                      {formatHour(prochainExamen.dateExamen || prochainExamen.heureDebut)
                        ? `• ${formatHour(prochainExamen.dateExamen || prochainExamen.heureDebut)}`
                        : ''}
                      {prochainExamen.heureFin ? ` - ${formatHour(prochainExamen.heureFin)}` : ''}
                    </span>
                  </div>
                  <div className="prof-meta-chip">
                    <MapPin size={14} />
                    <span>{prochainExamen.salle || 'Salle à définir'}</span>
                  </div>
                </div>
              </div>
            ) : (
              <div className="prof-empty-state compact">
                <ClipboardList size={24} />
                <p>Aucun examen à venir.</p>
              </div>
            )}
          </article>

        </div>

      </section>

    </div>
  );
};

export default TableauDeBordProfesseur;
