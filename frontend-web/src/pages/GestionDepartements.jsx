import { useState, useEffect } from 'react';
import { Building2, Users, GraduationCap, UserPlus, Plus, Monitor, Briefcase, BarChart3, Edit, Eye, ArrowLeft, BookOpen, FileText, Trash2 } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import BoutonAction from '../composants/communs/BoutonAction';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import './GestionDepartements.css';

const GestionDepartements = () => {
  const [departements, setDepartements] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  const [professeurs, setProfesseurs] = useState([]);

  const [filieres, setFilieres] = useState([]);

  const [modalAjoutDepartement, setModalAjoutDepartement] = useState(false);
  const [modalAffecterProf, setModalAffecterProf] = useState(false);
  const [modalAjouterFiliere, setModalAjouterFiliere] = useState(false);
  const [departementSelectionne, setDepartementSelectionne] = useState(null);
  const [departementCourant, setDepartementCourant] = useState(null);
  const [departementDetails, setDepartementDetails] = useState(null);
  const [filiereDetails, setFiliereDetails] = useState(null);
  const [moduleDetails, setModuleDetails] = useState(null);

  // États pour la modification
  const [modalModifierFiliere, setModalModifierFiliere] = useState(false);
  const [modalModifierModule, setModalModifierModule] = useState(false);
  const [modalModifierMatiere, setModalModifierMatiere] = useState(false);
  const [filiereCourant, setFiliereCourant] = useState(null);
  const [moduleCourant, setModuleCourant] = useState(null);
  const [matiereCourante, setMatiereCourante] = useState(null);

  // États pour l'ajout
  const [modalAjouterModule, setModalAjouterModule] = useState(false);
  const [modalAjouterMatiere, setModalAjouterMatiere] = useState(false);
  const [filierePourModule, setFilierePourModule] = useState(null);
  const [modulePourMatiere, setModulePourMatiere] = useState(null);

  const [modules, setModules] = useState([]);

  const [matieres, setMatieres] = useState([]);

  const [formulaireDepartement, setFormulaireDepartement] = useState({
    nom: '',
    code: '',
    description: '',
    chefDepartement: ''
  });
  const [formulaireFiliere, setFormulaireFiliere] = useState({
    codeFiliere: '',
    nom: '',
    etat: 'OUVERTE',
    chefFiliere: '',
    niveauDiplome: '',
    dureeFormation: '',
    description: ''
  });
  const [formulaireModule, setFormulaireModule] = useState({
    codeModule: '',
    nom: '',
    description: '',
    semestre: '',
    coefficient: '',
    filiere: '',
    nombreHeures: '',
    professeur: ''
  });
  const [formulaireMatiere, setFormulaireMatiere] = useState({
    codeMatiere: '',
    nom: '',
    module: '',
    nombreHeures: '',
    coefficient: '',
    professeur: '',
    type: '',
    description: ''
  });

  // Charger toutes les données au démarrage
  useEffect(() => {
    chargerDonneesGlobales();
  }, []);

  const chargerDonneesGlobales = async () => {
    try {
      setChargement(true);
      setErreur(null);

      // 1. Récupération parallèle de TOUTES les données
      const [
        departementsData,
        filieresData,
        modulesData,
        matieresData,
        professeursData,
        etudiantsData
      ] = await Promise.all([
        apiStructure.obtenirTousDepartements(),
        apiStructure.obtenirToutesFilieres(),
        apiStructure.obtenirTousModules(),
        apiStructure.obtenirToutesMatieres(),
        apiProfesseurs.obtenirTous().catch(() => []),
        // On pourrait utiliser apiComptes.obtenirTous() mais on filterait, 
        // ou un endpoint spécifique si disponible. Pour l'instant on suppose un tableau vide si erreur.
        fetch('http://localhost:8080/api/admin/utilisateurs/etudiants').then(r => r.json()).catch(() => [])
      ]);

      console.log('--- Données Globales Reçues ---');
      console.log('Départements:', departementsData);
      console.log('Filières:', filieresData);
      console.log('Modules:', modulesData);
      console.log('Matières:', matieresData);
      console.log('Professeurs:', professeursData);

      // 2. Création des Maps pour accès rapide

      // Map PPR -> Nom Complet Professeur
      const professeursMap = {};
      const professeursParDept = {}; // Code Dept -> Count
      const matiereResponsableMap = {}; // Nom Matière -> Nom Professeur (Reverse lookup)
      if (Array.isArray(professeursData)) {
        setProfesseurs(professeursData); // Mettre à jour l'état des professeurs
        professeursData.forEach(p => {
          if (p.ppr) {
            const nomComplet = `${p.prenom || ''} ${p.nom || ''}`.trim();
            professeursMap[p.ppr] = nomComplet;

            // Compter profs par département
            const dept = p.codeDepartementOuTravail || p.departement;
            if (dept) {
              professeursParDept[dept] = (professeursParDept[dept] || 0) + 1;
            }

            // Reverse mapping pour les matières
            if (Array.isArray(p.matieresEnseignees)) {
              p.matieresEnseignees.forEach(matiereNom => {
                matiereResponsableMap[matiereNom] = nomComplet;
              });
            }
          }
        });
      }

      // 3. Traitement des Matières
      const matieresTraitees = matieresData.map(m => ({
        id: m.id,
        codeMatiere: m.codeMatiere || '',
        nom: m.nom,
        module: m.codeModule || '',
        nombreHeures: m.volumeHoraire || 0, // Adapter selon DTO
        coefficient: m.coefficient || 0,
        // Essayer de trouver le prof via reverse mapping sur le nom de la matière
        professeur: matiereResponsableMap[m.nom] || 'Non défini',
        type: 'Cours', // TODO: Typologie à affiner
        description: m.description || ''
      }));
      setMatieres(matieresTraitees);

      // 4. Traitement des Modules
      const modulesTraites = modulesData.map(m => {
        // Calcul aggrégats depuis les matières
        const matieresDuModule = matieresTraitees.filter(mat => mat.module === m.codeModule);
        const heuresTotales = matieresDuModule.reduce((acc, mat) => acc + (mat.nombreHeures || 0), 0);

        return {
          id: m.id,
          codeModule: m.codeModule,
          nom: m.nom,
          description: m.description || '',
          semestre: m.semestre || '',
          coefficient: m.coefficient || 0,
          filiere: m.codeFiliere || '',
          nombreMatieres: matieresDuModule.length,
          nombreHeures: heuresTotales,
          professeur: m.pprResponsable ? (professeursMap[m.pprResponsable] || m.pprResponsable) : 'Non défini'
        };
      });
      setModules(modulesTraites);

      // 5. Traitement des Filières
      const filieresTraitees = filieresData.map(f => {
        // Calcul aggrégats depuis les modules
        const modulesDeLaFiliere = modulesTraites.filter(mod => mod.filiere === f.codeFiliere);

        // Est-ce qu'on peut lier étudiants -> filière ?
        // Supposons que l'étudiant a un champ 'filiere' ou 'codeFiliere'
        const etudiantsDeLaFiliere = Array.isArray(etudiantsData)
          ? etudiantsData.filter(e => e.codeFiliere === f.codeFiliere || e.filiere === f.nom).length
          : 0;

        return {
          id: f.id,
          codeFiliere: f.codeFiliere,
          nom: f.nom,
          departement: f.codeDepartement || '',
          etat: f.etat || 'OUVERTE',
          chefFiliere: f.chefFilierePpr ? (professeursMap[f.chefFilierePpr] || f.chefFilierePpr) : 'Non défini',
          nombreEtudiants: etudiantsDeLaFiliere,
          nombreModules: modulesDeLaFiliere.length,
          niveauDiplome: f.niveauDiplome || 'Licence',
          dureeFormation: f.dureeFormation || 3,
          description: f.description || ''
        };
      });
      setFilieres(filieresTraitees);

      // 6. Traitement des Départements (Top Level)
      const departementsTraites = departementsData.map(d => {
        const filieresDuDept = filieresTraitees.filter(f => f.departement === d.codeDepartement);
        const totalEtudiants = filieresDuDept.reduce((acc, f) => acc + f.nombreEtudiants, 0);

        // Essayer de trouver le nombre de profs via map ou nom
        const nbProfs = professeursParDept[d.codeDepartement] || professeursParDept[d.typeDepartement] || 0;

        return {
          id: d.id,
          nom: d.typeDepartement || d.codeDepartement,
          code: d.codeDepartement,
          description: d.description || '',
          chefDepartement: d.chefDepartementPpr ? (professeursMap[d.chefDepartementPpr] || d.chefDepartementPpr) : 'Non défini',
          nombreProfesseurs: nbProfs,
          nombreFilieres: filieresDuDept.length,
          nombreEtudiants: totalEtudiants
        };
      });
      setDepartements(departementsTraites);

    } catch (err) {
      console.error('Erreur chargement global:', err);
      setErreur('Erreur lors du chargement des données.');
    } finally {
      setChargement(false);
    }
  };


  const ouvrirModalAjoutDepartement = () => {
    setDepartementCourant(null);
    setFormulaireDepartement({
      nom: '',
      code: '',
      description: '',
      chefDepartement: ''
    });
    setModalAjoutDepartement(true);
  };

  const ouvrirModalModification = (departement) => {
    setDepartementCourant(departement);
    setFormulaireDepartement({
      nom: departement.nom,
      code: departement.code,
      description: departement.description || '',
      chefDepartement: departement.chefDepartement || ''
    });
    setModalAjoutDepartement(true);
  };

  const supprimer = (departement) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le département "${departement.nom}" ? Cette action est irréversible.`)) {
      setDepartements(prev => prev.filter(d => d.id !== departement.id));
    }
  };

  const ouvrirModalAffecterProf = (departement) => {
    setDepartementSelectionne(departement);
    setModalAffecterProf(true);
  };

  const ouvrirModalAjouterFiliere = (departement) => {
    setDepartementSelectionne(departement);
    setFormulaireFiliere({
      codeFiliere: '',
      nom: '',
      etat: 'OUVERTE'
    });
    setModalAjouterFiliere(true);
  };

  const gererChangementDepartement = (e) => {
    const { name, value } = e.target;
    setFormulaireDepartement(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementFiliere = (e) => {
    const { name, value } = e.target;
    setFormulaireFiliere(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementModule = (e) => {
    const { name, value } = e.target;
    setFormulaireModule(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementMatiere = (e) => {
    const { name, value } = e.target;
    setFormulaireMatiere(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarderDepartement = () => {
    if (!formulaireDepartement.nom || !formulaireDepartement.code) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (departementCourant) {
      // Modification
      setDepartements(prev => prev.map(d =>
        d.id === departementCourant.id
          ? { ...d, ...formulaireDepartement }
          : d
      ));
    } else {
      // Ajout
      const nouveauDepartement = {
        id: departements.length + 1,
        ...formulaireDepartement,
        nombreProfesseurs: 0,
        nombreFilieres: 0,
        nombreEtudiants: 0
      };
      setDepartements(prev => [...prev, nouveauDepartement]);
    }
    setModalAjoutDepartement(false);
  };

  const affecterProfesseur = (professeurId) => {
    if (!departementSelectionne) return;

    const professeur = professeurs.find(p => p.id === professeurId);
    if (!professeur) return;

    // Mettre à jour le département du professeur
    // Dans une vraie application, ceci serait fait via une API
    const departement = departements.find(d => d.id === departementSelectionne.id);
    if (departement) {
      setDepartements(prev => prev.map(d =>
        d.id === departement.id
          ? { ...d, nombreProfesseurs: d.nombreProfesseurs + 1 }
          : d
      ));
    }

    alert(`Professeur ${professeur.prenom} ${professeur.nom} affecté au département ${departementSelectionne.nom}`);
    setModalAffecterProf(false);
  };

  const sauvegarderFiliere = () => {
    if (!formulaireFiliere.codeFiliere || !formulaireFiliere.nom) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (filiereCourant) {
      // Modification
      setFilieres(prev => prev.map(f =>
        f.id === filiereCourant.id
          ? { ...f, ...formulaireFiliere, dureeFormation: parseInt(formulaireFiliere.dureeFormation) || f.dureeFormation }
          : f
      ));
      setFiliereCourant(null);
      setModalModifierFiliere(false);
    } else {
      // Ajout
      if (!departementSelectionne) {
        alert('Veuillez sélectionner un département');
        return;
      }
      const nouvelleFiliere = {
        id: filieres.length + 1,
        ...formulaireFiliere,
        departement: departementSelectionne.nom,
        dureeFormation: parseInt(formulaireFiliere.dureeFormation) || 3,
        nombreEtudiants: 0,
        nombreModules: 0
      };
      setFilieres(prev => [...prev, nouvelleFiliere]);
      // Mettre à jour le nombre de filières du département
      setDepartements(prev => prev.map(d =>
        d.id === departementSelectionne.id
          ? { ...d, nombreFilieres: d.nombreFilieres + 1 }
          : d
      ));
      setModalAjouterFiliere(false);
    }
  };

  const ouvrirModalModifierFiliere = (filiere) => {
    setFiliereCourant(filiere);
    const dep = departements.find(d => d.nom === filiere.departement);
    if (dep) setDepartementSelectionne(dep);
    setFormulaireFiliere({
      codeFiliere: filiere.codeFiliere,
      nom: filiere.nom,
      etat: filiere.etat,
      chefFiliere: filiere.chefFiliere || '',
      niveauDiplome: filiere.niveauDiplome || '',
      dureeFormation: filiere.dureeFormation?.toString() || '',
      description: filiere.description || ''
    });
    setModalModifierFiliere(true);
  };

  const ouvrirModalModifierModule = (module) => {
    setModuleCourant(module);
    setFormulaireModule({
      codeModule: module.codeModule,
      nom: module.nom,
      description: module.description || '',
      semestre: module.semestre || '',
      coefficient: module.coefficient?.toString() || '',
      filiere: module.filiere || '',
      nombreHeures: module.nombreHeures?.toString() || '',
      professeur: module.professeur || ''
    });
    setModalModifierModule(true);
  };

  const ouvrirModalModifierMatiere = (matiere) => {
    setMatiereCourante(matiere);
    setFormulaireMatiere({
      codeMatiere: matiere.codeMatiere,
      nom: matiere.nom,
      module: matiere.module || '',
      nombreHeures: matiere.nombreHeures?.toString() || '',
      coefficient: matiere.coefficient?.toString() || '',
      professeur: matiere.professeur || '',
      type: matiere.type || '',
      description: matiere.description || ''
    });
    setModalModifierMatiere(true);
  };

  const sauvegarderModule = () => {
    if (!formulaireModule.codeModule || !formulaireModule.nom) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (moduleCourant) {
      // Modification
      setModules(prev => prev.map(m =>
        m.id === moduleCourant.id
          ? { ...m, ...formulaireModule, coefficient: parseFloat(formulaireModule.coefficient) || m.coefficient, nombreHeures: parseInt(formulaireModule.nombreHeures) || m.nombreHeures }
          : m
      ));
      setModuleCourant(null);
      setModalModifierModule(false);
    } else {
      // Ajout
      if (!filierePourModule) {
        alert('Veuillez sélectionner une filière');
        return;
      }
      const nouveauModule = {
        id: modules.length + 1,
        ...formulaireModule,
        filiere: filierePourModule.codeFiliere,
        coefficient: parseFloat(formulaireModule.coefficient) || 1,
        nombreHeures: parseInt(formulaireModule.nombreHeures) || 0,
        nombreMatieres: 0
      };
      setModules(prev => [...prev, nouveauModule]);
      // Mettre à jour le nombre de modules de la filière
      setFilieres(prev => prev.map(f =>
        f.id === filierePourModule.id
          ? { ...f, nombreModules: (f.nombreModules || 0) + 1 }
          : f
      ));
      setModalAjouterModule(false);
    }
  };

  const sauvegarderMatiere = () => {
    if (!formulaireMatiere.codeMatiere || !formulaireMatiere.nom) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (matiereCourante) {
      // Modification
      setMatieres(prev => prev.map(m =>
        m.id === matiereCourante.id
          ? { ...m, ...formulaireMatiere, coefficient: parseFloat(formulaireMatiere.coefficient) || m.coefficient, nombreHeures: parseInt(formulaireMatiere.nombreHeures) || m.nombreHeures }
          : m
      ));
      setMatiereCourante(null);
      setModalModifierMatiere(false);
    } else {
      // Ajout
      if (!modulePourMatiere) {
        alert('Veuillez sélectionner un module');
        return;
      }
      const nouvelleMatiere = {
        id: matieres.length + 1,
        ...formulaireMatiere,
        module: modulePourMatiere.codeModule,
        coefficient: parseFloat(formulaireMatiere.coefficient) || 1,
        nombreHeures: parseInt(formulaireMatiere.nombreHeures) || 0
      };
      setMatieres(prev => [...prev, nouvelleMatiere]);
      // Mettre à jour le nombre de matières du module
      setModules(prev => prev.map(m =>
        m.codeModule === modulePourMatiere.codeModule
          ? { ...m, nombreMatieres: (m.nombreMatieres || 0) + 1 }
          : m
      ));
      setModalAjouterMatiere(false);
    }
  };

  const ouvrirModalAjouterModule = (filiere) => {
    setFilierePourModule(filiere);
    setFormulaireModule({
      codeModule: '',
      nom: '',
      description: '',
      semestre: '',
      coefficient: '',
      filiere: filiere.codeFiliere,
      nombreHeures: '',
      professeur: ''
    });
    setModuleCourant(null);
    setModalAjouterModule(true);
  };

  const ouvrirModalAjouterMatiere = (module) => {
    setModulePourMatiere(module);
    setFormulaireMatiere({
      codeMatiere: '',
      nom: '',
      module: module.codeModule,
      nombreHeures: '',
      coefficient: '',
      professeur: '',
      type: '',
      description: ''
    });
    setMatiereCourante(null);
    setModalAjouterMatiere(true);
  };

  const obtenirProfesseursDepartement = (departementNom) => {
    return professeurs.filter(p => p.departement === departementNom);
  };

  const obtenirFilieresDepartement = (departementCode) => {
    // Filtrer par code du département (ex: DEPT-INFO)
    return filieres.filter(f => f.departement === departementCode);
  };

  const obtenirModulesFiliere = (codeFiliere) => {
    return modules.filter(m => m.filiere === codeFiliere);
  };

  const obtenirMatieresModule = (codeModule) => {
    return matieres.filter(m => m.module === codeModule);
  };

  const professeursNonAffectes = professeurs.filter(p => !p.departement);

  const colonnes = [
    { key: 'code', label: 'Code' },
    { key: 'nom', label: 'Nom' },
    { key: 'chefDepartement', label: 'Chef de Département' },
    {
      key: 'nombreProfesseurs',
      label: 'Professeurs',
      render: (item) => `${item.nombreProfesseurs} professeur(s)`
    },
    {
      key: 'nombreFilieres',
      label: 'Filières',
      render: (item) => `${item.nombreFilieres} filière(s)`
    },
    {
      key: 'nombreEtudiants',
      label: 'Étudiants',
      render: (item) => `${item.nombreEtudiants} étudiant(s)`
    }
  ];

  const totalDepartements = departements.length;
  const totalProfesseurs = departements.reduce((sum, d) => sum + d.nombreProfesseurs, 0);
  const totalFilieres = departements.reduce((sum, d) => sum + d.nombreFilieres, 0);

  // Si on affiche les détails d'un module
  if (moduleDetails) {
    const matieresModule = obtenirMatieresModule(moduleDetails.codeModule);

    return (
      <div className="gestion-departements">
        <div className="details-departement-header">
          <button className="details-departement-btn-retour" onClick={() => {
            setModuleDetails(null);
          }}>
            <ArrowLeft size={20} />
            Retour
          </button>
          <div>
            <h1 className="details-departement-titre">{moduleDetails.nom}</h1>
            <p className="details-departement-sous-titre">{moduleDetails.description || `Module ${moduleDetails.nom}`}</p>
          </div>
        </div>

        <div className="details-departement-infos">
          <div className="details-departement-info-card">
            <h3>Informations du Module</h3>
            <div className="details-departement-info-grid">
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Code</span>
                <span className="details-departement-info-value">{moduleDetails.codeModule}</span>
              </div>

              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Semestre</span>
                <span className="details-departement-info-value">{moduleDetails.semestre || 'Non défini'}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Coefficient</span>
                <span className="details-departement-info-value">{moduleDetails.coefficient || 0}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre d'Heures</span>
                <span className="details-departement-info-value">{moduleDetails.nombreHeures || 0}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre de Matières</span>
                <span className="details-departement-info-value">{moduleDetails.nombreMatieres || 0}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="details-departement-filieres">
          <div className="details-departement-filieres-header">
            <h2 className="details-departement-filieres-titre">Matières du Module</h2>
            <button
              className="details-departement-btn-ajouter"
              onClick={() => ouvrirModalAjouterMatiere(moduleDetails)}
            >
              <Plus size={18} />
              Ajouter une Matière
            </button>
          </div>
          <div className="details-departement-filieres-cartes">
            {matieresModule.map(matiere => (
              <div key={matiere.id} className="details-filiere-carte">
                <div className="details-filiere-carte-header">
                  <div className="details-filiere-carte-icone">
                    <FileText size={24} />
                  </div>
                  <div className="details-filiere-carte-titre-container">
                    <h3 className="details-filiere-carte-titre">
                      {matiere.nom}
                    </h3>
                    <p className="details-filiere-carte-code">{matiere.codeMatiere}</p>
                  </div>
                </div>

                <div className="details-filiere-carte-infos">
                  <div className="details-filiere-carte-info-item-chef">
                    <span className="details-filiere-carte-info-label">Professeur</span>
                    <span className="details-filiere-carte-info-value">{matiere.professeur || 'Non défini'}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Heures</span>
                    <span className="details-filiere-carte-info-value">{matiere.nombreHeures || 0}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Type</span>
                    <span className="details-filiere-carte-info-value">{matiere.type || 'Non défini'}</span>
                  </div>
                </div>

                <div className="details-filiere-carte-contenu">
                  <div className="details-filiere-carte-details">
                    <div className="details-filiere-carte-detail-item">
                      <span className="details-filiere-carte-detail-label">Coefficient</span>
                      <span className="details-filiere-carte-detail-value">{matiere.coefficient || 0}</span>
                    </div>
                  </div>
                </div>

                <div className="details-filiere-carte-actions">
                  <button
                    className="details-filiere-carte-btn-modifier"
                    onClick={(e) => {
                      e.stopPropagation();
                      ouvrirModalModifierMatiere(matiere);
                    }}
                  >
                    Modifier
                  </button>
                  <button
                    className="details-filiere-carte-btn-supprimer"
                    onClick={(e) => {
                      e.stopPropagation();
                      supprimerMatiere(matiere);
                    }}
                  >
                    <Trash2 size={16} />
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Modals pour les matières et modules */}
        <FormulaireModal
          titre={matiereCourante ? 'Modifier la Matière' : `Ajouter une Matière - ${moduleDetails?.nom || ''}`}
          estOuvert={modalModifierMatiere || modalAjouterMatiere}
          surFermer={() => {
            setModalModifierMatiere(false);
            setModalAjouterMatiere(false);
            setMatiereCourante(null);
            setModulePourMatiere(null);
          }}
          surSauvegarder={sauvegarderMatiere}
        >
          <ChampFormulaire
            label="Code Matière"
            nom="codeMatiere"
            valeur={formulaireMatiere.codeMatiere}
            onChange={gererChangementMatiere}
            placeholder="Ex: ALG-101-1"
            requis
          />
          <ChampFormulaire
            label="Nom"
            nom="nom"
            valeur={formulaireMatiere.nom}
            onChange={gererChangementMatiere}
            placeholder="Nom de la matière"
            requis
          />
          <ChampFormulaire
            label="Module"
            nom="module"
            valeur={formulaireMatiere.module}
            onChange={gererChangementMatiere}
            placeholder="Code du module"
          />
          <ChampFormulaire
            label="Type"
            nom="type"
            type="select"
            valeur={formulaireMatiere.type}
            onChange={gererChangementMatiere}
            options={[
              { value: 'Cours', label: 'Cours' },
              { value: 'TP', label: 'TP' },
              { value: 'TD', label: 'TD' }
            ]}
          />
          <ChampFormulaire
            label="Nombre d'Heures"
            nom="nombreHeures"
            type="number"
            valeur={formulaireMatiere.nombreHeures}
            onChange={gererChangementMatiere}
            placeholder="Ex: 20"
          />
          <ChampFormulaire
            label="Coefficient"
            nom="coefficient"
            type="number"
            step="0.5"
            valeur={formulaireMatiere.coefficient}
            onChange={gererChangementMatiere}
            placeholder="Ex: 1.5"
          />
          <ChampFormulaire
            label="Professeur"
            nom="professeur"
            valeur={formulaireMatiere.professeur}
            onChange={gererChangementMatiere}
            placeholder="Nom du professeur"
          />
          <ChampFormulaire
            label="Description"
            nom="description"
            type="textarea"
            valeur={formulaireMatiere.description}
            onChange={gererChangementMatiere}
            placeholder="Description de la matière"
          />
        </FormulaireModal>
      </div>
    );
  }

  // Si on affiche les détails d'une filière
  if (filiereDetails) {
    const modulesFiliere = obtenirModulesFiliere(filiereDetails.codeFiliere);

    return (
      <div className="gestion-departements">
        <div className="details-departement-header">
          <button className="details-departement-btn-retour" onClick={() => {
            setFiliereDetails(null);
          }}>
            <ArrowLeft size={20} />
            Retour
          </button>
          <div>
            <h1 className="details-departement-titre">{filiereDetails.nom}</h1>
            <p className="details-departement-sous-titre">{filiereDetails.description || `Filière ${filiereDetails.nom}`}</p>
          </div>
        </div>

        <div className="details-departement-infos">
          <div className="details-departement-info-card">
            <h3>Informations de la Filière</h3>
            <div className="details-departement-info-grid">
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Code</span>
                <span className="details-departement-info-value">{filiereDetails.codeFiliere}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Chef de Filière</span>
                <span className="details-departement-info-value">{filiereDetails.chefFiliere || 'Non défini'}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Niveau</span>
                <span className="details-departement-info-value">{filiereDetails.niveauDiplome || 'Non défini'}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Durée</span>
                <span className="details-departement-info-value">{filiereDetails.dureeFormation || 0} ans</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre d'Étudiants</span>
                <span className="details-departement-info-value">{filiereDetails.nombreEtudiants || 0}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre de Modules</span>
                <span className="details-departement-info-value">{filiereDetails.nombreModules || 0}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">État</span>
                <BadgeStatut
                  statut={filiereDetails.etat === 'OUVERTE' ? 'success' : 'error'}
                  texte={filiereDetails.etat === 'OUVERTE' ? 'Ouverte' : 'Fermée'}
                />
              </div>
            </div>
          </div>
        </div>

        <div className="details-departement-filieres">
          <div className="details-departement-filieres-header">
            <h2 className="details-departement-filieres-titre">Modules de la Filière</h2>
            <button
              className="details-departement-btn-ajouter"
              onClick={() => ouvrirModalAjouterModule(filiereDetails)}
            >
              <Plus size={18} />
              Ajouter un Module
            </button>
          </div>
          <div className="details-departement-filieres-cartes">
            {modulesFiliere.map(module => (
              <div key={module.id} className="details-filiere-carte">
                <div className="details-filiere-carte-header">
                  <div className="details-filiere-carte-icone">
                    <BookOpen size={24} />
                  </div>
                  <div className="details-filiere-carte-titre-container">
                    <h3 className="details-filiere-carte-titre">
                      {module.nom}
                    </h3>
                    <p className="details-filiere-carte-code">{module.codeModule}</p>
                  </div>
                </div>

                <div className="details-filiere-carte-infos">
                  <div className="details-filiere-carte-info-item-chef">
                    <span className="details-filiere-carte-info-label">Professeur</span>
                    <span className="details-filiere-carte-info-value">{module.professeur || 'Non défini'}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Heures</span>
                    <span className="details-filiere-carte-info-value">{module.nombreHeures || 0}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Matières</span>
                    <span className="details-filiere-carte-info-value">{module.nombreMatieres || 0}</span>
                  </div>
                </div>

                <div className="details-filiere-carte-contenu">
                  <div className="details-filiere-carte-details">

                    <div className="details-filiere-carte-detail-item">
                      <span className="details-filiere-carte-detail-label">Coefficient</span>
                      <span className="details-filiere-carte-detail-value">{module.coefficient || 0}</span>
                    </div>
                  </div>

                </div>

                <div className="details-filiere-carte-actions">
                  <button
                    className="details-filiere-carte-btn-modifier"
                    onClick={(e) => {
                      e.stopPropagation();
                      ouvrirModalModifierModule(module);
                    }}
                  >
                    Modifier
                  </button>
                  <button
                    className="details-filiere-carte-btn-details"
                    onClick={(e) => {
                      e.stopPropagation();
                      setModuleDetails(module);
                    }}
                  >
                    Détails
                  </button>
                  <button
                    className="details-filiere-carte-btn-supprimer"
                    onClick={(e) => {
                      e.stopPropagation();
                      supprimerModule(module);
                    }}
                  >
                    <Trash2 size={16} />
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Modals pour les modules */}
        <FormulaireModal
          titre={moduleCourant ? 'Modifier le Module' : `Ajouter un Module - ${filiereDetails?.nom || ''}`}
          estOuvert={modalModifierModule || modalAjouterModule}
          surFermer={() => {
            setModalModifierModule(false);
            setModalAjouterModule(false);
            setModuleCourant(null);
            setFilierePourModule(null);
          }}
          surSauvegarder={sauvegarderModule}
        >
          <ChampFormulaire
            label="Code Module"
            nom="codeModule"
            valeur={formulaireModule.codeModule}
            onChange={gererChangementModule}
            placeholder="Ex: GI-INFO-101"
            requis
          />
          <ChampFormulaire
            label="Nom"
            nom="nom"
            valeur={formulaireModule.nom}
            onChange={gererChangementModule}
            placeholder="Nom du module"
            requis
          />
          <ChampFormulaire
            label="Description"
            nom="description"
            type="textarea"
            valeur={formulaireModule.description}
            onChange={gererChangementModule}
            placeholder="Description du module"
          />
          <ChampFormulaire
            label="Filière"
            nom="filiere"
            valeur={formulaireModule.filiere}
            onChange={gererChangementModule}
            placeholder="Code de la filière"
          />
          <ChampFormulaire
            label="Semestre"
            nom="semestre"
            valeur={formulaireModule.semestre}
            onChange={gererChangementModule}
            placeholder="Ex: S1"
          />
          <ChampFormulaire
            label="Coefficient"
            nom="coefficient"
            type="number"
            step="0.5"
            valeur={formulaireModule.coefficient}
            onChange={gererChangementModule}
            placeholder="Ex: 4.0"
          />
          <ChampFormulaire
            label="Nombre d'Heures"
            nom="nombreHeures"
            type="number"
            valeur={formulaireModule.nombreHeures}
            onChange={gererChangementModule}
            placeholder="Ex: 60"
          />
          <ChampFormulaire
            label="Professeur"
            nom="professeur"
            valeur={formulaireModule.professeur}
            onChange={gererChangementModule}
            placeholder="Nom du professeur"
          />
        </FormulaireModal>
      </div>
    );
  }

  // Si on affiche les détails d'un département
  if (departementDetails) {
    const filieresDepartement = obtenirFilieresDepartement(departementDetails.code);

    return (
      <div className="gestion-departements">
        <div className="details-departement-header">
          <button className="details-departement-btn-retour" onClick={() => setDepartementDetails(null)}>
            <ArrowLeft size={20} />
            Retour
          </button>
          <div>
            <h1 className="details-departement-titre">{departementDetails.nom}</h1>
            <p className="details-departement-sous-titre">{departementDetails.description || `Département ${departementDetails.nom}`}</p>
          </div>
        </div>

        <div className="details-departement-infos">
          <div className="details-departement-info-card">
            <h3>Informations du Département</h3>
            <div className="details-departement-info-grid">
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Code</span>
                <span className="details-departement-info-value">{departementDetails.code}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Chef de Département</span>
                <span className="details-departement-info-value">{departementDetails.chefDepartement || 'Non défini'}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre de Professeurs</span>
                <span className="details-departement-info-value">{departementDetails.nombreProfesseurs}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre de Filières</span>
                <span className="details-departement-info-value">{departementDetails.nombreFilieres}</span>
              </div>
              <div className="details-departement-info-item">
                <span className="details-departement-info-label">Nombre d'Étudiants</span>
                <span className="details-departement-info-value">{departementDetails.nombreEtudiants}</span>
              </div>
            </div>
          </div>
        </div>

        <div className="details-departement-filieres">
          <div className="details-departement-filieres-header">
            <h2 className="details-departement-filieres-titre">Filières du Département</h2>
            <button
              className="details-departement-btn-ajouter"
              onClick={() => ouvrirModalAjouterFiliere(departementDetails)}
            >
              <Plus size={18} />
              Ajouter une Filière
            </button>
          </div>
          <div className="details-departement-filieres-cartes">
            {filieresDepartement.map(filiere => (
              <div
                key={filiere.id}
                className="details-filiere-carte"
              >
                <div className="details-filiere-carte-header">
                  <div className="details-filiere-carte-icone">
                    <GraduationCap size={24} />
                  </div>
                  <div className="details-filiere-carte-titre-container">
                    <h3 className="details-filiere-carte-titre">
                      {filiere.nom.split(' ').map((mot, index) => (
                        <span key={index} className="details-filiere-carte-titre-ligne">
                          {mot}
                        </span>
                      ))}
                    </h3>
                    <p className="details-filiere-carte-code">{filiere.codeFiliere}</p>
                  </div>
                </div>

                <div className="details-filiere-carte-infos">
                  <div className="details-filiere-carte-info-item-chef">
                    <span className="details-filiere-carte-info-label">Chef de Filière</span>
                    <span className="details-filiere-carte-info-value">{filiere.chefFiliere || 'Non défini'}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Étudiants</span>
                    <span className="details-filiere-carte-info-value">{filiere.nombreEtudiants || 0}</span>
                  </div>
                  <div className="details-filiere-carte-info-item-ligne">
                    <span className="details-filiere-carte-info-label">Modules</span>
                    <span className="details-filiere-carte-info-value">{filiere.nombreModules || 0}</span>
                  </div>
                </div>
                <div className="details-filiere-carte-contenu">
                  <div className="details-filiere-carte-details">

                    <div className="details-filiere-carte-detail-item">
                      <span className="details-filiere-carte-detail-label">État</span>
                      <BadgeStatut
                        statut={filiere.etat === 'OUVERTE' ? 'success' : 'error'}
                        texte={filiere.etat === 'OUVERTE' ? 'Ouverte' : 'Fermée'}
                      />
                    </div>
                  </div>

                </div>

                <div className="details-filiere-carte-actions">
                  <button
                    className="details-filiere-carte-btn-modifier"
                    onClick={(e) => {
                      e.stopPropagation();
                      ouvrirModalModifierFiliere(filiere);
                    }}
                  >
                    Modifier
                  </button>
                  <button
                    className="details-filiere-carte-btn-details"
                    onClick={(e) => {
                      e.stopPropagation();
                      setFiliereDetails(filiere);
                    }}
                  >
                    Détails
                  </button>
                  <button
                    className="details-filiere-carte-btn-supprimer"
                    onClick={(e) => {
                      e.stopPropagation();
                      supprimerFiliere(filiere);
                    }}
                  >
                    <Trash2 size={16} />
                    Supprimer
                  </button>
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Modal Modifier Filière */}
        <FormulaireModal
          titre="Modifier la Filière"
          estOuvert={modalModifierFiliere}
          surFermer={() => {
            setModalModifierFiliere(false);
            setFiliereCourant(null);
          }}
          surSauvegarder={sauvegarderFiliere}
        >
          <ChampFormulaire
            label="Code Filière"
            nom="codeFiliere"
            valeur={formulaireFiliere.codeFiliere}
            onChange={gererChangementFiliere}
            placeholder="Ex: GI-2024"
            requis
          />
          <ChampFormulaire
            label="Nom de la Filière"
            nom="nom"
            valeur={formulaireFiliere.nom}
            onChange={gererChangementFiliere}
            placeholder="Ex: Génie Informatique"
            requis
          />
          <ChampFormulaire
            label="Chef de Filière"
            nom="chefFiliere"
            valeur={formulaireFiliere.chefFiliere}
            onChange={gererChangementFiliere}
            placeholder="Nom du chef de filière"
          />
          <ChampFormulaire
            label="Niveau Diplôme"
            nom="niveauDiplome"
            valeur={formulaireFiliere.niveauDiplome}
            onChange={gererChangementFiliere}
            placeholder="Ex: Licence"
          />
          <ChampFormulaire
            label="Durée de Formation (années)"
            nom="dureeFormation"
            type="number"
            valeur={formulaireFiliere.dureeFormation}
            onChange={gererChangementFiliere}
            placeholder="Ex: 3"
          />
          <ChampFormulaire
            label="État"
            nom="etat"
            type="select"
            valeur={formulaireFiliere.etat}
            onChange={gererChangementFiliere}
            options={[
              { value: 'OUVERTE', label: 'Ouverte' },
              { value: 'FERMEE', label: 'Fermée' }
            ]}
            requis
          />
          <ChampFormulaire
            label="Description"
            nom="description"
            type="textarea"
            valeur={formulaireFiliere.description}
            onChange={gererChangementFiliere}
            placeholder="Description de la filière"
          />
        </FormulaireModal>
      </div>
    );
  }

  return (
    <div className="gestion-departements">
      <div className="gestion-departements-header">
        <h1 className="gestion-departements-titre">Gestion des Départements</h1>
        <p className="gestion-departements-sous-titre">Départements et leurs filières</p>
      </div>

      <div className="gestion-departements-statistiques">
        <CarteStatistique
          titre="Total Départements"
          valeur={totalDepartements}
          variation={{ valeur: 3, label: '', positif: true }}
          icone={Building2}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Total Professeurs"
          valeur={totalProfesseurs}
          variation={{ valeur: 3, label: '', positif: true }}
          icone={Users}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Total Filières"
          valeur={totalFilieres}
          variation={{ valeur: 6, label: '', positif: true }}
          icone={GraduationCap}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

      <div className="gestion-departements-header-actions">
        <BoutonAction
          libelle="Nouveau département"
          variante="primaire"
          taille="moyen"
          icone={Plus}
          onClick={ouvrirModalAjoutDepartement}
        />
      </div>

      {/* Cartes des départements */}
      <div className="gestion-departements-cartes">
        {departements.map(departement => {
          const filieresDepartement = obtenirFilieresDepartement(departement.nom);
          const iconeMap = {
            'Informatique': Monitor,
            'Industriel': Briefcase,
            'Génie Civil': Building2,
            'Commerce': Briefcase,
            'Marketing': BarChart3
          };
          const Icone = iconeMap[departement.nom] || Building2;

          return (
            <div key={departement.id} className="departement-carte-nouveau">
              <div className="departement-carte-nouveau-header">
                <div className="departement-carte-nouveau-icone">
                  <Icone size={24} />
                </div>
                <div className="departement-carte-nouveau-titre-container">
                  <h3 className="departement-carte-nouveau-titre">{departement.nom}</h3>
                  <p className="departement-carte-nouveau-code">{departement.code}</p>
                </div>
              </div>

              <div className="departement-carte-nouveau-infos">
                <div className="departement-carte-nouveau-info-item-chef">
                  <span className="departement-carte-nouveau-info-label">Chef de département</span>
                  <span className="departement-carte-nouveau-info-value">{departement.chefDepartement || 'Non défini'}</span>
                </div>
                <div className="departement-carte-nouveau-info-item-ligne">
                  <span className="departement-carte-nouveau-info-label">Filières</span>
                  <span className="departement-carte-nouveau-info-value">{departement.nombreFilieres}</span>
                </div>
                <div className="departement-carte-nouveau-info-item-ligne">
                  <span className="departement-carte-nouveau-info-label">Étudiants</span>
                  <span className="departement-carte-nouveau-info-value">{departement.nombreEtudiants}</span>
                </div>
              </div>

              <div className="departement-carte-nouveau-separateur"></div>



              <div className="departement-carte-nouveau-actions">
                <button
                  className="departement-carte-nouveau-btn-modifier"
                  onClick={() => ouvrirModalModification(departement)}
                >
                  Modifier
                </button>
                <button
                  className="departement-carte-nouveau-btn-details"
                  onClick={() => setDepartementDetails(departement)}
                >
                  Détails
                </button>
              </div>
            </div>
          );
        })}
      </div>

      {/* Modal Ajout/Modification Département */}
      <FormulaireModal
        titre={departementCourant ? 'Modifier le Département' : 'Ajouter un Département'}
        estOuvert={modalAjoutDepartement}
        surFermer={() => {
          setModalAjoutDepartement(false);
          setDepartementCourant(null);
        }}
        surSauvegarder={sauvegarderDepartement}
      >
        <ChampFormulaire
          label="Nom du Département"
          nom="nom"
          valeur={formulaireDepartement.nom}
          onChange={gererChangementDepartement}
          placeholder="Ex: Informatique"
          requis
        />
        <ChampFormulaire
          label="Code"
          nom="code"
          valeur={formulaireDepartement.code}
          onChange={gererChangementDepartement}
          placeholder="Ex: INFO"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireDepartement.description}
          onChange={gererChangementDepartement}
          placeholder="Description du département"
        />
        <ChampFormulaire
          label="Chef de Département"
          nom="chefDepartement"
          valeur={formulaireDepartement.chefDepartement}
          onChange={gererChangementDepartement}
          placeholder="Nom du chef de département"
        />
      </FormulaireModal>

      {/* Modal Affecter Professeur */}
      <FormulaireModal
        titre={`Affecter un Professeur - ${departementSelectionne?.nom || ''}`}
        estOuvert={modalAffecterProf}
        surFermer={() => setModalAffecterProf(false)}
        surSauvegarder={null}
      >
        {professeursNonAffectes.length > 0 ? (
          <div className="liste-professeurs">
            <p className="liste-professeurs-titre">Sélectionnez un professeur à affecter :</p>
            {professeursNonAffectes.map(prof => (
              <div key={prof.id} className="professeur-item-select">
                <div>
                  <span className="professeur-item-nom">{prof.prenom} {prof.nom}</span>
                  <span className="professeur-item-detail">{prof.grade} - {prof.specialite}</span>
                  <span className="professeur-item-email">{prof.email}</span>
                </div>
                <BoutonAction
                  libelle="Affecter"
                  variante="primaire"
                  taille="petit"
                  onClick={() => affecterProfesseur(prof.id)}
                />
              </div>
            ))}
          </div>
        ) : (
          <p>Tous les professeurs sont déjà affectés à un département.</p>
        )}
      </FormulaireModal>

      {/* Modal Ajouter Filière */}
      <FormulaireModal
        titre={`Ajouter une Filière - ${departementSelectionne?.nom || ''}`}
        estOuvert={modalAjouterFiliere}
        surFermer={() => setModalAjouterFiliere(false)}
        surSauvegarder={sauvegarderFiliere}
      >
        <ChampFormulaire
          label="Code Filière"
          nom="codeFiliere"
          valeur={formulaireFiliere.codeFiliere}
          onChange={gererChangementFiliere}
          placeholder="Ex: GI-2024"
          requis
        />
        <ChampFormulaire
          label="Nom de la Filière"
          nom="nom"
          valeur={formulaireFiliere.nom}
          onChange={gererChangementFiliere}
          placeholder="Ex: Génie Informatique"
          requis
        />
        <ChampFormulaire
          label="État"
          nom="etat"
          type="select"
          valeur={formulaireFiliere.etat}
          onChange={gererChangementFiliere}
          options={[
            { value: 'OUVERTE', label: 'Ouverte' },
            { value: 'FERMEE', label: 'Fermée' }
          ]}
          requis
        />
      </FormulaireModal>

      {/* Modal Modifier Filière */}
      <FormulaireModal
        titre="Modifier la Filière"
        estOuvert={modalModifierFiliere}
        surFermer={() => {
          setModalModifierFiliere(false);
          setFiliereCourant(null);
        }}
        surSauvegarder={sauvegarderFiliere}
      >
        <ChampFormulaire
          label="Code Filière"
          nom="codeFiliere"
          valeur={formulaireFiliere.codeFiliere}
          onChange={gererChangementFiliere}
          placeholder="Ex: GI-2024"
          requis
        />
        <ChampFormulaire
          label="Nom de la Filière"
          nom="nom"
          valeur={formulaireFiliere.nom}
          onChange={gererChangementFiliere}
          placeholder="Ex: Génie Informatique"
          requis
        />
        <ChampFormulaire
          label="Chef de Filière"
          nom="chefFiliere"
          valeur={formulaireFiliere.chefFiliere}
          onChange={gererChangementFiliere}
          placeholder="Nom du chef de filière"
        />
        <ChampFormulaire
          label="Niveau Diplôme"
          nom="niveauDiplome"
          valeur={formulaireFiliere.niveauDiplome}
          onChange={gererChangementFiliere}
          placeholder="Ex: Licence"
        />
        <ChampFormulaire
          label="Durée de Formation (années)"
          nom="dureeFormation"
          type="number"
          valeur={formulaireFiliere.dureeFormation}
          onChange={gererChangementFiliere}
          placeholder="Ex: 3"
        />
        <ChampFormulaire
          label="État"
          nom="etat"
          type="select"
          valeur={formulaireFiliere.etat}
          onChange={gererChangementFiliere}
          options={[
            { value: 'OUVERTE', label: 'Ouverte' },
            { value: 'FERMEE', label: 'Fermée' }
          ]}
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireFiliere.description}
          onChange={gererChangementFiliere}
          placeholder="Description de la filière"
        />
      </FormulaireModal>

      {/* Modal Ajouter/Modifier Module */}
      <FormulaireModal
        titre={moduleCourant ? 'Modifier le Module' : 'Ajouter un Module'}
        estOuvert={modalModifierModule || modalAjouterModule}
        surFermer={() => {
          setModalModifierModule(false);
          setModalAjouterModule(false);
          setModuleCourant(null);
          setFilierePourModule(null);
        }}
        surSauvegarder={sauvegarderModule}
      >
        <ChampFormulaire
          label="Code Module"
          nom="codeModule"
          valeur={formulaireModule.codeModule}
          onChange={gererChangementModule}
          placeholder="Ex: GI-INFO-101"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaireModule.nom}
          onChange={gererChangementModule}
          placeholder="Nom du module"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireModule.description}
          onChange={gererChangementModule}
          placeholder="Description du module"
        />
        <ChampFormulaire
          label="Filière"
          nom="filiere"
          valeur={formulaireModule.filiere}
          onChange={gererChangementModule}
          placeholder="Code de la filière"
        />
        <ChampFormulaire
          label="Semestre"
          nom="semestre"
          valeur={formulaireModule.semestre}
          onChange={gererChangementModule}
          placeholder="Ex: S1"
        />
        <ChampFormulaire
          label="Coefficient"
          nom="coefficient"
          type="number"
          step="0.5"
          valeur={formulaireModule.coefficient}
          onChange={gererChangementModule}
          placeholder="Ex: 4.0"
        />
        <ChampFormulaire
          label="Nombre d'Heures"
          nom="nombreHeures"
          type="number"
          valeur={formulaireModule.nombreHeures}
          onChange={gererChangementModule}
          placeholder="Ex: 60"
        />
        <ChampFormulaire
          label="Professeur"
          nom="professeur"
          valeur={formulaireModule.professeur}
          onChange={gererChangementModule}
          placeholder="Nom du professeur"
        />
      </FormulaireModal>

      {/* Modal Ajouter/Modifier Matière */}
      <FormulaireModal
        titre={matiereCourante ? 'Modifier la Matière' : 'Ajouter une Matière'}
        estOuvert={modalModifierMatiere || modalAjouterMatiere}
        surFermer={() => {
          setModalModifierMatiere(false);
          setModalAjouterMatiere(false);
          setMatiereCourante(null);
          setModulePourMatiere(null);
        }}
        surSauvegarder={sauvegarderMatiere}
      >
        <ChampFormulaire
          label="Code Matière"
          nom="codeMatiere"
          valeur={formulaireMatiere.codeMatiere}
          onChange={gererChangementMatiere}
          placeholder="Ex: ALG-101-1"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaireMatiere.nom}
          onChange={gererChangementMatiere}
          placeholder="Nom de la matière"
          requis
        />
        <ChampFormulaire
          label="Module"
          nom="module"
          valeur={formulaireMatiere.module}
          onChange={gererChangementMatiere}
          placeholder="Code du module"
        />
        <ChampFormulaire
          label="Type"
          nom="type"
          type="select"
          valeur={formulaireMatiere.type}
          onChange={gererChangementMatiere}
          options={[
            { value: 'Cours', label: 'Cours' },
            { value: 'TP', label: 'TP' },
            { value: 'TD', label: 'TD' }
          ]}
        />
        <ChampFormulaire
          label="Nombre d'Heures"
          nom="nombreHeures"
          type="number"
          valeur={formulaireMatiere.nombreHeures}
          onChange={gererChangementMatiere}
          placeholder="Ex: 20"
        />
        <ChampFormulaire
          label="Coefficient"
          nom="coefficient"
          type="number"
          step="0.5"
          valeur={formulaireMatiere.coefficient}
          onChange={gererChangementMatiere}
          placeholder="Ex: 1.5"
        />
        <ChampFormulaire
          label="Professeur"
          nom="professeur"
          valeur={formulaireMatiere.professeur}
          onChange={gererChangementMatiere}
          placeholder="Nom du professeur"
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireMatiere.description}
          onChange={gererChangementMatiere}
          placeholder="Description de la matière"
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionDepartements;

