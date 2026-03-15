/*import { useState } from 'react';
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
*/
      /* Top Header & Stats */
       // <div className="presence-header-row">
       //  <div className="presence-title-block">
       //    <h1>Appel des Étudiants</h1>
       //    <p>Session : 23/01/2026 - 09:00 - Salle B204</p>
       //  </div>

        // <div className="stats-box">
        //   <div className="stat-item">
        //     <span className="stat-label">PRÉSENTS</span>
        //     <span className="stat-value text-green">{presents}</span>
        //   </div>
        //   <div className="stat-divider"></div>
        //   <div className="stat-item">
        //     <span className="stat-label">ABSENTS</span>
        //     <span className="stat-value text-red">{absents}</span>
        //   </div>
        //   <div className="stat-divider"></div>
        //   <div className="stat-item">
        //     <span className="stat-label">TOTAL</span>
        //     <span className="stat-value text-dark">{total}</span>
        //   </div>
        // </div>
      // </div>
      //
      // {/* Filters */}
      // <div className="filters-row">
      //   <div className="filter-group">
      //     <label className="filter-label">Class:</label>
      //     <div className="select-wrapper-rounded">
      //       <select
      //         value={classeSelectionnee}
      //         onChange={(e) => setClasseSelectionnee(e.target.value)}
      //       >
      //         {classes.map(c => <option key={c} value={c}>{c}</option>)}
      //       </select>
      //     </div>
      //   </div>
      //   <div className="filter-group">
      //     <label className="filter-label">Module:</label>
      //     <div className="select-wrapper-rounded">
      //       <select
      //         value={moduleSelectionne}
      //         onChange={(e) => setModuleSelectionne(e.target.value)}
      //       >
      //         {modules.map(m => <option key={m} value={m}>{m}</option>)}
      //       </select>
      //     </div>
      //   </div>
      // </div>

      // {/* Student Grid */}
      // <div className="student-grid">
      //   {etudiantsFiltres.map(etudiant => (
      //     <div key={etudiant.id} className="student-card">
      //
      //       {/* Avatar */}
      //       <div className={`student-avatar avatar-${etudiant.color}`}>
      //         {etudiant.initiales}
      //       </div>
      //
      //       {/* Info */}
      //       <h3 className="student-modal-name">{etudiant.prenom} {etudiant.nom}</h3>
      //       <span className="student-modal-id">ID: {etudiant.id_etudiant}</span>
      //
      //       {/* Actions */}
      //       <div className="student-actions">
      //         <button
      //           className={`btn-state btn-present ${etudiant.statut === 'present' ? 'active' : ''}`}
      //           onClick={() => handleStatusChange(etudiant.id, 'present')}
      //         >
      //           Présent
      //         </button>
      //         <button
      //           className={`btn-state btn-absent ${etudiant.statut === 'absent' ? 'active' : ''}`}
      //           onClick={() => handleStatusChange(etudiant.id, 'absent')}
      //         >
      //           Absent
      //         </button>
      //       </div>
      //
      //       <button
      //         className={`btn-full-width btn-justifie ${etudiant.statut === 'justifie' ? 'active' : ''}`}
      //         onClick={() => handleStatusChange(etudiant.id, 'justifie')}
// {/*            >*/}
// {/*              JUSTIFIÉ*/}
// {/*            </button>*/}
//
// {/*          </div>*/}
// {/*        ))}*/}
// {/*      </div>*/}
//
// {/*      /* Footer Submit */*/}
// {/*      <div className="page-footer">*/}
// {/*        <button className="btn-submit-final">*/}
// {/*          Soumettre l'appel final*/}
// {/*        </button>*/}
// {/*      </div>*/}
//
// {/*    </div>*/}
// {/*  );*/}
// {/*};*/}

//export default GestionPresenceProfesseur;

import { useState, useEffect } from 'react';
import { UserCheck, ChevronDown, Loader2, X, Calendar, Clock, MapPin } from 'lucide-react';
import { apiEnseignementsProfesseur } from '../services/professeur/apiEnseignementsProfesseur';
import  apiSeancesProfesseur  from '../services/professeur/apiSeancesProfesseur';
import { apiPresencesProfesseur }     from '../services/professeur/apiPresencesProfesseur';
import './GestionPresenceProfesseur.css';

