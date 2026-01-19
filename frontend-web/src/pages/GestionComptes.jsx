import { useState, useEffect } from 'react';
import { Users, UserCheck, Shield } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import { apiComptes } from '../services/apiComptes';
import './GestionComptes.css';

const GestionComptes = () => {
  const [comptes, setComptes] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  // Charger les comptes depuis l'API
  const chargerComptes = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = await apiComptes.obtenirTous();
      console.log('Comptes reçus:', donnees);

      // Mapper les données du backend vers le format frontend
      const comptesMapped = donnees.map(c => ({
        id: c.id,
        nom: c.nom,
        prenom: c.prenom,
        email: c.email,
        role: c.role, // Déjà ajouté par apiComptes (Professeur, Étudiant, Administrateur)
        enabled: c.enabled !== undefined ? c.enabled : true,
        dateCreation: c.dateCreation ? new Date(c.dateCreation).toISOString().split('T')[0] : 'N/A',
        dateDerniereConnexion: c.dateDerniereConnexion ? new Date(c.dateDerniereConnexion).toISOString().split('T')[0] : 'Jamais'
      }));

      setComptes(comptesMapped);
      console.log('Comptes mappés:', comptesMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des comptes:', err);
      setErreur('Erreur lors du chargement des comptes. Vérifiez que le serveur backend est démarré.');
    } finally {
      setChargement(false);
    }
  };

  useEffect(() => {
    chargerComptes();
  }, []);

  const [modalOuvert, setModalOuvert] = useState(false);
  const [compteCourant, setCompteCourant] = useState(null);
  const [formulaire, setFormulaire] = useState({
    nom: '',
    prenom: '',
    email: '',
    password: '',
    role: '',
    enabled: true
  });

  const roles = [
    { value: 'Administrateur', label: 'Administrateur' },
    { value: 'Professeur', label: 'Professeur' },
    { value: 'Étudiant', label: 'Étudiant' }
  ];

  const ouvrirModalAjout = () => {
    setCompteCourant(null);
    setFormulaire({
      nom: '',
      prenom: '',
      email: '',
      password: '',
      role: '',
      enabled: true
    });
    setModalOuvert(true);
  };

  const ouvrirModalModification = (compte) => {
    setCompteCourant(compte);
    setFormulaire({
      nom: compte.nom,
      prenom: compte.prenom,
      email: compte.email,
      password: '',
      role: compte.role,
      enabled: compte.enabled
    });
    setModalOuvert(true);
  };

  const gererChangement = (e) => {
    const { name, value, type, checked } = e.target;
    setFormulaire(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const sauvegarder = () => {
    if (compteCourant) {
      setComptes(prev => prev.map(c =>
        c.id === compteCourant.id
          ? {
            ...c,
            ...formulaire,
            dateCreation: c.dateCreation,
            dateDerniereConnexion: c.dateDerniereConnexion
          }
          : c
      ));
    } else {
      const nouveauCompte = {
        id: comptes.length + 1,
        ...formulaire,
        dateCreation: new Date().toISOString().split('T')[0],
        dateDerniereConnexion: null
      };
      setComptes(prev => [...prev, nouveauCompte]);
    }
    setModalOuvert(false);
  };

  const supprimer = (compte) => {
    if (window.confirm(`Êtes-vous sûr de vouloir supprimer le compte "${compte.prenom} ${compte.nom}" ?`)) {
      setComptes(prev => prev.filter(c => c.id !== compte.id));
    }
  };

  const colonnes = [
    {
      key: 'nom',
      label: 'Nom Complet',
      render: (item) => `${item.prenom} ${item.nom}`
    },
    { key: 'email', label: 'Email' },
    {
      key: 'role',
      label: 'Rôle',
      render: (item) => (
        <BadgeStatut
          statut={item.role === 'Administrateur' ? 'error' : item.role === 'Professeur' ? 'info' : 'success'}
          texte={item.role}
        />
      )
    },
    {
      key: 'enabled',
      label: 'Statut',
      render: (item) => (
        <BadgeStatut
          statut={item.enabled ? 'success' : 'error'}
          texte={item.enabled ? 'Actif' : 'Inactif'}
        />
      )
    },
    { key: 'dateCreation', label: 'Date Création' },
    { key: 'dateDerniereConnexion', label: 'Dernière Connexion' }
  ];

  const totalComptes = comptes.length;
  const comptesActifs = comptes.filter(c => c.enabled).length;
  const administrateurs = comptes.filter(c => c.role === 'Administrateur').length;

  return (
    <div className="gestion-comptes">
      <div className="gestion-comptes-header">
        <h1 className="gestion-comptes-titre">Gestion des Comptes</h1>
        <p className="gestion-comptes-sous-titre">Création, modification et suppression des comptes utilisateurs</p>
      </div>

      <div className="gestion-comptes-statistiques">
        <CarteStatistique
          titre="Total Comptes"
          valeur={totalComptes}
          variation={{ valeur: 5, label: '', positif: true }}
          icone={Users}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Comptes Actifs"
          valeur={comptesActifs}
          variation={{ valeur: comptesActifs, label: '', positif: true }}
          icone={UserCheck}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Administrateurs"
          valeur={administrateurs}
          variation={{ valeur: administrateurs, label: '', positif: true }}
          icone={Shield}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des comptes...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerComptes} className="btn-reessayer">Réessayer</button>
        </div>
      ) : (
        <TableauDonnees
          titre="Liste des Comptes"
          colonnes={colonnes}
          donnees={comptes}
          surAjouter={ouvrirModalAjout}
          surModifier={ouvrirModalModification}
          surSupprimer={supprimer}
        />
      )}

      <FormulaireModal
        titre={compteCourant ? 'Modifier le Compte' : 'Créer un Compte'}
        estOuvert={modalOuvert}
        surFermer={() => setModalOuvert(false)}
        surSauvegarder={sauvegarder}
      >
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
          label="Mot de passe"
          nom="password"
          type="password"
          valeur={formulaire.password}
          onChange={gererChangement}
          placeholder={compteCourant ? "Laisser vide pour ne pas changer" : "Mot de passe"}
          requis={!compteCourant}
        />
        <ChampFormulaire
          label="Rôle"
          nom="role"
          type="select"
          valeur={formulaire.role}
          onChange={gererChangement}
          options={roles}
          requis
        />
        <div className="champ-formulaire">
          <label className="champ-formulaire-label">
            <input
              type="checkbox"
              name="enabled"
              checked={formulaire.enabled}
              onChange={gererChangement}
            />
            <span style={{ marginLeft: '8px' }}>Compte activé</span>
          </label>
        </div>
      </FormulaireModal>
    </div>
  );
};

export default GestionComptes;

