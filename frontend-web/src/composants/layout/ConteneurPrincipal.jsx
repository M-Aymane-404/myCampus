import BarreLaterale from './BarreLaterale';
import EnTete from './EnTete';
import './ConteneurPrincipal.css';

const ConteneurPrincipal = ({ children, elementActif, surChangementElement }) => {
  return (
    <div className="conteneur-principal">
      <BarreLaterale 
        elementActif={elementActif} 
        surChangementElement={surChangementElement}
      />
      <div className="contenu-principal">
        <EnTete />
        <main className="contenu-page">
          {children}
        </main>
      </div>
    </div>
  );
};

export default ConteneurPrincipal;

