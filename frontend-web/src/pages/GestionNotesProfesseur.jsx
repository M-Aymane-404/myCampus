import { useState } from 'react';
import { GraduationCap, Plus, CheckCircle, Clock, Info } from 'lucide-react';
import BoutonAction from '../composants/communs/BoutonAction';
import './GestionNotesProfesseur.css';

const GestionNotesProfesseur = () => {
  const [classeActive, setClasseActive] = useState('L1-Info');
  const [evaluationActive, setEvaluationActive] = useState(null);

  const classes = [
    { id: 'toutes', label: 'Toutes les classes', badge: null },
    { id: 'L1-Info', label: 'Licence 1', badge: 'L1' },
    { id: 'L2-Info', label: 'Licence 2', badge: 'L2' },
    { id: 'L3-Info', label: 'Licence 3', badge: 'L3' },
    { id: 'M1-Info', label: 'Master 1', badge: 'M1' },
    { id: 'M2-Info', label: 'Master 2', badge: 'M2' }
  ];

  const evaluations = {
    'L1-Info': [
      {
        id: 1,
        titre: 'DS 1 - Introduction à l\'informatique',
        coefficient: 1,
        noteSur: 20,
        notesSaisies: 8,
        totalEtudiants: 8,
        moyenne: 13.5,
        statut: 'complete'
      },
      {
        id: 2,
        titre: 'TP 1 - Premiers programmes',
        coefficient: 0.5,
        noteSur: 20,
        notesSaisies: 6,
        totalEtudiants: 8,
        moyennePartielle: 14.8,
        statut: 'partial'
      }
    ],
    'L2-Info': [
      {
        id: 3,
        titre: 'DS 1 - Algorithmique',
        coefficient: 1,
        noteSur: 20,
        notesSaisies: 10,
        totalEtudiants: 10,
        moyenne: 15.2,
        statut: 'complete'
      }
    ],
    'L3-Info': [],
    'M1-Info': [],
    'M2-Info': []
  };

  const etudiantsEvaluation = [
    {
      id: 1,
      nom: 'Bennani',
      prenom: 'Ahmed',
      initiales: 'AB',
      note: 15,
      bonus: 0,
      noteFinale: 15.0
    },
    {
      id: 2,
      nom: 'El Amrani',
      prenom: 'Sara',
      initiales: 'SEA',
      note: 17,
      bonus: 1,
      noteFinale: 18.0
    },
    {
      id: 3,
      nom: 'Karim',
      prenom: 'Youssef',
      initiales: 'YK',
      note: 12,
      bonus: 0,
      noteFinale: 12.0
    },
    {
      id: 4,
      nom: 'Zahra',
      prenom: 'Fatima',
      initiales: 'FZ',
      note: 16,
      bonus: 2,
      noteFinale: 18.0
    }
  ];

  const [notesEtudiants, setNotesEtudiants] = useState(etudiantsEvaluation);
  const [modifications, setModifications] = useState({});

  const evaluationsClasse = evaluations[classeActive] || [];
  const classeInfo = classes.find(c => c.id === classeActive);
  const totalEtudiants = classeActive === 'L1-Info' ? 8 : 10;
  const totalEvaluations = evaluationsClasse.length;

  const ouvrirEvaluation = (evaluation) => {
    setEvaluationActive(evaluation);
    // Reset student grades for this evaluation
    setNotesEtudiants(etudiantsEvaluation);
    setModifications({});
  };

  const fermerEvaluation = () => {
    setEvaluationActive(null);
  };

  const gererChangementNote = (id, champ, valeur) => {
    const noteNum = parseFloat(valeur);
    
    if (champ === 'note') {
      if (valeur === '' || (!isNaN(noteNum) && noteNum >= 0 && noteNum <= 20)) {
        setModifications(prev => ({
          ...prev,
          [id]: {
            ...prev[id],
            note: valeur === '' ? '' : noteNum
          }
        }));
        calculerNoteFinale(id, valeur === '' ? '' : noteNum, modifications[id]?.bonus || notesEtudiants.find(e => e.id === id)?.bonus || 0);
      }
    } else if (champ === 'bonus') {
      if (valeur === '' || (!isNaN(noteNum) && noteNum >= 0 && noteNum <= 3)) {
        setModifications(prev => ({
          ...prev,
          [id]: {
            ...prev[id],
            bonus: valeur === '' ? '' : noteNum
          }
        }));
        const noteActuelle = modifications[id]?.note !== undefined 
          ? modifications[id].note 
          : notesEtudiants.find(e => e.id === id)?.note || 0;
        calculerNoteFinale(id, noteActuelle, valeur === '' ? '' : noteNum);
      }
    }
  };

  const calculerNoteFinale = (id, note, bonus) => {
    const noteNum = parseFloat(note) || 0;
    const bonusNum = parseFloat(bonus) || 0;
    const noteFinale = Math.min(20, noteNum + bonusNum);
    
    setModifications(prev => ({
      ...prev,
      [id]: {
        ...prev[id],
        noteFinale: noteFinale.toFixed(1)
      }
    }));
  };

  const obtenirValeur = (id, champ) => {
    if (modifications[id] && modifications[id][champ] !== undefined) {
      return modifications[id][champ];
    }
    return notesEtudiants.find(e => e.id === id)?.[champ] || '';
  };

  const sauvegarderNotes = () => {
    const nouvellesNotes = notesEtudiants.map(etudiant => ({
      ...etudiant,
      note: modifications[etudiant.id]?.note !== undefined 
        ? modifications[etudiant.id].note 
        : etudiant.note,
      bonus: modifications[etudiant.id]?.bonus !== undefined 
        ? modifications[etudiant.id].bonus 
        : etudiant.bonus,
      noteFinale: modifications[etudiant.id]?.noteFinale !== undefined 
        ? parseFloat(modifications[etudiant.id].noteFinale)
        : etudiant.noteFinale
    }));

    setNotesEtudiants(nouvellesNotes);
    setModifications({});
    console.log('Grades saved:', nouvellesNotes);
    alert('Notes sauvegardées avec succès');
  };

  // If an evaluation is selected, show the grade entry screen
  if (evaluationActive) {
    return (
      <div className="gestion-notes-professeur">
        <div className="gestion-notes-professeur-header">
          <div>
            <h1 className="gestion-notes-professeur-titre">{evaluationActive.titre}</h1>
            <p className="gestion-notes-professeur-sous-titre">
              Saisissez les notes pour cette évaluation
            </p>
          </div>
          <BoutonAction
            libelle="Retour"
            variante="secondaire"
            taille="moyen"
            onClick={fermerEvaluation}
          />
        </div>

        <div className="instructions-box">
          <Info size={18} />
          <div className="instructions-content">
            <strong>Instructions:</strong>
            <ul>
              <li>La note principale est sur 20 points</li>
              <li>Le bonus permet d'ajouter jusqu'à 3 points supplémentaires</li>
              <li>La note finale est calculée automatiquement (Note + Bonus, max 20)</li>
            </ul>
          </div>
        </div>

        <div className="notes-etudiants-liste">
          {notesEtudiants.map((etudiant) => {
            const noteActuelle = obtenirValeur(etudiant.id, 'note');
            const bonusActuel = obtenirValeur(etudiant.id, 'bonus');
            const noteFinaleActuelle = obtenirValeur(etudiant.id, 'noteFinale');
            const estModifie = modifications[etudiant.id] !== undefined;

            return (
              <div key={etudiant.id} className={`etudiant-note-card ${estModifie ? 'etudiant-note-modifiee' : ''}`}>
                <div className="etudiant-note-header">
                  <div className="etudiant-avatar">
                    {etudiant.initiales}
                  </div>
                  <div className="etudiant-info">
                    <h3 className="etudiant-nom">{etudiant.prenom} {etudiant.nom}</h3>
                  </div>
                </div>
                <div className="etudiant-note-inputs">
                  <div className="note-input-group">
                    <label className="note-input-label">Note /20</label>
                    <input
                      type="number"
                      className="note-input"
                      value={noteActuelle}
                      onChange={(e) => gererChangementNote(etudiant.id, 'note', e.target.value)}
                      min="0"
                      max="20"
                      step="0.5"
                      placeholder="0"
                    />
                  </div>
                  <div className="note-input-group">
                    <label className="note-input-label">Bonus /3</label>
                    <input
                      type="number"
                      className="note-input"
                      value={bonusActuel}
                      onChange={(e) => gererChangementNote(etudiant.id, 'bonus', e.target.value)}
                      min="0"
                      max="3"
                      step="0.5"
                      placeholder="0"
                    />
                  </div>
                  <div className="note-input-group note-finale-group">
                    <label className="note-input-label">Note finale</label>
                    <div className="note-finale-display">
                      {noteFinaleActuelle}/20
                    </div>
                  </div>
                </div>
              </div>
            );
          })}
        </div>

        <div className="notes-actions">
          <BoutonAction
            libelle="Sauvegarder les notes"
            variante="primaire"
            taille="grand"
            onClick={sauvegarderNotes}
            disabled={Object.keys(modifications).length === 0}
          />
        </div>
      </div>
    );
  }

  // Main grade management page with tabs
  return (
    <div className="gestion-notes-professeur">
      <div className="gestion-notes-professeur-header">
        <div>
          <h1 className="gestion-notes-professeur-titre">Gestion des Notes</h1>
          <p className="gestion-notes-professeur-sous-titre">
            Saisissez et gérez les notes par classe
          </p>
        </div>
        <BoutonAction
          libelle="+ Nouvelle évaluation"
          variante="primaire"
          taille="moyen"
          icone={Plus}
          onClick={() => alert('Nouvelle évaluation (à implémenter)')}
        />
      </div>

      {/* Class Tabs */}
      <div className="classes-tabs">
        {classes.map((classe) => (
          <button
            key={classe.id}
            className={`classe-tab ${classeActive === classe.id ? 'classe-tab-active' : ''}`}
            onClick={() => setClasseActive(classe.id)}
          >
            {classe.badge && (
              <span className="classe-badge">{classe.badge}</span>
            )}
            <span>{classe.label}</span>
          </button>
        ))}
      </div>

      {/* Class Info */}
      {classeInfo && classeInfo.badge && (
        <div className="classe-info">
          <div className="classe-badge-large">{classeInfo.badge}</div>
          <div className="classe-info-content">
            <h2 className="classe-nom">{classeInfo.label} Informatique</h2>
            <p className="classe-resume">
              {totalEtudiants} étudiants • {totalEvaluations} évaluations
            </p>
          </div>
        </div>
      )}

      {/* Evaluations List */}
      <div className="evaluations-liste">
        {evaluationsClasse.length === 0 ? (
          <div className="evaluations-vide">
            <p>Aucune évaluation pour cette classe</p>
          </div>
        ) : (
          evaluationsClasse.map((evaluation) => (
            <div key={evaluation.id} className="evaluation-card">
              <div className="evaluation-info">
                <h3 className="evaluation-titre">{evaluation.titre}</h3>
                <p className="evaluation-details">
                  Coef. {evaluation.coefficient} • Note sur {evaluation.noteSur}
                </p>
              </div>
              <div className="evaluation-statut">
                {evaluation.statut === 'complete' ? (
                  <>
                    <CheckCircle size={18} className="statut-icone statut-complete" />
                    <span className="statut-texte statut-complete">
                      {evaluation.notesSaisies}/{evaluation.totalEtudiants} notes saisies
                    </span>
                    <p className="evaluation-moyenne">
                      Moyenne: {evaluation.moyenne}/20
                    </p>
                  </>
                ) : (
                  <>
                    <Clock size={18} className="statut-icone statut-partial" />
                    <span className="statut-texte statut-partial">
                      {evaluation.notesSaisies}/{evaluation.totalEtudiants} notes saisies
                    </span>
                    <p className="evaluation-moyenne">
                      Moyenne partielle: {evaluation.moyennePartielle}/20
                    </p>
                  </>
                )}
              </div>
              <BoutonAction
                libelle="Saisir les notes"
                variante="primaire"
                taille="moyen"
                onClick={() => ouvrirEvaluation(evaluation)}
              />
            </div>
          ))
        )}
      </div>
    </div>
  );
};

export default GestionNotesProfesseur;
