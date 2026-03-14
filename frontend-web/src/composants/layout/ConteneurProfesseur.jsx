import EnTeteProfesseur from './EnTeteProfesseur';
import './ConteneurPrincipal.css';

const ConteneurProfesseur = ({ children, elementActif, surChangementElement }) => {
  return (
    <div className="conteneur-principal">
      <div className="contenu-principal">
          <EnTeteProfesseur
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
