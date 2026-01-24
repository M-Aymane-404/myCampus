import { useState } from 'react';
import {
  User, Users, Layers, ShieldAlert, BarChart3
} from 'lucide-react';
import {
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer
} from 'recharts';
import './TableauDeBordAdmin.css';

const TableauDeBordAdmin = ({ surNavigation }) => {
  // Mock Data for Graph
  const data = [
    { name: 'Lun', uv: 400 },
    { name: 'Mar', uv: 600 },
    { name: 'Mer', uv: 550 },
    { name: 'Jeu', uv: 700 },
    { name: 'Ven', uv: 650 },
    { name: 'Sam', uv: 200 },
    { name: 'Dim', uv: 150 },
  ];

  // Mock Data for Logs
  const logs = [
    { id: 1, action: 'Création Filière Informatique', user: 'ADMIN CAMPUS', time: 'IL Y A 10 MIN', color: 'green' },
    { id: 2, action: 'Publication de note', user: 'DR. ROBERT MARTIN', time: 'IL Y A 25 MIN', color: 'green' },
    { id: 3, action: 'Tentative de connexion échouée', user: 'SYSTÈME', time: 'IL Y A 1H', color: 'orange' }
  ];

  return (
    <div className="admin-dashboard">
      {/* Header */}
      <div className="admin-header">
        <h1>Console d'Administration</h1>
        <p>Supervision globale de l'écosystème MyCampus.</p>
      </div>

      {/* Stats Row */}
      <div className="stats-row">
        {/* Card 1: Students */}
        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">TOTAL ÉTUDIANTS</span>
            <span className="stat-number">1,248</span>
          </div>
          <div className="stat-icon-box bg-blue">
            <User size={24} className="icon-blue" />
          </div>
        </div>

        {/* Card 2: Professors */}
        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">TOTAL PROFESSEURS</span>
            <span className="stat-number">86</span>
          </div>
          <div className="stat-icon-box bg-purple">
            <Users size={24} className="icon-purple" />
          </div>
        </div>

        {/* Card 3: Formations/Filières */}
        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">FILIÈRES ACTIVES</span>
            <span className="stat-number">14</span>
          </div>
          <div className="stat-icon-box bg-green">
            <Layers size={24} className="icon-green" />
          </div>
        </div>

        {/* Card 4: Alerts */}
        <div className="stat-card">
          <div className="stat-info">
            <span className="stat-label">ALERTES SYSTÈME</span>
            <span className="stat-number">3</span>
          </div>
          <div className="stat-icon-box bg-orange">
            <ShieldAlert size={24} className="icon-orange" />
          </div>
        </div>
      </div>

      {/* Main Content Grid */}
      <div className="dashboard-grid">
        {/* Left: Chart */}
        <div className="chart-card">
          <div className="card-header">
            <BarChart3 size={20} className="header-icon" />
            <h2>Activité des Utilisateurs (Semaine)</h2>
          </div>
          <div style={{ width: '100%', height: 300 }}>
            <ResponsiveContainer>
              <AreaChart
                data={data}
                margin={{ top: 10, right: 30, left: 0, bottom: 0 }}
              >
                <defs>
                  <linearGradient id="colorUv" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#8884d8" stopOpacity={0.1} />
                    <stop offset="95%" stopColor="#8884d8" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <CartesianGrid strokeDasharray="3 3" vertical={false} stroke="#E2E8F0" />
                <XAxis
                  dataKey="name"
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#94A3B8', fontSize: 12 }}
                  dy={10}
                />
                <YAxis
                  axisLine={false}
                  tickLine={false}
                  tick={{ fill: '#94A3B8', fontSize: 12 }}
                />
                <Tooltip />
                <Area
                  type="monotone"
                  dataKey="uv"
                  stroke="#6366F1"
                  strokeWidth={3}
                  fillOpacity={1}
                  fill="url(#colorUv)"
                />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </div>

        {/* Right: Logs */}
        <div className="logs-card">
          <div className="card-header">
            <h2>Logs Récents</h2>
          </div>
          <div className="logs-list">
            {logs.map(log => (
              <div key={log.id} className="log-item">
                <div className={`log-dot dot-${log.color}`}></div>
                <div className="log-content">
                  <p className="log-action">{log.action}</p>
                  <p className="log-meta">
                    <span className="log-user">{log.user}</span> • {log.time}
                  </p>
                </div>
              </div>
            ))}
          </div>
          <button className="btn-view-logs" onClick={() => surNavigation && surNavigation('journal-audit')}>
            VOIR TOUS LES LOGS
          </button>
        </div>
      </div>
    </div>
  );
};

export default TableauDeBordAdmin;
