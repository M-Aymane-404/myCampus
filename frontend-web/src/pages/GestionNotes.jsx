import { useState } from 'react';
import { FileText, TrendingUp, Award } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import './GestionNotes.css';

const GestionNotes = () => {
  const [resultats] = useState([

  ]);

  const colonnes = [
    { key: 'apogee', label: 'Apogée' },
    { key: 'etudiant', label: 'Étudiant' },
    { key: 'module', label: 'Module' },
    { key: 'examen', label: 'Examen' },
    { 
      key: 'note', 
      label: 'Note',
      render: (item) => (
        <span className={`note-cellule ${item.note >= 16 ? 'note-excellente' : item.note >= 14 ? 'note-bonne' : item.note >= 10 ? 'note-moyenne' : 'note-faible'}`}>
          {item.note}/20
        </span>
      )
    },
    { key: 'mention', label: 'Mention' },
    { 
      key: 'valide', 
      label: 'Statut',
      render: (item) => (
        <BadgeStatut 
          statut={item.valide ? 'success' : 'error'}
          texte={item.valide ? 'Validé' : 'Non validé'}
        />
      )
    },
    { key: 'datePublication', label: 'Date Publication' }
  ];

  const moyenneGenerale = resultats.length > 0
    ? (resultats.reduce((sum, r) => sum + r.note, 0) / resultats.length).toFixed(2)
    : 0;
  const resultatsValides = resultats.filter(r => r.valide).length;
  const tauxReussite = resultats.length > 0
    ? ((resultats.filter(r => r.note >= 10).length / resultats.length) * 100).toFixed(1)
    : 0;

  return (
    <div className="gestion-notes">
      <div className="gestion-notes-header">
        <h1 className="gestion-notes-titre">Gestion des Notes</h1>
        <p className="gestion-notes-sous-titre">Gestion des résultats et notes des examens</p>
      </div>

      <div className="gestion-notes-statistiques">
        <CarteStatistique
          titre="Moyenne Générale"
          valeur={moyenneGenerale}
          variation={{ valeur: 2.5, label: 'points', positif: true }}
          icone={TrendingUp}
          couleur="primary"
        />
        <CarteStatistique
          titre="Résultats Validés"
          valeur={resultatsValides}
          variation={{ valeur: resultatsValides, label: 'validés', positif: true }}
          icone={FileText}
          couleur="success"
        />
        <CarteStatistique
          titre="Taux de Réussite"
          valeur={`${tauxReussite}%`}
          variation={{ valeur: parseFloat(tauxReussite), label: '%', positif: true }}
          icone={Award}
          couleur="info"
        />
      </div>

      <TableauDonnees
        titre="Liste des Résultats"
        colonnes={colonnes}
        donnees={resultats}
        recherche={true}
        actions={false}
      />
    </div>
  );
};

export default GestionNotes;

