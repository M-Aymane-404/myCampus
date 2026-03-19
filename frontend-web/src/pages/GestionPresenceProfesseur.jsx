import { useEffect, useMemo, useState } from 'react';
import { UserCheck, ChevronDown, Loader2, X, Calendar, Book, Clock3, MapPin } from 'lucide-react';
import { apiEnseignementsProfesseur } from '../services/professeur/apiEnseignementsProfesseur';
import apiSeancesProfesseur from '../services/professeur/apiSeancesProfesseur';
import { apiPresencesProfesseur } from '../services/professeur/apiPresencesProfesseur';
import './GestionPresenceProfesseur.css';
import { openBlobInNewTab } from '../utils/fileAccess';

const TYPE_COLOR = { COURS: 'blue', TD: 'green', TP: 'purple' };

const formatDate = (value) => {
  if (!value) return '--';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '--';

  return date.toLocaleDateString('fr-FR', {
    weekday: 'long',
    day: '2-digit',
    month: 'long',
  });
};

const formatHeure = (value) => {
  if (!value) return '';

  const date = new Date(value);
  if (Number.isNaN(date.getTime())) return '';

  return date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

const normalizeStatus = (status) => {
  const normalized = String(status || '').trim().toUpperCase();

  if (['PRESENT', 'PRESENTE', 'PR', 'P'].includes(normalized)) return 'PRESENT';
  if (['ABSENT', 'ABSENTE', 'ABS', 'A'].includes(normalized)) return 'ABSENT';
  if (['JUSTIFIE', 'JUSTIFIEE', 'JUSTIFIED'].includes(normalized)) return 'JUSTIFIE';
  if (['RETARD', 'LATE'].includes(normalized)) return 'RETARD';

  return normalized;
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
  const normalizedLines = Array.isArray(lignes) ? lignes.map((ligne) => normalizeStatus(ligne.status)) : [];

  const presentsFromLines = normalizedLines.filter((status) => status === 'PRESENT').length;
  const absentsFromLines = normalizedLines.filter((status) => status === 'ABSENT').length;
  const totalFromLines = normalizedLines.length;

  const presents = pickNumber(stats.nbPresents, stats.nombrePresents, stats.presentCount, presentsFromLines) ?? 0;
  const absents = pickNumber(stats.nbAbsents, stats.nombreAbsents, stats.absentCount, absentsFromLines) ?? 0;
  const total = pickNumber(stats.totalEtudiants, stats.nbEtudiants, stats.total, totalFromLines) ?? 0;

  return { presents, absents, total };
};

const getStatusMeta = (status) => {
  const normalized = normalizeStatus(status);

  if (normalized === 'PRESENT') {
    return { label: 'Present', className: 'present' };
  }

  if (normalized === 'ABSENT') {
    return { label: 'Absent', className: 'absent' };
  }

  if (normalized === 'JUSTIFIE') {
    return { label: 'Justifie', className: 'justifie' };
  }

  if (normalized === 'RETARD') {
    return { label: 'Retard', className: 'retard' };
  }

  return { label: 'Non defini', className: 'default' };
};

const hasJustification = (ligne) =>
  Boolean(ligne?.justificationSoumise || ligne?.motif || ligne?.motifUrl || ligne?.motifNom || ligne?.typeMotif);

const getJustificationStatus = (ligne) => {
  const normalized = String(ligne?.statutJustificatif || '').trim().toUpperCase();

  if (normalized === 'VALIDE') return { key: 'validated', label: 'Justifie' };
  if (normalized === 'REFUSE') return { key: 'rejected', label: 'Refuse' };
  if (normalized === 'EN_ATTENTE') return { key: 'pending', label: 'En attente' };
  if (ligne?.justifiee) return { key: 'validated', label: 'Justifie' };
  if (hasJustification(ligne)) return { key: 'pending', label: 'En attente' };

  return { key: 'empty', label: 'Aucun justificatif' };
};

const openJustificationFile = async (seanceId, presenceId) => {
  if (!seanceId || !presenceId) {
    throw new Error('Impossible de localiser la présence ciblée.');
  }

  const { blob } = await apiPresencesProfesseur.telechargerJustificatif(seanceId, presenceId);
  openBlobInNewTab(blob);
};

const GestionPresenceProfesseur = () => {
  const [enseignements, setEnseignements] = useState([]);
  const [selectedEnseignement, setSelectedEnseignement] = useState(null);
  const [loadingEns, setLoadingEns] = useState(true);

  const [seances, setSeances] = useState([]);
  const [selectedSeance, setSelectedSeance] = useState(null);
  const [loadingSeances, setLoadingSeances] = useState(false);

  const [feuille, setFeuille] = useState(null);
  const [lignes, setLignes] = useState([]);
  const [loadingFeuille, setLoadingFeuille] = useState(false);

  const [saving, setSaving] = useState({});
  const [processingJustification, setProcessingJustification] = useState({});
  const [error, setError] = useState(null);

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
      if (showLoader) {
        setLoadingFeuille(false);
      }
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

    return () => {
      cancelled = true;
    };
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

    return () => {
      cancelled = true;
    };
  }, [selectedSeance]);

  useEffect(() => {
    if (!selectedSeance?.id) return undefined;

    const interval = window.setInterval(() => {
      if (Object.values(saving).some(Boolean) || Object.values(processingJustification).some(Boolean)) {
        return;
      }

      chargerFeuille(selectedSeance.id, false).catch(() => {});
    }, 10000);

    return () => window.clearInterval(interval);
  }, [selectedSeance?.id, saving, processingJustification]);

  const handleStatusChange = async (apogee, newStatus) => {
    if (!selectedSeance?.id) return;

    const previousLine = lignes.find((ligne) => ligne.apogee === apogee);
    const previousStatus = previousLine?.status;

    setLignes((prev) =>
        prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, status: newStatus } : ligne))
    );
    setSaving((prev) => ({ ...prev, [apogee]: true }));
    setError(null);

    try {
      const updated = await apiPresencesProfesseur.saisirPresence(selectedSeance.id, {
        apogee,
        status: newStatus,
      });

      setLignes((prev) =>
          prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, ...updated } : ligne))
      );
    } catch (err) {
      setLignes((prev) =>
          prev.map((ligne) => (ligne.apogee === apogee ? { ...ligne, status: previousStatus } : ligne))
      );
      setError(`Erreur enregistrement : ${err.message}`);
    } finally {
      setSaving((prev) => ({ ...prev, [apogee]: false }));
    }
  };

  const handleJustificationDecision = async (ligne, action) => {
    if (!selectedSeance?.id || !ligne?.idPresence) return;

    setProcessingJustification((prev) => ({ ...prev, [ligne.idPresence]: action }));
    setError(null);

    try {
      const updated = action === 'approve'
        ? await apiPresencesProfesseur.validerJustificatif(selectedSeance.id, ligne.idPresence)
        : await apiPresencesProfesseur.refuserJustificatif(selectedSeance.id, ligne.idPresence);

      setLignes((prev) =>
        prev.map((item) => (item.idPresence === ligne.idPresence ? { ...item, ...updated } : item))
      );
    } catch (err) {
      setError(err.message || 'Erreur lors du traitement du justificatif.');
    } finally {
      setProcessingJustification((prev) => ({ ...prev, [ligne.idPresence]: null }));
    }
  };

  const summary = useMemo(() => buildPresenceSummary(feuille, lignes), [feuille, lignes]);
  const couleur = selectedSeance ? TYPE_COLOR[selectedSeance.type] || 'grey' : 'grey';
  return (
      <div className="gestion-presence-container">
        <div className="presence-hero-card">
          <div className="presence-hero-shell">
            <div className="presence-hero-main">
              <div className="presence-hero-badge">
                Gestion des presences
              </div>

              <div className="presence-brand-row">
                <div className="presence-icon-main">
                  <UserCheck size={22} color="white" />
                </div>

                <div className="presence-title-block">
                  <h1>Presence professeur</h1>
                  <p>
                    {selectedEnseignement
                      ? `${selectedEnseignement.nomMatiere || selectedEnseignement.codeMatiere} - ${selectedEnseignement.codeClasse || 'Classe'}`
                      : 'Selectionnez un cours et une seance pour commencer.'}
                  </p>
                </div>
              </div>

              <div className="presence-session-chips">
                <div className={`presence-session-chip tone-${couleur}`}>
                  <Book size={14} />
                  <span>{selectedSeance?.type || 'Seance'}</span>
                </div>

                {selectedSeance ? (
                  <div className="presence-session-chip">
                    <Calendar size={14} />
                    <span>{formatDate(selectedSeance.heureDebut || selectedSeance.date)}</span>
                  </div>
                ) : null}

                {selectedSeance?.heureDebut ? (
                  <div className="presence-session-chip">
                    <Clock3 size={14} />
                    <span>
                      {formatHeure(selectedSeance.heureDebut)}
                      {selectedSeance.heureFin ? ` - ${formatHeure(selectedSeance.heureFin)}` : ''}
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
            </div>

            <div className="presence-stats-grid">
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
            </div>
          </div>
        </div>

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
                    <Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} />
                    Chargement...
                  </div>
              ) : (
                  <div className="select-wrapper-rounded">
                    <select
                        value={
                          selectedEnseignement
                              ? `${selectedEnseignement.codeClasse}|${selectedEnseignement.codeMatiere}`
                              : ''
                        }
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
                    <Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} />
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
                            {seance.type} | {formatDate(seance.heureDebut || seance.date)}
                            {seance.heureDebut ? ` | ${formatHeure(seance.heureDebut)}` : ''}
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

        {error && (
            <div className="presence-alert presence-alert-error">
              <span>{error}</span>
              <button
                  onClick={() => setError(null)}
                  className="presence-alert-close"
              >
                <X size={14} />
              </button>
            </div>
        )}

        {loadingFeuille && (
            <div className="presence-feedback-box">
              <Loader2 size={22} style={{ animation: 'spin 0.8s linear infinite' }} />
              Chargement de la feuille d'appel...
            </div>
        )}

        {!loadingFeuille && lignes.length > 0 && (
            <>
              <div className="presence-history-head">
                <div className="presence-history-copy">
                  <span className="selection-title">Feuille d'appel</span>
                  <h2>Liste des etudiants</h2>
                </div>

                {selectedSeance ? (
                  <div className="selected-date-badge">
                    {formatDate(selectedSeance.heureDebut || selectedSeance.date)}
                    <span>{lignes.length} etudiant{lignes.length > 1 ? 's' : ''}</span>
                  </div>
                ) : null}
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
                  const justificationStatus = getJustificationStatus(ligne);
                  const canReviewJustification = hasJustification(ligne) && justificationStatus.key === 'pending';

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
                            className={`btn-state btn-present ${normalizeStatus(ligne.status) === 'PRESENT' ? 'active' : ''}`}
                            onClick={() => handleStatusChange(ligne.apogee, 'PRESENT')}
                            disabled={saving[ligne.apogee]}
                        >
                          {saving[ligne.apogee] ? '...' : 'Present'}
                        </button>
                        <button
                            className={`btn-state btn-absent ${normalizeStatus(ligne.status) === 'ABSENT' ? 'active' : ''}`}
                            onClick={() => handleStatusChange(ligne.apogee, 'ABSENT')}
                            disabled={saving[ligne.apogee]}
                        >
                          {saving[ligne.apogee] ? '...' : 'Absent'}
                        </button>
                      </div>

                      <div className="presence-line-justification">
                        {normalizeStatus(ligne.status) === 'ABSENT' ? (
                          <div className="justification-panel">
                            <div className={`justification-state ${justificationStatus.key}`}>
                              <strong>{justificationStatus.label}</strong>
                              <span>
                                {ligne.typeMotif || ligne.motif || (ligne.motifNom ? ligne.motifNom : 'Aucun document soumis')}
                              </span>
                            </div>

                            {hasJustification(ligne) ? (
                              <div className="justification-actions">
                                <button
                                  type="button"
                                  className="btn-justification btn-view"
                                  onClick={() =>
                                    openJustificationFile(selectedSeance?.id, ligne.idPresence).catch((err) =>
                                      setError(err.message || 'Impossible de consulter le justificatif.')
                                    )
                                  }
                                >
                                  Consulter
                                </button>

                                {canReviewJustification ? (
                                  <>
                                    <button
                                      type="button"
                                      className="btn-justification btn-approve"
                                      disabled={Boolean(processingJustification[ligne.idPresence])}
                                      onClick={() => handleJustificationDecision(ligne, 'approve')}
                                    >
                                      {processingJustification[ligne.idPresence] === 'approve' ? '...' : 'Valider'}
                                    </button>
                                    <button
                                      type="button"
                                      className="btn-justification btn-reject"
                                      disabled={Boolean(processingJustification[ligne.idPresence])}
                                      onClick={() => handleJustificationDecision(ligne, 'reject')}
                                    >
                                      {processingJustification[ligne.idPresence] === 'reject' ? '...' : 'Refuser'}
                                    </button>
                                  </>
                                ) : null}
                              </div>
                            ) : null}
                          </div>
                        ) : (
                          <div className="justification-state empty">
                            <strong>Non applicable</strong>
                            <span>Le justificatif apparait seulement pour les absences.</span>
                          </div>
                        )}
                      </div>
                    </div>
                  );
                })}
              </div>
              </div>
            </>
        )}

        {!loadingFeuille && selectedSeance && lignes.length === 0 && !error && (
            <div className="presence-empty-state">
              <UserCheck size={48} className="presence-empty-icon" />
              <p>Aucun etudiant inscrit pour cette seance.</p>
            </div>
        )}

        {!loadingFeuille && !selectedSeance && !loadingSeances && seances.length === 0 && !loadingEns && (
            <div className="presence-empty-state">
              <Calendar size={48} className="presence-empty-icon" />
              <p>Aucune seance disponible pour ce cours.</p>
            </div>
        )}
      </div>
  );
};

export default GestionPresenceProfesseur;
