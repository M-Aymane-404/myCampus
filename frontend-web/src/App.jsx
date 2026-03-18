import { useEffect, useRef, useState } from 'react';
import { AlertCircle, CheckCircle2, Info, X } from 'lucide-react';
import Login from './pages/Login';
import ConteneurPrincipal from './composants/layout/ConteneurPrincipal';
import ConteneurProfesseur from './composants/layout/ConteneurProfesseur';
import ConteneurEtudiant from './composants/layout/ConteneurEtudiant';
import TableauDeBordAdmin from './pages/TableauDeBordAdmin';
import GestionDepartements from './pages/GestionDepartements';
import GestionFilieres from './pages/GestionFilieres';
import GestionModules from './pages/GestionModules';
import GestionClasses from './pages/GestionClasses';
import GestionEmploiTemps from './pages/GestionEmploiTemps';
import GestionComptes from './pages/GestionComptes';
import GestionAnnoncesAdmin from './pages/GestionAnnoncesAdmin';
import TableauDeBordProfesseur from './pages/TableauDeBordProfesseur';
import GestionCoursProfesseur from './pages/GestionCoursProfesseur';
import GestionNotesProfesseur from './pages/GestionNotesProfesseur';
import GestionPresenceProfesseur from './pages/GestionPresenceProfesseur';
import GestionAnnoncesProfesseur from './pages/GestionAnnoncesProfesseur';
import TableauDeBordEtudiant from './pages/TableauDeBordEtudiant';
import CoursBibliotheque from './pages/CoursBibliotheque';
import NotesEtudiant from './pages/NotesEtudiant';
import PresenceEtudiant from './pages/PresenceEtudiant';
import CalendrierEtudiant from './pages/CalendrierEtudiant';
import AnnoncesEtudiant from './pages/AnnoncesEtudiant';
import ProfilPage from './pages/ProfilPage';
import ProfilProfesseur from './pages/ProfilProfesseur';
import ChatBot from './composants/ChatBot/ChatBot';
import { apiAuth } from './services/apiAuth';
import { apiAuditLogs } from './services/apiAuditLogs';
import CalendrierAcademiqueProfesseur from './pages/CalendrierAcademiqueProfesseur';
import './App.css';

const DUREE_AFFICHAGE_MESSAGE_MS = 6000;

const detecterTypeMessage = (message) => {
  const texte = String(message || '').toLowerCase();

  if (
    texte.includes('erreur')
    || texte.includes('impossible')
    || texte.includes('introuvable')
    || texte.includes('echec')
    || texte.includes('échou')
  ) {
    return 'error';
  }

  if (
    texte.includes('succes')
    || texte.includes('succès')
    || texte.includes('reussi')
    || texte.includes('réussi')
    || texte.includes('mise à jour')
    || texte.includes('mis à jour')
    || texte.includes('supprimé')
    || texte.includes('créé')
    || texte.includes('termine')
    || texte.includes('terminé')
  ) {
    return 'success';
  }

  if (
    texte.includes('veuillez')
    || texte.includes('attention')
    || texte.includes('obligatoire')
  ) {
    return 'warning';
  }

  return 'info';
};

const obtenirTitreMessage = (type) => {
  switch (type) {
    case 'success':
      return 'Succes';
    case 'warning':
      return 'Attention';
    case 'error':
      return 'Erreur';
    default:
      return 'Information';
  }
};

const obtenirIconeMessage = (type) => {
  switch (type) {
    case 'success':
      return <CheckCircle2 size={18} />;
    case 'warning':
    case 'error':
      return <AlertCircle size={18} />;
    default:
      return <Info size={18} />;
  }
};

