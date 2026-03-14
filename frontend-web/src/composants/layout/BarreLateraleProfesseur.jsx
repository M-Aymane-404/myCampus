// import { useState, useEffect } from 'react';
// import { LayoutDashboard, BookOpen, GraduationCap, Calendar, UserCheck, Megaphone } from 'lucide-react';
// import IndicateurSession from '../communs/IndicateurSession';
// import './BarreLaterale.css';
//
// const BarreLateraleProfesseur = ({ elementActif, surChangementElement, estOuverte = false }) => {
//   const [tempsRestant, setTempsRestant] = useState(30 * 60);
//
//   useEffect(() => {
//     const intervalle = setInterval(() => {
//       setTempsRestant((prev) => {
//         if (prev <= 1) {
//           clearInterval(intervalle);
//           return 0;
//         }
//         return prev - 1;
//       });
//     }, 1000);
//
//     return () => clearInterval(intervalle);
//   }, []);
//
//   const elementsMenu = [
//     { id: 'tableau-de-bord-professeur', icone: LayoutDashboard, libelle: 'Dashboard' },
//     { id: 'cours', icone: BookOpen, libelle: 'Courses' },
//     { id: 'notes', icone: GraduationCap, libelle: 'Grades' },
//     { id: 'calendrier', icone: Calendar, libelle: 'Calendar' },
//     { id: 'presence', icone: UserCheck, libelle: 'Attendance' },
//     { id: 'annonces', icone: Megaphone, libelle: 'Announcements' },
//   ];
//
//   const traductionsMenu = {
//     'tableau-de-bord-professeur': 'Dashboard',
//     'cours': 'Courses',
//     'notes': 'Grades',
//     'calendrier': 'Calendar',
//     'presence': 'Attendance',
//     'annonces': 'Announcements',
//   };
//
//   return (
//     <aside className={`barre-laterale ${estOuverte ? 'open' : ''}`}>
//       <nav className="barre-laterale-nav">
//         <ul className="menu-liste">
//           {elementsMenu.map((element) => {
//             const Icone = element.icone;
//             const estActif = elementActif === element.id;
//             return (
//               <li key={element.id}>
//                 <button
//                   className={`menu-item ${estActif ? 'menu-item-actif' : ''}`}
//                   onClick={() => surChangementElement(element.id)}
//                 >
//                   <Icone size={20} />
//                   <span>{traductionsMenu[element.id] || element.libelle}</span>
//                 </button>
//               </li>
//             );
//           })}
//         </ul>
//       </nav>
//
//       <div className="barre-laterale-footer">
//         <IndicateurSession
//           estActif={true}
//           tempsRestant={tempsRestant}
//           surDeconnexion={() => console.log('Déconnexion automatique')}
//         />
//       </div>
//     </aside>
//   );
// };
//
// export default BarreLateraleProfesseur;

import { useState, useEffect } from 'react';
import { LayoutDashboard, BookOpen, GraduationCap, Calendar, UserCheck, Megaphone, ClipboardList } from 'lucide-react';
import IndicateurSession from '../communs/IndicateurSession';
import './BarreLaterale.css';

const BarreLateraleProfesseur = ({ elementActif, surChangementElement, estOuverte = false }) => {
  const [tempsRestant, setTempsRestant] = useState(30 * 60);

  useEffect(() => {
    const intervalle = setInterval(() => {
      setTempsRestant((prev) => {
        if (prev <= 1) { clearInterval(intervalle); return 0; }
        return prev - 1;
      });
    }, 1000);
    return () => clearInterval(intervalle);
  }, []);

  const elementsMenu = [
    { id: 'tableau-de-bord-professeur', icone: LayoutDashboard, libelle: 'Tableau de bord' },
    { id: 'cours',                      icone: BookOpen,         libelle: 'Cours & Séances'  },
    { id: 'examens',                    icone: ClipboardList,    libelle: 'Examens'           },
    { id: 'notes',                      icone: GraduationCap,    libelle: 'Notes'             },
    { id: 'presence',                   icone: UserCheck,        libelle: 'Présences'         },
    { id: 'calendrier',                 icone: Calendar,         libelle: 'Calendrier'        },
    { id: 'annonces',                   icone: Megaphone,        libelle: 'Annonces'          },
  ];

  return (
      <aside className={`barre-laterale ${estOuverte ? 'open' : ''}`}>
        <nav className="barre-laterale-nav">
          <ul className="menu-liste">
            {elementsMenu.map((element) => {
              const Icone   = element.icone;
              const estActif = elementActif === element.id;
              return (
                  <li key={element.id}>
                    <button
                        className={`menu-item ${estActif ? 'menu-item-actif' : ''}`}
                        onClick={() => surChangementElement(element.id)}
                    >
                      <Icone size={20} />
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
              surDeconnexion={() => console.log('Déconnexion automatique')}
          />
        </div>
      </aside>
  );
};

export default BarreLateraleProfesseur;