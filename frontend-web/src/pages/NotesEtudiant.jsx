import { useState, useEffect, Fragment } from 'react';
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
import { apiGrades } from '../services/apiGrades';
import { getSpectializationFromClass, parseExams } from '../utils/academicUtils';

const NotesEtudiant = () => {
    const [activeSemester, setActiveSemester] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Stats State
    const [studentStats, setStudentStats] = useState({
        degree: "Diplôme d'Ingénieur",
        specialization: "Chargement...",
        generalAverage: 0,
        validatedModules: 0,
        totalModules: 0,
        sessionStatus: "...",
        sessionMessage: "En attente...",
        finalMention: "...",
        certId: "LUM-2024-PENDING",
        maxSemester: 1,
        availableSemesters: []
    });

    // Grades Data State
    const [currentData, setCurrentData] = useState({
        label: `Semestre ${activeSemester}`,
        average: 0,
        ects: "0/30",
        status: "EN COURS",
        modules: []
    });

    // Fetch Global Stats on Mount
    useEffect(() => {
        const fetchStats = async () => {
            try {
                const stats = await apiGrades.fetchGlobalStats();
                setStudentStats(prev => ({
                    ...prev,
                    ...stats,
                    sessionMessage: stats.currentSessionStatus === 'Admis' ? "Félicitations pour vos résultats !" : "Continuez vos efforts."
                }));
                // Set active semester to the latest one found
                if (stats.availableSemesters && stats.availableSemesters.length > 0) {
                    const lastSem = stats.availableSemesters[stats.availableSemesters.length - 1];
                    setActiveSemester(lastSem);
                } else if (stats.maxSemester) {
                    setActiveSemester(stats.maxSemester);
                }
            } catch (err) {
                console.error("Failed to fetch stats", err);
                // Fallback or error state
            }
        };
        fetchStats();
    }, []);

    // Fetch Grades when Active Semester Changes
    useEffect(() => {
        const fetchGrades = async () => {
            setLoading(true);
            setError(null);
            try {
                const data = await apiGrades.fetchStudentGrades(activeSemester);

                if (!data) {
                    // Handle empty data for this semester
                    setCurrentData({
                        label: `Semestre ${activeSemester}`,
                        average: 0,
                        ects: "0/30",
                        status: "NON DISPONIBLE",
                        modules: []
                    });
                    setLoading(false);
                    return;
                }

                // Process Modules
                const processedModules = data.modules.map((mod, idx) => {
                    // Check logic: "Langues" -> Nested. Others -> Flattened.
                    const normalizedName = mod.nomDuModule.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
                    const isLanguages = normalizedName.includes('Langages étrangère');
                    console.log('Module name:', mod.nomDuModule, '-> Normalized:', normalizedName, '-> isLanguages:', isLanguages);
                    if (isLanguages) {
                        // --- NESTED VIEW (Langues) ---
                        const subjects = mod.matieres.map(mat => {
                            const exams = parseExams(mat.examens);
                            return {
                                name: mat.nomDuMatiere,
                                ds: exams.ds,
                                exam: exams.exam,
                                average: mat.moyenneDuMatiere,
                                hasDsCopy: exams.hasDsCopy,
                                hasExamCopy: exams.hasExamCopy,
                                dsCopyPath: exams.dsCopyPath,
                                examCopyPath: exams.examCopyPath,
                                isRattrapage: exams.isRattrapage
                            };
                        });

                        return {
                            id: mod.id || idx,
                            name: mod.nomDuModule,
                            ects: mod.coefficient,
                            coeff: mod.coefficient,
                            mention: getMentionFromGrade(mod.moyenneDuModule),
                            average: mod.moyenneDuModule,
                            letter: mod.nomDuModule.charAt(0).toUpperCase(),
                            color: 'black',
                            isParent: true,
                            subjects: subjects,
                            isRattrapage: subjects.some(s => s.isRattrapage)
                        };

                    } else {
                        // --- FLATTENED VIEW (General Modules) ---
                        // "DS Column" -> Subject 1
                        // "Exam Column" -> Subject 2

                        const sub1 = mod.matieres && mod.matieres.length > 0 ? mod.matieres[0] : null;
                        const sub2 = mod.matieres && mod.matieres.length > 1 ? mod.matieres[1] : null;

                        const sub1Exams = sub1 ? parseExams(sub1.examens) : null;
                        const sub2Exams = sub2 ? parseExams(sub2.examens) : null;

                        // Copies
                        const sub1CopyPath = sub1Exams ? (sub1Exams.dsCopyPath || sub1Exams.examCopyPath) : null;
                        const sub2CopyPath = sub2Exams ? (sub2Exams.dsCopyPath || sub2Exams.examCopyPath) : null;

                        // Correction
                        const correctionPath = sub2Exams?.examCorrection || sub2Exams?.dsCorrection || sub1Exams?.examCorrection || sub1Exams?.dsCorrection;
                        const hasCorrection = !!correctionPath;

                        return {
                            id: mod.id || idx,
                            name: mod.nomDuModule,
                            ects: mod.coefficient,
                            coeff: mod.coefficient,

                            // Map Sub 1 to "DS" slot
                            ds: sub1 ? sub1.moyenneDuMatiere : null,
                            hasDsCopy: !!sub1CopyPath,
                            dsCopyPath: sub1CopyPath,

                            // Map Sub 2 to "Exam" slot
                            exam: sub2 ? sub2.moyenneDuMatiere : null,
                            hasExamCopy: !!sub2CopyPath,
                            examCopyPath: sub2CopyPath,

                            hasCorrection: hasCorrection,
                            correctionPath: correctionPath,

                            mention: getMentionFromGrade(mod.moyenneDuModule),
                            average: mod.moyenneDuModule,
                            letter: mod.nomDuModule.charAt(0).toUpperCase(),
                            isParent: false,
                            isRattrapage: (sub1Exams?.isRattrapage || sub2Exams?.isRattrapage)
                        };
                    }
                });

                setCurrentData({
                    label: `Semestre ${activeSemester}`,
                    average: data.moyenneSession,
                    ects: `${processedModules.reduce((acc, m) => acc + (m.average >= 10 ? 30 / processedModules.length : 0), 0).toFixed(0)}/30`, // Rough Estimate if ECTS not in backend
                    status: data.moyenneSession >= 10 ? "VALIDÉ" : "AJOURNÉ",
                    modules: processedModules
                });

            } catch (err) {
                console.error(err);
                setError(err);
            } finally {
                setLoading(false);
            }
        };

        fetchGrades();
    }, [activeSemester]);

    const getMentionClass = (mention) => {
        switch (mention) {
            case 'TRES BIEN': return 'mention-tres-bien';
            case 'BIEN': return 'mention-bien';
            case 'ASSEZ BIEN': return 'mention-assez-bien';
            default: return '';
        }
    };

    const getMentionFromGrade = (grade) => {
        if (!grade) return '-';
        if (grade >= 16) return 'TRES BIEN';
        if (grade >= 14) return 'BIEN';
        if (grade >= 12) return 'ASSEZ BIEN';
        if (grade >= 10) return 'PASSABLE';
        return 'AJOURNÉ';
    };

    // Helper to open PDF
    const openPdf = (path) => {
        if (!path) return;
        // Assuming path is relative or needs base URL
        // If path is full URL, open it.
        // If not, maybe append to some base static URL?
        // Backend DTO `copieEtudiantPath` is typically a string.
        // For now, assume it's a serve-able URL or we alert.
        // Or cleaner: window.open(path, '_blank');
        window.open(path, '_blank');
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

                    <button className="releve-global-btn" onClick={() => alert("Fonctionnalité de téléchargement globale à implémenter")}>
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
                                        {(studentStats.availableSemesters || [1]).map(s => (
                                            <option key={s} value={s}>Semestre {s}</option>
                                        ))}
                                    </select>
                                    <ChevronDown size={14} className="select-arrow" />
                                </div>
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
                            <th style={{ textAlign: 'center' }}>Session</th>
                            <th style={{ textAlign: 'center' }}>Moyenne</th>
                        </tr>
                    </thead>
                    <tbody>
                        {loading ? (
                            <tr><td colSpan="6" style={{ textAlign: 'center', padding: '2rem' }}>Chargement des notes...</td></tr>
                        ) : error ? (
                            <tr><td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: 'red' }}>Erreur de chargement ou semestre vide.</td></tr>
                        ) : currentData.modules.length === 0 ? (
                            <tr><td colSpan="6" style={{ textAlign: 'center', padding: '2rem' }}>Aucune note disponible pour ce semestre.</td></tr>
                        ) : (
                            currentData.modules.map((module, idx) => (
                                <Fragment key={module.id || idx}>
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
                                                        <span>• x{module.coeff}</span>
                                                    </div>
                                                </div>
                                            </div>
                                        </td>
                                        <td>
                                            {/* DS Cell */}
                                            {module.isParent ? null : (
                                                module.ds !== null && (
                                                    <div className="note-cell">
                                                        <div className="note-val">{module.ds}</div>
                                                        {module.hasDsCopy && (
                                                            <button className="btn-voir-copie" onClick={() => openPdf(module.dsCopyPath)}>
                                                                <Eye size={10} /> Voir Copie
                                                            </button>
                                                        )}
                                                    </div>
                                                )
                                            )}
                                        </td>
                                        <td>
                                            {/* Exam Cell */}
                                            {module.isParent ? null : (
                                                module.exam !== null && (
                                                    <div className="note-cell">
                                                        <div className="note-val">{module.exam}</div>
                                                        {module.hasExamCopy && (
                                                            <button className="btn-voir-copie" onClick={() => openPdf(module.examCopyPath)}>
                                                                <Eye size={10} /> Voir Copie
                                                            </button>
                                                        )}
                                                    </div>
                                                )
                                            )}
                                        </td>
                                        <td style={{ textAlign: 'center' }}>
                                            {/* Correction Cell */}
                                            {!module.isParent && (
                                                module.hasCorrection ? (
                                                    <div className="correction-icon" title="Correction disponible" onClick={() => openPdf(module.correctionPath)} style={{ cursor: 'pointer' }}>
                                                        <FileText size={14} />
                                                    </div>
                                                ) : (
                                                    <span style={{ color: '#cbd5e1', fontSize: '0.625rem', fontWeight: 700 }}>N/A</span>
                                                )
                                            )}
                                        </td>
                                        <td>
                                            <div style={{ textAlign: 'center' }}>
                                                <span className={`mention-badge ${module.isRattrapage ? 'mention-assez-bien' : 'mention-tres-bien'}`}>
                                                    {module.isRattrapage ? 'RATTRAPAGE' : 'NORMAL'}
                                                </span>
                                            </div>
                                        </td>
                                        <td>
                                            <div className="final-moy">{module.average?.toFixed(2)}</div>
                                        </td>
                                    </tr>

                                    {/* Subject Rows (for split modules like Languages) */}
                                    {module.isParent && module.subjects?.map((sub, sIdx) => (
                                        <tr key={`${module.id}-sub-${sIdx}`} className="subject-row">
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
                                                    <div className="note-val" style={{ color: '#94a3b8' }}>{sub.ds !== null ? sub.ds : '-'}</div>
                                                    {sub.hasDsCopy && (
                                                        <button className="btn-voir-copie" onClick={() => openPdf(sub.dsCopyPath)}>
                                                            <Eye size={10} /> COPIE
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                            <td>
                                                <div className="note-cell">
                                                    <div className="note-val" style={{ color: '#94a3b8' }}>{sub.exam !== null ? sub.exam : '-'}</div>
                                                    {sub.hasExamCopy && (
                                                        <button className="btn-voir-copie" onClick={() => openPdf(sub.examCopyPath)}>
                                                            <Eye size={10} /> COPIE
                                                        </button>
                                                    )}
                                                </div>
                                            </td>
                                            <td></td>
                                            <td><div style={{ textAlign: 'center', color: '#cbd5e1', fontSize: '0.625rem' }}>Moyenne:</div></td>
                                            <td><div className="final-moy" style={{ fontSize: '0.875rem', color: '#6366f1' }}>{sub.average?.toFixed(2)}</div></td>
                                        </tr>
                                    ))}
                                </Fragment>
                            ))
                        )}
                    </tbody>
                </table>
            </div>


        </div>
    );
};

export default NotesEtudiant;

