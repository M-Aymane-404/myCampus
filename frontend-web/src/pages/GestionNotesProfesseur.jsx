import React, { useEffect, useMemo, useRef, useState } from 'react';
import {
  FileText,
  ChevronDown,
  Plus,
  TrendingUp,
  Award,
  Globe,
  CheckCircle2,
  UploadCloud,
  CalendarDays,
  Clock3,
  Pencil,
  X,
  AlertTriangle,
  BookOpen,
  ClipboardList,
  FileUp,
  Download,
  LoaderCircle,
} from 'lucide-react';
import apiExamensProfesseur from '../services/professeur/apiExamensProfesseur';
import apiNotesProfesseur from '../services/professeur/apiNotesProfesseur';
import './GestionNotesProfesseur.css';

const TYPE_EXAMEN_OPTIONS = ['NORMAL', 'RATTRAPAGE'];
const SEMESTRE_OPTIONS = ['S1', 'S2', 'S3', 'S4', 'S5', 'S6', 'S7', 'S8', 'S9'];
const ETAT_EXAMEN_OPTIONS = ['PROGRAMME', 'PASSE', 'CORRIGE'];

const drawerCreationInitial = {
  semestre: 'S1',
  dateExamen: '',
  numeroExamen: '',
  typeExamen: 'NORMAL',
  duree: 120,
};

const drawerModificationInitial = {
  dateExamen: '',
  duree: 120,
  etat: 'PROGRAMME',
};

const formatDateShort = (date) => {
  if (!date) return '--';
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return '--';

  return d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
};

const formatDateLong = (date) => {
  if (!date) return '--';
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return '--';

  return d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
};

const toLocalDateTimeInput = (value) => {
  if (!value) return '';
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '';
  const offset = d.getTimezoneOffset();
  const adjusted = new Date(d.getTime() - offset * 60000);
  return adjusted.toISOString().slice(0, 16);
};

const toIsoFromLocalInput = (value) => {
  if (!value) return null;
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return null;
  return d.toISOString();
};

const formatExamenLabel = (exam) => {
  if (!exam) return 'Sélectionner un examen';
  return `#${exam.numeroExamen} · ${exam.typeExamen} · ${exam.semestre} · ${formatDateShort(exam.dateExamen)} [${exam.etat}]`;
};

const getEtatClass = (etat) => {
  if (etat === 'PROGRAMME') return 'state-programme';
  if (etat === 'PASSE') return 'state-passe';
  if (etat === 'CORRIGE') return 'state-corrige';
  return '';
};

