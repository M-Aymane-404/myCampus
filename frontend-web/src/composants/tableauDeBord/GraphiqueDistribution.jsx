import { PieChart, Pie, Cell, ResponsiveContainer, Tooltip, Legend } from 'recharts';
import './GraphiqueDistribution.css';

const GraphiqueDistribution = ({ donnees }) => {
  const formaterDonnees = () => {
    if (!donnees) {
      return [
        { name: 'Students', value: 1248 },
        { name: 'Professors', value: 87 },
        { name: 'Administrators', value: 5 },
      ];
    }
    return [
      { name: 'Students', value: donnees.totalEtudiants || 1248 },
      { name: 'Professors', value: donnees.totalProfesseurs || 87 },
      { name: 'Administrators', value: donnees.totalAdministrateurs || 5 },
    ];
  };

  const donneesFormatees = formaterDonnees();
  const couleurs = ['#1e3a8a', '#60a5fa', '#93c5fd'];

  return (
    <div className="graphique-distribution">
      <h3 className="graphique-titre">User Distribution</h3>
      <p className="graphique-sous-titre">Platform user breakdown</p>
      <div className="graphique-contenu">
        <ResponsiveContainer width="100%" height={350}>
          <PieChart>
            <Pie
              data={donneesFormatees}
              cx="50%"
              cy="50%"
              labelLine={false}
              label={({ name, value }) => `${name}: ${value}`}
              outerRadius={100}
              fill="#8884d8"
              dataKey="value"
            >
              {donneesFormatees.map((entry, index) => (
                <Cell key={`cell-${index}`} fill={couleurs[index % couleurs.length]} />
              ))}
            </Pie>
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
              iconType="circle"
            />
          </PieChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default GraphiqueDistribution;

