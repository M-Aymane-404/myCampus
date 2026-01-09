import './BadgeStatut.css';

const BadgeStatut = ({ type, libelle }) => {
  return (
    <span className={`badge-statut badge-statut-${type}`}>
      {libelle}
    </span>
  );
};

export default BadgeStatut;

