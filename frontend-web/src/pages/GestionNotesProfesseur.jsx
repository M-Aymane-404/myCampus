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
const ETAT_EXAMEN_OPTIONS = ['PROGRAMME', 'PASSE', 'CORRIGE'];

const drawerCreationInitial = {
  dateExamen: '',
  numeroExamen: '',
  typeExamen: 'NORMAL',
  duree: 120,
};

const drawerModificationInitial = {
  dateExamen: '',
  numeroExamen: '',
  typeExamen: 'NORMAL',
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
  if (!exam) return 'Selectionner un examen';
  return `#${exam.numeroExamen} - ${exam.typeExamen} - ${exam.semestre} - ${formatDateShort(exam.dateExamen)} [${exam.etat}]`;
};

const getEtatClass = (etat) => {
  if (etat === 'PROGRAMME') return 'state-programme';
  if (etat === 'PASSE') return 'state-passe';
  if (etat === 'CORRIGE') return 'state-corrige';
  return '';
};

const getCopyFileLabel = (ligne) =>
  ligne?.nomCopie ||
  ligne?.nomFichierCopie ||
  ligne?.copieNom ||
  ligne?.copieFilename ||
  '';

const getFileNameFromPath = (path) => {
  if (!path) return '';
  const normalized = path.split('?')[0].split('#')[0].replace(/\\/g, '/');
  const segments = normalized.split('/');
  return segments[segments.length - 1] || '';
};

const getCorrigeFileLabel = (examen) =>
    examen?.nomCorrige ||
    examen?.nomFichierCorrige ||
    examen?.corrigeNom ||
    examen?.corrigeFilename ||
    'PDF depose';

const getStoredPdfPath = (item, fallbackKeys = []) => {
  const candidates = [...fallbackKeys, 'copieEtudiantPath', 'copiePath', 'corrigePath', 'fichierPath', 'path'];
  for (const key of candidates) {
    const value = item?.[key];
    if (typeof value === 'string' && value.trim()) return value;
  }
  return '';
};

