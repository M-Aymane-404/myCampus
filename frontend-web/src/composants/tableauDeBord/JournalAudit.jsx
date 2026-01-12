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

  
};

export default JournalAudit;

