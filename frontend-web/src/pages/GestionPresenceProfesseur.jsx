import { useEffect, useMemo, useRef, useState } from 'react';
import {
  Book,
  Calendar,
  CheckCircle2,
  ChevronDown,
  Clock3,
  Eye,
  FileText,
  Layers3,
  Loader2,
  MapPin,
  RefreshCw,
  Search,
  UserCheck,
  X,
} from 'lucide-react';
import { apiEnseignementsProfesseur } from '../services/professeur/apiEnseignementsProfesseur';
import apiSeancesProfesseur from '../services/professeur/apiSeancesProfesseur';
import { apiPresencesProfesseur } from '../services/professeur/apiPresencesProfesseur';
import {
  buildJustificationRecord,
  formatDateLabel,
  formatHourLabel,
  getJustificationPreviewText,
  getJustificationStatusKey,
  getJustificationStatusMeta,
  mergeJustificationRecord,
  normalizePresenceStatus,
  parseDateValue,
  pickFirstValue,
} from '../utils/presenceProfesseur';
import './GestionPresenceProfesseur.css';

const TYPE_COLOR = { COURS: 'blue', TD: 'green', TP: 'purple' };
const VIEW_OPTIONS = [
  { id: 'feuille', label: "Feuille d'appel", icon: UserCheck },
  { id: 'justificatifs', label: 'Justificatifs', icon: FileText },
];
const STATUS_FILTER_OPTIONS = [
  { value: 'EN_ATTENTE', label: 'En attente' },
  { value: 'VALIDE', label: 'Valides' },
  { value: 'REFUSE', label: 'Refuses' },
  { value: 'TOUS', label: 'Tous' },
];
const PERIOD_FILTER_OPTIONS = [
  { value: 'ALL', label: 'Toutes periodes' },
  { value: '7D', label: '7 derniers jours' },
  { value: '30D', label: '30 derniers jours' },
  { value: 'THIS_MONTH', label: 'Ce mois-ci' },
  { value: 'THIS_YEAR', label: 'Cette annee' },
];
const DEFAULT_JUSTIFICATION_FILTERS = {
  status: 'TOUS',
  classe: 'TOUTES',
  period: 'ALL',
  search: '',
};

const getStatusMeta = (status) => {
  const normalized = normalizePresenceStatus(status);
  if (normalized === 'PRESENT') return { label: 'Present', className: 'present' };
  if (normalized === 'ABSENT') return { label: 'Absent', className: 'absent' };
  if (normalized === 'JUSTIFIE') return { label: 'Justifie', className: 'justifie' };
  if (normalized === 'RETARD') return { label: 'Retard', className: 'retard' };
  return { label: 'Non defini', className: 'default' };
};

const pickNumber = (...values) => {
  for (const value of values) {
    const number = Number(value);
    if (Number.isFinite(number)) return number;
  }
  return null;
};

const buildPresenceSummary = (feuille, lignes) => {
  const stats = feuille?.stats || {};
  const normalizedLines = Array.isArray(lignes) ? lignes.map((ligne) => normalizePresenceStatus(ligne.status)) : [];
  const presentsFromLines = normalizedLines.filter((status) => status === 'PRESENT').length;
  const absentsFromLines = normalizedLines.filter((status) => status === 'ABSENT').length;
  const totalFromLines = normalizedLines.length;
  return {
    presents: pickNumber(stats.nbPresents, stats.nombrePresents, stats.presentCount, presentsFromLines) ?? 0,
    absents: pickNumber(stats.nbAbsents, stats.nombreAbsents, stats.absentCount, absentsFromLines) ?? 0,
    total: pickNumber(stats.totalEtudiants, stats.nbEtudiants, stats.total, totalFromLines) ?? 0,
  };
};

const buildSeanceLabel = (record) =>
  [
    record?.seanceType,
    formatDateLabel(record?.absenceDate || record?.seance?.date, { day: '2-digit', month: 'short', year: 'numeric' }),
    record?.heureDebut
      ? `${formatHourLabel(record.heureDebut)}${record?.heureFin ? ` - ${formatHourLabel(record.heureFin)}` : ''}`
      : '',
    record?.salle ? `Salle ${record.salle}` : '',
  ]
    .filter(Boolean)
    .join(' | ');

const getJustificationStatLine = (ligne) => ({
  ...getJustificationStatusMeta(ligne),
  preview: getJustificationPreviewText(ligne),
});

const normalizeSearch = (value) =>
  String(value || '')
    .normalize('NFD')
    .replace(/[\u0300-\u036f]/g, '')
    .toLowerCase()
    .trim();

const matchesPeriod = (record, period) => {
  if (!period || period === 'ALL') return true;
  const date = parseDateValue(record?.absenceDate);
  if (!date) return false;
  const now = new Date();
  const delta = now.getTime() - date.getTime();
  if (period === '7D') return delta <= 7 * 24 * 60 * 60 * 1000;
  if (period === '30D') return delta <= 30 * 24 * 60 * 60 * 1000;
  if (period === 'THIS_MONTH') return date.getMonth() === now.getMonth() && date.getFullYear() === now.getFullYear();
  if (period === 'THIS_YEAR') return date.getFullYear() === now.getFullYear();
  return true;
};

