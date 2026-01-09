import { useState } from 'react';
import { Calendar, Clock, MapPin, Download, Upload } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import BoutonAction from '../composants/communs/BoutonAction';
import './GestionEmploiTemps.css';

const GestionEmploiTemps = () => {
  const [seances, setSeances] = useState([
    {
      id: 1,
      module: 'GI-INFO-101',
      matiere: 'Algorithmique',
      typeSeance: 'COURS',
      date: '2024-02-15',
      heureDebut: '08:00',
      heureFin: '10:00',
      salle: 'A101',
      professeur: 'Ahmed Alami',
      filiere: 'GI-2024',
      classe: 'L3-A',
      etat: 'Programmée'
    },
    {
      id: 2,
      module: 'GI-INFO-101',
      matiere: 'Algorithmique',
      typeSeance: 'TD',
      date: '2024-02-16',
      heureDebut: '10:00',
      heureFin: '12:00',
      salle: 'B205',
      professeur: 'Ahmed Alami',
      filiere: 'GI-2024',
      classe: 'L3-B',
      etat: 'Programmée'
    },
    {
      id: 3,
      module: 'GI-INFO-102',
      matiere: 'POO',
      typeSeance: 'TP',
      date: '2024-02-17',
      heureDebut: '14:00',
      heureFin: '17:00',
      salle: 'Lab-Info-1',
      professeur: 'Fatima Bennani',
      filiere: 'GI-2024',
      classe: 'L3-A',
      etat: 'Terminée'
    }
  ]);

  const [filtreFiliere, setFiltreFiliere] = useState('');
  const [filtreClasse, setFiltreClasse] = useState('');

  const filieres = [
    { value: '', label: 'Toutes les filières' },
    { value: 'GI-2024', label: 'Génie Informatique' },
    { value: 'GIL-2024', label: 'Génie Industriel et Logistique' },
    { value: 'GC-2024', label: 'Génie Civil' }
  ];

  const classes = [
    { value: '', label: 'Toutes les classes' },
    { value: 'L3-A', label: 'L3-A' },
    { value: 'L3-B', label: 'L3-B' },
    { value: 'M1-A', label: 'M1-A' },
    { value: 'M1-B', label: 'M1-B' }
  ];

  const seancesFiltrees = seances.filter(seance => {
    if (filtreFiliere && seance.filiere !== filtreFiliere) return false;
    if (filtreClasse && seance.classe !== filtreClasse) return false;
    return true;
  });

  const colonnes = [
    { key: 'date', label: 'Date' },
    { key: 'heureDebut', label: 'Heure Début' },
    { key: 'heureFin', label: 'Heure Fin' },
    { key: 'module', label: 'Module' },
    { key: 'matiere', label: 'Matière' },
    { key: 'filiere', label: 'Filière' },
    { key: 'classe', label: 'Classe' },
    { 
      key: 'typeSeance', 
      label: 'Type',
      render: (item) => {
        const typeMap = {
          'COURS': { statut: 'info', texte: 'Cours' },
          'TD': { statut: 'success', texte: 'TD' },
          'TP': { statut: 'warning', texte: 'TP' }
        };
        const type = typeMap[item.typeSeance] || { statut: 'error', texte: item.typeSeance };
        return <BadgeStatut statut={type.statut} texte={type.texte} />;
      }
    },
    { 
      key: 'salle', 
      label: 'Salle',
      render: (item) => (
        <div className="cellule-salle">
          <MapPin size={14} />
          <span>{item.salle}</span>
        </div>
      )
    },
    { key: 'professeur', label: 'Professeur' },
    { 
      key: 'etat', 
      label: 'État',
      render: (item) => (
        <BadgeStatut 
          statut={item.etat === 'Terminée' ? 'success' : 'info'}
          texte={item.etat}
        />
      )
    }
  ];

  const seancesProgrammees = seancesFiltrees.filter(s => s.etat === 'Programmée').length;
  const seancesTerminees = seancesFiltrees.filter(s => s.etat === 'Terminée').length;
  const totalHeures = seancesFiltrees.reduce((sum, s) => {
    const [debutH, debutM] = s.heureDebut.split(':').map(Number);
    const [finH, finM] = s.heureFin.split(':').map(Number);
    const duree = (finH * 60 + finM) - (debutH * 60 + debutM);
    return sum + (duree / 60);
  }, 0);

  const parserCSV = (texte) => {
    const lignes = texte.split('\n').filter(ligne => ligne.trim());
    if (lignes.length === 0) return [];

    const premiereLigne = lignes[0];
    const separateur = premiereLigne.includes(';') ? ';' : ',';

    const resultats = [];
    for (let i = 0; i < lignes.length; i++) {
      const colonnes = lignes[i].split(separateur).map(col => col.trim().replace(/^"|"$/g, ''));
      
      if (i === 0 && (
        colonnes[0].toLowerCase().includes('date') || 
        colonnes[0].toLowerCase().includes('module') ||
        colonnes[0].toLowerCase().includes('matiere')
      )) {
        continue;
      }

      if (colonnes.length >= 6) {
        resultats.push({
          module: colonnes[0] || '',
          matiere: colonnes[1] || '',
          typeSeance: colonnes[2] || 'COURS',
          date: colonnes[3] || '',
          heureDebut: colonnes[4] || '',
          heureFin: colonnes[5] || '',
          salle: colonnes[6] || '',
          professeur: colonnes[7] || '',
          filiere: colonnes[8] || '',
          classe: colonnes[9] || '',
          etat: colonnes[10] || 'Programmée'
        });
      }
    }
    return resultats;
  };

  const importerCSV = (fichier) => {
    const lecteur = new FileReader();
    
    lecteur.onload = (e) => {
      try {
        const texte = e.target.result;
        const seancesImportees = parserCSV(texte);
        
        if (seancesImportees.length === 0) {
          alert('Aucune séance trouvée dans le fichier CSV. Vérifiez le format du fichier.');
          return;
        }

        const seancesValides = [];
        const erreurs = [];

        seancesImportees.forEach((seance, index) => {
          if (!seance.module || !seance.matiere || !seance.date || !seance.filiere || !seance.classe) {
            erreurs.push(`Ligne ${index + 1}: Champs obligatoires manquants (Module, Matière, Date, Filière, Classe)`);
            return;
          }

          seancesValides.push({
            id: seances.length + seancesValides.length + 1,
            ...seance
          });
        });

        if (erreurs.length > 0) {
          alert(`Erreurs détectées:\n${erreurs.slice(0, 5).join('\n')}${erreurs.length > 5 ? `\n... et ${erreurs.length - 5} autres erreurs` : ''}`);
        }

        if (seancesValides.length > 0) {
          setSeances(prev => [...prev, ...seancesValides]);
          alert(`${seancesValides.length} séance(s) importée(s) avec succès${erreurs.length > 0 ? `, ${erreurs.length} erreur(s) détectée(s)` : ''}`);
        } else {
          alert('Aucune séance valide à importer.');
        }
      } catch (erreur) {
        console.error('Erreur lors de l\'import CSV:', erreur);
        alert('Erreur lors de la lecture du fichier CSV. Vérifiez le format du fichier.');
      }
    };

    lecteur.onerror = () => {
      alert('Erreur lors de la lecture du fichier.');
    };

    lecteur.readAsText(fichier, 'UTF-8');
  };

  const exporterCSV = () => {
    const seancesAExporter = filtreFiliere || filtreClasse ? seancesFiltrees : seances;
    
    if (seancesAExporter.length === 0) {
      alert('Aucune séance à exporter.');
      return;
    }

    const enTetes = ['Module', 'Matière', 'Type', 'Date', 'Heure Début', 'Heure Fin', 'Salle', 'Professeur', 'Filière', 'Classe', 'État'];
    const lignes = seancesAExporter.map(seance => [
      seance.module,
      seance.matiere,
      seance.typeSeance,
      seance.date,
      seance.heureDebut,
      seance.heureFin,
      seance.salle,
      seance.professeur,
      seance.filiere,
      seance.classe,
      seance.etat
    ]);

    const csvContent = [
      enTetes.join(','),
      ...lignes.map(ligne => ligne.map(cell => `"${cell}"`).join(','))
    ].join('\n');

    const blob = new Blob([csvContent], { type: 'text/csv;charset=utf-8;' });
    const lien = document.createElement('a');
    const url = URL.createObjectURL(blob);
    
    lien.setAttribute('href', url);
    lien.setAttribute('download', `emploi_temps_${filtreFiliere || 'tous'}_${new Date().toISOString().split('T')[0]}.csv`);
    lien.style.visibility = 'hidden';
    document.body.appendChild(lien);
    lien.click();
    document.body.removeChild(lien);
  };

  return (
    <div className="gestion-emploi-temps">
      <div className="gestion-emploi-temps-header">
        <h1 className="gestion-emploi-temps-titre">Gestion des Emplois du Temps</h1>
        <p className="gestion-emploi-temps-sous-titre">Gestion des séances et planning</p>
      </div>

      <div className="gestion-emploi-temps-statistiques">
        <CarteStatistique
          titre="Séances Programmées"
          valeur={seancesProgrammees}
          variation={{ valeur: seancesProgrammees, label: 'à venir', positif: true }}
          icone={Calendar}
          couleur="primary"
        />
        <CarteStatistique
          titre="Séances Terminées"
          valeur={seancesTerminees}
          variation={{ valeur: seancesTerminees, label: 'terminées', positif: true }}
          icone={Clock}
          couleur="success"
        />
        <CarteStatistique
          titre="Total Heures"
          valeur={totalHeures.toFixed(1)}
          variation={{ valeur: totalHeures, label: 'heures', positif: true }}
          icone={Clock}
          couleur="info"
        />
      </div>

      <div className="gestion-emploi-temps-filtres">
        <div className="filtres-container">
          <div className="filtre-item">
            <label htmlFor="filtre-filiere" className="filtre-label">Filière:</label>
            <select
              id="filtre-filiere"
              className="filtre-select"
              value={filtreFiliere}
              onChange={(e) => setFiltreFiliere(e.target.value)}
            >
              {filieres.map(f => (
                <option key={f.value} value={f.value}>{f.label}</option>
              ))}
            </select>
          </div>
          <div className="filtre-item">
            <label htmlFor="filtre-classe" className="filtre-label">Classe:</label>
            <select
              id="filtre-classe"
              className="filtre-select"
              value={filtreClasse}
              onChange={(e) => setFiltreClasse(e.target.value)}
            >
              {classes.map(c => (
                <option key={c.value} value={c.value}>{c.label}</option>
              ))}
            </select>
          </div>
          <BoutonAction
            libelle="Exporter CSV"
            variante="primaire"
            taille="moyen"
            icone={Download}
            onClick={exporterCSV}
          />
        </div>
      </div>

      <TableauDonnees
        titre="Liste des Séances"
        colonnes={colonnes}
        donnees={seancesFiltrees}
        surImporterCSV={importerCSV}
        recherche={true}
        actions={false}
      />
    </div>
  );
};

export default GestionEmploiTemps;

