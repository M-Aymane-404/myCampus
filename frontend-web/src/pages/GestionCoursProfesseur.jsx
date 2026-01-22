import { useState } from 'react';
import { Upload, FileText, Video, File, Trash2, Edit, Plus, MoreVertical, Settings, Code, Database, Zap } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import BoutonAction from '../composants/communs/BoutonAction';
import './GestionCoursProfesseur.css';

const GestionCoursProfesseur = () => {
  const [cours, setCours] = useState([
    {
      id: 1,
      titre: 'Programmation Java',
      niveau: 'Licence 3 Informatique',
      etudiants: 48,
      ressources: 12,
      prochainCours: { date: 'Aujourd\'hui', heure: '14:00' },
      ressourcesRecentes: [
        { id: 1, nom: 'Chapitre 5 - Héritage.pdf', type: 'PDF', dateAjout: 'Il y a 2 jours' },
        { id: 2, nom: 'TP4 - Polymorphisme.mp4', type: 'VIDEO', dateAjout: 'Il y a 5 jours' }
      ],
      icone: 'code',
      statut: 'Actif',
      description: 'Cours de programmation orientée objet avec Java'
    },
    {
      id: 2,
      titre: 'Bases de données',
      niveau: 'Master 1 Informatique',
      etudiants: 32,
      ressources: 8,
      prochainCours: { date: 'Demain', heure: '10:00' },
      ressourcesRecentes: [
        { id: 1, nom: 'SQL Avancé - Requêtes.pdf', type: 'PDF', dateAjout: 'Il y a 1 jour' }
      ],
      icone: 'database',
      statut: 'Actif',
      description: 'Cours sur les systèmes de gestion de bases de données'
    },
    {
      id: 3,
      titre: 'Algorithmique',
      niveau: 'Licence 2 Informatique',
      etudiants: 62,
      ressources: 15,
      prochainCours: { date: 'Mercredi', heure: '08:30' },
      ressourcesRecentes: [
        { id: 1, nom: 'TD3 - Tri et Recherche.pdf', type: 'PDF', dateAjout: 'Il y a 3 jours' }
      ],
      icone: 'algorithm',
      statut: 'Actif',
      description: 'Introduction aux algorithmes et structures de données'
    }
  ]);

  const [modalPublierCours, setModalPublierCours] = useState(false);
  const [modalAjouterRessource, setModalAjouterRessource] = useState(false);
  const [coursSelectionne, setCoursSelectionne] = useState(null);
  
  const [formulaireCours, setFormulaireCours] = useState({
    titre: '',
    classe: '',
    type: 'Cours magistral',
    description: ''
  });

  const [formulaireRessource, setFormulaireRessource] = useState({
    titre: '',
    typeRessource: 'PDF (Document)',
    fichier: null,
    description: ''
  });

  const [fichierSelectionne, setFichierSelectionne] = useState(null);
  const [dragActive, setDragActive] = useState(false);

  const classes = [
    { value: '', label: '-- Sélectionner --' },
    { value: 'L1-Info', label: 'Licence 1 Informatique' },
    { value: 'L2-Info', label: 'Licence 2 Informatique' },
    { value: 'L3-Info', label: 'Licence 3 Informatique' },
    { value: 'M1-Info', label: 'Master 1 Informatique' },
    { value: 'M2-Info', label: 'Master 2 Informatique' }
  ];

  const typesCours = [
    { value: 'Cours magistral', label: 'Cours magistral' },
    { value: 'TD', label: 'TD' },
    { value: 'TP', label: 'TP' }
  ];

  const typesRessource = [
    { value: 'PDF (Document)', label: 'PDF (Document)' },
    { value: 'VIDEO', label: 'Vidéo' },
    { value: 'DOC', label: 'Document Word' },
    { value: 'ZIP', label: 'Archive ZIP' }
  ];

  const ouvrirModalPublierCours = () => {
    setFormulaireCours({
      titre: '',
      classe: '',
      type: 'Cours magistral',
      description: ''
    });
    setModalPublierCours(true);
  };

  const ouvrirModalAjouterRessource = (coursId) => {
    setCoursSelectionne(coursId);
    setFormulaireRessource({
      titre: '',
      typeRessource: 'PDF (Document)',
      fichier: null,
      description: ''
    });
    setFichierSelectionne(null);
    setModalAjouterRessource(true);
  };

  const gererChangementCours = (e) => {
    const { name, value } = e.target;
    setFormulaireCours(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementRessource = (e) => {
    const { name, value } = e.target;
    setFormulaireRessource(prev => ({ ...prev, [name]: value }));
  };

  const gererSelectionFichier = (e) => {
    const fichier = e.target.files[0];
    if (fichier) {
      setFichierSelectionne(fichier);
      setFormulaireRessource(prev => ({ ...prev, fichier: fichier }));
    }
  };

  const gererDragOver = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(true);
  };

  const gererDragLeave = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);
  };

  const gererDrop = (e) => {
    e.preventDefault();
    e.stopPropagation();
    setDragActive(false);

    if (e.dataTransfer.files && e.dataTransfer.files[0]) {
      const fichier = e.dataTransfer.files[0];
      setFichierSelectionne(fichier);
      setFormulaireRessource(prev => ({ ...prev, fichier: fichier }));
    }
  };

  const sauvegarderCours = () => {
    if (!formulaireCours.titre || !formulaireCours.classe || !formulaireCours.type) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const nouveauCours = {
      id: Date.now(),
      titre: formulaireCours.titre,
      niveau: classes.find(c => c.value === formulaireCours.classe)?.label || formulaireCours.classe,
      etudiants: 0,
      ressources: 0,
      prochainCours: { date: '', heure: '' },
      ressourcesRecentes: [],
      icone: 'code',
      statut: 'Actif',
      description: formulaireCours.description
    };

    setCours(prev => [...prev, nouveauCours]);
    console.log('Course published:', nouveauCours);
    alert('Cours publié avec succès');
    setModalPublierCours(false);
  };

  const sauvegarderRessource = () => {
    if (!formulaireRessource.titre || !fichierSelectionne) {
      alert('Veuillez remplir tous les champs obligatoires et sélectionner un fichier');
      return;
    }

    const nouvelleRessource = {
      id: Date.now(),
      nom: fichierSelectionne.name,
      type: formulaireRessource.typeRessource.includes('PDF') ? 'PDF' : 
            formulaireRessource.typeRessource.includes('VIDEO') ? 'VIDEO' : 'DOC',
      dateAjout: 'Aujourd\'hui'
    };

    if (coursSelectionne) {
      setCours(prev => prev.map(c => 
        c.id === coursSelectionne 
          ? {
              ...c,
              ressources: c.ressources + 1,
              ressourcesRecentes: [nouvelleRessource, ...c.ressourcesRecentes].slice(0, 2)
            }
          : c
      ));
    }

    console.log('Resource added:', nouvelleRessource);
    alert('Ressource ajoutée avec succès');
    setModalAjouterRessource(false);
    setFichierSelectionne(null);
  };

  const obtenirIconeCours = (icone) => {
    switch(icone) {
      case 'code':
        return <Code size={24} />;
      case 'database':
        return <Database size={24} />;
      case 'algorithm':
        return <Zap size={24} />;
      default:
        return <FileText size={24} />;
    }
  };

  return (
    <div className="gestion-cours-professeur">
      <div className="gestion-cours-professeur-header">
        <div>
          <h1 className="gestion-cours-professeur-titre">Mes Cours</h1>
          <p className="gestion-cours-professeur-sous-titre">Gérez vos cours, TP et TD</p>
        </div>
        <BoutonAction
          libelle="Publier un cours"
          variante="primaire"
          taille="moyen"
          icone={Plus}
          onClick={ouvrirModalPublierCours}
        />
      </div>

      <div className="mes-cours-grid">
        {cours.map((coursItem) => (
          <div key={coursItem.id} className="cours-card">
            <div className="cours-card-header">
              <div className="cours-card-icone">
                {obtenirIconeCours(coursItem.icone)}
              </div>
              <div className="cours-card-info">
                <h3 className="cours-card-titre">{coursItem.titre}</h3>
                <p className="cours-card-niveau">{coursItem.niveau}</p>
              </div>
              <div className="cours-card-statut">
                <span className="badge-statut badge-statut-actif">{coursItem.statut}</span>
              </div>
            </div>

            <div className="cours-card-details">
              <div className="cours-detail-item">
                <span className="cours-detail-label">Étudiants inscrits</span>
                <span className="cours-detail-valeur">{coursItem.etudiants} étudiants</span>
              </div>
              <div className="cours-detail-item">
                <span className="cours-detail-label">Ressources</span>
                <span className="cours-detail-valeur">{coursItem.ressources} documents</span>
              </div>
              <div className="cours-detail-item">
                <span className="cours-detail-label">Prochain cours</span>
                <span className="cours-detail-valeur cours-detail-prochain">
                  {coursItem.prochainCours.date 
                    ? `${coursItem.prochainCours.date} ${coursItem.prochainCours.heure}`
                    : 'Non programmé'
                  }
                </span>
              </div>
            </div>

            {coursItem.ressourcesRecentes.length > 0 && (
              <div className="cours-card-ressources">
                <h4 className="ressources-titre">RESSOURCES RÉCENTES:</h4>
                <div className="ressources-liste">
                  {coursItem.ressourcesRecentes.map((ressource) => (
                    <div key={ressource.id} className="ressource-item">
                      <div className="ressource-info">
                        {ressource.type === 'PDF' ? (
                          <FileText size={16} className="ressource-icone ressource-icone-pdf" />
                        ) : (
                          <Video size={16} className="ressource-icone ressource-icone-video" />
                        )}
                        <div>
                          <span className="ressource-nom">{ressource.nom}</span>
                          <span className="ressource-date">{ressource.dateAjout}</span>
                        </div>
                      </div>
                      <button className="ressource-menu">
                        <MoreVertical size={16} />
                      </button>
                    </div>
                  ))}
                </div>
              </div>
            )}

            <div className="cours-card-actions">
              <BoutonAction
                libelle="Ajouter ressource"
                variante="primaire"
                taille="moyen"
                icone={Plus}
                onClick={() => ouvrirModalAjouterRessource(coursItem.id)}
              />
              <BoutonAction
                libelle="Gérer"
                variante="secondaire"
                taille="moyen"
                onClick={() => console.log('Manage course:', coursItem.id)}
              />
            </div>
          </div>
        ))}
      </div>

      {/* Modal Publier le cours */}
      <FormulaireModal
        titre="Publier le cours"
        estOuvert={modalPublierCours}
        surFermer={() => setModalPublierCours(false)}
        surSauvegarder={sauvegarderCours}
        texteBoutonSauvegarder="Publier le cours"
      >
        <ChampFormulaire
          label="Titre du cours"
          nom="titre"
          valeur={formulaireCours.titre}
          onChange={gererChangementCours}
          placeholder="Ex: Introduction aux algorithmes"
          requis
        />
        <ChampFormulaire
          label="Classe / Niveau"
          nom="classe"
          type="select"
          valeur={formulaireCours.classe}
          onChange={gererChangementCours}
          options={classes}
          requis
        />
        <ChampFormulaire
          label="Type"
          nom="type"
          type="select"
          valeur={formulaireCours.type}
          onChange={gererChangementCours}
          options={typesCours}
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireCours.description}
          onChange={gererChangementCours}
          placeholder="Décrivez le contenu du cours..."
        />
      </FormulaireModal>

      {/* Modal Ajouter une ressource */}
      <FormulaireModal
        titre="Ajouter une ressource"
        estOuvert={modalAjouterRessource}
        surFermer={() => {
          setModalAjouterRessource(false);
          setFichierSelectionne(null);
        }}
        surSauvegarder={sauvegarderRessource}
        texteBoutonSauvegarder="Ajouter la ressource"
      >
        <ChampFormulaire
          label="Titre du document"
          nom="titre"
          valeur={formulaireRessource.titre}
          onChange={gererChangementRessource}
          placeholder="Ex: Chapitre 6 - Les arbres"
          requis
        />
        <ChampFormulaire
          label="Type de ressource"
          nom="typeRessource"
          type="select"
          valeur={formulaireRessource.typeRessource}
          onChange={gererChangementRessource}
          options={typesRessource}
          requis
        />
        <div className="champ-formulaire">
          <label htmlFor="fichier-ressource" className="champ-formulaire-label">
            Fichier *
          </label>
          <div 
            className={`upload-zone ${dragActive ? 'upload-zone-active' : ''} ${fichierSelectionne ? 'upload-zone-has-file' : ''}`}
            onDragOver={gererDragOver}
            onDragLeave={gererDragLeave}
            onDrop={gererDrop}
          >
            <input
              type="file"
              id="fichier-ressource"
              className="upload-input-hidden"
              onChange={gererSelectionFichier}
              accept=".pdf,.doc,.docx,.mp4,.avi,.mov,.zip"
            />
            {!fichierSelectionne ? (
              <label htmlFor="fichier-ressource" className="upload-zone-label">
                <Upload size={32} className="upload-zone-icone" />
                <span className="upload-zone-texte-principal">Cliquez pour uploader</span>
                <span className="upload-zone-texte-secondaire">ou glissez-déposez votre fichier ici</span>
                <span className="upload-zone-formats">PDF, DOC, MP4, ZIP (Max 50MB)</span>
              </label>
            ) : (
              <div className="upload-zone-file-selected">
                <File size={24} />
                <div className="upload-zone-file-info">
                  <span className="upload-zone-file-name">{fichierSelectionne.name}</span>
                  <span className="upload-zone-file-size">
                    {(fichierSelectionne.size / (1024 * 1024)).toFixed(2)} MB
                  </span>
                </div>
                <button
                  type="button"
                  className="upload-zone-remove"
                  onClick={() => {
                    setFichierSelectionne(null);
                    setFormulaireRessource(prev => ({ ...prev, fichier: null }));
                  }}
                >
                  ×
                </button>
              </div>
            )}
          </div>
        </div>
        <ChampFormulaire
          label="Description (optionnel)"
          nom="description"
          type="textarea"
          valeur={formulaireRessource.description}
          onChange={gererChangementRessource}
          placeholder="Ajoutez des notes sur cette ressource..."
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionCoursProfesseur;
