import { useState, useEffect, Fragment } from 'react';
import {
    BookOpen,
    ChevronRight,
    Download,
    FileText,
    Printer,
    Trophy,
    History,
    Eye,
    Sparkles,
    Filter,
    Languages,
    MapPin,
    Hash,
    User,
    Calendar,
    Award
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
        studentName: "Chargement...",
        studentId: "...",
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
        modules: [],
        totalModules: 0,
        validatedModules: 0
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
                    setCurrentData({
                        label: `Semestre ${activeSemester}`,
                        average: 0,
                        ects: "0/30",
                        status: "NON DISPONIBLE",
                        modules: [],
                        totalModules: 0,
                        validatedModules: 0
                    });
                    setLoading(false);
                    return;
                }

                const processedModules = data.modules.map((mod, idx) => {
                    const normalizedName = mod.nomDuModule.toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
                    const isLanguages = normalizedName.includes('Langues étrangères') || normalizedName.includes('langues');

                    if (isLanguages) {
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
                            isParent: true,
                            subjects: subjects,
                            isRattrapage: subjects.some(s => s.isRattrapage)
                        };

                    } else {
                        const sub1 = mod.matieres?.[0] || null;
                        const sub2 = mod.matieres?.[1] || null;
                        const sub1Exams = sub1 ? parseExams(sub1.examens) : null;
                        const sub2Exams = sub2 ? parseExams(sub2.examens) : null;
                        const sub1CopyPath = sub1Exams?.dsCopyPath || sub1Exams?.examCopyPath || null;
                        const sub2CopyPath = sub2Exams?.dsCopyPath || sub2Exams?.examCopyPath || null;
                        const correctionPath = sub2Exams?.examCorrection || sub2Exams?.dsCorrection || sub1Exams?.examCorrection || sub1Exams?.dsCorrection;

                        return {
                            id: mod.id || idx,
                            name: mod.nomDuModule,
                            ects: mod.coefficient,
                            coeff: mod.coefficient,
                            ds: sub1 ? sub1.moyenneDuMatiere : null,
                            hasDsCopy: !!sub1CopyPath,
                            dsCopyPath: sub1CopyPath,
                            exam: sub2 ? sub2.moyenneDuMatiere : null,
                            hasExamCopy: !!sub2CopyPath,
                            examCopyPath: sub2CopyPath,
                            hasCorrection: !!correctionPath,
                            correctionPath: correctionPath,
                            mention: getMentionFromGrade(mod.moyenneDuModule),
                            average: mod.moyenneDuModule,
                            letter: mod.nomDuModule.charAt(0).toUpperCase(),
                            isParent: false,
                            isRattrapage: (sub1Exams?.isRattrapage || sub2Exams?.isRattrapage)
                        };
                    }
                });

                const calculatedSessionAverage = processedModules.length > 0
                    ? processedModules.reduce((sum, mod) => sum + (mod.average || 0), 0) / processedModules.length
                    : 0;

                setCurrentData({
                    label: `Semestre ${activeSemester}`,
                    average: data.moyenneSession || calculatedSessionAverage,
                    ects: `${processedModules.reduce((acc, m) => acc + (m.average >= 10 ? 30 / processedModules.length : 0), 0).toFixed(0)}/30`,
                    status: (data.moyenneSession || calculatedSessionAverage) >= 10 ? "VALIDÉ" : "AJOURNÉ",
                    modules: processedModules,
                    totalModules: data.totalModules || processedModules.length,
                    validatedModules: data.validatedModules || processedModules.filter(m => m.average >= 10).length
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

    const getMentionFromGrade = (grade) => {
        if (!grade && grade !== 0) return '-';
        if (grade >= 16) return 'TRÈS BIEN';
        if (grade >= 14) return 'BIEN';
        if (grade >= 12) return 'ASSEZ BIEN';
        if (grade >= 10) return 'PASSABLE';
        return 'AJOURNÉ';
    };

    const openPdf = (path) => {
        if (!path) return;
        // If path is relative, prepend the backend host
        const fullUrl = path.startsWith('http') ? path : `http://localhost:8080/${path}`;
        window.open(fullUrl, '_blank');
    };

    const handlePrint = () => {
        window.print();
    };

    return (
        <div className="notes-container">
            {/* Horizontal Layout Container */}
            <div className="notes-header-layout">
                {/* Main Banner */}
                <header className="notes-main-banner">
                    <div className="banner-title-box">
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                            <Trophy size={20} color="#818cf8" />
                            <span style={{ margin: 0, fontSize: '12px', color: '#94a3b8', letterSpacing: '2px' }}>PORTAIL ACADÉMIQUE</span>
                        </div>
                        <h1>{studentStats.degree}</h1>
                        <span>Spécialité: {studentStats.specialization}</span>
                    </div>

                    <div className="action-buttons-container">
                        <button className="btn-premium" onClick={handlePrint}>
                            <Printer size={16} />
                            IMPRIMER LE RELEVÉ DE NOTES
                        </button>
                    </div>

                    <div className="banner-metrics">
                        <div className="metric-item">
                            <span className="metric-label">MOYENNE GÉNÉRALE</span>
                            <div className="metric-value highlight">
                                {studentStats.generalAverage}<span>/20</span>
                            </div>
                        </div>
                        <div className="metric-item">
                            <span className="metric-label">PARCOURS</span>
                            <div className="metric-value accent">
                                {studentStats.validatedModules}<span>/{studentStats.totalModules}</span>
                            </div>
                        </div>
                    </div>
                </header>

                {/* Sidebar Summary Card */}
                <div className="session-summary-card">
                    <div className="session-summary-header">
                        <div className="session-icon-box">
                            <Sparkles size={24} color="white" />
                        </div>
                        <div className="session-average-display">
                            <span className="session-average-label">MOYENNE SESSION</span>
                            <div className="session-average-value">{currentData.average?.toFixed(2) || '0.00'}</div>
                        </div>
                    </div>

                    <div style={{ marginTop: 'auto' }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '8px' }}>
                            <History size={16} opacity={0.8} />
                            <span style={{ fontSize: '12px', opacity: 0.8, fontWeight: 600 }}>STATUS ACTUEL</span>
                        </div>
                        <h3 style={{ margin: 0, fontSize: '24px', fontWeight: 800 }}>{currentData.status}</h3>
                        <p style={{ margin: '8px 0 0 0', fontSize: '12px', opacity: 0.7, lineHeight: 1.5 }}>
                            {studentStats.sessionMessage}
                        </p>
                    </div>

                    <div className="session-info-footer">
                        <div className="session-mention">Mention: {getMentionFromGrade(currentData.average)}</div>
                    </div>
                </div>
            </div>

            <div className="notes-main-grid">
                {/* Grades Table Section */}
                <div className="grades-detail-card">
                    <div className="grades-header">
                        <div className="grades-title">
                            <div className="title-icon-box">
                                <BookOpen size={20} />
                            </div>
                            <h2>Détails des Modules</h2>
                        </div>

                        <div className="semester-selector">
                            <Filter size={14} color="#6366f1" />
                            <select
                                value={activeSemester}
                                onChange={(e) => setActiveSemester(parseInt(e.target.value))}
                            >
                                {(studentStats.availableSemesters || [1]).map(s => (
                                    <option key={s} value={s}>Semestre {s}</option>
                                ))}
                            </select>
                        </div>

                        <div className="modules-count-info">
                            <span className="modules-count-text">
                                Modules validés: {currentData.validatedModules}/{currentData.totalModules}
                            </span>
                        </div>
                    </div>

                    <div className="notes-table-container">
                        <table className="notes-table">
                            <thead>
                                <tr>
                                    <th style={{ width: '40%' }}>Module / Composante</th>
                                    <th>DS</th>
                                    <th>Examen</th>
                                    <th>Correction</th>
                                    <th>Session</th>
                                    <th>Moyenne</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr><td colSpan="6" style={{ textAlign: 'center', padding: '48px' }}>Chargement en cours...</td></tr>
                                ) : error ? (
                                    <tr><td colSpan="6" style={{ textAlign: 'center', padding: '48px', color: '#ef4444' }}>Erreur de chargement.</td></tr>
                                ) : currentData.modules.length === 0 ? (
                                    <tr><td colSpan="6" style={{ textAlign: 'center', padding: '48px' }}>Aucun résultat pour ce semestre.</td></tr>
                                ) : (
                                    currentData.modules.map((module, idx) => (
                                        <Fragment key={module.id || idx}>
                                            <tr className="module-row">
                                                <td>
                                                    <div className="module-info-cell">
                                                        <div className="module-icon">
                                                            {module.letter}
                                                        </div>
                                                        <div className="module-name">{module.name}</div>
                                                    </div>
                                                </td>
                                                <td>
                                                    {!module.isParent && module.ds !== null && (
                                                        <div className="grade-pill">
                                                            {module.ds}
                                                            {module.hasDsCopy && (
                                                                <button className="btn-copy-small" onClick={() => openPdf(module.dsCopyPath)}>
                                                                    <Eye size={10} /> Copie
                                                                </button>
                                                            )}
                                                        </div>
                                                    )}
                                                </td>
                                                <td>
                                                    {!module.isParent && module.exam !== null && (
                                                        <div className="grade-pill">
                                                            {module.exam}
                                                            {module.hasExamCopy && (
                                                                <button className="btn-copy-small" onClick={() => openPdf(module.examCopyPath)}>
                                                                    <Eye size={10} /> Copie
                                                                </button>
                                                            )}
                                                        </div>
                                                    )}
                                                </td>
                                                <td style={{ textAlign: 'center' }}>
                                                    {!module.isParent && module.hasCorrection && (
                                                        <div className="correction-icon" onClick={() => openPdf(module.correctionPath)} style={{ cursor: 'pointer' }}>
                                                            <FileText size={14} />
                                                        </div>
                                                    )}
                                                </td>
                                                <td>
                                                    <span className={`status-badge ${module.isRattrapage ? 'pending' : 'validated'}`}>
                                                        {module.isRattrapage ? 'Rattrapage' : 'Normal'}
                                                    </span>
                                                </td>
                                                <td>
                                                    <div className="grade-pill" style={{ background: '#eef2ff', color: '#6366f1' }}>
                                                        {module.average?.toFixed(2)}
                                                    </div>
                                                </td>
                                            </tr>

                                            {module.isParent && module.subjects?.map((sub, sIdx) => (
                                                <tr key={`${module.id}-sub-${sIdx}`} className="sub-module-row">
                                                    <td>
                                                        <div className="sub-module-info-cell">
                                                            <div className="sub-module-dash"></div>
                                                            <div className="sub-module-icon-box">
                                                                <Languages size={16} />
                                                            </div>
                                                            <span className="sub-module-name">{sub.name}</span>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div className="grade-display-stacked">
                                                            {sub.ds !== null ? (
                                                                <div className="grade-card">
                                                                    <div className="grade-card-left">
                                                                        <span className="grade-number">{sub.ds}</span>
                                                                    </div>
                                                                    <div className="grade-card-right">
                                                                        {sub.hasDsCopy && (
                                                                            <button className="btn-grade-copy" onClick={() => openPdf(sub.dsCopyPath)}>
                                                                                <Eye size={12} /> COPIE
                                                                            </button>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                            ) : (
                                                                <div className="grade-card" style={{ opacity: 0.6 }}>
                                                                    <div className="grade-card-left">
                                                                        <span className="grade-number">-</span>
                                                                    </div>
                                                                    <div className="grade-card-right">
                                                                        <span style={{ fontSize: '11px', color: '#94a3b8' }}>Non noté</span>
                                                                    </div>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <div className="grade-display-stacked">
                                                            {sub.exam !== null ? (
                                                                <div className="grade-card">
                                                                    <div className="grade-card-left">
                                                                        <span className="grade-number">{sub.exam}</span>
                                                                    </div>
                                                                    <div className="grade-card-right">
                                                                        {sub.hasExamCopy && (
                                                                            <button className="btn-grade-copy" onClick={() => openPdf(sub.examCopyPath)}>
                                                                                <Eye size={12} /> COPIE
                                                                            </button>
                                                                        )}
                                                                    </div>
                                                                </div>
                                                            ) : (
                                                                <div className="grade-card" style={{ opacity: 0.6 }}>
                                                                    <div className="grade-card-left">
                                                                        <span className="grade-number">-</span>
                                                                    </div>
                                                                    <div className="grade-card-right">
                                                                        <span style={{ fontSize: '11px', color: '#94a3b8' }}>Non noté</span>
                                                                    </div>
                                                                </div>
                                                            )}
                                                        </div>
                                                    </td>
                                                    <td></td>
                                                    <td></td>
                                                    <td>
                                                        <div className="sub-module-average-cell">
                                                            <div className="grade-pill" style={{ background: '#eef2ff', color: '#6366f1' }}>
                                                                {sub.average?.toFixed(2)}
                                                            </div>
                                                        </div>
                                                    </td>
                                                </tr>
                                            ))}
                                        </Fragment>
                                    ))
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>
            {/* --- PRINTABLE TRANSCRIPT TEMPLATE --- */}
            <div className="transcript-printable-area">
                <div className="transcript-content">
                    {/* Official Header */}
                    <header className="transcript-header">
                        <div className="uni-branding">
                            <div className="uni-logo-box">
                                <Award size={32} />
                            </div>
                            <div className="uni-names">
                                <h1>MyCampus</h1>
                                <div className="uni-contact-info">
                                    <div className="contact-item">
                                        <MapPin size={12} />
                                        <span>Avenue Mohammed V, BP 1014, Rabat, Maroc</span>
                                    </div>
                                    <div className="contact-item">
                                        <Hash size={12} />
                                        <span>+212 5 37 77 00 00 | contact@MyCampus.ac.ma</span>
                                    </div>
                                </div>
                            </div>
                        </div>
                        <div className="transcript-ref-container">
                            <div className="transcript-ref-badge">Document Officiel</div>
                            <div className="transcript-ref-details">
                                <span className="ref-label">Référence Document</span>
                                <span className="ref-value">RN-S{activeSemester}-2024-X</span>
                            </div>
                        </div>
                    </header>

                    <hr style={{ border: 'none', height: '3px', background: '#0f172a', margin: '1.5rem 0' }} />

                    {/* Title Section */}
                    <div className="transcript-title-section">
                        <h2>RELEVÉ DE NOTES ET RÉSULTATS</h2>
                        <div className="semester-info-line">
                            <div className="line-decor"></div>
                            <span>SEMESTRE {activeSemester} - SESSION {activeSemester % 2 === 0 ? 'PRINTEMPS' : 'AUTOMNE'} 2024</span>
                            <div className="line-decor"></div>
                        </div>
                    </div>

                    {/* Student Info Grid */}
                    <div className="transcript-student-grid">
                        <div className="info-card">
                            <div className="info-icon-box"><User size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">NOM ET PRÉNOM</span>
                                <span className="info-value">{studentStats.studentName}</span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><BookOpen size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">FILIÈRE / NIVEAU</span>
                                <span className="info-value">{studentStats.specialization} - {activeSemester > 4 ? '3ème Année (L3)' : activeSemester > 2 ? '2ème Année (L2)' : '1ère Année (L1)'}</span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><Hash size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">NUMÉRO D'ÉTUDIANT (CNE/PPR)</span>
                                <span className="info-value">{studentStats.studentId}</span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><Calendar size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">ANNÉE UNIVERSITAIRE</span>
                                <span className="info-value">2023 / 2024</span>
                            </div>
                        </div>
                    </div>

                    {/* Table */}
                    <table className="transcript-grades-table">
                        <thead>
                            <tr>
                                <th style={{ width: '15%' }}>CODE</th>
                                <th style={{ width: '55%' }}>INTITULÉ DU MODULE</th>
                                <th style={{ width: '15%', textAlign: 'center' }}>NOTE / 20</th>
                                <th style={{ width: '15%', textAlign: 'right' }}>RÉSULTAT</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentData.modules.length > 0 ? currentData.modules.map((module, idx) => (
                                <Fragment key={idx}>
                                    <tr className="row-module">
                                        <td className="col-code">{module.letter || 'M'}</td>
                                        <td className="col-name">{module.name}</td>
                                        <td className="col-grade">{module.average?.toFixed(2)}</td>
                                        <td className="col-result">{module.average >= 10 ? 'VALIDÉ' : 'AJOURNÉ'}</td>
                                    </tr>
                                    {module.isParent && module.subjects?.map((sub, sIdx) => (
                                        <tr key={`${idx}-sub-${sIdx}`} className="row-sub">
                                            <td className="col-code"></td>
                                            <td className="col-name">{sub.name}</td>
                                            <td className="col-grade">{sub.average?.toFixed(2)}</td>
                                            <td className="col-result"></td>
                                        </tr>
                                    ))}
                                </Fragment>
                            )) : (
                                <tr>
                                    <td colSpan="4" style={{ textAlign: 'center', padding: '2rem' }}>Aucun module disponible pour ce semestre.</td>
                                </tr>
                            )}
                        </tbody>
                    </table>

                    {/* Results Footer Box */}
                    <div className="transcript-results-summary">
                        <div className="summary-stats-box">
                            <div className="stat-row">
                                <span className="stat-label">Moyenne du Semestre</span>
                                <div className="stat-val">{currentData.average?.toFixed(2)}<span>/20</span></div>
                            </div>
                            <div className="stat-row mention-row">
                                <span className="stat-label">Mention</span>
                                <div className="stat-val">{getMentionFromGrade(currentData.average)}</div>
                            </div>
                            <div style={{ position: 'absolute', top: '1.5rem', right: '1.5rem', opacity: 0.1 }}>
                                <Award size={64} />
                            </div>
                        </div>

                        <div className="admin-validation">
                            <span className="admin-auth-text">Le Doyen de la Faculté</span>
                            
                            <p style={{ fontSize: '8px', color: '#94a3b8', maxWidth: '180px', margin: 0 }}>
                               Signature & Cachet
                            </p>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NotesEtudiant;

