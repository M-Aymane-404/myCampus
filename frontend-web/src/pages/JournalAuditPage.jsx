import { useState, useEffect } from 'react';
import { FileText, Shield, AlertCircle } from 'lucide-react';
import JournalAudit from '../composants/tableauDeBord/JournalAudit';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import apiAudit from '../services/apiAudit';
import './JournalAuditPage.css';

const JournalAuditPage = () => {
  const [journaux, setJournaux] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    chargerJournaux();
  }, []);

  const chargerJournaux = async () => {
    try {
      setChargement(true);
      setErreur(null);
      const donnees = await apiAudit.obtenirTous();
      
      // Handle both array and object responses
      const logsArray = Array.isArray(donnees) ? donnees : (donnees.logs || donnees.data || []);
      
      // Map backend data to frontend format
      const journauxMapped = logsArray.map(j => ({
        type: j.type || j.actionType || j.action || 'UNKNOWN',
        action: j.action || j.actionType || j.type || 'Action inconnue',
        utilisateur: j.userName || j.user || j.utilisateur || j.userEmail || 'System',
        user: j.userName || j.user || j.utilisateur || j.userEmail || 'System',
        date: j.timestamp || j.date || j.horodatage || j.createdAt || new Date().toISOString(),
        timestamp: j.timestamp || j.date || j.horodatage || j.createdAt || new Date().toISOString(),
        statut: j.status || j.statut || 'success',
        status: j.status || j.statut || 'success'
      }));
      
      setJournaux(journauxMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des journaux:', err);
      setErreur('Erreur lors du chargement des journaux d\'audit: ' + (err.message || 'Erreur inconnue'));
    } finally {
      setChargement(false);
    }
  };

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
          variation={{ valeur: 3, label: '', positif: true }}
          icone={FileText}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Connexions"
          valeur={logsConnexion}
          variation={{ valeur: 0, label: '', positif: true }}
          icone={Shield}
          couleur="info"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Modifications"
          valeur={logsModification}
          variation={{ valeur: 0, label: '', positif: true }}
          icone={AlertCircle}
          couleur="accent"
          iconeCouleur="dark"
        />
      </div>

      <div className="journal-audit-page-contenu">
        {chargement ? (
          <div className="chargement-container">Chargement des journaux d'audit...</div>
        ) : erreur ? (
          <div className="erreur-container">
            <p>{erreur}</p>
            <button onClick={chargerJournaux} className="btn-reessayer">Réessayer</button>
          </div>
        ) : (
          <JournalAudit journaux={journaux} />
        )}
      </div>
    </div>
  );
};

export default JournalAuditPage;

