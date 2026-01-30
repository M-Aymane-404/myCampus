import { Bell, GraduationCap, Clock, MapPin, TrendingUp, AlertCircle, BookOpen } from 'lucide-react';
import './TableauDeBordEtudiant.css';

const TableauDeBordEtudiant = ({ surNavigation }) => {
  console.log('DEBUG: TableauDeBordEtudiant component rendered');

  return (
    <div className="tableau-de-bord-etudiant">
      {/* Header Section */}
      <div className="etudiant-header">
        <div className="header-content">
          <div className="header-info">
            <h1>Espace Étudiant</h1>
            <p>L3 Informatique • Session d'Automne 2023</p>
          </div>
          <div className="session-status">
            <div className="status-indicator"></div>
            <span>SESSION ACTIVE</span>
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
              <div className="announcements-badge">0 NOUVELLES</div>
            </div>

            <div className="announcements-container">
              <Bell className="no-announcements-icon" size={48} />
              <p>Aucune annonce pour votre classe pour le moment.</p>
            </div>
          </div>

          {/* Bottom Row with Attendance and Next Session */}
          <div className="bottom-row">
            {/* Attendance Card */}
            <div className="card-modern attendance-card">
              <div className="section-label">
                <TrendingUp className="label-icon" size={20} />
                <h3>Assiduité Globale</h3>
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
                      strokeDasharray={`${2 * Math.PI * 40 * 0.85} ${2 * Math.PI * 40 * 0.15}`}
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
                      strokeDashoffset={`${-2 * Math.PI * 40 * 0.85}`}
                      transform="rotate(-90 50 50)"
                      strokeLinecap="round"
                    />
                  </svg>
                  <div className="gauge-percentage">85%</div>
                </div>
                <div className="attendance-details">
                  <p>Présence validée</p>
                  <span className="excellent-tag">EXCELLENT</span>
                </div>
              </div>
            </div>

            {/* Next Session Card */}
            <div className="card-modern next-session-card">
              <h3>Prochaine Séance</h3>
              <div className="session-info-main">
                <div className="session-time">09:00</div>
                <div className="session-details-text">
                  <h4>Algorithmique</h4>
                  <div className="session-meta">
                    <MapPin size={14} />
                    <span>B204 • COURS</span>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

        {/* Right column for Grades */}
        <div className="grades-sidebar card-modern">
          <div className="grades-header">
            <GraduationCap className="label-icon" size={22} />
            <h3>Dernières Notes</h3>
          </div>

          <div className="grades-list">
            <div className="grade-list-item">
              <div className="grade-icon-box">
                <BookOpen size={20} />
              </div>
              <div className="grade-subject-info">
                <h4>Algorithmique</h4>
                <div className="grade-date">IL Y A 2 JOURS</div>
              </div>
              <div className="grade-number">16.5</div>
            </div>

            <div className="grade-list-item">
              <div className="grade-icon-box">
                <BookOpen size={20} />
              </div>
              <div className="grade-subject-info">
                <h4>Mathématiques</h4>
                <div className="grade-date">IL Y A 5 JOURS</div>
              </div>
              <div className="grade-number">14</div>
            </div>

            <div className="grade-list-item">
              <div className="grade-icon-box">
                <BookOpen size={20} />
              </div>
              <div className="grade-subject-info">
                <h4>Physique</h4>
                <div className="grade-date">LA SEMAINE DERNIÈRE</div>
              </div>
              <div className="grade-number">12</div>
            </div>
          </div>

          <button className="view-bulletins-btn">
            VOIR TOUS LES BULLETINS
          </button>
        </div>
      </div>
    </div>
  );
};

export default TableauDeBordEtudiant;
