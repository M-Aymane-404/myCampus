import { useState, useEffect } from 'react';
import { Users, GraduationCap, UserCheck } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import { apiEtudiants } from '../services/apiEtudiants';
import { apiStructure } from '../services/apiStructure';
import './GestionEtudiants.css';

const GestionEtudiants = () => {
  const [etudiants, setEtudiants] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  // Charger les étudiants depuis l'API
  const chargerEtudiants = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = await apiEtudiants.obtenirTous();
      // Mapper les données du backend vers le format frontend
      const etudiantsMapped = donnees.map(e => ({
        id: e.id,
        apogee: e.apogee,
        nom: e.nom,
        prenom: e.prenom,
        email: e.email,
        cin: e.cin,
        tel: e.tel,
        niveau: e.niveau,
        codeClasse: e.codeClasse || '', // Stocker le codeClasse pour le filtrage
        filiere: e.codeClasse || '', // Pour l'affichage (temporaire, on pourrait améliorer)
        anneeInscription: e.anneeInscription,
        semestreActuel: e.semestreActuel,
        etatInscription: e.etatInscription,
        dateNaissance: e.dateNaissance
      }));
      setEtudiants(etudiantsMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des étudiants:', err);
      setErreur('Erreur lors du chargement des étudiants. Vérifiez que le serveur backend est démarré.');
    } finally {
      setChargement(false);
    }
  };

  const [filieres, setFilieres] = useState([]);
  const [toutesLesClasses, setToutesLesClasses] = useState([]); // Toutes les classes avec leur filière
  const [classesFiltrees, setClassesFiltrees] = useState([]); // Classes filtrées par filière sélectionnée
  const [chargementFilieres, setChargementFilieres] = useState(true);
  const [filiereSelectionnee, setFiliereSelectionnee] = useState(''); // Filière sélectionnée dans le formulaire

  // Charger les filières depuis l'API
  const chargerFilieres = async () => {
    try {
      setChargementFilieres(true);
      const donnees = await apiStructure.obtenirToutesFilieres();
      console.log('Filieres reçues:', donnees);
      if (donnees && donnees.length > 0) {
        const filieresMapped = donnees.map(f => ({
          value: f.codeFiliere,
          label: f.nom || f.codeFiliere
        }));
        setFilieres(filieresMapped);
        console.log('Filieres mappées:', filieresMapped);
      } else {
        console.warn('Aucune filière trouvée dans la base de données');
        setFilieres([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des filières:', err);
      alert('Erreur lors du chargement des filières: ' + err.message);
      setFilieres([]);
    } finally {
      setChargementFilieres(false);
    }
  };

  // Charger les classes depuis l'API
  const chargerClasses = async () => {
    try {
      const donnees = await apiStructure.obtenirToutesClasses();
      console.log('Classes reçues:', donnees);
      if (donnees && donnees.length > 0) {
        // Stocker toutes les classes avec leur codeFiliere pour le filtrage
        const classesAvecFiliere = donnees.map(c => ({
          value: c.codeClasse,
          label: `${c.codeClasse} - ${c.nom || ''}`,
          codeFiliere: c.codeFiliere // Pour le filtrage
        }));
        setToutesLesClasses(classesAvecFiliere);
        console.log('Classes mappées:', classesAvecFiliere);
      } else {
        console.warn('Aucune classe trouvée dans la base de données');
        setToutesLesClasses([]);
        setClassesFiltrees([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des classes:', err);
      alert('Erreur lors du chargement des classes: ' + err.message);
      setToutesLesClasses([]);
      setClassesFiltrees([]);
    }
  };

  // Charger les classes d'une filière spécifique
  const chargerClassesParFiliere = async (codeFiliere) => {
    if (!codeFiliere) {
      setClassesFiltrees([]);
      return;
    }
    try {
      const donnees = await apiStructure.obtenirClassesParFiliere(codeFiliere);
      if (donnees && donnees.length > 0) {
        const classesMapped = donnees.map(c => ({
          value: c.codeClasse,
          label: `${c.codeClasse} - ${c.nom || ''}`
        }));
        setClassesFiltrees(classesMapped);
      } else {
        setClassesFiltrees([]);
      }
    } catch (err) {
      console.error('Erreur lors du chargement des classes par filière:', err);
      setClassesFiltrees([]);
    }
  };

  useEffect(() => {
    chargerEtudiants();
    chargerFilieres();
    chargerClasses();
  }, []);

  const [filtreFiliere, setFiltreFiliere] = useState('');

  const [modalOuvert, setModalOuvert] = useState(false);
  const [etudiantCourant, setEtudiantCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    apogee: '',
    nom: '',
    prenom: '',
    email: '',
    password: '',
    cin: '',
    tel: '',
    niveau: '',
    codeClasse: '',
    anneeInscription: '',
    semestreActuel: '',
    etatInscription: 'INSCRIT',
    dateNaissance: ''
  });

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

  const ouvrirModalAjout = () => {
    setEtudiantCourant(null);
    setFiliereSelectionnee('');
    setClassesFiltrees([]);
    setFormulaire({
      apogee: '',
      nom: '',
      prenom: '',
      email: '',
      password: '',
      cin: '',
      tel: '',
      niveau: '',
      codeFiliere: '',
      codeClasse: '',
      anneeInscription: new Date().getFullYear().toString(),
      semestreActuel: '',
      etatInscription: 'INSCRIT',
      dateNaissance: ''
    });
    setModalOuvert(true);
  };

  const ouvrirModalModification = async (etudiant) => {
    setEtudiantCourant(etudiant);
    
    // Trouver la filière de la classe de l'étudiant
    const classeEtudiant = toutesLesClasses.find(c => c.value === etudiant.codeClasse);
    const codeFiliereEtudiant = classeEtudiant ? classeEtudiant.codeFiliere : '';
    
    if (codeFiliereEtudiant) {
      setFiliereSelectionnee(codeFiliereEtudiant);
      // Charger les classes de cette filière
      await chargerClassesParFiliere(codeFiliereEtudiant);
    } else {
      setClassesFiltrees([]);
    }
    
    setFormulaire({
      apogee: etudiant.apogee,
      nom: etudiant.nom,
      prenom: etudiant.prenom,
      email: etudiant.email,
      password: '', // Ne pas pré-remplir le mot de passe en modification
      cin: etudiant.cin,
      tel: etudiant.tel,
      niveau: etudiant.niveau,
      codeFiliere: codeFiliereEtudiant,
      codeClasse: etudiant.codeClasse,
      anneeInscription: etudiant.anneeInscription.toString(),
      semestreActuel: etudiant.semestreActuel,
      etatInscription: etudiant.etatInscription,
      dateNaissance: etudiant.dateNaissance
    });
    setModalOuvert(true);
  };

  const gererChangement = async (e) => {
    const { name, value } = e.target;
    
    // Si on change la filière, charger les classes de cette filière et réinitialiser la classe
    if (name === 'codeFiliere') {
      setFiliereSelectionnee(value);
      if (value) {
        await chargerClassesParFiliere(value);
        setFormulaire(prev => ({ ...prev, [name]: value, codeClasse: '' }));
      } else {
        setClassesFiltrees([]);
        setFormulaire(prev => ({ ...prev, [name]: value, codeClasse: '' }));
      }
    } else {
      setFormulaire(prev => ({ ...prev, [name]: value }));
    }
  };

  const sauvegarder = async () => {
    // Validation basique
    if (!formulaire.apogee || !formulaire.nom || !formulaire.prenom || !formulaire.email || !formulaire.codeFiliere || !formulaire.codeClasse) {
      alert('Veuillez remplir tous les champs obligatoires (y compris Filière et Classe)');
      return;
    }

    // Le mot de passe est obligatoire uniquement pour la création
    if (!etudiantCourant && !formulaire.password) {
      alert('Le mot de passe est obligatoire pour créer un nouvel étudiant');
      return;
    }

    try {
      // Validation des champs obligatoires
      if (!formulaire.semestreActuel) {
        alert('Veuillez sélectionner un semestre');
        return;
      }
      
      if (!formulaire.dateNaissance) {
        alert('Veuillez sélectionner une date de naissance');
        return;
      }

      // Préparer les données pour l'API backend (format exact attendu par le backend)
      const donneesAPI = {
        apogee: formulaire.apogee.trim(),
        nom: formulaire.nom.trim(),
        prenom: formulaire.prenom.trim(),
        email: formulaire.email.trim(),
        cin: formulaire.cin.trim(),
        niveau: formulaire.niveau ? formulaire.niveau.trim() : null,
        codeClasse: formulaire.codeClasse.trim(),
        anneeInscription: parseInt(formulaire.anneeInscription),
        semestreActuel: formulaire.semestreActuel, // Enum: S1, S2, S3, etc.
        etatInscription: formulaire.etatInscription, // Enum: INSCRIT, SUSPENDU, DIPLOME
        dateNaissance: formulaire.dateNaissance, // Format: YYYY-MM-DD
        password: formulaire.password.trim() // Obligatoire pour création
      };

      // Le téléphone est optionnel - on l'envoie seulement s'il est rempli
      if (formulaire.tel && formulaire.tel.trim()) {
        donneesAPI.tel = formulaire.tel.trim();
      }

      // L'année de fin d'étude est optionnelle
      if (formulaire.anneeFinEtude) {
        donneesAPI.anneeFinEtude = formulaire.anneeFinEtude;
      }

      console.log('Données envoyées à l\'API:', JSON.stringify(donneesAPI, null, 2));

      if (etudiantCourant) {
        // Pour la modification, le mot de passe n'est pas obligatoire
        if (!formulaire.password || !formulaire.password.trim()) {
          delete donneesAPI.password;
        }
        await apiEtudiants.modifier(etudiantCourant.apogee, donneesAPI);
      } else {
        // Pour la création, le mot de passe est obligatoire (déjà ajouté ci-dessus)
        await apiEtudiants.creer(donneesAPI);
      }
      
      setModalOuvert(false);
      chargerEtudiants(); // Recharger la liste
    } catch (err) {
      console.error('Erreur détaillée:', err);
      const messageErreur = err.message || 'Erreur inconnue lors de la sauvegarde';
      alert('Erreur lors de la sauvegarde: ' + messageErreur);
    }
  };

  const supprimer = async (etudiant) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer l'étudiant "${etudiant.prenom} ${etudiant.nom}" ?`)) {
      try {
        await apiEtudiants.supprimer(etudiant.apogee);
        chargerEtudiants(); // Recharger la liste
      } catch (err) {
        alert('Erreur lors de la suppression: ' + err.message);
      }
    }
  };

  const parserCSV = (texte) => {
    const lignes = texte.split('\n').filter(ligne => ligne.trim());
    if (lignes.length === 0) return [];

    // Détecter le séparateur (virgule ou point-virgule)
    const premiereLigne = lignes[0];
    const separateur = premiereLigne.includes(';') ? ';' : ',';

    // Parser les lignes
    const resultats = [];
    for (let i = 0; i < lignes.length; i++) {
      const colonnes = lignes[i].split(separateur).map(col => col.trim().replace(/^"|"$/g, ''));
      
      // Ignorer la ligne d'en-tête si elle existe
      if (i === 0 && (
        colonnes[0].toLowerCase().includes('apogee') || 
        colonnes[0].toLowerCase().includes('nom') ||
        colonnes[0].toLowerCase().includes('apogée')
      )) {
        continue;
      }

      // Format attendu: apogee, nom, prenom, email, cin, tel, niveau, filiere, anneeInscription, semestreActuel, etatInscription, dateNaissance
      if (colonnes.length >= 4) {
        resultats.push({
          apogee: colonnes[0] || '',
          nom: colonnes[1] || '',
          prenom: colonnes[2] || '',
          email: colonnes[3] || '',
          cin: colonnes[4] || '',
          tel: colonnes[5] || '',
          niveau: colonnes[6] || 'L1',
          filiere: colonnes[7] || '',
          anneeInscription: colonnes[8] ? parseInt(colonnes[8]) || new Date().getFullYear() : new Date().getFullYear(),
          semestreActuel: colonnes[9] || 'S1',
          etatInscription: colonnes[10] || 'INSCRIT',
          dateNaissance: colonnes[11] || ''
        });
      }
    }
    return resultats;
  };

  const importerCSV = (fichier) => {
    const lecteur = new FileReader();
    
    lecteur.onload = async (e) => {
      try {
        const texte = e.target.result;
        const etudiantsImportes = parserCSV(texte);
        
        if (etudiantsImportes.length === 0) {
          alert('Aucun étudiant trouvé dans le fichier CSV. Vérifiez le format du fichier.');
          return;
        }

        // Valider les données
        const etudiantsValides = [];
        const erreurs = [];

        etudiantsImportes.forEach((etudiant, index) => {
          if (!etudiant.apogee || !etudiant.nom || !etudiant.prenom || !etudiant.email) {
            erreurs.push(`Ligne ${index + 1}: Champs obligatoires manquants (Apogée, Nom, Prénom, Email)`);
            return;
          }

          // Vérifier si l'étudiant existe déjà
          const existeDeja = etudiants.some(e => e.apogee === etudiant.apogee);
          if (existeDeja) {
            erreurs.push(`Ligne ${index + 1}: L'étudiant avec l'Apogée ${etudiant.apogee} existe déjà`);
            return;
          }

          etudiantsValides.push({
            id: etudiants.length + etudiantsValides.length + 1,
            ...etudiant
          });
        });

        if (erreurs.length > 0) {
          alert(`Erreurs détectées:\n${erreurs.slice(0, 5).join('\n')}${erreurs.length > 5 ? `\n... et ${erreurs.length - 5} autres erreurs` : ''}`);
        }

        if (etudiantsValides.length > 0) {
          // Send POST request to backend API
          try {
            const response = await fetch('/api/import-csv/etudiants', {
              method: 'POST',
              headers: {
                'Content-Type': 'application/json',
              },
              body: JSON.stringify({ etudiants: etudiantsValides })
            });

            if (response.ok) {
              const result = await response.json();
              // Reload data from backend to ensure consistency
              chargerEtudiants();
              alert(`${etudiantsValides.length} étudiant(s) importé(s) avec succès${erreurs.length > 0 ? `, ${erreurs.length} erreur(s) détectée(s)` : ''}`);
            } else {
              throw new Error('Erreur lors de l\'envoi au serveur');
            }
          } catch (apiError) {
            console.error('Erreur API:', apiError);
            // Fallback: save to local state if API fails
            setEtudiants(prev => [...prev, ...etudiantsValides]);
            alert(`${etudiantsValides.length} étudiant(s) importé(s) localement (erreur serveur: ${apiError.message})${erreurs.length > 0 ? `, ${erreurs.length} erreur(s) détectée(s)` : ''}`);
          }
        } else {
          alert('Aucun étudiant valide à importer.');
        }
      } catch (erreur) {
        console.error('Erreur lors de l\'import CSV:', erreur);
        alert('Erreur lors de la lecture du fichier CSV. Vérifiez le format du fichier.');
      }
    };

    lecteur.onerror = () => {
      alert('Erreur lors de la lecture du fichier.');
    };

    lecteur.readAsText(fichier, 'UTF-8');
  };

  const colonnes = [
    { key: 'apogee', label: 'Apogée' },
    { 
      key: 'nom', 
      label: 'Nom Complet',
      render: (item) => `${item.prenom} ${item.nom}`
    },
    { key: 'email', label: 'Email' },
    { key: 'filiere', label: 'Filière' },
    { key: 'niveau', label: 'Niveau' },
    { key: 'semestreActuel', label: 'Semestre' },
    { 
      key: 'etatInscription', 
      label: 'État',
      render: (item) => {
        const etatMap = {
          'INSCRIT': { statut: 'success', texte: 'Inscrit' },
          'SUSPENDU': { statut: 'warning', texte: 'Suspendu' },
          'DIPLOME': { statut: 'info', texte: 'Diplômé' }
        };
        const etat = etatMap[item.etatInscription] || { statut: 'error', texte: item.etatInscription };
        return <BadgeStatut statut={etat.statut} texte={etat.texte} />;
      }
    },
    { key: 'anneeInscription', label: 'Année' }
  ];

  // Filtrer les étudiants selon la filière sélectionnée
  const etudiantsFiltres = filtreFiliere 
    ? etudiants.filter(e => {
        // Trouver la classe de l'étudiant dans toutesLesClasses
        const classeEtudiant = toutesLesClasses.find(c => c.value === e.codeClasse);
        // Vérifier si cette classe appartient à la filière sélectionnée
        return classeEtudiant && classeEtudiant.codeFiliere === filtreFiliere;
      })
    : etudiants;

  const totalEtudiants = etudiants.length;
  const etudiantsInscrits = etudiants.filter(e => e.etatInscription === 'INSCRIT').length;
  const etudiantsDiplomes = etudiants.filter(e => e.etatInscription === 'DIPLOME').length;

  return (
    <div className="gestion-etudiants">
      <div className="gestion-etudiants-header">
        <h1 className="gestion-etudiants-titre">Gestion des Étudiants</h1>
        <p className="gestion-etudiants-sous-titre">Gestion et attribution aux filières</p>
      </div>

     

      <div className="gestion-etudiants-statistiques">
        <CarteStatistique
          titre="Total Étudiants"
          valeur={totalEtudiants}
          variation={{ valeur: 45, label: '', positif: true }}
          icone={Users}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Étudiants Inscrits"
          valeur={etudiantsInscrits}
          variation={{ valeur: etudiantsInscrits, label: '', positif: true }}
          icone={UserCheck}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Diplômés"
          valeur={etudiantsDiplomes}
          variation={{ valeur: etudiantsDiplomes, label: '', positif: true }}
          icone={GraduationCap}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

       {/* Barre de tri */}
       <div className="gestion-etudiants-filtres">
        <div className="filtre-item">
          <label className="filtre-label">Filière:</label>
          <select 
            className="filtre-select"
            value={filtreFiliere}
            onChange={(e) => setFiltreFiliere(e.target.value)}
            disabled={chargementFilieres}
          >
            <option value="">Toutes les filières</option>
            {filieres.length > 0 ? (
              filieres.map(filiere => (
                <option key={filiere.value} value={filiere.value}>
                  {filiere.label}
                </option>
              ))
            ) : (
              !chargementFilieres && <option value="" disabled>Aucune filière disponible</option>
            )}
          </select>
          {chargementFilieres && <span style={{ marginLeft: '10px', fontSize: '0.9rem', color: '#666' }}>Chargement des filières...</span>}
          {!chargementFilieres && filieres.length === 0 && (
            <span style={{ marginLeft: '10px', fontSize: '0.9rem', color: '#dc2626' }}>Aucune filière trouvée</span>
          )}
        </div>
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des étudiants...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerEtudiants} className="btn-reessayer">Réessayer</button>
        </div>
      ) : (
        <TableauDonnees
          titre="Liste des Étudiants"
          colonnes={colonnes}
          donnees={etudiantsFiltres}
          surAjouter={ouvrirModalAjout}
          surModifier={ouvrirModalModification}
          surSupprimer={supprimer}
          surImporterCSV={importerCSV}
        />
      )}

      <FormulaireModal
        titre={etudiantCourant ? 'Modifier l\'Étudiant' : 'Ajouter un Étudiant'}
        estOuvert={modalOuvert}
        surFermer={() => setModalOuvert(false)}
        surSauvegarder={sauvegarder}
      >
        <ChampFormulaire
          label="Numéro Apogée"
          nom="apogee"
          valeur={formulaire.apogee}
          onChange={gererChangement}
          placeholder="Ex: APG001"
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
          placeholder="email@student.univ.ma"
          requis
        />
        {!etudiantCourant && (
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
          label="Date de Naissance"
          nom="dateNaissance"
          type="date"
          valeur={formulaire.dateNaissance}
          onChange={gererChangement}
          requis
        />
        <ChampFormulaire
          label="Filière"
          nom="codeFiliere"
          type="select"
          valeur={filiereSelectionnee || formulaire.codeFiliere || ''}
          onChange={gererChangement}
          options={filieres}
          requis
        />
        <ChampFormulaire
          label="Classe"
          nom="codeClasse"
          type="select"
          valeur={formulaire.codeClasse}
          onChange={gererChangement}
          options={classesFiltrees}
          requis
          disabled={!filiereSelectionnee && !formulaire.codeFiliere}
          placeholder={!filiereSelectionnee && !formulaire.codeFiliere ? "Sélectionnez d'abord une filière" : "Sélectionner..."}
        />
        <ChampFormulaire
          label="Niveau"
          nom="niveau"
          type="select"
          valeur={formulaire.niveau}
          onChange={gererChangement}
          options={niveaux}
          requis
        />
        <ChampFormulaire
          label="Semestre Actuel"
          nom="semestreActuel"
          type="select"
          valeur={formulaire.semestreActuel}
          onChange={gererChangement}
          options={semestres}
          requis
        />
        <ChampFormulaire
          label="Année d'Inscription"
          nom="anneeInscription"
          type="number"
          valeur={formulaire.anneeInscription}
          onChange={gererChangement}
          requis
        />
        <ChampFormulaire
          label="État d'Inscription"
          nom="etatInscription"
          type="select"
          valeur={formulaire.etatInscription}
          onChange={gererChangement}
          options={etatsInscription}
          requis
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionEtudiants;

