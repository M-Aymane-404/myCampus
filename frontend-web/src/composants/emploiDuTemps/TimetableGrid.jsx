import React, { useState } from 'react';
import { Clock, User, MapPin } from 'lucide-react';
import PlanningCard from './PlanningCard';
import './TimetableGrid.css';

const TimetableGrid = ({ seances, loading }) => {
    const [activeDay, setActiveDay] = useState('VENDREDI');
    const hours = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00'];
    const days = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];

    const getSeanceTypeClass = (typeSeance) => {
        console.log('Processing typeSeance:', typeSeance); // Debug log
        switch (typeSeance?.toUpperCase()) {
            case 'COURS':
                return 'ce-lecture-cours';
            case 'TP':
                return 'ce-lecture-tp';
            case 'TD':
                return 'ce-lecture-td';
            default:
                console.log('Unknown typeSeance, defaulting to cours:', typeSeance); // Debug log
                return 'ce-lecture-cours'; // Default to cours color
        }
    };

    const getSeanceForSlot = (day, hour) => {
        return seances.find(s => {
            // Handle both backend field names (dayOfWeek, start) and frontend field names (jour, heureDebut)
            const seanceDay = (s.dayOfWeek || s.jour)?.toUpperCase() || 
                             (s.date && new Intl.DateTimeFormat('fr-FR', { weekday: 'long' }).format(new Date(s.date)).toUpperCase());
            const seanceStart = s.start || s.heureDebut;
            return seanceDay === day && seanceStart === hour;
        });
    };

    return (
        <div className="ce-grid-wrapper">
            {/* Day Headers */}
            <div className="ce-grid-header">
                <div className="ce-time-column-header">
                    <Clock size={18} />
                </div>
                {days.map(day => (
                    <div 
                        key={day} 
                        className={`ce-day-header ${day === activeDay ? 'active' : ''}`}
                        onClick={() => setActiveDay(day)}
                    >
                        {day}
                        {day === activeDay && <div className="ce-day-dot"></div>}
                    </div>
                ))}
            </div>

            {/* Grid Rows */}
            {hours.map(time => (
                <div key={time} className="ce-grid-row">
                    <div className="ce-time-label">{time}</div>
                    {days.map(day => {
                        const seance = getSeanceForSlot(day, time);
                        return (
                            <div key={day} className={`ce-grid-cell ${day === activeDay ? 'active-day' : ''}`}>
                                {seance && (
                                    <div className={`ce-lecture-card ${getSeanceTypeClass(seance.typeSeance)}`}>
                                        {console.log('Applied classes for', seance.matiereNom, ':', `ce-lecture-card ${getSeanceTypeClass(seance.typeSeance)}`)}
                                        <div className="ce-lecture-subject">{seance.matiereNom || seance.matiere || seance.nom}</div>
                                        <div className="ce-lecture-meta">
                                            <div className="ce-meta-item">
                                                <User size={10} />
                                                <span>{seance.professeurNom || seance.professeur || 'Non assigné'}</span>
                                            </div>
                                            {seance.salle && (
                                                <div className="ce-meta-item">
                                                    <MapPin size={10} />
                                                    <span>{seance.salle}</span>
                                                </div>
                                            )}
                                        </div>
                                    </div>
                                )}
                            </div>
                        );
                    })}
                </div>
            ))}
        </div>
    );
};

export default TimetableGrid;
