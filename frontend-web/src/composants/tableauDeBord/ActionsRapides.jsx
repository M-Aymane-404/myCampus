import { UserPlus, UserCheck, BookOpen, Upload, Download, Building2, FileText, Settings } from 'lucide-react';
import './ActionsRapides.css';

const ActionsRapides = ({ surAction }) => {
  const actions = [
    { id: 'ajouter-etudiant', icone: UserPlus, libelle: 'Add Student' },
    { id: 'ajouter-professeur', icone: UserCheck, libelle: 'Add Professor' },
    { id: 'creer-module', icone: BookOpen, libelle: 'Database backup' },
    { id: 'gerer-departements', icone: Building2, libelle: 'Announcement' },
    
  ];

  return (
    <div className="actions-rapides">
      <h3 className="actions-rapides-titre">Quick Actions</h3>
      <div className="actions-rapides-grille">
        {actions.map((action) => {
          const Icone = action.icone;
          return (
            <button
              key={action.id}
              className="quick-action-btn"
              onClick={() => surAction?.(action.id)}
            >
              <Icone />
              <span>{action.libelle}</span>
            </button>
          );
        })}
      </div>
    </div>
  );
};

export default ActionsRapides;

