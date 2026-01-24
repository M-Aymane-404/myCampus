import { useState } from 'react';
import {
  BookOpen, Users, CheckCircle, XCircle,
  Bell, ChevronDown, ChevronRight
} from 'lucide-react';
import {
  BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import './TableauDeBordProfesseur.css';

const TableauDeBordProfesseur = ({ surNavigation }) => {
  // Mock Stats Data
  const stats = [
    { label: 'COURS ACTIFS', value: 4, icon: BookOpen, color: 'blue' },
    { label: 'ÉTUDIANTS TOTAL', value: 156, icon: Users, color: 'purple' },
    { label: 'TAUX DE PRÉSENCE', value: '92%', icon: CheckCircle, color: 'green' },
    { label: 'ALERTES NOTES', value: 12, icon: XCircle, color: 'orange' },
  ];

  // Mock Chart Data
  const dataMoyennes = [
    { name: 'INF101', moyenne: 14.5 },
    { name: 'MTH202', moyenne: 12.8 },
    { name: 'GEN303', moyenne: 15.2 },
    { name: 'PHY104', moyenne: 13.0 },
  ];

  // Mock Single Announcement
  const derniereAnnonce = {
    id: 1,
    sujet: 'Maintenance de la plateforme',
    date: 'Il y a 2 heures',
    message: 'La plateforme sera en maintenance ce soir de 23h à minuit. Veuillez sauvegarder votre travail.'
  };

  // Form State
  const [annonce, setAnnonce] = useState({
    sujet: '',
    classe: 'L3 Informatique',
    message: ''
  });

  return (
    <div className="dashboard-professeur">
      {/* Header */}
      <header className="dashboard-header">
        <h1 className="dashboard-title">Tableau de bord Professeur</h1>
        <p className="dashboard-subtitle">Gérez vos classes et communiquez avec vos étudiants.</p>
      </header>

      {/* Stats Row */}
      <section className="stats-grid">
        {stats.map((stat, index) => (
          <div key={index} className="stat-card">
            <div className="stat-info">
              <span className="stat-label">{stat.label}</span>
              <span className="stat-value">{stat.value}</span>
            </div>
            <div className={`stat-icon-wrapper color-${stat.color}`}>
              <stat.icon size={24} />
            </div>
          </div>
        ))}
      </section>

      {/* Main Content Split */}
      <section className="content-split">

        {/* Left Column: Announcement Form */}
        <div className="announcement-card">
          <div className="announcement-header">
            <Bell size={20} />
            <h2>Publier une Annonce</h2>
          </div>

          <div className="announcement-form">
            {/* Subject Input */}
            <div className="form-group">
              <label>SUJET DE L'ANNONCE</label>
              <input
                type="text"
                placeholder="Ex: Report du TP..."
                value={annonce.sujet}
                onChange={(e) => setAnnonce({ ...annonce, sujet: e.target.value })}
              />
            </div>

            {/* Class Select */}
            <div className="form-group">
              <label>CLASSE CIBLE</label>
              <div className="select-wrapper">
                <select
                  value={annonce.classe}
                  onChange={(e) => setAnnonce({ ...annonce, classe: e.target.value })}
                >
                  <option>L3 Informatique</option>
                  <option>M1 Génie Logiciel</option>
                  <option>L2 Mathématiques</option>
                </select>
                <ChevronDown size={16} className="select-icon" />
              </div>
            </div>

            {/* Message Textarea */}
            <div className="form-group">
              <label>MESSAGE</label>
              <textarea
                placeholder="Écrivez votre message ici..."
                rows={5}
                value={annonce.message}
                onChange={(e) => setAnnonce({ ...annonce, message: e.target.value })}
              ></textarea>
            </div>

            <button className="btn-publish">
              DIFFUSER MAINTENANT
            </button>
          </div>
        </div>

        {/* Right Column: Charts & Lists */}
        <div className="analytics-column">

          {/* Chart Card */}
          <div className="chart-card">
            <h3>Moyennes par Cours</h3>
            <div className="chart-wrapper">
              <ResponsiveContainer width="100%" height={300}>
                <BarChart data={dataMoyennes} barSize={40}>
                  <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                  <XAxis
                    dataKey="name"
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#64748B', fontSize: 12 }}
                    dy={10}
                  />
                  <YAxis
                    hide={false}
                    axisLine={false}
                    tickLine={false}
                    tick={{ fill: '#64748B', fontSize: 12 }}
                    domain={[0, 20]}
                  />
                  <Tooltip
                    cursor={{ fill: 'transparent' }}
                    contentStyle={{ borderRadius: '8px', border: 'none', boxShadow: '0 4px 6px -1px rgb(0 0 0 / 0.1)' }}
                  />
                  <Bar
                    dataKey="moyenne"
                    fill="#4F46E5"
                    radius={[6, 6, 0, 0]}
                  />
                </BarChart>
              </ResponsiveContainer>
            </div>
          </div>

          {/* Recent Activity */}
          <div className="recent-activity-card">
            <h3>Vos Annonces Récentes</h3>
            <div
              className="announcement-item-card"
              onClick={() => surNavigation && surNavigation('annonces')}
            >
              <div className="announcement-icon-circle">
                <Bell size={20} />
              </div>
              <div className="announcement-content-text">
                <h4 className="announcement-subject">{derniereAnnonce.sujet}</h4>
                <span className="announcement-meta">
                  {annonce.classe.toUpperCase()} • {new Date().toLocaleDateString()}
                </span>
              </div>
              <ChevronRight size={20} className="chevron-icon" />
            </div>
          </div>

        </div>

      </section>
    </div>
  );
};

export default TableauDeBordProfesseur;
