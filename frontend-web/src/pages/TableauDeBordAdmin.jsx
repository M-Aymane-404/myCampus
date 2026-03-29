import { useEffect, useState } from 'react';
import {
  GraduationCap,
  Users,
  School,
  BookOpen,
  Link2,
  UserPlus,
  Megaphone,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  LabelList,
} from 'recharts';
import { apiTableauDeBord } from '../services/apiTableauDeBord';
import './TableauDeBordAdmin.css';

const formaterNombre = (valeur) =>
  new Intl.NumberFormat('fr-FR').format(valeur ?? 0);

const formaterDerniereMiseAJour = (date) => {
  if (!date) return '';
  return `Mis a jour a ${date.toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })}`;
};

const construireKpis = (statistiquesGlobales) => [
  {
    label: 'Etudiants',
    valeur: statistiquesGlobales?.totalEtudiants ?? 0,
    icone: GraduationCap,
    tonalite: 'blue',
  },
  {
    label: 'Professeurs',
    valeur: statistiquesGlobales?.totalProfesseurs ?? 0,
    icone: Users,
    tonalite: 'teal',
  },
  {
    label: 'Classes',
    valeur: statistiquesGlobales?.totalClasses ?? 0,
    icone: School,
    tonalite: 'amber',
  },
  {
    label: 'Matieres',
    valeur: statistiquesGlobales?.totalMatieres ?? 0,
    icone: BookOpen,
    tonalite: 'indigo',
  },
  {
    label: 'Affectations actives',
    valeur: statistiquesGlobales?.totalAffectationsActives ?? 0,
    icone: Link2,
    tonalite: 'rose',
  },
];

const construireGraphique = (etudiantsParFiliere) =>
  [...(etudiantsParFiliere ?? [])]
    .map((entree) => ({
      filiere: entree.codeFiliere || entree.filiere,
      total: entree.totalEtudiants ?? 0,
    }))
    .sort((a, b) => b.total - a.total);

const TableauDeBordAdmin = ({ surNavigation }) => {
  const [dashboard, setDashboard] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');
  const [derniereMiseAJour, setDerniereMiseAJour] = useState(null);

  useEffect(() => {
    const chargerTableauDeBord = async (showLoader = true) => {
      try {
        if (showLoader) setChargement(true);
        setErreur('');
        const donnees = await apiTableauDeBord.obtenirTableauDeBord();
        setDashboard(donnees);
        setDerniereMiseAJour(new Date());
      } catch (err) {
        console.error('Chargement dashboard admin impossible:', err);
        setErreur(err.message || 'Impossible de charger le tableau de bord.');
      } finally {
        if (showLoader) setChargement(false);
      }
    };

    chargerTableauDeBord();
    const interval = setInterval(() => {
      chargerTableauDeBord(false);
    }, 60000);

    return () => clearInterval(interval);
  }, []);

  const kpis = construireKpis(dashboard?.statistiquesGlobales);
  const etudiantsParFiliere = construireGraphique(dashboard?.etudiantsParFiliere);
  const hauteurGraphique = Math.max(320, etudiantsParFiliere.length * 58);

  return (
    <div className="admin-dashboard">
      <header className="admin-header">
        <div className="admin-header-row">
          <div className="admin-header-info">
            <p className="admin-dashboard-eyebrow">Pilotage administratif</p>
            <h1>Dashboard administrateur</h1>
            <p className="admin-dashboard-description">
              Vue centrale pour suivre les effectifs, traiter les alertes et lancer
              les operations courantes dans le meme style que l espace etudiant.
            </p>
            {derniereMiseAJour ? (
              <span className="dashboard-refresh-meta">
                {formaterDerniereMiseAJour(derniereMiseAJour)}
              </span>
            ) : null}
          </div>

          <div className="admin-header-side">
            <div className="admin-status">
              <div className="admin-status-indicator"></div>
              <span>ADMIN ACTIF</span>
            </div>

            <div className="admin-header-actions">
              <button
                type="button"
                className="admin-action-button admin-action-button-primary"
                onClick={() => surNavigation && surNavigation('annonces-admin')}
              >
                <Megaphone size={18} />
                <span>Publier une annonce</span>
              </button>
              <button
                type="button"
                className="admin-action-button"
                onClick={() => surNavigation && surNavigation('comptes')}
              >
                <UserPlus size={18} />
                <span>Ajouter un compte</span>
              </button>
            </div>
          </div>
        </div>
      </header>

      {erreur ? (
        <div className="dashboard-feedback dashboard-feedback-error">
          <p>{erreur}</p>
          <button type="button" onClick={() => window.location.reload()}>
            Recharger
          </button>
        </div>
      ) : null}

      <section className="dashboard-strip">
        {kpis.map((item) => {
          const Icone = item.icone;
          return (
            <article key={item.label} className={`kpi-card tonalite-${item.tonalite}`}>
              <div className="kpi-icon">
                <Icone size={18} />
              </div>
              <div className="kpi-content">
                <span className="kpi-label">{item.label}</span>
                <strong className="kpi-value">
                  {chargement ? '...' : formaterNombre(item.valeur)}
                </strong>
              </div>
            </article>
          );
        })}
      </section>

      <section className="dashboard-main-grid">
        <article className="dashboard-card chart-card">
          <div className="dashboard-card-header">
            <div>
              <p className="section-kicker">Vue effectifs</p>
              <h2>Etudiants par filiere</h2>
            </div>
            <span className="section-meta">
              {chargement ? 'Chargement...' : `${etudiantsParFiliere.length} filieres`}
            </span>
          </div>

          <p className="dashboard-card-description">
            Lecture rapide de la repartition actuelle pour identifier les filieres
            les plus chargees.
          </p>

          <div className="chart-wrapper" style={{ height: `${hauteurGraphique}px` }}>
            {chargement ? (
              <div className="chart-empty-state">Chargement du graphique...</div>
            ) : etudiantsParFiliere.length === 0 ? (
              <div className="chart-empty-state">Aucune donnee disponible.</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={etudiantsParFiliere}
                  layout="vertical"
                  margin={{ top: 8, right: 24, left: 24, bottom: 0 }}
                >
                  <CartesianGrid
                    strokeDasharray="3 3"
                    horizontal={false}
                    stroke="rgba(120, 137, 153, 0.22)"
                  />
                  <XAxis
                    type="number"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#5f6f7c', fontSize: 12 }}
                  />
                  <YAxis
                    dataKey="filiere"
                    type="category"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#5f6f7c', fontSize: 12 }}
                    width={96}
                  />
                  <Tooltip
                    cursor={{ fill: 'rgba(92, 84, 237, 0.08)' }}
                    contentStyle={{
                      borderRadius: 14,
                      border: '1px solid rgba(92, 84, 237, 0.14)',
                      background: 'rgba(255, 255, 255, 0.98)',
                      boxShadow: '0 16px 38px rgba(15, 23, 42, 0.10)',
                    }}
                  />
                  <Bar
                    dataKey="total"
                    fill="#5c54ed"
                    radius={[0, 10, 10, 0]}
                    barSize={22}
                  >
                    <LabelList
                      dataKey="total"
                      position="right"
                      offset={10}
                      fill="#334155"
                      fontSize={12}
                      fontWeight={700}
                    />
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>
      </section>
    </div>
  );
};

export default TableauDeBordAdmin;