const GestionNotesProfesseur = () => {
  const [filtres, setFiltres] = useState([]);
  const [classes, setClasses] = useState([]);
  const [selectedClasseCode, setSelectedClasseCode] = useState('');
  const [selectedMatiereCode, setSelectedMatiereCode] = useState('');
  const [examens, setExamens] = useState([]);
  const [selectedExamenId, setSelectedExamenId] = useState('');
  const [feuille, setFeuille] = useState(null);

  const [isLoadingFiltres, setIsLoadingFiltres] = useState(true);
  const [isLoadingExamens, setIsLoadingExamens] = useState(false);
  const [isLoadingFeuille, setIsLoadingFeuille] = useState(false);

  const [notesLocales, setNotesLocales] = useState({});
  const [savingEtudiants, setSavingEtudiants] = useState({});
  const [publishing, setPublishing] = useState(false);
  const [uploadingCorrige, setUploadingCorrige] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState('create');
  const [creationForm, setCreationForm] = useState(drawerCreationInitial);
  const [modificationForm, setModificationForm] = useState(drawerModificationInitial);
  const [submittingExam, setSubmittingExam] = useState(false);

  const corrigInputRef = useRef(null);

  const selectedClasse = useMemo(
      () => classes.find((c) => c.codeClasse === selectedClasseCode) || null,
      [classes, selectedClasseCode]
  );

  const matieres = useMemo(() => selectedClasse?.matieres || [], [selectedClasse]);

  const selectedMatiere = useMemo(
      () => matieres.find((m) => m.codeMatiere === selectedMatiereCode) || null,
      [matieres, selectedMatiereCode]
  );

  const selectedExamen = useMemo(
      () => examens.find((e) => String(e.idExamen) === String(selectedExamenId)) || null,
      [examens, selectedExamenId]
  );

  const isSaisieDisabled = selectedExamen?.etat === 'PROGRAMME';

  const publicationStats = useMemo(() => {
    const lignes = feuille?.lignes || [];
    const publiees = lignes.filter((l) => l.publiee).length;
    const nonPubliees = lignes.length - publiees;
    return { publiees, nonPubliees };
  }, [feuille]);

  useEffect(() => {
    const init = async () => {
      setIsLoadingFiltres(true);
      setError('');
      try {
        const data = await apiExamensProfesseur.chargerFiltres();
        const classesData = Array.isArray(data?.classes) ? data.classes : [];
        setFiltres(classesData);
        setClasses(classesData);

        if (classesData.length > 0) {
          const firstClasse = classesData[0];
          setSelectedClasseCode(firstClasse.codeClasse);

          if (Array.isArray(firstClasse.matieres) && firstClasse.matieres.length > 0) {
            setSelectedMatiereCode(firstClasse.matieres[0].codeMatiere);
          }
        }
      } catch (err) {
        setError(err.message || 'Erreur lors du chargement des filtres.');
      } finally {
        setIsLoadingFiltres(false);
      }
    };

    init();
  }, []);

  useEffect(() => {
    if (!selectedClasseCode) return;

    const classe = classes.find((c) => c.codeClasse === selectedClasseCode);
    if (!classe) {
      setSelectedMatiereCode('');
      return;
    }

    const hasCurrent = classe.matieres?.some((m) => m.codeMatiere === selectedMatiereCode);
    if (!hasCurrent) {
      setSelectedMatiereCode(classe.matieres?.[0]?.codeMatiere || '');
    }
  }, [selectedClasseCode, classes, selectedMatiereCode]);

  useEffect(() => {
    const loadExamens = async () => {
      if (!selectedClasseCode || !selectedMatiereCode) {
        setExamens([]);
        setSelectedExamenId('');
        setFeuille(null);
        return;
      }

      setIsLoadingExamens(true);
      setError('');
      try {
        const data = await apiExamensProfesseur.listerExamens(selectedClasseCode, selectedMatiereCode);
        const examensData = Array.isArray(data) ? data : [];
        examensData.sort((a, b) => (a.numeroExamen || 0) - (b.numeroExamen || 0));
        setExamens(examensData);

        const hasSelected = examensData.some((e) => String(e.idExamen) === String(selectedExamenId));
        if (!hasSelected) {
          setSelectedExamenId(examensData[0]?.idExamen ? String(examensData[0].idExamen) : '');
        }
      } catch (err) {
        setError(err.message || 'Erreur lors du chargement des examens.');
        setExamens([]);
        setSelectedExamenId('');
      } finally {
        setIsLoadingExamens(false);
      }
    };

    loadExamens();
  }, [selectedClasseCode, selectedMatiereCode]);

  useEffect(() => {
    const loadFeuille = async () => {
      if (!selectedClasseCode || !selectedExamenId) {
        setFeuille(null);
        return;
      }

      setIsLoadingFeuille(true);
      setError('');
      try {
        const data = await apiNotesProfesseur.chargerFeuille(selectedClasseCode, selectedExamenId);
        setFeuille(data);

        const initialNotes = {};
        (data?.lignes || []).forEach((ligne) => {
          initialNotes[ligne.idEtudiant] =
              ligne.note === null || ligne.note === undefined ? '' : String(ligne.note);
        });
        setNotesLocales(initialNotes);
      } catch (err) {
        setError(err.message || 'Erreur lors du chargement de la feuille de notes.');
        setFeuille(null);
      } finally {
        setIsLoadingFeuille(false);
      }
    };

    loadFeuille();
  }, [selectedClasseCode, selectedExamenId]);

  const refreshExamens = async (preferredExamenId = null) => {
    if (!selectedClasseCode || !selectedMatiereCode) return;

    const data = await apiExamensProfesseur.listerExamens(selectedClasseCode, selectedMatiereCode);
    const examensData = Array.isArray(data) ? data : [];
    examensData.sort((a, b) => (a.numeroExamen || 0) - (b.numeroExamen || 0));
    setExamens(examensData);

    if (preferredExamenId) {
      setSelectedExamenId(String(preferredExamenId));
      return;
    }

    const stillExists = examensData.some((e) => String(e.idExamen) === String(selectedExamenId));
    if (!stillExists) {
      setSelectedExamenId(examensData[0]?.idExamen ? String(examensData[0].idExamen) : '');
    }
  };

  const refreshFeuille = async () => {
    if (!selectedClasseCode || !selectedExamenId) return;
    const data = await apiNotesProfesseur.chargerFeuille(selectedClasseCode, selectedExamenId);
    setFeuille(data);

    const refreshedNotes = {};
    (data?.lignes || []).forEach((ligne) => {
      refreshedNotes[ligne.idEtudiant] =
          ligne.note === null || ligne.note === undefined ? '' : String(ligne.note);
    });
    setNotesLocales(refreshedNotes);
  };

  const openCreateDrawer = () => {
    setCreationForm({
      ...drawerCreationInitial,
      semestre: selectedExamen?.semestre || 'S1',
      dateExamen: '',
      numeroExamen: examens.length > 0 ? (Math.max(...examens.map((e) => e.numeroExamen || 0)) + 1) : 1,
    });
    setDrawerMode('create');
    setDrawerOpen(true);
    setError('');
    setSuccess('');
  };

  const openEditDrawer = () => {
    if (!selectedExamen) return;

    setModificationForm({
      dateExamen: toLocalDateTimeInput(selectedExamen.dateExamen),
      duree: selectedExamen.duree || 120,
      etat: selectedExamen.etat || 'PROGRAMME',
    });
    setDrawerMode('edit');
    setDrawerOpen(true);
    setError('');
    setSuccess('');
  };

  const closeDrawer = () => {
    if (submittingExam) return;
    setDrawerOpen(false);
  };

  const handleSaveExam = async () => {
    setError('');
    setSuccess('');

    try {
      setSubmittingExam(true);

      if (drawerMode === 'create') {
        if (!selectedClasseCode || !selectedMatiereCode) {
          throw new Error('Sélectionnez une classe et une matière avant de créer un examen.');
        }

        const payload = {
          semestre: creationForm.semestre,
          dateExamen: toIsoFromLocalInput(creationForm.dateExamen),
          numeroExamen: Number(creationForm.numeroExamen),
          typeExamen: creationForm.typeExamen,
          duree: Number(creationForm.duree),
        };

        if (!payload.dateExamen) throw new Error("La date de l'examen est obligatoire.");
        if (!payload.numeroExamen) throw new Error("Le numéro de l'examen est obligatoire.");
        if (!payload.duree) throw new Error('La durée est obligatoire.');

        const created = await apiExamensProfesseur.creerExamen(
            selectedClasseCode,
            selectedMatiereCode,
            payload
        );

        await refreshExamens(created?.idExamen);
        setSuccess('Nouvel examen créé avec succès.');
      } else {
        if (!selectedExamenId) throw new Error('Aucun examen sélectionné.');

        const payload = {
          dateExamen: toIsoFromLocalInput(modificationForm.dateExamen),
          duree: Number(modificationForm.duree),
          etat: modificationForm.etat,
        };

        if (!payload.dateExamen) throw new Error("La date de l'examen est obligatoire.");
        if (!payload.duree) throw new Error('La durée est obligatoire.');

        await apiExamensProfesseur.modifierExamen(selectedExamenId, payload);
        await refreshExamens(selectedExamenId);
        await refreshFeuille();
        setSuccess("Examen mis à jour avec succès.");
      }

      setDrawerOpen(false);
    } catch (err) {
      setError(err.message || "Erreur lors de l'enregistrement de l'examen.");
    } finally {
      setSubmittingExam(false);
    }
  };

  const handleDeleteExam = async () => {
    if (!selectedExamenId) return;

    const confirmed = window.confirm(
        "Voulez-vous vraiment supprimer cet examen ? Cette action n'est possible que si le backend l'autorise."
    );
    if (!confirmed) return;

    setError('');
    setSuccess('');

    try {
      await apiExamensProfesseur.supprimerExamen(selectedExamenId);
      const deletedId = selectedExamenId;
      setSelectedExamenId('');
      await refreshExamens();
      setSuccess(`Examen ${deletedId} supprimé avec succès.`);
    } catch (err) {
      setError(err.message || "Impossible de supprimer l'examen.");
    }
  };

  const handleNoteChange = (etudiantId, value) => {
    if (value === '' || /^([0-9]|1[0-9]|20)?([.,][0-9]{0,2})?$/.test(value)) {
      setNotesLocales((prev) => ({ ...prev, [etudiantId]: value.replace(',', '.') }));
    }
  };

  const autosaveNote = async (ligne) => {
    if (!selectedClasseCode || !selectedExamenId || isSaisieDisabled) return;

    const raw = notesLocales[ligne.idEtudiant] ?? '';
    const normalized = raw === '' ? null : Number(raw);

    if (normalized !== null && (Number.isNaN(normalized) || normalized < 0 || normalized > 20)) {
      setError(`La note de ${ligne.prenom} ${ligne.nom} doit être comprise entre 0 et 20.`);
      return;
    }

    setSavingEtudiants((prev) => ({ ...prev, [ligne.idEtudiant]: true }));
    setError('');
    setSuccess('');

    try {
      const updated = await apiNotesProfesseur.autosaveNote(selectedClasseCode, selectedExamenId, {
        idEtudiant: ligne.idEtudiant,
        note: normalized,
      });

      setFeuille((prev) => {
        if (!prev) return prev;

        return {
          ...prev,
          lignes: prev.lignes.map((item) =>
              item.idEtudiant === ligne.idEtudiant ? { ...item, ...updated } : item
          ),
        };
      });

      await refreshFeuille();
    } catch (err) {
      setError(err.message || "Erreur lors de l'enregistrement de la note.");
    } finally {
      setSavingEtudiants((prev) => ({ ...prev, [ligne.idEtudiant]: false }));
    }
  };

  const handlePublier = async () => {
    if (!selectedClasseCode || !selectedExamenId) return;

    setPublishing(true);
    setError('');
    setSuccess('');

    try {
      const totalPublished = await apiNotesProfesseur.publierNotes(selectedClasseCode, selectedExamenId);
      await refreshExamens(selectedExamenId);
      await refreshFeuille();
      setSuccess(`${totalPublished} résultat(s) publié(s) avec succès.`);
    } catch (err) {
      setError(err.message || 'Erreur lors de la publication des résultats.');
    } finally {
      setPublishing(false);
    }
  };

  const handleUploadCorrige = async (event) => {
    const fichier = event.target.files?.[0];
    if (!fichier || !selectedExamenId) return;

    setUploadingCorrige(true);
    setError('');
    setSuccess('');

    try {
      await apiExamensProfesseur.deposerCorrige(selectedExamenId, fichier);
      await refreshExamens(selectedExamenId);
      setSuccess('Corrigé déposé avec succès.');
    } catch (err) {
      setError(err.message || 'Erreur lors du dépôt du corrigé.');
    } finally {
      setUploadingCorrige(false);
      event.target.value = '';
    }
  };

  const lignes = feuille?.lignes || [];
  const stats = feuille?.stats || {};
  const selectedExamHasCorrige = !!selectedExamen?.corrigeDisponible;

  return (
      <div className="gestion-notes-modern notes-v2-page">
        <div className="notes-header-banner notes-header-banner-v2">
          <div className="header-left">
            <div className="header-icon-box">
              <FileText size={30} color="white" />
            </div>

            <div className="header-titles">
              <h1>Gestion des notes</h1>
              <p className="subtitle">SAISIE, EXAMENS & PUBLICATION DES RÉSULTATS</p>
            </div>
          </div>

          <div className="header-actions-inline">
            <button className="exam-action-outline" onClick={openCreateDrawer} type="button">
              <Plus size={16} />
              Nouvel examen
            </button>

            <button
                className="exam-action-outline"
                onClick={openEditDrawer}
                type="button"
                disabled={!selectedExamen}
            >
              <Pencil size={16} />
              Modifier
            </button>
          </div>
        </div>

        {error && (
            <div className="notes-alert notes-alert-error">
              <div className="notes-alert-left">
                <AlertTriangle size={18} />
                <span>{error}</span>
              </div>
              <button type="button" onClick={() => setError('')}>
                ✕
              </button>
            </div>
        )}

        {success && (
            <div className="notes-alert notes-alert-success">
              <div className="notes-alert-left">
                <CheckCircle2 size={18} />
                <span>{success}</span>
              </div>
              <button type="button" onClick={() => setSuccess('')}>
                ✕
              </button>
            </div>
        )}

        <div className="notes-toolbar-card">
          <div className="notes-toolbar-grid">
            <div className="notes-select-group">
              <label>Classe</label>
              <div className="notes-select-shell">
                <select
                    value={selectedClasseCode}
                    onChange={(e) => setSelectedClasseCode(e.target.value)}
                    disabled={isLoadingFiltres}
                >
                  {classes.map((classe) => (
                      <option key={classe.codeClasse} value={classe.codeClasse}>
                        {classe.nomClasse || classe.codeClasse}
                      </option>
                  ))}
                </select>
                <ChevronDown size={18} />
              </div>
            </div>

            <div className="notes-select-group">
              <label>Matière</label>
              <div className="notes-select-shell">
                <select
                    value={selectedMatiereCode}
                    onChange={(e) => setSelectedMatiereCode(e.target.value)}
                    disabled={!selectedClasse || isLoadingFiltres}
                >
                  {matieres.map((matiere) => (
                      <option key={matiere.codeMatiere} value={matiere.codeMatiere}>
                        {matiere.nomMatiere}
                      </option>
                  ))}
                </select>
                <ChevronDown size={18} />
              </div>
            </div>

            <div className="notes-select-group">
              <label>Examen</label>
              <div className="notes-select-shell">
                <select
                    value={selectedExamenId}
                    onChange={(e) => setSelectedExamenId(e.target.value)}
                    disabled={isLoadingExamens || examens.length === 0}
                >
                  {examens.length === 0 ? (
                      <option value="">Aucun examen</option>
                  ) : (
                      examens.map((exam) => (
                          <option key={exam.idExamen} value={exam.idExamen}>
                            {formatExamenLabel(exam)}
                          </option>
                      ))
                  )}
                </select>
                <ChevronDown size={18} />
              </div>
            </div>
          </div>

          {selectedExamen && (
              <div className="notes-exam-summary-bar">
                <span><strong>Matière :</strong> {selectedExamen.nomMatiere}</span>
                <span><strong>Type :</strong> {selectedExamen.typeExamen}</span>
                <span><strong>Semestre :</strong> {selectedExamen.semestre}</span>
                <span>
              <strong>État :</strong>{' '}
                  <b className={`state-badge-inline ${getEtatClass(selectedExamen.etat)}`}>
                {selectedExamen.etat}
              </b>
            </span>
                <span><strong>Durée :</strong> {selectedExamen.duree} min</span>
                <span><strong>Date :</strong> {formatDateLong(selectedExamen.dateExamen)}</span>
              </div>
          )}
        </div>

        <div className="notes-page-body">
          <div className="notes-main">
            {!selectedExamenId ? (
                <div className="notes-empty-card">
                  <BookOpen size={28} />
                  <h3>Aucun examen disponible</h3>
                  <p>Créez un nouvel examen depuis cette page pour commencer la saisie des notes.</p>
                  <button className="notes-empty-btn" type="button" onClick={openCreateDrawer}>
                    <Plus size={16} />
                    Créer un examen
                  </button>
                </div>
            ) : (
                <>
                  <div className="grade-table-card">
                    <div className="table-title-row">
                      <div className="title-with-icon">
                        <div className="plus-icon-box">
                          <ClipboardList size={20} />
                        </div>
                        <div>
                          <h2>
                            {feuille?.nomMatiere || selectedExamen?.nomMatiere} — Examen n°
                            {feuille?.numeroExamen || selectedExamen?.numeroExamen}
                          </h2>
                          {selectedExamen?.etat === 'PROGRAMME' && (
                              <p className="table-info-warning">
                                Examen planifié : la saisie des notes est désactivée tant que l’état n’est
                                pas passé à <strong>PASSE</strong>.
                              </p>
                          )}
                          {selectedExamen?.etat === 'CORRIGE' && (
                              <p className="table-info-neutral">
                                Examen déjà corrigé : toute modification de note nécessitera une
                                republication.
                              </p>
                          )}
                        </div>
                      </div>

                      <span className="student-badge">
                    {stats.totalEtudiants || lignes.length} ÉTUDIANT{(stats.totalEtudiants || lignes.length) > 1 ? 'S' : ''}
                  </span>
                    </div>

                    {isLoadingFeuille ? (
                        <div className="notes-loading-box">
                          <LoaderCircle size={20} className="spin" />
                          Chargement de la feuille...
                        </div>
                    ) : (
                        <div className="table-responsive-shell">
                          <table className="modern-grades-table">
                            <thead>
                            <tr>
                              <th>ÉTUDIANT</th>
                              <th>APOGÉE</th>
                              <th>NOTE (/20)</th>
                              <th>STATUT</th>
                              <th>COPIE</th>
                            </tr>
                            </thead>

                            <tbody>
                            {lignes.map((ligne) => (
                                <tr key={ligne.idEtudiant}>
                                  <td className="student-cell">
                                    <div className="student-avatar">
                                      {(ligne.nom?.[0] || '') + (ligne.prenom?.[0] || '')}
                                    </div>
                                    <span className="student-name-bold">
                                {ligne.nom} {ligne.prenom}
                              </span>
                                  </td>

                                  <td className="apogee-cell">{ligne.apogee || '--'}</td>

                                  <td>
                                    <div className="note-input-wrap">
                                      <input
                                          type="text"
                                          className="note-input-modern"
                                          placeholder="--"
                                          value={notesLocales[ligne.idEtudiant] ?? ''}
                                          onChange={(e) => handleNoteChange(ligne.idEtudiant, e.target.value)}
                                          onBlur={() => autosaveNote(ligne)}
                                          disabled={isSaisieDisabled || !!savingEtudiants[ligne.idEtudiant]}
                                      />
                                      {savingEtudiants[ligne.idEtudiant] && (
                                          <LoaderCircle size={14} className="note-saving spin" />
                                      )}
                                    </div>
                                  </td>

                                  <td>
                                    <div className="line-status-cell">
                                      {ligne.publiee ? (
                                          <span className="line-status published">
                                    <CheckCircle2 size={14} />
                                    Publiée
                                  </span>
                                      ) : (
                                          <span className="line-status draft">
                                    Non publiée
                                  </span>
                                      )}
                                    </div>
                                  </td>

                                  <td>
                                    <div className="copy-actions-cell">
                                      <label className="copy-upload-btn">
                                        <FileUp size={14} />
                                        Déposer
                                        <input
                                            type="file"
                                            accept="application/pdf"
                                            onChange={async (e) => {
                                              const file = e.target.files?.[0];
                                              if (!file) return;
                                              try {
                                                await apiNotesProfesseur.deposerCopieEtudiant(
                                                    selectedClasseCode,
                                                    selectedExamenId,
                                                    ligne.idEtudiant,
                                                    file
                                                );
                                                await refreshFeuille();
                                                setSuccess(`Copie déposée pour ${ligne.nom} ${ligne.prenom}.`);
                                              } catch (err) {
                                                setError(err.message || 'Erreur lors du dépôt de la copie.');
                                              } finally {
                                                e.target.value = '';
                                              }
                                            }}
                                        />
                                      </label>

                                      <button
                                          className="copy-download-btn"
                                          type="button"
                                          disabled={!ligne.copieDisponible}
                                          onClick={() =>
                                              apiNotesProfesseur.telechargerCopieEtudiant(
                                                  selectedClasseCode,
                                                  selectedExamenId,
                                                  ligne.idEtudiant
                                              ).catch((err) =>
                                                  setError(err.message || 'Erreur lors du téléchargement.')
                                              )
                                          }
                                      >
                                        <Download size={14} />
                                        Télécharger
                                      </button>
                                    </div>
                                  </td>
                                </tr>
                            ))}

                            {lignes.length === 0 && (
                                <tr>
                                  <td colSpan="5">
                                    <div className="notes-empty-inline">
                                      Aucun étudiant trouvé pour cette feuille.
                                    </div>
                                  </td>
                                </tr>
                            )}
                            </tbody>
                          </table>
                        </div>
                    )}
                  </div>

                  <div className="notes-bottom-actions-card">
                    <button
                        className="btn-publish btn-publish-large"
                        type="button"
                        onClick={handlePublier}
                        disabled={!selectedExamen || isSaisieDisabled || publishing}
                    >
                      {publishing ? (
                          <>
                            <LoaderCircle size={18} className="spin" />
                            Publication...
                          </>
                      ) : (
                          <>
                            <Globe size={18} />
                            {selectedExamen?.etat === 'CORRIGE' && publicationStats.nonPubliees > 0
                                ? 'Republicer les résultats'
                                : 'Publier les résultats'}
                          </>
                      )}
                    </button>

                    <div className="auto-save-status">
                      <CheckCircle2 size={14} />
                      Auto-save actif à chaque sortie du champ
                    </div>
                  </div>
                </>
            )}
          </div>

          <aside className="notes-side">
            <div className="stats-card right-side-card">
              <div className="stats-card-header">
                <TrendingUp size={16} color="#10B981" />
                <span>STATISTIQUES</span>
              </div>

              <div className="main-stat-box">
                <div>
                  <span className="stat-label-mini">MOYENNE GÉNÉRALE</span>
                  <div className="stat-value-large">
                    {stats.moyenne != null ? `${Number(stats.moyenne).toFixed(2)}/20` : '--'}
                  </div>
                </div>
                <Award size={36} className="medal-icon-box" />
              </div>

              <div className="secondary-stats vertical-stats">
                <div className="stat-pill-small">
                  <h4>MAX</h4>
                  <p>{stats.max != null ? Number(stats.max).toFixed(2) : '--'}</p>
                </div>
                <div className="stat-pill-small">
                  <h4>MIN</h4>
                  <p>{stats.min != null ? Number(stats.min).toFixed(2) : '--'}</p>
                </div>
                <div className="stat-pill-small">
                  <h4>SAISIS</h4>
                  <p>
                    {stats.nbNotesSaisies || 0}/{stats.totalEtudiants || 0}
                  </p>
                </div>
              </div>
            </div>

            <div className="exam-context-card right-side-card">
              <div className="side-card-title">
                <CalendarDays size={16} />
                <span>ÉTAT DE L’EXAMEN</span>
              </div>

              {selectedExamen ? (
                  <div className="exam-context-grid">
                    <div className="exam-context-item">
                      <span>Type</span>
                      <strong>{selectedExamen.typeExamen}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Numéro</span>
                      <strong>#{selectedExamen.numeroExamen}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Semestre</span>
                      <strong>{selectedExamen.semestre}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Durée</span>
                      <strong>{selectedExamen.duree} min</strong>
                    </div>
                    <div className="exam-context-item full">
                      <span>Date</span>
                      <strong>{formatDateLong(selectedExamen.dateExamen)}</strong>
                    </div>
                    <div className="exam-context-item full">
                      <span>État</span>
                      <strong className={`state-badge ${getEtatClass(selectedExamen.etat)}`}>
                        {selectedExamen.etat}
                      </strong>
                    </div>
                  </div>
              ) : (
                  <div className="side-empty">Sélectionnez un examen.</div>
              )}
            </div>

            <div className="exam-context-card right-side-card">
              <div className="side-card-title">
                <CheckCircle2 size={16} />
                <span>PUBLICATION</span>
              </div>

              <div className="exam-context-grid">
                <div className="exam-context-item">
                  <span>Publiées</span>
                  <strong>{publicationStats.publiees}</strong>
                </div>
                <div className="exam-context-item">
                  <span>En attente</span>
                  <strong>{publicationStats.nonPubliees}</strong>
                </div>
                <div className="exam-context-item full">
                  <span>Corrigé</span>
                  <strong>{selectedExamHasCorrige ? 'Disponible' : 'Non déposé'}</strong>
                </div>
              </div>
            </div>

            <div className="actions-container right-side-card">
              <button className="btn-draft side-btn" type="button" onClick={openCreateDrawer}>
                <Plus size={18} />
                Nouvel examen
              </button>

              <button
                  className="btn-draft side-btn"
                  type="button"
                  onClick={openEditDrawer}
                  disabled={!selectedExamen}
              >
                <Pencil size={18} />
                Modifier l’examen
              </button>

              <button
                  className="btn-draft side-btn"
                  type="button"
                  onClick={() => corrigInputRef.current?.click()}
                  disabled={!selectedExamen || uploadingCorrige}
              >
                {uploadingCorrige ? <LoaderCircle size={18} className="spin" /> : <UploadCloud size={18} />}
                Déposer corrigé
              </button>

              <input
                  ref={corrigInputRef}
                  type="file"
                  accept="application/pdf"
                  hidden
                  onChange={handleUploadCorrige}
              />

              <button
                  className="btn-draft side-btn"
                  type="button"
                  disabled={!selectedExamHasCorrige}
                  onClick={() =>
                      apiExamensProfesseur.telechargerCorrige(selectedExamenId).catch((err) =>
                          setError(err.message || 'Erreur lors du téléchargement du corrigé.')
                      )
                  }
              >
                <Download size={18} />
                Télécharger corrigé
              </button>

              <button
                  className="btn-danger side-btn"
                  type="button"
                  disabled={!selectedExamen}
                  onClick={handleDeleteExam}
              >
                <X size={18} />
                Supprimer examen
              </button>
            </div>
          </aside>
        </div>

        {drawerOpen && (
            <div className="exam-drawer-overlay" onClick={closeDrawer}>
              <div className="exam-drawer" onClick={(e) => e.stopPropagation()}>
                <div className="exam-drawer-header">
                  <div>
                    <h3>{drawerMode === 'create' ? 'Nouvel examen' : 'Modifier l’examen'}</h3>
                    <p>
                      {drawerMode === 'create'
                          ? 'Créez un examen puis saisissez directement les notes depuis cette page.'
                          : 'Le backend autorise ici la modification de la date, de la durée et de l’état.'}
                    </p>
                  </div>

                  <button className="drawer-close-btn" type="button" onClick={closeDrawer}>
                    <X size={18} />
                  </button>
                </div>

                {drawerMode === 'create' ? (
                    <div className="drawer-form-grid">
                      <div className="drawer-field">
                        <label>Semestre</label>
                        <select
                            value={creationForm.semestre}
                            onChange={(e) =>
                                setCreationForm((prev) => ({ ...prev, semestre: e.target.value }))
                            }
                        >
                          {SEMESTRE_OPTIONS.map((s) => (
                              <option key={s} value={s}>
                                {s}
                              </option>
                          ))}
                        </select>
                      </div>

                      <div className="drawer-field">
                        <label>Numéro examen</label>
                        <input
                            type="number"
                            min="1"
                            value={creationForm.numeroExamen}
                            onChange={(e) =>
                                setCreationForm((prev) => ({ ...prev, numeroExamen: e.target.value }))
                            }
                        />
                      </div>

                      <div className="drawer-field">
                        <label>Type examen</label>
                        <select
                            value={creationForm.typeExamen}
                            onChange={(e) =>
                                setCreationForm((prev) => ({ ...prev, typeExamen: e.target.value }))
                            }
                        >
                          {TYPE_EXAMEN_OPTIONS.map((t) => (
                              <option key={t} value={t}>
                                {t}
                              </option>
                          ))}
                        </select>
                      </div>

                      <div className="drawer-field">
                        <label>Durée (min)</label>
                        <input
                            type="number"
                            min="1"
                            value={creationForm.duree}
                            onChange={(e) =>
                                setCreationForm((prev) => ({ ...prev, duree: e.target.value }))
                            }
                        />
                      </div>

                      <div className="drawer-field full">
                        <label>Date examen</label>
                        <input
                            type="datetime-local"
                            value={creationForm.dateExamen}
                            onChange={(e) =>
                                setCreationForm((prev) => ({ ...prev, dateExamen: e.target.value }))
                            }
                        />
                      </div>
                    </div>
                ) : (
                    <div className="drawer-form-grid">
                      <div className="drawer-field full">
                        <label>Date examen</label>
                        <input
                            type="datetime-local"
                            value={modificationForm.dateExamen}
                            onChange={(e) =>
                                setModificationForm((prev) => ({ ...prev, dateExamen: e.target.value }))
                            }
                        />
                      </div>

                      <div className="drawer-field">
                        <label>Durée (min)</label>
                        <input
                            type="number"
                            min="1"
                            value={modificationForm.duree}
                            onChange={(e) =>
                                setModificationForm((prev) => ({ ...prev, duree: e.target.value }))
                            }
                        />
                      </div>

                      <div className="drawer-field">
                        <label>État</label>
                        <select
                            value={modificationForm.etat}
                            onChange={(e) =>
                                setModificationForm((prev) => ({ ...prev, etat: e.target.value }))
                            }
                        >
                          {ETAT_EXAMEN_OPTIONS.map((etat) => (
                              <option key={etat} value={etat}>
                                {etat}
                              </option>
                          ))}
                        </select>
                      </div>
                    </div>
                )}

                <div className="drawer-help-box">
                  <strong>Logique métier :</strong>
                  <span>
                PROGRAMME = pas de saisie, PASSE = saisie ouverte, CORRIGE = résultats publiés ou
                corrigés.
              </span>
                </div>

                <div className="drawer-actions">
                  <button className="drawer-cancel" type="button" onClick={closeDrawer}>
                    Annuler
                  </button>

                  <button className="drawer-save" type="button" onClick={handleSaveExam} disabled={submittingExam}>
                    {submittingExam ? (
                        <>
                          <LoaderCircle size={16} className="spin" />
                          Enregistrement...
                        </>
                    ) : (
                        <>
                          <CheckCircle2 size={16} />
                          Enregistrer
                        </>
                    )}
                  </button>
                </div>
              </div>
            </div>
        )}
      </div>
  );
};

export default GestionNotesProfesseur;
