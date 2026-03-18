import { useState, useEffect } from 'react';
import { GraduationCap, LayoutDashboard, Users, UserCheck, BookOpen, Building2, Calendar, Megaphone } from 'lucide-react';
import IndicateurSession from '../communs/IndicateurSession';
import './BarreLaterale.css';

const BarreLaterale = ({ elementActif, surChangementElement, estOuverte = false }) => {
  const [tempsRestant, setTempsRestant] = useState(30 * 60);

  useEffect(() => {
    const intervalle = setInterval(() => {
      setTempsRestant((prev) => {
        if (prev <= 1) {
          clearInterval(intervalle);
          return 0;
        }
        return prev - 1;
      });
    }, 1000);

    return () => clearInterval(intervalle);
  }, []);

  const elementsMenu = [
    { id: 'tableau-de-bord', icone: LayoutDashboard, libelle: 'Tableau de bord' },
    { id: 'departements', icone: Building2, libelle: 'Départements' },
    { id: 'filieres', icone: GraduationCap, libelle: 'Filières' },
    { id: 'modules', icone: BookOpen, libelle: 'Modules' },
    { id: 'emploi-temps', icone: Calendar, libelle: 'Emploi du temps' },
    { id: 'comptes', icone: Users, libelle: 'Comptes' },
    { id: 'annonces-admin', icone: Megaphone, libelle: 'Annonces' },
  ];

  const formaterTemps = (secondes) => {
    const minutes = Math.floor(secondes / 60);
    const secs = secondes % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  };

  const traductionsMenu = {
    'tableau-de-bord': 'Dashboard',
    'departements': 'Departments',
    'filieres': 'Filières',
    'modules': 'Modules',
    'emploi-temps': 'Schedule',
    'comptes': 'Accounts',
    'annonces-admin': 'Announcements',
  };

  return (
    <aside className={`barre-laterale ${estOuverte ? 'open' : ''}`}>
      <nav className="barre-laterale-nav">
        <ul className="menu-liste">
          {elementsMenu.map((element) => {
            const Icone = element.icone;
            const estActif = elementActif === element.id;
            return (
              <li key={element.id}>
                <button
                  className={`menu-item ${estActif ? 'menu-item-actif' : ''}`}
                  onClick={() => surChangementElement(element.id)}
                >
                  <Icone size={20} />
                  <span>{traductionsMenu[element.id] || element.libelle}</span>
                </button>
              </li>
            );
          })}
        </ul>
      </nav>

      <div className="barre-laterale-footer">
        <IndicateurSession
          estActif={true}
          tempsRestant={tempsRestant}
          surDeconnexion={() => console.log('Déconnexion automatique')}
        />
      </div>
    </aside>
  );
};

export default BarreLaterale;

