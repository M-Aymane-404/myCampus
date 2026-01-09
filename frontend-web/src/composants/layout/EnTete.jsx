import { Moon, Sun, Bell } from 'lucide-react';
import { useState, useEffect } from 'react';
import './EnTete.css';

const EnTete = ({ nombreNotifications = 3 }) => {
  const [modeSombre, setModeSombre] = useState(false);

  useEffect(() => {
    const modeEnregistre = localStorage.getItem('modeSombre');
    if (modeEnregistre === 'true') {
      setModeSombre(true);
      document.documentElement.classList.add('mode-sombre');
    }
  }, []);

  const basculerModeSombre = () => {
    const nouveauMode = !modeSombre;
    setModeSombre(nouveauMode);
    localStorage.setItem('modeSombre', nouveauMode.toString());
    document.documentElement.classList.toggle('mode-sombre', nouveauMode);
  };

  return (
    <header className="en-tete">
      <div className="en-tete-actions">
        <button 
          className="bouton-mode-sombre"
          onClick={basculerModeSombre}
          aria-label="Basculer le mode sombre"
        >
          {modeSombre ? <Sun size={20} /> : <Moon size={20} />}
        </button>

        <div className="notification-container">
          <button className="bouton-notification" aria-label="Notifications">
            <Bell size={20} />
            
          </button>
        </div>

        <div className="profil-admin">
          <div className="avatar-profil">
            <span className="avatar-lettre">A</span>
          </div>
          <div className="info-profil">
            <span className="nom-profil">Admin User</span>
            <span className="role-profil">Administrator</span>
          </div>
        </div>
      </div>
    </header>
  );
};

export default EnTete;

