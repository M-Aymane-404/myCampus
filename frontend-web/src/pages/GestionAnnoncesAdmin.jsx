import { useEffect, useRef, useState } from 'react';
import {
  Megaphone,
  Info,
  Plus,
  Paperclip,
  FileText,
  Send,
  Users,
  GraduationCap,
  Layers,
  Eye,
  Trash2,
  Download,
  CheckCircle2,
  AlertTriangle,
  Upload,
  X,
} from 'lucide-react';
import apiAnnonces from '../services/apiAnnonces';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAuth } from '../services/apiAuth';
import PhotoSecurisee from '../composants/communs/PhotoSecurisee';
import './GestionAnnoncesAdmin.css';
import './GestionAnnoncesProfesseur.css';

const TYPES_ANNONCE = [
  { value: 'AUTRES', label: 'Autres' },
  { value: 'COURS', label: 'Cours' },
  { value: 'EVENT', label: 'Evenement' },
  { value: 'REUNION', label: 'Reunion' },
  { value: 'EXAMEN', label: 'Examen' },
];

const FORM_INITIAL = {
  titre: '',
  categorie: 'AUTRES',
  contenu: '',
};

const GestionAnnoncesAdmin = () => {
  const [chargement, setChargement] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [cible, setCible] = useState('etudiants');
  const [classes, setClasses] = useState([]);
  const [professeurs, setProfesseurs] = useState([]);
  const [diffusionProfGlobale, setDiffusionProfGlobale] = useState(false);
  const [annonces, setAnnonces] = useState([]);
  const [ressourcesParAnnonce, setRessourcesParAnnonce] = useState({});
  const [formulaire, setFormulaire] = useState(FORM_INITIAL);
  const [fichiersSelectionnes, setFichiersSelectionnes] = useState([]);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const fileInputRef = useRef(null);
  const emailAdminConnecte = (apiAuth.getUser()?.email || '').toLowerCase();

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
      const [donneesClasses, donneesProfs, donneesAnnonces] = await Promise.all([
        apiStructure.obtenirToutesClasses(),
        apiProfesseurs.obtenirTous(),
        apiAnnonces.obtenirToutes(),
      ]);

      setClasses(
        (Array.isArray(donneesClasses) ? donneesClasses : []).map((classe) => ({
          id: classe.codeClasse || classe.id?.toString(),
          nom:
            classe.nom || classe.nomClasse || classe.libelle || classe.codeClasse || '',
          filiere: classe.nomFiliere || 'GENIE INFORMATIQUE',
          selected: false,
        }))
      );

      setProfesseurs(
        (Array.isArray(donneesProfs) ? donneesProfs : []).map((professeur) => ({
          id: professeur.id,
          ppr: professeur.ppr || '',
          nom: `Pr. ${professeur.prenom} ${professeur.nom}`,
          specialite:
            professeur.specialite ||
            professeur.departement?.nom ||
            professeur.departement?.typeDepartement ||
            professeur.departement ||
            'DEPARTEMENT ENSEIGNEMENT',
          photo: professeur.photoUrl || null,
          selected: false,
        }))
      );

      const annoncesChargees = Array.isArray(donneesAnnonces) ? donneesAnnonces : [];
      setAnnonces(annoncesChargees);

      const annoncesAdmin = annoncesChargees.filter((annonce) => {
        const emailAuteur = (annonce?.auteur?.email || '').toLowerCase();
        return emailAuteur && emailAuteur === emailAdminConnecte;
      });

      const ressourcesEntries = await Promise.all(
        annoncesAdmin.map(async (annonce) => {
          try {
            const ressources = await apiAnnonces.listerRessources(annonce.id);
            return [annonce.id, Array.isArray(ressources) ? ressources : []];
          } catch {
            return [annonce.id, []];
          }
        })
      );

      setRessourcesParAnnonce(Object.fromEntries(ressourcesEntries));
    } catch (err) {
      console.error(err);
      setError(err.message || 'Impossible de charger les annonces administrateur.');
      setClasses([]);
      setProfesseurs([]);
      setDiffusionProfGlobale(false);
      setAnnonces([]);
      setRessourcesParAnnonce({});
    } finally {
      if (showLoader) setChargement(false);
    }
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire((prev) => ({ ...prev, [name]: value }));
  };

  const gererSelectionFichiers = (event) => {
    const nouveauxFichiers = Array.from(event.target.files || []);
    if (!nouveauxFichiers.length) {
      return;
    }

    setFichiersSelectionnes((prev) => {
      const existants = new Map(
        prev.map((fichier) => [
          `${fichier.name}-${fichier.size}-${fichier.lastModified}`,
          fichier,
        ])
      );

      nouveauxFichiers.forEach((fichier) => {
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
    setFichiersSelectionnes((prev) =>
      prev.filter((_, index) => index !== indexFichier)
    );
  };

  const toggleTarget = (id) => {
    if (cible === 'etudiants') {
      setClasses((prev) =>
        prev.map((classe) =>
          classe.id === id ? { ...classe, selected: !classe.selected } : classe
        )
      );
      return;
    }

    if (diffusionProfGlobale) {
      setDiffusionProfGlobale(false);
    }

    setProfesseurs((prev) =>
      prev.map((professeur) => ({
        ...professeur,
        selected:
          professeur.id === id ? !professeur.selected : professeur.selected,
      }))
    );
  };

  const selectAllTargets = () => {
    if (cible === 'etudiants') {
      const toutSelectionne = classes.every((classe) => classe.selected);
      setClasses((prev) =>
        prev.map((classe) => ({ ...classe, selected: !toutSelectionne }))
      );
      return;
    }

    setDiffusionProfGlobale((prev) => !prev);
    setProfesseurs((prev) =>
      prev.map((professeur) => ({ ...professeur, selected: false }))
    );
  };

  const getSelectedCount = () => {
    if (cible === 'etudiants') {
      return classes.filter((classe) => classe.selected).length;
    }

    if (diffusionProfGlobale) {
      return professeurs.length;
    }

    return professeurs.filter((professeur) => professeur.selected).length;
  };

  const tousLesEtudiantsSelectionnes =
    classes.length > 0 && classes.every((classe) => classe.selected);

  const isFormValid =
    formulaire.titre.trim() &&
    formulaire.contenu.trim() &&
    getSelectedCount() > 0;

  const annoncesAffichees = annonces.filter((annonce) => {
    const emailAuteur = (annonce?.auteur?.email || '').toLowerCase();
    return emailAuteur && emailAuteur === emailAdminConnecte;
  });

  const resetForm = () => {
    setFormulaire(FORM_INITIAL);
    setClasses((prev) => prev.map((classe) => ({ ...classe, selected: false })));
    setProfesseurs((prev) =>
      prev.map((professeur) => ({ ...professeur, selected: false }))
    );
    setDiffusionProfGlobale(false);
    viderFichiers();
  };

  const handleChangeCible = (nouvelleCible) => {
    setCible(nouvelleCible);
    setError('');
    setSuccess('');

    if (nouvelleCible === 'etudiants') {
      setDiffusionProfGlobale(false);
      setProfesseurs((prev) =>
        prev.map((professeur) => ({ ...professeur, selected: false }))
      );
      return;
    }

    setClasses((prev) => prev.map((classe) => ({ ...classe, selected: false })));
  };

  const televerserRessources = async (annonceId) => {
    if (!fichiersSelectionnes.length) {
      return 0;
    }

    const ressourcesCreees = await apiAnnonces.ajouterRessources(annonceId, fichiersSelectionnes);

    return Array.isArray(ressourcesCreees) ? ressourcesCreees.length : 0;
  };

  const diffuserAnnonce = async () => {
    setError('');
    setSuccess('');

    if (!formulaire.titre.trim() || !formulaire.contenu.trim()) {
      setError("Veuillez remplir le titre et le contenu de l'annonce.");
      return;
    }

    const selectedCount = getSelectedCount();
    if (selectedCount === 0) {
      setError("Veuillez selectionner au moins une cible pour l'annonce.");
      return;
    }

    try {
      setSubmitting(true);

      const annonceData = {
        titre: formulaire.titre.trim(),
        contenu: formulaire.contenu.trim(),
        typeAnnonce: formulaire.categorie,
      };

      if (cible === 'etudiants') {
        const selectedClasses = classes.filter((classe) => classe.selected);
        if (selectedClasses.length === 1) {
          annonceData.cibleType = 'ETUDIANTS_CLASSE';
          annonceData.codeClasse = selectedClasses[0].id;
        } else {
          annonceData.cibleType = 'ETUDIANTS_GLOBAL';
        }
      } else if (diffusionProfGlobale) {
        annonceData.cibleType = 'PROFESSEURS_GLOBAL';
      } else {
        const selectedProfs = professeurs.filter((professeur) => professeur.selected);
        annonceData.cibleType = 'PROFESSEURS_DEPARTEMENT';
        annonceData.professeurPprs = selectedProfs
          .map((professeur) => professeur.ppr)
          .filter(Boolean);
      }

      const annonceCreee = await apiAnnonces.creer(annonceData);
      let nbRessources = 0;

      try {
        nbRessources = await televerserRessources(annonceCreee.id);
      } catch (uploadError) {
        await chargerDonnees(false);
        resetForm();
        setSuccess("Annonce creee, mais l'envoi des ressources a echoue.");
        setError(uploadError.message || "Erreur lors de l'envoi des ressources.");
        return;
      }

      setSuccess(
        nbRessources > 0
          ? `Annonce diffusee avec succes avec ${nbRessources} ressource(s).`
          : 'Annonce diffusee avec succes.'
      );
      resetForm();
      await chargerDonnees(false);
    } catch (err) {
      setError(err.message || "Erreur lors de la diffusion de l'annonce.");
    } finally {
      setSubmitting(false);
    }
  };

  const handleSupprimer = async (id) => {
    const confirmation = window.confirm(
      'Voulez-vous vraiment supprimer cette annonce ?'
    );
    if (!confirmation) return;

    setError('');
    setSuccess('');

    try {
      await apiAnnonces.supprimer(id);
      setAnnonces((prev) => prev.filter((annonce) => annonce.id !== id));
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
        await apiAnnonces.depublier(annonce.id);
        setSuccess('Annonce depubliee avec succes.');
      } else {
        await apiAnnonces.publier(annonce.id);
        setSuccess('Annonce publiee avec succes.');
      }

      await chargerDonnees(false);
    } catch (err) {
      setError(err.message || 'Erreur lors du changement de publication.');
    }
  };

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
    if (diffMinutes < 60) {
      return `Il y a ${diffMinutes} minute${diffMinutes > 1 ? 's' : ''}`;
    }
    if (diffHours < 24) {
      return `Il y a ${diffHours} heure${diffHours > 1 ? 's' : ''}`;
    }
    if (diffDays < 7) {
      return `Il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;
    }

    return date.toLocaleDateString('fr-FR', {
      day: '2-digit',
      month: 'long',
      year: 'numeric',
    });
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

  const getAnnonceDestination = (annonce) => {
    if (annonce?.cibleType === 'ETUDIANTS_GLOBAL') return 'Tous les etudiants';
    if (annonce?.cibleType === 'PROFESSEURS_GLOBAL') return 'Tous les professeurs';
    if (annonce?.cibleType === 'ETUDIANTS_CLASSE') {
      const codeClasse = annonce?.classe?.codeClasse || 'Classe inconnue';
      const nomClasse = annonce?.classe?.nom || annonce?.classe?.nomClasse || codeClasse;
      return `${nomClasse} (${codeClasse})`;
    }
    if (annonce?.cibleType === 'PROFESSEURS_DEPARTEMENT') {
      return (
        annonce?.departement?.nom ||
        annonce?.departement?.codeDepartement ||
        'Professeurs selectionnes'
      );
    }
    return 'Cible inconnue';
  };

  return (
    <div className="communication-center">
      <div className="communication-header">
        <div className="header-content">
          <div className="header-icon">
            <Megaphone size={32} />
          </div>
          <div className="header-text">
            <h1>Centre de Communication</h1>
            <p>DIFFUSEZ VOS ANNONCES STRATEGIQUES</p>
          </div>
        </div>

        <div className="target-buttons">
          <button
            className={`target-btn ${cible === 'etudiants' ? 'active' : ''}`}
            type="button"
            onClick={() => handleChangeCible('etudiants')}
          >
            <GraduationCap size={20} />
            CIBLER ETUDIANTS
          </button>
          <button
            className={`target-btn ${cible === 'enseignants' ? 'active' : ''}`}
            type="button"
            onClick={() => handleChangeCible('enseignants')}
          >
            <Users size={20} />
            CIBLER ENSEIGNANTS
          </button>
        </div>
      </div>

      {error ? (
        <div className="prof-alert prof-alert-error">
          <div className="prof-alert-left">
            <AlertTriangle size={18} />
            <span>{error}</span>
          </div>
          <button type="button" onClick={() => setError('')}>
            x
          </button>
        </div>
      ) : null}

      {success ? (
        <div className="prof-alert prof-alert-success">
          <div className="prof-alert-left">
            <CheckCircle2 size={18} />
            <span>{success}</span>
          </div>
          <button type="button" onClick={() => setSuccess('')}>
            x
          </button>
        </div>
      ) : null}

      <div className="communication-content">
        <div className="compose-section">
          <div className="section-title">
            <Plus className="plus-icon" size={20} />
            COMPOSER L'ANNONCE
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
                  placeholder="Ex: Report du CC2..."
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
            <button
              className={`select-all-toggle ${
                (cible === 'etudiants' && tousLesEtudiantsSelectionnes) ||
                (cible === 'enseignants' && diffusionProfGlobale)
                  ? 'active'
                  : ''
              }`}
              type="button"
              onClick={selectAllTargets}
            >
              {cible === 'etudiants'
                ? tousLesEtudiantsSelectionnes
                  ? 'ANNULER TOUS LES ETUDIANTS'
                  : 'TOUTES LES CLASSES'
                : diffusionProfGlobale
                  ? 'ANNULER TOUS LES PROFS'
                  : 'TOUS LES PROFS'}
            </button>
          </div>

          <div className="audience-list">
            {cible === 'etudiants'
              ? classes.map((classe) => (
                  <div
                    key={classe.id}
                    className={`audience-card ${classe.selected ? 'selected' : ''}`}
                    onClick={() => toggleTarget(classe.id)}
                  >
                    <div className="audience-info">
                      <h3>{classe.nom}</h3>
                      <p>{classe.filiere}</p>
                    </div>
                  </div>
                ))
              : professeurs.map((prof) => (
                  <div
                    key={prof.id}
                    className={`audience-card ${(prof.selected || diffusionProfGlobale) ? 'selected' : ''}`}
                    onClick={() => toggleTarget(prof.id)}
                  >
                    <PhotoSecurisee
                      photoUrl={prof.photo}
                      alt={prof.nom}
                      className="audience-avatar"
                      fallback={
                        <div className="audience-avatar admin-audience-avatar-fallback">
                          {(prof.nom || '?').charAt(4) || (prof.nom || '?').charAt(0)}
                        </div>
                      }
                    />
                    <div className="audience-info">
                      <h3>{prof.nom}</h3>
                      <p>{prof.specialite}</p>
                    </div>
                  </div>
                ))}
          </div>

          <div className="audience-status-bar">
            <Info size={16} />
            {cible === 'enseignants' && diffusionProfGlobale
              ? "TOUS LES ENSEIGNANTS RECEVRONT L'ANNONCE."
              : getSelectedCount() === 0
                ? cible === 'etudiants'
                  ? "0 CLASSE(S) RECEVRONT L'ANNONCE."
                  : "AUCUN ENSEIGNANT NE RECEVRA L'ANNONCE."
                : `${getSelectedCount()} ${
                    cible === 'etudiants' ? 'CLASSE(S)' : 'ENSEIGNANT'
                  } RECEVRA L'ANNONCE.`}
          </div>
        </div>
      </div>

      <div className="broadcast-section">
        <button
          className={`broadcast-btn ${isFormValid ? 'active' : ''}`}
          onClick={diffuserAnnonce}
          disabled={submitting || !isFormValid}
        >
          <Send size={24} />
          {submitting ? 'DIFFUSION...' : "DIFFUSER L'ANNONCE"}
        </button>
      </div>

      <div className="prof-annonces-section">
        <div className="prof-section-header">
          <div>
            <h2>Mes annonces</h2>
            <p>Retrouvez les annonces creees depuis l espace administrateur</p>
          </div>
          <span className="prof-counter">
            {annoncesAffichees.length} annonce{annoncesAffichees.length > 1 ? 's' : ''}
          </span>
        </div>

        {chargement ? (
          <div className="prof-empty-state">Chargement des annonces...</div>
        ) : annoncesAffichees.length === 0 ? (
          <div className="prof-empty-state">Vous n'avez encore cree aucune annonce.</div>
        ) : (
          <div className="prof-annonces-grid">
            {annoncesAffichees.map((annonce) => (
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
                      {formaterDate(annonce.datePublication)} | {getAnnonceDestination(annonce)}
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
                          onClick={() => apiAnnonces.telechargerRessource(ressource.id)}
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
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GestionAnnoncesAdmin;



