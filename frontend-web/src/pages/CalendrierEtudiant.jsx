import { useState } from 'react';
import {
    Calendar as CalendarIcon,
    ChevronLeft,
    ChevronRight,
    Download,
    Clock,
    User,
    MapPin
} from 'lucide-react';
import './CalendrierEtudiant.css';

const CalendrierEtudiant = () => {
    const [selectedWeek, setSelectedWeek] = useState('SEMAINE DU 20 MAI');
    const days = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];
    const timeSlots = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00'];
    const activeDay = 'VENDREDI'; // As per the reference image

    // Mock Lecture Data
    const lectures = [
        {
            day: 'LUNDI',
            startTime: '08:00',
            subject: 'Architecture JEE',
            prof: 'PR. ALAOUI',
            room: 'SALLE 12'
        },
        {
            day: 'MARDI',
            startTime: '10:00',
            subject: 'Data Science',
            prof: 'PR. BENANI',
            room: 'AMPHI B'
        },
        {
            day: 'VENDREDI',
            startTime: '09:00',
            subject: 'React & TypeScript',
            prof: 'PR. IDRISSI',
            room: 'LABO 4'
        }
    ];

    const getLectureForSlot = (day, time) => {
        return lectures.find(l => l.day === day && l.startTime === time);
    };

    return (
        <div className="ce-container">
            {/* Dark Modern Header */}
            <header className="ce-header-banner">
                <div className="ce-header-left">
                    <div className="ce-icon-box">
                        <CalendarIcon size={24} color="white" />
                    </div>
                    <div className="ce-title-info">
                        <h1>Mon Emploi du Temps</h1>
                        <span className="ce-subtitle">L3 GÉNIE LOGICIEL • SEMESTRE 5</span>
                    </div>
                </div>

                <div className="ce-header-right">
                    <div className="ce-week-selector">
                        <button className="ce-nav-btn">
                            <ChevronLeft size={18} />
                        </button>
                        <span className="ce-week-label">{selectedWeek}</span>
                        <button className="ce-nav-btn">
                            <ChevronRight size={18} />
                        </button>
                    </div>
                    <button className="ce-download-btn">
                        <Download size={20} />
                    </button>
                </div>
            </header>

            {/* Timetable Grid Area */}
            <div className="ce-grid-wrapper">
                {/* Day Headers */}
                <div className="ce-grid-header">
                    <div className="ce-time-column-header">
                        <Clock size={18} />
                    </div>
                    {days.map(day => (
                        <div key={day} className={`ce-day-header ${day === activeDay ? 'active' : ''}`}>
                            {day}
                            {day === activeDay && <div className="ce-day-dot"></div>}
                        </div>
                    ))}
                </div>

                {/* Grid Rows */}
                {timeSlots.map(time => (
                    <div key={time} className="ce-grid-row">
                        <div className="ce-time-label">{time}</div>
                        {days.map(day => {
                            const lecture = getLectureForSlot(day, time);
                            return (
                                <div key={day} className={`ce-grid-cell ${day === activeDay ? 'active-day' : ''}`}>
                                    {lecture && (
                                        <div className="ce-lecture-card">
                                            <div className="ce-lecture-subject">{lecture.subject}</div>
                                            <div className="ce-lecture-meta">
                                                <div className="ce-meta-item">
                                                    <User size={10} />
                                                    <span>{lecture.prof}</span>
                                                </div>
                                                <div className="ce-meta-item">
                                                    <MapPin size={10} />
                                                    <span>{lecture.room}</span>
                                                </div>
                                            </div>
                                        </div>
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

export default CalendrierEtudiant;
