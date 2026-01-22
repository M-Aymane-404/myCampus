import { useState } from 'react';
import { Calendar, Clock, MapPin, BookOpen } from 'lucide-react';
import './CalendrierAcademiqueProfesseur.css';

const CalendrierAcademiqueProfesseur = () => {
  // Mock academic events data
  const [evenements] = useState([
    {
      id: 1,
      titre: 'Start of Semester',
      date: '2024-09-15',
      type: 'academic',
      description: 'Beginning of Fall Semester 2024'
    },
    {
      id: 2,
      titre: 'Midterm Exams',
      date: '2024-10-20',
      type: 'exam',
      description: 'Midterm examination period'
    },
    {
      id: 3,
      titre: 'Holiday - National Day',
      date: '2024-11-18',
      type: 'holiday',
      description: 'National Independence Day'
    },
    {
      id: 4,
      titre: 'Final Exams',
      date: '2024-12-15',
      type: 'exam',
      description: 'Final examination period'
    },
    {
      id: 5,
      titre: 'End of Semester',
      date: '2024-12-30',
      type: 'academic',
      description: 'End of Fall Semester 2024'
    },
    {
      id: 6,
      titre: 'Registration Period',
      date: '2025-01-10',
      type: 'academic',
      description: 'Registration for Spring Semester'
    }
  ]);

  const [moisSelectionne, setMoisSelectionne] = useState(new Date().getMonth());
  const [anneeSelectionnee, setAnneeSelectionnee] = useState(new Date().getFullYear());

  const mois = [
    'January', 'February', 'March', 'April', 'May', 'June',
    'July', 'August', 'September', 'October', 'November', 'December'
  ];

  const joursSemaine = ['Sun', 'Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat'];

  const obtenirPremierJourMois = (annee, mois) => {
    return new Date(annee, mois, 1).getDay();
  };

  const obtenirJoursDansMois = (annee, mois) => {
    return new Date(annee, mois + 1, 0).getDate();
  };

  const obtenirEvenementsPourDate = (date) => {
    const dateStr = `${anneeSelectionnee}-${String(moisSelectionne + 1).padStart(2, '0')}-${String(date).padStart(2, '0')}`;
    return evenements.filter(e => e.date === dateStr);
  };

  const formaterDate = (dateStr) => {
    const date = new Date(dateStr);
    return date.toLocaleDateString('en-US', { month: 'short', day: 'numeric' });
  };

  const premierJour = obtenirPremierJourMois(anneeSelectionnee, moisSelectionne);
  const joursDansMois = obtenirJoursDansMois(anneeSelectionnee, moisSelectionne);

  const jours = [];
  for (let i = 0; i < premierJour; i++) {
    jours.push(null);
  }
  for (let i = 1; i <= joursDansMois; i++) {
    jours.push(i);
  }

  const gererChangementMois = (direction) => {
    if (direction === 'prev') {
      if (moisSelectionne === 0) {
        setMoisSelectionne(11);
        setAnneeSelectionnee(anneeSelectionnee - 1);
      } else {
        setMoisSelectionne(moisSelectionne - 1);
      }
    } else {
      if (moisSelectionne === 11) {
        setMoisSelectionne(0);
        setAnneeSelectionnee(anneeSelectionnee + 1);
      } else {
        setMoisSelectionne(moisSelectionne + 1);
      }
    }
  };

  return (
    <div className="calendrier-academique-professeur">
      <div className="calendrier-academique-professeur-header">
        <h1 className="calendrier-academique-professeur-titre">Academic Calendar</h1>
        <p className="calendrier-academique-professeur-sous-titre">View academic events and important dates</p>
      </div>

      <div className="calendrier-container">
        <div className="calendrier-vue">
          <div className="calendrier-header">
            <button 
              className="calendrier-nav-button"
              onClick={() => gererChangementMois('prev')}
            >
              ←
            </button>
            <h2 className="calendrier-mois-titre">
              {mois[moisSelectionne]} {anneeSelectionnee}
            </h2>
            <button 
              className="calendrier-nav-button"
              onClick={() => gererChangementMois('next')}
            >
              →
            </button>
          </div>

          <div className="calendrier-grille">
            <div className="calendrier-jours-semaine">
              {joursSemaine.map((jour) => (
                <div key={jour} className="calendrier-jour-header">
                  {jour}
                </div>
              ))}
            </div>
            <div className="calendrier-jours">
              {jours.map((jour, index) => {
                const evenementsJour = jour ? obtenirEvenementsPourDate(jour) : [];
                const estAujourdhui = jour === new Date().getDate() && 
                                     moisSelectionne === new Date().getMonth() &&
                                     anneeSelectionnee === new Date().getFullYear();
                
                return (
                  <div 
                    key={index} 
                    className={`calendrier-jour ${!jour ? 'calendrier-jour-vide' : ''} ${estAujourdhui ? 'calendrier-jour-aujourdhui' : ''}`}
                  >
                    {jour && (
                      <>
                        <span className="calendrier-jour-numero">{jour}</span>
                        {evenementsJour.length > 0 && (
                          <div className="calendrier-evenements">
                            {evenementsJour.slice(0, 2).map((evt) => (
                              <div 
                                key={evt.id} 
                                className={`calendrier-evenement calendrier-evenement-${evt.type}`}
                                title={evt.titre}
                              >
                                {evt.titre}
                              </div>
                            ))}
                            {evenementsJour.length > 2 && (
                              <div className="calendrier-evenement-plus">
                                +{evenementsJour.length - 2}
                              </div>
                            )}
                          </div>
                        )}
                      </>
                    )}
                  </div>
                );
              })}
            </div>
          </div>
        </div>

        <div className="calendrier-evenements-liste">
          <h3 className="calendrier-evenements-titre">Upcoming Events</h3>
          <div className="calendrier-evenements-container">
            {evenements
              .filter(evt => {
                const evtDate = new Date(evt.date);
                return evtDate >= new Date();
              })
              .sort((a, b) => new Date(a.date) - new Date(b.date))
              .slice(0, 5)
              .map((evt) => (
                <div key={evt.id} className="calendrier-evenement-item">
                  <div className={`calendrier-evenement-indicateur calendrier-evenement-indicateur-${evt.type}`}></div>
                  <div className="calendrier-evenement-contenu">
                    <div className="calendrier-evenement-date">
                      <Calendar size={14} />
                      <span>{formaterDate(evt.date)}</span>
                    </div>
                    <h4 className="calendrier-evenement-nom">{evt.titre}</h4>
                    <p className="calendrier-evenement-description">{evt.description}</p>
                  </div>
                </div>
              ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default CalendrierAcademiqueProfesseur;
