import { useState, useEffect } from 'react';
import { GraduationCap, LayoutDashboard, Users, UserCheck, BookOpen, Building2, FileText, Settings } from 'lucide-react';
import IndicateurSession from '../communs/IndicateurSession';
import './BarreLaterale.css';

const BarreLaterale = ({ elementActif, surChangementElement }) => {
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
    { id: 'etudiants', icone: Users, libelle: 'Étudiants' },
    { id: 'professeurs', icone: UserCheck, libelle: 'Professeurs' },
    { id: 'modules', icone: BookOpen, libelle: 'Modules' },
    { id: 'departements', icone: Building2, libelle: 'Départements' },
    { id: 'journal-audit', icone: FileText, libelle: 'Journal d\'audit' },
    { id: 'parametres', icone: Settings, libelle: 'Paramètres' },
  ];

  const formaterTemps = (secondes) => {
    const minutes = Math.floor(secondes / 60);
    const secs = secondes % 60;
    return `${minutes}:${secs.toString().padStart(2, '0')}`;
  };

  const traductionsMenu = {
    'tableau-de-bord': 'Dashboard',
    'etudiants': 'Students',
    'professeurs': 'Professors',
    'modules': 'Modules',
    'departements': 'Departments',
    'journal-audit': 'Audit Logs',
    'parametres': 'Settings',
  };

  return (
    <aside className="barre-laterale">
      <div className="barre-laterale-header">
        <div className="logo-container">
          <div className="logo-icone-container">
            <GraduationCap className="logo-icone" size={24} />
          </div>
          <div className="logo-texte">
            <h1 className="logo-titre">MyCampus</h1>
            <p className="logo-sous-titre">Academic Portal</p>
          </div>
        </div>
      </div>

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

