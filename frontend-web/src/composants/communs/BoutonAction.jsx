import './BoutonAction.css';

const BoutonAction = ({ 
  libelle, 
  variante = 'primaire', 
  taille = 'moyen',
  icone: Icone,
  onClick,
  disabled = false,
  type = 'button'
}) => {
  return (
    <button
      type={type}
      className={`bouton-action bouton-action-${variante} bouton-action-${taille}`}
      onClick={onClick}
      disabled={disabled}
    >
      {Icone && <Icone size={16} />}
      {libelle}
    </button>
  );
};

export default BoutonAction;

