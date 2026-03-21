import { useState, useEffect } from 'react';
import { GraduationCap, Building2 } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import { apiStructure } from '../services/apiStructure';
import './GestionFilieres.css';

const GestionFilieres = ({ surVoirClasses }) => {
  const [filieres, setFilieres] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [departements, setDepartements] = useState([]);

  // Charger les filières depuis l'API
  const chargerFilieres = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = await apiStructure.obtenirToutesFilieres();
      console.log('Filières reçues:', donnees);

      // Mapper les données backend vers le format frontend
      const filieresMapped = donnees.map(f => ({
        id: f.id,
        codeFiliere: f.codeFiliere,
        nom: f.nom,
        description: f.description || '',
        dureeFormation: f.dureeFormation || 3,
        niveauDiplome: f.niveauDiplome || 'Licence',
        etat: f.etat || 'OUVERTE',
        departement: f.codeDepartement || '',
        nombreEtudiants: 0, // TODO: compter depuis les classes
        nombreModules: 0 // TODO: compter depuis les modules
      }));

      setFilieres(filieresMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des filières:', err);
      setErreur('Erreur lors du chargement des filières');
    } finally {
      setChargement(false);
    }
  };

  // Charger les départements pour le sélecteur
  const chargerDepartements = async () => {
    try {
      const donnees = await apiStructure.obtenirTousDepartements();
      const departementsMapped = donnees.map(d => ({
        value: d.codeDepartement,
        label: d.typeDepartement || d.codeDepartement
      }));
      setDepartements(departementsMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des départements:', err);
      setDepartements([]);
    }
  };

  useEffect(() => {
    chargerFilieres();
    chargerDepartements();
  }, []);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [filiereCourante, setFiliereCourante] = useState(null);
  const [isEditing, setIsEditing] = useState(false);
  const [formulaire, setFormulaire] = useState({
    codeFiliere: '',
    nom: '',
    description: '',
    dureeFormation: '',
    niveauDiplome: '',
    etat: 'OUVERTE',
    departement: ''
  });

  const niveauxDiplome = [
    { value: 'Licence', label: 'Licence' },
    { value: 'Master', label: 'Master' },
    { value: 'Doctorat', label: 'Doctorat' }
  ];

  const etats = [
    { value: 'OUVERTE', label: 'Ouverte' },
    { value: 'FERMEE', label: 'Fermée' }
  ];

  const ouvrirModalAjout = () => {
    setFiliereCourante(null);
    setIsEditing(false);
    setFormulaire({
      codeFiliere: '',
      nom: '',
      description: '',
      dureeFormation: '',
      niveauDiplome: '',
      etat: 'OUVERTE',
      departement: ''
    });
    setModalOuvert(true);
  };

  const ouvrirModalModification = (filiere) => {
    setFiliereCourante(filiere);
    setIsEditing(true);
    setFormulaire({
      codeFiliere: filiere.codeFiliere,
      nom: filiere.nom,
      description: filiere.description,
      dureeFormation: filiere.dureeFormation.toString(),
      niveauDiplome: filiere.niveauDiplome,
      etat: filiere.etat,
      departement: filiere.departement
    });
    setModalOuvert(true);
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarder = () => {
    // Validation basique
    if (!formulaire.codeFiliere || !formulaire.nom || !formulaire.departement || !formulaire.niveauDiplome) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (filiereCourante) {
      // Modification
      setFilieres(prev => prev.map(f =>
        f.id === filiereCourante.id
          ? { ...f, ...formulaire, dureeFormation: parseInt(formulaire.dureeFormation) }
          : f
      ));
    } else {
      // Ajout
      const nouvelleFiliere = {
        id: filieres.length + 1,
        ...formulaire,
        dureeFormation: parseInt(formulaire.dureeFormation),
        nombreEtudiants: 0,
        nombreModules: 0
      };
      setFilieres(prev => [...prev, nouvelleFiliere]);
    }
    setModalOuvert(false);
  };

  const supprimer = (filiere) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer la filière "${filiere.nom}" ?`)) {
      setFilieres(prev => prev.filter(f => f.id !== filiere.id));
    }
  };

  const colonnes = [
    { key: 'codeFiliere', label: 'Code' },
    { key: 'nom', label: 'Nom' },
    {
      key: 'departement',
      label: 'Département',
      render: (item) => (
        <div className="cellule-departement">
          <Building2 size={16} />
          <span>{item.departement}</span>
        </div>
      )
    },
    { key: 'niveauDiplome', label: 'Niveau' },
    { key: 'dureeFormation', label: 'Durée (ans)' },
    {
      key: 'etat',
      label: 'État',
      render: (item) => (
        <BadgeStatut
          statut={item.etat === 'OUVERTE' ? 'success' : 'error'}
          texte={item.etat}
        />
      )
    },
    { key: 'nombreEtudiants', label: 'Étudiants' },
    { key: 'nombreModules', label: 'Modules' }
  ];

  const totalFilieres = filieres.length;
  const filieresOuvertes = filieres.filter(f => f.etat === 'OUVERTE').length;
  const totalEtudiants = filieres.reduce((sum, f) => sum + f.nombreEtudiants, 0);

  return (
    <div className="gestion-filieres">
      <div className="gestion-filieres-header">
        <h1 className="gestion-filieres-titre">Gestion des Filières</h1>
        <p className="gestion-filieres-sous-titre">CRUD complet des filières académiques</p>
      </div>

      <div className="gestion-filieres-statistiques">
        <CarteStatistique
          titre="Total Filières"
          valeur={totalFilieres}
          variation={{ valeur: 2, label: '', positif: true }}
          icone={GraduationCap}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Filières Ouvertes"
          valeur={filieresOuvertes}
          variation={{ valeur: filieresOuvertes, label: '', positif: true }}
          icone={GraduationCap}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Total Étudiants"
          valeur={totalEtudiants}
          variation={{ valeur: 45, label: '', positif: true }}
          icone={GraduationCap}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des filières...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerFilieres} className="btn-reessayer">Réessayer</button>
        </div>
      ) : (
        <TableauDonnees
          titre="Liste des Filières"
          colonnes={colonnes}
          donnees={filieres}
          surAjouter={ouvrirModalAjout}
          surModifier={ouvrirModalModification}
          surSupprimer={supprimer}
          surVoir={surVoirClasses}
        />
      )}

      <FormulaireModal
        titre={filiereCourante ? 'Modifier la Filière' : 'Ajouter une Filière'}
        estOuvert={modalOuvert}
        surFermer={() => {
          setModalOuvert(false);
          setIsEditing(false);
          setFiliereCourante(null);
        }}
        surSauvegarder={sauvegarder}
      >
      {!isEditing && (
        <ChampFormulaire
          label="Code Filière"
          nom="codeFiliere"
          valeur={formulaire.codeFiliere}
          onChange={gererChangement}
          placeholder="Ex: GI-2024"
          requis
        />
      )}
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaire.nom}
          onChange={gererChangement}
          placeholder="Ex: Génie Informatique"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaire.description}
          onChange={gererChangement}
          placeholder="Description de la filière"
        />
        <ChampFormulaire
          label="Département"
          nom="departement"
          type="select"
          valeur={formulaire.departement}
          onChange={gererChangement}
          options={departements}
          requis
        />
        <ChampFormulaire
          label="Niveau Diplôme"
          nom="niveauDiplome"
          type="select"
          valeur={formulaire.niveauDiplome}
          onChange={gererChangement}
          options={niveauxDiplome}
          requis
        />
        <ChampFormulaire
          label="Durée de Formation (années)"
          nom="dureeFormation"
          type="number"
          valeur={formulaire.dureeFormation}
          onChange={gererChangement}
          requis
        />
        <ChampFormulaire
          label="État"
          nom="etat"
          type="select"
          valeur={formulaire.etat}
          onChange={gererChangement}
          options={etats}
          requis
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionFilieres;