const buildClasseValue = (record) => record.codeClasse || record.classeLabel;

const GestionPresenceProfesseur = ({ navigationIntent }) => {
  const [enseignements, setEnseignements] = useState([]);
  const [selectedEnseignement, setSelectedEnseignement] = useState(null);
  const [loadingEns, setLoadingEns] = useState(true);
  const [seances, setSeances] = useState([]);
  const [selectedSeance, setSelectedSeance] = useState(null);
  const [loadingSeances, setLoadingSeances] = useState(false);
  const [feuille, setFeuille] = useState(null);
  const [lignes, setLignes] = useState([]);
  const [loadingFeuille, setLoadingFeuille] = useState(false);
  const [activeView, setActiveView] = useState('feuille');
  const [justificationFilters, setJustificationFilters] = useState(DEFAULT_JUSTIFICATION_FILTERS);
  const [justificationRecords, setJustificationRecords] = useState([]);
  const [loadingJustifications, setLoadingJustifications] = useState(false);
  const [justificationsLoaded, setJustificationsLoaded] = useState(false);
  const [justificationError, setJustificationError] = useState('');
  const [selectedJustification, setSelectedJustification] = useState(null);
  const [documentState, setDocumentState] = useState({ url: '', filename: '', error: '', loading: false });
  const [saving, setSaving] = useState({});
  const [processingJustification, setProcessingJustification] = useState({});
  const [error, setError] = useState(null);
  const documentUrlRef = useRef('');

  const chargerFeuille = async (seanceId, showLoader = true) => {
    if (!seanceId) return;
    if (showLoader) {
      setLoadingFeuille(true);
      setFeuille(null);
      setLignes([]);
    }
    setError(null);
    try {
      const data = await apiPresencesProfesseur.chargerFeuille(seanceId);
      setFeuille(data || null);
      setLignes(Array.isArray(data?.lignes) ? data.lignes : []);
    } catch (err) {
      setError(`Erreur lors du chargement : ${err.message}`);
    } finally {
      if (showLoader) setLoadingFeuille(false);
    }
  };

  const chargerJustificatifs = async (showLoader = true) => {
    if (showLoader) setLoadingJustifications(true);
    setJustificationError('');
    try {
      const data = await apiPresencesProfesseur.listerJustificatifsProfesseur();
      setJustificationRecords(Array.isArray(data) ? data : []);
      setJustificationsLoaded(true);
    } catch (err) {
      setJustificationError(err.message || 'Impossible de charger les justificatifs.');
    } finally {
      if (showLoader) setLoadingJustifications(false);
    }
  };

  const openJustificationDrawer = (record) => {
    if (record) setSelectedJustification(record);
  };

  const closeJustificationDrawer = () => {
    setSelectedJustification(null);
  };

  const syncJustificationStates = (record, updatedLine) => {
    if (!record) return;
    const mergedRecord = mergeJustificationRecord(record, updatedLine);
    if (!mergedRecord) return;
    setJustificationRecords((prev) => prev.map((item) => (item.id === record.id ? mergedRecord : item)));
    if (selectedSeance?.id && record.seanceId === selectedSeance.id) {
      setLignes((prev) => prev.map((ligne) => (ligne.idPresence === record.presenceId ? { ...ligne, ...updatedLine } : ligne)));
    }
    setSelectedJustification((prev) => (prev?.id === record.id ? mergedRecord : prev));
  };

  const handleStatusChange = async (apogee, newStatus) => {
    if (!selectedSeance?.id) return;
    const previousLine = lignes.find((ligne) => ligne.apogee === apogee);
    const previousStatus = previousLine?.status;
    setLignes((prev) => prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, status: newStatus } : ligne)));
    setSaving((prev) => ({ ...prev, [apogee]: true }));
    setError(null);
    try {
      const updated = await apiPresencesProfesseur.saisirPresence(selectedSeance.id, { apogee, status: newStatus });
      setLignes((prev) => prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, ...updated } : ligne)));
    } catch (err) {
      setLignes((prev) => prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, status: previousStatus } : ligne)));
      setError(`Erreur enregistrement : ${err.message}`);
    } finally {
      setSaving((prev) => ({ ...prev, [apogee]: false }));
    }
  };

  const handleJustificationDecision = async (record, action) => {
    if (!record?.seanceId || !record?.presenceId) return;
    setProcessingJustification((prev) => ({ ...prev, [record.presenceId]: action }));
    setError(null);
    setJustificationError('');
    try {
      const updated = action === 'approve'
        ? await apiPresencesProfesseur.validerJustificatif(record.seanceId, record.presenceId)
        : await apiPresencesProfesseur.refuserJustificatif(record.seanceId, record.presenceId);
      syncJustificationStates(record, updated);
    } catch (err) {
      setError(err.message || 'Erreur lors du traitement du justificatif.');
    } finally {
      setProcessingJustification((prev) => ({ ...prev, [record.presenceId]: null }));
    }
  };

  useEffect(() => {
    const load = async () => {
      try {
        const data = await apiEnseignementsProfesseur.listerEnseignements();
        const items = Array.isArray(data) ? data : [];
        setEnseignements(items);
        setSelectedEnseignement(items[0] || null);
      } catch (err) {
        setError(`Impossible de charger les cours : ${err.message}`);
      } finally {
        setLoadingEns(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    if (!selectedEnseignement) return;
    let cancelled = false;
    const load = async () => {
      setLoadingSeances(true);
      setSeances([]);
      setSelectedSeance(null);
      setFeuille(null);
      setLignes([]);
      try {
        const data = await apiSeancesProfesseur.rechercherSeances({
          codeClasse: selectedEnseignement.codeClasse,
          codeMatiere: selectedEnseignement.codeMatiere,
        });
        const sorted = (Array.isArray(data) ? data : []).sort(
          (a, b) => new Date(b.heureDebut || b.date || 0) - new Date(a.heureDebut || a.date || 0)
        );
        if (!cancelled) {
          setSeances(sorted);
          setSelectedSeance(sorted[0] || null);
        }
      } catch {
        if (!cancelled) {
          setSeances([]);
          setSelectedSeance(null);
        }
      } finally {
        if (!cancelled) setLoadingSeances(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [selectedEnseignement]);

  useEffect(() => {
    if (!selectedSeance) return;
    let cancelled = false;
    const load = async () => {
      try {
        await chargerFeuille(selectedSeance.id, true);
      } catch (err) {
        if (!cancelled) setError(`Erreur lors du chargement : ${err.message}`);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [selectedSeance]);

  useEffect(() => {
    if (!selectedSeance?.id) return undefined;
    const interval = window.setInterval(() => {
      if (Object.values(saving).some(Boolean) || Object.values(processingJustification).some(Boolean)) return;
      chargerFeuille(selectedSeance.id, false).catch(() => {});
    }, 10000);
    return () => window.clearInterval(interval);
  }, [selectedSeance?.id, saving, processingJustification]);

  useEffect(() => {
    if (!navigationIntent?.requestId) return;
    setActiveView(navigationIntent.activeView === 'justificatifs' ? 'justificatifs' : 'feuille');
    setJustificationFilters((prev) => ({
      ...prev,
      ...(navigationIntent.statusFilter ? { status: navigationIntent.statusFilter } : {}),
      ...(navigationIntent.classeFilter ? { classe: navigationIntent.classeFilter } : {}),
      ...(navigationIntent.periodFilter ? { period: navigationIntent.periodFilter } : {}),
      ...(navigationIntent.search ? { search: navigationIntent.search } : {}),
    }));
  }, [navigationIntent]);

  useEffect(() => {
    if (activeView !== 'justificatifs' || justificationsLoaded) return;
    chargerJustificatifs(true);
  }, [activeView, justificationsLoaded]);

  useEffect(() => {
    if (documentUrlRef.current) {
      URL.revokeObjectURL(documentUrlRef.current);
      documentUrlRef.current = '';
    }
    if (!selectedJustification?.seanceId || !selectedJustification?.presenceId || !selectedJustification?.hasSubmittedJustification) {
      setDocumentState({ url: '', filename: '', error: '', loading: false });
      return undefined;
    }
    let active = true;
    setDocumentState({ url: '', filename: '', error: '', loading: true });
    apiPresencesProfesseur.telechargerJustificatif(selectedJustification.seanceId, selectedJustification.presenceId)
      .then(({ blob, filename }) => {
        if (!active) return;
        const objectUrl = URL.createObjectURL(blob);
        documentUrlRef.current = objectUrl;
        setDocumentState({ url: objectUrl, filename: filename || selectedJustification.documentName || 'justificatif.pdf', error: '', loading: false });
      })
      .catch((err) => {
        if (!active) return;
        setDocumentState({ url: '', filename: '', error: err.message || 'Impossible de recuperer le document justificatif.', loading: false });
      });
    return () => {
      active = false;
      if (documentUrlRef.current) {
        URL.revokeObjectURL(documentUrlRef.current);
        documentUrlRef.current = '';
      }
    };
  }, [
    selectedJustification?.id,
    selectedJustification?.seanceId,
    selectedJustification?.presenceId,
    selectedJustification?.hasSubmittedJustification,
    selectedJustification?.documentName,
  ]);

  useEffect(() => () => {
    if (documentUrlRef.current) URL.revokeObjectURL(documentUrlRef.current);
  }, []);

  const summary = useMemo(() => buildPresenceSummary(feuille, lignes), [feuille, lignes]);
  const couleur = selectedSeance ? TYPE_COLOR[selectedSeance.type] || 'grey' : 'grey';
  const justificationOverview = useMemo(
    () => justificationRecords.reduce((acc, record) => {
      acc.total += 1;
      if (record.status === 'EN_ATTENTE') acc.pending += 1;
      if (record.status === 'VALIDE') acc.validated += 1;
      if (record.status === 'REFUSE') acc.rejected += 1;
      return acc;
    }, { total: 0, pending: 0, validated: 0, rejected: 0 }),
    [justificationRecords]
  );
  const classeOptions = useMemo(
    () => Array.from(justificationRecords.reduce((map, record) => {
      const value = buildClasseValue(record);
      if (value && !map.has(value)) map.set(value, record.classeLabel);
      return map;
    }, new Map())).map(([value, label]) => ({ value, label })).sort((a, b) => a.label.localeCompare(b.label, 'fr')),
    [justificationRecords]
  );
  const filteredJustifications = useMemo(() => {
    const searchNeedle = normalizeSearch(justificationFilters.search);
    return justificationRecords.filter((record) => {
      if (justificationFilters.status !== 'TOUS' && record.status !== justificationFilters.status) return false;
      if (justificationFilters.classe !== 'TOUTES' && buildClasseValue(record) !== justificationFilters.classe) return false;
      if (!matchesPeriod(record, justificationFilters.period)) return false;
      if (!searchNeedle) return true;
      const haystack = normalizeSearch([record.etudiantNomComplet, record.apogee, record.classeLabel, record.matiereLabel, record.previewText].join(' '));
      return haystack.includes(searchNeedle);
    });
  }, [justificationRecords, justificationFilters]);
  const currentViewBadge = activeView === 'justificatifs'
    ? `${filteredJustifications.length} justificatif${filteredJustifications.length > 1 ? 's' : ''}`
    : selectedSeance
      ? formatDateLabel(selectedSeance.heureDebut || selectedSeance.date, { day: '2-digit', month: 'short', year: 'numeric' })
      : 'Aucune seance';

  const renderCallSheetView = () => (
    <>
      <div className="presence-selectors-bar">
        <div className="presence-selectors-head">
          <span className="selection-title">Selection rapide</span>
          <p>Choisissez un cours puis une seance a traiter.</p>
        </div>

        <div className="presence-selectors-grid">
          <div className="selector-group">
            <label className="selector-label">
              <UserCheck size={14} /> Cours
            </label>
            {loadingEns ? (
              <div className="selector-loading">
                <Loader2 size={14} className="spin-icon" />
                Chargement...
              </div>
            ) : (
              <div className="select-wrapper-rounded">
                <select
                  value={selectedEnseignement ? `${selectedEnseignement.codeClasse}|${selectedEnseignement.codeMatiere}` : ''}
                  onChange={(e) => {
                    const [codeClasse, codeMatiere] = e.target.value.split('|');
                    const enseignement = enseignements.find(
                      (item) => item.codeClasse === codeClasse && item.codeMatiere === codeMatiere
                    );
                    setSelectedEnseignement(enseignement || null);
                  }}
                >
                  {enseignements.map((enseignement) => (
                    <option
                      key={`${enseignement.codeClasse}|${enseignement.codeMatiere}`}
                      value={`${enseignement.codeClasse}|${enseignement.codeMatiere}`}
                    >
                      {enseignement.codeClasse} - {enseignement.nomMatiere || enseignement.codeMatiere}
                    </option>
                  ))}
                </select>
                <ChevronDown size={16} />
              </div>
            )}
          </div>

          <div className="selector-group">
            <label className="selector-label">
              <Calendar size={14} /> Seance
            </label>
            {loadingSeances ? (
              <div className="selector-loading">
                <Loader2 size={14} className="spin-icon" />
                Chargement...
              </div>
            ) : seances.length === 0 ? (
              <div className="selector-empty">Aucune seance disponible</div>
            ) : (
              <div className="select-wrapper-rounded">
                <select
                  value={selectedSeance?.id || ''}
                  onChange={(e) => {
                    const seance = seances.find((item) => String(item.id) === e.target.value);
                    setSelectedSeance(seance || null);
                  }}
                >
                  {seances.map((seance) => (
                    <option key={seance.id} value={seance.id}>
                      {seance.type} | {formatDateLabel(seance.heureDebut || seance.date, {
                        weekday: 'long',
                        day: '2-digit',
                        month: 'long',
                      })}
                      {seance.heureDebut ? ` | ${formatHourLabel(seance.heureDebut)}` : ''}
                      {seance.salle ? ` | Salle ${seance.salle}` : ''}
                    </option>
                  ))}
                </select>
                <ChevronDown size={16} />
              </div>
            )}
          </div>
        </div>
      </div>

      {error ? (
        <div className="presence-alert presence-alert-error">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="presence-alert-close">
            <X size={14} />
          </button>
        </div>
      ) : null}

      {loadingFeuille ? (
        <div className="presence-feedback-box">
          <Loader2 size={22} className="spin-icon" />
          Chargement de la feuille d'appel...
        </div>
      ) : null}

      {!loadingFeuille && lignes.length > 0 ? (
        <>
          <div className="presence-history-head">
            <div className="presence-history-copy">
              <span className="selection-title">Feuille d'appel</span>
              <h2>Liste des etudiants</h2>
            </div>
          </div>

          <div className="presence-lines-card">
            <div className="presence-lines-head">
              <span>Etudiant</span>
              <span>Statut</span>
              <span>Actions</span>
              <span>Justificatif</span>
            </div>

            <div className="presence-lines-body">
              {lignes.map((ligne) => {
                const statusMeta = getStatusMeta(ligne.status);
                const justificationStat = getJustificationStatLine(ligne);
                const canOpenJustification = getJustificationStatusKey(ligne) !== 'AUCUN';
                const quickRecord = buildJustificationRecord({
                  seance: selectedSeance,
                  enseignement: selectedEnseignement,
                  ligne,
                });

                return (
                  <div
                    key={ligne.idEtudiant || ligne.apogee}
                    className={`presence-line-row status-${statusMeta.className}`}
                  >
                    <div className="presence-line-student">
                      <div className="presence-line-student-copy">
                        <strong>{ligne.prenomEtudiant} {ligne.nomEtudiant}</strong>
                        <span>Apogee : {ligne.apogee}</span>
                      </div>
                    </div>

                    <div className="presence-line-status">
                      <span className={`student-status-badge ${statusMeta.className}`}>
                        {statusMeta.label}
                      </span>
                    </div>

                    <div className="student-actions">
                      <button
                        className={`btn-state btn-present ${normalizePresenceStatus(ligne.status) === 'PRESENT' ? 'active' : ''}`}
                        onClick={() => handleStatusChange(ligne.apogee, 'PRESENT')}
                        disabled={saving[ligne.apogee]}
                      >
                        {saving[ligne.apogee] ? '...' : 'Present'}
                      </button>
                      <button
                        className={`btn-state btn-absent ${normalizePresenceStatus(ligne.status) === 'ABSENT' ? 'active' : ''}`}
                        onClick={() => handleStatusChange(ligne.apogee, 'ABSENT')}
                        disabled={saving[ligne.apogee]}
                      >
                        {saving[ligne.apogee] ? '...' : 'Absent'}
                      </button>
                    </div>

                    <div className="presence-line-justification">
                      {normalizePresenceStatus(ligne.status) === 'ABSENT' || getJustificationStatusKey(ligne) !== 'AUCUN' ? (
                        <div className="justification-panel">
                          <div className={`justification-state ${justificationStat.key}`}>
                            <strong>{justificationStat.label}</strong>
                            <span>{justificationStat.preview}</span>
                          </div>

                          <div className="justification-actions">
                            {canOpenJustification ? (
                              <button
                                type="button"
                                className="btn-justification btn-view"
                                onClick={() => openJustificationDrawer(quickRecord)}
                              >
                                {quickRecord?.canProcess ? 'Traiter' : 'Consulter'}
                              </button>
                            ) : null}

                            <button
                              type="button"
                              className="btn-justification btn-secondary"
                              onClick={() => setActiveView('justificatifs')}
                            >
                              Vue globale
                            </button>
                          </div>
                        </div>
                      ) : (
                        <div className="justification-state empty">
                          <strong>Non applicable</strong>
                          <span>Le suivi detaille reste disponible dans l'onglet Justificatifs.</span>
                        </div>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>
        </>
      ) : null}

      {!loadingFeuille && selectedSeance && lignes.length === 0 && !error ? (
        <div className="presence-empty-state">
          <UserCheck size={48} className="presence-empty-icon" />
          <p>Aucun etudiant inscrit pour cette seance.</p>
        </div>
      ) : null}

      {!loadingFeuille && !selectedSeance && !loadingSeances && seances.length === 0 && !loadingEns ? (
        <div className="presence-empty-state">
          <Calendar size={48} className="presence-empty-icon" />
          <p>Aucune seance disponible pour ce cours.</p>
        </div>
      ) : null}
    </>
  );

  const renderJustificationView = () => (
    <>
      <div className="presence-selectors-bar justification-filters-bar">
        <div className="presence-selectors-head">
          <span className="selection-title">Traitement transverse</span>
          <p>Retrouvez rapidement les justificatifs de toutes vos classes, puis validez ou refusez sans parcourir chaque feuille d'appel.</p>
        </div>

        <div className="justification-filters-content">
          <div className="justification-status-tabs" role="tablist" aria-label="Filtres de statut">
            {STATUS_FILTER_OPTIONS.map((option) => (
              <button
                key={option.value}
                type="button"
                className={`justification-status-tab ${justificationFilters.status === option.value ? 'active' : ''}`}
                onClick={() => setJustificationFilters((prev) => ({ ...prev, status: option.value }))}
              >
                {option.label}
              </button>
            ))}
          </div>

          <div className="justification-filter-grid">
            <div className="selector-group">
              <label className="selector-label">
                <Layers3 size={14} /> Classe
              </label>
              <div className="select-wrapper-rounded">
                <select
                  value={justificationFilters.classe}
                  onChange={(e) => setJustificationFilters((prev) => ({ ...prev, classe: e.target.value }))}
                >
                  <option value="TOUTES">Toutes les classes</option>
                  {classeOptions.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <ChevronDown size={16} />
              </div>
            </div>

            <div className="selector-group">
              <label className="selector-label">
                <Calendar size={14} /> Periode
              </label>
              <div className="select-wrapper-rounded">
                <select
                  value={justificationFilters.period}
                  onChange={(e) => setJustificationFilters((prev) => ({ ...prev, period: e.target.value }))}
                >
                  {PERIOD_FILTER_OPTIONS.map((option) => (
                    <option key={option.value} value={option.value}>
                      {option.label}
                    </option>
                  ))}
                </select>
                <ChevronDown size={16} />
              </div>
            </div>

            <div className="selector-group justification-search-group">
              <label className="selector-label">
                <Search size={14} /> Recherche
              </label>
              <label className="justification-search-box">
                <Search size={16} />
                <input
                  type="search"
                  value={justificationFilters.search}
                  placeholder="Nom etudiant ou apogee"
                  onChange={(e) => setJustificationFilters((prev) => ({ ...prev, search: e.target.value }))}
                />
                {justificationFilters.search ? (
                  <button
                    type="button"
                    className="justification-search-clear"
                    onClick={() => setJustificationFilters((prev) => ({ ...prev, search: '' }))}
                  >
                    <X size={14} />
                  </button>
                ) : null}
              </label>
            </div>

            <div className="selector-group justification-refresh-group">
              <label className="selector-label">Actualisation</label>
              <button
                type="button"
                className="btn-justification btn-secondary btn-refresh"
                onClick={() => chargerJustificatifs(true)}
                disabled={loadingJustifications}
              >
                {loadingJustifications ? <Loader2 size={14} className="spin-icon" /> : <RefreshCw size={14} />}
                <span>Recharger</span>
              </button>
            </div>
          </div>
        </div>
      </div>

      {error ? (
        <div className="presence-alert presence-alert-error">
          <span>{error}</span>
          <button onClick={() => setError(null)} className="presence-alert-close">
            <X size={14} />
          </button>
        </div>
      ) : null}

      {justificationError ? (
        <div className="presence-empty-state">
          <FileText size={48} className="presence-empty-icon" />
          <p>{justificationError}</p>
          <button type="button" className="btn-justification btn-secondary" onClick={() => chargerJustificatifs(true)}>
            Reessayer
          </button>
        </div>
      ) : null}

      {!justificationError && loadingJustifications ? (
        <div className="presence-feedback-box">
          <Loader2 size={22} className="spin-icon" />
          Chargement des justificatifs...
        </div>
      ) : null}

      {!justificationError && !loadingJustifications ? (
        <>
          <div className="presence-history-head">
            <div className="presence-history-copy">
              <span className="selection-title">Vue justificatifs</span>
              <h2>Liste transversale des justificatifs</h2>
            </div>

            <div className="selected-date-badge">
              {filteredJustifications.length} resultat{filteredJustifications.length > 1 ? 's' : ''}
              <span>{justificationOverview.pending} en attente</span>
            </div>
          </div>

          {filteredJustifications.length > 0 ? (
            <div className="justification-list-card">
              {filteredJustifications.map((record) => {
                const isProcessing = Boolean(processingJustification[record.presenceId]);
                return (
                  <article key={record.id} className="justification-list-row">
                    <div className="justification-date-box">
                      <strong>{formatDateLabel(record.absenceDate, { day: '2-digit', month: '2-digit', year: 'numeric' })}</strong>
                      <span>{formatHourLabel(record.heureDebut || record.absenceDate) || '--:--'}</span>
                    </div>

                    <div className="justification-main-box">
                      <div className="justification-main-head">
                        <div>
                          <h3>{record.etudiantNomComplet}</h3>
                          <span>Apogee : {record.apogee}</span>
                        </div>
                        <div className={`justification-chip ${record.statusMeta.key}`}>
                          {record.statusMeta.label}
                        </div>
                      </div>

                      <div className="justification-meta-grid">
                        <span><strong>Classe</strong> {record.classeLabel}</span>
                        <span><strong>Matiere</strong> {record.matiereLabel}</span>
                        <span><strong>Seance</strong> {buildSeanceLabel(record)}</span>
                        <span><strong>Statut</strong> {record.statusMeta.shortLabel}</span>
                      </div>

                      <p className="justification-motif-text">
                        {record.previewText || 'Aucun motif saisi.'}
                      </p>
                    </div>

                    <div className="justification-row-actions">
                      <button
                        type="button"
                        className="btn-justification btn-view"
                        onClick={() => openJustificationDrawer(record)}
                      >
                        <Eye size={14} />
                        <span>Consulter</span>
                      </button>

                      {record.canProcess ? (
                        <>
                          <button
                            type="button"
                            className="btn-justification btn-approve"
                            disabled={isProcessing}
                            onClick={() => handleJustificationDecision(record, 'approve')}
                          >
                            {processingJustification[record.presenceId] === 'approve' ? '...' : 'Valider'}
                          </button>
                          <button
                            type="button"
                            className="btn-justification btn-reject"
                            disabled={isProcessing}
                            onClick={() => handleJustificationDecision(record, 'reject')}
                          >
                            {processingJustification[record.presenceId] === 'reject' ? '...' : 'Refuser'}
                          </button>
                        </>
                      ) : null}
                    </div>
                  </article>
                );
              })}
            </div>
          ) : (
            <div className="presence-empty-state">
              <FileText size={48} className="presence-empty-icon" />
              <p>Aucun justificatif ne correspond aux filtres actuels.</p>
            </div>
          )}
        </>
      ) : null}
    </>
  );

  const renderJustificationDrawer = () => {
    if (!selectedJustification) return null;

    return (
      <div className="justification-drawer-overlay" onClick={closeJustificationDrawer}>
        <aside
          className="justification-drawer"
          role="dialog"
          aria-modal="true"
          aria-label="Consultation d'un justificatif"
          onClick={(event) => event.stopPropagation()}
        >
          <div className="justification-drawer-head">
            <div>
              <span className="selection-title">Consultation justificatif</span>
              <h2>{selectedJustification.etudiantNomComplet}</h2>
              <p>{selectedJustification.classeLabel} | {selectedJustification.matiereLabel}</p>
            </div>

            <button type="button" className="justification-drawer-close" onClick={closeJustificationDrawer}>
              <X size={18} />
            </button>
          </div>

          <div className="justification-drawer-body">
            <div className="justification-drawer-top">
              <div className={`justification-chip large ${selectedJustification.statusMeta.key}`}>
                {selectedJustification.statusMeta.label}
              </div>
              <div className="presence-session-chip tone-grey">
                <Calendar size={14} />
                <span>{formatDateLabel(selectedJustification.absenceDate, { weekday: 'long', day: '2-digit', month: 'long', year: 'numeric' })}</span>
              </div>
            </div>

            <div className="justification-detail-grid">
              <article className="justification-detail-card">
                <span className="selection-title">Etudiant</span>
                <strong>{selectedJustification.etudiantNomComplet}</strong>
                <p>Apogee : {selectedJustification.apogee}</p>
              </article>

              <article className="justification-detail-card">
                <span className="selection-title">Classe</span>
                <strong>{selectedJustification.classeLabel}</strong>
                <p>{selectedJustification.codeClasse || 'Classe affectee'}</p>
              </article>

              <article className="justification-detail-card">
                <span className="selection-title">Seance / matiere</span>
                <strong>{selectedJustification.matiereLabel}</strong>
                <p>{buildSeanceLabel(selectedJustification)}</p>
              </article>

              <article className="justification-detail-card">
                <span className="selection-title">Motif</span>
                <strong>{selectedJustification.motifType || 'Motif libre'}</strong>
                <p>{selectedJustification.motif || selectedJustification.previewText || 'Aucune precision fournie.'}</p>
              </article>
            </div>

            <div className="justification-document-card">
              <div className="justification-document-head">
                <div>
                  <span className="selection-title">Document justificatif</span>
                  <h3>{selectedJustification.documentName || 'Document transmis par l etudiant'}</h3>
                </div>

                {documentState.url ? (
                  <button
                    type="button"
                    className="btn-justification btn-view"
                    onClick={() => window.open(documentState.url, '_blank', 'noopener,noreferrer')}
                  >
                    Ouvrir
                  </button>
                ) : null}
              </div>

              {documentState.loading ? (
                <div className="justification-document-empty">
                  <Loader2 size={18} className="spin-icon" />
                  Chargement du document...
                </div>
              ) : documentState.error ? (
                <div className="justification-document-empty error">
                  <span>{documentState.error}</span>
                </div>
              ) : documentState.url ? (
                <iframe
                  title={`Document justificatif ${selectedJustification.etudiantNomComplet}`}
                  src={documentState.url}
                  className="justification-document-frame"
                />
              ) : (
                <div className="justification-document-empty">
                  <span>Aucun document previsualisable pour ce justificatif.</span>
                </div>
              )}
            </div>
          </div>

          <div className="justification-drawer-footer">
            <button type="button" className="btn-justification btn-secondary" onClick={closeJustificationDrawer}>
              Fermer
            </button>

            {selectedJustification.canProcess ? (
              <div className="justification-footer-actions">
                <button
                  type="button"
                  className="btn-justification btn-approve"
                  disabled={Boolean(processingJustification[selectedJustification.presenceId])}
                  onClick={() => handleJustificationDecision(selectedJustification, 'approve')}
                >
                  {processingJustification[selectedJustification.presenceId] === 'approve' ? '...' : 'Valider'}
                </button>
                <button
                  type="button"
                  className="btn-justification btn-reject"
                  disabled={Boolean(processingJustification[selectedJustification.presenceId])}
                  onClick={() => handleJustificationDecision(selectedJustification, 'reject')}
                >
                  {processingJustification[selectedJustification.presenceId] === 'reject' ? '...' : 'Refuser'}
                </button>
              </div>
            ) : null}
          </div>
        </aside>
      </div>
    );
  };

  return (
    <div className="gestion-presence-container">
      <div className="presence-hero-card">
        <div className="presence-hero-shell">
          <div className="presence-hero-main">
            <div className="presence-hero-badge">Gestion des presences</div>

            <div className="presence-brand-row">
              <div className="presence-icon-main">
                <UserCheck size={22} color="white" />
              </div>

              <div className="presence-title-block">
                <h1>Presence professeur</h1>
                <p>
                  {activeView === 'feuille'
                    ? selectedEnseignement
                      ? `${selectedEnseignement.nomMatiere || selectedEnseignement.codeMatiere} - ${selectedEnseignement.codeClasse || 'Classe'}`
                      : 'Selectionnez un cours et une seance pour commencer.'
                    : "Traitement transversal des justificatifs d'absence sur l'ensemble de vos classes."}
                </p>
              </div>
            </div>

            {activeView === 'feuille' ? (
              <div className="presence-session-chips">
                <div className={`presence-session-chip tone-${couleur}`}>
                  <Book size={14} />
                  <span>{selectedSeance?.type || 'Seance'}</span>
                </div>

                {selectedSeance ? (
                  <div className="presence-session-chip">
                    <Calendar size={14} />
                    <span>{formatDateLabel(selectedSeance.heureDebut || selectedSeance.date, { weekday: 'long', day: '2-digit', month: 'long' })}</span>
                  </div>
                ) : null}

                {selectedSeance?.heureDebut ? (
                  <div className="presence-session-chip">
                    <Clock3 size={14} />
                    <span>
                      {formatHourLabel(selectedSeance.heureDebut)}
                      {selectedSeance.heureFin ? ` - ${formatHourLabel(selectedSeance.heureFin)}` : ''}
                    </span>
                  </div>
                ) : null}

                {selectedSeance?.salle ? (
                  <div className="presence-session-chip">
                    <MapPin size={14} />
                    <span>Salle {selectedSeance.salle}</span>
                  </div>
                ) : null}
              </div>
            ) : (
              <div className="presence-session-chips">
                <div className="presence-session-chip tone-purple">
                  <Layers3 size={14} />
                  <span>Vue transverse</span>
                </div>
                <div className="presence-session-chip">
                  <FileText size={14} />
                  <span>{justificationOverview.total} dossier{justificationOverview.total > 1 ? 's' : ''}</span>
                </div>
                {justificationFilters.status !== 'TOUS' ? (
                  <div className="presence-session-chip">
                    <CheckCircle2 size={14} />
                    <span>{pickFirstValue(STATUS_FILTER_OPTIONS.find((item) => item.value === justificationFilters.status)?.label, 'Filtre actif')}</span>
                  </div>
                ) : null}
              </div>
            )}
          </div>

          <div className="presence-stats-grid">
            {activeView === 'feuille' ? (
              <>
                <article className="presence-stat-card tone-present">
                  <span className="presence-stat-label">Presents</span>
                  <strong className="presence-stat-value">{summary.presents}</strong>
                </article>
                <article className="presence-stat-card tone-absent">
                  <span className="presence-stat-label">Absents</span>
                  <strong className="presence-stat-value">{summary.absents}</strong>
                </article>
                <article className="presence-stat-card tone-total">
                  <span className="presence-stat-label">Total</span>
                  <strong className="presence-stat-value">{summary.total}</strong>
                </article>
              </>
            ) : (
              <>
                <article className="presence-stat-card tone-pending">
                  <span className="presence-stat-label">En attente</span>
                  <strong className="presence-stat-value">{justificationOverview.pending}</strong>
                </article>
                <article className="presence-stat-card tone-valid">
                  <span className="presence-stat-label">Valides</span>
                  <strong className="presence-stat-value">{justificationOverview.validated}</strong>
                </article>
                <article className="presence-stat-card tone-refused">
                  <span className="presence-stat-label">Refuses</span>
                  <strong className="presence-stat-value">{justificationOverview.rejected}</strong>
                </article>
              </>
            )}
          </div>
        </div>
      </div>

      <div className="presence-view-switcher">
        <div className="presence-view-tabs" role="tablist" aria-label="Navigation presence professeur">
          {VIEW_OPTIONS.map((view) => {
            const Icon = view.icon;
            const isActive = activeView === view.id;
            return (
              <button
                key={view.id}
                type="button"
                className={`presence-view-tab ${isActive ? 'active' : ''}`}
                onClick={() => setActiveView(view.id)}
              >
                <Icon size={14} />
                <span>{view.label}</span>
              </button>
            );
          })}
        </div>

        <div className="selected-date-badge">
          {currentViewBadge}
          {activeView === 'justificatifs'
            ? <span>{justificationFilters.status === 'TOUS' ? 'Tous statuts' : justificationFilters.status}</span>
            : <span>{lignes.length} etudiant{lignes.length > 1 ? 's' : ''}</span>}
        </div>
      </div>

      {activeView === 'feuille' ? renderCallSheetView() : renderJustificationView()}
      {renderJustificationDrawer()}
    </div>
  );
};

export default GestionPresenceProfesseur;
