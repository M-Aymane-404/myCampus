import { useEffect, useMemo, useState } from 'react';
import {
  AlertCircle,
  BarChart3,
  BookOpen,
  Calendar,
  CheckCircle2,
  FileWarning,
  ClipboardList,
  Clock,
  GraduationCap,
  Layers3,
  Loader2,
  MapPin,
  Trophy,
  Users,
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
import './TableauDeBordProfesseur.css';

const TYPE_COLOR = { COURS: 'blue', TD: 'green', TP: 'purple' };

const NOTE_TABS = [
  { id: 'classe', label: 'Par classe', icon: Layers3 },
  { id: 'matiere', label: 'Par matiere', icon: BookOpen },
];

const CHART_COLORS = ['#5c54ed', '#3d7ea6', '#60a5fa', '#8b5cf6', '#f4a261', '#64748b'];

const EMPTY_NOTE_STATS = {
  parClasse: [],
  parMatiere: [],
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
    notesPubliees: 0,
    notesEnAttente: 0,
    examensSansPublication: 0,
    copiesACorriger: 0,
    groupesEnDifficulte: 0,
  },
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

const timeAgo = (value) => {
  if (!value) return 'RECENT';
  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return 'RECENT';
  const diffMs = Date.now() - date.getTime();
  const diffHours = Math.floor(diffMs / 3600000);
  if (diffHours < 1) return 'RECENT';
  if (diffHours < 24) return `IL Y A ${diffHours}H`;
  return `IL Y A ${Math.floor(diffHours / 24)}J`;
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
  let notesPubliees = 0;
  let notesEnAttente = 0;
  let examensSansPublication = 0;
  let copiesACorriger = 0;

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

          await Promise.all(
            examensList.map(async (examen) => {
              const examenId = examen.idExamen || examen.id;
              if (!examenId) return;

              const feuille = await apiNotesProfesseur.chargerFeuille(codeClasse, examenId);
              const lignes = Array.isArray(feuille?.lignes) ? feuille.lignes : [];
              const notes = [];

              lignes.forEach((ligne) => {
                const value = ligne.note;
                const numeric = typeof value === 'string' ? Number(value.replace(',', '.')) : Number(value);
                if (Number.isFinite(numeric)) notes.push(numeric);

                const studentId = ligne.idEtudiant || ligne.apogee;
                if (studentId) studentIds.add(studentId);

                if (ligne.publiee) notesPubliees += 1;
                else notesEnAttente += 1;
              });

              if (lignes.some((ligne) => !ligne.publiee)) examensSansPublication += 1;
              if (examen.etat === 'PASSE') copiesACorriger += 1;

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

  return {
    parClasse,
    parMatiere,
    resumeGlobal: finalizeAggregate(globalAggregate, 'global'),
    meta: {
      uniqueStudentCount: studentIds.size,
      totalClasses: parClasse.length,
      totalMatieres: parMatiere.length,
      notesPubliees,
      notesEnAttente,
      examensSansPublication,
      copiesACorriger,
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

const TableauDeBordProfesseur = ({ surNavigation }) => {
  const [dashboard, setDashboard] = useState(null);
  const [prochaineSeance, setProchaineSeance] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [activeNoteTab, setActiveNoteTab] = useState('classe');
  const [notesState, setNotesState] = useState({ loading: true, error: '', data: EMPTY_NOTE_STATS });

  useEffect(() => {
    const loadNotesStats = async () => {
      try {
        setNotesState((current) => ({ ...current, loading: true, error: '' }));
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

    const loadDashboard = async () => {
      try {
        setLoading(true);
        setError('');
        const dashboardResponse = await apiTableauDeBordProfesseur.obtenirTableauDeBord();
        setDashboard(dashboardResponse);

        const [nextSessionResult] = await Promise.allSettled([
          apiTableauDeBordProfesseur.obtenirProchaineSeance(),
        ]);

        setProchaineSeance(nextSessionResult.status === 'fulfilled' ? nextSessionResult.value?.prochaineSeance || null : null);
      } catch (err) {
        console.error('Chargement dashboard professeur impossible:', err);
        setError(err.message || 'Impossible de charger le tableau de bord professeur.');
      } finally {
        setLoading(false);
      }
    };

    loadDashboard();
    loadNotesStats();
  }, []);

  const reloadNoteStats = async () => {
    try {
      setNotesState((current) => ({ ...current, loading: true, error: '' }));
      const noteStats = await buildNoteStatistics();
      setNotesState({ loading: false, error: '', data: noteStats });
    } catch (err) {
      setNotesState({
        loading: false,
        error: err.message || 'Impossible de charger les statistiques des notes.',
        data: EMPTY_NOTE_STATS,
      });
    }
  };

  const dashboardData = dashboard || {};
  const entete = dashboardData.entete || {};
  const stats = dashboardData.stats || {};
  const examensAVenir = Array.isArray(dashboardData.examensAVenir) ? dashboardData.examensAVenir.slice(0, 3) : [];
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
    { label: 'Enseignements actifs', value: stats?.nbEnseignements ?? 0, accent: 'indigo', icon: BookOpen },
    { label: 'Etudiants concernes', value: noteMeta.uniqueStudentCount, accent: 'teal', icon: Users },
    { label: 'Examens programmes', value: stats?.nbExamensProgrammes ?? 0, accent: 'amber', icon: ClipboardList },
    { label: 'Copies a corriger', value: noteMeta.copiesACorriger, accent: 'rose', icon: FileWarning },
    { label: 'Taux de reussite global', value: formatPercent(notesState.data.resumeGlobal?.tauxReussite), accent: 'emerald', icon: Trophy },
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
        <div className="prof-side-column">
          <article className="prof-card prof-focus-card">
            <div className="prof-card-head">
              <div>
                <p className="prof-card-eyebrow">Maintenant</p>
                <h2>Prochaine seance</h2>
              </div>
            </div>

            {prochaineSeance ? (
              <div className="prof-session-body">
                <div className="prof-session-top">
                  <span className={`prof-type-pill tone-${TYPE_COLOR[prochaineSeance.type] || 'grey'}`}>{prochaineSeance.type || 'COURS'}</span>
                  <span className="prof-class-pill">{prochaineSeance.nomClasse || prochaineSeance.classe || 'Classe'}</span>
                </div>
                <h3>{prochaineSeance.nomMatiere || prochaineSeance.matiere || 'Matiere non precisee'}</h3>
                <div className="prof-session-grid">
                  <div className="prof-meta-chip"><Calendar size={14} /><span>{prochaineSeance.dayOfWeek || formatDate(prochaineSeance.date)}</span></div>
                  <div className="prof-meta-chip"><Clock size={14} /><span>{prochaineSeance.start || formatHour(prochaineSeance.heureDebut)}{(prochaineSeance.end || prochaineSeance.heureFin) ? ` | ${prochaineSeance.end || formatHour(prochaineSeance.heureFin)}` : ''}</span></div>
                  <div className="prof-meta-chip"><MapPin size={14} /><span>{prochaineSeance.salle || prochaineSeance.room || 'Salle a definir'}</span></div>
                  <div className="prof-meta-chip"><BookOpen size={14} /><span>{prochaineSeance.nomMatiere || prochaineSeance.matiere || 'Matiere'}</span></div>
                </div>
              </div>
            ) : (
              <div className="prof-empty-state compact">
                <Clock size={24} />
                <p>Aucune seance a venir pour le moment.</p>
              </div>
            )}
          </article>

          <article className="prof-card prof-compact-card">
            <div className="prof-card-head">
              <div>
                <p className="prof-card-eyebrow">A surveiller</p>
                <h2>Examens a venir</h2>
              </div>
            </div>

            {examensAVenir.length > 0 ? (
              <div className="prof-compact-list">
                {examensAVenir.map((examen) => (
                  <div key={examen.id || `${examen.nomMatiere}-${examen.numeroExamen}`} className="prof-compact-row">
                    <div className="prof-compact-icon warning"><ClipboardList size={16} /></div>
                    <div className="prof-compact-content">
                      <strong>{examen.nomMatiere || 'Examen'}</strong>
                      <span>{examen.nomClasse || examen.codeClasse || 'Classe'} | #{examen.numeroExamen || '--'} | {examen.dateExamen ? formatDate(examen.dateExamen) : 'Date a definir'}</span>
                    </div>
                  </div>
                ))}
              </div>
            ) : (
              <div className="prof-empty-state compact">
                <ClipboardList size={24} />
                <p>Aucun examen programme.</p>
              </div>
            )}
          </article>

        </div>

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
                        <BarChart data={chartData} layout="vertical" margin={{ top: 4, right: 6, left: 10, bottom: 4 }}>
                          <CartesianGrid strokeDasharray="3 3" horizontal={false} stroke="rgba(120, 137, 153, 0.16)" />
                          <XAxis type="number" domain={[0, 20]} axisLine={false} tickLine={false} tick={{ fill: '#64748b', fontSize: 12 }} />
                          <YAxis dataKey="label" type="category" width={118} axisLine={false} tickLine={false} tick={{ fill: '#334155', fontSize: 12 }} />
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
      </section>

    </div>
  );
};

export default TableauDeBordProfesseur;
