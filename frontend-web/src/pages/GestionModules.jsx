import { useState, useEffect } from 'react';
import { BookOpen, GraduationCap, FileText, ArrowLeft } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BoutonAction from '../composants/communs/BoutonAction';
import { apiStructure } from '../services/apiStructure';
import './GestionModules.css';

const GestionModules = ({ classeId, classeNom, filiereCode, filiereNom, onRetour }) => {
  const [modules, setModules] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [filieres, setFilieres] = useState([]);

  // Charger les modules depuis l'API
  const chargerModules = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = filiereCode
        ? await apiStructure.obtenirModulesParFiliere(filiereCode)
        : await apiStructure.obtenirTousModules();
      console.log('Modules reçus:', donnees);

      // Mapper les données backend vers le format frontend
      const modulesMapped = donnees.map(m => ({
        id: m.id,
        codeModule: m.codeModule,
        nom: m.nom,
        description: m.description || '',
        semestre: m.semestre || '',
        filiere: m.codeFiliere || '',
        classeId: m.classeId || m.codeClasse || null,
        codeClasse: m.codeClasse || null,
        nombreMatieres: 0, // TODO: compter depuis les matières
        nombreHeures: 0 // TODO: calculer depuis les matières
      }));

      setModules(modulesMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des modules:', err);
      setErreur('Erreur lors du chargement des modules');
    } finally {
      setChargement(false);
    }
  };

  // Charger les filières pour le sélecteur
  const chargerFilieres = async () => {
    try {
      const donnees = await apiStructure.obtenirToutesFilieres();
      const filieresMapped = donnees.map(f => ({
        value: f.codeFiliere,
        label: f.nom || f.codeFiliere
      }));
      setFilieres(filieresMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des filières:', err);
      setFilieres([]);
    }
  };

  useEffect(() => {
    chargerModules();
    // Always load filieres for the dropdown, regardless of classeId
    chargerFilieres();
  }, [classeId, filiereCode]);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [moduleCourant, setModuleCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    codeModule: '',
    nom: '',
    description: '',
    semestre: '',
    filiere: filiereCode || '',
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
      filiere: filiereCode || '',
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
      filiere: module.filiere,
      nombreHeures: module.nombreHeures.toString()
    });
    setModalOuvert(true);
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarder = async () => {
    // Validation basique
    if (!formulaire.codeModule || !formulaire.nom || !(formulaire.filiere || filiereCode) || !formulaire.semestre) {
      alert('Veuillez remplir tous les champs obligatoires');
      console.log('Formulaire validation failed:', formulaire);
      return;
    }

    setChargement(true);
    try {
      // Prepare data for backend - map frontend field names to backend field names
      const moduleData = {
        codeModule: formulaire.codeModule,
        nom: formulaire.nom,
        description: formulaire.description,
        semestre: formulaire.semestre,
        codeFiliere: formulaire.filiere || filiereCode,
      };

      console.log('Sending module data:', moduleData);

      if (moduleCourant) {
        // Update existing module
        await apiStructure.modifierModule(moduleCourant.codeModule, moduleData);
      } else {
        // Create new module
        await apiStructure.creerModule(moduleData);
      }

      // Refresh the modules list
      await chargerModules();
      setModalOuvert(false);
    } catch (err) {
      console.error('Save failed:', err);
      alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
    } finally {
      setChargement(false);
    }
  };

  const supprimer = async (module) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le module "${module.nom}" ?`)) {
      setChargement(true);
      try {
        await apiStructure.supprimerModule(module.codeModule);
        await chargerModules();
      } catch (err) {
        console.error('Delete failed:', err);
        alert('Une erreur est survenue lors de la suppression: ' + err.message);
      } finally {
        setChargement(false);
      }
    }
  };

  const colonnes = [
    { key: 'codeModule', label: 'Code' },
    { key: 'nom', label: 'Nom' },
    { key: 'filiere', label: 'Filière' },
    { key: 'semestre', label: 'Semestre' },
    { key: 'nombreHeures', label: 'Heures' },
    { key: 'nombreMatieres', label: 'Matières' }
  ];

  const modulesFiltres = modules;

  const totalModules = modulesFiltres.length;
  const totalHeures = modulesFiltres.reduce((sum, m) => sum + m.nombreHeures, 0);
  

  return (
    <div className="gestion-modules">
      <div className="gestion-modules-header">
        <div className="gestion-modules-header-gauche">
          {onRetour && (
            <BoutonAction
              libelle="Retour"
              variante="secondaire"
              taille="moyen"
              icone={ArrowLeft}
              onClick={onRetour}
            />
          )}
          <div>
            <h1 className="gestion-modules-titre">Gestion des Modules</h1>
            <p className="gestion-modules-sous-titre">
              {classeNom
                ? `${classeNom} - Modules`
                : filiereNom
                  ? `${filiereNom} - Modules`
                  : 'Gestion des cours et modules académiques'}
            </p>
          </div>
        </div>
      </div>

      <div className="gestion-modules-statistiques">
        <CarteStatistique
          titre="Total Modules"
          valeur={totalModules}
          variation={{ valeur: 5, label: '', positif: true }}
          icone={BookOpen}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Total Heures"
          valeur={totalHeures}
          variation={{ valeur: totalHeures, label: '', positif: true }}
          icone={FileText}
          couleur="info"
          iconeCouleur="light"
        />
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des modules...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerModules} className="btn-reessayer">Réessayer</button>
        </div>
      ) : (
        <TableauDonnees
          titre="Liste des Modules"
          colonnes={colonnes}
          donnees={modulesFiltres}
          surAjouter={ouvrirModalAjout}
          surModifier={ouvrirModalModification}
          surSupprimer={supprimer}
        />
      )}

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

