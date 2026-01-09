import BoutonAction from '../communs/BoutonAction';
import './AlerteNotification.css';

const AlerteNotification = ({ type, message, boutonTexte, surClicBouton, couleurBordure = 'info' }) => {
  return (
    <div className={`alerte-notification alerte-notification-${couleurBordure}`}>
      <div className="alerte-notification-bordure"></div>
      <div className="alerte-notification-contenu">
        <p className="alerte-notification-message">{message}</p>
        {boutonTexte && (
          <BoutonAction
            libelle={boutonTexte}
            variante={couleurBordure === 'warning' ? 'primaire' : 'secondaire'}
            taille="petit"
            onClick={surClicBouton}
          />
        )}
      </div>
    </div>
  );
};

export default AlerteNotification;

