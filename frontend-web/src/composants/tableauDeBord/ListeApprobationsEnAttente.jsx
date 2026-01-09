import { User, Circle } from 'lucide-react';
import BoutonAction from '../communs/BoutonAction';
import './ListeApprobationsEnAttente.css';

const ListeApprobationsEnAttente = ({ approbations, surApprouver, surRejeter }) => {
  const obtenirInitiales = (nom) => {
    if (!nom) return 'U';
    const parties = nom.split(' ');
    if (parties.length >= 2) {
      return `${parties[0][0]}${parties[1][0]}`.toUpperCase();
    }
    return nom.substring(0, 2).toUpperCase();
  };

  const obtenirCouleurRole = (role) => {
    switch (role?.toLowerCase()) {
      case 'étudiant':
      case 'student':
        return '#1e3a8a';
      case 'professeur':
      case 'professor':
        return '#3b82f6';
      default:
        return '#64748b';
    }
  };

  const traduireRole = (role) => {
    const traductions = {
      'Étudiant': 'Student',
      'Professeur': 'Professor',
      'Student': 'Student',
      'Professor': 'Professor',
    };
    return traductions[role] || role;
  };

  const traduireDepartement = (departement) => {
    const traductions = {
      'Informatique': 'Computer Science',
      'Ingénierie': 'Engineering',
      'Commerce': 'Business',
      'Médecine': 'Medicine',
      'Droit': 'Law',
      'Arts': 'Arts',
    };
    return traductions[departement] || departement;
  };

  return (
    <div className="liste-approbations">
      <h3 className="liste-approbations-titre">Pending Approvals</h3>
      <div className="liste-approbations-contenu">
        {(!approbations || approbations.length === 0) ? (
          <p className="liste-approbations-vide">No pending approvals</p>
        ) : (
          approbations.map((item, index) => (
            <div key={index} className="item-approbation">
              <div className="item-approbation-avatar">
                <Circle size={40} fill={obtenirCouleurRole(item.role)} color="white" />
                <span className="item-approbation-initiales">{obtenirInitiales(item.nom)}</span>
              </div>
              <div className="item-approbation-info">
                <h4 className="item-approbation-nom">{item.nom}</h4>
                <p className="item-approbation-role">{traduireRole(item.role)}</p>
                <p className="item-approbation-departement">{traduireDepartement(item.departement)}</p>
              </div>
              <div className="item-approbation-date">
                <p className="item-approbation-date-texte">{item.date}</p>
              </div>
              <div className="item-approbation-actions">
                <BoutonAction
                  libelle="Reject"
                  variante="secondaire"
                  taille="petit"
                  onClick={() => surRejeter?.(item)}
                />
                <BoutonAction
                  libelle="Approve"
                  variante="primaire"
                  taille="petit"
                  onClick={() => surApprouver?.(item)}
                />
              </div>
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default ListeApprobationsEnAttente;

