import { useState, useEffect } from 'react';
import {
    Calendar as CalendarIcon,
    ChevronLeft,
    ChevronRight,
    Download,
    Clock,
    User,
    MapPin
} from 'lucide-react';
import { apiEtudiant } from '../services/apiEtudiant';
import './CalendrierEtudiant.css';

const CalendrierEtudiant = () => {
    const [currentWeekOffset, setCurrentWeekOffset] = useState(0);
    const [lectures, setLectures] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [studentInfo, setStudentInfo] = useState({ nom: '', prenom: '', classe: '', semestre: '' });
    const days = ['LUNDI', 'MARDI', 'MERCREDI', 'JEUDI', 'VENDREDI', 'SAMEDI'];
    const timeSlots = ['08:00', '09:00', '10:00', '11:00', '12:00', '13:00', '14:00'];
    const activeDay = 'VENDREDI'; // As per the reference image

    // Calculate week dates based on offset
    const getWeekDates = (offset = 0) => {
        const today = new Date();
        const currentDay = today.getDay(); // 0 = Sunday, 1 = Monday, etc.
        const mondayOffset = currentDay === 0 ? -6 : 1 - currentDay; // Adjust for Sunday
        const monday = new Date(today);
        monday.setDate(today.getDate() + mondayOffset + (offset * 7));
        
        const sunday = new Date(monday);
        sunday.setDate(monday.getDate() + 6);
        
        return { monday, sunday };
    };

    // Format date as "DD MONTH"
    const formatDate = (date) => {
        const months = ['JANVIER', 'FÉVRIER', 'MARS', 'AVRIL', 'MAI', 'JUIN', 
                       'JUILLET', 'AOÛT', 'SEPTEMBRE', 'OCTOBRE', 'NOVEMBRE', 'DÉCEMBRE'];
        return `${date.getDate()} ${months[date.getMonth()]}`;
    };

    // Get current week label
    const getCurrentWeekLabel = () => {
        const { monday, sunday } = getWeekDates(currentWeekOffset);
        return `SEMAINE DU ${formatDate(monday)} AU ${formatDate(sunday)}`;
    };

    // Fetch student data and timetable
    useEffect(() => {
        const fetchData = async () => {
            try {
                setLoading(true);
                // Get student profile to determine current semester
                const profile = await apiEtudiant.getProfile();
                console.log('Student profile:', profile);
                
                // Convert semestreActuel to backend enum format (S1, S2, etc.)
                const semestre = profile.semestreActuel || 'S5'; // Default to S5 if not set
                console.log('Using semester:', semestre);
                
                setStudentInfo({
                    nom: profile.nom || '',
                    prenom: profile.prenom || '',
                    classe: profile.classe?.nomClasse || 'L3 GÉNIE LOGICIEL',
                    semestre: semestre.replace('S', 'SEMESTRE ')
                });

                console.log('Fetching timetable for semester:', semestre);
                
                const timetableData = await apiEtudiant.getTimetable(semestre);
                console.log('Raw timetable data:', timetableData);
                
                // Map backend data to frontend format
                const mappedLectures = timetableData.map(slot => ({
                    day: slot.dayOfWeek, // Backend already uses French day names
                    startTime: slot.start, // Backend uses 'start' not 'startTime'
                    subject: slot.matiereNom, // Backend uses 'matiereNom'
                    prof: slot.professeurNom, // Backend uses 'professeurNom'
                    room: slot.salle, // Backend uses 'salle'
                    typeSeance: slot.typeSeance // Add session type for color coding
                }));
                
                console.log('Mapped lectures:', mappedLectures);
                setLectures(mappedLectures);
                setError(null);
            } catch (err) {
                console.error('Error fetching timetable:', err);
                setError('Impossible de charger l\'emploi du temps');
                setLectures([]); // Set empty array instead of mock data
            } finally {
                setLoading(false);
            }
        };

        fetchData();
    }, []);

    const getSeanceTypeClass = (typeSeance) => {
        switch (typeSeance?.toUpperCase()) {
            case 'COURS':
                return 'ce-lecture-cours';
            case 'TP':
                return 'ce-lecture-tp';
            case 'TD':
                return 'ce-lecture-td';
            default:
                return 'ce-lecture-cours'; // Default to cours color
        }
    };

    const getLectureForSlot = (day, time) => {
        return lectures.find(l => l.day === day && l.startTime === time);
    };

    // Week navigation functions
    const goToPreviousWeek = () => {
        setCurrentWeekOffset(prev => prev - 1);
    };

    const goToNextWeek = () => {
        setCurrentWeekOffset(prev => prev + 1);
    };

    return (
        <div className="ce-container">
            {/* Loading State */}
            {loading && (
                <div className="ce-loading">
                    <div className="ce-loading-spinner">Chargement de l'emploi du temps...</div>
                </div>
            )}
            
            {/* Error State */}
            {error && !loading && (
                <div className="ce-error">
                    <div className="ce-error-message">{error}</div>
                </div>
            )}

            {/* Dark Modern Header */}
            <header className="ce-header-banner">
                <div className="ce-header-left">
                    <div className="ce-icon-box">
                        <CalendarIcon size={24} color="white" />
                    </div>
                    <div className="ce-title-info">
                        <h1>Mon Emploi du Temps</h1>
                        <span className="ce-subtitle">{studentInfo.classe} • {studentInfo.semestre}</span>
                    </div>
                </div>

                <div className="ce-header-right">
                    <div className="ce-week-selector">
                        <button className="ce-nav-btn" onClick={goToPreviousWeek}>
                            <ChevronLeft size={18} />
                        </button>
                        <span className="ce-week-label">{getCurrentWeekLabel()}</span>
                        <button className="ce-nav-btn" onClick={goToNextWeek}>
                            <ChevronRight size={18} />
                        </button>
                    </div>
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
                                        <div className={`ce-lecture-card ${getSeanceTypeClass(lecture.typeSeance)}`}>
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

            {/* Footer Legend Bar */}
            <div className="planning-footer-legend">
                <div className="legend-items">
                    <div className="legend-item">
                        <span className="dot dot-cours"></span>
                        <span className="label">Cours</span>
                    </div>
                    <div className="legend-item">
                        <span className="dot dot-tp"></span>
                        <span className="label">TP</span>
                    </div>
                    <div className="legend-item">
                        <span className="dot dot-td"></span>
                        <span className="label">TD</span>
                    </div>
                </div>

                <div className="footer-timestamp">
                    <Clock size={16} />
                    <span>Dernière mise à jour: Aujourd'hui à {new Date().toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}</span>
                </div>
            </div>
        </div>
    );
};

export default CalendrierEtudiant;
