import { useState, useEffect } from 'react';
import { Calendar, Search, ChevronRight, Clock, Clock3, Info } from 'lucide-react';
import { apiClasses } from '../services/apiClasses';
import { apiEmploiTemps } from '../services/apiEmploiTemps';
import { apiAuth } from '../services/apiAuth';
import TimetableGrid from '../composants/emploiDuTemps/TimetableGrid';
import './GestionEmploiTemps.css';

const GestionEmploiTemps = () => {
  const [seances, setSeances] = useState([]);
  const [classes, setClasses] = useState([]);
  const [selectedClasse, setSelectedClasse] = useState(null);
  const [isLoading, setIsLoading] = useState(false);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [searchQuery, setSearchQuery] = useState('');

  const user = apiAuth.getUser();
  const isAdmin = user?.role === 'admin' || user?.role === 'administrateur';

  // Load classes on mount
  useEffect(() => {
    const loadClasses = async () => {
      try {
        const data = await apiClasses.obtenirToutes();
        setClasses(data);
        if (data.length > 0) {
          setSelectedClasse(data[0]);
        }
      } catch (err) {
        console.error('Failed to load classes', err);
      }
    };
    loadClasses();
  }, []);

  // Load schedule when class changes
  useEffect(() => {
    if (selectedClasse) {
      const loadSchedule = async () => {
        setIsLoading(true);
        try {
          const data = await apiEmploiTemps.obtenirParClasse(selectedClasse.id);
          setSeances(data);
        } catch (err) {
          console.error('Failed to load schedule', err);
        } finally {
          setIsLoading(false);
        }
      };
      loadSchedule();
    }
  }, [selectedClasse]);

  const filteredClasses = classes.filter(c =>
    (c.nom?.toLowerCase() || '').includes(searchQuery.toLowerCase()) ||
    (c.codeClasse?.toLowerCase() || '').includes(searchQuery.toLowerCase())
  );

  return (
    <div className="gestion-emploi-temps-modern">
      {/* Header Banner */}
      <div className="planning-header-banner">
        <div className="header-left">
          <div className="header-icon-box">
            <Calendar size={28} color="white" />
          </div>
          <div className="header-titles">
            <h1>Planning Académique</h1>
            <div className="breadcrumb">
              <span>{isAdmin ? 'GESTION CENTRALISÉE' : 'PROFESSEUR'}</span>
              <span className="separator">›</span>
              <span className="active">EMPLOI DU TEMPS</span>
            </div>
          </div>
        </div>

        <div className="header-right">
          <div className="class-selector-pill" onClick={() => setIsDropdownOpen(!isDropdownOpen)}>
            <Search size={18} className="selector-icon" />
            <span className="selector-text">
              {selectedClasse ? `${selectedClasse.nom} • ${selectedClasse.codeClasse}` : 'Choisir une classe...'}
            </span>
            <ChevronRight size={18} className={`selector-arrow ${isDropdownOpen ? 'open' : ''}`} />
          </div>

          {isDropdownOpen && (
            <div className="selector-dropdown">
              <div className="dropdown-search">
                <input
                  type="text"
                  placeholder="Rechercher une classe..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  onClick={(e) => e.stopPropagation()}
                />
              </div>
              <div className="dropdown-list">
                {filteredClasses.map(c => (
                  <div
                    key={c.id}
                    className={`dropdown-item ${selectedClasse?.id === c.id ? 'active' : ''}`}
                    onClick={() => {
                      setSelectedClasse(c);
                      setIsDropdownOpen(false);
                    }}
                  >
                    {c.nom} • {c.codeClasse}
                  </div>
                ))}
                {filteredClasses.length === 0 && (
                  <div className="dropdown-no-results">Aucune classe trouvée</div>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {/* Timetable Grid Area */}
      <div className="timetable-main-area">
        {isLoading ? (
          <div className="timetable-loading">Chargement du planning...</div>
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
          <span>Dernière mise à jour: Aujourd'hui à {new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</span>
        </div>
      </div>
    </div>
  );
};

export default GestionEmploiTemps;
