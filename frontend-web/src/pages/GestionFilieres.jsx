import { useState } from 'react';
import { GraduationCap, Building2 } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import './GestionFilieres.css';

const GestionFilieres = () => {
  const [filieres, setFilieres] = useState([
    {
      id: 1,
      codeFiliere: 'GI-2024',
      nom: 'Génie Informatique',
      description: 'Formation en informatique et génie logiciel',
      dureeFormation: 3,
      niveauDiplome: 'Licence',
      etat: 'OUVERTE',
      departement: 'Informatique',
      nombreEtudiants: 245,
      nombreModules: 12
    },
    {
      id: 2,
      codeFiliere: 'GIL-2024',
      nom: 'Génie Industriel et Logistique',
      description: 'Formation en industrie et logistique',
      dureeFormation: 3,
      niveauDiplome: 'Licence',
      etat: 'OUVERTE',
      departement: 'Industriel',
      nombreEtudiants: 180,
      nombreModules: 10
    },
    {
      id: 3,
      codeFiliere: 'GC-2024',
      nom: 'Génie Civil',
      description: 'Formation en génie civil et construction',
      dureeFormation: 3,
      niveauDiplome: 'Licence',
      etat: 'FERMEE',
      departement: 'Civil',
      nombreEtudiants: 0,
      nombreModules: 8
    }
  ]);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [filiereCourante, setFiliereCourante] = useState(null);
  const [formulaire, setFormulaire] = useState({
    codeFiliere: '',
    nom: '',
    description: '',
    dureeFormation: '',
    niveauDiplome: '',
    etat: 'OUVERTE',
    departement: ''
  });

  const departements = [
    { value: 'Informatique', label: 'Informatique' },
    { value: 'Industriel', label: 'Industriel' },
    { value: 'Civil', label: 'Génie Civil' },
    { value: 'Mecanique', label: 'Mécanique' }
  ];

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
          variation={{ valeur: 2, label: 'cette année', positif: true }}
          icone={GraduationCap}
          couleur="primary"
        />
        <CarteStatistique
          titre="Filières Ouvertes"
          valeur={filieresOuvertes}
          variation={{ valeur: filieresOuvertes, label: 'actives', positif: true }}
          icone={GraduationCap}
          couleur="success"
        />
        <CarteStatistique
          titre="Total Étudiants"
          valeur={totalEtudiants}
          variation={{ valeur: 45, label: 'nouveaux', positif: true }}
          icone={GraduationCap}
          couleur="info"
        />
      </div>

      <TableauDonnees
        titre="Liste des Filières"
        colonnes={colonnes}
        donnees={filieres}
        surAjouter={ouvrirModalAjout}
        surModifier={ouvrirModalModification}
        surSupprimer={supprimer}
      />

      <FormulaireModal
        titre={filiereCourante ? 'Modifier la Filière' : 'Ajouter une Filière'}
        estOuvert={modalOuvert}
        surFermer={() => setModalOuvert(false)}
        surSauvegarder={sauvegarder}
      >
        <ChampFormulaire
          label="Code Filière"
          nom="codeFiliere"
          valeur={formulaire.codeFiliere}
          onChange={gererChangement}
          placeholder="Ex: GI-2024"
          requis
        />
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

