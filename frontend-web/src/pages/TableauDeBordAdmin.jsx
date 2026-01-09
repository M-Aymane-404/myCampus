import { useState, useEffect } from 'react';
import { Users, UserCheck, BookOpen, Building2, Users as UsersActive, Clock } from 'lucide-react';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import AlerteNotification from '../composants/tableauDeBord/AlerteNotification';
import GraphiqueDepartement from '../composants/tableauDeBord/GraphiqueDepartement';
import GraphiqueDistribution from '../composants/tableauDeBord/GraphiqueDistribution';
import JournalAudit from '../composants/tableauDeBord/JournalAudit';
import ActionsRapides from '../composants/tableauDeBord/ActionsRapides';
import { donneesTestTableauDeBord } from '../donnees/donneesTest';
import { apiTableauDeBord } from '../services/apiTableauDeBord';
import './TableauDeBordAdmin.css';

const TableauDeBordAdmin = () => {
  const [donneesTableauDeBord, setDonneesTableauDeBord] = useState(null);
  const [chargement, setChargement] = useState(true);

  useEffect(() => {
    chargerDonnees();
  }, []);

  const chargerDonnees = async () => {
    try {
      setChargement(true);
      const donnees = await apiTableauDeBord.obtenirTableauDeBord();
      setDonneesTableauDeBord(donnees);
    } catch (erreur) {
      console.warn('Utilisation des données de test:', erreur);
      setDonneesTableauDeBord(donneesTestTableauDeBord);
    } finally {
      setChargement(false);
    }
  };

  const gererActionRapide = (idAction) => {
    console.log('Action rapide:', idAction);
  };

  if (chargement) {
    return (
      <div className="chargement-container">
        <div className="chargement-spinner"></div>
        <p>Chargement du tableau de bord...</p>
      </div>
    );
  }

  const statistiques = donneesTableauDeBord?.statistiquesGlobales || donneesTestTableauDeBord.statistiquesGlobales;
  const statistiquesDepartement = donneesTableauDeBord?.statistiquesParDepartement || donneesTestTableauDeBord.statistiquesParDepartement;
  const journaux = donneesTableauDeBord?.journauxAudit || donneesTestTableauDeBord.journauxAudit;

  return (
    <div className="tableau-de-bord-admin">
      <div className="tableau-de-bord-header">
        <h1 className="tableau-de-bord-titre">Administrator Dashboard</h1>
        <p className="tableau-de-bord-sous-titre">System overview and management controls</p>
      </div>

      <div className="tableau-de-bord-statistiques-ligne1">
        <CarteStatistique
          titre="Students"
          valeur={statistiques.totalEtudiants}
          variation={{ valeur: 12, label: 'this month', positif: true }}
          icone={Users}
          couleur="primary"
        />
        <CarteStatistique
          titre="Professors"
          valeur={statistiques.totalProfesseurs}
          variation={{ valeur: 3, label: 'this month', positif: true }}
          icone={UserCheck}
          couleur="primary"
        />
        <CarteStatistique
          titre="Modules"
          valeur={statistiques.totalModules}
          variation={{ valeur: statistiques.totalModules, label: 'Active courses', positif: true }}
          icone={BookOpen}
          couleur="primary"
        />
      </div>

      <div className="tableau-de-bord-statistiques-ligne2">
        <CarteStatistique
          titre="Departments"
          valeur={statistiques.totalDepartements}
          variation={{ valeur: statistiques.totalDepartements, label: 'Academic units', positif: true }}
          icone={Building2}
          couleur="primary"
        />
        <CarteStatistique
          titre="Active Now"
          valeur={statistiques.totalUtilisateursActifs}
          variation={{ valeur: statistiques.totalUtilisateursActifs, label: 'Online users', positif: true }}
          icone={UsersActive}
          couleur="success"
        />
        <CarteStatistique
          titre="Pending"
          valeur={0}
          variation={{ valeur: 12, label: 'chi haja urgent', positif: false }}
          icone={Clock}
          couleur="accent"
        />
      </div>

      <div className="tableau-de-bord-alertes">
        <AlerteNotification
          type="warning"
          message="hadik lhaja li urgent"
          boutonTexte="Review Now"
          couleurBordure="warning"
          surClicBouton={() => console.log('Review approvals')}
        />
        <AlerteNotification
          type="info"
          message="Database backup scheduled for tonight at 2:00 AM"
          boutonTexte="View Schedule"
          couleurBordure="info"
          surClicBouton={() => console.log('View schedule')}
        />
        <AlerteNotification
          type="success"
          message="All systems operational - 99.9% uptime"
          boutonTexte="View Status"
          couleurBordure="success"
          surClicBouton={() => console.log('View status')}
        />
      </div>

      <div className="tableau-de-bord-graphiques">
        <div className="graphique-grand">
          <GraphiqueDepartement donnees={statistiquesDepartement} />
        </div>
        <div className="graphique-moyen">
          <GraphiqueDistribution donnees={statistiques} />
        </div>
      </div>

      <div className="tableau-de-bord-journal-audit">
        <JournalAudit journaux={journaux} />
      </div>

      <div className="tableau-de-bord-actions-rapides">
        <ActionsRapides surAction={gererActionRapide} />
      </div>
    </div>
  );
};

export default TableauDeBordAdmin;

