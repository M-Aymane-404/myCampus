import { useState, Fragment } from 'react';
import {
    Award,
    BookOpen,
    Calendar,
    ChevronRight,
    ClipboardCheck,
    Download,
    FileText,
    Printer,
    Star,
    Trophy,
    History,
    Shield,
    Eye,
    CheckCircle,
    Hash,
    Library,
    ChevronDown,
    Filter
} from 'lucide-react';
import './NotesEtudiant.css';

const NotesEtudiant = () => {
    const [activeSemester, setActiveSemester] = useState(4);

    // Mock Data for the Student Portal
    const studentStats = {
        degree: "Diplôme d'Ingénieur",
        specialization: "Génie Logiciel",
        generalAverage: 16.12,
        validatedModules: 24,
        totalModules: 28,
        sessionStatus: "Admis avec Mention",
        sessionMessage: "Félicitations pour vos résultats !",
        finalMention: "Très Bien",
        certId: "LUM-2024-CERT-S4"
    };

    const gradeData = {
        4: {
            label: "Semestre 4 – Printemps 2024",
            average: 15.50,
            ects: "30/30",
            status: "VALIDÉ",
            modules: [
                {
                    id: 'react',
                    name: 'React.js & TypeScript',
                    ects: 4,
                    coeff: 4,
                    ds: 16.5,
                    exam: 15.5,
                    hasCopy: true,
                    hasCorrection: true,
                    mention: 'BIEN',
                    average: 15.83,
                    letter: 'R'
                },
                {
                    id: 'node',
                    name: 'Node.js Architecture',
                    ects: 4,
                    coeff: 4,
                    ds: 14.0,
                    exam: 17.5,
                    hasCopy: true,
                    hasCorrection: true,
                    mention: 'TRES BIEN',
                    average: 16.33,
                    letter: 'N'
                },
                {
                    id: 'ai',
                    name: 'Intelligence Artificielle & ML',
                    ects: 4,
                    coeff: 3,
                    ds: 15.0,
                    exam: 14.25,
                    hasCopy: false,
                    hasCorrection: false,
                    mention: 'BIEN',
                    average: 14.50,
                    letter: 'A'
                },
                {
                    id: 'design',
                    name: 'Analyse & Conception UML',
                    ects: 4,
                    coeff: 2,
                    ds: 13.0,
                    exam: 14.5,
                    hasCopy: true,
                    hasCorrection: false,
                    mention: 'BIEN',
                    average: 14.00,
                    letter: 'A'
                },
                {
                    id: 'algo',
                    name: 'Algorithmique Avancée',
                    ects: 4,
                    coeff: 3,
                    ds: 18.0,
                    exam: 15.0,
                    hasCopy: false,
                    hasCorrection: true,
                    mention: 'TRES BIEN',
                    average: 16.00,
                    letter: 'A'
                },
                {
                    id: 'law',
                    name: 'Droit Numérique & Éthique',
                    ects: 2,
                    coeff: 1,
                    ds: 12.5,
                    exam: 14.0,
                    hasCopy: false,
                    hasCorrection: false,
                    mention: 'ASSEZ BIEN',
                    average: 13.50,
                    letter: 'D'
                },
                {
                    id: 'lang',
                    name: 'Langues Étrangères',
                    ects: 4,
                    coeff: 2,
                    isParent: true,
                    mention: 'TRES BIEN',
                    average: 16.83,
                    letter: 'L',
                    color: 'black',
                    subjects: [
                        { name: 'Français Professionnel', ds: 14.0, exam: 16.5, average: 15.67, hasDsCopy: true },
                        { name: 'Anglais Technique (TOEIC)', ds: 17.0, exam: 18.5, average: 18.00, hasExamCopy: true }
                    ]
                }
            ]
        }
    };

    const currentData = gradeData[activeSemester] || { modules: [] };

    const getMentionClass = (mention) => {
        switch (mention) {
            case 'TRES BIEN': return 'mention-tres-bien';
            case 'BIEN': return 'mention-bien';
            case 'ASSEZ BIEN': return 'mention-assez-bien';
            default: return '';
        }
    };

    return (
        <div className="notes-container">
            {/* Top Banner and Summary Card */}
            <div className="notes-top-layout">
                <div className="notes-main-banner">
                    <div className="banner-portal-tag">
                        <History size={16} />
                        Portail Résultats Lumina
                    </div>

                    <div className="banner-title-box">
                        <h1>{studentStats.degree}</h1>
                        <span>{studentStats.specialization}</span>
                    </div>

                    <button className="releve-global-btn">
                        <CheckCircle size={16} />
                        Relevé de notes global
                    </button>

                    <div className="banner-metrics">
                        <div className="metric-item">
                            <span className="metric-label">Moyenne Générale</span>
                            <div className="metric-value highlight">
                                {studentStats.generalAverage}<span>/20</span>
                            </div>
                        </div>
                        <div className="metric-item">
                            <span className="metric-label">Modules Validés</span>
                            <div className="metric-value accent">
                                {studentStats.validatedModules}<span>/{studentStats.totalModules}</span>
                            </div>
                        </div>
                    </div>
                </div>

                <div className="session-summary-card">
                    <div className="session-summary-icon">
                        <Trophy size={32} />
                    </div>
                    <div className="session-info">
                        <span className="session-tag">Session Actuelle</span>
                        <h2 className="session-status-title">{studentStats.sessionStatus}</h2>
                        <p className="session-message">{studentStats.sessionMessage}</p>
                    </div>
                </div>
            </div>

            {/* Semester Navigation REMOVED - replaced by integrated filter */}

            {/* Main Grade Detail Card */}
            <div className="grades-detail-card">
                <header className="grades-header">
                    <div className="grades-title-box">
                        <div className="modules-icon-box">
                            <Library size={20} />
                        </div>
                        <div className="grades-main-info">
                            <div className="modules-header-flex">
                                <h2 className="modules-label">Modules</h2>
                                <div className="semester-filter-box">
                                    <Filter size={12} className="filter-icon-inline" />
                                    <span className="filter-label-text">Filtrer:</span>
                                    <select
                                        value={activeSemester}
                                        onChange={(e) => setActiveSemester(parseInt(e.target.value))}
                                        className="semester-select-minimal"
                                    >
                                        {[1, 2, 3, 4].map(s => (
                                            <option key={s} value={s}>Semestre {s}</option>
                                        ))}
                                    </select>
                                    <ChevronDown size={14} className="select-arrow" />
                                </div>
                            </div>
                            <div className="grades-sub-info">
                                <span className="status-valid">● VALIDÉ</span>
                                <span className="ects-info">• {currentData.ects} ECTS ACQUIS</span>
                            </div>
                        </div>
                    </div>

                    <div className="semester-average-box">
                        <span className="avg-label">Moyenne Session</span>
                        <div className="avg-value">
                            {currentData.average?.toFixed(2)}<span>/20</span>
                        </div>
                    </div>
                </header>

                <table className="notes-table">
                    <thead>
                        <tr>
                            <th style={{ width: '40%' }}>Module / Matière</th>
                            <th style={{ textAlign: 'center' }}>DS / Copie</th>
                            <th style={{ textAlign: 'center' }}>Exam / Copie</th>
                            <th style={{ textAlign: 'center' }}>Correction</th>
                            <th style={{ textAlign: 'center' }}>Mention</th>
                            <th style={{ textAlign: 'center' }}>Moyenne</th>
                        </tr>
                    </thead>
                    <tbody>
                        {currentData.modules.map(module => (
                            <Fragment key={module.id}>
                                {/* Module Row */}
                                <tr className="module-row">
                                    <td>
                                        <div className="module-info">
                                            <div className={`module-letter ${module.color === 'black' ? 'black' : ''}`}>
                                                {module.letter}
                                            </div>
                                            <div className="item-details">
                                                <span className="item-name">{module.name}</span>
                                                <div className="item-meta">
                                                    {module.ects} ECTS <span>• x{module.coeff}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        {module.ds && (
                                            <div className="note-cell">
                                                <div className="note-val">{module.ds}</div>
                                                {module.hasCopy && (
                                                    <button className="btn-voir-copie">
                                                        <Eye size={10} /> Voir Copie
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </td>
                                    <td>
                                        {module.exam && (
                                            <div className="note-cell">
                                                <div className="note-val">{module.exam}</div>
                                                {module.hasCopy && (
                                                    <button className="btn-voir-copie">
                                                        <Eye size={10} /> Voir Copie
                                                    </button>
                                                )}
                                            </div>
                                        )}
                                    </td>
                                    <td style={{ textAlign: 'center' }}>
                                        {module.hasCorrection ? (
                                            <div className="correction-icon">
                                                <FileText size={14} />
                                            </div>
                                        ) : (
                                            <span style={{ color: '#cbd5e1', fontSize: '0.625rem', fontWeight: 700 }}>N/A</span>
                                        )}
                                    </td>
                                    <td>
                                        <div style={{ textAlign: 'center' }}>
                                            <span className={`mention-badge ${getMentionClass(module.mention)}`}>
                                                {module.mention}
                                            </span>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="final-moy">{module.average?.toFixed(2)}</div>
                                    </td>
                                </tr>

                                {/* Subject Rows (for split modules like Languages) */}
                                {module.subjects?.map((sub, idx) => (
                                    <tr key={idx} className="subject-row">
                                        <td>
                                            <div className="subject-info">
                                                <div className="subject-icon">
                                                    <FileText size={16} />
                                                </div>
                                                <span className="item-name" style={{ fontSize: '0.75rem' }}>{sub.name}</span>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="note-cell">
                                                <div className="note-val" style={{ color: '#94a3b8' }}>{sub.ds}</div>
                                                {sub.hasDsCopy && <button className="btn-voir-copie"><Eye size={10} /> COPIE</button>}
                                            </div>
                                        </td>
                                        <td>
                                            <div className="note-cell">
                                                <div className="note-val" style={{ color: '#94a3b8' }}>{sub.exam}</div>
                                                {sub.hasExamCopy && <button className="btn-voir-copie"><Eye size={10} /> COPIE</button>}
                                            </div>
                                        </td>
                                        <td></td>
                                        <td><div style={{ textAlign: 'center', color: '#cbd5e1', fontSize: '0.625rem' }}>Moyenne:</div></td>
                                        <td><div className="final-moy" style={{ fontSize: '0.875rem', color: '#6366f1' }}>{sub.average?.toFixed(2)}</div></td>
                                    </tr>
                                ))}
                            </Fragment>
                        ))}
                    </tbody>
                </table>
            </div>

            {/* Footer Final Result Bar */}
            <footer className="notes-footer-result">
                <div className="footer-left">
                    <div className="footer-icon">
                        <Award size={24} color="white" />
                    </div>
                    <div className="footer-res-details">
                        <span className="tag">Mention Académique Finale</span>
                        <div className="val">{studentStats.finalMention} <span>★</span></div>
                        <p style={{ margin: 0, fontSize: '0.625rem', color: '#94a3b8' }}>
                            Félicitations pour vos résultats exceptionnels.
                        </p>
                    </div>
                </div>

                <div className="footer-right">
                    <div className="statut-session">
                        <span className="statut-tag">Statut Session</span>
                        <span className="statut-val">ADMIS</span>
                    </div>
                    <div className="cert-id-badge">
                        <Shield size={12} />
                        ID: {studentStats.certId}
                    </div>
                </div>
            </footer>
        </div>
    );
};

export default NotesEtudiant;
