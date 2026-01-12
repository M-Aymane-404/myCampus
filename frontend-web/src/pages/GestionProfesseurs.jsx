import { useState } from 'react';
import { UserCheck, BookOpen, GraduationCap } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import './GestionProfesseurs.css';

const GestionProfesseurs = () => {
  const [professeurs, setProfesseurs] = useState([
    {
      id: 1,
      ppr: 'PPR001',
      nom: 'Alami',
      prenom: 'Ahmed',
      email: 'ahmed.alami@univ.ma',
      specialite: 'Informatique',
      grade: 'Professeur',
      filieres: ['GI-2024', 'GIL-2024'],
      dateRecrutement: '2015-09-01',
      status: 'Actif'
    },
    {
      id: 2,
      ppr: 'PPR002',
      nom: 'Bennani',
      prenom: 'Fatima',
      email: 'fatima.bennani@univ.ma',
      specialite: 'Mathématiques',
      grade: 'Maître de Conférences',
      filieres: ['GI-2024'],
      dateRecrutement: '2018-09-01',
      status: 'Actif'
    },
    {
      id: 3,
      ppr: 'PPR003',
      nom: 'Chraibi',
      prenom: 'Mohamed',
      email: 'mohamed.chraibi@univ.ma',
      specialite: 'Génie Industriel',
      grade: 'Professeur',
      filieres: ['GIL-2024'],
      dateRecrutement: '2010-09-01',
      status: 'Actif'
    }
  ]);

  const [filieres] = useState([
    { value: 'GI-2024', label: 'Génie Informatique' },
    { value: 'GIL-2024', label: 'Génie Industriel et Logistique' },
    { value: 'GC-2024', label: 'Génie Civil' }
  ]);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [modalAttribution, setModalAttribution] = useState(false);
  const [professeurCourant, setProfesseurCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    ppr: '',
    nom: '',
    prenom: '',
    email: '',
    specialite: '',
    grade: '',
    dateRecrutement: '',
    status: 'Actif'
  });

  const [filieresSelectionnees, setFilieresSelectionnees] = useState([]);

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

  const ouvrirModalAjout = () => {
    setProfesseurCourant(null);
    setFormulaire({
      ppr: '',
      nom: '',
      prenom: '',
      email: '',
      specialite: '',
      grade: '',
      dateRecrutement: '',
      status: 'Actif'
    });
    setModalOuvert(true);
  };

  const ouvrirModalModification = (professeur) => {
    setProfesseurCourant(professeur);
    setFormulaire({
      ppr: professeur.ppr,
      nom: professeur.nom,
      prenom: professeur.prenom,
      email: professeur.email,
      specialite: professeur.specialite,
      grade: professeur.grade,
      dateRecrutement: professeur.dateRecrutement,
      status: professeur.status
    });
    setModalOuvert(true);
  };

  const ouvrirModalAttribution = (professeur) => {
    setProfesseurCourant(professeur);
    setFilieresSelectionnees(professeur.filieres || []);
    setModalAttribution(true);
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const gererSelectionFilieres = (e) => {
    const options = Array.from(e.target.selectedOptions, option => option.value);
    setFilieresSelectionnees(options);
  };

  const sauvegarder = () => {
    // Validation basique
    if (!modalAttribution && (!formulaire.ppr || !formulaire.nom || !formulaire.prenom || !formulaire.email)) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (modalAttribution) {
      setProfesseurs(prev => prev.map(p => 
        p.id === professeurCourant.id 
          ? { ...p, filieres: filieresSelectionnees }
          : p
      ));
      setModalAttribution(false);
      return;
    }

    if (professeurCourant) {
      // Modification
      setProfesseurs(prev => prev.map(p => 
        p.id === professeurCourant.id 
          ? { ...p, ...formulaire }
          : p
      ));
    } else {
      // Ajout
      const nouveauProfesseur = {
        id: professeurs.length + 1,
        ...formulaire,
        filieres: []
      };
      setProfesseurs(prev => [...prev, nouveauProfesseur]);
    }
    setModalOuvert(false);
  };

  const supprimer = (professeur) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le professeur "${professeur.nom} ${professeur.prenom}" ?`)) {
      setProfesseurs(prev => prev.filter(p => p.id !== professeur.id));
    }
  };

  const colonnes = [
    { key: 'ppr', label: 'PPR' },
    { 
      key: 'nom', 
      label: 'Nom Complet',
      render: (item) => `${item.prenom} ${item.nom}`
    },
    { key: 'email', label: 'Email' },
    { key: 'specialite', label: 'Spécialité' },
    { key: 'grade', label: 'Grade' },
    { 
      key: 'filieres', 
      label: 'Filières',
      render: (item) => (
        <div className="cellule-filieres">
          {item.filieres && item.filieres.length > 0 ? (
            <>
              <BookOpen size={14} />
              <span>{item.filieres.length} filière(s)</span>
            </>
          ) : (
            <span className="texte-secondaire">Aucune</span>
          )}
        </div>
      )
    },
    { 
      key: 'status', 
      label: 'Statut',
      render: (item) => (
        <BadgeStatut 
          statut={item.status === 'Actif' ? 'success' : 'error'}
          texte={item.status}
        />
      )
    }
  ];

  const totalProfesseurs = professeurs.length;
  const professeursActifs = professeurs.filter(p => p.status === 'Actif').length;
  const totalFilieresAttribuees = professeurs.reduce((sum, p) => sum + (p.filieres?.length || 0), 0);

  return (
    <div className="gestion-professeurs">
      <div className="gestion-professeurs-header">
        <h1 className="gestion-professeurs-titre">Gestion des Professeurs</h1>
        <p className="gestion-professeurs-sous-titre">Gestion et attribution aux filières</p>
      </div>

      <div className="gestion-professeurs-statistiques">
        <CarteStatistique
          titre="Total Professeurs"
          valeur={totalProfesseurs}
          variation={{ valeur: 2, label: '', positif: true }}
          icone={UserCheck}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Professeurs Actifs"
          valeur={professeursActifs}
          variation={{ valeur: professeursActifs, label: '', positif: true }}
          icone={UserCheck}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Attributions"
          valeur={totalFilieresAttribuees}
          variation={{ valeur: totalFilieresAttribuees, label: '', positif: true }}
          icone={GraduationCap}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

      <TableauDonnees
        titre="Liste des Professeurs"
        colonnes={colonnes}
        donnees={professeurs}
        surAjouter={ouvrirModalAjout}
        surModifier={ouvrirModalModification}
        surSupprimer={supprimer}
        surVoir={(prof) => ouvrirModalAttribution(prof)}
      />

      <FormulaireModal
        titre={professeurCourant ? 'Modifier le Professeur' : 'Ajouter un Professeur'}
        estOuvert={modalOuvert}
        surFermer={() => setModalOuvert(false)}
        surSauvegarder={sauvegarder}
      >
        <ChampFormulaire
          label="PPR"
          nom="ppr"
          valeur={formulaire.ppr}
          onChange={gererChangement}
          placeholder="Ex: PPR001"
          requis
        />
        <ChampFormulaire
          label="Nom"
          nom="nom"
          valeur={formulaire.nom}
          onChange={gererChangement}
          placeholder="Nom de famille"
          requis
        />
        <ChampFormulaire
          label="Prénom"
          nom="prenom"
          valeur={formulaire.prenom}
          onChange={gererChangement}
          placeholder="Prénom"
          requis
        />
        <ChampFormulaire
          label="Email"
          nom="email"
          type="email"
          valeur={formulaire.email}
          onChange={gererChangement}
          placeholder="email@univ.ma"
          requis
        />
        <ChampFormulaire
          label="Spécialité"
          nom="specialite"
          type="select"
          valeur={formulaire.specialite}
          onChange={gererChangement}
          options={specialites}
          requis
        />
        <ChampFormulaire
          label="Grade"
          nom="grade"
          type="select"
          valeur={formulaire.grade}
          onChange={gererChangement}
          options={grades}
          requis
        />
        <ChampFormulaire
          label="Date de Recrutement"
          nom="dateRecrutement"
          type="date"
          valeur={formulaire.dateRecrutement}
          onChange={gererChangement}
          requis
        />
        <ChampFormulaire
          label="Statut"
          nom="status"
          type="select"
          valeur={formulaire.status}
          onChange={gererChangement}
          options={[
            { value: 'Actif', label: 'Actif' },
            { value: 'Inactif', label: 'Inactif' }
          ]}
          requis
        />
      </FormulaireModal>

      <FormulaireModal
        titre={`Attribuer des Filières - ${professeurCourant ? `${professeurCourant.prenom} ${professeurCourant.nom}` : ''}`}
        estOuvert={modalAttribution}
        surFermer={() => setModalAttribution(false)}
        surSauvegarder={sauvegarder}
        texteBoutonSauvegarder="Enregistrer les Attributions"
      >
        <ChampFormulaire
          label="Filières à Enseigner"
          nom="filieres"
          type="select"
          valeur={filieresSelectionnees}
          onChange={gererSelectionFilieres}
          options={filieres}
          multiple
        />
        <div className="info-attribution">
          <p>Vous pouvez sélectionner plusieurs filières en maintenant la touche Ctrl (ou Cmd sur Mac).</p>
          <p>Filières sélectionnées : {filieresSelectionnees.length}</p>
        </div>
      </FormulaireModal>
    </div>
  );
};

export default GestionProfesseurs;

