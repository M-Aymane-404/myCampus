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
        <h1 className="tableau-de-bord-titre">Welcome back to your workspace</h1>
        <p className="tableau-de-bord-sous-titre">Here is a comprehensive overview of your institution for today.</p>
      </div>

      <div className="tableau-de-bord-statistiques-container">
        <CarteStatistique
          titre="Students"
          valeur={statistiques.totalEtudiants}
          variation={{ valeur: 12, label: '%', positif: true }}
          icone={Users}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Professors"
          valeur={statistiques.totalProfesseurs}
          variation={{ valeur: 3, label: '', positif: true }}
          icone={UserCheck}
          couleur="primary"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Modules"
          valeur={statistiques.totalModules}
          variation={{ valeur: 42, label: '', positif: true }}
          icone={BookOpen}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Departments"
          valeur={statistiques.totalDepartements}
          variation={{ valeur: statistiques.totalDepartements, label: 'Academic units', positif: true }}
          icone={Building2}
          couleur="primary"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Pending"
          valeur={0}
          variation={{ valeur: 94, label: '%', positif: true }}
          icone={Clock}
          couleur="accent"
          iconeCouleur="dark"
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

