import { LogOut } from 'lucide-react';
import './IndicateurSession.css';

const IndicateurSession = ({ estActif, tempsRestant, surDeconnexion }) => {
  if (!estActif) return null;

  return (
    <div className="indicateur-session">
      <button 
        className="bouton-deconnexion"
        onClick={surDeconnexion}
        title="Manual logout"
      >
        <LogOut size={16} />
        <span>Logout</span>
      </button>
    </div>
  );
};

export default IndicateurSession;

