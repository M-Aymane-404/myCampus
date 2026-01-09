import { useState } from 'react';
import { BookOpen, GraduationCap, FileText } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import './GestionModules.css';

const GestionModules = () => {
  const [modules, setModules] = useState([
    {
      id: 1,
      codeModule: 'GI-INFO-101',
      nom: 'Algorithmique et Structures de Données',
      description: 'Introduction à l\'algorithmique et aux structures de données',
      semestre: 'S1',
      coefficient: 4.0,
      filiere: 'GI-2024',
      nombreMatieres: 3,
      nombreHeures: 60
    },
    {
      id: 2,
      codeModule: 'GI-INFO-102',
      nom: 'Programmation Orientée Objet',
      description: 'Concepts de la programmation orientée objet',
      semestre: 'S2',
      coefficient: 5.0,
      filiere: 'GI-2024',
      nombreMatieres: 2,
      nombreHeures: 75
    },
    {
      id: 3,
      codeModule: 'GIL-IND-201',
      nom: 'Gestion de Production',
      description: 'Techniques de gestion de production industrielle',
      semestre: 'S3',
      coefficient: 4.5,
      filiere: 'GIL-2024',
      nombreMatieres: 4,
      nombreHeures: 90
    }
  ]);

  const [filieres] = useState([
    { value: 'GI-2024', label: 'Génie Informatique' },
    { value: 'GIL-2024', label: 'Génie Industriel et Logistique' },
    { value: 'GC-2024', label: 'Génie Civil' }
  ]);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [moduleCourant, setModuleCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    codeModule: '',
    nom: '',
    description: '',
    semestre: '',
    coefficient: '',
    filiere: '',
    nombreHeures: ''
  });

  const semestres = [
    { value: 'S1', label: 'Semestre 1' },
    { value: 'S2', label: 'Semestre 2' },
    { value: 'S3', label: 'Semestre 3' },
    { value: 'S4', label: 'Semestre 4' },
    { value: 'S5', label: 'Semestre 5' },
    { value: 'S6', label: 'Semestre 6' }
  ];

  const ouvrirModalAjout = () => {
    setModuleCourant(null);
    setFormulaire({
      codeModule: '',
      nom: '',
      description: '',
      semestre: '',
      coefficient: '',
      filiere: '',
      nombreHeures: ''
    });
    setModalOuvert(true);
  };

  const ouvrirModalModification = (module) => {
    setModuleCourant(module);
    setFormulaire({
      codeModule: module.codeModule,
      nom: module.nom,
      description: module.description,
      semestre: module.semestre,
      coefficient: module.coefficient.toString(),
      filiere: module.filiere,
      nombreHeures: module.nombreHeures.toString()
    });
    setModalOuvert(true);
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarder = () => {
    // Validation basique
    if (!formulaire.codeModule || !formulaire.nom || !formulaire.filiere || !formulaire.semestre) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (moduleCourant) {
      setModules(prev => prev.map(m => 
        m.id === moduleCourant.id 
          ? { 
              ...m, 
              ...formulaire, 
              coefficient: parseFloat(formulaire.coefficient),
              nombreHeures: parseInt(formulaire.nombreHeures),
              nombreMatieres: m.nombreMatieres
            }
          : m
      ));
    } else {
      const nouveauModule = {
        id: modules.length + 1,
        ...formulaire,
        coefficient: parseFloat(formulaire.coefficient),
        nombreHeures: parseInt(formulaire.nombreHeures),
        nombreMatieres: 0
      };
      setModules(prev => [...prev, nouveauModule]);
    }
    setModalOuvert(false);
  };

  const supprimer = (module) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le module "${module.nom}" ?`)) {
      setModules(prev => prev.filter(m => m.id !== module.id));
    }
  };

  const colonnes = [
    { key: 'codeModule', label: 'Code' },
    { key: 'nom', label: 'Nom' },
    { key: 'filiere', label: 'Filière' },
    { key: 'semestre', label: 'Semestre' },
    { key: 'coefficient', label: 'Coefficient' },
    { key: 'nombreHeures', label: 'Heures' },
    { key: 'nombreMatieres', label: 'Matières' }
  ];

  const totalModules = modules.length;
  const totalHeures = modules.reduce((sum, m) => sum + m.nombreHeures, 0);
  const moyenneCoefficient = modules.length > 0 
    ? (modules.reduce((sum, m) => sum + m.coefficient, 0) / modules.length).toFixed(2)
    : 0;

  return (
    <div className="gestion-modules">
      <div className="gestion-modules-header">
        <h1 className="gestion-modules-titre">Gestion des Modules</h1>
        <p className="gestion-modules-sous-titre">Gestion des cours et modules académiques</p>
      </div>

      <div className="gestion-modules-statistiques">
        <CarteStatistique
          titre="Total Modules"
          valeur={totalModules}
          variation={{ valeur: 5, label: 'cette année', positif: true }}
          icone={BookOpen}
          couleur="primary"
        />
        <CarteStatistique
          titre="Total Heures"
          valeur={totalHeures}
          variation={{ valeur: totalHeures, label: 'heures', positif: true }}
          icone={FileText}
          couleur="info"
        />
        <CarteStatistique
          titre="Coefficient Moyen"
          valeur={moyenneCoefficient}
          variation={{ valeur: moyenneCoefficient, label: 'moyenne', positif: true }}
          icone={GraduationCap}
          couleur="success"
        />
      </div>

      <TableauDonnees
        titre="Liste des Modules"
        colonnes={colonnes}
        donnees={modules}
        surAjouter={ouvrirModalAjout}
        surModifier={ouvrirModalModification}
        surSupprimer={supprimer}
      />

      <FormulaireModal
        titre={moduleCourant ? 'Modifier le Module' : 'Ajouter un Module'}
        estOuvert={modalOuvert}
        surFermer={() => setModalOuvert(false)}
        surSauvegarder={sauvegarder}
      >
        <ChampFormulaire
          label="Code Module"
          nom="codeModule"
          valeur={formulaire.codeModule}
          onChange={gererChangement}
          placeholder="Ex: GI-INFO-101"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaire.nom}
          onChange={gererChangement}
          placeholder="Nom du module"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaire.description}
          onChange={gererChangement}
          placeholder="Description du module"
        />
        <ChampFormulaire
          label="Filière"
          nom="filiere"
          type="select"
          valeur={formulaire.filiere}
          onChange={gererChangement}
          options={filieres}
          requis
        />
        <ChampFormulaire
          label="Semestre"
          nom="semestre"
          type="select"
          valeur={formulaire.semestre}
          onChange={gererChangement}
          options={semestres}
          requis
        />
        <ChampFormulaire
          label="Coefficient"
          nom="coefficient"
          type="number"
          step="0.5"
          valeur={formulaire.coefficient}
          onChange={gererChangement}
          placeholder="Ex: 4.0"
          requis
        />
        <ChampFormulaire
          label="Nombre d'Heures"
          nom="nombreHeures"
          type="number"
          valeur={formulaire.nombreHeures}
          onChange={gererChangement}
          placeholder="Ex: 60"
          requis
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionModules;

