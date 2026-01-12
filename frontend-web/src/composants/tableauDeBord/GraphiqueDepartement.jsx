import { useState } from 'react';
import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer } from 'recharts';
import { ChevronDown } from 'lucide-react';
import './GraphiqueDepartement.css';

const GraphiqueDepartement = ({ donnees }) => {
  // Générer la liste des années de 2020 à maintenant
  const anneeActuelle = new Date().getFullYear();
  const annees = [];
  for (let annee = 2020; annee <= anneeActuelle; annee++) {
    annees.push(annee.toString());
  }
  annees.reverse(); // Inverser pour avoir les années les plus récentes en premier
  
  const [anneeSelectionnee, setAnneeSelectionnee] = useState(annees[0]);
  
  // Données mensuelles pour 2024
  const donneesMensuelles = [
    { mois: 'Jan', inscriptions: 120 },
    { mois: 'Fév', inscriptions: 145 },
    { mois: 'Mar', inscriptions: 180 },
    { mois: 'Avr', inscriptions: 165 },
    { mois: 'Mai', inscriptions: 200 },
    { mois: 'Juin', inscriptions: 185 },
    { mois: 'Juil', inscriptions: 170 },
    { mois: 'Août', inscriptions: 150 },
    { mois: 'Sep', inscriptions: 220 },
    { mois: 'Oct', inscriptions: 240 },
    { mois: 'Nov', inscriptions: 210 },
    { mois: 'Déc', inscriptions: 195 }
  ];

  return (
    <div className="graphique-departement">
      <div className="graphique-header">
        <div>
          <h3 className="graphique-titre">Évolution des inscriptions</h3>
          <p className="graphique-sous-titre">Tendance mensuelle {anneeSelectionnee}</p>
        </div>
        <div className="graphique-select-container">
          <select 
            className="graphique-select-annee"
            value={anneeSelectionnee}
            onChange={(e) => setAnneeSelectionnee(e.target.value)}
          >
            {annees.map(annee => (
              <option key={annee} value={annee}>{annee}</option>
            ))}
          </select>
          <ChevronDown className="graphique-select-icon" size={16} />
        </div>
      </div>
      <div className="graphique-contenu">
        <ResponsiveContainer width="100%" height={350}>
          <LineChart data={donneesMensuelles} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#F2F4F7" />
            <XAxis 
              dataKey="mois" 
              tick={{ fill: '#4A4A4A', fontSize: 12 }}
              stroke="#F2F4F7"
            />
            <YAxis 
              tick={{ fill: '#4A4A4A', fontSize: 12 }}
              stroke="#F2F4F7"
              domain={[0, 'dataMax']}
            />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: '#FFFFFF', 
                border: '1px solid #F2F4F7',
                borderRadius: '8px',
                padding: '8px 12px',
                boxShadow: '0 2px 8px rgba(0, 0, 0, 0.1)'
              }}
            />
            <Line 
              type="monotone" 
              dataKey="inscriptions" 
              stroke="#328CC1" 
              strokeWidth={3}
              dot={{ fill: '#328CC1', r: 4 }}
              activeDot={{ r: 6 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default GraphiqueDepartement;

