import { UserPlus, FileEdit, BookOpen, AlertCircle, CheckCircle, XCircle, UserCheck, Trash2 } from 'lucide-react';
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
      'Utilisateur Supprimé': 'User Deleted',
      'User Deleted': 'User Deleted',
      'Connexion': 'Login',
      'Login': 'Login',
      'Déconnexion': 'Logout',
      'Logout': 'Logout'
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
      case 'user_deleted':
      case 'utilisateur_supprimé':
        return <Trash2 size={16} />;
      case 'login':
      case 'connexion':
        return <UserCheck size={16} />;
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

  const formaterDate = (dateStr) => {
    if (!dateStr) return 'N/A';
    const date = new Date(dateStr);
    return date.toLocaleString('fr-FR', {
      day: '2-digit',
      month: '2-digit',
      year: 'numeric',
      hour: '2-digit',
      minute: '2-digit'
    });
  };

  return (
    <div className="journal-audit">
      <div className="journal-audit-table-container">
        <table className="journal-audit-table">
          <thead>
            <tr>
              <th>User Name</th>
              <th>Action</th>
              <th>Date/Time</th>
              <th>Status</th>
            </tr>
          </thead>
          <tbody>
            {journaux.length === 0 ? (
              <tr>
                <td colSpan="4" className="journal-audit-vide">
                  Aucun log disponible
                </td>
              </tr>
            ) : (
              journaux.map((journal, index) => {
                const statut = obtenirStatut(journal.statut || journal.status);
                return (
                  <tr key={index}>
                    <td>
                      <div className="journal-audit-user">
                        {obtenirIcone(journal.type || journal.action)}
                        <span>{journal.utilisateur || journal.user || 'System'}</span>
                      </div>
                    </td>
                    <td>
                      <span className="journal-audit-action">
                        {traduireAction(journal.action || journal.type)}
                      </span>
                    </td>
                    <td>
                      <span className="journal-audit-date">
                        {formaterDate(journal.date || journal.timestamp)}
                      </span>
                    </td>
                    <td>
                      <BadgeStatut
                        statut={statut.type}
                        texte={statut.libelle}
                      />
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default JournalAudit;
