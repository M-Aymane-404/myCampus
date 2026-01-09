import './CarteStatistique.css';

const CarteStatistique = ({ titre, valeur, variation, icone: Icone, couleur = 'primaire' }) => {
  return (
    <div className={`carte-statistique carte-statistique-${couleur}`}>
      <div className="carte-statistique-header">
        <div className="carte-statistique-icone">
          {Icone && <Icone size={24} />}
        </div>
        {variation && (
          <span className={`carte-statistique-variation ${variation.positif ? 'positif' : 'negatif'}`}>
            {variation.positif ? '+' : ''}{variation.valeur} {variation.label}
          </span>
        )}
      </div>
      <div className="carte-statistique-contenu">
        <h3 className="carte-statistique-titre">{titre}</h3>
        <p className="carte-statistique-valeur">{valeur}</p>
      </div>
    </div>
  );
};

export default CarteStatistique;

