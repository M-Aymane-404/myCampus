import './BadgeStatut.css';

const BadgeStatut = ({ type, libelle, statut, texte }) => {
  // Support des deux formats : type/libelle ou statut/texte
  const typeFinal = statut || type;
  const texteFinal = texte || libelle;
  
  return (
    <span className={`badge-statut badge-statut-${typeFinal}`}>
      {texteFinal}
    </span>
  );
};

export default BadgeStatut;

