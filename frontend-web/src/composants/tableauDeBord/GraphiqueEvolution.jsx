import { LineChart, Line, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './GraphiqueEvolution.css';

const GraphiqueEvolution = ({ donnees }) => {
  const formaterDonnees = () => {
    if (!donnees || !Array.isArray(donnees)) {
      return [
        { mois: 'Sep', etudiants: 1150, professeurs: 80 },
        { mois: 'Oct', etudiants: 1180, professeurs: 82 },
        { mois: 'Nov', etudiants: 1200, professeurs: 84 },
        { mois: 'Dec', etudiants: 1230, professeurs: 85 },
        { mois: 'Jan', etudiants: 1248, professeurs: 87 },
      ];
    }
    return donnees;
  };

  const donneesFormatees = formaterDonnees();

  return (
    <div className="graphique-evolution">
      <h3 className="graphique-titre">Enrollment Trend</h3>
      <p className="graphique-sous-titre">5-month growth overview</p>
      <div className="graphique-contenu">
        <ResponsiveContainer width="100%" height={350}>
          <LineChart data={donneesFormatees} margin={{ top: 20, right: 30, left: 20, bottom: 20 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis 
              dataKey="mois" 
              tick={{ fill: '#64748b', fontSize: 12 }}
            />
            <YAxis 
              tick={{ fill: '#64748b', fontSize: 12 }}
              domain={[0, 1400]}
            />
            <Tooltip 
              contentStyle={{ 
                backgroundColor: 'white', 
                border: '1px solid #e2e8f0',
                borderRadius: '0.5rem',
                padding: '0.5rem'
              }}
            />
            <Legend 
              wrapperStyle={{ paddingTop: '1rem' }}
              iconType="line"
            />
            <Line 
              type="monotone" 
              dataKey="etudiants" 
              stroke="#1e3a8a" 
              strokeWidth={3}
              name="students"
              dot={{ fill: '#1e3a8a', r: 5 }}
              activeDot={{ r: 7 }}
            />
            <Line 
              type="monotone" 
              dataKey="professeurs" 
              stroke="#60a5fa" 
              strokeWidth={3}
              name="professors"
              dot={{ fill: '#60a5fa', r: 5 }}
              activeDot={{ r: 7 }}
            />
          </LineChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default GraphiqueEvolution;

