import { useState, useEffect, Fragment } from 'react';
import {
    Award,
    BookOpen,
    Calendar,
    ChevronDown,
    ChevronRight,
    Eye,
    FileText,
    Filter,
    Hash,
    History,
    Languages,
    MapPin,
    Printer,
    Sparkles,
    Trophy,
    User
} from 'lucide-react';
import './NotesEtudiant.css';
import { apiGrades } from '../services/apiGrades';
import { parseExams } from '../utils/academicUtils';
import {
    fetchAuthenticatedBlob,
    normalizeStoredFileUrl,
    openBlobInNewTab
} from '../utils/fileAccess';

const buildMatiereKey = (moduleId, matiereId) => `${moduleId || 'module'}-${matiereId || 'matiere'}`;

const formatNote = (note) => (note || note === 0 ? Number(note).toFixed(2) : '-');

const sortExamEntries = (exams = []) => {
    const order = { NORMAL: 0, RATTRAPAGE: 1 };

    return [...exams].sort((a, b) => {
        const typeDiff = (order[a.typeExamen] ?? 99) - (order[b.typeExamen] ?? 99);
        if (typeDiff !== 0) return typeDiff;
        return (a.numeroExamen || 0) - (b.numeroExamen || 0);
    });
};

const normalizeExamLabel = (exam) => {
    if (exam.typeExamen === 'RATTRAPAGE') {
        return `Rattrapage ${exam.numeroExamen || ''}`.trim();
    }

    if (exam.numeroExamen === 1) return 'DS';
    if (exam.numeroExamen === 2) return 'Examen final';
    return `Examen ${exam.numeroExamen || ''}`.trim();
};