function App() {
  const [utilisateurConnecte, setUtilisateurConnecte] = useState(null);
  const [elementActif, setElementActif] = useState('tableau-de-bord');
  const [navigationState, setNavigationState] = useState({
    filiereSelectionnee: null,
    classeSelectionnee: null
  });
  const [messagesGlobaux, setMessagesGlobaux] = useState([]);
  const temporisateursMessagesRef = useRef(new Map());

  const fermerMessageGlobal = (idMessage) => {
    const timeoutId = temporisateursMessagesRef.current.get(idMessage);
    if (timeoutId) {
      window.clearTimeout(timeoutId);
      temporisateursMessagesRef.current.delete(idMessage);
    }

    setMessagesGlobaux((precedents) =>
      precedents.filter((message) => message.id !== idMessage)
    );
  };

  const afficherMessageGlobal = (message, options = {}) => {
    const texte = String(message ?? '').trim();
    if (!texte) {
      return;
    }

    const idMessage = `${Date.now()}-${Math.random().toString(36).slice(2, 8)}`;
    const type = options.type || detecterTypeMessage(texte);

    setMessagesGlobaux((precedents) => [
      ...precedents,
      {
        id: idMessage,
        texte,
        type,
      },
    ]);

    const timeoutId = window.setTimeout(() => {
      temporisateursMessagesRef.current.delete(idMessage);
      setMessagesGlobaux((precedents) =>
        precedents.filter((messageGlobal) => messageGlobal.id !== idMessage)
      );
    }, options.duree ?? DUREE_AFFICHAGE_MESSAGE_MS);

    temporisateursMessagesRef.current.set(idMessage, timeoutId);
  };

  const gererLogin = (donneesLogin) => {
    const role = donneesLogin.role;
    const userData = {
      email: donneesLogin.email,
      role: role,
      user: donneesLogin.user
    };

    setUtilisateurConnecte(userData);

    // Redirection based on role
    if (role === 'admin') {
      setElementActif('tableau-de-bord');
    } else if (role === 'professor') {
      setElementActif('tableau-de-bord-professeur');
    } else if (role === 'student') {
      setElementActif('tableau-de-bord-etudiant');
    } else {
      setElementActif('tableau-de-bord');
    }
  };

  // Check for existing session on mount
  useEffect(() => {
    apiAuditLogs.initClientCapture();
    if (apiAuth.isAuthenticated()) {
      const userData = apiAuth.getUser();
      if (userData) {
        // Normalize role
        let role = userData.role?.toLowerCase();
        if (role === 'administrateur' || role === 'administrator' || role === 'admin') {
          role = 'admin';
        } else if (role === 'professeur' || role === 'professor' || role === 'prof') {
          role = 'professor';
        } else if (role === 'etudiant' || role === 'student' || role === 'etud') {
          role = 'student';
        }

        setUtilisateurConnecte({
          email: userData.email,
          role: role,
          user: userData
        });

        // Set default active element based on role
        if (role === 'professor') {
          setElementActif('tableau-de-bord-professeur');
        } else if (role === 'admin') {
          setElementActif('tableau-de-bord');
        } else if (role === 'student') {
          setElementActif('tableau-de-bord-etudiant');
        }
      }
    }
  }, []);

  useEffect(() => {
    const alerteOriginale = window.alert;
    const notifyOriginal = window.__MYCAMPUS_NOTIFY__;

    window.__MYCAMPUS_NOTIFY__ = afficherMessageGlobal;
    window.alert = (message) => {
      afficherMessageGlobal(message);
    };

    return () => {
      window.alert = alerteOriginale;

      if (notifyOriginal) {
        window.__MYCAMPUS_NOTIFY__ = notifyOriginal;
      } else {
        delete window.__MYCAMPUS_NOTIFY__;
      }

      temporisateursMessagesRef.current.forEach((timeoutId) => {
        window.clearTimeout(timeoutId);
      });
      temporisateursMessagesRef.current.clear();
    };
  }, []);

  const gererChangementElement = (idElement) => {
    setElementActif(idElement);
  };

  const gererVoirClasses = (filiere) => {
    setNavigationState({
      filiereSelectionnee: filiere,
      classeSelectionnee: null
    });
    setElementActif('classes');
  };

  const gererVoirModules = (classe) => {
    setNavigationState(prev => ({
      ...prev,
      classeSelectionnee: classe
    }));
    setElementActif('modules');
  };

  const retourFilieres = () => {
    setNavigationState({
      filiereSelectionnee: null,
      classeSelectionnee: null
    });
    setElementActif('filieres');
  };

  const retourClasses = () => {
    setNavigationState(prev => ({
      ...prev,
      classeSelectionnee: null
    }));
    setElementActif('classes');
  };

  const rendreContenuAdmin = () => {
    switch (elementActif) {
      case 'tableau-de-bord':
        return <TableauDeBordAdmin surNavigation={gererChangementElement} />;
      case 'departements':
        return <GestionDepartements />;
      case 'filieres':
        return <GestionFilieres surVoirClasses={gererVoirClasses} />;
      case 'classes':
        return (
          <GestionClasses
            filiereId={navigationState.filiereSelectionnee?.codeFiliere || navigationState.filiereSelectionnee?.id}
            filiereNom={navigationState.filiereSelectionnee?.nom}
            onRetour={retourFilieres}
            surVoirModules={gererVoirModules}
          />
        );
      case 'modules':
        return (
          <GestionModules
            classeId={navigationState.classeSelectionnee?.codeClasse || navigationState.classeSelectionnee?.id}
            classeNom={navigationState.classeSelectionnee?.nom}
            filiereCode={navigationState.classeSelectionnee?.codeFiliere || navigationState.filiereSelectionnee?.codeFiliere || navigationState.filiereSelectionnee?.id}
            filiereNom={navigationState.classeSelectionnee?.filiereNom || navigationState.filiereSelectionnee?.nom}
            onRetour={retourClasses}
          />
        );
      case 'emploi-temps':
        return <GestionEmploiTemps />;
      case 'comptes':
        return <GestionComptes />;
      case 'annonces-admin':
        return <GestionAnnoncesAdmin />;
      case 'profil':
        return <ProfilPage />;
      default:
        return <TableauDeBordAdmin />;
    }
  };

  const rendreContenuProfesseur = () => {
    switch (elementActif) {
      case 'tableau-de-bord-professeur':
        return <TableauDeBordProfesseur surNavigation={gererChangementElement} />;
      case 'cours':
        return <GestionCoursProfesseur />;
      case 'examens':
        return <GestionExamensProfesseur />;
      case 'notes':
        return <GestionNotesProfesseur />;
      case 'calendrier':
        return <CalendrierAcademiqueProfesseur />;
      case 'presence':
        return <GestionPresenceProfesseur />;
      case 'annonces':
        return <GestionAnnoncesProfesseur />;
      case 'profil':
        return <ProfilProfesseur />;
      default:
        return <TableauDeBordProfesseur surNavigation={gererChangementElement} />;
    }
  };

  const rendreContenuEtudiant = () => {
    console.log('DEBUG: rendreContenuEtudiant called with elementActif:', elementActif);
    switch (elementActif) {
      case 'tableau-de-bord-etudiant':
        console.log('DEBUG: Rendering TableauDeBordEtudiant');
        return <TableauDeBordEtudiant surNavigation={gererChangementElement} />;
      case 'profil':
        return <ProfilPage />;
      case 'cours':
        return <CoursBibliotheque />;
      case 'notes':
        return <NotesEtudiant />;
      case 'presence':
        return <PresenceEtudiant />;
      case 'calendrier':
        return <CalendrierEtudiant />;
      case 'annonces':
        return <AnnoncesEtudiant />;
      default:
        console.log('DEBUG: Rendering default TableauDeBordEtudiant');
        return <TableauDeBordEtudiant surNavigation={gererChangementElement} />;
    }
  };

  let interfaceCourante;

  if (!utilisateurConnecte) {
    interfaceCourante = <Login onLogin={gererLogin} />;
  } else if (utilisateurConnecte.role === 'professor') {
    interfaceCourante = (
      <ConteneurProfesseur
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuProfesseur()}
        <ChatBot />
      </ConteneurProfesseur>
    );
  } else if (utilisateurConnecte.role === 'admin') {
    interfaceCourante = (
      <ConteneurPrincipal
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuAdmin()}
        <ChatBot />
      </ConteneurPrincipal>
    );
  } else if (utilisateurConnecte.role === 'student') {
    console.log('DEBUG: Student role detected, rendering student container');
    console.log('DEBUG: Current elementActif:', elementActif);
    interfaceCourante = (
      <ConteneurEtudiant
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuEtudiant()}
        <ChatBot />
      </ConteneurEtudiant>
    );
  } else {
    interfaceCourante = (
      <ConteneurPrincipal
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuAdmin()}
        <ChatBot />
      </ConteneurPrincipal>
    );
  }

  return (
    <>
      <div className="app-inline-alerts" aria-live="polite" aria-atomic="true">
        {messagesGlobaux.map((message) => (
          <div
            key={message.id}
            className={`app-inline-alert app-inline-alert-${message.type}`}
            role={message.type === 'error' ? 'alert' : 'status'}
          >
            <div className="app-inline-alert-icon">
              {obtenirIconeMessage(message.type)}
            </div>
            <div className="app-inline-alert-content">
              <strong>{obtenirTitreMessage(message.type)}</strong>
              <p>{message.texte}</p>
            </div>
            <button
              type="button"
              className="app-inline-alert-close"
              onClick={() => fermerMessageGlobal(message.id)}
              aria-label="Fermer le message"
            >
              <X size={16} />
            </button>
          </div>
        ))}
      </div>

      {interfaceCourante}
    </>
  );
}

export default App;
