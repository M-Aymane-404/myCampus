import EnTete from './EnTete';
import './ConteneurPrincipal.css';

const ConteneurEtudiant = ({ children, elementActif, surChangementElement }) => {
  console.log('DEBUG: ConteneurEtudiant rendered with elementActif:', elementActif);
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

export default ConteneurEtudiant;
