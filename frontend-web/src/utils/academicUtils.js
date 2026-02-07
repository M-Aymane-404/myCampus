
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

    // Filter by type
    const normals = exams.filter(e => e.typeExamen === 'NORMAL');
    const rattrapages = exams.filter(e => e.typeExamen === 'RATTRAPAGE');

    let dsRes = null;
    let examRes = null;

    if (normals.length === 2) {
        // Assume sorted by number or date? Backend doesn't guarantee order but we have numeroExamen
        // Sort by numeroExamen
        normals.sort((a, b) => (a.numeroExamen || 0) - (b.numeroExamen || 0));
        dsRes = normals[0];
        examRes = normals[1];
    } else if (normals.length === 1) {
        // Only one normal exam. Is it Exam or DS?
        // Usually if only one, it's the Final Exam.
        examRes = normals[0];
    }

    // If there is a rattrapage, it usually replaces the Exam note if better (or strictly replaces).
    // For display, we might want to show the rattrapage note as the exam note or separate?
    // The design has "DS" and "Exam".
    // If rattrapage exists, we'll use it as the 'Exam' value but maybe indicate it?
    // For now, let's just use it if present and better? Or just use it.
    // Backend `MatiereDeReleveEtudiantDTO` calculation logic seemed to handle average.
    // Front end just wants to show the values.

    if (rattrapages.length > 0) {
        // Replacing exam with Rattrapage for display if rattrapage exists? 
        // Or show rattrapage?
        // Let's use rattrapage as the Exam value if it exists.
        examRes = rattrapages[0];
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
        isRattrapage: rattrapages.length > 0
    };
};
