import { useState } from 'react';
import { Users, GraduationCap, UserCheck } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import './GestionEtudiants.css';

const GestionEtudiants = () => {
  const [etudiants, setEtudiants] = useState([
    {
      id: 1,
      apogee: 'APG001',
      nom: 'Alaoui',
      prenom: 'Hassan',
      email: 'hassan.alaoui@student.univ.ma',
      cin: 'AB123456',
      tel: '0612345678',
      niveau: 'L3',
      filiere: 'GI-2024',
      anneeInscription: 2022,
      semestreActuel: 'S6',
      etatInscription: 'INSCRIT',
      dateNaissance: '2000-05-15'
    },
    {
      id: 2,
      apogee: 'APG002',
      nom: 'Bennani',
      prenom: 'Sara',
      email: 'sara.bennani@student.univ.ma',
      cin: 'CD789012',
      tel: '0698765432',
      niveau: 'M1',
      filiere: 'GI-2024',
      anneeInscription: 2021,
      semestreActuel: 'S2',
      etatInscription: 'INSCRIT',
      dateNaissance: '1999-08-20'
    },
    {
      id: 3,
      apogee: 'APG003',
      nom: 'Chraibi',
      prenom: 'Omar',
      email: 'omar.chraibi@student.univ.ma',
      cin: 'EF345678',
      tel: '0654321098',
      niveau: 'L2',
      filiere: 'GIL-2024',
      anneeInscription: 2023,
      semestreActuel: 'S4',
      etatInscription: 'SUSPENDU',
      dateNaissance: '2001-12-10'
    }
  ]);

  const [filieres] = useState([
    { value: 'GI-2024', label: 'Génie Informatique' },
    { value: 'GIL-2024', label: 'Génie Industriel et Logistique' },
    { value: 'GC-2024', label: 'Génie Civil' }
  ]);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [etudiantCourant, setEtudiantCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    apogee: '',
    nom: '',
    prenom: '',
    email: '',
    cin: '',
    tel: '',
    niveau: '',
    filiere: '',
    anneeInscription: '',
    semestreActuel: '',
    etatInscription: 'INSCRIT',
    dateNaissance: ''
  });

  const niveaux = [
    { value: 'L1', label: 'Licence 1' },
    { value: 'L2', label: 'Licence 2' },
    { value: 'L3', label: 'Licence 3' },
    { value: 'M1', label: 'Master 1' },
    { value: 'M2', label: 'Master 2' }
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
    setFormulaire({
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
    setModalOuvert(true);
  };

  const ouvrirModalModification = (etudiant) => {
    setEtudiantCourant(etudiant);
    setFormulaire({
      apogee: etudiant.apogee,
      nom: etudiant.nom,
      prenom: etudiant.prenom,
      email: etudiant.email,
      cin: etudiant.cin,
      tel: etudiant.tel,
      niveau: etudiant.niveau,
      filiere: etudiant.filiere,
      anneeInscription: etudiant.anneeInscription.toString(),
      semestreActuel: etudiant.semestreActuel,
      etatInscription: etudiant.etatInscription,
      dateNaissance: etudiant.dateNaissance
    });
    setModalOuvert(true);
  };

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarder = () => {
    // Validation basique
    if (!formulaire.apogee || !formulaire.nom || !formulaire.prenom || !formulaire.email || !formulaire.filiere) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    if (etudiantCourant) {
      setEtudiants(prev => prev.map(e => 
        e.id === etudiantCourant.id 
          ? { ...e, ...formulaire, anneeInscription: parseInt(formulaire.anneeInscription) }
          : e
      ));
    } else {
      const nouvelEtudiant = {
        id: etudiants.length + 1,
        ...formulaire,
        anneeInscription: parseInt(formulaire.anneeInscription)
      };
      setEtudiants(prev => [...prev, nouvelEtudiant]);
    }
    setModalOuvert(false);
  };

  const supprimer = (etudiant) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer l'étudiant "${etudiant.prenom} ${etudiant.nom}" ?`)) {
      setEtudiants(prev => prev.filter(e => e.id !== etudiant.id));
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
    
    lecteur.onload = (e) => {
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
          setEtudiants(prev => [...prev, ...etudiantsValides]);
          alert(`${etudiantsValides.length} étudiant(s) importé(s) avec succès${erreurs.length > 0 ? `, ${erreurs.length} erreur(s) détectée(s)` : ''}`);
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

      <TableauDonnees
        titre="Liste des Étudiants"
        colonnes={colonnes}
        donnees={etudiants}
        surAjouter={ouvrirModalAjout}
        surModifier={ouvrirModalModification}
        surSupprimer={supprimer}
        surImporterCSV={importerCSV}
      />

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
          nom="filiere"
          type="select"
          valeur={formulaire.filiere}
          onChange={gererChangement}
          options={filieres}
          requis
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

