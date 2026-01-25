import { useState, useEffect } from 'react';
import { Calendar, Clock3 } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import { apiEmploiTemps } from '../services/apiEmploiTemps';
import TimetableGrid from '../composants/emploiDuTemps/TimetableGrid';
import './CalendrierAcademiqueProfesseur.css';

const CalendrierAcademiqueProfesseur = () => {
  const [seances, setSeances] = useState([]);
  const [isLoading, setIsLoading] = useState(false);
  const user = apiAuth.getUser();

  useEffect(() => {
    const loadSchedule = async () => {
      if (!user) return;
      setIsLoading(true);
      try {
        // Use PPR or ID depending on your API implementation
        const profId = user.ppr || user.id;
        const data = await apiEmploiTemps.obtenirParProfesseur(profId);
        setSeances(data);
      } catch (err) {
        console.error('Failed to load professor schedule', err);
      } finally {
        setIsLoading(false);
      }
    };
    loadSchedule();
  }, []);

  return (
    <div className="gestion-emploi-temps-modern">
      {/* Header Banner */}
      <div className="planning-header-banner">
        <div className="header-left">
          <div className="header-icon-box">
            <Calendar size={28} color="white" />
          </div>
          <div className="header-titles">
            <h1>Mon Planning Académique</h1>
            <div className="breadcrumb">
              <span>PROFESSEUR</span>
              <span className="separator">›</span>
              <span className="active">EMPLOI DU TEMPS</span>
            </div>
          </div>
        </div>

        <div className="header-right">
          <div className="professor-badge-pill">
            <div className="prof-avatar-small">
              {user?.prenom?.[0] || 'P'}{user?.nom?.[0] || 'R'}
            </div>
            <span className="prof-name-text">
              Pr. {user?.prenom} {user?.nom}
            </span>
          </div>
        </div>
      </div>

      {/* Timetable Grid Area */}
      <div className="timetable-main-area">
        {isLoading ? (
          <div className="timetable-loading">Chargement de votre planning...</div>
        ) : (
          <TimetableGrid seances={seances} />
        )}
      </div>

      {/* Footer Legend Bar */}
      <div className="planning-footer-legend">
        <div className="legend-items">
          <div className="legend-item">
            <span className="dot dot-tronc"></span>
            <span className="label">TRONC COMMUN</span>
          </div>
          <div className="legend-item">
            <span className="dot dot-tp"></span>
            <span className="label">TRAVAUX PRATIQUES</span>
          </div>
          <div className="legend-item">
            <span className="dot dot-exam"></span>
            <span className="label">EXAMENS</span>
          </div>
        </div>

        <div className="footer-timestamp">
          <Clock3 size={16} />
          <span>Mis à jour le {new Date().toLocaleDateString('fr-FR')}</span>
        </div>
      </div>
    </div>
  );
};

export default CalendrierAcademiqueProfesseur;
