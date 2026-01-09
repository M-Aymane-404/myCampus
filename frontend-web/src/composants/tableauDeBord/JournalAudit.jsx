import { UserPlus, FileEdit, BookOpen, AlertCircle, CheckCircle, XCircle } from 'lucide-react';
import BadgeStatut from '../communs/BadgeStatut';
import './JournalAudit.css';

const JournalAudit = ({ journaux }) => {
  const traduireAction = (action) => {
    const traductions = {
      'Utilisateur Créé': 'User Created',
      'User Created': 'User Created',
      'Note Mise à Jour': 'Grade Updated',
      'Grade Updated': 'Grade Updated',
      'Module Créé': 'Module Created',
      'Module Created': 'Module Created',
      'Tentative de Connexion Échouée': 'Login Attempt Failed',
      'Login Attempt Failed': 'Login Attempt Failed',
    };
    return traductions[action] || action;
  };

  const obtenirIcone = (type) => {
    switch (type?.toLowerCase()) {
      case 'user_created':
      case 'utilisateur_créé':
        return <UserPlus size={16} />;
      case 'grade_updated':
      case 'note_mise_à_jour':
        return <FileEdit size={16} />;
      case 'module_created':
      case 'module_créé':
        return <BookOpen size={16} />;
      case 'login_failed':
      case 'connexion_échouée':
        return <AlertCircle size={16} />;
      default:
        return <CheckCircle size={16} />;
    }
  };

  const obtenirStatut = (status) => {
    switch (status?.toLowerCase()) {
      case 'success':
      case 'succès':
        return { type: 'success', libelle: 'SUCCESS' };
      case 'warning':
      case 'avertissement':
        return { type: 'warning', libelle: 'WARNING' };
      case 'error':
      case 'erreur':
        return { type: 'error', libelle: 'ERROR' };
      default:
        return { type: 'info', libelle: 'INFO' };
    }
  };

  return (
    <div className="journal-audit">
      <h3 className="journal-audit-titre">Recent Audit Log</h3>
      <div className="journal-audit-contenu">
        {(!journaux || journaux.length === 0) ? (
          <p className="journal-audit-vide">No recent activity</p>
        ) : (
          <table className="tableau-audit">
            <thead>
              <tr>
                <th>ACTION</th>
                <th>USER</th>
                <th>TARGET</th>
                <th>TIMESTAMP</th>
                <th>STATUS</th>
              </tr>
            </thead>
            <tbody>
              {journaux.map((journal, index) => {
                const statut = obtenirStatut(journal.statut);
                return (
                  <tr key={index}>
                    <td>
                      <div className="cellule-action">
                        {obtenirIcone(journal.type)}
                        <span>{traduireAction(journal.action || journal.type)}</span>
                      </div>
                    </td>
                    <td>{journal.utilisateurEmail || journal.email}</td>
                    <td>{journal.cible || journal.target}</td>
                    <td>{journal.horodatage || journal.timestamp}</td>
                    <td>
                      <BadgeStatut type={statut.type} libelle={statut.libelle} />
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        )}
      </div>
    </div>
  );
};

export default JournalAudit;

