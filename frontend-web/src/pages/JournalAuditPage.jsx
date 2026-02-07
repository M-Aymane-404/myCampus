import React from 'react';
import { Download, FileText } from 'lucide-react';
import AuditLogsTable from '../composants/AuditLogsTable';
import './JournalAuditPage.css';

const JournalAuditPage = () => {
  const handleExportCSV = async () => {
    try {
      // This will be handled by the AuditLogsTable component
      // We can trigger it via a ref if needed, but for now the table has its own export button
      console.log('Export CSV - Use the export button in the table');
    } catch (error) {
      console.error('Export error:', error);
    }
  };

  return (
    <div className="logs-page-container">
      {/* Header */}
      <div className="logs-header-row">
        <div className="logs-title-block">
          <h1>Logs de Sécurité</h1>
          <p>Historique complet des actions système et accès.</p>
        </div>
      </div>

      {/* Real Audit Logs Table */}
      <AuditLogsTable />
    </div>
  );
};

export default JournalAuditPage;
