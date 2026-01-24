import { useState } from 'react';
import { Download, FileText } from 'lucide-react';
import './JournalAuditPage.css';

const JournalAuditPage = () => {
  // Mock Data matching screenshot
  // 12:45:30 Admin Campus ...
  const logs = [
    { id: 1, time: '12:45:30', user: 'Admin Campus', action: 'Connexion réussie', ip: '192.168.1.1', status: 'SUCCESS' },
    { id: 2, time: '12:30:12', user: 'Jean Étudiant', action: 'Téléchargement Support PDF', ip: '10.0.0.52', status: 'SUCCESS' },
    { id: 3, time: '11:55:04', user: 'Inconnu', action: 'Tentative brute-force bloquée', ip: '185.122.4.10', status: 'ERROR' },
    { id: 4, time: '11:20:45', user: 'Dr. Robert Martin', action: 'Modification de note CC', ip: '192.168.1.45', status: 'WARNING' },
    { id: 5, time: '04:00:00', user: 'Système', action: 'Sauvegarde quotidienne effectuée', ip: 'Localhost', status: 'SUCCESS' }
  ];

  const handleExportCSV = () => {
    // 1. Define CSV Headers
    const headers = ['ID,HORODATAGE,UTILISATEUR,ACTION,ADRESSE IP,STATUT'];

    // 2. Map data to CSV rows
    const rows = logs.map(log =>
      `${log.id},"${log.time}","${log.user}","${log.action}","${log.ip}","${log.status}"`
    );

    // 3. Combine headers and rows
    const csvContent = [headers, ...rows].join('\n');

    // 4. Create Blob and Download Link
    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.setAttribute('href', url);
    link.setAttribute('download', 'logs_securite.csv');
    link.style.visibility = 'hidden';
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
  };

  return (
    <div className="logs-page-container">
      {/* Header */}
      <div className="logs-header-row">
        <div className="logs-title-block">
          <h1>Logs de Sécurité</h1>
          <p>Historique complet des actions système et accès.</p>
        </div>
        <button className="btn-export-csv" onClick={handleExportCSV}>
          <Download size={16} />
          EXPORTER CSV
        </button>
      </div>

      {/* Table Card */}
      <div className="logs-table-card">
        <table className="logs-table">
          <thead>
            <tr>
              <th>HORODATAGE</th>
              <th>UTILISATEUR</th>
              <th>ACTION</th>
              <th>ADRESSE IP</th>
              <th>STATUT</th>
            </tr>
          </thead>
          <tbody>
            {logs.map(log => (
              <tr key={log.id}>
                <td className="td-time">{log.time}</td>
                <td className="td-user">{log.user}</td>
                <td className="td-action">{log.action}</td>
                <td className="td-ip">{log.ip}</td>
                <td className="td-status">
                  <span className={`status-badge badge-${log.status.toLowerCase()}`}>
                    {log.status}
                  </span>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default JournalAuditPage;
