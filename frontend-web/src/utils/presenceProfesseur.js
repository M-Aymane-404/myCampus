const JUSTIFICATION_STATUS_META = {
  AUCUN: {
    backendStatus: 'AUCUN',
    key: 'empty',
    label: 'Aucun justificatif',
    shortLabel: 'Aucun',
  },
  EN_ATTENTE: {
    backendStatus: 'EN_ATTENTE',
    key: 'pending',
    label: 'En attente',
    shortLabel: 'En attente',
  },
  VALIDE: {
    backendStatus: 'VALIDE',
    key: 'validated',
    label: 'Valide',
    shortLabel: 'Valide',
  },
  REFUSE: {
    backendStatus: 'REFUSE',
    key: 'rejected',
    label: 'Refuse',
    shortLabel: 'Refuse',
  },
};

export const pickFirstValue = (...values) =>
  values.find((value) => value !== undefined && value !== null && value !== '');

export const parseDateValue = (value) => {
  if (!value) return null;

  if (value instanceof Date) {
    return Number.isNaN(value.getTime()) ? null : value;
  }

  if (Array.isArray(value) && value.length >= 3) {
    const [year, month, day, hour = 0, minute = 0, second = 0] = value;
    const parsed = new Date(year, Number(month) - 1, day, hour, minute, second);
    return Number.isNaN(parsed.getTime()) ? null : parsed;
  }

  const parsed = new Date(value);
  return Number.isNaN(parsed.getTime()) ? null : parsed;
};

export const combineDateAndTime = (dateValue, timeValue) => {
  const datePart =
    typeof dateValue === 'string' && dateValue.length >= 10
      ? dateValue.slice(0, 10)
      : parseDateValue(dateValue)?.toISOString().slice(0, 10);

  if (!datePart || !timeValue) return null;

  const rawTime =
    typeof timeValue === 'string'
      ? timeValue.includes('T')
        ? timeValue.slice(11, 19)
        : timeValue
      : parseDateValue(timeValue)?.toISOString().slice(11, 19);

  if (!rawTime) return null;

  const normalizedTime =
    rawTime.length === 5 ? `${rawTime}:00` : rawTime.length >= 8 ? rawTime.slice(0, 8) : null;

  if (!normalizedTime) return null;
  return parseDateValue(`${datePart}T${normalizedTime}`);
};

export const getSessionStartDate = (session) =>
  combineDateAndTime(session?.date, session?.heureDebut) ||
  parseDateValue(session?.start) ||
  parseDateValue(session?.heureDebut) ||
  parseDateValue(session?.date);

export const getSessionEndDate = (session) =>
  combineDateAndTime(session?.date, session?.heureFin) ||
  parseDateValue(session?.end) ||
  parseDateValue(session?.heureFin);

export const formatDateLabel = (value, options = { weekday: 'short', day: '2-digit', month: 'short', year: 'numeric' }) => {
  const date = parseDateValue(value);
  if (!date) return '--';
  return date.toLocaleDateString('fr-FR', options);
};

