import { useEffect, useMemo, useRef, useState } from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  FileText,
  GraduationCap,
  Info,
  Layers,
  Megaphone,
  Paperclip,
  Plus,
  RefreshCw,
  Search,
  Send,
  Trash2,
  Download,
  Upload,
  User,
  Users,
  X,
} from 'lucide-react';
import apiAnnoncesProfesseur from '../services/professeur/apiAnnoncesProfesseur';
import './GestionAnnoncesAdmin.css';
import './GestionAnnoncesProfesseur.css';
import './AnnoncesEtudiant.css';

const TYPES_ANNONCE = [
  { value: 'AUTRES', label: 'Autres' },
  { value: 'COURS', label: 'Cours' },
  { value: 'EVENT', label: 'Evenement' },
  { value: 'REUNION', label: 'Reunion' },
  { value: 'EXAMEN', label: 'Examen' },
];

const FILTRES_TYPE_ANNONCE = ['TOUS', 'EXAMEN', 'COURS', 'REUNION', 'EVENT', 'AUTRES'];

const FORM_INITIAL = {
  titre: '',
  categorie: 'AUTRES',
  contenu: '',
  classesSelectionnees: [],
};

const MAX_ANNONCE_RESOURCE_SIZE = 50 * 1024 * 1024;

const formaterDate = (dateStr) => {
  if (!dateStr) return 'Date inconnue';

  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return 'Date inconnue';

  const now = new Date();
  const diffMs = now - date;
  const diffMinutes = Math.floor(diffMs / 60000);
  const diffHours = Math.floor(diffMinutes / 60);
  const diffDays = Math.floor(diffHours / 24);

  if (diffMinutes < 1) return "A l'instant";
  if (diffMinutes < 60) return `Il y a ${diffMinutes} minute${diffMinutes > 1 ? 's' : ''}`;
  if (diffHours < 24) return `Il y a ${diffHours} heure${diffHours > 1 ? 's' : ''}`;
  if (diffDays < 7) return `Il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;

  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
};

const formatDateSidebar = (dateStr) => {
  if (!dateStr) return 'DATE INCONNUE';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return 'DATE INCONNUE';

  return date
    .toLocaleDateString('fr-FR', {
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
    .toUpperCase();
};

const formatDateComplete = (dateStr) => {
  if (!dateStr) return 'Date inconnue';
  const date = new Date(dateStr);
  if (Number.isNaN(date.getTime())) return 'Date inconnue';

  return date.toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const construireMessageTailleRessources = (fichiersInvalides) => {
  if (!fichiersInvalides.length) {
    return '';
  }

  const noms = fichiersInvalides.map((fichier) => fichier.name).join(', ');
  return fichiersInvalides.length === 1
    ? `Le fichier "${noms}" depasse la taille autorisee. Chaque ressource doit faire 50 Mo maximum.`
    : `Ces fichiers depassent la taille autorisee: ${noms}. Chaque ressource doit faire 50 Mo maximum.`;
};

const construireMessageErreurRessources = (messageErreur, nombreClasses) =>
  nombreClasses > 1
    ? `Annonces non publiees a cause des fichiers. ${messageErreur || 'Le depot des ressources a echoue pour au moins une classe.'}`
    : `Annonce non publiee a cause des fichiers. ${messageErreur || 'Le depot des ressources a echoue.'}`;

const getTypeCategorie = (typeAnnonce, cibleType) => {
  if (typeAnnonce && TYPES_ANNONCE.some((type) => type.value === typeAnnonce)) {
    return typeAnnonce;
  }

  if (cibleType === 'PROFESSEURS') {
    return 'COURS';
  }

  return 'AUTRES';
};

const transformAnnonceAdmin = (annonce) => {
  const publishedAt = annonce?.datePublication || annonce?.dateCreation || null;
  const publishedTimestamp = publishedAt ? new Date(publishedAt).getTime() : 0;
  const auteur = annonce?.auteur;
  const auteurLabel = auteur?.prenom || auteur?.nom
    ? `${auteur?.prenom || ''} ${auteur?.nom || ''}`.trim()
    : 'Administration';

  return {
    id: annonce?.id,
    title: annonce?.titre || 'Sans titre',
    date: formatDateSidebar(publishedAt),
    fullDate: formatDateComplete(publishedAt),
    publishedTimestamp: Number.isNaN(publishedTimestamp) ? 0 : publishedTimestamp,
    category: getTypeCategorie(annonce?.typeAnnonce, annonce?.cibleType),
    subject: annonce?.codeClasse || 'Annonce administration',
    professor: auteurLabel,
    authorPhoto: auteur?.photoPath || auteur?.photoProfile || null,
    content: annonce?.contenu || '',
  };
};

const toUpper = (value) => String(value || '').trim().toUpperCase();

const estAnnonceAdminPourProf = (annonce) => {
  const cibleType = toUpper(annonce?.cibleType);
  const auteurRole = toUpper(
    annonce?.auteur?.role || annonce?.auteurRole || annonce?.roleAuteur
  );
  const ciblesAutoriseesProf = new Set([
    'PROFESSEURS_DEPARTEMENT',
    'PROFESSEURS_GLOBAL',
  ]);

  if (ciblesAutoriseesProf.has(cibleType)) {
    return true;
  }

  if (auteurRole.includes('ADMIN')) {
    return true;
  }

  return false;
};

const GestionAnnoncesProfesseur = ({
  annonceSelectionneeId = null,
  vueAnnonceInitiale = null,
}) => {
  const [chargement, setChargement] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [vueActive, setVueActive] = useState(
    vueAnnonceInitiale === 'admin' ? 'admin' : 'etudiants'
  );
  const [mesAnnonces, setMesAnnonces] = useState([]);
  const [annoncesAdmin, setAnnoncesAdmin] = useState([]);
  const [annonceAdminActive, setAnnonceAdminActive] = useState(null);
  const [adminSearchQuery, setAdminSearchQuery] = useState('');
  const [adminActiveFilter, setAdminActiveFilter] = useState('TOUS');
  const [ressourcesParAnnonce, setRessourcesParAnnonce] = useState({});
  const [adminRessourceLoading, setAdminRessourceLoading] = useState(false);
  const [mesClasses, setMesClasses] = useState([]);
  const [formulaire, setFormulaire] = useState(FORM_INITIAL);
  const [fichiersSelectionnes, setFichiersSelectionnes] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const fileInputRef = useRef(null);

  useEffect(() => {
    chargerDonnees();

    const interval = setInterval(() => {
      chargerDonnees(false);
    }, 30000);

    return () => clearInterval(interval);
  }, []);

  const chargerDonnees = async (showLoader = true) => {
    if (showLoader) setChargement(true);
    setError('');

    try {
      const [annonces, annoncesProfesseurs, classes] = await Promise.all([
        apiAnnoncesProfesseur.listerMesAnnonces(),
        apiAnnoncesProfesseur.listerMesProfAnnonces(),
        apiAnnoncesProfesseur.listerMesClasses(),
      ]);

      const annoncesChargees = Array.isArray(annonces) ? annonces : [];
      const annoncesAdminsUniquement = (Array.isArray(annoncesProfesseurs) ? annoncesProfesseurs : [])
        .filter(estAnnonceAdminPourProf);
      setMesAnnonces(annoncesChargees);
      setAnnoncesAdmin(annoncesAdminsUniquement);
      setMesClasses(Array.isArray(classes) ? classes : []);

      const ressourcesEntries = await Promise.all(
        annoncesChargees.map(async (annonce) => {
          try {
            const ressources = await apiAnnoncesProfesseur.listerRessourcesAnnonce(annonce.id);
            return [annonce.id, Array.isArray(ressources) ? ressources : []];
          } catch {
            return [annonce.id, []];
          }
        })
      );

      setRessourcesParAnnonce(Object.fromEntries(ressourcesEntries));
    } catch (err) {
      setError(err.message || 'Impossible de charger les annonces professeur.');
      setMesAnnonces([]);
      setAnnoncesAdmin([]);
      setMesClasses([]);
      setRessourcesParAnnonce({});
    } finally {
      if (showLoader) setChargement(false);
    }
  };

  const classesCibles = useMemo(() => {
    return Array.isArray(formulaire.classesSelectionnees)
      ? formulaire.classesSelectionnees.filter(Boolean)
      : [];
  }, [formulaire.classesSelectionnees]);

  const annoncesAdminTriees = useMemo(
    () =>
      (Array.isArray(annoncesAdmin) ? annoncesAdmin : [])
        .map(transformAnnonceAdmin)
        .sort((a, b) => b.publishedTimestamp - a.publishedTimestamp),
    [annoncesAdmin]
  );

  const annoncesAdminFiltrees = useMemo(
    () =>
      annoncesAdminTriees.filter((annonce) => {
        const matchesSearch = annonce.title
          .toLowerCase()
          .includes(adminSearchQuery.toLowerCase());
        const matchesFilter =
          adminActiveFilter === 'TOUS' || annonce.category === adminActiveFilter;

        return matchesSearch && matchesFilter;
      }),
    [annoncesAdminTriees, adminSearchQuery, adminActiveFilter]
  );

  const ressourcesAnnonceAdminActives = annonceAdminActive?.id
    ? ressourcesParAnnonce[annonceAdminActive.id] || []
    : [];

  useEffect(() => {
    if (vueAnnonceInitiale === 'admin') {
      setVueActive('admin');
    }
  }, [vueAnnonceInitiale]);

  useEffect(() => {
    if (!annoncesAdminTriees.length) {
      setAnnonceAdminActive(null);
      return;
    }

    setAnnonceAdminActive((precedente) => {
      if (annonceSelectionneeId != null) {
        const annonceCible = annoncesAdminTriees.find(
          (item) => String(item.id) === String(annonceSelectionneeId)
        );
        if (annonceCible) {
          return annonceCible;
        }
      }

      if (precedente) {
        return (
          annoncesAdminTriees.find((item) => item.id === precedente.id) ||
          annoncesAdminTriees[0]
        );
      }

      return annoncesAdminTriees[0];
    });
  }, [annoncesAdminTriees, annonceSelectionneeId]);

  useEffect(() => {
    if (annonceSelectionneeId == null) {
      return;
    }

    const annonceCible = annoncesAdminTriees.find(
      (item) => String(item.id) === String(annonceSelectionneeId)
    );

    if (annonceCible) {
      setVueActive('admin');
      setAnnonceAdminActive(annonceCible);
    }
  }, [annonceSelectionneeId, annoncesAdminTriees]);

  useEffect(() => {
    if (!annonceAdminActive?.id || ressourcesParAnnonce[annonceAdminActive.id]) {
      return;
    }

    const chargerRessourcesAnnonceAdmin = async () => {
      try {
        setAdminRessourceLoading(true);
        const ressources = await apiAnnoncesProfesseur.listerRessourcesAnnonce(
          annonceAdminActive.id
        );
        setRessourcesParAnnonce((prev) => ({
          ...prev,
          [annonceAdminActive.id]: Array.isArray(ressources) ? ressources : [],
        }));
      } catch {
        setRessourcesParAnnonce((prev) => ({
          ...prev,
          [annonceAdminActive.id]: [],
        }));
      } finally {
        setAdminRessourceLoading(false);
      }
    };

    chargerRessourcesAnnonceAdmin();
  }, [annonceAdminActive, ressourcesParAnnonce]);

  const isFormValid = Boolean(
    formulaire.titre.trim() &&
      formulaire.contenu.trim() &&
      classesCibles.length > 0
  );

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire((prev) => ({ ...prev, [name]: value }));
  };

  const gererSelectionClasse = (codeClasse) => {
    setFormulaire((prev) => ({
      ...prev,
      classesSelectionnees: prev.classesSelectionnees.includes(codeClasse)
        ? prev.classesSelectionnees.filter((code) => code !== codeClasse)
        : [...prev.classesSelectionnees, codeClasse],
    }));
  };

  const gererSelectionToutesClasses = () => {
    const toutesLesClasses = mesClasses.map((classe) => classe.codeClasse).filter(Boolean);

    setFormulaire((prev) => ({
      ...prev,
      classesSelectionnees:
        prev.classesSelectionnees.length === toutesLesClasses.length
          ? []
          : toutesLesClasses,
    }));
  };

  const gererSelectionFichiers = (event) => {
    const nouveauxFichiers = Array.from(event.target.files || []);
    if (!nouveauxFichiers.length) {
      return;
    }

    const fichiersInvalides = nouveauxFichiers.filter(
      (fichier) => Number(fichier?.size || 0) > MAX_ANNONCE_RESOURCE_SIZE
    );
    const fichiersValides = nouveauxFichiers.filter(
      (fichier) => Number(fichier?.size || 0) <= MAX_ANNONCE_RESOURCE_SIZE
    );

    if (fichiersInvalides.length) {
      setError(construireMessageTailleRessources(fichiersInvalides));
      setSuccess('');
    } else {
      setError('');
    }

    if (!fichiersValides.length) {
      event.target.value = '';
      return;
    }

    setFichiersSelectionnes((prev) => {
      const existants = new Map(
        prev.map((fichier) => [
          `${fichier.name}-${fichier.size}-${fichier.lastModified}`,
          fichier,
        ])
      );

      fichiersValides.forEach((fichier) => {
        existants.set(
          `${fichier.name}-${fichier.size}-${fichier.lastModified}`,
          fichier
        );
      });

      return Array.from(existants.values());
    });

    event.target.value = '';
  };

  const viderFichiers = () => {
    setFichiersSelectionnes([]);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const retirerFichier = (indexFichier) => {
    setFichiersSelectionnes((prev) => prev.filter((_, index) => index !== indexFichier));
  };

  const reinitialiserFormulaire = () => {
    setFormulaire(FORM_INITIAL);
    viderFichiers();
  };

  const televerserRessources = async (annonceId) => {
    if (!fichiersSelectionnes.length) {
      return 0;
    }

    const ressourcesCreees = await apiAnnoncesProfesseur.ajouterRessourcesAnnonce(
      annonceId,
      fichiersSelectionnes
    );

    return Array.isArray(ressourcesCreees) ? ressourcesCreees.length : 0;
  };

  const creerAnnonce = async () => {
    setError('');
    setSuccess('');

    if (!formulaire.titre.trim()) {
      setError("Le titre de l'annonce est obligatoire.");
      return;
    }

    if (!formulaire.contenu.trim()) {
      setError("Le contenu de l'annonce est obligatoire.");
      return;
    }

    if (classesCibles.length === 0) {
      setError('Vous devez selectionner une classe.');
      return;
    }

    try {
      setSubmitting(true);

      const annoncesCreees = [];
      const nbClasses = classesCibles.length;
      const nbFichiers = fichiersSelectionnes.length;

      for (const codeClasse of classesCibles) {
        const annonceCreee = await apiAnnoncesProfesseur.creerAnnonce({
          titre: formulaire.titre.trim(),
          contenu: formulaire.contenu.trim(),
          typeAnnonce: formulaire.categorie,
          cibleType: 'ETUDIANTS_CLASSE',
          codeClasse,
          codeDepartement: null,
        });

        annoncesCreees.push(annonceCreee);
      }

      try {
        await Promise.all(annoncesCreees.map((annonce) => televerserRessources(annonce.id)));
      } catch (uploadError) {
        await Promise.allSettled(
          annoncesCreees.map((annonce) => apiAnnoncesProfesseur.supprimerAnnonce(annonce.id))
        );

        await chargerDonnees(false);
        setSuccess('');
        setError(construireMessageErreurRessources(uploadError.message, annoncesCreees.length));
        return;
      }

      if (nbClasses > 1) {
        setSuccess(
          nbFichiers > 0
            ? `Annonces creees pour ${nbClasses} classes avec ${nbFichiers} ressource(s) par annonce.`
            : `Annonces creees pour ${nbClasses} classes.`
        );
      } else {
        setSuccess(
          nbFichiers > 0
            ? `Annonce creee avec succes avec ${nbFichiers} ressource(s).`
            : 'Annonce creee avec succes.'
        );
      }

      reinitialiserFormulaire();
      await chargerDonnees(false);
    } catch (err) {
      setError(err.message || "Erreur lors de la creation de l'annonce.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleSupprimer = async (id) => {
    const confirmation = window.confirm('Voulez-vous vraiment supprimer cette annonce ?');
    if (!confirmation) return;

    setError('');
    setSuccess('');

    try {
      await apiAnnoncesProfesseur.supprimerAnnonce(id);
      setMesAnnonces((prev) => prev.filter((annonce) => annonce.id !== id));
      setSuccess('Annonce supprimee avec succes.');
    } catch (err) {
      setError(err.message || 'Erreur lors de la suppression.');
    }
  };

  const handleTogglePublication = async (annonce) => {
    setError('');
    setSuccess('');

    try {
      if (annonce.publiee) {
        await apiAnnoncesProfesseur.depublierAnnonce(annonce.id);
        setSuccess('Annonce depubliee avec succes.');
      } else {
        await apiAnnoncesProfesseur.publierAnnonce(annonce.id);
        setSuccess('Annonce publiee avec succes.');
      }

      await chargerDonnees(false);
    } catch (err) {
      setError(err.message || 'Erreur lors du changement de publication.');
    }
  };

  const formaterTaille = (taille) => {
    if (!taille) return '0 o';
    const unites = ['o', 'Ko', 'Mo', 'Go'];
    let valeur = Number(taille);
    let index = 0;

    while (valeur >= 1024 && index < unites.length - 1) {
      valeur /= 1024;
      index += 1;
    }

    return `${valeur.toFixed(index === 0 ? 0 : 1)} ${unites[index]}`;
  };

  return (
    <div className="communication-center professeur-communication-center">
      <div className="communication-header">
        <div className="header-content">
          <div className="header-icon">
            <Megaphone size={32} />
          </div>
          <div className="header-text">
            <h1>Centre de Communication</h1>
            <p>GERER LES ANNONCES ET CONSULTER LES DIFFUSIONS ADMIN</p>
          </div>
        </div>

        <div className="target-buttons prof-header-switches">
          <button
            className={`target-btn ${vueActive === 'etudiants' ? 'active' : ''}`}
            type="button"
            onClick={() => setVueActive('etudiants')}
          >
            <GraduationCap size={20} />
            ENVOYER AUX ETUDIANTS
          </button>
          <button
            className={`target-btn ${vueActive === 'admin' ? 'active' : ''}`}
            type="button"
            onClick={() => setVueActive('admin')}
          >
            <Users size={20} />
            ANNONCES ADMIN
          </button>
        </div>
      </div>

      {error && (
        <div className="prof-alert prof-alert-error">
          <div className="prof-alert-left">
            <AlertTriangle size={18} />
            <span>{error}</span>
          </div>
          <button type="button" onClick={() => setError('')}>
            x
          </button>
        </div>
      )}

      {success && (
        <div className="prof-alert prof-alert-success">
          <div className="prof-alert-left">
            <CheckCircle2 size={18} />
            <span>{success}</span>
          </div>
          <button type="button" onClick={() => setSuccess('')}>
            x
          </button>
        </div>
      )}

      {vueActive === 'etudiants' ? (
        <>
          <div className="communication-content">
            <div className="compose-section">
              <div className="section-title">
                <Plus className="plus-icon" size={20} />
                CREER L'ANNONCE
              </div>

              <div className="compose-form">
                <div className="form-row">
                  <div className="form-group">
                    <label>TITRE DE L'ANNONCE</label>
                    <input
                      type="text"
                      name="titre"
                      value={formulaire.titre}
                      onChange={gererChangement}
                      placeholder="Ex: Report du controle continu..."
                      className="form-input"
                      maxLength={200}
                    />
                  </div>

                  <div className="form-group">
                    <label>CATEGORIE</label>
                    <select
                      name="categorie"
                      value={formulaire.categorie}
                      onChange={gererChangement}
                      className="form-input"
                    >
                      {TYPES_ANNONCE.map((type) => (
                        <option key={type.value} value={type.value}>
                          {type.label}
                        </option>
                      ))}
                    </select>
                  </div>
                </div>

                <div className="form-group">
                  <label>CONTENU DETAILLE</label>
                  <textarea
                    name="contenu"
                    value={formulaire.contenu}
                    onChange={gererChangement}
                    placeholder="Saisissez le corps de votre message ici..."
                    className="form-textarea"
                    maxLength={2000}
                  />
                </div>

                <div className="prof-selected-class-box">
                  <div className="prof-selected-class-label">CIBLAGE</div>
                  <strong>
                    {classesCibles.length === 0
                      ? 'Aucune classe selectionnee'
                      : classesCibles.length === 1
                        ? `${classesCibles[0]}`
                        : `${classesCibles.length} classes selectionnees`}
                  </strong>
                  <span>
                    Le frontend cree une annonce distincte pour chaque classe selectionnee.
                  </span>
                </div>
              </div>

              <div className="compose-footer">
                <div className="attachment-upload-box">
                  <div className="attachment-upload-header">
                    <div className="attachment-upload-title">
                      <Paperclip size={16} />
                      <span>RESSOURCES ATTACHEES</span>
                    </div>
                    <span className="attachment-upload-count">
                      {fichiersSelectionnes.length} fichier(s)
                    </span>
                  </div>
                  <p className="attachment-upload-hint">
                    Taille maximale: 50 Mo par fichier.
                  </p>

                  <div className="attachment-upload-actions">
                    <input
                      ref={fileInputRef}
                      className="attachment-input-hidden"
                      type="file"
                      multiple
                      onChange={gererSelectionFichiers}
                    />

                    <button
                      className="attachment-btn"
                      type="button"
                      onClick={() => fileInputRef.current?.click()}
                    >
                      <Upload size={18} />
                      AJOUTER DES FICHIERS
                    </button>

                    {fichiersSelectionnes.length ? (
                      <button
                        className="attachment-btn attachment-btn-muted"
                        type="button"
                        onClick={viderFichiers}
                      >
                        <X size={18} />
                        VIDER LA LISTE
                      </button>
                    ) : null}
                  </div>

                  {fichiersSelectionnes.length ? (
                    <div className="attachment-file-list">
                      {fichiersSelectionnes.map((fichier, index) => (
                        <div className="attachment-file-chip" key={`${fichier.name}-${index}`}>
                          <Paperclip size={14} />
                          <span>{fichier.name}</span>
                          <button type="button" onClick={() => retirerFichier(index)}>
                            <X size={14} />
                          </button>
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="attachment-empty-hint">
                      Les ressources selectionnees seront televersees apres la creation de l'annonce.
                    </p>
                  )}
                </div>
              </div>
            </div>

            <div className="audience-section">
              <div className="audience-header">
                <h2>
                  <Layers size={20} color="#6366f1" />
                  AUDIENCE
                </h2>
                <div className="prof-audience-actions">
                  <button
                    className={`select-all-toggle ${
                      mesClasses.length > 0 && classesCibles.length === mesClasses.length
                        ? 'active'
                        : ''
                    }`}
                    type="button"
                    onClick={gererSelectionToutesClasses}
                  >
                    {mesClasses.length > 0 && classesCibles.length === mesClasses.length
                      ? 'ANNULER TOUTES LES CLASSES'
                      : 'TOUTES LES CLASSES'}
                  </button>
                </div>
              </div>

              <div className="audience-status-bar">
                <Info size={16} />
                {mesClasses.length > 0 && classesCibles.length === mesClasses.length
                  ? `${classesCibles.length} CLASSES RECEVRONT L'ANNONCE.`
                  : classesCibles.length === 1
                    ? `1 CLASSE RECEVRA L'ANNONCE : ${classesCibles[0]}`
                    : classesCibles.length > 1
                      ? `${classesCibles.length} CLASSES RECEVRONT L'ANNONCE.`
                    : 'SELECTIONNEZ UNE CLASSE POUR DIFFUSER VOTRE ANNONCE.'}
              </div>

              {chargement ? (
                <div className="prof-empty-state">Chargement des classes...</div>
              ) : mesClasses.length === 0 ? (
                <div className="prof-empty-state">
                  Aucune classe assignee trouvee pour ce professeur.
                </div>
              ) : (
                <div className="audience-list">
                  {mesClasses.map((classe) => {
                    const isSelected = classesCibles.includes(classe.codeClasse);

                    return (
                      <div
                        key={classe.codeClasse}
                        className={`audience-card ${isSelected ? 'selected' : ''}`}
                        onClick={() => gererSelectionClasse(classe.codeClasse)}
                      >
                        <div className="audience-avatar audience-avatar-class">
                          {(classe.nomClasse || classe.codeClasse || 'C').charAt(0).toUpperCase()}
                        </div>

                        <div className="audience-info">
                          <h3>{classe.nomClasse || classe.codeClasse}</h3>
                          <p>{classe.codeClasse}</p>
                          {classe.matiereNom && <small>{classe.matiereNom}</small>}
                        </div>
                      </div>
                    );
                  })}
                </div>
              )}

              <div className="prof-target-note">
                Le mode "toutes les classes" duplique la meme annonce sur chaque classe de vos
                enseignements.
              </div>
            </div>
          </div>

          <div className="broadcast-section">
            <button
              className={`broadcast-btn ${isFormValid ? 'active' : ''}`}
              onClick={creerAnnonce}
              disabled={submitting || !isFormValid}
            >
              <Send size={24} />
              {submitting ? 'CREATION...' : "CREER L'ANNONCE"}
            </button>
          </div>

          <div className="prof-annonces-section">
            <div className="prof-section-header">
              <div>
                <h2>Mes annonces</h2>
                <p>Gerez vos brouillons et vos annonces publiees</p>
              </div>
              <span className="prof-counter">
                {mesAnnonces.length} annonce{mesAnnonces.length > 1 ? 's' : ''}
              </span>
            </div>

            {chargement ? (
              <div className="prof-empty-state">Chargement des annonces...</div>
            ) : mesAnnonces.length === 0 ? (
              <div className="prof-empty-state">
                Vous n'avez encore cree aucune annonce.
              </div>
            ) : (
              <div className="prof-annonces-grid">
                {mesAnnonces.map((annonce) => {
                  const codeClasse =
                    annonce?.classe?.codeClasse || annonce?.codeClasse || 'Classe inconnue';
                  const nomClasse =
                    annonce?.classe?.nomClasse || annonce?.classe?.nom || codeClasse;

                  return (
                    <div key={annonce.id} className="prof-annonce-card">
                      <div className="prof-annonce-header">
                        <div>
                          <div className="prof-annonce-title-row">
                            <h3>{annonce.titre || 'Sans titre'}</h3>
                            {annonce.publiee ? (
                              <span className="prof-badge published">PUBLIEE</span>
                            ) : (
                              <span className="prof-badge draft">BROUILLON</span>
                            )}
                          </div>

                          <p className="prof-annonce-meta">
                            {formaterDate(annonce.datePublication)} | {nomClasse} ({codeClasse})
                          </p>
                        </div>
                      </div>

                      <div className="prof-annonce-content">
                        {annonce.contenu || 'Aucun contenu.'}
                      </div>

                      {ressourcesParAnnonce[annonce.id]?.length ? (
                        <div className="prof-annonce-resources">
                          <div className="prof-annonce-resources-title">
                            <Paperclip size={14} />
                            <span>Ressources attachees</span>
                          </div>
                          <div className="prof-resource-list">
                            {ressourcesParAnnonce[annonce.id].map((ressource) => (
                              <button
                                key={ressource.id}
                                type="button"
                                className="prof-resource-chip"
                                onClick={() =>
                                  apiAnnoncesProfesseur.telechargerRessourceAnnonce(
                                    ressource.id
                                  )
                                }
                              >
                                <FileText size={14} />
                                <span>{ressource.nomOriginal}</span>
                                <small>{formaterTaille(ressource.taille)}</small>
                                <Download size={14} />
                              </button>
                            ))}
                          </div>
                        </div>
                      ) : null}

                      <div className="prof-card-actions">
                        <button
                          className={`prof-action-btn ${annonce.publiee ? 'warning' : 'success'}`}
                          type="button"
                          onClick={() => handleTogglePublication(annonce)}
                        >
                          <Eye size={16} />
                          {annonce.publiee ? 'Depublier' : 'Publier'}
                        </button>

                        <button
                          className="prof-action-btn danger"
                          type="button"
                          onClick={() => handleSupprimer(annonce.id)}
                        >
                          <Trash2 size={16} />
                          Supprimer
                        </button>
                      </div>
                    </div>
                  );
                })}
              </div>
            )}
          </div>
        </>
      ) : (
        <div className="prof-annonces-section">
          <div className="prof-section-header">
            <div>
              <h2>Annonces administrateur</h2>
              <p>Consultez les annonces diffusees au corps professoral dans un flux detaille.</p>
            </div>
            <span className="prof-counter">
              {annoncesAdminTriees.length} annonce{annoncesAdminTriees.length > 1 ? 's' : ''}
            </span>
          </div>

          {chargement ? (
            <div className="prof-empty-state">Chargement des annonces...</div>
          ) : annoncesAdminTriees.length === 0 ? (
            <div className="prof-empty-state">
              Aucune annonce administrateur disponible pour le moment.
            </div>
          ) : (
            <div className="prof-admin-annonces-like-student">
              <div className="ann-container">
                <aside className="ann-sidebar">
                  <div className="ann-sidebar-header">
                    <h1 className="ann-sidebar-title">Flux admin</h1>
                    <button
                      type="button"
                      onClick={() => chargerDonnees(false)}
                      className="ann-refresh-btn"
                      title="Rafraichir les annonces"
                    >
                      <RefreshCw size={18} />
                    </button>
                  </div>

                  <div className="ann-search-box">
                    <Search className="ann-search-icon" size={18} />
                    <input
                      type="text"
                      placeholder="Rechercher..."
                      value={adminSearchQuery}
                      onChange={(e) => setAdminSearchQuery(e.target.value)}
                    />
                  </div>

                  <div className="ann-filters">
                    {FILTRES_TYPE_ANNONCE.map((filter) => (
                      <button
                        key={filter}
                        type="button"
                        className={`ann-filter-pill ${adminActiveFilter === filter ? 'active' : ''}`}
                        onClick={() => setAdminActiveFilter(filter)}
                      >
                        {filter}
                      </button>
                    ))}
                  </div>

                  <div className="ann-list">
                    {annoncesAdminFiltrees.length > 0 ? (
                      annoncesAdminFiltrees.map((annonce) => (
                        <div
                          key={annonce.id}
                          className={`ann-item ${annonceAdminActive?.id === annonce.id ? 'active' : ''}`}
                          onClick={() => setAnnonceAdminActive(annonce)}
                        >
                          <div className="ann-item-header">
                            <span className="ann-item-date">{annonce.date}</span>
                          </div>
                          <div className="ann-item-title">{annonce.title}</div>
                          <div className="ann-item-subtitle">{annonce.subject}</div>
                        </div>
                      ))
                    ) : (
                      <div className="ann-empty">
                        <p>Aucune annonce trouvee</p>
                      </div>
                    )}
                  </div>
                </aside>

                <main className="ann-main">
                  {annonceAdminActive ? (
                    <article className="ann-detail">
                      <header className="ann-detail-header">
                        <div className="ann-prof-box">
                          <div className="ann-prof-img">
                            {annonceAdminActive.authorPhoto ? (
                              <img
                                src={`http://localhost:8080/uploads/usersPhotos/${annonceAdminActive.authorPhoto}`}
                                alt={annonceAdminActive.professor}
                                onError={(e) => {
                                  e.target.onerror = null;
                                  e.target.style.display = 'none';
                                  if (e.target.nextSibling) {
                                    e.target.nextSibling.style.display = 'block';
                                  }
                                }}
                                style={{
                                  width: '100%',
                                  height: '100%',
                                  objectFit: 'cover',
                                  borderRadius: '50%',
                                }}
                              />
                            ) : null}
                            <User
                              size={30}
                              style={{
                                margin: 'auto',
                                display: annonceAdminActive.authorPhoto ? 'none' : 'block',
                              }}
                              color="#94a3b8"
                            />
                          </div>
                          <div className="ann-prof-info">
                            <div className="ann-prof-name">{annonceAdminActive.professor}</div>
                            <div className="ann-prof-subject">{annonceAdminActive.subject}</div>
                          </div>
                        </div>

                        <div className="ann-status-box">
                          <div className="ann-status-label">PUBLIE LE</div>
                          <div className="ann-status-date">{annonceAdminActive.fullDate}</div>
                        </div>
                      </header>

                      <div className="ann-badges">
                        <div className={`ann-badge ${annonceAdminActive.category.toLowerCase()}`}>
                          {annonceAdminActive.category}
                        </div>
                      </div>

                      <h2 className="ann-headline">{annonceAdminActive.title}</h2>

                      <div className="ann-content">
                        {(annonceAdminActive.content || 'Aucun contenu.')
                          .split('\n\n')
                          .filter(Boolean)
                          .map((paragraph, idx) => (
                            <p key={idx}>{paragraph}</p>
                          ))}
                      </div>

                      <div className="ann-resources-section">
                        <div className="ann-resources-title">
                          <Paperclip size={14} />
                          RESSOURCES ATTACHEES
                        </div>

                        <div className="ann-resources-grid">
                          {adminRessourceLoading ? (
                            <div className="ann-no-resources">
                              <p>Chargement des ressources...</p>
                            </div>
                          ) : ressourcesAnnonceAdminActives.length > 0 ? (
                            ressourcesAnnonceAdminActives.map((ressource) => (
                              <div
                                key={ressource.id}
                                className="ann-resource-card"
                              >
                                <div className="ann-resource-icon">
                                  <FileText size={20} />
                                </div>
                                <div className="ann-resource-info">
                                  <strong>{ressource.nomOriginal || 'Ressource'}</strong>
                                  <span>{formaterTaille(ressource.taille)}</span>
                                </div>
                                <div className="ann-resource-actions">
                                  <button
                                    type="button"
                                    className="ann-resource-btn"
                                    onClick={() =>
                                      apiAnnoncesProfesseur.telechargerRessourceAnnonce(ressource.id)
                                    }
                                  >
                                    <Download size={16} />
                                    Telecharger
                                  </button>
                                </div>
                              </div>
                            ))
                          ) : (
                            <div className="ann-no-resources">
                              <p>Aucune ressource attachee</p>
                            </div>
                          )}
                        </div>
                      </div>
                    </article>
                  ) : (
                    <div className="ann-no-selection">
                      <p>Selectionnez une annonce pour voir les details</p>
                    </div>
                  )}
                </main>
              </div>
            </div>
          )}
        </div>
      )}
    </div>
  );
};

export default GestionAnnoncesProfesseur;

