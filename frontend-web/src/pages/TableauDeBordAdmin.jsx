import { useEffect, useState } from 'react';
import {
  GraduationCap,
  Users,
  School,
  BookOpen,
  Link2,
  ArrowRight,
  UserPlus,
  Megaphone,
  PlusSquare,
} from 'lucide-react';
import {
  ResponsiveContainer,
  BarChart,
  Bar,
  XAxis,
  YAxis,
  CartesianGrid,
  Tooltip,
  Cell,
} from 'recharts';
import { apiTableauDeBord } from '../services/apiTableauDeBord';
import './TableauDeBordAdmin.css';

const actionsRapides = [
  {
    id: 'comptes',
    titre: 'Ajouter etudiant',
    description: 'Creer un compte et lier directement la classe.',
    icone: UserPlus,
  },
  {
    id: 'comptes',
    titre: 'Ajouter professeur',
    description: 'Ouvrir un compte enseignant avec ses acces.',
    icone: Users,
  },
  {
    id: 'filieres',
    titre: 'Creer classe',
    description: 'Ajouter une nouvelle classe a la structure academique.',
    icone: PlusSquare,
  },
  {
    id: 'annonces-admin',
    titre: 'Publier annonce',
    description: 'Envoyer une information ciblee aux classes concernees.',
    icone: Megaphone,
  },
];

const couleursGraphes = [
  '#1e3a5f',
  '#3d7ea6',
  '#5c54ed',
  '#60a5fa',
  '#93c5fd',
  '#f4a261',
  '#64748b',
];

const formaterNombre = (valeur) =>
  new Intl.NumberFormat('fr-FR').format(valeur ?? 0);

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
  (etudiantsParFiliere ?? []).map((entree, index) => ({
    filiere: entree.filiere,
    total: entree.totalEtudiants ?? 0,
    couleur: couleursGraphes[index % couleursGraphes.length],
  }));

const TableauDeBordAdmin = ({ surNavigation }) => {
  const [dashboard, setDashboard] = useState(null);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState('');

  useEffect(() => {
    const chargerTableauDeBord = async () => {
      try {
        setChargement(true);
        setErreur('');
        const donnees = await apiTableauDeBord.obtenirTableauDeBord();
        setDashboard(donnees);
      } catch (err) {
        console.error('Chargement dashboard admin impossible:', err);
        setErreur(err.message || 'Impossible de charger le tableau de bord.');
      } finally {
        setChargement(false);
      }
    };

    chargerTableauDeBord();
  }, []);

  const kpis = construireKpis(dashboard?.statistiquesGlobales);
  const etudiantsParFiliere = construireGraphique(dashboard?.etudiantsParFiliere);

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

          <div className="chart-wrapper">
            {chargement ? (
              <div className="chart-empty-state">Chargement du graphique...</div>
            ) : etudiantsParFiliere.length === 0 ? (
              <div className="chart-empty-state">Aucune donnee disponible.</div>
            ) : (
              <ResponsiveContainer width="100%" height="100%">
                <BarChart
                  data={etudiantsParFiliere}
                  margin={{ top: 8, right: 6, left: -22, bottom: 0 }}
                >
                  <CartesianGrid
                    strokeDasharray="3 3"
                    vertical={false}
                    stroke="rgba(120, 137, 153, 0.22)"
                  />
                  <XAxis
                    dataKey="filiere"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#5f6f7c', fontSize: 12 }}
                  />
                  <YAxis
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#5f6f7c', fontSize: 12 }}
                  />
                  <Tooltip
                    cursor={{ fill: 'rgba(61, 126, 166, 0.08)' }}
                    contentStyle={{
                      borderRadius: 14,
                      border: '1px solid rgba(61, 126, 166, 0.12)',
                      boxShadow: '0 16px 38px rgba(15, 23, 42, 0.10)',
                    }}
                  />
                  <Bar dataKey="total" radius={[12, 12, 0, 0]}>
                    {etudiantsParFiliere.map((entree) => (
                      <Cell key={entree.filiere} fill={entree.couleur} />
                    ))}
                  </Bar>
                </BarChart>
              </ResponsiveContainer>
            )}
          </div>
        </article>

        <div className="dashboard-side-column">
          <article className="dashboard-card actions-card">
            <div className="dashboard-card-header">
              <div>
                <p className="section-kicker">Raccourcis</p>
                <h2>Actions rapides</h2>
              </div>
            </div>

            <div className="actions-grid">
              {actionsRapides.map((action) => {
                const Icone = action.icone;
                return (
                  <button
                    key={`${action.id}-${action.titre}`}
                    type="button"
                    className="action-card"
                    onClick={() => surNavigation && surNavigation(action.id)}
                  >
                    <div className="action-card-top">
                      <div className="action-icon">
                        <Icone size={18} />
                      </div>
                      <ArrowRight size={16} className="action-arrow" />
                    </div>
                    <strong>{action.titre}</strong>
                    <p>{action.description}</p>
                  </button>
                );
              })}
            </div>
          </article>
        </div>
      </section>
    </div>
  );
};

export default TableauDeBordAdmin;
