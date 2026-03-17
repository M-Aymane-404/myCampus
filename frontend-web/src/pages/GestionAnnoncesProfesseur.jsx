import { useEffect, useMemo, useState } from 'react';
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
  urgent: false,
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
  const [mesAnnonces, setMesAnnonces] = useState([]);
  const [mesClasses, setMesClasses] = useState([]);
  const [formulaire, setFormulaire] = useState(FORM_INITIAL);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

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
      const [annonces, classes] = await Promise.all([
        apiAnnoncesProfesseur.listerMesAnnonces(),
        apiAnnoncesProfesseur.listerMesClasses(),
      ]);

      setMesAnnonces(Array.isArray(annonces) ? annonces : []);
      setMesClasses(Array.isArray(classes) ? classes : []);
    } catch (err) {
      setError(err.message || 'Impossible de charger les annonces professeur.');
      setMesAnnonces([]);
      setMesClasses([]);
    } finally {
      if (showLoader) setChargement(false);
    }
  };

  const classeSelectionnee = useMemo(
    () => mesClasses.find((classe) => classe.codeClasse === formulaire.codeClasse) || null,
    [mesClasses, formulaire.codeClasse],
  );

  const classesCibles = useMemo(() => {
    if (formulaire.modeCiblage === 'ALL') {
      return mesClasses.map((classe) => classe.codeClasse).filter(Boolean);
    }

    return formulaire.codeClasse ? [formulaire.codeClasse] : [];
  }, [formulaire.modeCiblage, formulaire.codeClasse, mesClasses]);

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

  const reinitialiserFormulaire = () => {
    setFormulaire(FORM_INITIAL);
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
      setError('Vous devez selectionner une classe ou choisir toutes les classes.');
      return;
    }

    try {
      setSubmitting(true);

      for (const codeClasse of classesCibles) {
        await apiAnnoncesProfesseur.creerAnnonce({
          titre: formulaire.titre.trim(),
          contenu: formulaire.contenu.trim(),
          urgent: formulaire.urgent,
          typeAnnonce: formulaire.categorie,
          cibleType: 'ETUDIANTS_CLASSE',
          codeClasse,
          codeDepartement: null,
        });
      }

      setSuccess(
        classesCibles.length > 1
          ? `Annonce creee pour ${classesCibles.length} classes.`
          : 'Annonce creee avec succes en brouillon.',
      );
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
            <p>Diffusez vos annonces a vos classes</p>
          </div>
        </div>

        <section className="prof-summary-banner" aria-label="Resume de diffusion">
          <div className="prof-summary-head">
            <div className="prof-summary-heading-row">
              <span className="prof-summary-kicker">Resume de diffusion</span>
              <span className="prof-summary-chip">Etat en temps reel</span>
            </div>
            <h2 className="prof-summary-main-title">Votre annonce sera diffusee selon la cible active.</h2>
            <p className="prof-summary-intro">
              Cette zone vous donne un resume rapide de l'audience choisie et du nombre de classes disponibles dans vos enseignements.
            </p>
          </div>

          <div className="prof-summary-grid">
            <div className="prof-summary-item" role="status" aria-live="polite">
              <div className="prof-summary-icon">
                <GraduationCap size={18} />
              </div>
              <div className="prof-summary-copy">
                <span className="prof-summary-label">Audience selectionnee</span>
                <strong className="prof-summary-title">
                  {formulaire.modeCiblage === 'ALL' ? 'Toutes les classes' : 'Une classe'}
                </strong>
                <span className="prof-summary-text">
                  {formulaire.modeCiblage === 'ALL'
                    ? "L'annonce sera dupliquee pour chaque classe de vos enseignements."
                    : "L'annonce sera envoyee uniquement a la classe choisie."}
                </span>
              </div>
            </div>

            <div className="prof-summary-item" role="status" aria-live="polite">
              <div className="prof-summary-icon">
                <Layers size={18} />
              </div>
              <div className="prof-summary-copy">
                <span className="prof-summary-label">Classes disponibles</span>
                <strong className="prof-summary-title">
                  {mesClasses.length} classe{mesClasses.length > 1 ? 's' : ''}
                </strong>
                <span className="prof-summary-text">
                  Vous pouvez diffuser vers 1 classe dans cet espace.
                </span>
              </div>
            </div>
          </div>
        </section>
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
                  ? 'Le frontend cree une annonce par classe de vos enseignements.'
                  : 'Le backend professeur accepte le ciblage par classe.'}
              </span>
            </div>
          </div>

          <div className="compose-footer">
            <div className="priority-toggle-container">
              <label className="switch">
                <input
                  type="checkbox"
                  checked={formulaire.urgent}
                  onChange={(e) =>
                    setFormulaire((prev) => ({
                      ...prev,
                      urgent: e.target.checked,
                    }))
                  }
                />
                <span className="slider"></span>
              </label>

              <div className="priority-info">
                <h4>PRIORITE HAUTE</h4>
                <p>Affiche une alerte plus visible cote etudiant</p>
              </div>
            </div>

            <button className="attachment-btn is-disabled" type="button" disabled>
              <Paperclip size={18} />
              PIECES JOINTES INDISPONIBLES
            </button>
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
                {formulaire.modeCiblage === 'ALL' ? 'TOUTES LES CLASSES SELECTIONNEES' : 'CHOISIR TOUTES LES CLASSES'}
              </button>
              <button
                className="select-all-toggle"
                type="button"
                onClick={() => setFormulaire((prev) => ({ ...prev, modeCiblage: 'ONE', codeClasse: '' }))}
              >
                REINITIALISER
              </button>
            </div>
          </div>

          {chargement ? (
            <div className="prof-empty-state">Chargement des classes...</div>
          ) : mesClasses.length === 0 ? (
            <div className="prof-empty-state">Aucune classe assignee trouvee pour ce professeur.</div>
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

          <div className="audience-status-bar">
            <Info size={16} />
            {formulaire.modeCiblage === 'ALL'
              ? `${classesCibles.length} CLASSES RECEVRONT L'ANNONCE.`
              : formulaire.codeClasse
                ? `1 CLASSE RECEVRA L'ANNONCE : ${formulaire.codeClasse}`
                : "AUCUNE CLASSE N'EST ENCORE SELECTIONNEE."}
          </div>

          <div className="prof-target-note">
            Le mode "toutes les classes" cree la meme annonce pour chaque classe de vos enseignements.
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
          <div className="prof-empty-state">Vous n'avez encore cree aucune annonce.</div>
        ) : (
          <div className="prof-annonces-grid">
            {mesAnnonces.map((annonce) => {
              const codeClasse = annonce?.classe?.codeClasse || annonce?.codeClasse || 'Classe inconnue';
              const nomClasse = annonce?.classe?.nomClasse || annonce?.classe?.nom || codeClasse;

              return (
                <div key={annonce.id} className="prof-annonce-card">
                  <div className="prof-annonce-header">
                    <div>
                      <div className="prof-annonce-title-row">
                        <h3>{annonce.titre || 'Sans titre'}</h3>

                        {annonce.urgent && <span className="prof-badge urgent">URGENT</span>}
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
    </div>
  );
};

export default GestionAnnoncesProfesseur;
