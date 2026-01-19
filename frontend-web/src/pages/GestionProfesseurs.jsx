import { useState, useEffect } from 'react';
import { UserCheck, BookOpen, GraduationCap } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiStructure } from '../services/apiStructure';
import './GestionProfesseurs.css';

const GestionProfesseurs = () => {
  const [professeurs, setProfesseurs] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [filieres, setFilieres] = useState([]);
  const [departements, setDepartements] = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [chargementDonnees, setChargementDonnees] = useState(true);

  // Charger les professeurs depuis l'API
  const chargerProfesseurs = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = await apiProfesseurs.obtenirTous();
      console.log('Professeurs reçus:', donnees);
      // Mapper les données du backend vers le format frontend
      const professeursMapped = donnees.map(p => ({
        id: p.id,
        ppr: p.ppr,
        nom: p.nom,
        prenom: p.prenom,
        email: p.email,
        cin: p.cin,
        tel: p.tel,
        grade: p.grade,
        departement: p.codeDepartementOuTravail || '', // Utiliser le code du département
        matieresEnseignees: p.matieresEnseignees || [], // Données venant du backend
        filieres: p.filieres || [], // Données venant du backend
        dateRecrutement: p.dateRecrutement,
        status: p.statut || (p.enabled ? 'Actif' : 'Inactif')
      }));
      setProfesseurs(professeursMapped);
      console.log('Professeurs mappés:', professeursMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des professeurs:', err);
      setErreur('Erreur lors du chargement des professeurs. Vérifiez que le serveur backend est démarré.');
    } finally {
      setChargement(false);
    }
  };

  // Charger les filières depuis l'API
  const chargerFilieres = async () => {
    try {
      const donnees = await apiStructure.obtenirToutesFilieres();
      console.log('Filieres reçues:', donnees);
      if (donnees && donnees.length > 0) {
        const filieresMapped = donnees.map(f => ({
          value: f.codeFiliere,
          label: f.nom || f.codeFiliere
        }));
        setFilieres(filieresMapped);
      } else {
        setFilieres([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des filières:', err);
      setFilieres([]);
    }
  };

  // Charger les départements depuis l'API
  const chargerDepartements = async () => {
    try {
      const donnees = await apiStructure.obtenirTousDepartements();
      console.log('Départements reçus:', donnees);
      if (donnees && donnees.length > 0) {
        // Le backend attend le codeDepartement, pas le nom
        const departementsMapped = donnees.map(d => ({
          value: d.codeDepartement, // Utiliser le code, pas le nom
          label: d.typeDepartement || d.codeDepartement
        }));
        setDepartements(departementsMapped);
        console.log('Départements mappés:', departementsMapped);
      } else {
        setDepartements([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des départements:', err);
      setDepartements([]);
    } finally {
      setChargementDonnees(false);
    }
  };

  // Charger les matières depuis l'API
  const chargerMatieres = async () => {
    try {
      const donnees = await apiStructure.obtenirToutesMatieres();
      console.log('Matieres reçues:', donnees);
      if (donnees && donnees.length > 0) {
        const matieresMapped = donnees.map(m => ({
          value: m.codeMatiere,
          label: m.nom || m.codeMatiere
        }));
        setMatieres(matieresMapped);
        console.log('Matieres mappées:', matieresMapped);
      } else {
        setMatieres([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des matières:', err);
      setMatieres([]);
    }
  };

  useEffect(() => {
    chargerProfesseurs();
    chargerFilieres();
    chargerDepartements();
    chargerMatieres();
  }, []);

  const [filtreDepartement, setFiltreDepartement] = useState('');
  const [filtreFiliere, setFiltreFiliere] = useState('');
  const [filtreMatiere, setFiltreMatiere] = useState('');

  const [modalOuvert, setModalOuvert] = useState(false);
  const [modalAttribution, setModalAttribution] = useState(false);
  const [professeurCourant, setProfesseurCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    ppr: '',
    nom: '',
    prenom: '',
    email: '',
    password: '',
    cin: '',
    tel: '',
    matieresEnseignees: [], // Matières enseignées (multi-sélection)
    grade: '',
    dateRecrutement: '',
    statut: 'Actif',
    codeDepartementOuTravail: ''
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
      password: '',
      cin: '',
      tel: '',
      matieresEnseignees: [],
      grade: '',
      dateRecrutement: '',
      statut: 'Actif',
      codeDepartementOuTravail: ''
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
      password: '', // Ne pas pré-remplir le mot de passe en modification
      cin: professeur.cin || '',
      tel: professeur.tel || '',
      matieresEnseignees: professeur.matieresEnseignees || [],
      grade: professeur.grade || '',
      dateRecrutement: professeur.dateRecrutement,
      statut: professeur.status,
      codeDepartementOuTravail: professeur.departement || ''
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

  const gererSelectionMatieres = (e) => {
    const options = Array.from(e.target.selectedOptions, option => option.value);
    setFormulaire(prev => ({ ...prev, matieresEnseignees: options }));
  };

  const gererSelectionFilieres = (e) => {
    const options = Array.from(e.target.selectedOptions, option => option.value);
    setFilieresSelectionnees(options);
  };

  const sauvegarder = async () => {
    // Validation basique
    if (!modalAttribution && (!formulaire.ppr || !formulaire.nom || !formulaire.prenom || !formulaire.email || !formulaire.cin)) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    // Le mot de passe est obligatoire uniquement pour la création
    if (!modalAttribution && !professeurCourant && !formulaire.password) {
      alert('Le mot de passe est obligatoire pour créer un nouveau professeur');
      return;
    }

    if (modalAttribution) {
      // TODO: Implémenter l'attribution de filières via API si nécessaire
      setProfesseurs(prev => prev.map(p => 
        p.id === professeurCourant.id 
          ? { ...p, filieres: filieresSelectionnees }
          : p
      ));
      setModalAttribution(false);
      return;
    }

    try {
      // Préparer les données pour l'API backend
      const donneesAPI = {
        ppr: formulaire.ppr.trim(),
        nom: formulaire.nom.trim(),
        prenom: formulaire.prenom.trim(),
        email: formulaire.email.trim(),
        cin: formulaire.cin.trim(),
        tel: formulaire.tel && formulaire.tel.trim() ? formulaire.tel.trim() : '',
        grade: formulaire.grade.trim(), // Grade est maintenant un champ texte manuel
        dateRecrutement: formulaire.dateRecrutement || null,
        statut: formulaire.statut,
        // Specialite n'est plus utilisé, mais gardé pour compatibilité backend (optionnel)
        specialite: formulaire.matieresEnseignees.length > 0 
          ? formulaire.matieresEnseignees.join(', ') 
          : ''
      };

      // Le département doit être envoyé seulement s'il est rempli, et doit être le CODE du département
      if (formulaire.codeDepartementOuTravail && formulaire.codeDepartementOuTravail.trim()) {
        donneesAPI.codeDepartementOuTravail = formulaire.codeDepartementOuTravail.trim();
      } else {
        // Ne pas envoyer le champ si vide
        donneesAPI.codeDepartementOuTravail = null;
      }

      // Ajouter le mot de passe seulement si fourni (création ou modification)
      if (formulaire.password && formulaire.password.trim()) {
        donneesAPI.password = formulaire.password.trim();
      }

      console.log('Données envoyées à l\'API:', JSON.stringify(donneesAPI, null, 2));

      if (professeurCourant) {
        await apiProfesseurs.modifier(professeurCourant.ppr, donneesAPI);
      } else {
        await apiProfesseurs.creer(donneesAPI);
      }
      
      setModalOuvert(false);
      chargerProfesseurs(); // Recharger la liste
    } catch (err) {
      console.error('Erreur détaillée:', err);
      const messageErreur = err.message || 'Erreur inconnue lors de la sauvegarde';
      alert('Erreur lors de la sauvegarde: ' + messageErreur);
    }
  };

  const supprimer = async (professeur) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le professeur "${professeur.prenom} ${professeur.nom}" ?`)) {
      try {
        await apiProfesseurs.supprimer(professeur.ppr);
        chargerProfesseurs(); // Recharger la liste
      } catch (err) {
        alert('Erreur lors de la suppression: ' + err.message);
      }
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
    { 
      key: 'matieresEnseignees', 
      label: 'Matières Enseignées',
      render: (item) => (
        <div className="cellule-filieres">
          {item.matieresEnseignees && item.matieresEnseignees.length > 0 ? (
            <>
              <BookOpen size={14} />
              <span>{item.matieresEnseignees.length} matière(s)</span>
            </>
          ) : (
            <span className="texte-secondaire">Aucune</span>
          )}
        </div>
      )
    },
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
          texte={item.status || 'Inactif'}
        />
      )
    }
  ];

  // Filtrer les professeurs selon les critères sélectionnés
  const professeursFiltres = professeurs.filter(p => {
    // Filtre par département - comparer avec le code du département
    if (filtreDepartement && p.departement !== filtreDepartement) {
      return false;
    }
    // Note: Les filtres par filière et matière nécessiteraient des données supplémentaires
    // qui ne sont pas disponibles dans ProfesseurSortieDTO pour l'instant
    return true;
  });

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

      {/* Barre de tri */}
      <div className="gestion-professeurs-filtres">
        <div className="filtre-item">
          <label className="filtre-label">Département:</label>
          <select 
            className="filtre-select"
            value={filtreDepartement}
            onChange={(e) => setFiltreDepartement(e.target.value)}
          >
            <option value="">Tous les départements</option>
            {chargementDonnees ? (
              <option value="" disabled>Chargement...</option>
            ) : departements.length > 0 ? (
              departements.map(dep => (
                <option key={dep.value} value={dep.value}>
                  {dep.label}
                </option>
              ))
            ) : (
              <option value="" disabled>Aucun département disponible</option>
            )}
          </select>
        </div>
        <div className="filtre-item">
          <label className="filtre-label">Filière:</label>
          <select 
            className="filtre-select"
            value={filtreFiliere}
            onChange={(e) => setFiltreFiliere(e.target.value)}
          >
            <option value="">Toutes les filières</option>
            {chargementDonnees ? (
              <option value="" disabled>Chargement...</option>
            ) : filieres.length > 0 ? (
              filieres.map(filiere => (
                <option key={filiere.value} value={filiere.value}>
                  {filiere.label}
                </option>
              ))
            ) : (
              <option value="" disabled>Aucune filière disponible</option>
            )}
          </select>
        </div>
        <div className="filtre-item">
          <label className="filtre-label">Matière:</label>
          <select 
            className="filtre-select"
            value={filtreMatiere}
            onChange={(e) => setFiltreMatiere(e.target.value)}
          >
            <option value="">Toutes les matières</option>
            {chargementDonnees ? (
              <option value="" disabled>Chargement...</option>
            ) : matieres.length > 0 ? (
              matieres.map(matiere => (
                <option key={matiere.value} value={matiere.value}>
                  {matiere.label}
                </option>
              ))
            ) : (
              <option value="" disabled>Aucune matière disponible</option>
            )}
          </select>
        </div>
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des professeurs...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerProfesseurs} className="btn-reessayer">Réessayer</button>
        </div>
      ) : (
        <TableauDonnees
          titre="Liste des Professeurs"
          colonnes={colonnes}
          donnees={professeursFiltres}
          surAjouter={ouvrirModalAjout}
          surModifier={ouvrirModalModification}
          surSupprimer={supprimer}
          surVoir={(prof) => ouvrirModalAttribution(prof)}
        />
      )}

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
        {!professeurCourant && (
          <ChampFormulaire
            label="Mot de passe"
            nom="password"
            type="password"
            valeur={formulaire.password}
            onChange={gererChangement}
            placeholder="Mot de passe"
            requis
          />
        )}
        <ChampFormulaire
          label="CIN"
          nom="cin"
          valeur={formulaire.cin}
          onChange={gererChangement}
          placeholder="AB123456"
          requis
        />
        <ChampFormulaire
          label="Téléphone"
          nom="tel"
          type="tel"
          valeur={formulaire.tel}
          onChange={gererChangement}
          placeholder="0612345678"
        />
        <ChampFormulaire
          label="Département"
          nom="codeDepartementOuTravail"
          type="select"
          valeur={formulaire.codeDepartementOuTravail}
          onChange={gererChangement}
          options={departements}
          placeholder={departements.length === 0 ? "Chargement des départements..." : "Sélectionner un département (optionnel)"}
        />
        <ChampFormulaire
          label="Matières Enseignées"
          nom="matieresEnseignees"
          type="select"
          valeur={formulaire.matieresEnseignees}
          onChange={gererSelectionMatieres}
          options={matieres}
          multiple
          requis={false}
        />
        <div style={{ fontSize: '0.85rem', color: '#666', marginTop: '-10px', marginBottom: '10px', paddingLeft: '4px' }}>
          Maintenez Ctrl (ou Cmd sur Mac) pour sélectionner plusieurs matières. Matières sélectionnées : {formulaire.matieresEnseignees.length}
        </div>
        <ChampFormulaire
          label="Grade"
          nom="grade"
          type="text"
          valeur={formulaire.grade}
          onChange={gererChangement}
          placeholder="Ex: Professeur, Maître de Conférences, Maître Assistant, Assistant"
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
          nom="statut"
          type="select"
          valeur={formulaire.statut}
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

