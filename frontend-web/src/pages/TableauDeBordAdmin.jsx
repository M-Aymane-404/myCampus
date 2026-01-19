import { useState, useEffect } from 'react';
import { Users, UserCheck, BookOpen, Building2, Users as UsersActive, Clock } from 'lucide-react';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import AlerteNotification from '../composants/tableauDeBord/AlerteNotification';
import GraphiqueDepartement from '../composants/tableauDeBord/GraphiqueDepartement';
import GraphiqueDistribution from '../composants/tableauDeBord/GraphiqueDistribution';
import JournalAudit from '../composants/tableauDeBord/JournalAudit';
import ActionsRapides from '../composants/tableauDeBord/ActionsRapides';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { donneesTestTableauDeBord } from '../donnees/donneesTest';
import { apiTableauDeBord } from '../services/apiTableauDeBord';
import './TableauDeBordAdmin.css';

const TableauDeBordAdmin = () => {
  const [donneesTableauDeBord, setDonneesTableauDeBord] = useState(null);
  const [chargement, setChargement] = useState(true);

  // États pour les modals d'ajout
  const [modalEtudiantOuvert, setModalEtudiantOuvert] = useState(false);
  const [modalProfesseurOuvert, setModalProfesseurOuvert] = useState(false);

  // Formulaire étudiant
  const [formulaireEtudiant, setFormulaireEtudiant] = useState({
    apogee: '',
    nom: '',
    prenom: '',
    email: '',
    cin: '',
    tel: '',
    niveau: '',
    filiere: '',
    anneeInscription: new Date().getFullYear().toString(),
    semestreActuel: '',
    etatInscription: 'INSCRIT',
    dateNaissance: ''
  });

  // Formulaire professeur
  const [formulaireProfesseur, setFormulaireProfesseur] = useState({
    ppr: '',
    nom: '',
    prenom: '',
    email: '',
    specialite: '',
    grade: '',
    dateRecrutement: '',
    status: 'Actif'
  });

  // Données pour les formulaires
  const filieres = [
    { value: 'GI-2024', label: 'Génie Informatique' },
    { value: 'GIL-2024', label: 'Génie Industriel et Logistique' },
    { value: 'GC-2024', label: 'Génie Civil' }
  ];

  const niveaux = [
    { value: 'DUT', label: 'Diplôme Universitaire de Technologie' },
    { value: 'BUT', label: 'Bachelor Universitaire de Technologie' }
  ];

  const semestres = [
    { value: 'S1', label: 'Semestre 1' },
    { value: 'S2', label: 'Semestre 2' },
    { value: 'S3', label: 'Semestre 3' },
    { value: 'S4', label: 'Semestre 4' },
    { value: 'S5', label: 'Semestre 5' },
    { value: 'S6', label: 'Semestre 6' }
  ];

  const etatsInscription = [
    { value: 'INSCRIT', label: 'Inscrit' },
    { value: 'SUSPENDU', label: 'Suspendu' },
    { value: 'DIPLOME', label: 'Diplômé' }
  ];

  const grades = [
    { value: 'Professeur', label: 'Professeur' },
    { value: 'Maître de Conférences', label: 'Maître de Conférences' },
    { value: 'Maître Assistant', label: 'Maître Assistant' },
    { value: 'Assistant', label: 'Assistant' }
  ];

  const specialites = [
    { value: 'Informatique', label: 'Informatique' },
    { value: 'Mathématiques', label: 'Mathématiques' },
    { value: 'Génie Industriel', label: 'Génie Industriel' },
    { value: 'Génie Civil', label: 'Génie Civil' },
    { value: 'Mécanique', label: 'Mécanique' }
  ];

  useEffect(() => {
    chargerDonnees();
  }, []);

  const chargerDonnees = async () => {
    try {
      setChargement(true);
      const donnees = await apiTableauDeBord.obtenirTableauDeBord();
      setDonneesTableauDeBord(donnees);
    } catch (erreur) {
      console.warn('Utilisation des données de test:', erreur);
      setDonneesTableauDeBord(donneesTestTableauDeBord);
    } finally {
      setChargement(false);
    }
  };

  const gererActionRapide = (idAction) => {
    if (idAction === 'ajouter-etudiant') {
      setFormulaireEtudiant({
        apogee: '',
        nom: '',
        prenom: '',
        email: '',
        cin: '',
        tel: '',
        niveau: '',
        filiere: '',
        anneeInscription: new Date().getFullYear().toString(),
        semestreActuel: '',
        etatInscription: 'INSCRIT',
        dateNaissance: ''
      });
      setModalEtudiantOuvert(true);
    } else if (idAction === 'ajouter-professeur') {
      setFormulaireProfesseur({
        ppr: '',
        nom: '',
        prenom: '',
        email: '',
        specialite: '',
        grade: '',
        dateRecrutement: '',
        status: 'Actif'
      });
      setModalProfesseurOuvert(true);
    }
  };

  const gererChangementEtudiant = (e) => {
    const { name, value } = e.target;
    setFormulaireEtudiant(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementProfesseur = (e) => {
    const { name, value } = e.target;
    setFormulaireProfesseur(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarderEtudiant = () => {
    // Validation basique
    if (!formulaireEtudiant.apogee || !formulaireEtudiant.nom || !formulaireEtudiant.prenom || !formulaireEtudiant.email || !formulaireEtudiant.filiere) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    // Ici, vous pouvez envoyer les données à une API
    // Exemple: await api.ajouterEtudiant(formulaireEtudiant);
    
    console.log('Nouvel étudiant:', formulaireEtudiant);
    alert('Étudiant ajouté avec succès');
    setModalEtudiantOuvert(false);
    
    // Réinitialiser le formulaire
    setFormulaireEtudiant({
      apogee: '',
      nom: '',
      prenom: '',
      email: '',
      cin: '',
      tel: '',
      niveau: '',
      filiere: '',
      anneeInscription: new Date().getFullYear().toString(),
      semestreActuel: '',
      etatInscription: 'INSCRIT',
      dateNaissance: ''
    });
  };

  const sauvegarderProfesseur = () => {
    // Validation basique
    if (!formulaireProfesseur.ppr || !formulaireProfesseur.nom || !formulaireProfesseur.prenom || !formulaireProfesseur.email) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    // Ici, vous pouvez envoyer les données à une API
    // Exemple: await api.ajouterProfesseur(formulaireProfesseur);
    
    console.log('Nouveau professeur:', formulaireProfesseur);
    alert('Professeur ajouté avec succès');
    setModalProfesseurOuvert(false);
    
    // Réinitialiser le formulaire
    setFormulaireProfesseur({
      ppr: '',
      nom: '',
      prenom: '',
      email: '',
      specialite: '',
      grade: '',
      dateRecrutement: '',
      status: 'Actif'
    });
  };

  if (chargement) {
    return (
      <div className="chargement-container">
        <div className="chargement-spinner"></div>
        <p>Chargement du tableau de bord...</p>
      </div>
    );
  }

  const statistiques = donneesTableauDeBord?.statistiquesGlobales || donneesTestTableauDeBord.statistiquesGlobales;
  const statistiquesDepartement = donneesTableauDeBord?.statistiquesParDepartement || donneesTestTableauDeBord.statistiquesParDepartement;
  const journaux = donneesTableauDeBord?.journauxAudit || donneesTestTableauDeBord.journauxAudit;

  return (
    <div className="tableau-de-bord-admin">
      <div className="tableau-de-bord-header">
        <h1 className="tableau-de-bord-titre">Welcome back to your workspace</h1>
        <p className="tableau-de-bord-sous-titre">Here is a comprehensive overview of your institution for today.</p>
      </div>

      <div className="tableau-de-bord-statistiques-container">
        <CarteStatistique
          titre="Students"
          valeur={statistiques.totalEtudiants}
          variation={{ valeur: 12, label: '%', positif: true }}
          icone={Users}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Professors"
          valeur={statistiques.totalProfesseurs}
          variation={{ valeur: 3, label: '', positif: true }}
          icone={UserCheck}
          couleur="primary"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Modules"
          valeur={statistiques.totalModules}
          variation={{ valeur: 42, label: '', positif: true }}
          icone={BookOpen}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Departments"
          valeur={statistiques.totalDepartements}
          variation={{ valeur: statistiques.totalDepartements, label: 'Academic units', positif: true }}
          icone={Building2}
          couleur="primary"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Pending"
          valeur={0}
          variation={{ valeur: 94, label: '%', positif: true }}
          icone={Clock}
          couleur="accent"
          iconeCouleur="dark"
        />
      </div>

      

      <div className="tableau-de-bord-graphiques">
        <div className="graphique-grand">
          <GraphiqueDepartement donnees={statistiquesDepartement} />
        </div>
        <div className="graphique-moyen">
          <GraphiqueDistribution donnees={statistiques} />
        </div>
      </div>

      <div className="tableau-de-bord-journal-audit">
        <JournalAudit journaux={journaux} />
      </div>

      <div className="tableau-de-bord-actions-rapides">
        <ActionsRapides surAction={gererActionRapide} />
      </div>

      {/* Modal Ajouter Étudiant */}
      <FormulaireModal
        titre="Ajouter un Étudiant"
        estOuvert={modalEtudiantOuvert}
        surFermer={() => setModalEtudiantOuvert(false)}
        surSauvegarder={sauvegarderEtudiant}
      >
        <ChampFormulaire
          label="Numéro Apogée"
          nom="apogee"
          valeur={formulaireEtudiant.apogee}
          onChange={gererChangementEtudiant}
          placeholder="Ex: APG001"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaireEtudiant.nom}
          onChange={gererChangementEtudiant}
          placeholder="Nom de famille"
          requis
        />
        <ChampFormulaire
          label="Prénom"
          nom="prenom"
          valeur={formulaireEtudiant.prenom}
          onChange={gererChangementEtudiant}
          placeholder="Prénom"
          requis
        />
        <ChampFormulaire
          label="Email"
          nom="email"
          type="email"
          valeur={formulaireEtudiant.email}
          onChange={gererChangementEtudiant}
          placeholder="email@student.univ.ma"
          requis
        />
        <ChampFormulaire
          label="CIN"
          nom="cin"
          valeur={formulaireEtudiant.cin}
          onChange={gererChangementEtudiant}
          placeholder="AB123456"
          requis
        />
        <ChampFormulaire
          label="Téléphone"
          nom="tel"
          type="tel"
          valeur={formulaireEtudiant.tel}
          onChange={gererChangementEtudiant}
          placeholder="0612345678"
        />
        <ChampFormulaire
          label="Date de Naissance"
          nom="dateNaissance"
          type="date"
          valeur={formulaireEtudiant.dateNaissance}
          onChange={gererChangementEtudiant}
          requis
        />
        <ChampFormulaire
          label="Filière"
          nom="filiere"
          type="select"
          valeur={formulaireEtudiant.filiere}
          onChange={gererChangementEtudiant}
          options={filieres}
          requis
        />
        <ChampFormulaire
          label="Niveau"
          nom="niveau"
          type="select"
          valeur={formulaireEtudiant.niveau}
          onChange={gererChangementEtudiant}
          options={niveaux}
          requis
        />
        <ChampFormulaire
          label="Semestre Actuel"
          nom="semestreActuel"
          type="select"
          valeur={formulaireEtudiant.semestreActuel}
          onChange={gererChangementEtudiant}
          options={semestres}
          requis
        />
        <ChampFormulaire
          label="Année d'Inscription"
          nom="anneeInscription"
          type="number"
          valeur={formulaireEtudiant.anneeInscription}
          onChange={gererChangementEtudiant}
          requis
        />
        <ChampFormulaire
          label="État d'Inscription"
          nom="etatInscription"
          type="select"
          valeur={formulaireEtudiant.etatInscription}
          onChange={gererChangementEtudiant}
          options={etatsInscription}
          requis
        />
      </FormulaireModal>

      {/* Modal Ajouter Professeur */}
      <FormulaireModal
        titre="Ajouter un Professeur"
        estOuvert={modalProfesseurOuvert}
        surFermer={() => setModalProfesseurOuvert(false)}
        surSauvegarder={sauvegarderProfesseur}
      >
        <ChampFormulaire
          label="PPR"
          nom="ppr"
          valeur={formulaireProfesseur.ppr}
          onChange={gererChangementProfesseur}
          placeholder="Ex: PPR001"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaireProfesseur.nom}
          onChange={gererChangementProfesseur}
          placeholder="Nom de famille"
          requis
        />
        <ChampFormulaire
          label="Prénom"
          nom="prenom"
          valeur={formulaireProfesseur.prenom}
          onChange={gererChangementProfesseur}
          placeholder="Prénom"
          requis
        />
        <ChampFormulaire
          label="Email"
          nom="email"
          type="email"
          valeur={formulaireProfesseur.email}
          onChange={gererChangementProfesseur}
          placeholder="email@univ.ma"
          requis
        />
        <ChampFormulaire
          label="Spécialité"
          nom="specialite"
          type="select"
          valeur={formulaireProfesseur.specialite}
          onChange={gererChangementProfesseur}
          options={specialites}
          requis
        />
        <ChampFormulaire
          label="Grade"
          nom="grade"
          type="select"
          valeur={formulaireProfesseur.grade}
          onChange={gererChangementProfesseur}
          options={grades}
          requis
        />
        <ChampFormulaire
          label="Date de Recrutement"
          nom="dateRecrutement"
          type="date"
          valeur={formulaireProfesseur.dateRecrutement}
          onChange={gererChangementProfesseur}
          requis
        />
        <ChampFormulaire
          label="Statut"
          nom="status"
          type="select"
          valeur={formulaireProfesseur.status}
          onChange={gererChangementProfesseur}
          options={[
            { value: 'Actif', label: 'Actif' },
            { value: 'Inactif', label: 'Inactif' }
          ]}
          requis
        />
      </FormulaireModal>
    </div>
  );
};

export default TableauDeBordAdmin;

