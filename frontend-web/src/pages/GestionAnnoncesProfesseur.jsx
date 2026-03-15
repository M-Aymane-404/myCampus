import { useEffect, useMemo, useState } from 'react';
import {
  Megaphone,
  Plus,
  Send,
  GraduationCap,
  Layers,
  Info,
  Eye,
  Trash2,
  CheckCircle2,
  AlertTriangle,
  Paperclip,
} from 'lucide-react';
import apiAnnoncesProfesseur from '../services/professeur/apiAnnoncesProfesseur';
import './GestionAnnoncesAdmin.css';
import './GestionAnnoncesProfesseur.css';

const TYPES_ANNONCE = [
  { value: 'AUTRES', label: 'Autres' },
  { value: 'COURS', label: 'Cours' },
  { value: 'EVENT', label: 'Événement' },
  { value: 'REUNION', label: 'Réunion' },
  { value: 'EXAMEN', label: 'Examen' },
];

const FORM_INITIAL = {
  titre: '',
  categorie: 'AUTRES',
  contenu: '',
  codeClasse: '',
  urgent: false,
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
      () =>
          mesClasses.find(
              (classe) => classe.codeClasse === formulaire.codeClasse
          ) || null,
      [mesClasses, formulaire.codeClasse]
  );

  const isFormValid =
      formulaire.titre.trim() &&
      formulaire.contenu.trim() &&
      formulaire.codeClasse;

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire((prev) => ({ ...prev, [name]: value }));
  };

  const gererSelectionClasse = (codeClasse) => {
    setFormulaire((prev) => ({
      ...prev,
      codeClasse: prev.codeClasse === codeClasse ? '' : codeClasse,
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

    if (!formulaire.codeClasse) {
      setError('Vous devez sélectionner une classe.');
      return;
    }

    try {
      setSubmitting(true);

      const payload = {
        titre: formulaire.titre.trim(),
        contenu: formulaire.contenu.trim(),
        urgent: formulaire.urgent,
        typeAnnonce: formulaire.categorie,
        cibleType: 'ETUDIANTS_CLASSE',
        codeClasse: formulaire.codeClasse,
        codeDepartement: null,
      };

      await apiAnnoncesProfesseur.creerAnnonce(payload);

      setSuccess('Annonce créée avec succès en brouillon.');
      reinitialiserFormulaire();
      await chargerDonnees(false);
    } catch (err) {
      setError(err.message || "Erreur lors de la création de l'annonce.");
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
      await apiAnnoncesProfesseur.supprimerAnnonce(id);
      setMesAnnonces((prev) => prev.filter((annonce) => annonce.id !== id));
      setSuccess('Annonce supprimée avec succès.');
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
        setSuccess("Annonce dépubliée avec succès.");
      } else {
        await apiAnnoncesProfesseur.publierAnnonce(annonce.id);
        setSuccess("Annonce publiée avec succès.");
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

    if (diffMinutes < 1) return "À l'instant";
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

  return (
      <div className="communication-center professeur-communication-center">
        <div className="communication-header">
          <div className="header-content">
            <div className="header-icon">
              <Megaphone size={32} />
            </div>
            <div className="header-text">
              <h1>Centre de Communication</h1>
              <p>DIFFUSEZ VOS ANNONCES À VOS CLASSES</p>
            </div>
          </div>

          <div className="target-buttons">
            <button className="target-btn active" type="button">
              <GraduationCap size={20} />
              CIBLER ÉTUDIANTS
            </button>

            <div className="prof-target-chip">
              <Layers size={18} />
              {mesClasses.length} CLASSE{mesClasses.length > 1 ? 'S' : ''} ASSIGNÉE
              {mesClasses.length > 1 ? 'S' : ''}
            </div>
          </div>
        </div>

        {error && (
            <div className="prof-alert prof-alert-error">
              <div className="prof-alert-left">
                <AlertTriangle size={18} />
                <span>{error}</span>
              </div>
              <button type="button" onClick={() => setError('')}>
                ✕
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
                ✕
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
                      placeholder="Ex: Report du contrôle continu..."
                      className="form-input"
                      maxLength={200}
                  />
                </div>

                <div className="form-group">
                  <label>CATÉGORIE</label>
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
                <label>CONTENU DÉTAILLÉ</label>
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
                <div className="prof-selected-class-label">CLASSE CIBLÉE</div>
                <strong>
                  {classeSelectionnee
                      ? `${classeSelectionnee.nomClasse} (${classeSelectionnee.codeClasse})`
                      : 'Aucune classe sélectionnée'}
                </strong>
                <span>
                Le backend professeur autorise uniquement les annonces de type
                <code> ETUDIANTS_CLASSE </code>.
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
                  <h4>PRIORITÉ HAUTE</h4>
                  <p>AFFICHE UNE ALERTE PLUS VISIBLE CÔTÉ ÉTUDIANT</p>
                </div>
              </div>

              <button className="attachment-btn is-disabled" type="button" disabled>
                <Paperclip size={18} />
                PIÈCES JOINTES INDISPONIBLES
              </button>
            </div>
          </div>

          <div className="audience-section">
            <div className="audience-header">
              <h2>
                <Layers size={20} color="#6366f1" />
                AUDIENCE
              </h2>
              <button
                  className="select-all-toggle"
                  type="button"
                  onClick={() =>
                      setFormulaire((prev) => ({ ...prev, codeClasse: '' }))
                  }
              >
                RÉINITIALISER
              </button>
            </div>

            {chargement ? (
                <div className="prof-empty-state">
                  Chargement des classes...
                </div>
            ) : mesClasses.length === 0 ? (
                <div className="prof-empty-state">
                  Aucune classe assignée trouvée pour ce professeur.
                </div>
            ) : (
                <div className="audience-list">
                  {mesClasses.map((classe) => {
                    const isSelected = formulaire.codeClasse === classe.codeClasse;

                    return (
                        <div
                            key={classe.codeClasse}
                            className={`audience-card ${isSelected ? 'selected' : ''}`}
                            onClick={() => gererSelectionClasse(classe.codeClasse)}
                        >
                          <div className="audience-avatar audience-avatar-class">
                            {(classe.nomClasse || classe.codeClasse || 'C')
                                .charAt(0)
                                .toUpperCase()}
                          </div>

                          <div className="audience-info">
                            <h3>{classe.nomClasse || classe.codeClasse}</h3>
                            <p>{classe.codeClasse}</p>
                            {classe.matiereNom && (
                                <small>{classe.matiereNom}</small>
                            )}
                          </div>
                        </div>
                    );
                  })}
                </div>
            )}

            <div className="audience-status-bar">
              <Info size={16} />
              {formulaire.codeClasse
                  ? `1 CLASSE RECEVRA L'ANNONCE : ${formulaire.codeClasse}`
                  : "AUCUNE CLASSE N'EST ENCORE SÉLECTIONNÉE."}
            </div>

            <div className="prof-target-note">
              Sélection unique obligatoire : le backend professeur refuse les
              annonces globales.
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
            {submitting ? 'CRÉATION...' : "CRÉER L'ANNONCE"}
          </button>
        </div>

        <div className="prof-annonces-section">
          <div className="prof-section-header">
            <div>
              <h2>Mes annonces</h2>
              <p>Gérez vos brouillons et vos annonces publiées</p>
            </div>
            <span className="prof-counter">
            {mesAnnonces.length} annonce{mesAnnonces.length > 1 ? 's' : ''}
          </span>
          </div>

          {chargement ? (
              <div className="prof-empty-state">Chargement des annonces...</div>
          ) : mesAnnonces.length === 0 ? (
              <div className="prof-empty-state">
                Vous n’avez encore créé aucune annonce.
              </div>
          ) : (
              <div className="prof-annonces-grid">
                {mesAnnonces.map((annonce) => {
                  const codeClasse =
                      annonce?.classe?.codeClasse ||
                      annonce?.codeClasse ||
                      'Classe inconnue';

                  const nomClasse =
                      annonce?.classe?.nomClasse ||
                      annonce?.classe?.nom ||
                      codeClasse;

                  return (
                      <div key={annonce.id} className="prof-annonce-card">
                        <div className="prof-annonce-header">
                          <div>
                            <div className="prof-annonce-title-row">
                              <h3>{annonce.titre || 'Sans titre'}</h3>

                              {annonce.urgent && (
                                  <span className="prof-badge urgent">URGENT</span>
                              )}

                              {annonce.publiee ? (
                                  <span className="prof-badge published">PUBLIÉE</span>
                              ) : (
                                  <span className="prof-badge draft">BROUILLON</span>
                              )}
                            </div>

                            <p className="prof-annonce-meta">
                              {formaterDate(annonce.datePublication)} • {nomClasse} (
                              {codeClasse})
                            </p>
                          </div>
                        </div>

                        <div className="prof-annonce-content">
                          {annonce.contenu || 'Aucun contenu.'}
                        </div>

                        <div className="prof-card-actions">
                          <button
                              className={`prof-action-btn ${
                                  annonce.publiee ? 'warning' : 'success'
                              }`}
                              type="button"
                              onClick={() => handleTogglePublication(annonce)}
                          >
                            <Eye size={16} />
                            {annonce.publiee ? 'Dépublier' : 'Publier'}
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