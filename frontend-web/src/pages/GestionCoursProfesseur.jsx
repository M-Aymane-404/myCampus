// import { useState } from 'react';
// import {
//   Plus, FileText, Video, ExternalLink, X
// } from 'lucide-react';
// import './GestionCoursProfesseur.css';
//
// const GestionCoursProfesseur = () => {
//   const [activeTab, setActiveTab] = useState('INF101');
//
//   // Mock Data
//   const courses = [
//     { id: 'INF101', label: 'INF101 - Informatique 101', name: 'Informatique 101' },
//     { id: 'MTH202', label: 'MTH202 - Mathématiques Appliquées', name: 'Mathématiques Appliquées' }
//   ];
//
//   const resources = {
//     'INF101': [
//       { id: 1, name: "Introduction à l'Algorithmique", type: 'PDF', date: '01/10/2023', icon: FileText, color: 'red' },
//       { id: 2, name: "Structures de Données", type: 'Video', date: '05/10/2023', icon: Video, color: 'blue' },
//       { id: 3, name: "TP 1: Listes Chaînées", type: 'TP', date: '10/10/2023', icon: Plus, color: 'green' }
//     ],
//     'MTH202': [
//       { id: 4, name: "Matrices et Vecteurs", type: 'PDF', date: '02/10/2023', icon: FileText, color: 'red' },
//       { id: 5, name: "Calcul Intégral", type: 'PDF', date: '06/10/2023', icon: FileText, color: 'red' }
//     ]
//   };

  // const currentResources = resources[activeTab] || [];
  // const currentCourseName = courses.find(c => c.id === activeTab)?.name;
  //
  // return (
  //   <div className="gestion-cours-container">
  //     {/* Header */}
  //     <div className="page-header">
  //       <div className="header-text">
  //         <h1>Gestion des Cours</h1>
  //         <p>Gérez vos supports pédagogiques, TP et TD.</p>
  //       </div>
  //       <button className="btn-add-doc">
  //         <Plus size={18} />
  //         Ajouter un document
  //       </button>
  //     </div>
  //
  //     {/* Tabs */}
  //     <div className="tabs-container">
  //       {courses.map(course => (
  //         <button
  //           key={course.id}
  //           className={`tab-btn ${activeTab === course.id ? 'active' : ''}`}
  //           onClick={() => setActiveTab(course.id)}
  //         >
  //           {course.label}
  //         </button>
  //       ))}
  //     </div>

    //   {/* Content Card */}
    //   <div className="content-card">
    //     <div className="card-header-bar">
    //       <h2>Documents pour {currentCourseName}</h2>
    //       <span className="file-count">{currentResources.length} FICHIERS</span>
    //     </div>
    //
    //     <table className="resources-table">
    //       <thead>
    //         <tr>
    //           <th className="th-name">NOM DU SUPPORT</th>
    //           <th className="th-type">TYPE</th>
    //           <th className="th-date">DATE D'AJOUT</th>
    //           <th className="th-action">ACTION</th>
    //         </tr>
    //       </thead>
    //       <tbody>
    //         {currentResources.map((resource, index) => (
    //           <tr key={resource.id}>
    //             <td className="td-name">
    //               <div className={`file-icon-wrapper bg-${resource.color}`}>
    //                 <resource.icon size={22} className={`icon-${resource.color}`} />
    //               </div>
    //               <span className="file-name">{resource.name}</span>
    //             </td>
    //             <td className="td-type">
    //               <span className={`type-badge badge-${resource.color}`}>
    //                 {resource.type}
    //               </span>
    //             </td>
    //             <td className="td-date">{resource.date}</td>
    //             <td className="td-action">
    //               {/* Showing actions only on the first item to match visual screenshot, or all if preferred. Let's make them accessible on all but styled precisely */}
    //               <div className="action-buttons">
    //                 <button className="btn-icon-action btn-open">
    //                   <ExternalLink size={18} />
    //                 </button>
    //                 <button className="btn-icon-action btn-delete">
    //                   <X size={18} />
    //                 </button>
    //               </div>
    //             </td>
    //           </tr>
    //         ))}
    //       </tbody>
    //     </table>
    //   </div>
    // </div>
 // );
//};

// export default GestionCoursProfesseur;



import { useEffect, useMemo, useState } from 'react';
import {
  BookOpen,
  GraduationCap,
  CalendarDays,
  Clock3,
  FolderOpen,
  ChevronRight,
  ChevronDown,
  Upload,
  Download,
  Trash2,
  FileText,
  File,
  Search,
  AlertTriangle,
  CheckCircle2,
  Info,
  Layers3,
  MapPin,
  Plus,
  Pencil,
  X,
  Save,
  LoaderCircle,
  MoreHorizontal,
} from 'lucide-react';
import apiSeancesProfesseur from '../services/professeur/apiSeancesProfesseur';
import { downloadBlob, openBlobInNewTab } from '../utils/fileAccess';
import './GestionCoursProfesseur.css';

