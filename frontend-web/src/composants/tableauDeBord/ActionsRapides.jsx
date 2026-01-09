import { UserPlus, UserCheck, BookOpen, Upload, Download, Building2, FileText, Settings } from 'lucide-react';
import BoutonAction from '../communs/BoutonAction';
import './ActionsRapides.css';

const ActionsRapides = ({ surAction }) => {
  const actions = [
    { id: 'ajouter-etudiant', icone: UserPlus, libelle: 'Add Student' },
    { id: 'ajouter-professeur', icone: UserCheck, libelle: 'Add Professor' },
    { id: 'creer-module', icone: BookOpen, libelle: 'Create Module' },
    { id: 'import-bulk', icone: Upload, libelle: 'Import CSV' },
    { id: 'exporter-donnees', icone: Download, libelle: 'Export Data' },
    { id: 'gerer-departements', icone: Building2, libelle: 'Manage Departments' },
    { id: 'journal-audit', icone: FileText, libelle: 'Audit Log' },
    { id: 'parametres-systeme', icone: Settings, libelle: 'System Settings' },
  ];

  return (
    <div className="actions-rapides">
      <h3 className="actions-rapides-titre">Quick Actions</h3>
      <div className="actions-rapides-grille">
        {actions.map((action) => {
          const Icone = action.icone;
          return (
            <BoutonAction
              key={action.id}
              libelle={action.libelle}
              variante="secondaire"
              taille="moyen"
              icone={Icone}
              onClick={() => surAction?.(action.id)}
            />
          );
        })}
      </div>
    </div>
  );
};

export default ActionsRapides;

