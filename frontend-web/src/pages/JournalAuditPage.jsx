import { useState } from 'react';
import { FileText, Shield, AlertCircle } from 'lucide-react';
import JournalAudit from '../composants/tableauDeBord/JournalAudit';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import { donneesTestTableauDeBord } from '../donnees/donneesTest';
import './JournalAuditPage.css';

const JournalAuditPage = () => {
  const [journaux] = useState(donneesTestTableauDeBord.journauxAudit || []);

  const totalLogs = journaux.length;
  const logsConnexion = journaux.filter(j => j.type.includes('LOGIN')).length;
  const logsModification = journaux.filter(j => j.type.includes('MODIFIE') || j.type.includes('AJOUTE')).length;

  return (
    <div className="journal-audit-page">
      <div className="journal-audit-page-header">
        <h1 className="journal-audit-page-titre">Journal d'Audit</h1>
        <p className="journal-audit-page-sous-titre">Supervision des logs d'accès et de sécurité</p>
      </div>

      <div className="journal-audit-page-statistiques">
        <CarteStatistique
          titre="Total Logs"
          valeur={totalLogs}
          variation={{ valeur: totalLogs, label: 'entrées', positif: true }}
          icone={FileText}
          couleur="primary"
        />
        <CarteStatistique
          titre="Connexions"
          valeur={logsConnexion}
          variation={{ valeur: logsConnexion, label: 'connexions', positif: true }}
          icone={Shield}
          couleur="info"
        />
        <CarteStatistique
          titre="Modifications"
          valeur={logsModification}
          variation={{ valeur: logsModification, label: 'modifications', positif: true }}
          icone={AlertCircle}
          couleur="accent"
        />
      </div>

      <div className="journal-audit-page-contenu">
        <JournalAudit journaux={journaux} />
      </div>
    </div>
  );
};

export default JournalAuditPage;

