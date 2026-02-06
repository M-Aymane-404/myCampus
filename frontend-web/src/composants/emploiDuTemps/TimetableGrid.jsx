import React, { useState } from 'react';
import { Clock, User, MapPin } from 'lucide-react';
import PlanningCard from './PlanningCard';
import './TimetableGrid.css';

const TimetableGrid = ({ seances, loading }) => {
    const [activeDay, setActiveDay] = useState('VENDREDI');
    const hours = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00'];
    const days = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];

    const getSeanceForSlot = (day, hour) => {
        return seances.find(s => {
            const seanceDay = s.jour?.toUpperCase() || (s.date && new Intl.DateTimeFormat('fr-FR', { weekday: 'long' }).format(new Date(s.date)).toUpperCase());
            return seanceDay === day && s.heureDebut === hour;
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
                                    <div className="ce-lecture-card">
                                        <div className="ce-lecture-subject">{seance.matiere || seance.nom}</div>
                                        <div className="ce-lecture-meta">
                                            <div className="ce-meta-item">
                                                <User size={10} />
                                                <span>{seance.professeur || 'Non assigné'}</span>
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
