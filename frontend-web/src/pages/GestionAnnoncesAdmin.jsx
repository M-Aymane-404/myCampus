import { useEffect, useState } from 'react';
import {
  Megaphone,
  Info,
  Plus,
  Paperclip,
  Send,
  Users,
  GraduationCap,
  Layers,
  Eye,
  Trash2,
  CheckCircle2,
  AlertTriangle,
} from 'lucide-react';
import apiAnnonces from '../services/apiAnnonces';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAuth } from '../services/apiAuth';
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
  const [prioriteHaute, setPrioriteHaute] = useState(false);
  const [classes, setClasses] = useState([]);
  const [professeurs, setProfesseurs] = useState([]);
  const [annonces, setAnnonces] = useState([]);
  const [formulaire, setFormulaire] = useState(FORM_INITIAL);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
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
          nom: classe.nom || classe.libelle || classe.codeClasse || '',
          filiere: classe.nomFiliere || 'GENIE INFORMATIQUE',
          selected: false,
        }))
      );

      setProfesseurs(
        (Array.isArray(donneesProfs) ? donneesProfs : []).map((professeur) => ({
          id: professeur.id,
          nom: `Pr. ${professeur.prenom} ${professeur.nom}`,
          specialite:
            professeur.specialite ||
            professeur.departement ||
            'DEPARTEMENT ENSEIGNEMENT',
          photo: professeur.photoUrl || null,
          selected: false,
        }))
      );

      setAnnonces(Array.isArray(donneesAnnonces) ? donneesAnnonces : []);
    } catch (err) {
      console.error(err);
      setError(err.message || 'Impossible de charger les annonces administrateur.');
      setClasses([]);
      setProfesseurs([]);
      setAnnonces([]);
    } finally {
      if (showLoader) setChargement(false);
    }
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire((prev) => ({ ...prev, [name]: value }));
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

    setProfesseurs((prev) =>
      prev.map((professeur) =>
        professeur.id === id
          ? { ...professeur, selected: !professeur.selected }
          : professeur
      )
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

    const toutSelectionne = professeurs.every((professeur) => professeur.selected);
    setProfesseurs((prev) =>
      prev.map((professeur) => ({ ...professeur, selected: !toutSelectionne }))
    );
  };

  const getSelectedCount = () => {
    if (cible === 'etudiants') {
      return classes.filter((classe) => classe.selected).length;
    }

    return professeurs.filter((professeur) => professeur.selected).length;
  };

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
    setPrioriteHaute(false);
    setClasses((prev) => prev.map((classe) => ({ ...classe, selected: false })));
    setProfesseurs((prev) =>
      prev.map((professeur) => ({ ...professeur, selected: false }))
    );
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
        urgent: prioriteHaute,
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
      } else {
        const selectedProfs = professeurs.filter((professeur) => professeur.selected);
        if (selectedProfs.length === 1) {
          annonceData.cibleType = 'PROFESSEURS_DEPARTEMENT';
          annonceData.codeDepartement = 'INFO';
        } else {
          annonceData.cibleType = 'PROFESSEURS_GLOBAL';
        }
      }

      await apiAnnonces.creer(annonceData);
      setSuccess('Annonce diffusee avec succes.');
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

  const getAnnonceDestination = (annonce) => {
    if (annonce?.cibleType === 'ETUDIANTS_GLOBAL') return 'Tous les etudiants';
    if (annonce?.cibleType === 'PROFESSEURS_GLOBAL') return 'Tous les professeurs';
    if (annonce?.cibleType === 'ETUDIANTS_CLASSE') {
      const codeClasse = annonce?.classe?.codeClasse || 'Classe inconnue';
      const nomClasse = annonce?.classe?.nom || annonce?.classe?.nomClasse || codeClasse;
      return `${nomClasse} (${codeClasse})`;
    }
    if (annonce?.cibleType === 'PROFESSEURS_DEPARTEMENT') {
      return annonce?.departement?.nom || annonce?.departement?.codeDepartement || 'Departement';
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
            onClick={() => setCible('etudiants')}
          >
            <GraduationCap size={20} />
            CIBLER ETUDIANTS
          </button>
          <button
            className={`target-btn ${cible === 'enseignants' ? 'active' : ''}`}
            type="button"
            onClick={() => setCible('enseignants')}
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
            <div className="priority-toggle-container">
              <label className="switch">
                <input
                  type="checkbox"
                  checked={prioriteHaute}
                  onChange={(e) => setPrioriteHaute(e.target.checked)}
                />
                <span className="slider"></span>
              </label>
              <div className="priority-info">
                <h4>PRIORITE HAUTE</h4>
                <p>AFFICHE UNE ALERTE SUR LE DASHBOARD</p>
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
            <button className="select-all-toggle" type="button" onClick={selectAllTargets}>
              {cible === 'etudiants' ? 'TOUTES LES CLASSES' : 'TOUS LES PROFS'}
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
                    className={`audience-card ${prof.selected ? 'selected' : ''}`}
                    onClick={() => toggleTarget(prof.id)}
                  >
                    {prof.photo ? (
                      <img src={prof.photo} alt={prof.nom} className="audience-avatar" />
                    ) : (
                      <div className="audience-avatar admin-audience-avatar-fallback">
                        {prof.nom.charAt(4)}
                      </div>
                    )}
                    <div className="audience-info">
                      <h3>{prof.nom}</h3>
                      <p>{prof.specialite}</p>
                    </div>
                  </div>
                ))}
          </div>

          <div className="audience-status-bar">
            <Info size={16} />
            {getSelectedCount() === 0
              ? cible === 'etudiants'
                ? "0 CLASSE(S) RECEVRONT L'ANNONCE."
                : "AUCUN ENSEIGNANT NE RECEVRA L'ANNONCE."
              : `${getSelectedCount()} ${
                  cible === 'etudiants' ? 'CLASSE(S)' : 'ENSEIGNANT(S)'
                } RECEVRONT L'ANNONCE.`}
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

                      {annonce.urgent ? (
                        <span className="prof-badge urgent">URGENT</span>
                      ) : null}

                      {annonce.publiee ? (
                        <span className="prof-badge published">PUBLIEE</span>
                      ) : (
                        <span className="prof-badge draft">BROUILLON</span>
                      )}
                    </div>

                    <p className="prof-annonce-meta">
                      {formaterDate(annonce.datePublication)} • {getAnnonceDestination(annonce)}
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
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default GestionAnnoncesAdmin;
