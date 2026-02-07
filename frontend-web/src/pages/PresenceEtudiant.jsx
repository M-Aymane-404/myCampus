import { useState, useEffect } from 'react';
import {
    ChevronLeft,
    ChevronRight,
    Book,
    User,
    MapPin,
    X,
    Zap,
    UploadCloud
} from 'lucide-react';
import './PresenceEtudiant.css';
import { apiAttendance } from '../services/apiAttendance';

const PresenceEtudiant = () => {
    // Current Calendar View State
    const [currentDate, setCurrentDate] = useState(new Date()); // Tracks the month being viewed

    // Selected Day State (Default to today)
    const [selectedDay, setSelectedDay] = useState(new Date().getDate());

    // Data State
    const [attendanceData, setAttendanceData] = useState([]); // All fetched records
    const [loading, setLoading] = useState(true);
    const [availableYears, setAvailableYears] = useState([]);

    // UI State
    const [activeFilter, setActiveFilter] = useState('HISTORIQUE');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedSession, setSelectedSession] = useState(null);

    const months = ['JAN', 'FEV', 'MAR', 'AVR', 'MAI', 'JUIN', 'JUIL', 'AOUT', 'SEPT', 'OCT', 'NOV', 'DEC'];

    // 1. Initialize: Fetch Profile (Year Range) and Attendance Data
    useEffect(() => {
        const initData = async () => {
            setLoading(true);
            try {
                // Fetch Profile for Year Range
                const profile = await apiAttendance.fetchStudentProfile();
                if (profile) {
                    const startYear = profile.anneeInscription || new Date().getFullYear();
                    const endYear = profile.anneeFinEtudes || (startYear + 5);

                    const years = [];
                    for (let y = endYear; y >= startYear; y--) {
                        years.push(y);
                    }
                    setAvailableYears(years);
                }

                // Fetch All Attendance Data
                const presenceList = await apiAttendance.fetchAttendance();
                setAttendanceData(presenceList);

            } catch (err) {
                console.error("Initialization failed", err);
            } finally {
                setLoading(false);
            }
        };

        initData();
    }, []);

    // Calendar Navigation
    const handleMonthChange = (direction) => {
        const newDate = new Date(currentDate);
        newDate.setMonth(newDate.getMonth() + direction);
        setCurrentDate(newDate);
        setSelectedDay(null);
    };

    const handleYearChange = (year) => {
        const newDate = new Date(currentDate);
        newDate.setFullYear(year);
        setCurrentDate(newDate);
    };

    // Helper: Handle Spring Boot Date Array [yyyy, mm, dd, hh, mm, ss] or ISO String
    const parseBackendDate = (dateVal) => {
        if (!dateVal) return null;
        if (Array.isArray(dateVal)) {
            // Java months are 1-12, JS Date months are 0-11
            const y = dateVal[0];
            const m = dateVal[1] - 1;
            const d = dateVal[2];
            const h = dateVal[3] || 0;
            const min = dateVal[4] || 0;
            const s = dateVal[5] || 0;
            return new Date(y, m, d, h, min, s);
        }
        return new Date(dateVal);
    };

    // Helper: Get day number
    const getDayFromDate = (dateVal) => {
        const d = parseBackendDate(dateVal);
        return d ? d.getDate() : null;
    };

    const isSameMonthAndYear = (dateVal) => {
        const d = parseBackendDate(dateVal);
        if (!d) return false;
        return d.getMonth() === currentDate.getMonth() && d.getFullYear() === currentDate.getFullYear();
    };

    // Derived Data: Calendar Dots
    const getCalendarStatusMap = () => {
        const map = {};
        attendanceData.forEach(record => {
            const eventDate = record.seance?.date || record.dateDeclaration;
            if (isSameMonthAndYear(eventDate)) {
                const day = getDayFromDate(eventDate);
                if (day) {
                    if (!map[day]) map[day] = new Set();
                    const statusLower = record.status ? record.status.toLowerCase() : 'unknown';
                    map[day].add(statusLower);
                }
            }
        });
        return map;
    };

    const calendarStatusMap = getCalendarStatusMap();

    // Derived Data: Filtered Sessions
    const getSessionsForSelectedDay = () => {
        if (!selectedDay) return [];

        let daySessions = attendanceData.filter(record => {
            const eventDate = record.seance?.date || record.dateDeclaration;
            const d = parseBackendDate(eventDate);
            if (!d) return false;

            return d.getDate() === selectedDay &&
                d.getMonth() === currentDate.getMonth() &&
                d.getFullYear() === currentDate.getFullYear();
        });

        if (activeFilter === 'ABSENCES') {
            daySessions = daySessions.filter(s => s.status?.toUpperCase() === 'ABSENT');
        } else if (activeFilter === 'RETARDS') {
            daySessions = daySessions.filter(s => s.status?.toUpperCase() === 'RETARD');
        }

        return daySessions.sort((a, b) => {
            const dA = parseBackendDate(a.dateDeclaration) || new Date(0);
            const dB = parseBackendDate(b.dateDeclaration) || new Date(0);
            return dA - dB;
        });
    };

    const currentSessions = getSessionsForSelectedDay();

    const renderCalendar = () => {
        const cells = [];
        const year = currentDate.getFullYear();
        const month = currentDate.getMonth();

        const daysInMonth = new Date(year, month + 1, 0).getDate();
        const firstDayObj = new Date(year, month, 1);

        let startDay = firstDayObj.getDay();
        startDay = startDay === 0 ? 6 : startDay - 1;

        for (let i = 0; i < startDay; i++) {
            cells.push(<div key={`empty-${i}`} className="calendar-cell empty"></div>);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const isSelected = day === selectedDay;
            const statuses = calendarStatusMap[day] || new Set();
            const statusArray = Array.from(statuses);

            cells.push(
                <div
                    key={day}
                    className={`calendar-cell ${isSelected ? 'active' : ''}`}
                    onClick={() => setSelectedDay(day)}
                >
                    {day}
                    {!isSelected && statusArray.length > 0 && (
                        <div className="dot-indicators">
                            {statusArray.map(s => (
                                <div key={s} className={`dot ${s}`} title={s}></div>
                            ))}
                        </div>
                    )}
                    {isSelected && statusArray.length > 0 && (
                        <div className="cell-dots-selected">
                            {statusArray.map(s => (
                                <div key={s} className="dot-selected"></div>
                            ))}
                        </div>
                    )}
                </div>
            );
        }
        return cells;
    };

    const getStatusLabel = (status) => {
        switch (status?.toUpperCase()) {
            case 'PRESENT': return 'Présent';
            case 'RETARD': return 'En Retard';
            case 'ABSENT': return 'Absent';
            default: return status;
        }
    };

    return (
        <div className="presence-container">
            <div className="presence-calendar-wrapper">
                <div className="presence-calendar-left">
                    <div className="presence-calendar-header">
                        <div className="presence-logo-box">
                            <div className="presence-icon-main">
                                <Zap size={22} color="white" />
                            </div>
                            <div className="presence-logo-text">
                                <h2>Calendrier Présences</h2>
                                <span>Suivi Temporel Interactif</span>
                            </div>
                        </div>

                        <div className="selection-box">
                            <span className="selection-title">Sélection Rapide</span>
                            <div className="control-row">
                                <span className="control-label">Année</span>
                                <select
                                    className="year-select"
                                    value={currentDate.getFullYear()}
                                    onChange={(e) => handleYearChange(parseInt(e.target.value))}
                                >
                                    {availableYears.length > 0 ? (
                                        availableYears.map(y => <option key={y} value={y}>{y}</option>)
                                    ) : (
                                        <option value={currentDate.getFullYear()}>{currentDate.getFullYear()}</option>
                                    )}
                                </select>
                            </div>

                            <div className="control-row">
                                <span className="control-label">Mois</span>
                                <div className="month-stepper">
                                    <button className="stepper-btn" onClick={() => handleMonthChange(-1)}>
                                        <ChevronLeft size={14} />
                                    </button>
                                    <span className="current-month">{months[currentDate.getMonth()]}</span>
                                    <button className="stepper-btn" onClick={() => handleMonthChange(1)}>
                                        <ChevronRight size={14} />
                                    </button>
                                </div>
                            </div>
                        </div>

                        <div className="calendar-legend">
                            <div className="legend-item">
                                <div className="legend-dot-box present"></div>
                                <span className="legend-label">Présent</span>
                            </div>
                            <div className="legend-item">
                                <div className="legend-dot-box retard"></div>
                                <span className="legend-label">Retard</span>
                            </div>
                            <div className="legend-item">
                                <div className="legend-dot-box absent"></div>
                                <span className="legend-label">Absent</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="calendar-right-side">
                    <div className="days-header">
                        <div className="day-name">Lun</div>
                        <div className="day-name">Mar</div>
                        <div className="day-name">Mer</div>
                        <div className="day-name">Jeu</div>
                        <div className="day-name">Ven</div>
                        <div className="day-name">Sam</div>
                        <div className="day-name">Dim</div>
                    </div>
                    <div className="calendar-grid">
                        {renderCalendar()}
                    </div>
                </div>
            </div>

            <div className="history-section">
                <div className="history-controls-row">
                    <div className="history-tabs">
                        <button
                            className={`history-tab ${activeFilter === 'HISTORIQUE' ? 'active' : ''}`}
                            onClick={() => setActiveFilter('HISTORIQUE')}
                        >
                            Historique Complet
                        </button>
                        <button
                            className={`history-tab ${activeFilter === 'ABSENCES' ? 'active' : ''}`}
                            onClick={() => setActiveFilter('ABSENCES')}
                        >
                            Absences
                        </button>
                        <button
                            className={`history-tab ${activeFilter === 'RETARDS' ? 'active' : ''}`}
                            onClick={() => setActiveFilter('RETARDS')}
                        >
                            Retards
                        </button>
                    </div>

                    <div style={{ display: 'flex', gap: '1rem', alignItems: 'center' }}>
                        <div className="selected-date-badge">
                            {selectedDay ? `${selectedDay} ${months[currentDate.getMonth()]} ${currentDate.getFullYear()}` : 'Aucune date'}
                            {selectedDay && <X size={14} style={{ cursor: 'pointer' }} onClick={() => setSelectedDay(null)} />}
                        </div>
                    </div>
                </div>

                <div className="presence-list">
                    {loading ? (
                        <div style={{ padding: '2rem', textAlign: 'center' }}>Chargement...</div>
                    ) : currentSessions.length === 0 ? (
                        <div style={{ padding: '2rem', textAlign: 'center', color: '#aaa' }}>Aucun cours enregistré pour cette date.</div>
                    ) : (
                        currentSessions.map((session, idx) => {
                            const eventDateRaw = session.seance?.date || session.dateDeclaration;
                            const eventDateObj = parseBackendDate(eventDateRaw) || new Date();
                            const dateDeclObj = parseBackendDate(session.dateDeclaration);
                            const timeStr = dateDeclObj ? dateDeclObj.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }) : '--:--';

                            const courseName = session.seance?.type || 'Cours';
                            const profName = session.seance?.prof || 'Enseignant';
                            const roomName = session.seance?.salle || 'Salle';

                            return (
                                <div key={session.id || idx} className="presence-card">
                                    <div className="time-box">
                                        <span className="date-text">{eventDateObj.toLocaleDateString()}</span>
                                        <span className="hour-text">{timeStr}</span>
                                    </div>

                                    <div className="course-icon-box">
                                        <Book size={24} />
                                    </div>

                                    <div className="course-info-box">
                                        <h3>{courseName}</h3>
                                        <div className="prof-room">
                                            <span><User size={12} /> {profName}</span>
                                            <span><MapPin size={12} /> {roomName}</span>
                                        </div>
                                    </div>

                                    <div className="status-box">
                                        <div className={`status-badge ${session.status?.toLowerCase()}`}>
                                            <div className={`dot ${session.status?.toLowerCase()}`}></div>
                                            {getStatusLabel(session.status)}
                                        </div>
                                    </div>

                                    <div className="action-box">
                                        {session.status?.toUpperCase() === 'ABSENT' && (
                                            <button
                                                className="justifier-btn"
                                                onClick={() => {
                                                    setSelectedSession(session);
                                                    setIsModalOpen(true);
                                                }}
                                            >
                                                JUSTIFIER
                                            </button>
                                        )}
                                    </div>
                                </div>
                            );
                        })
                    )}
                </div>
            </div>

            {isModalOpen && (
                <div className="presence-modal-overlay">
                    <div className="presence-modal-content">
                        <header className="modal-header">
                            <h2>Justification</h2>
                            <button className="modal-close-btn" onClick={() => setIsModalOpen(false)}>
                                <X size={20} />
                            </button>
                        </header>

                        <div className="modal-body">
                            <div className="session-info-card">
                                <span className="session-date-label">
                                    {selectedSession && parseBackendDate(selectedSession.seance?.date || selectedSession.dateDeclaration)?.toLocaleDateString()}
                                </span>
                                <h3 className="session-title-label">{selectedSession?.seance?.type || 'Cours'}</h3>
                            </div>

                            <div className="form-group">
                                <label className="input-label">TYPE DE MOTIF</label>
                                <select className="modal-select">
                                    <option>Certificat Médical</option>
                                    <option>Raisons Familiales</option>
                                    <option>Autre</option>
                                </select>
                            </div>

                            <div className="upload-zone">
                                <UploadCloud size={32} className="upload-icon" />
                                <span className="upload-text">IMPORTER PDF (MAX 5MO)</span>
                                <input type="file" className="file-input-hidden" accept=".pdf" onChange={() => alert("Fonctionnalité d'upload bientôt disponible")} />
                            </div>

                            <button className="submit-justif-btn" onClick={() => {
                                alert("Document soumis pour validation.");
                                setIsModalOpen(false);
                            }}>
                                SOUMETTRE LE DOCUMENT
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default PresenceEtudiant;