const AVATAR_COLORS = ['blue', 'pink', 'purple', 'brown', 'green', 'grey', 'green-light', 'purple-light'];
const colorFor = (i) => AVATAR_COLORS[i % AVATAR_COLORS.length];

const initiales = (nom, prenom) =>
    `${(prenom || '')[0] || ''}${(nom || '')[0] || ''}`.toUpperCase();

const formatDate = (d) => {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('fr-FR', { weekday: 'long', day: '2-digit', month: 'long' });
};

const formatHeure = (d) => {
  if (!d) return '';
  return new Date(d).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

const TYPE_COLOR = { COURS: 'blue', TD: 'green', TP: 'purple' };

// ─── Composant principal ──────────────────────────────────────────────────────

const GestionPresenceProfesseur = () => {

  // ── Étape 1 : enseignements ───────────────────────────────────────────────
  const [enseignements, setEnseignements] = useState([]);
  const [selectedEnseignement, setSelectedEnseignement] = useState(null);
  const [loadingEns, setLoadingEns]       = useState(true);

  // ── Étape 2 : séances de la classe/matière sélectionnée ───────────────────
  const [seances, setSeances]             = useState([]);
  const [selectedSeance, setSelectedSeance] = useState(null);
  const [loadingSeances, setLoadingSeances] = useState(false);

  // ── Étape 3 : feuille d'appel ─────────────────────────────────────────────
  const [feuille, setFeuille]             = useState(null);
  const [lignes, setLignes]               = useState([]);
  const [loadingFeuille, setLoadingFeuille] = useState(false);

  // ── UI ────────────────────────────────────────────────────────────────────
  const [saving, setSaving]               = useState({});   // { [apogee]: bool }
  const [error, setError]                 = useState(null);

  // ─── Étape 1 : charger les enseignements ──────────────────────────────────
  useEffect(() => {
    const load = async () => {
      try {
        const data = await apiEnseignementsProfesseur.listerEnseignements();
        setEnseignements(data || []);
        if (data?.length > 0) setSelectedEnseignement(data[0]);
      } catch (err) {
        setError('Impossible de charger les cours : ' + err.message);
      } finally {
        setLoadingEns(false);
      }
    };
    load();
  }, []);

  // ─── Étape 2 : charger les séances quand enseignement change ─────────────
  useEffect(() => {
    if (!selectedEnseignement) return;
    let cancelled = false;
    const load = async () => {
      setLoadingSeances(true);
      setSeances([]);
      setSelectedSeance(null);
      setFeuille(null);
      setLignes([]);
      try {
        const data = await apiSeancesProfesseur.rechercherSeances({
          codeClasse:  selectedEnseignement.codeClasse,
          codeMatiere: selectedEnseignement.codeMatiere,
        });
        // Tri par date décroissante (les plus récentes en premier)
        const sorted = (data || []).sort((a, b) =>
            new Date(b.heureDebut || b.date || 0) - new Date(a.heureDebut || a.date || 0)
        );
        if (!cancelled) {
          setSeances(sorted);
          if (sorted.length > 0) setSelectedSeance(sorted[0]);
        }
      } catch {
        if (!cancelled) setSeances([]);
      } finally {
        if (!cancelled) setLoadingSeances(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [selectedEnseignement]);

  // ─── Étape 3 : charger la feuille d'appel quand séance change ────────────
  useEffect(() => {
    if (!selectedSeance) return;
    let cancelled = false;
    const load = async () => {
      setLoadingFeuille(true);
      setFeuille(null);
      setLignes([]);
      setError(null);
      try {
        const data = await apiPresencesProfesseur.chargerFeuille(selectedSeance.id);
        if (!cancelled) {
          setFeuille(data);
          setLignes(data.lignes || []);
        }
      } catch (err) {
        if (!cancelled) setError('Erreur lors du chargement : ' + err.message);
      } finally {
        if (!cancelled) setLoadingFeuille(false);
      }
    };
    load();
    return () => { cancelled = true; };
  }, [selectedSeance]);

  // ─── Saisir présence ─────────────────────────────────────────────────────
  const handleStatusChange = async (apogee, newStatus) => {
    // Mise à jour optimiste immédiate
    setLignes(prev => prev.map(l => l.apogee === apogee ? { ...l, status: newStatus } : l));
    setSaving(prev => ({ ...prev, [apogee]: true }));
    try {
      const updated = await apiPresencesProfesseur.saisirPresence(
          selectedSeance.id,
          { apogee, status: newStatus }
      );
      setLignes(prev => prev.map(l => l.apogee === apogee ? { ...l, ...updated } : l));
    } catch (err) {
      // Rollback
      setLignes(prev =>
          prev.map(l =>
              l.apogee === apogee
                  ? { ...l, status: newStatus === 'PRESENT' ? 'ABSENT' : 'PRESENT' }
                  : l
          )
      );
      setError('Erreur enregistrement : ' + err.message);
    } finally {
      setSaving(prev => ({ ...prev, [apogee]: false }));
    }
  };

  // ── Stats ─────────────────────────────────────────────────────────────────
  const presents = lignes.filter(l => l.status === 'PRESENT').length;
  const absents  = lignes.filter(l => l.status === 'ABSENT').length;
  const total    = lignes.length;
  const taux     = total > 0 ? Math.round((presents / total) * 100) : 0;

  const couleur  = selectedSeance ? (TYPE_COLOR[selectedSeance.type] || 'grey') : 'grey';

  // ─────────────────────────────────────────────────────────────────────────
  return (
      <div className="gestion-presence-container">

        {/* ── Header ──────────────────────────────────────────────────── */}
        <div className="presence-header-row">
          <div className="presence-title-block">
            <h1>Appel des Étudiants</h1>
            {selectedSeance ? (
                <p>
                  <span className={`type-badge-header type-${couleur}`}>{selectedSeance.type}</span>
                  {' '}{formatDate(selectedSeance.heureDebut || selectedSeance.date)}
                  {selectedSeance.heureDebut && ` · ${formatHeure(selectedSeance.heureDebut)}`}
                  {selectedSeance.heureFin   && ` → ${formatHeure(selectedSeance.heureFin)}`}
                  {selectedSeance.salle && ` · Salle ${selectedSeance.salle}`}
                </p>
            ) : (
                <p>Sélectionnez un cours et une séance</p>
            )}
          </div>

          {/* Stats */}
          {feuille && (
              <div className="stats-box">
                <div className="stat-item">
                  <span className="stat-label">PRÉSENTS</span>
                  <span className="stat-value text-green">{presents}</span>
                </div>
                <div className="stat-divider" />
                <div className="stat-item">
                  <span className="stat-label">ABSENTS</span>
                  <span className="stat-value text-red">{absents}</span>
                </div>
                <div className="stat-divider" />
                <div className="stat-item">
                  <span className="stat-label">TAUX</span>
                  <span className="stat-value text-dark">{taux}%</span>
                </div>
              </div>
          )}
        </div>

        {/* ── Sélecteurs ──────────────────────────────────────────────── */}
        <div className="presence-selectors-bar">

          {/* Cours (classe + matière) */}
          <div className="selector-group">
            <label className="selector-label">
              <UserCheck size={14} /> Cours
            </label>
            {loadingEns ? (
                <div className="selector-loading"><Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} /> Chargement...</div>
            ) : (
                <div className="select-wrapper-rounded">
                  <select
                      value={selectedEnseignement
                          ? `${selectedEnseignement.codeClasse}|${selectedEnseignement.codeMatiere}`
                          : ''
                      }
                      onChange={e => {
                        const [codeClasse, codeMatiere] = e.target.value.split('|');
                        const ens = enseignements.find(
                            x => x.codeClasse === codeClasse && x.codeMatiere === codeMatiere
                        );
                        setSelectedEnseignement(ens || null);
                      }}
                  >
                    {enseignements.map(ens => (
                        <option key={`${ens.codeClasse}|${ens.codeMatiere}`} value={`${ens.codeClasse}|${ens.codeMatiere}`}>
                          {ens.codeClasse} — {ens.nomMatiere || ens.codeMatiere}
                        </option>
                    ))}
                  </select>
                  <ChevronDown size={16} />
                </div>
            )}
          </div>

          {/* Séance */}
          <div className="selector-group">
            <label className="selector-label">
              <Calendar size={14} /> Séance
            </label>
            {loadingSeances ? (
                <div className="selector-loading"><Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} /> Chargement...</div>
            ) : seances.length === 0 ? (
                <div className="selector-empty">Aucune séance disponible</div>
            ) : (
                <div className="select-wrapper-rounded">
                  <select
                      value={selectedSeance?.id || ''}
                      onChange={e => {
                        const s = seances.find(x => String(x.id) === e.target.value);
                        setSelectedSeance(s || null);
                      }}
                  >
                    {seances.map(s => (
                        <option key={s.id} value={s.id}>
                          {s.type} · {formatDate(s.heureDebut || s.date)}
                          {s.heureDebut ? ` · ${formatHeure(s.heureDebut)}` : ''}
                          {s.salle ? ` · Salle ${s.salle}` : ''}
                        </option>
                    ))}
                  </select>
                  <ChevronDown size={16} />
                </div>
            )}
          </div>

        </div>

        {/* ── Erreur ──────────────────────────────────────────────────── */}
        {error && (
            <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '10px 16px', borderRadius: 8, marginBottom: 16, display: 'flex', justifyContent: 'space-between', alignItems: 'center' }}>
              <span>{error}</span>
              <button onClick={() => setError(null)} style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#b91c1c' }}><X size={14} /></button>
            </div>
        )}

        {/* ── Chargement feuille ───────────────────────────────────────── */}
        {loadingFeuille && (
            <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '2rem', color: '#94a3b8' }}>
              <Loader2 size={22} style={{ animation: 'spin 0.8s linear infinite' }} />
              Chargement de la feuille d'appel...
            </div>
        )}

        {/* ── Grille étudiants ─────────────────────────────────────────── */}
        {!loadingFeuille && lignes.length > 0 && (
            <div className="student-grid">
              {lignes.map((ligne, index) => (
                  <div key={ligne.idEtudiant || ligne.apogee} className="student-card">
                    <div className={`student-avatar avatar-${colorFor(index)}`}>
                      {initiales(ligne.nomEtudiant, ligne.prenomEtudiant)}
                    </div>
                    <h3 className="student-modal-name">
                      {ligne.prenomEtudiant} {ligne.nomEtudiant}
                    </h3>
                    <span className="student-modal-id">Apogée : {ligne.apogee}</span>

                    <div className="student-actions">
                      <button
                          className={`btn-state btn-present ${ligne.status === 'PRESENT' ? 'active' : ''}`}
                          onClick={() => handleStatusChange(ligne.apogee, 'PRESENT')}
                          disabled={saving[ligne.apogee]}
                      >
                        {saving[ligne.apogee] ? '...' : '✓'} Présent
                      </button>
                      <button
                          className={`btn-state btn-absent ${ligne.status === 'ABSENT' ? 'active' : ''}`}
                          onClick={() => handleStatusChange(ligne.apogee, 'ABSENT')}
                          disabled={saving[ligne.apogee]}
                      >
                        {saving[ligne.apogee] ? '...' : '✗'} Absent
                      </button>
                    </div>
                  </div>
              ))}
            </div>
        )}

        {/* ── État vide — séances sélectionnée mais pas d'étudiants ──── */}
        {!loadingFeuille && selectedSeance && lignes.length === 0 && !error && (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#94a3b8' }}>
              <UserCheck size={48} style={{ opacity: 0.2, margin: '0 auto 12px', display: 'block' }} />
              <p>Aucun étudiant inscrit pour cette séance.</p>
            </div>
        )}

        {/* ── État initial — rien de sélectionné ──────────────────────── */}
        {!loadingFeuille && !selectedSeance && !loadingSeances && seances.length === 0 && !loadingEns && (
            <div style={{ textAlign: 'center', padding: '3rem', color: '#94a3b8' }}>
              <Calendar size={48} style={{ opacity: 0.2, margin: '0 auto 12px', display: 'block' }} />
              <p>Aucune séance disponible pour ce cours.</p>
            </div>
        )}

      </div>
  );
};

export default GestionPresenceProfesseur;