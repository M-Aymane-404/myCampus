import './CarteStatistique.css';

const CarteStatistique = ({ titre, valeur, variation, icone: Icone, couleur = 'primary', iconeCouleur = 'dark' }) => {
  // Formatage du badge
  const formaterBadge = () => {
    if (!variation) return '';
    if (typeof variation.valeur === 'number' && variation.label.includes('%')) {
      return `${variation.positif ? '+' : ''}${variation.valeur}%`;
    }
    if (typeof variation.valeur === 'number' && variation.label.includes('this month')) {
      return `${variation.positif ? '+' : ''}${variation.valeur}`;
    }
    return `${variation.positif ? '+' : ''}${variation.valeur}`;
  };
  
  return (
    <div className="carte-statistique">
      {/* Corner brackets */}
      <div className="carte-corner-bracket top-left"></div>
      <div className="carte-corner-bracket top-right"></div>
      <div className="carte-corner-bracket bottom-left"></div>
      <div className="carte-corner-bracket bottom-right"></div>
      
      <div className="carte-statistique-header">
        <div className={`carte-statistique-icone ${iconeCouleur}`}>
          {Icone && <Icone size={24} />}
        </div>
        {variation && (
          <span className={`carte-statistique-badge ${iconeCouleur === 'dark' ? 'light' : 'dark'}`}>
            {formaterBadge()}
          </span>
        )}
      </div>
      
      <div className="carte-statistique-contenu">
        <p className="carte-statistique-valeur">{valeur}</p>
        <h3 className="carte-statistique-titre">{titre}</h3>
      </div>
      
      {/* Progress bar */}
      <div className="carte-statistique-progress">
        <div className="carte-statistique-progress-bar"></div>
      </div>
    </div>
  );
};

export default CarteStatistique;

