
/**
 * Maps backend class codes to full specialization names.
 */
export const getSpectializationFromClass = (codeClasse) => {
    if (!codeClasse) return "Génie Logiciel"; // Default fallback

    const code = codeClasse.toUpperCase();
    if (code.startsWith('GL')) return "Génie Logiciel";
    if (code.startsWith('RT')) return "Réseaux & Télécoms";
    if (code.startsWith('BI')) return "Informatique Décisionnelle";
    if (code.startsWith('II')) return "Ingénierie Informatique";

    return "Génie Informatique"; // Default generic
};

/**
 * Heuristic to identify exam components from a list of exams.
 * Returns { ds: number|null, exam: number|null, hasDsCopy: boolean, hasExamCopy: boolean, ... }
 */
export const parseExams = (exams) => {
    if (!exams || exams.length === 0) return { ds: null, exam: null };

    const takenExams = exams.filter(e => e?.note !== null && e?.note !== undefined);
    const normals = takenExams.filter(e => e.typeExamen === 'NORMAL');
    const rattrapages = takenExams.filter(e => e.typeExamen === 'RATTRAPAGE');

    let dsRes = null;
    let examRes = null;
    let retakeRes = null;

    normals.sort((a, b) => (a.numeroExamen || 0) - (b.numeroExamen || 0));
    rattrapages.sort((a, b) => (a.numeroExamen || 0) - (b.numeroExamen || 0));

    if (normals.length >= 2) {
        dsRes = normals[0];
        examRes = normals[1];
    } else if (normals.length === 1) {
        examRes = normals[0];
    }

    if (rattrapages.length > 0) {
        retakeRes = rattrapages[0];
        if (!examRes) {
            examRes = retakeRes;
        }
    }

    return {
        ds: dsRes ? dsRes.note : null,
        exam: examRes ? examRes.note : null,
        hasDsCopy: dsRes ? !!dsRes.copieEtudiantPath : false,
        hasExamCopy: examRes ? !!examRes.copieEtudiantPath : false,
        dsCopyPath: dsRes ? dsRes.copieEtudiantPath : null,
        examCopyPath: examRes ? examRes.copieEtudiantPath : null,
        // Correction
        dsCorrection: dsRes ? dsRes.corrigePath : null,
        examCorrection: examRes ? examRes.corrigePath : null,
        retake: retakeRes ? retakeRes.note : null,
        retakeCopyPath: retakeRes ? retakeRes.copieEtudiantPath : null,
        retakeCorrection: retakeRes ? retakeRes.corrigePath : null,
        isRattrapage: rattrapages.length > 0,
        exams: takenExams
    };
};
