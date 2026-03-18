import { useState, useEffect } from 'react';
import { Bell, GraduationCap, Clock, MapPin, TrendingUp, AlertCircle, BookOpen, ChevronRight, Circle } from 'lucide-react';
import { apiEtudiant } from '../services/apiEtudiant';
import './TableauDeBordEtudiant.css';

const TableauDeBordEtudiant = ({ surNavigation }) => {
  console.log('DEBUG: TableauDeBordEtudiant component rendered');

  const [dashboardData, setDashboardData] = useState(null);
  const [annonces, setAnnonces] = useState([]);
  const [nextSession, setNextSession] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      try {
        setLoading(true);
        
        // Fetch dashboard data first
        const dashboardResponse = await apiEtudiant.getDashboardData();
        setDashboardData(dashboardResponse);
        
        // Fetch next session
        try {
          const nextSessionResponse = await apiEtudiant.getNextSession();
          console.log('Next session response:', nextSessionResponse);
          setNextSession(nextSessionResponse);
        } catch (nextSessionError) {
          console.warn('Failed to fetch next session:', nextSessionError);
          setNextSession(null);
        }
        
        // Try to fetch announcements, but don't fail the entire dashboard if it fails
        try {
          const annoncesResponse = await apiEtudiant.getAnnonces();
          setAnnonces(annoncesResponse || []);
        } catch (annoncesError) {
          console.warn('Failed to fetch announcements, continuing without them:', annoncesError);
          setAnnonces([]); // Set empty array as fallback
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

  // Helper function to calculate time ago
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
      {/* Header Section */}
      <div className="etudiant-header">
        <div className="header-content">
          <div className="header-info">
            <h1>Espace Etudiant</h1>
            <p>
              {dashboardData?.entete ? 
                `${dashboardData.entete.niveau || ''} ${dashboardData.entete.filiere || ''} | ${dashboardData.entete.session || 'Session d\'Automne 2023'}` : 
                'L3 Informatique | Session d\'Automne 2023'
              }
            </p>
          </div>
          <div className="session-status">
            <div className="status-indicator"></div>
            <span>{dashboardData?.entete?.statut || 'SESSION ACTIVE'}</span>
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="dashboard-grid">
        {/* Left column for Announcements and Bottom Cards */}
        <div className="left-column">
          {/* Announcements Section */}
          <div className="announcements-section">
            <div className="announcements-header">
              <div className="section-label">
                <Bell className="label-icon" size={20} />
                <h3>Annonces de vos Professeurs</h3>
              </div>
              <div className="announcements-badge">
                {annonces.length > 0 ? `${annonces.length} NOUVELLES` : '0 NOUVELLES'}
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

          {/* Bottom Row with Attendance and Next Session */}
          <div className="bottom-row">
            {/* Attendance Card */}
            <div className="card-modern attendance-card">
              <div className="section-label">
                <TrendingUp className="label-icon" size={20} />
                <h3>Taux de presence</h3>
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

            {/* Next Session Card */}
            <div className="card-modern next-session-card">
              <h3>Prochaine Seance</h3>
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

        {/* Right column for Grades */}
        <div className="grades-sidebar card-modern">
          <div className="grades-header">
            <GraduationCap className="label-icon" size={22} />
            <h3>Dernieres Notes</h3>
          </div>

          <div className="grades-list">
            {dashboardData?.dernieresNotes?.length > 0 ? (
              dashboardData.dernieresNotes.map((grade, index) => (
                <div key={index} className="grade-list-item">
                  <div className="grade-icon-box">
                    <BookOpen size={20} />
                  </div>
                  <div className="grade-subject-info">
                    <h4>{grade.matiere}</h4>
                    <div className="grade-date">
                      {(() => {
                        if (!grade.datePublication) return 'DATE INCONNUE';
                        const date = new Date(grade.datePublication);
                        return isNaN(date.getTime()) ? 'DATE INCONNUE' : date.toISOString().split('T')[0];
                      })()}
                    </div>
                  </div>
                  <div className="grade-number">{grade.note}</div>
                </div>
              ))
            ) : null}
          </div>

          <button className="view-bulletins-btn" onClick={() => surNavigation('notes')}>
            VOIR TOUS LES BULLETINS
          </button>
        </div>
      </div>
    </div>
  );
};

export default TableauDeBordEtudiant;

