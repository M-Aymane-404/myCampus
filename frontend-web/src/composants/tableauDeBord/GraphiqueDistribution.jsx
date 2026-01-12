import './GraphiqueDistribution.css';

const GraphiqueDistribution = ({ donnees }) => {
  // Données de répartition par filière
  const donneesFilieres = [
    { nom: 'ILCS', pourcentage: 15, couleur: '#0B3C5D' },
    { nom: 'RT', pourcentage: 18, couleur: '#328CC1' },
    { nom: 'GCCD', pourcentage: 10, couleur: '#0B3C5D' },
    { nom: 'FBTD', pourcentage: 12, couleur: '#328CC1' },
    { nom: 'IAID', pourcentage: 15, couleur: '#4A4A4A' },
    { nom: 'WM', pourcentage: 12, couleur: '#328CC1' },
    { nom: 'GEE', pourcentage: 13, couleur: '#4A4A4A' },
    { nom: 'Autres', pourcentage: 5, couleur: '#328CC1' }
  ];

  return (
    <div className="graphique-distribution">
      <h3 className="graphique-titre">Répartition par filière</h3>
      <div className="graphique-contenu">
        <div className="graphique-filieres-liste">
          {donneesFilieres.map((filiere, index) => (
            <div key={index} className="graphique-filiere-item">
              <div className="graphique-filiere-header">
                <span className="graphique-filiere-nom">{filiere.nom}</span>
                <span className="graphique-filiere-pourcentage">{filiere.pourcentage}%</span>
              </div>
              <div className="graphique-filiere-barre-container">
                <div 
                  className="graphique-filiere-barre"
                  style={{ 
                    width: `${filiere.pourcentage}%`,
                    backgroundColor: filiere.couleur
                  }}
                ></div>
              </div>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
};

export default GraphiqueDistribution;

