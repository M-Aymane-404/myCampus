import { useEffect, useState } from 'react';
import IndicateurSession from '../communs/IndicateurSession';
import './BarreLaterale.css';

const BarreLateraleProfesseur = ({ elementActif, surChangementElement, estOuverte = false }) => {
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
    { id: 'tableau-de-bord-professeur', libelle: 'Tableau de bord' },
    { id: 'cours', libelle: 'Cours & Seances' },
    { id: 'examens', libelle: 'Examens' },
    { id: 'notes', libelle: 'Notes' },
    { id: 'presence', libelle: 'Presences' },
    { id: 'calendrier', libelle: 'Calendrier' },
    { id: 'annonces', libelle: 'Annonces' },
  ];

  return (
    <aside className={`barre-laterale ${estOuverte ? 'open' : ''}`}>
      <nav className="barre-laterale-nav">
        <ul className="menu-liste">
          {elementsMenu.map((element) => {
            const estActif = elementActif === element.id;

            return (
              <li key={element.id}>
                <button
                  className={`menu-item ${estActif ? 'menu-item-actif' : ''}`}
                  onClick={() => surChangementElement(element.id)}
                >
                  <span>{element.libelle}</span>
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
          surDeconnexion={() => console.log('Deconnexion automatique')}
        />
      </div>
    </aside>
  );
};

export default BarreLateraleProfesseur;
