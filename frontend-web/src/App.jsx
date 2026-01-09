import { useState } from 'react';
import ConteneurPrincipal from './composants/layout/ConteneurPrincipal';
import TableauDeBordAdmin from './pages/TableauDeBordAdmin';
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
      case 'etudiants':
        return <div>Page Étudiants (À implémenter)</div>;
      case 'professeurs':
        return <div>Page Professeurs (À implémenter)</div>;
      case 'modules':
        return <div>Page Modules (À implémenter)</div>;
      case 'departements':
        return <div>Page Départements (À implémenter)</div>;
      case 'journal-audit':
        return <div>Page Journal d'Audit (À implémenter)</div>;
      case 'parametres':
        return <div>Page Paramètres (À implémenter)</div>;
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
