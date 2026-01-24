import { useState } from 'react';
import { Check, ChevronDown, CheckCircle } from 'lucide-react';
import './GestionNotesProfesseur.css';

const GestionNotesProfesseur = () => {
  // Mock Data matching the screenshot
  const [etudiants, setEtudiants] = useState([
    { id: 1, prenom: 'Alice', nom: 'Bertrand', numero: '2023001', note: 16, initiales: 'AB', color: 'blue', classe: 'L3 Informatique' },
    { id: 2, prenom: 'Jean', nom: 'Dupont', numero: '2023012', note: 12.5, initiales: 'JD', color: 'pink', classe: 'L3 Informatique' },
    { id: 3, prenom: 'Marie', nom: 'Curie', numero: '2023045', note: 18, initiales: 'MC', color: 'purple', classe: 'L3 Informatique' },
    { id: 4, prenom: 'Thomas', nom: 'Edison', numero: '2023099', note: 9, initiales: 'TE', color: 'brown', classe: 'M1 Génie Logiciel' },
    { id: 5, prenom: 'Sophie', nom: 'Germain', numero: '2023150', note: 14, initiales: 'SG', color: 'green', classe: 'M1 Génie Logiciel' },
    { id: 6, prenom: 'Albert', nom: 'Einstein', numero: '2023210', note: 19, initiales: 'AE', color: 'grey', classe: 'M1 Génie Logiciel' },
  ]);

  const [examenSelectionne, setExamenSelectionne] = useState('Examen Final - Semestre 1');
  const [classeSelectionnee, setClasseSelectionnee] = useState('L3 Informatique');

  const classes = ['L3 Informatique', 'M1 Génie Logiciel'];

  const handleNoteChange = (id, newNote) => {
    setEtudiants(prev => prev.map(etudiant =>
      etudiant.id === id ? { ...etudiant, note: newNote } : etudiant
    ));
  };

  const getStatus = (note) => {
    const numNote = parseFloat(note);
    if (isNaN(numNote)) return null;
    return numNote >= 10 ? 'VALIDÉ' : 'N. VALIDÉ';
  };

  const etudiantsFiltres = etudiants.filter(e => e.classe === classeSelectionnee);

  return (
    <div className="gestion-notes-container">
      {/* Header Section */}
      <div className="page-header">
        <div className="header-text">
          <h1>Gestion des Notes</h1>
          <p>Mise à jour des notes pour le cours INF101 - Algorithmique.</p>
        </div>
        <button className="btn-save">
          <CheckCircle size={18} />
          Enregistrer les notes
        </button>
      </div>

      {/* Main Content Card */}
      <div className="content-card">

        {/* Controls Bar */}
        <div className="controls-bar">
          <div className="filters-group">
            {/* Class Select */}
            <div className="select-wrapper-light">
              <select
                value={classeSelectionnee}
                onChange={(e) => setClasseSelectionnee(e.target.value)}
              >
                {classes.map(c => <option key={c} value={c}>{c}</option>)}
              </select>
              <ChevronDown size={16} className="select-arrow" />
            </div>

            {/* Exam Select */}
            <div className="select-wrapper-light">
              <select
                value={examenSelectionne}
                onChange={(e) => setExamenSelectionne(e.target.value)}
              >
                <option>Examen Final - Semestre 1</option>
                <option>Contrôle Continu - Semestre 1</option>
                <option>Projet - Semestre 1</option>
              </select>
              <ChevronDown size={16} className="select-arrow" />
            </div>
          </div>

          <span className="student-count">{etudiantsFiltres.length} ÉTUDIANTS INSCRITS</span>
        </div>

        {/* Table */}
        <table className="grades-table">
          <thead>
            <tr>
              <th className="th-student">ÉTUDIANT</th>
              <th className="th-id">NUMÉRO ÉTUDIANT</th>
              <th className="th-grade">NOTE (/20)</th>
              <th className="th-status">STATUS</th>
            </tr>
          </thead>
          <tbody>
            {etudiantsFiltres.map((etudiant) => (
              <tr key={etudiant.id}>
                {/* Student Info */}
                <td className="td-student">
                  <div className={`avatar avatar-${etudiant.color}`}>
                    {etudiant.initiales}
                  </div>
                  <span className="student-name">{etudiant.prenom} {etudiant.nom}</span>
                </td>

                {/* ID */}
                <td className="td-id">{etudiant.numero}</td>

                {/* Grade Input */}
                <td className="td-grade">
                  <div className="grade-input-wrapper">
                    <input
                      type="text"
                      value={etudiant.note}
                      onChange={(e) => handleNoteChange(etudiant.id, e.target.value)}
                      className="grade-input"
                    />
                  </div>
                </td>

                {/* Status Badge */}
                <td className="td-status">
                  <span className={`status-badge status-${getStatus(etudiant.note).toLowerCase().replace('. ', '').replace('é', 'e')}`}>
                    {getStatus(etudiant.note)}
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

export default GestionNotesProfesseur;
