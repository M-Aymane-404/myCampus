import { useState, useEffect } from 'react';
import { Megaphone, Info, Eye } from 'lucide-react';
import BoutonAction from '../composants/communs/BoutonAction';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import apiAnnonces from '../services/apiAnnonces';
import apiNotifications from '../services/apiNotifications';
import { apiStructure } from '../services/apiStructure';
import { apiAuth } from '../services/apiAuth';
import './GestionAnnoncesAdmin.css';

const GestionAnnoncesAdmin = () => {
  const [annonces, setAnnonces] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);
  const [modalNouvelleAnnonce, setModalNouvelleAnnonce] = useState(false);
  const [classes, setClasses] = useState([]);
  const [chargementClasses, setChargementClasses] = useState(true);
  const [formulaire, setFormulaire] = useState({
    titre: '',
    type: '',
    classe: '',
    contenu: ''
  });

  useEffect(() => {
    chargerAnnonces();
    chargerClasses();
  }, []);

  const chargerClasses = async () => {
    try {
      setChargementClasses(true);
      const donnees = await apiStructure.obtenirToutesClasses();
      const classesMapped = donnees.map(c => ({
        value: c.codeClasse || c.id?.toString(),
        label: `${c.codeClasse || ''} - ${c.nom || c.libelle || ''}`.trim()
      }));
      setClasses(classesMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des classes:', err);
      setClasses([]);
    } finally {
      setChargementClasses(false);
    }
  };

  const chargerAnnonces = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const annoncesChargees = await apiAnnonces.obtenirToutes();
      setAnnonces(annoncesChargees);
    } catch (err) {
      console.error('Erreur lors du chargement des annonces:', err);
      setErreur('Erreur lors du chargement des annonces');
    } finally {
      setChargement(false);
    }
  };

  const typesAnnonce = [
    { value: '', label: '-- Sélectionner --' },
    { value: 'Urgent', label: 'Urgent (DS, Examen)' },
    { value: 'Normal', label: 'Normal' },
    { value: 'Info', label: 'Information' }
  ];

  const classesOptions = [
    { value: '', label: '-- Sélectionner --' },
    ...classes
  ];

  const gererChangement = (e) => {
    const { name, value } = e.target;
    setFormulaire(prev => ({ ...prev, [name]: value }));
  };

  const publierAnnonce = async () => {
    if (!formulaire.titre || !formulaire.type || !formulaire.classe || !formulaire.contenu) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    try {
      // Get current user for utilisateur_id
      const currentUser = apiAuth.getUser();
      const utilisateurId = currentUser?.id || 1; // Default to 1 if not available

      // Create notification in the database
      const notificationData = {
        titre: formulaire.titre,
        message: formulaire.contenu,
        type: formulaire.type,
        utilisateur_id: utilisateurId,
        lu: false,
        date_envoi: new Date().toISOString(),
        date_lecture: null
      };

      // Save to notification table
      await apiNotifications.creer(notificationData);

      // Also create announcement (if needed for backward compatibility)
      try {
        await apiAnnonces.creer({
          titre: formulaire.titre,
          type: formulaire.type,
          classe: classes.find(c => c.value === formulaire.classe)?.label || formulaire.classe,
          contenu: formulaire.contenu,
          classeCode: formulaire.classe
        });
      } catch (announcementErr) {
        console.warn('Erreur lors de la création de l\'annonce (non bloquant):', announcementErr);
      }

      await chargerAnnonces();
      alert('Annonce publiée avec succès');
      setModalNouvelleAnnonce(false);
      setFormulaire({
        titre: '',
        type: '',
        classe: '',
        contenu: ''
      });
    } catch (err) {
      alert('Erreur lors de la publication de l\'annonce: ' + (err.message || 'Erreur inconnue'));
      console.error(err);
    }
  };

  const supprimerAnnonce = async (id) => {
    if (window.confirm('Êtes-vous sûr de vouloir supprimer cette annonce ?')) {
      try {
        await apiAnnonces.supprimer(id);
        await chargerAnnonces();
        console.log('Announcement deleted:', id);
      } catch (err) {
        alert('Erreur lors de la suppression: ' + (err.message || 'Erreur inconnue'));
        console.error(err);
      }
    }
  };

  const obtenirConseils = () => {
    if (formulaire.type === 'Urgent') {
      return '• Pour un DS/Examen : date, heure, salle, contenu, documents autorisés';
    } else if (formulaire.type === 'Normal') {
      return '• Pour un devoir : sujet, date limite, format de rendu';
    } else if (formulaire.type === 'Info') {
      return '• Pour une info : détails complets';
    }
    return '';
  };

  return (
    <div className="gestion-annonces-admin">
      <div className="gestion-annonces-admin-header">
        <div>
          <h1 className="gestion-annonces-admin-titre">Gestion des Annonces</h1>
          <p className="gestion-annonces-admin-sous-titre">
            Publiez des annonces importantes pour les étudiants
          </p>
        </div>
        <BoutonAction
          libelle="Publier une annonce"
          variante="primaire"
          taille="moyen"
          icone={Megaphone}
          onClick={() => setModalNouvelleAnnonce(true)}
        />
      </div>

      <div className="annonces-liste">
        {chargement ? (
          <div className="chargement-container">Chargement des annonces...</div>
        ) : erreur ? (
          <div className="erreur-container">
            <p>{erreur}</p>
            <button onClick={chargerAnnonces} className="btn-reessayer">Réessayer</button>
          </div>
        ) : annonces.length === 0 ? (
          <div className="annonces-vide">
            <p>Aucune annonce publiée</p>
          </div>
        ) : (
          annonces.map((annonce) => (
            <div key={annonce.id} className="annonce-card">
              <div className="annonce-card-header">
                <div className="annonce-header-gauche">
                  <div className={`annonce-icone annonce-icone-${annonce.type?.toLowerCase() || 'normal'}`}>
                    <Megaphone size={24} />
                  </div>
                  <div className="annonce-header-info">
                    <div className="annonce-titre-container">
                      <h3 className="annonce-titre">{annonce.titre}</h3>
                      {annonce.type === 'Urgent' && (
                        <span className="badge-urgent">URGENT</span>
                      )}
                    </div>
                    <p className="annonce-metadata">
                      Publié {new Date(annonce.dateCreation).toLocaleString('fr-FR')} • {annonce.classe}
                    </p>
                  </div>
                </div>
                <button
                  className="annonce-supprimer"
                  onClick={() => supprimerAnnonce(annonce.id)}
                  aria-label="Supprimer l'annonce"
                >
                  ×
                </button>
              </div>

              <div className="annonce-contenu">
                <div className="annonce-detail-item">
                  <div className="annonce-detail-content">
                    <span className="annonce-detail-label">Contenu:</span>
                    <span className="annonce-detail-valeur">{annonce.contenu}</span>
                  </div>
                </div>
              </div>

              {annonce.etudiantsConcernes > 0 && (
                <div className="annonce-card-footer">
                  <div className="annonce-etudiants-concernes">
                    <Eye size={16} />
                    <span>{annonce.etudiantsConcernes} étudiants concernés</span>
                  </div>
                </div>
              )}
            </div>
          ))
        )}
      </div>

      {/* Modal Nouvelle Annonce */}
      <FormulaireModal
        titre="Publier une annonce"
        estOuvert={modalNouvelleAnnonce}
        surFermer={() => {
          setModalNouvelleAnnonce(false);
          setFormulaire({
            titre: '',
            type: '',
            classe: '',
            contenu: ''
          });
        }}
        surSauvegarder={publierAnnonce}
        texteBoutonSauvegarder="Publier l'annonce"
      >
        <ChampFormulaire
          label="Titre de l'annonce"
          nom="titre"
          valeur={formulaire.titre}
          onChange={gererChangement}
          placeholder="Ex: Devoir Surveillé - Programmation Java"
          requis
        />

        <div className="formulaire-row">
          <div className="formulaire-col">
            <ChampFormulaire
              label="Type d'annonce"
              nom="type"
              type="select"
              valeur={formulaire.type}
              onChange={gererChangement}
              options={typesAnnonce}
              requis
            />
          </div>
          <div className="formulaire-col">
            <ChampFormulaire
              label="Classe concernée"
              nom="classe"
              type="select"
              valeur={formulaire.classe}
              onChange={gererChangement}
              options={chargementClasses ? [{ value: '', label: 'Chargement...' }] : classesOptions}
              requis
              disabled={chargementClasses}
            />
          </div>
        </div>

        <ChampFormulaire
          label="Contenu de l'annonce"
          nom="contenu"
          type="textarea"
          valeur={formulaire.contenu}
          onChange={gererChangement}
          placeholder="Détails de l'annonce..."
          requis
        />

        {formulaire.type && (
          <div className="conseils-box">
            <strong>Conseils :</strong>
            <p>{obtenirConseils()}</p>
          </div>
        )}

        <div className="info-box">
          <Info size={18} />
          <p>Les étudiants de la classe sélectionnée recevront une notification pour cette annonce.</p>
        </div>
      </FormulaireModal>
    </div>
  );
};

export default GestionAnnoncesAdmin;
