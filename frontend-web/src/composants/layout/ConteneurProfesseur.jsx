import EnTete from './EnTete';
import './ConteneurPrincipal.css';

const ConteneurProfesseur = ({ children, elementActif, surChangementElement }) => {
  return (
    <div className="conteneur-principal">
      <div className="contenu-principal">
        <EnTete
          elementActif={elementActif}
          surChangementElement={surChangementElement}
        />
        <main className="contenu-page">
          {children}
        </main>
      </div>
    </div>
  );
};

export default ConteneurProfesseur;