const openBlobInNewTab = (blob) => {
  const objectUrl = window.URL.createObjectURL(blob);
  window.open(objectUrl, '_blank', 'noopener,noreferrer');
  window.setTimeout(() => window.URL.revokeObjectURL(objectUrl), 60000);
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
  const [copyFileNames, setCopyFileNames] = useState({});
  const [corrigeFileNames, setCorrigeFileNames] = useState({});

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
      numeroExamen: selectedExamen.numeroExamen || '',
      typeExamen: selectedExamen.typeExamen || 'NORMAL',
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
          throw new Error('Selectionnez une classe et une matiere avant de creer un examen.');
        }

        const payload = {
          dateExamen: toIsoFromLocalInput(creationForm.dateExamen),
          numeroExamen: Number(creationForm.numeroExamen),
          typeExamen: creationForm.typeExamen,
          duree: Number(creationForm.duree),
        };

        if (!payload.dateExamen) throw new Error("La date de l'examen est obligatoire.");
        if (!payload.numeroExamen) throw new Error("Le numero de l'examen est obligatoire.");
        if (!payload.duree) throw new Error('La duree est obligatoire.');

        const created = await apiExamensProfesseur.creerExamen(
            selectedClasseCode,
            selectedMatiereCode,
            payload
        );

        await refreshExamens(created?.idExamen);
        setSuccess('Nouvel examen cree avec succes.');
      } else {
        if (!selectedExamenId) throw new Error('Aucun examen selectionne.');

        const payload = {
          dateExamen: toIsoFromLocalInput(modificationForm.dateExamen),
          duree: Number(modificationForm.duree),
          etat: modificationForm.etat,
        };

        if (!payload.dateExamen) throw new Error("La date de l'examen est obligatoire.");
        if (!payload.numeroExamen) throw new Error("Le numero de l'examen est obligatoire.");
        if (!payload.duree) throw new Error('La duree est obligatoire.');

        await apiExamensProfesseur.modifierExamen(selectedExamenId, payload);
        await refreshExamens(selectedExamenId);
        await refreshFeuille();
        setSuccess("Examen mis a jour avec succes.");
      }

      setDrawerOpen(false);
    } catch (err) {
      setError(err.message || "Erreur lors de l'enregistrement de l'examen.");
    } finally {
      setSubmittingExam(false);
    }
  };

  const handleQuickEtatChange = async (etat) => {
    if (!selectedExamen) return;
    if (etat === selectedExamen.etat) return;

    setError('');
    setSuccess('');

    try {
      const payload = { etat };

      await apiExamensProfesseur.modifierExamen(selectedExamenId, payload);
      await refreshExamens(selectedExamenId);
      await refreshFeuille();
      setSuccess(`Etat de l'examen mis a jour : ${etat}.`);
    } catch (err) {
      setError(err.message || "Impossible de modifier l'etat de l'examen.");
    }
  };

  const handleDeleteExam = async () => {
    if (!selectedExamenId) return;

    const confirmed = window.confirm(
        "Voulez-vous vraiment supprimer cet examen ? Cette action est irreversible."
    );
    if (!confirmed) return;

    setError('');
    setSuccess('');

    try {
      await apiExamensProfesseur.supprimerExamen(selectedExamenId);
      const deletedLabel = selectedExamen?.numeroExamen ? `n°${selectedExamen.numeroExamen}` : 'selectionne';
      setSelectedExamenId('');
      await refreshExamens();
      setSuccess(`Examen ${deletedLabel} supprime avec succes.`);
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
      setError(`La note de ${ligne.prenom} ${ligne.nom} doit etre comprise entre 0 et 20.`);
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
      setSuccess(`${totalPublished} resultat(s) publie(s) avec succes.`);
    } catch (err) {
      setError(err.message || 'Erreur lors de la publication des resultats.');
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
      setCorrigeFileNames((prev) => ({ ...prev, [selectedExamenId]: fichier.name }));
      await refreshExamens(selectedExamenId);
      setSuccess('Corrige depose avec succes.');
    } catch (err) {
      setError(err.message || 'Erreur lors du depot du corrige.');
    } finally {
      setUploadingCorrige(false);
      event.target.value = '';
    }
  };

  const handleDeleteCorrige = async () => {
    if (!selectedExamenId || !selectedExamHasCorrige) return;

    const confirmed = window.confirm('Voulez-vous vraiment supprimer ce corrige ?');
    if (!confirmed) return;

    setError('');
    setSuccess('');

    try {
      await apiExamensProfesseur.supprimerCorrige(selectedExamenId);
      setExamens((prev) =>
        prev.map((exam) =>
          String(exam.idExamen) === String(selectedExamenId)
            ? {
                ...exam,
                corrigeDisponible: false,
                corrigePath: '',
                nomCorrige: '',
                nomFichierCorrige: '',
                corrigeNom: '',
                corrigeFilename: '',
              }
            : exam
        )
      );
      setFeuille((prev) =>
        prev
          ? {
              ...prev,
              corrigeDisponible: false,
              corrigePath: '',
              nomCorrige: '',
              nomFichierCorrige: '',
            }
          : prev
      );
      setCorrigeFileNames((prev) => {
        const next = { ...prev };
        delete next[selectedExamenId];
        return next;
      });
      await refreshExamens(selectedExamenId);
      await refreshFeuille();
      setSuccess('Corrige supprime avec succes.');
    } catch (err) {
      setError(err.message || 'Erreur lors de la suppression du corrige.');
    }
  };

  const examForm = drawerMode === 'create' ? creationForm : modificationForm;
  const setExamForm = drawerMode === 'create' ? setCreationForm : setModificationForm;

  const lignes = feuille?.lignes || [];
  const stats = feuille?.stats || {};
  const selectedExamHasCorrige = !!selectedExamen?.corrigeDisponible;
  const selectedCorrigePath = getStoredPdfPath(selectedExamen, ['corrigePath']);
  const selectedCorrigeLabel =
      corrigeFileNames[selectedExamenId] ||
      getFileNameFromPath(selectedCorrigePath) ||
      getCorrigeFileLabel(selectedExamen);

  return (
      <div className="gestion-notes-modern notes-v2-page">
        <div className="notes-header-banner notes-header-banner-v2">
          <div className="header-left">
            <div className="header-icon-box">
              <FileText size={30} color="white" />
            </div>

            <div className="header-titles">
              <h1>Gestion des notes</h1>
              <p className="subtitle">SAISIE, EXAMENS & PUBLICATION DES RESULTATS</p>
            </div>
          </div>

          <div className="header-actions-inline">
            <button className="exam-action-outline primary" onClick={openCreateDrawer} type="button">
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

            <button
                className="exam-action-outline danger"
                onClick={handleDeleteExam}
                type="button"
                disabled={!selectedExamen}
            >
              <X size={16} />
              Supprimer
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
                ×
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
                ×
              </button>
            </div>
        )}

        <div className="notes-page-body">
          <div className="notes-main">
            {!selectedExamenId ? (
                <div className="notes-empty-card">
                  <BookOpen size={28} />
                  <h3>Aucun examen disponible</h3>
                  <p>Creez un nouvel examen depuis cette page pour commencer la saisie des notes.</p>
                  <button className="notes-empty-btn" type="button" onClick={openCreateDrawer}>
                    <Plus size={16} />
                    Creer un examen
                  </button>
                </div>
            ) : (
                <>
                  <div className="stats-card notes-inline-stats">
                    <div className="stats-card-header">
                      <TrendingUp size={16} color="#10B981" />
                      <span>STATISTIQUES</span>
                    </div>

                    <div className="notes-inline-stats-grid">
                      <div className="main-stat-box notes-inline-stat-main">
                        <div>
                          <span className="stat-label-mini">MOYENNE GENERALE</span>
                          <div className="stat-value-large">
                            {stats.moyenne != null ? `${Number(stats.moyenne).toFixed(2)}/20` : '--'}
                          </div>
                        </div>
                        <Award size={32} className="medal-icon-box" />
                      </div>

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

                    <div className="grade-table-card">
                      <div className="table-title-row">
                        <div className="title-with-icon">
                          <div className="plus-icon-box">
                            <ClipboardList size={20} />
                          </div>
                          <div className="table-title-copy">
                            <h2>
                              {feuille?.nomMatiere || selectedExamen?.nomMatiere} - Examen n°
                              {feuille?.numeroExamen || selectedExamen?.numeroExamen}
                            </h2>
                            {selectedExamen?.etat === 'PROGRAMME' && (
                                <p className="table-info-warning">
                                  Examen planifie : la saisie des notes est desactivee tant que l'etat n'est
                                  pas passe a <strong>PASSE</strong>.
                                </p>
                            )}
                            {selectedExamen?.etat === 'CORRIGE' && (
                                <p className="table-info-neutral">
                                  Examen deja corrige : toute modification de note necessitera une
                                  republication.
                                </p>
                            )}
                          </div>
                        </div>

                        <div className="exam-card-header-actions">
                          <span className="student-badge">
                            {stats.totalEtudiants || lignes.length} ETUDIANT{(stats.totalEtudiants || lignes.length) > 1 ? 'S' : ''}
                          </span>
                        </div>
                      </div>

                    {isLoadingFeuille ? (
                        <div className="notes-loading-box">
                          <LoaderCircle size={20} className="spin" />
                          Chargement de la feuille...
                        </div>
                    ) : (
                        <>
                        <div className="table-responsive-shell">
                          <table className="modern-grades-table">
                            <thead>
                            <tr>
                              <th>ETUDIANT</th>
                              <th>APOGEE</th>
                              <th>NOTE (/20)</th>
                              <th>STATUT</th>
                              <th>COPIE</th>
                            </tr>
                            </thead>

                            <tbody>
                            {lignes.map((ligne) => (
                                (() => {
                                  const ligneCopyPath = getStoredPdfPath(ligne, ['copieEtudiantPath', 'copiePath']);
                                  const hasStoredCopy = Boolean(ligne.copieDisponible || ligneCopyPath);
                                  const ligneCopyRawLabel =
                                      copyFileNames[ligne.idEtudiant] ||
                                      getFileNameFromPath(ligneCopyPath) ||
                                      getCopyFileLabel(ligne);
                                  const ligneCopyLabel = ligneCopyRawLabel || 'Consulter le fichier';
                                  return (
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
                                    Publiee
                                  </span>
                                      ) : (
                                          <span className="line-status draft">
                                    Non publiee
                                  </span>
                                      )}
                                    </div>
                                  </td>

                                  <td>
                                    <div className="copy-actions-cell">
                                      {hasStoredCopy ? (
                                        <button
                                          type="button"
                                          className="copy-file-indicator"
                                          title={ligneCopyLabel}
                                          onClick={() => {
                                            setError('');
                                            apiNotesProfesseur
                                              .obtenirCopieEtudiantBlob(
                                                selectedClasseCode,
                                                selectedExamenId,
                                                ligne.idEtudiant
                                              )
                                              .then(({ blob, filename }) => {
                                                setCopyFileNames((prev) => ({ ...prev, [ligne.idEtudiant]: filename }));
                                                openBlobInNewTab(blob);
                                              })
                                              .catch((err) =>
                                                setError(err.message || 'Erreur lors de la consultation du fichier.')
                                              );
                                          }}
                                        >
                                          <FileText size={14} />
                                          <div className="copy-file-indicator-text">
                                            <span className="copy-file-indicator-status">Depose</span>
                                            <strong>{ligneCopyLabel}</strong>
                                          </div>
                                        </button>
                                      ) : (
                                        <div className="copy-empty-indicator">
                                          <FileText size={14} />
                                          <div className="copy-file-indicator-text">
                                            <span className="copy-file-indicator-status">Aucun fichier depose</span>
                                            <strong>Deposer</strong>
                                          </div>
                                        </div>
                                      )}

                                      <label className="copy-upload-btn">
                                        <FileUp size={14} />
                                        Deposer
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
                                                setCopyFileNames((prev) => ({ ...prev, [ligne.idEtudiant]: file.name }));
                                                await refreshFeuille();
                                                setSuccess(`Copie deposee pour ${ligne.nom} ${ligne.prenom}.`);
                                              } catch (err) {
                                                setError(err.message || 'Erreur lors du depot de la copie.');
                                              } finally {
                                                e.target.value = '';
                                              }
                                            }}
                                        />
                                      </label>

                                      <button
                                          className="copy-download-btn"
                                          type="button"
                                          disabled={!hasStoredCopy}
                                          onClick={() => {
                                              setError('');
                                              apiNotesProfesseur
                                                  .telechargerCopieEtudiant(
                                                      selectedClasseCode,
                                                      selectedExamenId,
                                                      ligne.idEtudiant
                                                  )
                                                  .then(({ filename }) =>
                                                      setCopyFileNames((prev) => ({ ...prev, [ligne.idEtudiant]: filename }))
                                                  )
                                                  .catch((err) =>
                                                      setError(err.message || 'Erreur lors du telechargement.')
                                                  );
                                          }}
                                      >
                                        <Download size={14} />
                                        Telecharger
                                      </button>
                                    </div>
                                  </td>
                                </tr>
                                  );
                                })()
                            ))}

                            {lignes.length === 0 && (
                                <tr>
                                  <td colSpan="5">
                                    <div className="notes-empty-inline">
                                      Aucun etudiant trouve pour cette feuille.
                                    </div>
                                  </td>
                                </tr>
                            )}
                            </tbody>
                          </table>
                        </div>

                        <div className="notes-table-footer">
                          <button
                              className="btn-publish notes-footer-publish-btn"
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
                                      ? 'Republicer les resultats'
                                      : 'Publier les resultats'}
                                </>
                            )}
                          </button>
                        </div>
                        </>
                    )}
                  </div>

                  <div className="corrige-section-card">
                    <div className="corrige-section-head">
                      <div className="corrige-section-copy">
                        <div className="corrige-section-icon">
                          <UploadCloud size={18} />
                        </div>
                        <div>
                          <h3>Depot du corrige</h3>
                          <p>
                            {feuille?.nomMatiere || selectedExamen?.nomMatiere} - Examen n°
                            {feuille?.numeroExamen || selectedExamen?.numeroExamen}
                          </p>
                        </div>
                      </div>
                    </div>

                    <div className={`corrige-file-banner ${selectedExamHasCorrige ? 'is-available' : ''}`}>
                      {selectedExamHasCorrige ? (
                          <button
                              type="button"
                              className="corrige-file-link"
                          onClick={() => {
                              setError('');
                              apiExamensProfesseur
                                  .obtenirCorrigeBlob(selectedExamenId)
                                  .then(({ blob, filename }) => {
                                    setCorrigeFileNames((prev) => ({ ...prev, [selectedExamenId]: filename }));
                                    openBlobInNewTab(blob);
                                  })
                                  .catch((err) =>
                                      setError(err.message || 'Erreur lors de la consultation du corrige.')
                                  );
                          }}
                      >
                            <FileText size={16} />
                            <div>
                              <span>PDF du corrige depose</span>
                              <strong>{selectedCorrigeLabel}</strong>
                            </div>
                          </button>
                      ) : (
                          <>
                            <FileText size={16} />
                            <div>
                              <span>Aucun PDF depose</span>
                              <strong>Deposez un seul fichier PDF</strong>
                            </div>
                          </>
                      )}
                    </div>

                    <div className="corrige-section-actions">
                      <button
                          className="btn-draft corrige-btn"
                          type="button"
                          onClick={() => corrigInputRef.current?.click()}
                          disabled={!selectedExamen || uploadingCorrige}
                      >
                        {uploadingCorrige ? <LoaderCircle size={18} className="spin" /> : <UploadCloud size={18} />}
                        Deposer corrige
                      </button>

                      <input
                          ref={corrigInputRef}
                          type="file"
                          accept="application/pdf"
                          hidden
                          onChange={handleUploadCorrige}
                      />

                      <button
                          className="btn-draft corrige-btn"
                          type="button"
                          disabled={!selectedExamHasCorrige}
                          onClick={() => {
                              setError('');
                              apiExamensProfesseur
                                  .telechargerCorrige(selectedExamenId)
                                  .then(({ filename }) =>
                                      setCorrigeFileNames((prev) => ({ ...prev, [selectedExamenId]: filename }))
                                  )
                                  .catch((err) =>
                                      setError(err.message || 'Erreur lors du telechargement du corrige.')
                                  );
                          }}
                      >
                        <Download size={18} />
                        Telecharger corrige
                      </button>

                      <button
                          className="btn-danger corrige-btn"
                          type="button"
                          disabled={!selectedExamHasCorrige}
                          onClick={handleDeleteCorrige}
                      >
                        <X size={18} />
                        Supprimer corrige
                      </button>
                    </div>

                  </div>
                </>
            )}
          </div>

          <aside className="notes-side">
            <div className="notes-toolbar-card notes-toolbar-side-card">
              <div className="side-card-title">
                <BookOpen size={16} />
                <span>FILTRES</span>
              </div>

              <div className="notes-toolbar-grid notes-toolbar-grid-side">
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
                  <label>Matiere</label>
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
            </div>

            <div className="exam-context-card right-side-card">
              <div className="side-card-title">
                <CalendarDays size={16} />
                <span>ETAT DE L'EXAMEN</span>
              </div>

              {selectedExamen ? (
                  <div className="exam-context-grid">
                    <div className="exam-context-item">
                      <span>Type</span>
                      <strong>{selectedExamen.typeExamen}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Numero</span>
                      <strong>#{selectedExamen.numeroExamen}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Semestre</span>
                      <strong>{selectedExamen.semestre}</strong>
                    </div>
                    <div className="exam-context-item">
                      <span>Duree</span>
                      <strong>{selectedExamen.duree} min</strong>
                    </div>
                    <div className="exam-context-item full">
                      <span>Date</span>
                      <strong>{formatDateLong(selectedExamen.dateExamen)}</strong>
                    </div>
                    <div className="exam-context-item full">
                      <span>Etat</span>
                      <strong className={`state-badge ${getEtatClass(selectedExamen.etat)}`}>
                        {selectedExamen.etat}
                      </strong>
                    </div>
                  </div>
              ) : (
                  <div className="side-empty">Selectionnez un examen.</div>
              )}

              <div className="exam-status-editor">
                <div className="exam-status-editor-copy">
                  <span>Mise a jour d'etat</span>
                  <small>Action distincte des filtres de recherche.</small>
                </div>
                <div className="notes-select-shell notes-select-shell-compact">
                  <select
                      value={selectedExamen?.etat || ''}
                      onChange={(e) => handleQuickEtatChange(e.target.value)}
                      disabled={!selectedExamen}
                  >
                    {!selectedExamen ? (
                        <option value="">Aucun examen</option>
                    ) : (
                        ETAT_EXAMEN_OPTIONS.map((etat) => (
                            <option key={etat} value={etat}>
                              {etat}
                            </option>
                        ))
                    )}
                  </select>
                  <ChevronDown size={18} />
                </div>
              </div>
            </div>

            <div className="exam-context-card right-side-card">
              <div className="side-card-title">
                <CheckCircle2 size={16} />
                <span>PUBLICATION</span>
              </div>

              <div className="exam-context-grid">
                <div className="exam-context-item">
                  <span>Publiees</span>
                  <strong>{publicationStats.publiees}</strong>
                </div>
                <div className="exam-context-item">
                  <span>En attente</span>
                  <strong>{publicationStats.nonPubliees}</strong>
                </div>
                <div className="exam-context-item full">
                  <span>Corrige</span>
                  <strong>{selectedExamHasCorrige ? 'Disponible' : 'Non depose'}</strong>
                </div>
              </div>
            </div>

          </aside>
        </div>

        {drawerOpen && (
            <div className="exam-drawer-overlay" onClick={closeDrawer}>
              <div className="exam-drawer" onClick={(e) => e.stopPropagation()}>
                <div className="exam-drawer-header">
                  <div>
                    <h3>{drawerMode === 'create' ? 'Nouvel examen' : "Modifier l'examen"}</h3>
                    <p>
                      {drawerMode === 'create'
                          ? 'Creez un examen puis saisissez directement les notes depuis cette page.'
                          : "Retrouvez le meme formulaire que la creation pour modifier l'examen."}
                    </p>
                  </div>

                  <button className="drawer-close-btn" type="button" onClick={closeDrawer}>
                    <X size={18} />
                  </button>
                </div>

                <div className="drawer-form-grid">
                  {drawerMode === 'edit' && (
                    <div className="drawer-field">
                      <label>Semestre</label>
                      <div className="drawer-field-readonly">
                        {selectedExamen?.semestre || '--'}
                      </div>
                    </div>
                  )}

                  <div className="drawer-field">
                    <label>Numero examen</label>
                    <input
                        type="number"
                        min="1"
                        value={examForm.numeroExamen}
                        onChange={(e) =>
                            setExamForm((prev) => ({ ...prev, numeroExamen: e.target.value }))
                        }
                    />
                  </div>

                  <div className="drawer-field">
                    <label>Type examen</label>
                    <select
                        value={examForm.typeExamen}
                        onChange={(e) =>
                            setExamForm((prev) => ({ ...prev, typeExamen: e.target.value }))
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
                    <label>Duree (min)</label>
                    <input
                        type="number"
                        min="1"
                        value={examForm.duree}
                        onChange={(e) =>
                            setExamForm((prev) => ({ ...prev, duree: e.target.value }))
                        }
                    />
                  </div>

                  <div className="drawer-field full">
                    <label>Date examen</label>
                    <input
                        type="datetime-local"
                        value={examForm.dateExamen}
                        onChange={(e) =>
                            setExamForm((prev) => ({ ...prev, dateExamen: e.target.value }))
                        }
                    />
                  </div>

                  {drawerMode === 'edit' && (
                    <div className="drawer-field full">
                      <label>Etat</label>
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
                  )}

                </div>

                <div className="drawer-help-box">
                  <strong>Logique metier :</strong>
                  <span>
                PROGRAMME = pas de saisie, PASSE = saisie ouverte, CORRIGE = resultats publies ou
                corriges.
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

