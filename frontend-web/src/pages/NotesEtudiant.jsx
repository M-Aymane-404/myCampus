import { useState, useEffect, Fragment } from 'react';
import {
    BookOpen,
    ChevronDown,
    ChevronRight,
    Eye,
    FileText,
    Filter,
    History,
    Languages,
    Printer,
    Trophy
} from 'lucide-react';
import './NotesEtudiant.css';
import { apiGrades } from '../services/apiGrades';
import { apiEtudiant } from '../services/apiEtudiant';
import { apiCours } from '../services/apiCours';
import { parseExams } from '../utils/academicUtils';
import {
    openBlobInNewTab
} from '../utils/fileAccess';

const buildMatiereKey = (moduleId, matiereId) => `${moduleId || 'module'}-${matiereId || 'matiere'}`;

const formatNote = (note) => {
    if (note === null || note === undefined) return 'Note non disponible';
    const parsed = Number(note);
    if (!Number.isFinite(parsed)) return 'Note non disponible';
    return parsed.toFixed(2);
};

const formatSessionAverage = (average, hasSessionAverage) => (
    hasSessionAverage ? formatNote(average) : 'Moyenne non disponible'
);

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

const normalizeSemesterCode = (value) => {
    if (typeof value === 'number' && Number.isFinite(value)) {
        const numeric = Math.max(1, Math.trunc(value));
        return `S${numeric}`;
    }

    const raw = String(value || '').trim().toUpperCase();
    if (!raw) return null;
    if (/^S\d+$/.test(raw)) return raw;
    if (/^\d+$/.test(raw)) return `S${parseInt(raw, 10)}`;
    return null;
};

const extractSemesterNumber = (value) => {
    const code = normalizeSemesterCode(value);
    if (!code) return 1;
    return parseInt(code.replace('S', ''), 10);
};

const normalizeSemesterOptions = (values) => {
    const unique = new Set();
    (Array.isArray(values) ? values : []).forEach((value) => {
        const normalized = normalizeSemesterCode(value);
        if (normalized) {
            unique.add(normalized);
        }
    });

    if (unique.size === 0) {
        unique.add('S1');
    }

    return Array.from(unique).sort((left, right) => extractSemesterNumber(left) - extractSemesterNumber(right));
};

const normalizeText = (value) => String(value || '')
    .trim()
    .toLowerCase()
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '');

const normalizeCode = (value) => String(value || '').trim().toUpperCase();

const isLanguagesModule = (moduleName) => normalizeText(moduleName).includes('langues');

const hasAnyNumericGrade = (modules = []) =>
    modules.some((moduleItem) => Number.isFinite(Number(moduleItem?.average)));

const parseNumericGrade = (value) => {
    const numeric = Number(value);
    return Number.isFinite(numeric) ? numeric : null;
};

const mergeDashboardNotesIntoModules = (modules = [], dashboardNotes = []) => {
    if (!Array.isArray(modules) || modules.length === 0) return modules;
    if (!Array.isArray(dashboardNotes) || dashboardNotes.length === 0) return modules;

    const notesByMatiere = new Map();
    dashboardNotes.forEach((entry) => {
        const key = normalizeText(entry?.matiere || entry?.matiereNom || entry?.nomMatiere);
        const note = parseNumericGrade(entry?.note);

        if (!key || note === null) return;
        const queue = notesByMatiere.get(key) || [];
        queue.push(note);
        notesByMatiere.set(key, queue);
    });

    if (notesByMatiere.size === 0) return modules;

    return modules.map((moduleItem) => {
        const subjects = Array.isArray(moduleItem?.subjects) ? moduleItem.subjects : [];
        const mergedSubjects = subjects.map((subject) => {
            const key = normalizeText(subject?.name || subject?.code);
            const queue = key ? notesByMatiere.get(key) : null;
            if (!queue || queue.length === 0) {
                return subject;
            }

            const alreadyHasGrade = Number.isFinite(Number(subject?.average));
            if (alreadyHasGrade) {
                return subject;
            }

            const nextNote = queue.shift();
            return {
                ...subject,
                average: nextNote,
                exam: nextNote
            };
        });

        const currentAverage = Number(moduleItem?.average);
        if (Number.isFinite(currentAverage)) {
            return {
                ...moduleItem,
                subjects: mergedSubjects
            };
        }

        const subjectAverages = mergedSubjects
            .map((subject) => Number(subject?.average))
            .filter((value) => Number.isFinite(value));

        const fallbackAverage = subjectAverages.length > 0
            ? subjectAverages.reduce((sum, value) => sum + value, 0) / subjectAverages.length
            : null;

        return {
            ...moduleItem,
            subjects: mergedSubjects,
            average: fallbackAverage
        };
    });
};

