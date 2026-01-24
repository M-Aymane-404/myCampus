import { useState } from 'react';
import { Save } from 'lucide-react';
import './GestionPresenceProfesseur.css';

const GestionPresenceProfesseur = () => {
  // Mock Data matching screenshot
  // Students: Alice, Jean, Marie, Thomas, Sophie, Albert, Nikola, Ada
  const [etudiants, setEtudiants] = useState([
    { id: 1, prenom: 'Alice', nom: 'Bertrand', id_etudiant: '2023001', initiales: 'AB', color: 'blue', statut: 'present', classe: 'L3-A', module: 'Algorithmique' },
    { id: 2, prenom: 'Jean', nom: 'Dupont', id_etudiant: '2023012', initiales: 'JD', color: 'pink', statut: 'present', classe: 'L3-A', module: 'Algorithmique' },
    { id: 3, prenom: 'Marie', nom: 'Curie', id_etudiant: '2023045', initiales: 'MC', color: 'purple', statut: 'absent', classe: 'L3-A', module: 'Algorithmique' },
    { id: 4, prenom: 'Thomas', nom: 'Edison', id_etudiant: '2023099', initiales: 'TE', color: 'brown', statut: 'present', classe: 'M1-B', module: 'POO' },
    { id: 5, prenom: 'Sophie', nom: 'Germain', id_etudiant: '2023150', initiales: 'SG', color: 'green', statut: 'present', classe: 'M1-B', module: 'POO' },
    { id: 6, prenom: 'Albert', nom: 'Einstein', id_etudiant: '2023210', initiales: 'AE', color: 'grey', statut: 'justifie', classe: 'M1-B', module: 'POO' },
    { id: 7, prenom: 'Nikola', nom: 'Tesla', id_etudiant: '2023255', initiales: 'NT', color: 'green-light', statut: 'present', classe: 'L3-A', module: 'Algorithmique' },
    { id: 8, prenom: 'Ada', nom: 'Lovelace', id_etudiant: '2023300', initiales: 'AL', color: 'purple-light', statut: 'present', classe: 'M1-B', module: 'POO' }
  ]);

  const [classeSelectionnee, setClasseSelectionnee] = useState('All Classes');
  const [moduleSelectionne, setModuleSelectionne] = useState('All Modules');

  const classes = ['All Classes', 'L3-A', 'M1-B'];
  const modules = ['All Modules', 'Algorithmique', 'POO'];

  const handleStatusChange = (id, newStatus) => {
    setEtudiants(prev => prev.map(etudiant =>
      etudiant.id === id ? { ...etudiant, statut: newStatus } : etudiant
    ));
  };

  const etudiantsFiltres = etudiants.filter(etudiant => {
    const matchClasse = classeSelectionnee === 'All Classes' || etudiant.classe === classeSelectionnee;
    const matchModule = moduleSelectionne === 'All Modules' || etudiant.module === moduleSelectionne;
    return matchClasse && matchModule;
  });

  const total = etudiantsFiltres.length;
  const presents = etudiantsFiltres.filter(e => e.statut === 'present').length;
  const absents = etudiantsFiltres.filter(e => e.statut === 'absent').length;

  return (
    <div className="gestion-presence-container">

      {/* Top Header & Stats */}
      <div className="presence-header-row">
        <div className="presence-title-block">
          <h1>Appel des Étudiants</h1>
          <p>Session : 23/01/2026 - 09:00 - Salle B204</p>
        </div>

        <div className="stats-box">
          <div className="stat-item">
            <span className="stat-label">PRÉSENTS</span>
            <span className="stat-value text-green">{presents}</span>
          </div>
          <div className="stat-divider"></div>
          <div className="stat-item">
            <span className="stat-label">ABSENTS</span>
            <span className="stat-value text-red">{absents}</span>
          </div>
          <div className="stat-divider"></div>
          <div className="stat-item">
            <span className="stat-label">TOTAL</span>
            <span className="stat-value text-dark">{total}</span>
          </div>
        </div>
      </div>

      {/* Filters */}
      <div className="filters-row">
        <div className="filter-group">
          <label className="filter-label">Class:</label>
          <div className="select-wrapper-rounded">
            <select
              value={classeSelectionnee}
              onChange={(e) => setClasseSelectionnee(e.target.value)}
            >
              {classes.map(c => <option key={c} value={c}>{c}</option>)}
            </select>
          </div>
        </div>
        <div className="filter-group">
          <label className="filter-label">Module:</label>
          <div className="select-wrapper-rounded">
            <select
              value={moduleSelectionne}
              onChange={(e) => setModuleSelectionne(e.target.value)}
            >
              {modules.map(m => <option key={m} value={m}>{m}</option>)}
            </select>
          </div>
        </div>
      </div>

      {/* Student Grid */}
      <div className="student-grid">
        {etudiantsFiltres.map(etudiant => (
          <div key={etudiant.id} className="student-card">

            {/* Avatar */}
            <div className={`student-avatar avatar-${etudiant.color}`}>
              {etudiant.initiales}
            </div>

            {/* Info */}
            <h3 className="student-modal-name">{etudiant.prenom} {etudiant.nom}</h3>
            <span className="student-modal-id">ID: {etudiant.id_etudiant}</span>

            {/* Actions */}
            <div className="student-actions">
              <button
                className={`btn-state btn-present ${etudiant.statut === 'present' ? 'active' : ''}`}
                onClick={() => handleStatusChange(etudiant.id, 'present')}
              >
                Présent
              </button>
              <button
                className={`btn-state btn-absent ${etudiant.statut === 'absent' ? 'active' : ''}`}
                onClick={() => handleStatusChange(etudiant.id, 'absent')}
              >
                Absent
              </button>
            </div>

            <button
              className={`btn-full-width btn-justifie ${etudiant.statut === 'justifie' ? 'active' : ''}`}
              onClick={() => handleStatusChange(etudiant.id, 'justifie')}
            >
              JUSTIFIÉ
            </button>

          </div>
        ))}
      </div>

      {/* Footer Submit */}
      <div className="page-footer">
        <button className="btn-submit-final">
          Soumettre l'appel final
        </button>
      </div>

    </div>
  );
};

export default GestionPresenceProfesseur;