const TYPE_OPTIONS = ['COURS', 'TD', 'TP'];
const ETAT_OPTIONS = ['PLANIFIEE', 'TERMINEE', 'ANNULEE'];
const DOCX_MIME =
  'application/vnd.openxmlformats-officedocument.wordprocessingml.document';
const SIMPLE_UPLOAD_ERROR_MESSAGE =
  "Le format du fichier n'est pas valide. Formats acceptes : PDF ou DOCX.";

const emptySeanceForm = {
  type: 'COURS',
  date: '',
  heureDebut: '08:00',
  heureFin: '10:00',
  etat: 'PLANIFIEE',
  salle: '',
  contenu: '',
};

const getTypeClass = (type) => {
  if (type === 'COURS') return 'gc-type-cours';
  if (type === 'TD') return 'gc-type-td';
  if (type === 'TP') return 'gc-type-tp';
  return 'gc-type-cours';
};

const getFileIcon = (mime = '') => {
  const lower = mime.toLowerCase();
  if (lower.includes('pdf')) return FileText;
  return File;
};

const getFileExtension = (filename = '') => {
  const normalized = String(filename).trim().toLowerCase();
  const index = normalized.lastIndexOf('.');
  return index >= 0 ? normalized.slice(index + 1) : '';
};

const isPdfFile = ({ type = '', name = '' } = {}) =>
  type.toLowerCase().includes('pdf') || getFileExtension(name) === 'pdf';

const isDocxFile = ({ type = '', name = '' } = {}) =>
  type === DOCX_MIME || getFileExtension(name) === 'docx';

const isSupportedCourseFile = (file) => isPdfFile(file) || isDocxFile(file);

const normalizeUploadErrorMessage = (message = '') => {
  const normalized = String(message).trim();
  const lower = normalized.toLowerCase();

  if (
    lower.includes('signature binaire du fichier non reconnue') ||
    lower.includes('format du fichier') ||
    lower.includes('type de fichier')
  ) {
    return SIMPLE_UPLOAD_ERROR_MESSAGE;
  }

  return normalized || 'Impossible de deposer le fichier.';
};

const formatDate = (date) => {
  if (!date) return '--';
  const d = new Date(date);
  if (Number.isNaN(d.getTime())) return '--';
  return d.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'short',
    year: 'numeric',
  });
};

const formatTime = (dateTime) => {
  if (!dateTime) return '--';
  const d = new Date(dateTime);
  if (Number.isNaN(d.getTime())) return '--';
  return d.toLocaleTimeString('fr-FR', {
    hour: '2-digit',
    minute: '2-digit',
  });
};

const formatBytes = (bytes) => {
  if (!bytes) return '0 B';
  const units = ['B', 'KB', 'MB', 'GB'];
  let value = bytes;
  let index = 0;
  while (value >= 1024 && index < units.length - 1) {
    value /= 1024;
    index += 1;
  }
  return `${value.toFixed(value >= 10 || index === 0 ? 0 : 1)} ${units[index]}`;
};

const sortSeances = (items) =>
    [...items].sort((a, b) => {
      const da = new Date(a.heureDebut || a.date || 0).getTime();
      const db = new Date(b.heureDebut || b.date || 0).getTime();
      return db - da;
    });

const toDateInput = (value) => {
  if (!value) return '';
  if (typeof value === 'string' && value.length >= 10) return value.slice(0, 10);
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '';
  return d.toISOString().slice(0, 10);
};

const toTimeInput = (value) => {
  if (!value) return '';
  if (typeof value === 'string' && value.includes('T')) return value.slice(11, 16);
  const d = new Date(value);
  if (Number.isNaN(d.getTime())) return '';
  return d.toISOString().slice(11, 16);
};

const buildDateTime = (date, time) => {
  if (!date || !time) return null;
  return `${date}T${time}:00`;
};

const parseTimeToMinutes = (time) => {
  if (!time || typeof time !== 'string') return NaN;
  const [hours, minutes] = time.split(':');
  const h = Number(hours);
  const m = Number(minutes);
  if (!Number.isFinite(h) || !Number.isFinite(m)) return NaN;
  return h * 60 + m;
};

const openCourseFile = async (fichier) => {
  if (!fichier?.id) {
    throw new Error('Aucun fichier lié à cette séance.');
  }

  const { blob, filename } = await apiSeancesProfesseur.telechargerFichierBlob(fichier.id);
  const resolvedFilename = filename || fichier.nomOriginal || 'document';

  if (isPdfFile({ type: blob.type, name: resolvedFilename })) {
    openBlobInNewTab(blob);
    return;
  }

  if (isDocxFile({ type: blob.type, name: resolvedFilename })) {
    downloadBlob(blob, resolvedFilename);
    return;
  }

  throw new Error(SIMPLE_UPLOAD_ERROR_MESSAGE);
};

