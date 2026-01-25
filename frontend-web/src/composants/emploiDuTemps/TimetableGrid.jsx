import React from 'react';
import PlanningCard from './PlanningCard';
import './TimetableGrid.css';

const TimetableGrid = ({ seances, loading }) => {
    const hours = ['08:30', '10:30', '13:30', '15:30'];
    const days = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];

    const getSeanceForSlot = (day, hour) => {
        return seances.find(s => {
            const seanceDay = s.jour?.toUpperCase() || (s.date && new Intl.DateTimeFormat('fr-FR', { weekday: 'long' }).format(new Date(s.date)).toUpperCase());
            return seanceDay === day && s.heureDebut === hour;
        });
    };

    return (
        <div className="timetable-container">
            {/* Day Headers */}
            <div className="days-nav">
                <div className="time-axis-spacer"></div>
                {days.map(day => (
                    <div key={day} className="day-header-pill">
                        {day}
                    </div>
                ))}
            </div>

            {/* Grid Content */}
            <div className="grid-content">
                {hours.map((hour, hourIdx) => (
                    <div key={hour} className="time-row">
                        {/* Time marker */}
                        <div className="time-axis-marker">
                            <div className="marker-dot"></div>
                            <div className="marker-label">{hour}</div>
                            {hourIdx < hours.length - 1 && <div className="marker-line"></div>}
                        </div>

                        {/* Slots */}
                        {days.map(day => {
                            const seance = getSeanceForSlot(day, hour);
                            return (
                                <div key={`${day}-${hour}`} className="grid-slot">
                                    {seance ? (
                                        <PlanningCard seance={seance} />
                                    ) : (
                                        <div className="empty-slot"></div>
                                    )}
                                </div>
                            );
                        })}
                    </div>
                ))}
            </div>
        </div>
    );
};

export default TimetableGrid;
