import { useEffect, useState } from 'react';
import {
  AlertCircle,
  Award,
  BarChart3,
  Bell,
  BookOpen,
  ChevronRight,
  Circle,
  Clock,
  Layers3,
  MapPin,
  TrendingUp
} from 'lucide-react';
import { apiEtudiant } from '../services/apiEtudiant';
import { apiGrades } from '../services/apiGrades';
import './TableauDeBordEtudiant.css';

const EMPTY_GRADE_INSIGHTS = {
  globalAverage: null,
  validatedModules: 0,
  totalModules: 0,
  classSnapshots: [],
  subjectStats: []
};

const formatGrade = (value) => (Number.isFinite(value) ? value.toFixed(2) : '--');

const getGradeTone = (value) => {
  if (!Number.isFinite(value)) return 'neutral';
  if (value >= 14) return 'strong';
  if (value >= 10) return 'good';
  return 'alert';
};

const TableauDeBordEtudiant = ({ surNavigation }) => {
  const [dashboardData, setDashboardData] = useState(null);
  const [gradeInsights, setGradeInsights] = useState(EMPTY_GRADE_INSIGHTS);
  const [annonces, setAnnonces] = useState([]);
  const [nextSession, setNextSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchGradeInsights = async () => {
      try {
        const globalStats = await apiGrades.fetchGlobalStats();
        const semesterNumbers = globalStats?.availableSemesters || [1];
        const semesterResults = await Promise.all(
          semesterNumbers.map((semester) => apiGrades.fetchStudentGrades(semester))
        );

        const validSemesters = semesterResults
          .map((semesterData, index) => ({
            semesterNumber: semesterNumbers[index],
            semesterData
          }))
          .filter(({ semesterData }) => semesterData && Array.isArray(semesterData.modules));

        const classSnapshots = validSemesters.map(({ semesterNumber, semesterData }) => {
          const modules = semesterData.modules || [];
          const validatedCount = modules.filter((module) => Number(module.moyenneDuModule) >= 10).length;
          const computedAverage = modules.length
            ? modules.reduce((sum, module) => sum + (Number(module.moyenneDuModule) || 0), 0) / modules.length
            : 0;
          const average = Number(semesterData.moyenneSession) || computedAverage;

          return {
            id: `S${semesterNumber}`,
            label: `Semestre ${semesterNumber}`,
            average,
            validatedCount,
            moduleCount: modules.length
          };
        });

        const subjectMap = new Map();
        validSemesters.forEach(({ semesterData, semesterNumber }) => {
          (semesterData.modules || []).forEach((module, moduleIndex) => {
            const moduleName = module.nomDuModule || `Module ${moduleIndex + 1}`;
            const moduleAverage = Number(module.moyenneDuModule) || 0;
            const subjects = Array.isArray(module.matieres) && module.matieres.length > 0
              ? module.matieres
              : [{ nomDuMatiere: moduleName, moyenneDuMatiere: moduleAverage }];

            subjects.forEach((subject, subjectIndex) => {
              const subjectName = subject.nomDuMatiere || `${moduleName} ${subjectIndex + 1}`;
              const rawScore = Number(subject.moyenneDuMatiere);
              const score = Number.isFinite(rawScore) ? rawScore : moduleAverage;
              const key = `${moduleName}-${subjectName}`;
              const existing = subjectMap.get(key) || {
                id: key,
                moduleName,
                subjectName,
                total: 0,
                count: 0,
                latestSemester: semesterNumber,
                latestScore: score
              };

              existing.total += score;
              existing.count += 1;
              if (semesterNumber >= existing.latestSemester) {
                existing.latestSemester = semesterNumber;
                existing.latestScore = score;
              }

              subjectMap.set(key, existing);
            });
          });
        });

        const subjectStats = Array.from(subjectMap.values())
          .map((subject) => ({
            ...subject,
            average: subject.count > 0 ? subject.total / subject.count : 0
          }))
          .sort((a, b) => b.average - a.average)
          .slice(0, 6);

        setGradeInsights({
          globalAverage: Number(globalStats?.generalAverage) || null,
          validatedModules: globalStats?.validatedModules || 0,
          totalModules: globalStats?.totalModules || 0,
          classSnapshots,
          subjectStats
        });
      } catch (gradesError) {
        console.warn('Failed to fetch grade insights:', gradesError);
        setGradeInsights(EMPTY_GRADE_INSIGHTS);
      }
    };

    const fetchData = async () => {
      try {
        setLoading(true);

        const dashboardResponse = await apiEtudiant.getDashboardData();
        setDashboardData(dashboardResponse);

        await fetchGradeInsights();

        try {
          const nextSessionResponse = await apiEtudiant.getNextSession();
          setNextSession(nextSessionResponse);
        } catch (nextSessionError) {
          console.warn('Failed to fetch next session:', nextSessionError);
          setNextSession(null);
        }

        try {
          const annoncesResponse = await apiEtudiant.getAnnonces();
          setAnnonces(annoncesResponse || []);
        } catch (annoncesError) {
          console.warn('Failed to fetch announcements, continuing without them:', annoncesError);
          setAnnonces([]);
        }
      } catch (err) {
        console.error('Error fetching dashboard data:', err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  if (loading) {
    return (
      <div className="tableau-de-bord-etudiant">
        <div className="loading-container">
          <div className="loading-spinner">Chargement...</div>
        </div>
      </div>
    );
  }

  if (error) {
    return (
      <div className="tableau-de-bord-etudiant">
        <div className="error-container">
          <AlertCircle size={24} />
          <p>Erreur: {error}</p>
        </div>
      </div>
    );
  }

  const getTimeAgo = (dateString) => {
    const now = new Date();
    const date = new Date(dateString);
    const diffInHours = Math.floor((now - date) / (1000 * 60 * 60));

    if (diffInHours < 1) return 'IL Y A 1H';
    if (diffInHours < 24) return `IL Y A ${diffInHours}H`;
    const diffInDays = Math.floor(diffInHours / 24);
    return `IL Y A ${diffInDays}J`;
  };

  return (
    <div className="tableau-de-bord-etudiant">
      <div className="etudiant-header">
        <div className="header-content">
          <div className="header-info">
            <h1>Espace Etudiant</h1>
            <p>
              {dashboardData?.entete
                ? `${dashboardData.entete.niveau || ''} ${dashboardData.entete.filiere || ''} • ${dashboardData.entete.session || 'Session en cours'}`
                : 'Parcours academique • Session en cours'}
            </p>
          </div>
          <div className="session-status">
            <div className="status-indicator"></div>
            <span>{dashboardData?.entete?.statut || 'SESSION ACTIVE'}</span>
          </div>
        </div>
      </div>

      <div className="dashboard-grid">
        <div className="left-column">
          <div className="announcements-section">
            <div className="announcements-header">
              <div className="section-label">
                <Bell className="label-icon" size={20} />
                <h3>Annonces de vos professeurs</h3>
              </div>
              <div className="announcements-badge">
                {annonces.length > 0 ? `${annonces.length} nouvelles` : '0 nouvelle'}
              </div>
            </div>

            <div className="announcements-container">
              {annonces.length > 0 ? (
                <div className="annonces-list">
                  {annonces.slice(0, 3).map((annonce) => {
                    const timeAgo = getTimeAgo(annonce.datePublication);
                    return (
                      <div
                        key={annonce.id}
                        className="annonce-box"
                        onClick={() => surNavigation('annonces')}
                      >
                        <div className="annonce-box-header">
                          <div className="annonce-left">
                            <div className="annonce-indicator">
                              <Circle size={8} fill="#ef4444" color="#ef4444" />
                            </div>
                            <div className="annonce-info">
                              <h4 className="annonce-title">{annonce.titre}</h4>
                              <div className="annonce-details">
                                <span className="annonce-category">{annonce.categorie || 'EXAMEN'}</span>
                                <span className="annonce-time">{timeAgo}</span>
                              </div>
                            </div>
                          </div>
                          <ChevronRight className="annonce-arrow" size={20} color="#94a3b8" />
                        </div>
                      </div>
                    );
                  })}
                </div>
              ) : (
                <>
                  <Bell className="no-announcements-icon" size={48} />
                  <p>Aucune annonce pour votre classe pour le moment.</p>
                </>
              )}
            </div>
          </div>

          <div className="bottom-row">
            <div className="card-modern attendance-card">
              <div className="section-label">
                <TrendingUp className="label-icon" size={20} />
                <h3>Assiduite globale</h3>
              </div>

              <div className="attendance-content">
                <div className="gauge-container">
                  <svg width="100" height="100" viewBox="0 0 100 100">
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      fill="none"
                      stroke="#f1f5f9"
                      strokeWidth="8"
                    />
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      fill="none"
                      stroke="#5c54ed"
                      strokeWidth="8"
                      strokeDasharray={`${2 * Math.PI * 40 * (dashboardData?.assiduite?.tauxPresence / 100 || 0.85)} ${2 * Math.PI * 40 * (1 - (dashboardData?.assiduite?.tauxPresence / 100 || 0.85))}`}
                      strokeDashoffset="0"
                      transform="rotate(-90 50 50)"
                      strokeLinecap="round"
                    />
                    <circle
                      cx="50"
                      cy="50"
                      r="40"
                      fill="none"
                      stroke="#ef4444"
                      strokeWidth="8"
                      strokeDasharray={`${2 * Math.PI * 40 * 0.05} ${2 * Math.PI * 40 * 0.95}`}
                      strokeDashoffset={`${-2 * Math.PI * 40 * (dashboardData?.assiduite?.tauxPresence / 100 || 0.85)}`}
                      transform="rotate(-90 50 50)"
                      strokeLinecap="round"
                    />
                  </svg>
                  <div className="gauge-percentage">
                    {Math.round(dashboardData?.assiduite?.tauxPresence || 85)}%
                  </div>
                </div>
                <div className="attendance-details">
                  <p>Presence validee</p>
                  <span className="excellent-tag">
                    {dashboardData?.assiduite?.labelQualite || 'EXCELLENT'}
                  </span>
                </div>
              </div>
            </div>

            <div className="card-modern next-session-card">
              <h3>Prochaine seance</h3>
              <div className="session-info-main">
                {nextSession?.prochaineSeance ? (
                  <>
                    <div className="session-time">
                      {nextSession.prochaineSeance.start} - {nextSession.prochaineSeance.end}
                    </div>
                    <div className="session-details-text">
                      <h4>{nextSession.prochaineSeance.matiereNom || 'Matiere inconnue'}</h4>
                      <div className="session-meta">
                        <MapPin size={14} />
                        <span>{nextSession.prochaineSeance.salle || 'Salle inconnue'}</span>
                      </div>
                      <div className="session-meta">
                        <Clock size={14} />
                        <span>{nextSession.prochaineSeance.dayOfWeek || 'Jour inconnu'}</span>
                      </div>
                    </div>
                  </>
                ) : (
                  <>
                    <div className="session-time">--:--</div>
                    <div className="session-details-text">
                      <h4>Non disponible</h4>
                      <div className="session-meta">
                        <MapPin size={14} />
                        <span>Aucune seance a venir</span>
                      </div>
                    </div>
                  </>
                )}
              </div>
            </div>
          </div>
        </div>

        <div className="grades-sidebar card-modern">
          <div className="grades-header grade-insights-header">
            <div className="section-title-stack">
              <div className="section-label compact">
                <BarChart3 className="label-icon" size={20} />
                <h3>Statistiques des notes</h3>
              </div>
              <p>Une lecture plus utile de vos resultats, par semestre et par matiere.</p>
            </div>
            <button className="inline-link-btn" onClick={() => surNavigation('notes')}>
              Voir tous les bulletins
            </button>
          </div>

          <div className="grade-summary-strip">
            <div className="summary-chip">
              <span>Moyenne generale</span>
              <strong>{formatGrade(gradeInsights.globalAverage)}</strong>
            </div>
            <div className="summary-chip">
              <span>Modules valides</span>
              <strong>{gradeInsights.validatedModules}/{gradeInsights.totalModules}</strong>
            </div>
          </div>

          <div className="insights-block">
            <div className="insights-block-header">
              <Layers3 size={18} />
              <h4>Par classe / semestre</h4>
            </div>
            <div className="semester-stats-list">
              {gradeInsights.classSnapshots.length > 0 ? (
                gradeInsights.classSnapshots.map((snapshot) => (
                  <div key={snapshot.id} className="semester-stat-card">
                    <div className="semester-stat-top">
                      <div>
                        <span className="semester-label">{snapshot.label}</span>
                        <strong>{formatGrade(snapshot.average)}/20</strong>
                      </div>
                      <span className={`grade-badge ${getGradeTone(snapshot.average)}`}>
                        {snapshot.validatedCount}/{snapshot.moduleCount} valides
                      </span>
                    </div>
                    <div className="semester-progress-track">
                      <div
                        className={`semester-progress-fill ${getGradeTone(snapshot.average)}`}
                        style={{ width: `${Math.max(8, Math.min((snapshot.average / 20) * 100, 100))}%` }}
                      />
                    </div>
                  </div>
                ))
              ) : (
                <p className="empty-insight">Aucune statistique de semestre disponible.</p>
              )}
            </div>
          </div>

          <div className="insights-block">
            <div className="insights-block-header">
              <Award size={18} />
              <h4>Par matiere</h4>
            </div>
            <div className="subject-stats-list">
              {gradeInsights.subjectStats.length > 0 ? (
                gradeInsights.subjectStats.map((subject) => (
                  <div key={subject.id} className="subject-stat-row">
                    <div className="subject-stat-main">
                      <div className="grade-icon-box">
                        <BookOpen size={18} />
                      </div>
                      <div className="subject-stat-text">
                        <h5>{subject.subjectName}</h5>
                        <span>{subject.moduleName} • S{subject.latestSemester}</span>
                      </div>
                    </div>
                    <div className="subject-stat-score">
                      <strong>{formatGrade(subject.average)}</strong>
                      <span className={`grade-badge ${getGradeTone(subject.latestScore)}`}>
                        Derniere note {formatGrade(subject.latestScore)}
                      </span>
                    </div>
                  </div>
                ))
              ) : (
                <p className="empty-insight">Aucune statistique de matiere disponible.</p>
              )}
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};

export default TableauDeBordEtudiant;
