import { useState, useEffect } from 'react';
import { Megaphone, Info, Plus, Paperclip, Send, Users, GraduationCap, Layers } from 'lucide-react';
import apiAnnonces from '../services/apiAnnonces';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAuth } from '../services/apiAuth';
import './GestionAnnoncesAdmin.css';

const GestionAnnoncesAdmin = () => {
  const [chargement, setChargement] = useState(false);
  const [cible, setCible] = useState('etudiants'); // 'etudiants' or 'enseignants'
  const [prioriteHaute, setPrioriteHaute] = useState(false);
  const [classes, setClasses] = useState([]);
  const [professeurs, setProfesseurs] = useState([]);
  const [formulaire, setFormulaire] = useState({
    titre: '',
    categorie: 'AUTRES',
    contenu: ''
  });

  useEffect(() => {
    chargerClasses();
    chargerProfesseurs();
  }, []);

  const chargerClasses = async () => {
    try {
      const donnees = await apiStructure.obtenirToutesClasses();
      const classesMapped = donnees.map(c => ({
        id: c.codeClasse || c.id?.toString(),
        nom: c.nom || c.libelle || c.codeClasse || '',
        filiere: c.nomFiliere || 'GÉNIE INFORMATIQUE',
        selected: false
      }));
      setClasses(classesMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des classes:', err);
      setClasses([]);
    }
  };

  const chargerProfesseurs = async () => {
    try {
      const donnees = await apiProfesseurs.obtenirTous();
      const profsMapped = donnees.map(p => ({
        id: p.id,
        nom: `Pr. ${p.prenom} ${p.nom}`,
        specialite: p.specialite || p.departement || 'DÉPARTEMENT ENSEIGNEMENT',
        photo: p.photoUrl || null,
        selected: false
      }));
      setProfesseurs(profsMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des professeurs:', err);
      setProfesseurs([]);
    }
  };

  const typesAnnonce = [
    { value: 'AUTRES', label: 'Autres' },
    { value: 'COURS', label: 'Cours' },
    { value: 'EVENT', label: 'Événement' },
    { value: 'REUNION', label: 'Réunion' },
    { value: 'EXAMEN', label: 'Examen' }
  ];

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const toggleTarget = (id) => {
    if (cible === 'etudiants') {
      setClasses(prev => prev.map(classe =>
        classe.id === id ? { ...classe, selected: !classe.selected } : classe
      ));
    } else {
      setProfesseurs(prev => prev.map(prof =>
        prof.id === id ? { ...prof, selected: !prof.selected } : prof
      ));
    }
  };

  const selectAllTargets = () => {
    if (cible === 'etudiants') {
      const allSelected = classes.every(c => c.selected);
      setClasses(prev => prev.map(classe => ({ ...classe, selected: !allSelected })));
    } else {
      const allSelected = professeurs.every(p => p.selected);
      setProfesseurs(prev => prev.map(prof => ({ ...prof, selected: !allSelected })));
    }
  };

  const getSelectedCount = () => {
    if (cible === 'etudiants') {
      return classes.filter(c => c.selected).length;
    } else {
      return professeurs.filter(p => p.selected).length;
    }
  };

  const diffuserAnnonce = async () => {
    if (!formulaire.titre || !formulaire.contenu) {
      alert('Veuillez remplir le titre et le contenu de l\'annonce');
      return;
    }

    const selectedCount = getSelectedCount();
    if (selectedCount === 0) {
      alert('Veuillez sélectionner au moins une cible pour l\'annonce');
      return;
    }

    try {
      setChargement(true);

      // Prepare announcement data
      let annonceData = {
        titre: formulaire.titre,
        contenu: formulaire.contenu,
        urgent: prioriteHaute,
        typeAnnonce: formulaire.categorie
      };

      if (cible === 'etudiants') {
        const selectedClasses = classes.filter(c => c.selected);
        
        if (selectedClasses.length === 1) {
          // Single class - use ETUDIANTS_CLASSE
          annonceData.cibleType = 'ETUDIANTS_CLASSE';
          annonceData.codeClasse = selectedClasses[0].id;
        } else {
          // Multiple classes - use ETUDIANTS_GLOBAL
          annonceData.cibleType = 'ETUDIANTS_GLOBAL';
        }
      } else {
        // Teachers
        const selectedProfs = professeurs.filter(p => p.selected);
        
        if (selectedProfs.length === 1) {
          // Single department - use PROFESSEURS_DEPARTEMENT
          annonceData.cibleType = 'PROFESSEURS_DEPARTEMENT';
          annonceData.codeDepartement = 'INFO'; // Default department code
        } else {
          // Multiple professors - use PROFESSEURS_GLOBAL
          annonceData.cibleType = 'PROFESSEURS_GLOBAL';
        }
      }

      // Create announcement - backend will handle notifications automatically
      await apiAnnonces.creer(annonceData);
      alert('Annonce diffusée avec succès !');
      resetForm();

    } catch (err) {
      alert('Erreur: ' + (err.message || 'Erreur inconnue'));
      console.error(err);
    } finally {
      setChargement(false);
    }
  };

  const resetForm = () => {
    setFormulaire({
      titre: '',
      categorie: 'AUTRES',
      contenu: ''
    });
    setPrioriteHaute(false);
    setClasses(prev => prev.map(c => ({ ...c, selected: false })));
    setProfesseurs(prev => prev.map(p => ({ ...p, selected: false })));
  };

  const isFormValid = formulaire.titre && formulaire.contenu && getSelectedCount() > 0;

  return (
    <div className="communication-center">
      {/* Header */}
      <div className="communication-header">
        <div className="header-content">
          <div className="header-icon">
            <Megaphone size={32} />
          </div>
          <div className="header-text">
            <h1>Centre de Communication</h1>
            <p>DIFFUSEZ VOS ANNONCES STRATÉGIQUES</p>
          </div>
        </div>
        <div className="target-buttons">
          <button
            className={`target-btn ${cible === 'etudiants' ? 'active' : ''}`}
            onClick={() => setCible('etudiants')}
          >
            <GraduationCap size={20} />
            CIBLER ÉTUDIANTS
          </button>
          <button
            className={`target-btn ${cible === 'enseignants' ? 'active' : ''}`}
            onClick={() => setCible('enseignants')}
          >
            <Users size={20} />
            CIBLER ENSEIGNANTS
          </button>
        </div>
      </div>

      {/* Main Content */}
      <div className="communication-content">
        {/* Left Section - Compose */}
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
                  {typesAnnonce.map(type => (
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
                <h4>PRIORITÉ HAUTE</h4>
                <p>AFFICHE UNE ALERTE SUR LE DASHBOARD</p>
              </div>
            </div>

            <button className="attachment-btn">
              <Paperclip size={18} />
              AJOUTER DES PIÈCES JOINTES
            </button>
          </div>
        </div>

        {/* Right Section - Audience */}
        <div className="audience-section">
          <div className="audience-header">
            <h2>
              <Layers size={20} color="#6366f1" />
              AUDIENCE
            </h2>
            <button className="select-all-toggle" onClick={selectAllTargets}>
              {cible === 'etudiants' ? 'TOUTES LES CLASSES' : 'TOUS LES PROFS'}
            </button>
          </div>

          <div className="audience-list">
            {cible === 'etudiants' ? (
              classes.map(classe => (
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
            ) : (
              professeurs.map(prof => (
                <div
                  key={prof.id}
                  className={`audience-card ${prof.selected ? 'selected' : ''}`}
                  onClick={() => toggleTarget(prof.id)}
                >
                  {prof.photo ? (
                    <img src={prof.photo} alt={prof.nom} className="audience-avatar" />
                  ) : (
                    <div className="audience-avatar" style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', background: '#e2e8f0', color: '#64748b', fontWeight: '800' }}>
                      {prof.nom.charAt(4)}
                    </div>
                  )}
                  <div className="audience-info">
                    <h3>{prof.nom}</h3>
                    <p>{prof.specialite}</p>
                  </div>
                </div>
              ))
            )}
          </div>

          <div className="audience-status-bar">
            <Info size={16} />
            {getSelectedCount() === 0 ? (
              cible === 'etudiants' ? "0 CLASSE(S) RECEVRONT L'ANNONCE." : "AUCUN RECEVRA L'ANNONCE."
            ) : (
              `${getSelectedCount()} ${cible === 'etudiants' ? 'CLASSE(S)' : 'ENSEIGNANT(S)'} RECEVRONT L'ANNONCE.`
            )}
          </div>
        </div>
      </div>

      {/* Bottom Action */}
      <div className="broadcast-section">
        <button
          className={`broadcast-btn ${isFormValid ? 'active' : ''}`}
          onClick={diffuserAnnonce}
          disabled={chargement || !isFormValid}
        >
          <Send size={24} />
          {chargement ? 'DIFFUSION...' : 'DIFFUSER L\'ANNONCE'}
        </button>
      </div>
    </div>
  );
};

export default GestionAnnoncesAdmin;