const NotesEtudiant = () => {
    const [activeSemester, setActiveSemester] = useState('S1');
    const [loading, setLoading] = useState(true);
    const [loadError, setLoadError] = useState(null);
    const [documentError, setDocumentError] = useState('');
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
        availableSemesters: ['S1']
    });

    const [currentData, setCurrentData] = useState({
        label: `Semestre ${extractSemesterNumber(activeSemester)}`,
        average: 0,
        hasSessionAverage: false,
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
                const normalizedSemesters = normalizeSemesterOptions(stats.availableSemesters);

                setStudentStats((prev) => ({
                    ...prev,
                    ...stats,
                    availableSemesters: normalizedSemesters,
                    sessionMessage: stats.currentSessionStatus === 'Admis'
                        ? 'Felicitations pour vos resultats !'
                        : 'Continuez vos efforts.'
                }));
                setActiveSemester((previousSemester) => {
                    const normalizedPrevious = normalizeSemesterCode(previousSemester);
                    if (normalizedPrevious && normalizedSemesters.includes(normalizedPrevious)) {
                        return normalizedPrevious;
                    }

                    return normalizedSemesters[0] || 'S1';
                });
            } catch (err) {
                console.error('Failed to fetch stats', err);
            }
        };

        fetchStats();
    }, []);

    useEffect(() => {
        const fetchGrades = async () => {
            setLoading(true);
            setLoadError(null);
            setDocumentError('');

            try {
                const data = await apiGrades.fetchStudentGrades(activeSemester);
                const activeSemesterCode = normalizeSemesterCode(activeSemester) || 'S1';
                const activeSemesterNumber = extractSemesterNumber(activeSemesterCode);
                const gradeData = data && typeof data === 'object' ? data : { modules: [] };

                const processedModules = (Array.isArray(gradeData.modules) ? gradeData.modules : []).map((mod, idx) => {
                    const moduleName = mod.nomDuModule || mod.nomModule || mod.nom || mod.moduleName || `Module ${idx + 1}`;
                    const moduleSemesterCode = normalizeSemesterCode(
                        mod.semestre
                        || mod.codeSemestre
                        || mod.semestreModule
                        || mod.semester
                    );

                    const normalizedName = moduleName
                        .toLowerCase()
                        .normalize('NFD')
                        .replace(/[\u0300-\u036f]/g, '');
                    const isLanguages = normalizedName.includes('langues');

                    const subjects = (Array.isArray(mod.matieres) ? mod.matieres : []).map((mat, matIdx) => {
                        const rawExams = Array.isArray(mat.examens) ? mat.examens : [];
                        const normalizedRawExams = rawExams.map((exam) => ({
                            ...exam,
                            note: exam?.note ?? exam?.noteExamen ?? exam?.noteFinale ?? exam?.valeurNote ?? null
                        }));
                        const parsedExams = parseExams(normalizedRawExams);
                        const exams = sortExamEntries(parsedExams.exams || []).map((exam, examIdx) => ({
                            id: exam.id || `${mod.id || idx}-${mat.id || matIdx}-${examIdx}`,
                            typeExamen: exam.typeExamen,
                            numeroExamen: exam.numeroExamen,
                            label: normalizeExamLabel(exam),
                            note: exam.note ?? exam.noteExamen ?? exam.noteFinale ?? exam.valeurNote,
                            corrigePath: exam.corrigePath,
                            copieEtudiantPath: exam.copieEtudiantPath
                        }));

                        return {
                            id: mat.id || `${mod.id || idx}-mat-${matIdx}`,
                            code: mat.codeMatiere || mat.code || mat.codeModule,
                            name: mat.nomDuMatiere || mat.nomMatiere || mat.nom || mat.name || `Matiere ${matIdx + 1}`,
                            ds: parsedExams.ds,
                            exam: parsedExams.exam,
                            average: mat.moyenneDuMatiere ?? mat.moyenneMatiere ?? mat.moyenne ?? null,
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
                        codeModule: mod.codeModule || mod.code || mod.codeDuModule || '',
                        name: moduleName,
                        ects: mod.coefficient ?? mod.ects ?? 0,
                        coeff: mod.coefficient ?? mod.ects ?? 0,
                        ds: sub1 ? sub1.average : null,
                        hasDsCopy: !!sub1CopyPath,
                        dsCopyPath: sub1CopyPath,
                        exam: sub2 ? sub2.average : null,
                        hasExamCopy: !!sub2CopyPath,
                        examCopyPath: sub2CopyPath,
                        hasCorrection: !!correctionPath,
                        correctionPath,
                        mention: getMentionFromGrade(mod.moyenneDuModule ?? mod.moyenneModule ?? mod.moyenne),
                        average: mod.moyenneDuModule ?? mod.moyenneModule ?? mod.moyenne ?? null,
                        letter: (moduleName || 'M').charAt(0).toUpperCase(),
                        semesterCode: moduleSemesterCode,
                        isParent: isLanguages,
                        subjects,
                        isRattrapage: subjects.some((subject) => subject.isRattrapage)
                    };
                });

                let modulesToDisplay = [];
                let modulesFromCatalog = [];

                try {
                    const modulesSemestre = await apiCours.getStudentModules(activeSemesterCode);
                    modulesFromCatalog = (Array.isArray(modulesSemestre) ? modulesSemestre : []).map((moduleItem, index) => ({
                        id: moduleItem.id || moduleItem.codeModule || `catalog-${index}`,
                        code: normalizeCode(moduleItem.codeModule || moduleItem.code || moduleItem.id),
                        name: moduleItem.nom || moduleItem.nomModule || moduleItem.libelle || `Module ${index + 1}`,
                        semesterCode: normalizeSemesterCode(
                            moduleItem.semestre
                            || moduleItem.codeSemestre
                            || moduleItem.semestreModule
                            || moduleItem.semester
                        )
                    }));
                } catch (catalogError) {
                    console.warn('Erreur lors du chargement des modules du semestre:', catalogError);
                }

                if (modulesFromCatalog.length > 0) {
                    const semesterAwareCatalogModules = modulesFromCatalog.filter(
                        (moduleItem) => !moduleItem.semesterCode || moduleItem.semesterCode === activeSemesterCode
                    );
                    const modulesCatalogToUse = semesterAwareCatalogModules.length > 0
                        ? semesterAwareCatalogModules
                        : modulesFromCatalog;

                    modulesToDisplay = await Promise.all(
                        modulesCatalogToUse.map(async (catalogItem, index) => {
                            const moduleMatch = processedModules.find((moduleItem) => {
                                const moduleCode = normalizeCode(moduleItem.codeModule);
                                const moduleName = normalizeText(moduleItem.name);

                                return (
                                    (moduleCode && catalogItem.code && moduleCode === catalogItem.code)
                                    || (moduleName && normalizeText(catalogItem.name) === moduleName)
                                );
                            });

                            if (moduleMatch) {
                                return {
                                    ...moduleMatch,
                                    id: moduleMatch.id || catalogItem.id || `module-${index}`,
                                    codeModule: moduleMatch.codeModule || catalogItem.code,
                                    name: moduleMatch.name || catalogItem.name,
                                    semesterCode: activeSemesterCode,
                                };
                            }

                            let matieres = [];
                            try {
                                const matieresPayload = await apiCours.getMatieres(catalogItem.code || catalogItem.id);
                                matieres = Array.isArray(matieresPayload) ? matieresPayload : [];
                            } catch {
                                matieres = [];
                            }

                            const subjects = matieres.map((matiere, matIdx) => ({
                                id: matiere.id || `${catalogItem.id}-mat-${matIdx}`,
                                code: matiere.codeMatiere || matiere.code || '',
                                name: matiere.nom || matiere.nomMatiere || `Matiere ${matIdx + 1}`,
                                ds: null,
                                exam: null,
                                average: null,
                                hasDsCopy: false,
                                hasExamCopy: false,
                                dsCopyPath: null,
                                examCopyPath: null,
                                hasCorrection: false,
                                correctionPath: null,
                                isRattrapage: false,
                                exams: []
                            }));

                            return {
                                id: catalogItem.id || `module-${index}`,
                                codeModule: catalogItem.code,
                                name: catalogItem.name,
                                ects: 0,
                                coeff: 0,
                                ds: null,
                                hasDsCopy: false,
                                dsCopyPath: null,
                                exam: null,
                                hasExamCopy: false,
                                examCopyPath: null,
                                hasCorrection: false,
                                correctionPath: null,
                                mention: '-',
                                average: null,
                                letter: (catalogItem.name || 'M').charAt(0).toUpperCase(),
                                semesterCode: activeSemesterCode,
                                isParent: isLanguagesModule(catalogItem.name),
                                subjects,
                                isRattrapage: false,
                            };
                        })
                    );
                } else {
                    const semesterAwareModules = processedModules.filter(
                        (moduleItem) =>
                            !moduleItem.semesterCode || moduleItem.semesterCode === activeSemesterCode
                    );

                    const hasSemesterMetadata = processedModules.some(
                        (moduleItem) => !!moduleItem.semesterCode
                    );
                    modulesToDisplay = hasSemesterMetadata ? semesterAwareModules : processedModules;
                }

                if (!hasAnyNumericGrade(modulesToDisplay) && modulesToDisplay.length > 0) {
                    try {
                        const dashboardData = await apiEtudiant.getDashboardData();
                        modulesToDisplay = mergeDashboardNotesIntoModules(
                            modulesToDisplay,
                            dashboardData?.dernieresNotes
                        );
                    } catch (dashboardFallbackError) {
                        console.warn('Impossible de recuperer les dernieres notes du dashboard:', dashboardFallbackError);
                    }
                }

                const calculatedSessionAverage = modulesToDisplay.length > 0
                    ? modulesToDisplay.reduce((sum, mod) => sum + (Number(mod.average) || 0), 0) / modulesToDisplay.length
                    : 0;

                const hasSessionAverage = Number.isFinite(Number(gradeData.moyenneSession));
                const effectiveAverage = hasSessionAverage
                    ? Number(gradeData.moyenneSession)
                    : calculatedSessionAverage;

                const hasGradeInDisplayedModules = hasAnyNumericGrade(modulesToDisplay);
                const displayedAverage = hasGradeInDisplayedModules
                    ? effectiveAverage
                    : calculatedSessionAverage;
                const effectiveStatus = hasGradeInDisplayedModules
                    ? (displayedAverage >= 10 ? 'VALIDE' : 'AJOURNE')
                    : 'EN COURS';

                const validatedModules = modulesToDisplay.filter((mod) => Number(mod.average) >= 10).length;
                const totalModules = modulesToDisplay.length;
                const ectsValue = totalModules > 0
                    ? modulesToDisplay.reduce(
                        (acc, mod) => acc + (Number(mod.average) >= 10 ? 30 / totalModules : 0),
                        0
                    )
                    : 0;

                setCurrentData({
                    label: `Semestre ${activeSemesterNumber}`,
                    average: displayedAverage,
                    hasSessionAverage,
                    ects: `${ectsValue.toFixed(0)}/30`,
                    status: effectiveStatus,
                    modules: modulesToDisplay,
                    totalModules,
                    validatedModules
                });
                setSelectedMatiereKey(null);
                setCollapsedExamLists({});
            } catch (err) {
                console.error(err);
                setLoadError(err);
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

const openExamDocument = async (examenId, type) => {
    if (!examenId) return;

    try {
        setDocumentError('');
        const fetchMethod =
            type === 'corrige' ? apiEtudiant.telechargerCorrigeExamen : apiEtudiant.telechargerCopieExamen;
        const { blob } = await fetchMethod(examenId);
        const contentType = blob.type || '';
        if (!contentType.toLowerCase().includes('pdf')) {
            throw new Error('Le fichier PDF est introuvable ou invalide.');
        }

        openBlobInNewTab(blob);
    } catch (err) {
        console.error('Erreur ouverture PDF:', err);
        setDocumentError(err.message || 'Impossible d ouvrir le document.');
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
                                            <button type="button" className="btn-grade-copy" onClick={() => openExamDocument(exam.id, 'copie')}>
                                                <Eye size={12} />
                                                Copie etudiant
                                            </button>
                                        )}
                                        {exam.corrigePath && (
                                            <button type="button" className="btn-grade-copy secondary" onClick={() => openExamDocument(exam.id, 'corrige')}>
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

    const activeSemesterCode = normalizeSemesterCode(activeSemester) || 'S1';
    const activeSemesterNumber = extractSemesterNumber(activeSemesterCode);
    const currentYear = new Date();
    const academicStartYear = currentYear.getMonth() >= 7 ? currentYear.getFullYear() : currentYear.getFullYear() - 1;
    const academicYearLabel = `${academicStartYear} / ${academicStartYear + 1}`;
    const issueDateLabel = currentYear.toLocaleDateString('fr-FR');
    const transcriptResult = currentData.hasSessionAverage
        ? (currentData.average >= 10 ? 'VALIDE' : 'AJOURNE')
        : 'EN COURS';
    const transcriptMention = currentData.hasSessionAverage
        ? getMentionFromGrade(currentData.average)
        : '-';
    const studentLevelLabel = activeSemesterNumber > 4
        ? '3eme Annee'
        : activeSemesterNumber > 2
            ? '2eme Annee'
            : '1ere Annee';

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
                        <div className="session-average-display">
                            <span className="session-average-label">MOYENNE SESSION</span>
                            <div className="session-average-value">
                                {formatSessionAverage(currentData.average, currentData.hasSessionAverage)}
                            </div>
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
                                onChange={(e) => setActiveSemester(normalizeSemesterCode(e.target.value) || 'S1')}
                            >
                                {normalizeSemesterOptions(studentStats.availableSemesters).map((semesterCode) => (
                                    <option key={semesterCode} value={semesterCode}>
                                        Semestre {extractSemesterNumber(semesterCode)}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="modules-count-info">
                            <span className="modules-count-text">
                                Modules valides: {currentData.validatedModules}/{currentData.totalModules}
                            </span>
                        </div>
                    </div>

                    {documentError ? (
                        <div style={{
                            margin: '0 24px 16px',
                            padding: '12px 16px',
                            borderRadius: '14px',
                            background: '#fef2f2',
                            color: '#dc2626',
                            fontSize: '0.9rem',
                            fontWeight: 600
                        }}>
                            {documentError}
                        </div>
                    ) : null}

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
                                ) : loadError ? (
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
                        <div className="transcript-brand-block">
                            <span className="transcript-brand-eyebrow">Portail Academique</span>
                            <h1>MyCampus</h1>
                            <p>Releve officiel des notes et resultats</p>
                        </div>
                        <div className="transcript-doc-meta">
                            <div className="transcript-doc-row">
                                <span>Reference</span>
                                <strong>RN-{activeSemesterCode}-{academicStartYear}-X</strong>
                            </div>
                            <div className="transcript-doc-row">
                                <span>Date d edition</span>
                                <strong>{issueDateLabel}</strong>
                            </div>
                        </div>
                    </header>

                    <div className="transcript-title-section">
                        <h2>RELEVE DE NOTES ET RESULTATS</h2>
                        <p>
                            Semestre {activeSemesterNumber} - Session {activeSemesterNumber % 2 === 0 ? 'Printemps' : 'Automne'} -
                            {' '}Annee universitaire {academicYearLabel}
                        </p>
                    </div>

                    <div className="transcript-student-grid">
                        <div className="info-card">
                            <span className="info-label">Nom et prenom</span>
                            <span className="info-value">{studentStats.studentName}</span>
                        </div>
                        <div className="info-card">
                            <span className="info-label">Filiere / niveau</span>
                            <span className="info-value">{studentStats.specialization} - {studentLevelLabel}</span>
                        </div>
                        <div className="info-card">
                            <span className="info-label">Numero etudiant</span>
                            <span className="info-value">{studentStats.studentId}</span>
                        </div>
                        <div className="info-card">
                            <span className="info-label">Annee universitaire</span>
                            <span className="info-value">{academicYearLabel}</span>
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
                                <div className="stat-val">
                                    {currentData.hasSessionAverage
                                        ? <>{formatNote(currentData.average)}<span>/20</span></>
                                        : 'Moyenne non disponible'}
                                </div>
                            </div>
                            <div className="stat-row">
                                <span className="stat-label">Resultat Global</span>
                                <div className="stat-val">{transcriptResult}</div>
                            </div>
                            <div className="stat-row mention-row">
                                <span className="stat-label">Mention</span>
                                <div className="stat-val">{transcriptMention}</div>
                            </div>
                        </div>

                        <div className="admin-validation">
                            <span className="admin-auth-text">Pour servir et valoir ce que de droit</span>
                            <div className="admin-signature-block">
                                <strong>Le Doyen de la Faculte</strong>
                                <span>Signature et cachet</span>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default NotesEtudiant;
