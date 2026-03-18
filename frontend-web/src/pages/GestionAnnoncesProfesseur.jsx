import { useEffect, useMemo, useRef, useState } from 'react';
import {
  AlertTriangle,
  CheckCircle2,
  Eye,
  GraduationCap,
  Info,
  Layers,
  Megaphone,
  Paperclip,
  Plus,
  Send,
  Trash2,
  Upload,
  Users,
  X,
} from 'lucide-react';
import apiAnnoncesProfesseur from '../services/professeur/apiAnnoncesProfesseur';
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
  modeCiblage: 'ONE',
  codeClasse: '',
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
  if (diffMinutes < 60) return `Il y a ${diffMinutes} minute${diffMinutes > 1 ? 's' : ''}`;
  if (diffHours < 24) return `Il y a ${diffHours} heure${diffHours > 1 ? 's' : ''}`;
  if (diffDays < 7) return `Il y a ${diffDays} jour${diffDays > 1 ? 's' : ''}`;

  return date.toLocaleDateString('fr-FR', {
    day: '2-digit',
    month: 'long',
    year: 'numeric',
  });
};

const GestionAnnoncesProfesseur = () => {
  const [chargement, setChargement] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [vueActive, setVueActive] = useState('etudiants');
  const [mesAnnonces, setMesAnnonces] = useState([]);
  const [annoncesAdmin, setAnnoncesAdmin] = useState([]);
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

      setMesAnnonces(Array.isArray(annonces) ? annonces : []);
      setAnnoncesAdmin(Array.isArray(annoncesProfesseurs) ? annoncesProfesseurs : []);
      setMesClasses(Array.isArray(classes) ? classes : []);
    } catch (err) {
      setError(err.message || 'Impossible de charger les annonces professeur.');
      setMesAnnonces([]);
      setAnnoncesAdmin([]);
      setMesClasses([]);
    } finally {
      if (showLoader) setChargement(false);
    }
  };

  const classesCibles = (() => {
    if (formulaire.modeCiblage === 'ALL') {
      return mesClasses.map((classe) => classe.codeClasse).filter(Boolean);
    }

    return formulaire.codeClasse ? [formulaire.codeClasse] : [];
  })();

  const classeSelectionnee = useMemo(
    () => mesClasses.find((classe) => classe.codeClasse === formulaire.codeClasse) || null,
    [mesClasses, formulaire.codeClasse],
  );

  const annoncesAdminTriees = useMemo(
    () =>
      [...annoncesAdmin].sort(
        (a, b) => new Date(b?.datePublication || 0) - new Date(a?.datePublication || 0),
      ),
    [annoncesAdmin],
  );

  const getAnnonceAdminSource = (annonce) => {
    const auteur = annonce?.auteur;
    if (auteur?.prenom || auteur?.nom) {
      return `${auteur.prenom || ''} ${auteur.nom || ''}`.trim();
    }
    return 'Administration';
  };

  const isFormValid = Boolean(
    formulaire.titre.trim() &&
    formulaire.contenu.trim() &&
    classesCibles.length > 0,
  );

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire((prev) => ({ ...prev, [name]: value }));
  };

  const gererSelectionClasse = (codeClasse) => {
    setFormulaire((prev) => ({
      ...prev,
      modeCiblage: 'ONE',
      codeClasse: prev.codeClasse === codeClasse ? '' : codeClasse,
    }));
  };

  const gererSelectionToutesClasses = () => {
    setFormulaire((prev) => ({
      ...prev,
      modeCiblage: prev.modeCiblage === 'ALL' ? 'ONE' : 'ALL',
      codeClasse: prev.modeCiblage === 'ALL' ? prev.codeClasse : '',
    }));
  };

  const gererSelectionFichiers = (event) => {
    setFichiersSelectionnes(Array.from(event.target.files || []));
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

    await Promise.all(
      fichiersSelectionnes.map((fichier) =>
        apiAnnoncesProfesseur.ajouterRessourceAnnonce(annonceId, fichier),
      ),
    );

    return fichiersSelectionnes.length;
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
        await chargerDonnees(false);
        reinitialiserFormulaire();
        setSuccess(
          annoncesCreees.length > 1
            ? `Annonces creees pour ${annoncesCreees.length} classes, mais l'envoi des ressources a echoue.`
            : "Annonce creee, mais l'envoi des ressources a echoue.",
        );
        setError(uploadError.message || "Erreur lors de l'envoi des ressources.");
        return;
      }

      if (nbClasses > 1) {
        setSuccess(
          nbFichiers > 0
            ? `Annonces creees pour ${nbClasses} classes avec ${nbFichiers} ressource(s) par annonce.`
            : `Annonces creees pour ${nbClasses} classes.`,
        );
      } else {
        setSuccess(
          nbFichiers > 0
            ? `Annonce creee avec succes avec ${nbFichiers} ressource(s).`
            : 'Annonce creee avec succes.',
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
                    {formulaire.modeCiblage === 'ALL'
                      ? `Toutes mes classes (${classesCibles.length})`
                      : classeSelectionnee
                        ? `${classeSelectionnee.nomClasse} (${classeSelectionnee.codeClasse})`
                        : 'Aucune classe selectionnee'}
                  </strong>
                  <span>
                    {formulaire.modeCiblage === 'ALL'
                      ? 'Le frontend cree une annonce distincte pour chaque classe selectionnee.'
                      : 'Le backend professeur accepte uniquement le ciblage par classe.'}
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
                    className={`select-all-toggle ${formulaire.modeCiblage === 'ALL' ? 'active' : ''}`}
                    type="button"
                    onClick={gererSelectionToutesClasses}
                  >
                    {formulaire.modeCiblage === 'ALL'
                      ? 'TOUTES LES CLASSES SELECTIONNEES'
                      : 'CHOISIR TOUTES LES CLASSES'}
                  </button>
                </div>
              </div>

              <div className="audience-status-bar">
                <Info size={16} />
                {formulaire.modeCiblage === 'ALL'
                  ? `${classesCibles.length} CLASSES RECEVRONT L'ANNONCE.`
                  : formulaire.codeClasse
                    ? `1 CLASSE RECEVRA L'ANNONCE : ${formulaire.codeClasse}`
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
                    const isSelected =
                      formulaire.modeCiblage === 'ALL' || formulaire.codeClasse === classe.codeClasse;

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
              <p>Consultez les annonces diffusees au corps professoral.</p>
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
            <div className="prof-annonces-grid">
              {annoncesAdminTriees.map((annonce) => {
                return (
                  <div key={annonce.id} className="prof-annonce-card admin-annonce-card">
                    <div className="prof-annonce-header">
                      <div>
                        <div className="prof-annonce-title-row">
                          <h3>{annonce.titre || 'Sans titre'}</h3>
                          <span className="prof-badge published">ADMIN</span>
                        </div>

                        <p className="prof-annonce-meta">
                          {formaterDate(annonce.datePublication)} | {getAnnonceAdminSource(annonce)}
                        </p>
                      </div>
                    </div>

                    <div className="prof-annonce-content">
                      {annonce.contenu || 'Aucun contenu.'}
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
};

export default GestionAnnoncesProfesseur;