const GestionCoursProfesseur = () => {
  const [enseignements, setEnseignements] = useState([]);
  const [selectedEnseignementKey, setSelectedEnseignementKey] = useState(null);

  const [seances, setSeances] = useState([]);
  const [fichiersParSeance, setFichiersParSeance] = useState({});
  const [loadingFichiersParSeance, setLoadingFichiersParSeance] = useState({});
  const [expandedSeances, setExpandedSeances] = useState({});
  const [fichiersIndisponibles, setFichiersIndisponibles] = useState({});

  const [loadingEnseignements, setLoadingEnseignements] = useState(true);
  const [loadingSeances, setLoadingSeances] = useState(false);
  const [uploadingSeanceId, setUploadingSeanceId] = useState(null);
  const [deletingFileId, setDeletingFileId] = useState(null);

  const [searchTeaching, setSearchTeaching] = useState('');
  const [message, setMessage] = useState({ type: '', text: '' });

  const [drawerOpen, setDrawerOpen] = useState(false);
  const [drawerMode, setDrawerMode] = useState('create');
  const [editingSeanceId, setEditingSeanceId] = useState(null);
  const [seanceForm, setSeanceForm] = useState(emptySeanceForm);
  const [savingSeance, setSavingSeance] = useState(false);
  const [deletingSeanceId, setDeletingSeanceId] = useState(null);

  const [openActionMenuSeanceId, setOpenActionMenuSeanceId] = useState(null);

  const normalizedEnseignements = useMemo(
      () =>
          enseignements.map((item) => ({
            ...item,
            _key: item.id ?? `${item.codeClasse}__${item.codeMatiere}`,
          })),
      [enseignements]
  );

  const filteredEnseignements = useMemo(() => {
    const q = searchTeaching.trim().toLowerCase();
    if (!q) return normalizedEnseignements;

    return normalizedEnseignements.filter((item) => {
      return (
          (item.matiereNom || '').toLowerCase().includes(q) ||
          (item.codeMatiere || '').toLowerCase().includes(q) ||
          (item.classeNom || '').toLowerCase().includes(q) ||
          (item.codeClasse || '').toLowerCase().includes(q)
      );
    });
  }, [normalizedEnseignements, searchTeaching]);

  const selectedEnseignement = useMemo(
      () =>
          normalizedEnseignements.find(
              (e) => String(e._key) === String(selectedEnseignementKey)
          ) || null,
      [normalizedEnseignements, selectedEnseignementKey]
  );

  const stats = useMemo(() => {
    const totalDocumentsCharges = Object.values(fichiersParSeance).reduce(
        (acc, files) => acc + (Array.isArray(files) ? files.length : 0),
        0
    );

    return {
      enseignements: normalizedEnseignements.length,
      seances: seances.length,
      fichiersCharges: totalDocumentsCharges,
    };
  }, [normalizedEnseignements, seances, fichiersParSeance]);

  useEffect(() => {
    chargerEnseignements();
  }, []);

  useEffect(() => {
    if (!selectedEnseignement) return;
    chargerSeancesPourEnseignement(selectedEnseignement, { preserveExpanded: false });
  }, [selectedEnseignementKey]);

  useEffect(() => {
    const interval = setInterval(async () => {
      if (
          drawerOpen ||
          savingSeance ||
          uploadingSeanceId ||
          deletingFileId ||
          deletingSeanceId
      ) {
        return;
      }

      await chargerEnseignements(true);

      if (selectedEnseignement) {
        await chargerSeancesPourEnseignement(selectedEnseignement, {
          silent: true,
          preserveExpanded: true,
        });
      }
    }, 30000);

    return () => clearInterval(interval);
  }, [
    selectedEnseignement,
    drawerOpen,
    savingSeance,
    uploadingSeanceId,
    deletingFileId,
    deletingSeanceId,
    expandedSeances,
  ]);

  const chargerEnseignements = async (silent = false) => {
    if (!silent) setLoadingEnseignements(true);

    try {
      const data = await apiSeancesProfesseur.listerEnseignements();
      const list = Array.isArray(data) ? data : [];
      setEnseignements(list);

      const normalized = list.map((item) => ({
        ...item,
        _key: item.id ?? `${item.codeClasse}__${item.codeMatiere}`,
      }));

      const stillExists = normalized.some(
          (e) => String(e._key) === String(selectedEnseignementKey)
      );

      if (!stillExists && normalized.length > 0) {
        setSelectedEnseignementKey(normalized[0]._key);
      }
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de charger les enseignements.',
      });
    } finally {
      if (!silent) setLoadingEnseignements(false);
    }
  };

  const chargerFichiers = async (seanceId, { silent = false } = {}) => {
    if (!silent) {
      setLoadingFichiersParSeance((prev) => ({ ...prev, [seanceId]: true }));
    }

    try {
      const data = await apiSeancesProfesseur.listerFichiersDeSeance(seanceId);
      setFichiersParSeance((prev) => ({
        ...prev,
        [seanceId]: Array.isArray(data) ? data : [],
      }));
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de charger les fichiers de la séance.',
      });
    } finally {
      if (!silent) {
        setLoadingFichiersParSeance((prev) => ({ ...prev, [seanceId]: false }));
      }
    }
  };

  const chargerSeancesPourEnseignement = async (
      enseignement,
      { silent = false, preserveExpanded = true } = {}
  ) => {
    if (!enseignement) return;

    if (!silent) setLoadingSeances(true);

    try {
      const payload = {
        codeClasse: enseignement.codeClasse,
        codeMatiere: enseignement.codeMatiere,
      };

      const data = await apiSeancesProfesseur.rechercherSeances(payload);
      const seancesData = sortSeances(Array.isArray(data) ? data : []);
      setSeances(seancesData);

      if (!preserveExpanded) {
        setExpandedSeances({});
        setFichiersParSeance({});
        setFichiersIndisponibles({});
      } else {
        const stillOpenedIds = Object.keys(expandedSeances).filter(
            (id) =>
                expandedSeances[id] &&
                seancesData.some((seance) => String(seance.id) === String(id))
        );

        await Promise.all(stillOpenedIds.map((id) => chargerFichiers(id)));
      }

      seancesData.forEach((seance) => {
        if (!fichiersParSeance[seance.id]) {
          chargerFichiers(seance.id, { silent: true }).catch(() => {});
        }
      });
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de charger les séances.',
      });
    } finally {
      if (!silent) setLoadingSeances(false);
    }
  };

  const toggleSeance = async (seanceId) => {
    setOpenActionMenuSeanceId(null);

    const isOpen = !!expandedSeances[seanceId];

    setExpandedSeances((prev) => ({
      ...prev,
      [seanceId]: !isOpen,
    }));

    if (!isOpen && !fichiersParSeance[seanceId]) {
      await chargerFichiers(seanceId);
    }
  };

  const handleUpload = async (seanceId, event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!isSupportedCourseFile(file)) {
      setMessage({
        type: 'error',
        text: SIMPLE_UPLOAD_ERROR_MESSAGE,
      });
      event.target.value = '';
      return;
    }

    setUploadingSeanceId(seanceId);
    setMessage({ type: '', text: '' });

    try {
      await apiSeancesProfesseur.deposerFichier(seanceId, file);
      await chargerFichiers(seanceId);

      setMessage({
        type: 'success',
        text: 'Fichier déposé avec succès.',
      });
    } catch (err) {
      setMessage({
        type: 'error',
        text: normalizeUploadErrorMessage(err.message),
      });
    } finally {
      setUploadingSeanceId(null);
      event.target.value = '';
    }
  };

  const handleDeleteFile = async (fichierId, seanceId) => {
    const confirmed = window.confirm('Supprimer ce fichier ?');
    if (!confirmed) return;

    setDeletingFileId(fichierId);
    setMessage({ type: '', text: '' });

    try {
      await apiSeancesProfesseur.supprimerFichier(fichierId);
      await chargerFichiers(seanceId);

      setMessage({
        type: 'success',
        text: 'Fichier supprimé avec succès.',
      });
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de supprimer le fichier.',
      });
    } finally {
      setDeletingFileId(null);
    }
  };

  const handleDownload = async (fichier) => {
    try {
      await apiSeancesProfesseur.telechargerFichier(fichier.id);
    } catch (err) {
      const msg = err.message || 'Impossible de télécharger le fichier.';

      if (msg.toLowerCase().includes('introuvable sur le disque')) {
        setFichiersIndisponibles((prev) => ({ ...prev, [fichier.id]: true }));
        setMessage({
          type: 'warning',
          text:
              "Ce fichier existe en base mais pas physiquement sur le serveur. Dépose un nouveau fichier ou supprime cette entrée.",
        });
      } else {
        setMessage({
          type: 'error',
          text: msg,
        });
      }
    }
  };

  const handleOpenFile = async (fichier) => {
    try {
      await openCourseFile(fichier);
    } catch (err) {
      const msg = err.message || 'Aucun fichier n est depose.';
      setMessage({
        type: 'warning',
        text: msg,
      });
    }
  };

  const openCreateDrawer = () => {
    const today = new Date().toISOString().slice(0, 10);
    setDrawerMode('create');
    setEditingSeanceId(null);
    setSeanceForm({
      ...emptySeanceForm,
      date: today,
    });
    setDrawerOpen(true);
    setMessage({ type: '', text: '' });
  };

  const openEditDrawer = async (seanceId) => {
    try {
      const details = await apiSeancesProfesseur.obtenirSeance(seanceId);

      setDrawerMode('edit');
      setEditingSeanceId(seanceId);
      setSeanceForm({
        type: details.type || 'COURS',
        date: toDateInput(details.date || details.heureDebut),
        heureDebut: toTimeInput(details.heureDebut),
        heureFin: toTimeInput(details.heureFin),
        etat: details.etat || 'PLANIFIEE',
        salle: details.salle || '',
        contenu: details.contenu || '',
      });
      setDrawerOpen(true);
      setMessage({ type: '', text: '' });
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de charger la séance.',
      });
    }
  };

  const closeDrawer = () => {
    if (savingSeance) return;
    setDrawerOpen(false);
  };

  const handleSaveSeance = async () => {
    if (!selectedEnseignement) {
      setMessage({ type: 'error', text: 'Aucun enseignement sélectionné.' });
      return;
    }

    if (!seanceForm.date || !seanceForm.heureDebut || !seanceForm.heureFin) {
      setMessage({
        type: 'error',
        text: 'La date, l’heure de début et l’heure de fin sont obligatoires.',
      });
      return;
    }

    const debutMinutes = parseTimeToMinutes(seanceForm.heureDebut);
    const finMinutes = parseTimeToMinutes(seanceForm.heureFin);

    if (!Number.isFinite(debutMinutes) || !Number.isFinite(finMinutes)) {
      setMessage({
        type: 'error',
        text: "Format d'heure invalide.",
      });
      return;
    }

    if (debutMinutes >= finMinutes) {
      setMessage({
        type: 'error',
        text: "L'heure de début doit être strictement avant l'heure de fin.",
      });
      return;
    }

    if (!seanceForm.salle.trim()) {
      setMessage({
        type: 'error',
        text: 'La salle est obligatoire.',
      });
      return;
    }

    const payloadCreate = {
      codeClasse: selectedEnseignement.codeClasse,
      codeMatiere: selectedEnseignement.codeMatiere,
      anneeUniversitaire:
          selectedEnseignement.anneeUniversitaire ||
          selectedEnseignement.annee ||
          '2025-2026',
      type: seanceForm.type,
      date: seanceForm.date,
      heureDebut: buildDateTime(seanceForm.date, seanceForm.heureDebut),
      heureFin: buildDateTime(seanceForm.date, seanceForm.heureFin),
      etat: seanceForm.etat,
      salle: seanceForm.salle.trim(),
      contenu: seanceForm.contenu.trim(),
    };

    const payloadUpdate = {
      type: seanceForm.type,
      date: seanceForm.date,
      heureDebut: buildDateTime(seanceForm.date, seanceForm.heureDebut),
      heureFin: buildDateTime(seanceForm.date, seanceForm.heureFin),
      etat: seanceForm.etat,
      salle: seanceForm.salle.trim(),
      contenu: seanceForm.contenu.trim(),
    };

    setSavingSeance(true);
    setMessage({ type: '', text: '' });

    try {
      if (drawerMode === 'create') {
        await apiSeancesProfesseur.creerSeance(payloadCreate);
        setMessage({
          type: 'success',
          text: 'Séance créée avec succès.',
        });
      } else {
        await apiSeancesProfesseur.modifierSeance(editingSeanceId, payloadUpdate);
        setMessage({
          type: 'success',
          text: 'Séance modifiée avec succès.',
        });
      }

      setDrawerOpen(false);
      await chargerSeancesPourEnseignement(selectedEnseignement, {
        silent: true,
        preserveExpanded: true,
      });
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible d’enregistrer la séance.',
      });
    } finally {
      setSavingSeance(false);
    }
  };

  const handleDeleteSeance = async (seanceId) => {
    const confirmed = window.confirm(
        'Supprimer cette séance ? Les fichiers liés doivent aussi être gérés.'
    );
    if (!confirmed) return;

    setDeletingSeanceId(seanceId);
    setMessage({ type: '', text: '' });

    try {
      await apiSeancesProfesseur.supprimerSeance(seanceId);
      setExpandedSeances((prev) => {
        const clone = { ...prev };
        delete clone[seanceId];
        return clone;
      });
      setFichiersParSeance((prev) => {
        const clone = { ...prev };
        delete clone[seanceId];
        return clone;
      });

      await chargerSeancesPourEnseignement(selectedEnseignement, {
        silent: true,
        preserveExpanded: true,
      });

      setMessage({
        type: 'success',
        text: 'Séance supprimée avec succès.',
      });
    } catch (err) {
      setMessage({
        type: 'error',
        text: err.message || 'Impossible de supprimer la séance.',
      });
    } finally {
      setDeletingSeanceId(null);
    }
  };

  return (
      <div className="gc-page-v2">
        <div className="gc-hero-v2">
          <div className="gc-hero-left">
            <div className="gc-hero-icon">
              <BookOpen size={28} />
            </div>

            <div>
              <h1>Gestion des cours</h1>
              <p>
                Accédez à vos enseignements, ouvrez une séance, puis gérez les
                supports comme dans une bibliothèque de dossiers.
              </p>

              <div className="gc-hero-stats">
                <span>{stats.enseignements} enseignement(s)</span>
                <span>{stats.seances} séance(s)</span>
                <span>{stats.fichiersCharges} fichier(s) chargé(s)</span>
              </div>
            </div>
          </div>
        </div>

        {message.text && (
            <div className={`gc-message gc-message-${message.type || 'info'}`}>
              <div className="gc-message-left">
                {message.type === 'success' ? (
                    <CheckCircle2 size={18} />
                ) : message.type === 'warning' ? (
                    <Info size={18} />
                ) : (
                    <AlertTriangle size={18} />
                )}
                <span>{message.text}</span>
              </div>

              <button type="button" onClick={() => setMessage({ type: '', text: '' })}>
                ✕
              </button>
            </div>
        )}

        <div className="gc-layout-v2">
          <aside className="gc-sidebar-v2">
            <div className="gc-sidebar-header">
              <div>
                <span className="gc-overline">Enseignements</span>
                <h2>Mes classes & matières</h2>
              </div>

              <div className="gc-count-pill">{normalizedEnseignements.length}</div>
            </div>

            <div className="gc-search-box">
              <Search size={16} />
              <input
                  type="text"
                  placeholder="Rechercher un enseignement..."
                  value={searchTeaching}
                  onChange={(e) => setSearchTeaching(e.target.value)}
              />
            </div>

            <div className="gc-enseignements-scroll">
              {loadingEnseignements ? (
                  <div className="gc-empty-box">Chargement des enseignements...</div>
              ) : filteredEnseignements.length === 0 ? (
                  <div className="gc-empty-box">Aucun enseignement trouvé.</div>
              ) : (
                  filteredEnseignements.map((enseignement) => {
                    const active = String(enseignement._key) === String(selectedEnseignementKey);

                    return (
                        <button
                            key={enseignement._key}
                            className={`gc-enseignement-item ${active ? 'active' : ''}`}
                            type="button"
                            onClick={() => setSelectedEnseignementKey(enseignement._key)}
                        >
                          <div className="gc-enseignement-top">
                            <div className="gc-enseignement-icon">
                              <GraduationCap size={16} />
                            </div>
                            <ChevronRight size={16} />
                          </div>

                          <h3>{enseignement.matiereNom}</h3>
                          <p>{enseignement.codeMatiere}</p>

                          <div className="gc-enseignement-meta">
                            <span>{enseignement.classeNom || enseignement.codeClasse}</span>
                          </div>
                        </button>
                    );
                  })
              )}
            </div>
          </aside>

          <main className="gc-main-v2">
            {selectedEnseignement ? (
                <>
                  <div className="gc-main-header">
                    <div>
                      <span className="gc-overline">Enseignement sélectionné</span>
                      <h2>{selectedEnseignement.matiereNom}</h2>
                      <p>
                        {selectedEnseignement.classeNom} — {selectedEnseignement.codeMatiere}
                      </p>
                    </div>

                    <div className="gc-main-badges">
                  <span className="gc-badge">
                    <Layers3 size={14} />
                    {seances.length} séance(s)
                  </span>
                      <span className="gc-badge">
                    <GraduationCap size={14} />
                        {selectedEnseignement.codeClasse}
                  </span>
                      <button
                          className="gc-create-seance-btn"
                          type="button"
                          onClick={openCreateDrawer}
                      >
                        <Plus size={16} />
                        Nouvelle séance
                      </button>
                    </div>
                  </div>

                  {loadingSeances ? (
                      <div className="gc-empty-state-large">Chargement des séances...</div>
                  ) : seances.length === 0 ? (
                      <div className="gc-empty-state-large">
                        Aucune séance trouvée pour cet enseignement.
                      </div>
                  ) : (
                      <div className="gc-seances-list">
                        {seances.map((seance) => {
                          const fichiers = fichiersParSeance[seance.id] || [];
                          const isOpen = !!expandedSeances[seance.id];
                          const loadingFiles = !!loadingFichiersParSeance[seance.id];

                          return (
                              <div
                                  key={seance.id}
                                  className={`gc-seance-card-v2 ${
                                      openActionMenuSeanceId === seance.id ? 'menu-open' : ''
                                  }`}
                              >
                                <div className="gc-seance-header-v2">
                                  <div className="gc-seance-left">
                                    <button
                                        className="gc-collapse-btn"
                                        type="button"
                                        onClick={() => toggleSeance(seance.id)}
                                    >
                                      {isOpen ? (
                                          <ChevronDown size={18}/>
                                      ) : (
                                          <ChevronRight size={18}/>
                                      )}
                                    </button>

                                    <div className="gc-seance-content">
                                      <div className="gc-seance-title-row">
                                <span className={`gc-type-pill ${getTypeClass(seance.type)}`}>
                                  {seance.type}
                                </span>
                                        <h3>{seance.salle || 'Séance'}</h3>
                                      </div>

                                      <div className="gc-seance-meta-row">
                                <span>
                                  <CalendarDays size={14}/>
                                  {formatDate(seance.date)}
                                </span>
                                        <span>
                                  <Clock3 size={14}/>
                                          {formatTime(seance.heureDebut)} — {formatTime(seance.heureFin)}
                                </span>
                                        <span>
                                  <MapPin size={14}/>
                                          {seance.salle || '--'}
                                </span>
                                      </div>
                                    </div>
                                  </div>

                                  <div className="gc-seance-actions modern-actions">
                                    <label className="gc-primary-upload-btn">
                                      <Upload size={16}/>
                                      {uploadingSeanceId === seance.id ? 'Envoi...' : 'Ajouter support'}
                                      <input
                                          type="file"
                                          accept=".pdf,.docx,application/pdf,application/vnd.openxmlformats-officedocument.wordprocessingml.document"
                                          hidden
                                          onChange={(e) => handleUpload(seance.id, e)}
                                      />
                                    </label>

                                    <button
                                        className="gc-files-pill"
                                        type="button"
                                        onClick={() => toggleSeance(seance.id)}
                                    >
                                      <FolderOpen size={15}/>
                                      {(fichiersParSeance[seance.id]?.length ?? 0)} fichier(s)
                                      {isOpen ? (
                                          <ChevronDown size={15}/>
                                      ) : (
                                          <ChevronRight size={15}/>
                                      )}
                                    </button>

                                    <div className="gc-seance-menu-wrap">
                                      <button
                                          className="gc-more-btn"
                                          type="button"
                                          onClick={() =>
                                              setOpenActionMenuSeanceId((prev) =>
                                                  prev === seance.id ? null : seance.id
                                              )
                                          }
                                          title="Actions de la séance"
                                      >
                                        <MoreHorizontal size={18}/>
                                      </button>

                                      {openActionMenuSeanceId === seance.id && (
                                          <div className="gc-seance-menu">
                                            <button
                                                type="button"
                                                className="gc-seance-menu-item"
                                                onClick={() => {
                                                  setOpenActionMenuSeanceId(null);
                                                  openEditDrawer(seance.id);
                                                }}
                                            >
                                              <Pencil size={15}/>
                                              Modifier
                                            </button>

                                            <button
                                                type="button"
                                                className="gc-seance-menu-item danger"
                                                disabled={deletingSeanceId === seance.id}
                                                onClick={() => {
                                                  setOpenActionMenuSeanceId(null);
                                                  handleDeleteSeance(seance.id);
                                                }}
                                            >
                                              {deletingSeanceId === seance.id ? (
                                                  <LoaderCircle size={15} className="spin"/>
                                              ) : (
                                                  <Trash2 size={15}/>
                                              )}
                                              Supprimer
                                            </button>
                                          </div>
                                      )}
                                    </div>
                                  </div>
                                </div>

                                {isOpen && (
                                    <div className="gc-files-panel-v2">
                                      {loadingFiles ? (
                                          <div className="gc-files-empty">Chargement des fichiers...</div>
                                      ) : fichiers.length === 0 ? (
                                          <div className="gc-files-empty">
                                            <FolderOpen size={18}/>
                                            Aucun fichier pour cette séance.
                                          </div>
                                      ) : (
                                          <div className="gc-files-grid">
                                            {fichiers.map((fichier) => {
                                              const Icon = getFileIcon(fichier.typeMime);
                                              const indisponible = !!fichiersIndisponibles[fichier.id];

                                              return (
                                                  <div
                                                      key={fichier.id}
                                                      className={`gc-file-card ${
                                                          indisponible ? 'is-unavailable' : ''
                                                      }`}
                                                  >
                                                    <div className="gc-file-left">
                                                      <div className="gc-file-icon">
                                                        <Icon size={18}/>
                                                      </div>

                                                      <div className="gc-file-info">
                                                        <button
                                                            type="button"
                                                            className="gc-file-name-link"
                                                            onClick={() => handleOpenFile(fichier)}
                                                        >
                                                          {fichier.nomOriginal}
                                                        </button>
                                                        <span>
                                            {formatBytes(fichier.taille)} •{' '}
                                                          {formatDate(fichier.dateUpload)}
                                          </span>

                                                        {indisponible && (
                                                            <small className="gc-file-warning">
                                                              Fichier indisponible sur le serveur
                                                            </small>
                                                        )}
                                                      </div>
                                                    </div>

                                                    <div className="gc-file-actions">
                                                      {!indisponible && (
                                                          <button
                                                              type="button"
                                                              className="gc-icon-btn"
                                                              onClick={() => handleDownload(fichier)}
                                                              title="Télécharger"
                                                          >
                                                            <Download size={16}/>
                                                          </button>
                                                      )}

                                                      <button
                                                          type="button"
                                                          className="gc-icon-btn danger"
                                                          disabled={deletingFileId === fichier.id}
                                                          onClick={() =>
                                                              handleDeleteFile(fichier.id, seance.id)
                                                          }
                                                          title="Supprimer"
                                                      >
                                                        <Trash2 size={16}/>
                                                      </button>
                                                    </div>
                                                  </div>
                                              );
                                            })}
                                          </div>
                                      )}
                                    </div>
                                )}
                              </div>
                          );
                        })}
                      </div>
                  )}
                </>
            ) : (
                <div className="gc-empty-state-large">
                  Sélectionnez un enseignement pour accéder à ses séances.
                </div>
            )}
          </main>
        </div>

        {drawerOpen && (
            <div className="gc-drawer-overlay" onClick={closeDrawer}>
              <div className="gc-drawer" onClick={(e) => e.stopPropagation()}>
                <div className="gc-drawer-header">
                  <div>
                    <h3>{drawerMode === 'create' ? 'Nouvelle séance' : 'Modifier la séance'}</h3>
                    <p>
                      {drawerMode === 'create'
                          ? 'Créez une séance pour cet enseignement.'
                          : 'Mettez à jour les informations de la séance.'}
                    </p>
                  </div>

                  <button className="gc-drawer-close" type="button" onClick={closeDrawer}>
                    <X size={18} />
                  </button>
                </div>

                <div className="gc-drawer-context">
                  <span>{selectedEnseignement?.classeNom || selectedEnseignement?.codeClasse}</span>
                  <span>{selectedEnseignement?.matiereNom}</span>
                </div>

                <div className="gc-drawer-grid">
                  <div className="gc-drawer-field">
                    <label>Type</label>
                    <select
                        value={seanceForm.type}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, type: e.target.value }))
                        }
                    >
                      {TYPE_OPTIONS.map((type) => (
                          <option key={type} value={type}>
                            {type}
                          </option>
                      ))}
                    </select>
                  </div>

                  <div className="gc-drawer-field">
                    <label>État</label>
                    <select
                        value={seanceForm.etat}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, etat: e.target.value }))
                        }
                    >
                      {ETAT_OPTIONS.map((etat) => (
                          <option key={etat} value={etat}>
                            {etat}
                          </option>
                      ))}
                    </select>
                  </div>

                  <div className="gc-drawer-field">
                    <label>Date</label>
                    <input
                        type="date"
                        value={seanceForm.date}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, date: e.target.value }))
                        }
                    />
                  </div>

                  <div className="gc-drawer-field">
                    <label>Salle</label>
                    <input
                        type="text"
                        value={seanceForm.salle}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, salle: e.target.value }))
                        }
                        placeholder="Ex: Salle 3-4"
                    />
                  </div>

                  <div className="gc-drawer-field">
                    <label>Heure début</label>
                    <input
                        type="time"
                        value={seanceForm.heureDebut}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, heureDebut: e.target.value }))
                        }
                    />
                  </div>

                  <div className="gc-drawer-field">
                    <label>Heure fin</label>
                    <input
                        type="time"
                        value={seanceForm.heureFin}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, heureFin: e.target.value }))
                        }
                    />
                  </div>

                  <div className="gc-drawer-field full">
                    <label>Contenu</label>
                    <textarea
                        value={seanceForm.contenu}
                        onChange={(e) =>
                            setSeanceForm((prev) => ({ ...prev, contenu: e.target.value }))
                        }
                        placeholder="Description du contenu de la séance..."
                    />
                  </div>
                </div>

                <div className="gc-drawer-actions">
                  <button className="gc-drawer-cancel" type="button" onClick={closeDrawer}>
                    Annuler
                  </button>

                  <button
                      className="gc-drawer-save"
                      type="button"
                      onClick={handleSaveSeance}
                      disabled={savingSeance}
                  >
                    {savingSeance ? (
                        <>
                          <LoaderCircle size={16} className="spin" />
                          Enregistrement...
                        </>
                    ) : (
                        <>
                          <Save size={16} />
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

export default GestionCoursProfesseur;