const NotesEtudiant = () => {
    const [activeSemester, setActiveSemester] = useState(1);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [selectedMatiereKey, setSelectedMatiereKey] = useState(null);
    const [collapsedExamLists, setCollapsedExamLists] = useState({});

    const [studentStats, setStudentStats] = useState({
        degree: "Diplome d'Ingenieur",
        specialization: 'Chargement...',
        studentName: 'Chargement...',
        studentId: '...',
        generalAverage: null,
        hasCompleteTranscript: false,
        validatedModules: 0,
        totalModules: 0,
        sessionStatus: '...',
        sessionMessage: 'En attente...',
        finalMention: '...',
        certId: 'LUM-2024-PENDING',
        maxSemester: 1,
        availableSemesters: []
    });

    const [currentData, setCurrentData] = useState({
        label: `Semestre ${activeSemester}`,
        average: 0,
        ects: '0/30',
        status: 'EN COURS',
        modules: [],
        totalModules: 0,
        validatedModules: 0
    });

    useEffect(() => {
        const fetchStats = async () => {
            try {
                const stats = await apiGrades.fetchGlobalStats();
                setStudentStats((prev) => ({
                    ...prev,
                    ...stats,
                    sessionMessage: stats.currentSessionStatus === 'Admis'
                        ? 'Felicitations pour vos resultats !'
                        : 'Continuez vos efforts.'
                }));
                setActiveSemester(1);
            } catch (err) {
                console.error('Failed to fetch stats', err);
            }
        };

        fetchStats();
    }, []);

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
                        ects: '0/30',
                        status: 'NON DISPONIBLE',
                        modules: [],
                        totalModules: 0,
                        validatedModules: 0
                    });
                    setSelectedMatiereKey(null);
                    setCollapsedExamLists({});
                    setLoading(false);
                    return;
                }

                const processedModules = (data.modules || []).map((mod, idx) => {
                    const normalizedName = (mod.nomDuModule || '')
                        .toLowerCase()
                        .normalize('NFD')
                        .replace(/[\u0300-\u036f]/g, '');
                    const isLanguages = normalizedName.includes('langues');

                    const subjects = (mod.matieres || []).map((mat, matIdx) => {
                        const parsedExams = parseExams(mat.examens || []);
                        const exams = sortExamEntries(parsedExams.exams || []).map((exam, examIdx) => ({
                            id: exam.id || `${mod.id || idx}-${mat.id || matIdx}-${examIdx}`,
                            typeExamen: exam.typeExamen,
                            numeroExamen: exam.numeroExamen,
                            label: normalizeExamLabel(exam),
                            note: exam.note,
                            corrigePath: exam.corrigePath,
                            copieEtudiantPath: exam.copieEtudiantPath
                        }));

                        return {
                            id: mat.id || `${mod.id || idx}-mat-${matIdx}`,
                            code: mat.codeMatiere,
                            name: mat.nomDuMatiere,
                            ds: parsedExams.ds,
                            exam: parsedExams.exam,
                            average: mat.moyenneDuMatiere,
                            hasDsCopy: parsedExams.hasDsCopy,
                            hasExamCopy: parsedExams.hasExamCopy,
                            dsCopyPath: parsedExams.dsCopyPath,
                            examCopyPath: parsedExams.examCopyPath,
                            hasCorrection: !!(parsedExams.examCorrection || parsedExams.dsCorrection || parsedExams.retakeCorrection),
                            correctionPath: parsedExams.examCorrection || parsedExams.dsCorrection || parsedExams.retakeCorrection,
                            isRattrapage: parsedExams.isRattrapage,
                            exams
                        };
                    });

                    const sub1 = subjects[0] || null;
                    const sub2 = subjects[1] || null;
                    const sub1CopyPath = sub1?.dsCopyPath || sub1?.examCopyPath || null;
                    const sub2CopyPath = sub2?.dsCopyPath || sub2?.examCopyPath || null;
                    const correctionPath = sub2?.correctionPath || sub1?.correctionPath || null;

                    return {
                        id: mod.id || idx,
                        name: mod.nomDuModule,
                        ects: mod.coefficient,
                        coeff: mod.coefficient,
                        ds: sub1 ? sub1.average : null,
                        hasDsCopy: !!sub1CopyPath,
                        dsCopyPath: sub1CopyPath,
                        exam: sub2 ? sub2.average : null,
                        hasExamCopy: !!sub2CopyPath,
                        examCopyPath: sub2CopyPath,
                        hasCorrection: !!correctionPath,
                        correctionPath,
                        mention: getMentionFromGrade(mod.moyenneDuModule),
                        average: mod.moyenneDuModule,
                        letter: (mod.nomDuModule || 'M').charAt(0).toUpperCase(),
                        isParent: isLanguages,
                        subjects,
                        isRattrapage: subjects.some((subject) => subject.isRattrapage)
                    };
                });

                const calculatedSessionAverage = processedModules.length > 0
                    ? processedModules.reduce((sum, mod) => sum + (mod.average || 0), 0) / processedModules.length
                    : 0;

                setCurrentData({
                    label: `Semestre ${activeSemester}`,
                    average: data.moyenneSession || calculatedSessionAverage,
                    ects: `${processedModules.reduce((acc, mod) => acc + (mod.average >= 10 ? 30 / processedModules.length : 0), 0).toFixed(0)}/30`,
                    status: (data.moyenneSession || calculatedSessionAverage) >= 10 ? 'VALIDE' : 'AJOURNE',
                    modules: processedModules,
                    totalModules: data.totalModules || processedModules.length,
                    validatedModules: data.validatedModules || processedModules.filter((mod) => mod.average >= 10).length
                });
                setSelectedMatiereKey(null);
                setCollapsedExamLists({});
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
        if (grade >= 16) return 'TRES BIEN';
        if (grade >= 14) return 'BIEN';
        if (grade >= 12) return 'ASSEZ BIEN';
        if (grade >= 10) return 'PASSABLE';
        return 'AJOURNE';
    };

    const openPdf = async (path) => {
        if (!path) return;

        try {
            const fullUrl = normalizeStoredFileUrl(path);
            const response = await fetchAuthenticatedBlob(fullUrl);
            const blob = await response.blob();
            openBlobInNewTab(blob);
        } catch (err) {
            console.error('Erreur ouverture PDF:', err);
            setError(err.message || 'Impossible d ouvrir le document.');
        }
    };

    const handlePrint = () => {
        window.print();
    };

    const toggleMatiere = (moduleId, matiereId) => {
        const nextKey = buildMatiereKey(moduleId, matiereId);
        setSelectedMatiereKey((current) => (current === nextKey ? null : nextKey));
    };

    const toggleExamList = (moduleId, matiereId) => {
        const key = buildMatiereKey(moduleId, matiereId);
        setCollapsedExamLists((current) => ({
            ...current,
            [key]: !current[key]
        }));
    };

    const renderMatiereDetails = (module, subject) => {
        const subjectKey = buildMatiereKey(module.id, subject.id);
        const isCollapsed = !!collapsedExamLists[subjectKey];

        return (
            <div className="exam-detail-panel">
                <div className="exam-detail-header">
                    <div>
                        <span className="exam-detail-eyebrow">Examens passes</span>
                        <h3>{subject.name}</h3>
                        <p>{module.name}</p>
                    </div>
                    <div className="exam-detail-header-actions">
                        <button
                            type="button"
                            className="exam-collapse-button"
                            onClick={() => toggleExamList(module.id, subject.id)}
                        >
                            {isCollapsed ? <ChevronRight size={14} /> : <ChevronDown size={14} />}
                            {isCollapsed ? 'Afficher les examens' : 'Masquer les examens'}
                        </button>
                    </div>
                </div>

                {!isCollapsed && (
                    subject.exams?.length > 0 ? (
                        <div className="exam-compact-list">
                            {subject.exams.map((exam) => (
                                <div key={exam.id} className="exam-compact-row">
                                    <div className="exam-compact-name">{exam.label}</div>
                                    <div className="exam-compact-status">
                                        <span className={`status-badge ${exam.typeExamen === 'RATTRAPAGE' ? 'pending' : 'validated'}`}>
                                            {exam.typeExamen === 'RATTRAPAGE' ? 'Rattrapage' : 'Normal'}
                                        </span>
                                    </div>
                                    <div className="exam-compact-grade">{formatNote(exam.note)}</div>
                                    <div className="exam-compact-actions">
                                        {exam.copieEtudiantPath && (
                                            <button type="button" className="btn-grade-copy" onClick={() => openPdf(exam.copieEtudiantPath)}>
                                                <Eye size={12} />
                                                Copie etudiant
                                            </button>
                                        )}
                                        {exam.corrigePath && (
                                            <button type="button" className="btn-grade-copy secondary" onClick={() => openPdf(exam.corrigePath)}>
                                                <FileText size={12} />
                                                Correction
                                            </button>
                                        )}
                                    </div>
                                </div>
                            ))}
                        </div>
                    ) : (
                        <div className="exam-empty-state">Aucun examen note pour cette matiere.</div>
                    )
                )}
            </div>
        );
    };

    return (
        <div className="notes-container">
            <div className="notes-header-layout">
                <header className="notes-main-banner">
                    <div className="banner-title-box">
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px', marginBottom: '8px' }}>
                            <Trophy size={20} color="#818cf8" />
                            <span style={{ margin: 0, fontSize: '12px', color: '#94a3b8', letterSpacing: '2px' }}>PORTAIL ACADEMIQUE</span>
                        </div>
                        <h1>{studentStats.degree}</h1>
                        <span>Specialite: {studentStats.specialization}</span>
                    </div>

                    <div className="action-buttons-container">
                        <button className="btn-premium" onClick={handlePrint}>
                            <Printer size={16} />
                            IMPRIMER LE RELEVE DE NOTES
                        </button>
                    </div>

                    <div className="banner-metrics">
                        {studentStats.hasCompleteTranscript && studentStats.generalAverage !== null && (
                            <div className="metric-item">
                                <span className="metric-label">MOYENNE GENERALE</span>
                                <div className="metric-value highlight">
                                    {studentStats.generalAverage}<span>/20</span>
                                </div>
                            </div>
                        )}
                    </div>
                </header>

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
                <div className="grades-detail-card">
                    <div className="grades-header">
                        <div className="grades-title">
                            <div className="title-icon-box">
                                <BookOpen size={20} />
                            </div>
                            <h2>Details des Modules</h2>
                        </div>

                        <div className="semester-selector">
                            <Filter size={14} color="#6366f1" />
                            <select
                                value={activeSemester}
                                onChange={(e) => setActiveSemester(parseInt(e.target.value, 10))}
                            >
                                {(studentStats.availableSemesters || [1]).map((semester) => (
                                    <option key={semester} value={semester}>Semestre {semester}</option>
                                ))}
                            </select>
                        </div>

                        <div className="modules-count-info">
                            <span className="modules-count-text">
                                Modules valides: {currentData.validatedModules}/{currentData.totalModules}
                            </span>
                        </div>
                    </div>

                    <div className="notes-table-container">
                        <table className="notes-table">
                            <thead>
                                <tr>
                                    <th style={{ width: '60%' }}>Module / Matiere</th>
                                    <th>Session</th>
                                    <th>Moyenne</th>
                                </tr>
                            </thead>
                            <tbody>
                                {loading ? (
                                    <tr><td colSpan="3" style={{ textAlign: 'center', padding: '48px' }}>Chargement en cours...</td></tr>
                                ) : error ? (
                                    <tr><td colSpan="3" style={{ textAlign: 'center', padding: '48px', color: '#ef4444' }}>Erreur de chargement.</td></tr>
                                ) : currentData.modules.length === 0 ? (
                                    <tr><td colSpan="3" style={{ textAlign: 'center', padding: '48px' }}>Aucun resultat pour ce semestre.</td></tr>
                                ) : (
                                    currentData.modules.map((module, idx) => {
                                        const hasSingleSubject = (module.subjects?.length || 0) === 1;
                                        const moduleSubject = hasSingleSubject ? module.subjects[0] : null;
                                        const moduleSubjectKey = moduleSubject
                                            ? buildMatiereKey(module.id, moduleSubject.id)
                                            : null;
                                        const selectedSubject = module.subjects?.find(
                                            (subject) => buildMatiereKey(module.id, subject.id) === selectedMatiereKey
                                        ) || null;

                                        return (
                                            <Fragment key={module.id || idx}>
                                                <tr
                                                    className={`module-row ${selectedSubject && !module.isParent ? 'selected' : ''} ${hasSingleSubject ? 'clickable' : ''}`}
                                                    onClick={hasSingleSubject ? () => toggleMatiere(module.id, moduleSubject.id) : undefined}
                                                >
                                                    <td>
                                                        <div className="module-info-cell">
                                                            <div className="module-icon">
                                                                {module.letter}
                                                            </div>
                                                            <div className="module-name-block">
                                                                <div className="module-name">{module.name}</div>
                                                                {hasSingleSubject && moduleSubject && (
                                                                    <div className="single-subject-hint">
                                                                        {selectedMatiereKey === moduleSubjectKey ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                                                                        {moduleSubject.code || moduleSubject.name}
                                                                    </div>
                                                                )}
                                                                {(module.subjects?.length || 0) > 1 && (
                                                                    <div className="subject-selector-inline">
                                                                        {module.subjects.map((subject) => {
                                                                            const subjectKey = buildMatiereKey(module.id, subject.id);
                                                                            const isActive = selectedMatiereKey === subjectKey;

                                                                            return (
                                                                                <button
                                                                                    key={subjectKey}
                                                                                    type="button"
                                                                                    className={`subject-chip ${isActive ? 'active' : ''}`}
                                                                                    onClick={() => toggleMatiere(module.id, subject.id)}
                                                                                >
                                                                                    {isActive ? <ChevronDown size={12} /> : <ChevronRight size={12} />}
                                                                                    {subject.code || subject.name}
                                                                                </button>
                                                                            );
                                                                        })}
                                                                    </div>
                                                                )}
                                                            </div>
                                                        </div>
                                                    </td>
                                                    <td>
                                                        <span className={`status-badge ${module.isRattrapage ? 'pending' : 'validated'}`}>
                                                            {module.isRattrapage ? 'Rattrapage' : 'Normal'}
                                                        </span>
                                                    </td>
                                                    <td>
                                                        <div className="grade-pill" style={{ background: '#eef2ff', color: '#6366f1' }}>
                                                            {formatNote(module.average)}
                                                        </div>
                                                    </td>
                                                </tr>

                                                {!module.isParent && selectedSubject && (
                                                    <tr className="exam-detail-row">
                                                        <td colSpan="3">
                                                            {renderMatiereDetails(module, selectedSubject)}
                                                        </td>
                                                    </tr>
                                                )}

                                                {module.isParent && module.subjects?.map((subject, subjectIdx) => {
                                                    const subjectKey = buildMatiereKey(module.id, subject.id);
                                                    const isSelected = selectedMatiereKey === subjectKey;

                                                    return (
                                                        <Fragment key={`${module.id}-sub-${subjectIdx}`}>
                                                            <tr
                                                                className={`sub-module-row ${isSelected ? 'selected' : ''}`}
                                                                onClick={() => toggleMatiere(module.id, subject.id)}
                                                            >
                                                                <td>
                                                                    <div className="sub-module-info-cell">
                                                                        <div className="sub-module-dash"></div>
                                                                        <div className="sub-module-icon-box">
                                                                            <Languages size={16} />
                                                                        </div>
                                                                        <span className="sub-module-name">{subject.name}</span>
                                                                    </div>
                                                                </td>
                                                                <td>
                                                                    <span className={`status-badge ${subject.isRattrapage ? 'pending' : 'validated'}`}>
                                                                        {subject.isRattrapage ? 'Rattrapage' : 'Normal'}
                                                                    </span>
                                                                </td>
                                                                <td>
                                                                    <div className="sub-module-average-cell">
                                                                        <div className="grade-pill" style={{ background: '#eef2ff', color: '#6366f1' }}>
                                                                            {formatNote(subject.average)}
                                                                        </div>
                                                                    </div>
                                                                </td>
                                                            </tr>

                                                            {isSelected && (
                                                                <tr className="exam-detail-row">
                                                                    <td colSpan="3">
                                                                        {renderMatiereDetails(module, subject)}
                                                                    </td>
                                                                </tr>
                                                            )}
                                                        </Fragment>
                                                    );
                                                })}
                                            </Fragment>
                                        );
                                    })
                                )}
                            </tbody>
                        </table>
                    </div>
                </div>
            </div>

            <div className="transcript-printable-area">
                <div className="transcript-content">
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
                                <span className="ref-label">Reference Document</span>
                                <span className="ref-value">RN-S{activeSemester}-2024-X</span>
                            </div>
                        </div>
                    </header>

                    <hr style={{ border: 'none', height: '3px', background: '#0f172a', margin: '1.5rem 0' }} />

                    <div className="transcript-title-section">
                        <h2>RELEVE DE NOTES ET RESULTATS</h2>
                        <div className="semester-info-line">
                            <div className="line-decor"></div>
                            <span>SEMESTRE {activeSemester} - SESSION {activeSemester % 2 === 0 ? 'PRINTEMPS' : 'AUTOMNE'} 2024</span>
                            <div className="line-decor"></div>
                        </div>
                    </div>

                    <div className="transcript-student-grid">
                        <div className="info-card">
                            <div className="info-icon-box"><User size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">NOM ET PRENOM</span>
                                <span className="info-value">{studentStats.studentName}</span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><BookOpen size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">FILIERE / NIVEAU</span>
                                <span className="info-value">
                                    {studentStats.specialization} - {activeSemester > 4 ? '3eme Annee (L3)' : activeSemester > 2 ? '2eme Annee (L2)' : '1ere Annee (L1)'}
                                </span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><Hash size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">NUMERO D ETUDIANT (CNE/PPR)</span>
                                <span className="info-value">{studentStats.studentId}</span>
                            </div>
                        </div>
                        <div className="info-card">
                            <div className="info-icon-box"><Calendar size={20} /></div>
                            <div className="info-details">
                                <span className="info-label">ANNEE UNIVERSITAIRE</span>
                                <span className="info-value">2023 / 2024</span>
                            </div>
                        </div>
                    </div>

                    <table className="transcript-grades-table">
                        <thead>
                            <tr>
                                <th style={{ width: '15%' }}>CODE</th>
                                <th style={{ width: '55%' }}>INTITULE DU MODULE</th>
                                <th style={{ width: '15%', textAlign: 'center' }}>NOTE / 20</th>
                                <th style={{ width: '15%', textAlign: 'right' }}>RESULTAT</th>
                            </tr>
                        </thead>
                        <tbody>
                            {currentData.modules.length > 0 ? currentData.modules.map((module, idx) => (
                                <Fragment key={idx}>
                                    <tr className="row-module">
                                        <td className="col-code">{module.letter || 'M'}</td>
                                        <td className="col-name">{module.name}</td>
                                        <td className="col-grade">{formatNote(module.average)}</td>
                                        <td className="col-result">{module.average >= 10 ? 'VALIDE' : 'NON VALIDE'}</td>
                                    </tr>
                                    {module.isParent && module.subjects?.map((subject, subjectIdx) => (
                                        <tr key={`${idx}-sub-${subjectIdx}`} className="row-sub">
                                            <td className="col-code"></td>
                                            <td className="col-name">{subject.name}</td>
                                            <td className="col-grade">{formatNote(subject.average)}</td>
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

                    <div className="transcript-results-summary">
                        <div className="summary-stats-box">
                            <div className="stat-row">
                                <span className="stat-label">Moyenne du Semestre</span>
                                <div className="stat-val">{currentData.average?.toFixed(2)}<span>/20</span></div>
                            </div>
                            <div className="stat-row">
                                <span className="stat-label">Resultat Global</span>
                                <div className="stat-val">{currentData.average >= 10 ? 'VALIDE' : 'AJOURNE'}</div>
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
                            <span className="admin-auth-text">Le Doyen de la Faculte</span>
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
