import { useState, useEffect } from 'react';
import Login from './pages/Login';
import ConteneurPrincipal from './composants/layout/ConteneurPrincipal';
import ConteneurProfesseur from './composants/layout/ConteneurProfesseur';
import TableauDeBordAdmin from './pages/TableauDeBordAdmin';
import GestionDepartements from './pages/GestionDepartements';
import GestionFilieres from './pages/GestionFilieres';
import GestionProfesseurs from './pages/GestionProfesseurs';
import GestionEtudiants from './pages/GestionEtudiants';
import GestionModules from './pages/GestionModules';
import GestionClasses from './pages/GestionClasses';
import GestionEmploiTemps from './pages/GestionEmploiTemps';
import GestionComptes from './pages/GestionComptes';
import JournalAuditPage from './pages/JournalAuditPage';
import GestionAnnoncesAdmin from './pages/GestionAnnoncesAdmin';
import TableauDeBordProfesseur from './pages/TableauDeBordProfesseur';
import GestionCoursProfesseur from './pages/GestionCoursProfesseur';
import GestionNotesProfesseur from './pages/GestionNotesProfesseur';
import CalendrierAcademiqueProfesseur from './pages/CalendrierAcademiqueProfesseur';
import GestionPresenceProfesseur from './pages/GestionPresenceProfesseur';
import GestionAnnoncesProfesseur from './pages/GestionAnnoncesProfesseur';
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
    // Student -> Student Dashboard (placeholder)
    else if (role === 'student') {
      setElementActif('tableau-de-bord'); // Placeholder - can be updated when student interface is ready
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
          setElementActif('tableau-de-bord');
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
        return <TableauDeBordAdmin />;
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
      case 'professeurs':
        return <GestionProfesseurs />;
      case 'etudiants':
        return <GestionEtudiants />;
      case 'emploi-temps':
        return <GestionEmploiTemps />;
      case 'comptes':
        return <GestionComptes />;
      case 'annonces-admin':
        return <GestionAnnoncesAdmin />;
      case 'journal-audit':
        return <JournalAuditPage />;
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
        return <CalendrierAcademiqueProfesseur />;
      case 'presence':
        return <GestionPresenceProfesseur />;
      case 'annonces':
        return <GestionAnnoncesProfesseur />;
      default:
        return <TableauDeBordProfesseur surNavigation={gererChangementElement} />;
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
  } else {
    // Student dashboard placeholder
    return (
      <div style={{ padding: '50px', textAlign: 'center' }}>
        <h1>Student Dashboard</h1>
        <p>Student interface coming soon...</p>
      </div>
    );
  }
}

export default App;