export const formatHourLabel = (value) => {
  const date = parseDateValue(value);
  if (!date) return '';
  return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

export const normalizePresenceStatus = (status) => {
  const normalized = String(status || '').trim().toUpperCase();

  if (['PRESENT', 'PRESENTE', 'PR', 'P'].includes(normalized)) return 'PRESENT';
  if (['ABSENT', 'ABSENTE', 'ABS', 'A'].includes(normalized)) return 'ABSENT';
  if (['JUSTIFIE', 'JUSTIFIEE', 'JUSTIFIED'].includes(normalized)) return 'JUSTIFIE';
  if (['RETARD', 'LATE'].includes(normalized)) return 'RETARD';

  return normalized;
};

export const hasJustification = (ligne) =>
  Boolean(
    ligne?.justificationSoumise ||
      ligne?.motif ||
      ligne?.motifUrl ||
      ligne?.motifNom ||
      ligne?.typeMotif ||
      ligne?.descriptionDeJustification
  );

export const getJustificationStatusKey = (ligne) => {
  const normalized = String(ligne?.statutJustificatif || '').trim().toUpperCase();

  if (normalized === 'VALIDE' || normalized === 'VALIDEE') return 'VALIDE';
  if (normalized === 'REFUSE' || normalized === 'REFUSEE') return 'REFUSE';
  if (normalized === 'EN_ATTENTE') return 'EN_ATTENTE';
  if (ligne?.justifiee || normalizePresenceStatus(ligne?.status) === 'JUSTIFIE') return 'VALIDE';
  if (hasJustification(ligne)) return 'EN_ATTENTE';

  return 'AUCUN';
};

export const getJustificationStatusMeta = (ligneOrStatus) => {
  const status =
    typeof ligneOrStatus === 'string'
      ? String(ligneOrStatus || '').trim().toUpperCase()
      : getJustificationStatusKey(ligneOrStatus);

  return JUSTIFICATION_STATUS_META[status] || JUSTIFICATION_STATUS_META.AUCUN;
};

export const getJustificationPreviewText = (ligne) =>
  pickFirstValue(
    ligne?.descriptionDeJustification,
    ligne?.motif,
    ligne?.typeMotif,
    ligne?.motifNom,
    'Aucun document soumis',
  );

export const buildJustificationRecord = ({ seance, enseignement, ligne }) => {
  if (!ligne) return null;

  const status = getJustificationStatusKey(ligne);
  const statusMeta = getJustificationStatusMeta(status);
  const absenceDate =
    getSessionStartDate(seance) ||
    parseDateValue(seance?.date) ||
    parseDateValue(ligne?.dateDeclaration);

  const classeLabel = pickFirstValue(
    enseignement?.nomClasse,
    enseignement?.classeNom,
    seance?.nomClasse,
    seance?.classeNom,
    enseignement?.codeClasse,
    seance?.codeClasse,
    'Classe',
  );

  const matiereLabel = pickFirstValue(
    enseignement?.nomMatiere,
    enseignement?.matiereNom,
    seance?.nomMatiere,
    seance?.matiereNom,
    enseignement?.codeMatiere,
    seance?.codeMatiere,
    'Matiere',
  );

  const etudiantNomComplet = [ligne?.prenomEtudiant, ligne?.nomEtudiant].filter(Boolean).join(' ').trim() || 'Etudiant';

  return {
    id: `${seance?.id || 'seance'}-${ligne?.idPresence || ligne?.idEtudiant || ligne?.apogee || 'ligne'}`,
    status,
    statusMeta,
    canProcess: status === 'EN_ATTENTE' && Boolean(seance?.id && ligne?.idPresence),
    etudiantNomComplet,
    prenomEtudiant: ligne?.prenomEtudiant || '',
    nomEtudiant: ligne?.nomEtudiant || '',
    apogee: ligne?.apogee || '--',
    classeLabel,
    codeClasse: pickFirstValue(enseignement?.codeClasse, seance?.codeClasse, ''),
    matiereLabel,
    codeMatiere: pickFirstValue(enseignement?.codeMatiere, seance?.codeMatiere, ''),
    seanceType: pickFirstValue(seance?.type, seance?.typeSeance, 'Seance'),
    salle: pickFirstValue(seance?.salle, seance?.nomSalle, ''),
    heureDebut: pickFirstValue(seance?.heureDebut, seance?.start, absenceDate),
    heureFin: pickFirstValue(seance?.heureFin, seance?.end, ''),
    absenceDate,
    motif: ligne?.descriptionDeJustification || ligne?.motif || '',
    motifType: ligne?.typeMotif || '',
    previewText: getJustificationPreviewText(ligne),
    documentName: ligne?.motifNom || '',
    documentUrl: ligne?.motifUrl || '',
    hasSubmittedJustification: status !== 'AUCUN',
    justificationValidee: Boolean(ligne?.justifiee),
    presenceStatus: normalizePresenceStatus(ligne?.status),
    seanceId: seance?.id || null,
    presenceId: ligne?.idPresence || null,
    seance,
    enseignement,
    ligne,
  };
};

export const buildJustificationRecordFromApi = (item) => {
  if (!item) return null;

  const ligne = {
    idPresence: item.presenceId,
    idEtudiant: item.idEtudiant,
    apogee: item.apogee,
    nomEtudiant: item.nomEtudiant,
    prenomEtudiant: item.prenomEtudiant,
    status: item.status,
    statutJustificatif: item.statutJustificatif,
    justifiee: item.justifiee,
    justificationSoumise: item.justificationSoumise,
    motif: item.motif,
    typeMotif: item.typeMotif,
    motifUrl: item.motifUrl,
    motifNom: item.motifNom,
    dateDeclaration: item.dateDeclaration,
  };

  const seance = {
    id: item.seanceId,
    type: item.typeSeance,
    date: item.dateAbsence,
    heureDebut: item.heureDebut,
    heureFin: item.heureFin,
    salle: item.salle,
    codeClasse: item.codeClasse,
    nomClasse: item.nomClasse,
    codeMatiere: item.codeMatiere,
    nomMatiere: item.nomMatiere,
  };

  const enseignement = {
    codeClasse: item.codeClasse,
    nomClasse: item.nomClasse,
    codeMatiere: item.codeMatiere,
    nomMatiere: item.nomMatiere,
  };

  return buildJustificationRecord({ seance, enseignement, ligne });
};

export const mergeJustificationRecord = (record, updatedLine) =>
  buildJustificationRecord({
    seance: record?.seance,
    enseignement: record?.enseignement,
    ligne: {
      ...(record?.ligne || {}),
      ...(updatedLine || {}),
    },
  });
