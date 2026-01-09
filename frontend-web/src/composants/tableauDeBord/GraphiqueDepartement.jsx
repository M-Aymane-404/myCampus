import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer } from 'recharts';
import './GraphiqueDepartement.css';

const GraphiqueDepartement = ({ donnees }) => {
  const formaterDonnees = () => {
    if (!donnees || !Array.isArray(donnees)) return [];
    return donnees.map(item => ({
      nom: item.departementNom || item.nom,
      etudiants: item.totalEtudiants || item.etudiants || 0,
      professeurs: item.totalProfesseurs || item.professeurs || 0,
    }));
  };

  const donneesFormatees = formaterDonnees();

  const traduireDepartements = (nom) => {
    const traductions = {
      'Informatique': 'Computer Science',
      'Ingénierie': 'Engineering',
      'Commerce': 'Business',
      'Médecine': 'Medicine',
      'Droit': 'Law',
      'Arts': 'Arts',
    };
    return traductions[nom] || nom;
  };

  const donneesAvecTraduction = donneesFormatees.map(item => ({
    ...item,
    nom: traduireDepartements(item.nom),
  }));

  return (
    <div className="graphique-departement">
      <h3 className="graphique-titre">Department Overview</h3>
      <p className="graphique-sous-titre">Students and professors by department</p>
      <div className="graphique-contenu">
        <ResponsiveContainer width="100%" height={350}>
          <BarChart data={donneesAvecTraduction} margin={{ top: 20, right: 30, left: 20, bottom: 80 }}>
            <CartesianGrid strokeDasharray="3 3" stroke="#e2e8f0" />
            <XAxis 
              dataKey="nom" 
              tick={{ fill: '#64748b', fontSize: 12 }}
              angle={-45}
              textAnchor="end"
              height={100}
            />
            <YAxis 
              tick={{ fill: '#64748b', fontSize: 12 }}
              domain={[0, 'dataMax']}
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
              iconType="circle"
            />
            <Bar 
              dataKey="etudiants" 
              fill="#1e3a8a" 
              name="students"
              radius={[8, 8, 0, 0]}
            />
            <Bar 
              dataKey="professeurs" 
              fill="#60a5fa" 
              name="professors"
              radius={[8, 8, 0, 0]}
            />
          </BarChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
};

export default GraphiqueDepartement;

