import { useState } from 'react';
import ConteneurPrincipal from './composants/layout/ConteneurPrincipal';
import TableauDeBordAdmin from './pages/TableauDeBordAdmin';
import GestionDepartements from './pages/GestionDepartements';
import GestionFilieres from './pages/GestionFilieres';
import GestionProfesseurs from './pages/GestionProfesseurs';
import GestionEtudiants from './pages/GestionEtudiants';
import GestionModules from './pages/GestionModules';
import GestionEmploiTemps from './pages/GestionEmploiTemps';
import GestionComptes from './pages/GestionComptes';
import JournalAuditPage from './pages/JournalAuditPage';
import './App.css';

function App() {
  const [elementActif, setElementActif] = useState('tableau-de-bord');

  const gererChangementElement = (idElement) => {
    setElementActif(idElement);
  };

  const rendreContenu = () => {
    switch (elementActif) {
      case 'tableau-de-bord':
        return <TableauDeBordAdmin />;
      case 'departements':
        return <GestionDepartements />;
      case 'filieres':
        return <GestionFilieres />;
      case 'professeurs':
        return <GestionProfesseurs />;
      case 'etudiants':
        return <GestionEtudiants />;
      case 'modules':
        return <GestionModules />;
      case 'emploi-temps':
        return <GestionEmploiTemps />;
      case 'comptes':
        return <GestionComptes />;
      case 'journal-audit':
        return <JournalAuditPage />;
      default:
        return <TableauDeBordAdmin />;
    }
  };

  return (
    <ConteneurPrincipal
      elementActif={elementActif}
      surChangementElement={gererChangementElement}
    >
      {rendreContenu()}
    </ConteneurPrincipal>
  );
}

export default App;
