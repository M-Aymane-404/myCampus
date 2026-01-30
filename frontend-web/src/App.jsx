import { useState, useEffect } from 'react';
import { LogOut, GraduationCap } from 'lucide-react';
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
import JournalAuditPage from './pages/JournalAuditPage';
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
import { apiAuth } from './services/apiAuth';
import './App.css';

function App() {
  const [utilisateurConnecte, setUtilisateurConnecte] = useState(null);
  const [elementActif, setElementActif] = useState('tableau-de-bord');
  const [navigationState, setNavigationState] = useState({
    filiereSelectionnee: null,
    classeSelectionnee: null
  });

  const gererLogin = (donneesLogin) => {
    // MOCK MODE: Use role directly from dropdown selection (no normalization needed)
    const role = donneesLogin.role?.toLowerCase() || 'admin';

    // User data is already stored by apiAuth.mockLogin()
    const userData = {
      email: donneesLogin.email,
      role: role,
      user: donneesLogin.user
    };

    // Set the logged-in user and role
    setUtilisateurConnecte(userData);

    // Set default active element based on role - automatic redirection
    // Administrator -> Admin Dashboard
    if (role === 'admin') {
      setElementActif('tableau-de-bord');
    }
    // Professor -> Professor Dashboard
    else if (role === 'professor') {
      setElementActif('tableau-de-bord-professeur');
    }
    // Student -> Student Dashboard
    else if (role === 'student') {
      setElementActif('tableau-de-bord-etudiant');
    }
    // Default to admin dashboard if role is unknown
    else {
      setElementActif('tableau-de-bord');
    }
  };

  // Check for existing session on mount
  useEffect(() => {
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
            onRetour={retourClasses}
          />
        );
      case 'emploi-temps':
        return <GestionEmploiTemps />;
      case 'comptes':
        return <GestionComptes />;
      case 'annonces-admin':
        return <GestionAnnoncesAdmin />;
      case 'journal-audit':
        return <JournalAuditPage />;
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
      case 'notes':
        return <GestionNotesProfesseur />;
      case 'calendrier':
        return <GestionEmploiTemps />;
      case 'presence':
        return <GestionPresenceProfesseur />;
      case 'annonces':
        return <GestionAnnoncesProfesseur />;
      case 'profil':
        return <ProfilPage />;
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

  // Show login page if not logged in
  if (!utilisateurConnecte) {
    return <Login onLogin={gererLogin} />;
  }

  // Render appropriate interface based on role
  if (utilisateurConnecte.role === 'professor') {
    return (
      <ConteneurProfesseur
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuProfesseur()}
      </ConteneurProfesseur>
    );
  } else if (utilisateurConnecte.role === 'admin') {
    return (
      <ConteneurPrincipal
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuAdmin()}
      </ConteneurPrincipal>
    );
  } else if (utilisateurConnecte.role === 'student') {
    console.log('DEBUG: Student role detected, rendering student container');
    console.log('DEBUG: Current elementActif:', elementActif);
    return (
      <ConteneurEtudiant
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuEtudiant()}
      </ConteneurEtudiant>
    );
  } else {
    // Fallback for unknown roles - show admin dashboard
    return (
      <ConteneurPrincipal
        elementActif={elementActif}
        surChangementElement={gererChangementElement}
      >
        {rendreContenuAdmin()}
      </ConteneurPrincipal>
    );
  }
}

export default App;
