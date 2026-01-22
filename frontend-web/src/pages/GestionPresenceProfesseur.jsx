import { useState } from 'react';
import { UserCheck, CheckCircle, XCircle, Save, Calendar } from 'lucide-react';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BoutonAction from '../composants/communs/BoutonAction';
import './GestionPresenceProfesseur.css';

const GestionPresenceProfesseur = () => {
  const [presences, setPresences] = useState([
    { id: 1, apogee: 'APG001', nom: 'Alami', prenom: 'Ahmed', present: true, classe: 'L3-A', module: 'GI-INFO-101' },
    { id: 2, apogee: 'APG002', nom: 'Bennani', prenom: 'Fatima', present: true, classe: 'L3-A', module: 'GI-INFO-101' },
    { id: 3, apogee: 'APG003', nom: 'Chakir', prenom: 'Youssef', present: false, classe: 'L3-A', module: 'GI-INFO-101' },
    { id: 4, apogee: 'APG004', nom: 'Daoudi', prenom: 'Sara', present: true, classe: 'L3-A', module: 'GI-INFO-101' },
    { id: 5, apogee: 'APG005', nom: 'El Amrani', prenom: 'Mohamed', present: null, classe: 'L3-B', module: 'GI-INFO-102' },
    { id: 6, apogee: 'APG006', nom: 'Fassi', prenom: 'Aicha', present: true, classe: 'L3-B', module: 'GI-INFO-102' },
  ]);

  const [filtreClasse, setFiltreClasse] = useState('');
  const [filtreModule, setFiltreModule] = useState('');
  const [dateSeance, setDateSeance] = useState(new Date().toISOString().split('T')[0]);
  const [modifications, setModifications] = useState({});

  const classes = [
    { value: '', label: 'All Classes' },
    { value: 'L3-A', label: 'L3-A' },
    { value: 'L3-B', label: 'L3-B' },
    { value: 'M1-A', label: 'M1-A' }
  ];

  const modules = [
    { value: '', label: 'All Modules' },
    { value: 'GI-INFO-101', label: 'GI-INFO-101 - Algorithmique' },
    { value: 'GI-INFO-102', label: 'GI-INFO-102 - Structures de données' },
    { value: 'GI-INFO-103', label: 'GI-INFO-103 - POO' }
  ];

  const presencesFiltrees = presences.filter(presence => {
    if (filtreClasse && presence.classe !== filtreClasse) return false;
    if (filtreModule && presence.module !== filtreModule) return false;
    return true;
  });

  const gererChangementPresence = (id, nouvellePresence) => {
    setModifications(prev => ({ ...prev, [id]: nouvellePresence }));
  };

  const obtenirPresence = (id) => {
    return modifications[id] !== undefined 
      ? modifications[id] 
      : presences.find(p => p.id === id)?.present;
  };

  const marquerTousPresents = () => {
    const nouvellesModifications = {};
    presencesFiltrees.forEach(p => {
      nouvellesModifications[p.id] = true;
    });
    setModifications(prev => ({ ...prev, ...nouvellesModifications }));
  };

  const marquerTousAbsents = () => {
    const nouvellesModifications = {};
    presencesFiltrees.forEach(p => {
      nouvellesModifications[p.id] = false;
    });
    setModifications(prev => ({ ...prev, ...nouvellesModifications }));
  };

  const sauvegarderPresences = () => {
    const nouvellesPresences = presences.map(presence => ({
      ...presence,
      present: modifications[presence.id] !== undefined ? modifications[presence.id] : presence.present
    }));

    setPresences(nouvellesPresences);
    setModifications({});
    console.log('Attendance saved:', nouvellesPresences);
    alert('Présences sauvegardées avec succès');
  };

  const totalEtudiants = presencesFiltrees.length;
  const presents = presencesFiltrees.filter(p => obtenirPresence(p.id) === true).length;
  const absents = presencesFiltrees.filter(p => obtenirPresence(p.id) === false).length;
  const nonMarques = presencesFiltrees.filter(p => obtenirPresence(p.id) === null || obtenirPresence(p.id) === undefined).length;

  return (
    <div className="gestion-presence-professeur">
      <div className="gestion-presence-professeur-header">
        <h1 className="gestion-presence-professeur-titre">Student Attendance</h1>
        <p className="gestion-presence-professeur-sous-titre">Mark student attendance for your classes</p>
      </div>

      <div className="gestion-presence-professeur-statistiques">
        <CarteStatistique
          titre="Total Students"
          valeur={totalEtudiants}
          variation={{ valeur: totalEtudiants, label: '', positif: true }}
          icone={UserCheck}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Present"
          valeur={presents}
          variation={{ valeur: presents, label: '', positif: true }}
          icone={CheckCircle}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Absent"
          valeur={absents}
          variation={{ valeur: absents, label: '', positif: false }}
          icone={XCircle}
          couleur="error"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Not Marked"
          valeur={nonMarques}
          variation={{ valeur: nonMarques, label: '', positif: false }}
          icone={UserCheck}
          couleur="accent"
          iconeCouleur="dark"
        />
      </div>

      <div className="gestion-presence-professeur-filtres">
        <div className="filtre-item">
          <label className="filtre-label">Date:</label>
          <input
            type="date"
            className="filtre-input-date"
            value={dateSeance}
            onChange={(e) => setDateSeance(e.target.value)}
          />
        </div>
        <div className="filtre-item">
          <label className="filtre-label">Class:</label>
          <select 
            className="filtre-select"
            value={filtreClasse}
            onChange={(e) => setFiltreClasse(e.target.value)}
          >
            {classes.map(cl => (
              <option key={cl.value} value={cl.value}>{cl.label}</option>
            ))}
          </select>
        </div>
        <div className="filtre-item">
          <label className="filtre-label">Module:</label>
          <select 
            className="filtre-select"
            value={filtreModule}
            onChange={(e) => setFiltreModule(e.target.value)}
          >
            {modules.map(mod => (
              <option key={mod.value} value={mod.value}>{mod.label}</option>
            ))}
          </select>
        </div>
      </div>

      <div className="gestion-presence-professeur-actions-rapides">
        <BoutonAction
          libelle="Mark All Present"
          variante="success"
          taille="moyen"
          icone={CheckCircle}
          onClick={marquerTousPresents}
        />
        <BoutonAction
          libelle="Mark All Absent"
          variante="error"
          taille="moyen"
          icone={XCircle}
          onClick={marquerTousAbsents}
        />
        <BoutonAction
          libelle="Save Attendance"
          variante="primaire"
          taille="moyen"
          icone={Save}
          onClick={sauvegarderPresences}
          disabled={Object.keys(modifications).length === 0}
        />
      </div>

      <div className="gestion-presence-professeur-tableau">
        <div className="tableau-presence-header">
          <h2 className="tableau-presence-titre">Attendance List</h2>
        </div>
        <div className="tableau-presence-contenu">
          <table className="tableau-presence-table">
            <thead>
              <tr>
                <th>Apogee</th>
                <th>Last Name</th>
                <th>First Name</th>
                <th>Class</th>
                <th>Module</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {presencesFiltrees.map((etudiant) => {
                const presence = obtenirPresence(etudiant.id);
                const estModifie = modifications[etudiant.id] !== undefined;
                
                return (
                  <tr 
                    key={etudiant.id} 
                    className={`${estModifie ? 'ligne-modifiee' : ''} ${presence === true ? 'ligne-presente' : presence === false ? 'ligne-absente' : ''}`}
                  >
                    <td>{etudiant.apogee}</td>
                    <td>{etudiant.nom}</td>
                    <td>{etudiant.prenom}</td>
                    <td>{etudiant.classe}</td>
                    <td>{etudiant.module}</td>
                    <td>
                      <div className="presence-boutons">
                        <button
                          className={`bouton-presence bouton-present ${presence === true ? 'actif' : ''}`}
                          onClick={() => gererChangementPresence(etudiant.id, true)}
                          title="Present"
                        >
                          <CheckCircle size={18} />
                          <span>Present</span>
                        </button>
                        <button
                          className={`bouton-presence bouton-absent ${presence === false ? 'actif' : ''}`}
                          onClick={() => gererChangementPresence(etudiant.id, false)}
                          title="Absent"
                        >
                          <XCircle size={18} />
                          <span>Absent</span>
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default GestionPresenceProfesseur;
