import { useState } from 'react';
import {
    Calendar as CalendarIcon,
    ChevronLeft,
    ChevronRight,
    Search,
    Download,
    Book,
    User,
    MapPin,
    ArrowUpRight,
    X,
    Zap,
    UploadCloud
} from 'lucide-react';
import './PresenceEtudiant.css';

const PresenceEtudiant = () => {
    const [selectedDay, setSelectedDay] = useState(20);
    const [currentMonth, setCurrentMonth] = useState('MAI');
    const [currentYear, setCurrentYear] = useState(2024);
    const [activeFilter, setActiveFilter] = useState('HISTORIQUE');
    const [isModalOpen, setIsModalOpen] = useState(false);
    const [selectedSession, setSelectedSession] = useState(null);

    // Mock Attendance Data for the calendar
    const calendarData = {
        12: { status: ['absent'] },
        15: { status: ['present'] },
        20: { status: ['present', 'retard', 'absent'] }, // Today/Selected sample
        21: { status: ['present'] },
    };

    // Sessions for the selected day (20 Mai)
    const sessions = [
        {
            id: 1,
            date: '20 Mai',
            time: '08:30 - 10:30',
            course: 'React.js & TypeScript',
            prof: 'PR. MERYEM ALAOUI',
            room: 'AMPHI A',
            status: 'present'
        },
        {
            id: 2,
            date: '20 Mai',
            time: '10:30 - 12:30',
            course: 'Node.js Architecture',
            prof: 'PR. KARIM EL IDRISSI',
            room: 'LABO 4',
            status: 'retard'
        },
        {
            id: 3,
            date: '20 Mai',
            time: '14:00 - 16:00',
            course: 'Machine Learning',
            prof: 'PR. KARIM EL IDRISSI',
            room: 'AMPHI B',
            status: 'absent'
        }
    ];

    const months = ['JAN', 'FEV', 'MAR', 'AVR', 'MAI', 'JUIN', 'JUIL', 'AOUT', 'SEPT', 'OCT', 'NOV', 'DEC'];

    const handleMonthChange = (direction) => {
        const currentIndex = months.indexOf(currentMonth);
        let newIndex = currentIndex + direction;
        
        if (newIndex < 0) newIndex = 11; // Wrap to December
        if (newIndex > 11) newIndex = 0;  // Wrap to January
        
        setCurrentMonth(months[newIndex]);
    };

    const renderCalendar = () => {
        const cells = [];
        const monthIndex = months.indexOf(currentMonth);
        const daysInMonth = new Date(currentYear, monthIndex + 1, 0).getDate();
        const firstDay = new Date(currentYear, monthIndex, 1).getDay();
        const startOffset = firstDay === 0 ? 6 : firstDay - 1; // Convert Sunday=0 to Saturday=6 offset

        // Empty cells for alignment
        for (let i = 0; i < startOffset; i++) {
            cells.push(<div key={`empty-${i}`} className="calendar-cell empty"></div>);
        }

        for (let day = 1; day <= daysInMonth; day++) {
            const dayData = calendarData[day];
            const isSelected = day === selectedDay;

            cells.push(
                <div
                    key={day}
                    className={`calendar-cell ${isSelected ? 'active' : ''}`}
                    onClick={() => setSelectedDay(day)}
                >
                    {day}
                    {dayData && !isSelected && (
                        <div className="dot-indicators">
                            {dayData.status.map((s, idx) => (
                                <div key={idx} className={`dot ${s}`}></div>
                            ))}
                        </div>
                    )}
                    {isSelected && dayData && (
                        <div className="cell-dots-selected">
                            {dayData.status.map((_, idx) => (
                                <div key={idx} className="dot-selected"></div>
                            ))}
                        </div>
                    )}
                </div>
            );
        }
        return cells;
    };

    return (
        <div className="presence-container">
            {/* Top Dark Header & Calendar Card */}
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
                                    value={currentYear}
                                    onChange={(e) => setCurrentYear(parseInt(e.target.value))}
                                >
                                    <option value={2026}>2026</option>
                                    <option value={2025}>2025</option>
                                    <option value={2024}>2024</option>
                                    <option value={2023}>2023</option>
                                </select>
                            </div>

                            <div className="control-row">
                                <span className="control-label">Mois</span>
                                <div className="month-stepper">
                                    <button className="stepper-btn" onClick={() => handleMonthChange(-1)}>
                                        <ChevronLeft size={14} />
                                    </button>
                                    <span className="current-month">{currentMonth}</span>
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

            {/* Bottom History Section */}
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
                            {selectedDay} {currentMonth} {currentYear}
                            <X size={14} style={{ cursor: 'pointer' }} />
                        </div>

                    </div>
                </div>

                <div className="presence-list">
                    {sessions.map(session => (
                        <div key={session.id} className="presence-card">
                            <div className="time-box">
                                <span className="date-text">{session.date}</span>
                                <span className="hour-text">{session.time}</span>
                            </div>

                            <div className="course-icon-box">
                                <Book size={24} />
                            </div>

                            <div className="course-info-box">
                                <h3>{session.course}</h3>
                                <div className="prof-room">
                                    <span><User size={12} /> {session.prof}</span>
                                    <span><MapPin size={12} /> {session.room}</span>
                                </div>
                            </div>

                            <div className="status-box">
                                <div className={`status-badge ${session.status}`}>
                                    <div className={`dot ${session.status}`}></div>
                                    {session.status === 'present' ? 'Présent' :
                                        session.status === 'retard' ? 'En Retard' : 'Absent'}
                                </div>
                            </div>

                            <div className="action-box">
                                {session.status === 'absent' && (
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
                    ))}
                </div>
            </div>

            {/* Justification Modal */}
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
                                <span className="session-date-label">{selectedSession?.date} {currentYear}</span>
                                <h3 className="session-title-label">{selectedSession?.course}</h3>
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
                                <input type="file" className="file-input-hidden" accept=".pdf" />
                            </div>

                            <button className="submit-justif-btn">
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
