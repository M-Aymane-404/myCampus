import { useState } from 'react';
import { Plus, Edit, ClipboardCheck, Calendar, Clock, FileText, Video, MoreVertical, BookOpen, GraduationCap, UserCheck } from 'lucide-react';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BoutonAction from '../composants/communs/BoutonAction';
import './TableauDeBordProfesseur.css';

const TableauDeBordProfesseur = ({ surNavigation }) => {
  // Mock data for statistics
  const [statistiques] = useState({
    totalCours: 8,
    totalEtudiants: 145,
    notesEnAttente: 12,
    seancesCetteSemaine: 15
  });

  // Mock data for courses
  const [cours] = useState([
    {
      id: 1,
      titre: 'Programmation Java',
      niveau: 'Licence 3 Informatique',
      etudiants: 48,
      ressources: 12,
      prochainCours: { date: 'Aujourd\'hui', heure: '14:00' },
      ressourcesRecentes: [
        { id: 1, nom: 'Chapitre 5 - Héritage.pdf', type: 'PDF', dateAjout: 'Il y a 2 jours' },
        { id: 2, nom: 'TP4 - Polymorphisme.mp4', type: 'VIDEO', dateAjout: 'Il y a 5 jours' }
      ],
      icone: 'code',
      statut: 'Actif'
    },
    {
      id: 2,
      titre: 'Bases de données',
      niveau: 'Master 1 Informatique',
      etudiants: 32,
      ressources: 8,
      prochainCours: { date: 'Demain', heure: '10:00' },
      ressourcesRecentes: [
        { id: 1, nom: 'SQL Avancé - Requêtes.pdf', type: 'PDF', dateAjout: 'Il y a 1 jour' }
      ],
      icone: 'database',
      statut: 'Actif'
    },
    {
      id: 3,
      titre: 'Algorithmique',
      niveau: 'Licence 2 Informatique',
      etudiants: 62,
      ressources: 15,
      prochainCours: { date: 'Mercredi', heure: '08:30' },
      ressourcesRecentes: [
        { id: 1, nom: 'TD3 - Tri et Recherche.pdf', type: 'PDF', dateAjout: 'Il y a 3 jours' }
      ],
      icone: 'algorithm',
      statut: 'Actif'
    }
  ]);

  const gererActionRapide = (action) => {
    if (surNavigation) {
      switch(action) {
        case 'publier-cours':
          surNavigation('cours');
          break;
        case 'saisir-notes':
          surNavigation('notes');
          break;
        case 'faire-appel':
          surNavigation('presence');
          break;
        case 'voir-calendrier':
          surNavigation('calendrier');
          break;
        default:
          break;
      }
    }
  };

  const gererAjouterRessource = (coursId) => {
    // Navigate to course management with resource modal open
    if (surNavigation) {
      surNavigation('cours');
    }
  };

  const gererGererCours = (coursId) => {
    // Navigate to course management
    if (surNavigation) {
      surNavigation('cours');
    }
  };

  return (
    <div className="tableau-de-bord-professeur">
      <div className="tableau-de-bord-professeur-header">
        <h1 className="tableau-de-bord-professeur-titre">Bienvenue professeur</h1>
        <p className="tableau-de-bord-professeur-sous-titre">
          Gérez vos cours et suivez la progression de vos étudiants
        </p>
      </div>

      <div className="tableau-de-bord-professeur-contenu">
        <div className="tableau-de-bord-statistiques-container">
        <CarteStatistique
          titre="My Courses"
          valeur={statistiques.totalCours}
          variation={{ valeur: 2, label: 'active', positif: true }}
          icone={BookOpen}
          couleur="primary"
          iconeCouleur="dark"
        />

        <CarteStatistique
          titre="Students"
          valeur={statistiques.totalEtudiants}
          variation={{ valeur: 5, label: 'new', positif: true }}
          icone={UserCheck}
          couleur="primary"
          iconeCouleur="light"
        />
         <CarteStatistique
          titre="Pending Grades"
          valeur={statistiques.notesEnAttente}
          variation={{ valeur: 3, label: '', positif: false }}
          icone={GraduationCap}
          couleur="accent"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="This Week"
          valeur={statistiques.seancesCetteSemaine}
          variation={{ valeur: statistiques.seancesCetteSemaine, label: 'sessions', positif: true }}
          icone={Clock}
          couleur="primary"
          iconeCouleur="light"
        />

        </div>

        

        {/* Prochains Cours Section */}
        <div className="prochains-cours-section">
          <h2 className="section-titre">Prochains cours</h2>
          <a href="#" className="voir-tout-link">Voir tout →</a>
          <div className="prochains-cours-liste">
            {cours.slice(0, 3).map((coursItem) => (
              <div 
                key={coursItem.id} 
                className={`prochain-cours-item ${coursItem.prochainCours.date === 'Aujourd\'hui' ? 'prochain-cours-aujourdhui' : ''}`}
              >
                <div className="prochain-cours-icone">
                  <Clock size={18} />
                </div>
                <div className="prochain-cours-info">
                  <h3 className="prochain-cours-titre">{coursItem.titre}</h3>
                  <p className="prochain-cours-details">{coursItem.niveau} • Salle A101</p>
                </div>
                <div className="prochain-cours-date">
                  {coursItem.prochainCours.date === 'Aujourd\'hui' ? (
                    <span>Aujourd'hui</span>
                  ) : (
                    <span>{coursItem.prochainCours.heure} {coursItem.prochainCours.date}</span>
                  )}
                </div>
              </div>
            ))}
          </div>
        </div>


        {/* Actions Rapides Section */}
        <div className="actions-rapides-section">
          <h2 className="section-titre">Actions rapides</h2>
          <div className="actions-rapides-grid">
            <button 
              className="action-rapide-card"
              onClick={() => gererActionRapide('publier-cours')}
            >
              <div className="action-rapide-icone action-rapide-icone-primary">
                <Plus size={24} />
              </div>
              <span className="action-rapide-label">Publier un cours</span>
            </button>
            <button 
              className="action-rapide-card"
              onClick={() => gererActionRapide('saisir-notes')}
            >
              <div className="action-rapide-icone action-rapide-icone-secondary">
                <Edit size={24} />
              </div>
              <span className="action-rapide-label">Saisir des notes</span>
            </button>
            <button 
              className="action-rapide-card"
              onClick={() => gererActionRapide('faire-appel')}
            >
              <div className="action-rapide-icone action-rapide-icone-primary">
                <ClipboardCheck size={24} />
              </div>
              <span className="action-rapide-label">Faire l'appel</span>
            </button>
            <button 
              className="action-rapide-card"
              onClick={() => gererActionRapide('voir-calendrier')}
            >
              <div className="action-rapide-icone action-rapide-icone-secondary">
                <Calendar size={24} />
              </div>
              <span className="action-rapide-label">Voir le calendrier</span>
            </button>
          </div>
        </div>


        
      </div>
    </div>
  );
};

export default TableauDeBordProfesseur;
