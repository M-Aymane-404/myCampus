// import { useState } from 'react';
// import {
//   Plus, FileText, Video, ExternalLink, X
// } from 'lucide-react';
// import './GestionCoursProfesseur.css';
//
// const GestionCoursProfesseur = () => {
//   const [activeTab, setActiveTab] = useState('INF101');
//
//   // Mock Data
//   const courses = [
//     { id: 'INF101', label: 'INF101 - Informatique 101', name: 'Informatique 101' },
//     { id: 'MTH202', label: 'MTH202 - Mathématiques Appliquées', name: 'Mathématiques Appliquées' }
//   ];
//
//   const resources = {
//     'INF101': [
//       { id: 1, name: "Introduction à l'Algorithmique", type: 'PDF', date: '01/10/2023', icon: FileText, color: 'red' },
//       { id: 2, name: "Structures de Données", type: 'Video', date: '05/10/2023', icon: Video, color: 'blue' },
//       { id: 3, name: "TP 1: Listes Chaînées", type: 'TP', date: '10/10/2023', icon: Plus, color: 'green' }
//     ],
//     'MTH202': [
//       { id: 4, name: "Matrices et Vecteurs", type: 'PDF', date: '02/10/2023', icon: FileText, color: 'red' },
//       { id: 5, name: "Calcul Intégral", type: 'PDF', date: '06/10/2023', icon: FileText, color: 'red' }
//     ]
//   };

  // const currentResources = resources[activeTab] || [];
  // const currentCourseName = courses.find(c => c.id === activeTab)?.name;
  //
  // return (
  //   <div className="gestion-cours-container">
  //     {/* Header */}
  //     <div className="page-header">
  //       <div className="header-text">
  //         <h1>Gestion des Cours</h1>
  //         <p>Gérez vos supports pédagogiques, TP et TD.</p>
  //       </div>
  //       <button className="btn-add-doc">
  //         <Plus size={18} />
  //         Ajouter un document
  //       </button>
  //     </div>
  //
  //     {/* Tabs */}
  //     <div className="tabs-container">
  //       {courses.map(course => (
  //         <button
  //           key={course.id}
  //           className={`tab-btn ${activeTab === course.id ? 'active' : ''}`}
  //           onClick={() => setActiveTab(course.id)}
  //         >
  //           {course.label}
  //         </button>
  //       ))}
  //     </div>

    //   {/* Content Card */}
    //   <div className="content-card">
    //     <div className="card-header-bar">
    //       <h2>Documents pour {currentCourseName}</h2>
    //       <span className="file-count">{currentResources.length} FICHIERS</span>
    //     </div>
    //
    //     <table className="resources-table">
    //       <thead>
    //         <tr>
    //           <th className="th-name">NOM DU SUPPORT</th>
    //           <th className="th-type">TYPE</th>
    //           <th className="th-date">DATE D'AJOUT</th>
    //           <th className="th-action">ACTION</th>
    //         </tr>
    //       </thead>
    //       <tbody>
    //         {currentResources.map((resource, index) => (
    //           <tr key={resource.id}>
    //             <td className="td-name">
    //               <div className={`file-icon-wrapper bg-${resource.color}`}>
    //                 <resource.icon size={22} className={`icon-${resource.color}`} />
    //               </div>
    //               <span className="file-name">{resource.name}</span>
    //             </td>
    //             <td className="td-type">
    //               <span className={`type-badge badge-${resource.color}`}>
    //                 {resource.type}
    //               </span>
    //             </td>
    //             <td className="td-date">{resource.date}</td>
    //             <td className="td-action">
    //               {/* Showing actions only on the first item to match visual screenshot, or all if preferred. Let's make them accessible on all but styled precisely */}
    //               <div className="action-buttons">
    //                 <button className="btn-icon-action btn-open">
    //                   <ExternalLink size={18} />
    //                 </button>
    //                 <button className="btn-icon-action btn-delete">
    //                   <X size={18} />
    //                 </button>
    //               </div>
    //             </td>
    //           </tr>
    //         ))}
    //       </tbody>
    //     </table>
    //   </div>
    // </div>
 // );
//};

// export default GestionCoursProfesseur;



import { useState, useEffect } from 'react';
import {
  FileText, Video, ExternalLink, X,
  Upload, ChevronDown, ChevronUp,
  Folder, Loader2
} from 'lucide-react';
import { apiEnseignementsProfesseur } from '../services/professeur/apiEnseignementsProfesseur.js';
import { apiSeancesProfesseur }       from '../services/professeur/apiSeancesProfesseur';
import './GestionCoursProfesseur.css';

// ─── Helpers ──────────────────────────────────────────────────────────────────

const TYPE_COLOR  = { COURS: 'blue', TD: 'green', TP: 'purple' };

const formatDate = (dt) => {
  if (!dt) return '—';
  return new Date(dt).toLocaleDateString('fr-FR', {
    weekday: 'short', day: '2-digit', month: 'short'
  });
};

const formatHeure = (dt) => {
  if (!dt) return '';
  return new Date(dt).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

// ─── Carte séance (fichiers chargés automatiquement) ─────────────────────────

const SeanceCard = ({ seance }) => {
  const [fichiers, setFichiers]     = useState([]);
  const [loading, setLoading]       = useState(true);
  const [ouvert, setOuvert]         = useState(false);
  const [uploading, setUploading]   = useState(false);
  const [error, setError]           = useState(null);

  // Chargement automatique dès le montage
  useEffect(() => {
    const charger = async () => {
      try {
        const data = await apiSeancesProfesseur.listerFichiers(seance.id);
        setFichiers(data || []);
      } catch {
        setFichiers([]);
      } finally {
        setLoading(false);
      }
    };
    charger();
  }, [seance.id]);

  const handleUpload = async (e) => {
    const fichier = e.target.files?.[0];
    if (!fichier) return;
    setUploading(true);
    setError(null);
    try {
      const nouveau = await apiSeancesProfesseur.deposerFichier(seance.id, fichier);
      setFichiers(prev => [...prev, nouveau]);
      setOuvert(true);
    } catch (err) {
      setError('Erreur : ' + err.message);
    } finally {
      setUploading(false);
      e.target.value = '';
    }
  };

  const handleSupprimer = async (fichierId) => {
    if (!window.confirm('Supprimer ce fichier ?')) return;
    try {
      await apiSeancesProfesseur.supprimerFichier(fichierId);
      setFichiers(prev => prev.filter(f => f.id !== fichierId));
    } catch (err) {
      setError('Erreur : ' + err.message);
    }
  };

  const handleTelecharger = async (fichierId) => {
    try {
      const { blob, filename } = await apiSeancesProfesseur.telechargerFichier(fichierId);
      const url = URL.createObjectURL(blob);
      const a   = document.createElement('a');
      a.href = url; a.download = filename; a.click();
      URL.revokeObjectURL(url);
    } catch (err) {
      setError('Erreur : ' + err.message);
    }
  };

  const couleur = TYPE_COLOR[seance.type] || 'grey';
  const nbFichiers = loading ? '…' : fichiers.length;

  return (
      <div className="seance-classroom-card">

        {/* En-tête séance */}
        <div className="seance-classroom-header">

          <div className="seance-classroom-left">
            <div className={`seance-type-strip strip-${couleur}`} />
            <div className="seance-classroom-meta">
              <div className="seance-classroom-title">
                <span className={`type-pill-inline pill-${couleur}`}>{seance.type}</span>
                <span className="seance-salle-text">
                {seance.salle ? `Salle ${seance.salle}` : 'Salle non précisée'}
              </span>
              </div>
              <div className="seance-classroom-date">
                {formatDate(seance.date || seance.heureDebut)}
                {seance.heureDebut && (
                    <span className="seance-heure-text">
                  {' · '}{formatHeure(seance.heureDebut)}
                      {seance.heureFin && ` → ${formatHeure(seance.heureFin)}`}
                </span>
                )}
              </div>
              {seance.contenu && (
                  <p className="seance-contenu-preview">{seance.contenu}</p>
              )}
            </div>
          </div>

          {/* Actions */}
          <div className="seance-classroom-actions">
            <label
                className="btn-seance-action btn-upload-seance"
                htmlFor={`upload-s-${seance.id}`}
                title="Ajouter un fichier"
            >
              {uploading
                  ? <Loader2 size={15} className="spin" />
                  : <Upload size={15} />
              }
              <span>Ajouter</span>
              <input
                  id={`upload-s-${seance.id}`}
                  type="file"
                  style={{ display: 'none' }}
                  onChange={handleUpload}
                  disabled={uploading}
              />
            </label>

            <button
                className="btn-seance-action btn-toggle-fichiers"
                onClick={() => setOuvert(v => !v)}
            >
              <Folder size={15} />
              <span>{nbFichiers} fichier{nbFichiers !== 1 ? 's' : ''}</span>
              {ouvert ? <ChevronUp size={13} /> : <ChevronDown size={13} />}
            </button>
          </div>
        </div>

        {/* Erreur */}
        {error && (
            <div className="seance-error-bar">
              <span>{error}</span>
              <button onClick={() => setError(null)}><X size={12} /></button>
            </div>
        )}

        {/* Fichiers inline — style Google Classroom */}
        {ouvert && (
            <div className="seance-fichiers-zone">
              {loading ? (
                  <div className="fichiers-loading-row">
                    <Loader2 size={15} className="spin" />
                    <span>Chargement...</span>
                  </div>
              ) : fichiers.length === 0 ? (
                  <div className="fichiers-empty-row">
                    <FileText size={24} opacity={0.25} />
                    <span>Aucun fichier pour cette séance</span>
                  </div>
              ) : (
                  <div className="fichiers-chips-grid">
                    {fichiers.map(f => {
                      const isVideo = f.typeMime?.includes('video');
                      const FIcon   = isVideo ? Video : FileText;
                      const ext     = f.typeMime?.split('/')[1]?.toUpperCase() || 'FICHIER';
                      return (
                          <div key={f.id} className="fichier-chip-card">
                            <div className="fichier-chip-icon-box">
                              <FIcon size={20} />
                            </div>
                            <div className="fichier-chip-text">
                      <span className="fichier-chip-name" title={f.nomOriginal}>
                        {f.nomOriginal}
                      </span>
                              <span className="fichier-chip-meta">
                        {ext}
                                {f.taille ? ` · ${(f.taille / 1024).toFixed(0)} Ko` : ''}
                      </span>
                            </div>
                            <div className="fichier-chip-btns">
                              <button
                                  className="fichier-btn fichier-btn-dl"
                                  title="Télécharger"
                                  onClick={() => handleTelecharger(f.id)}
                              >
                                <ExternalLink size={14} />
                              </button>
                              <button
                                  className="fichier-btn fichier-btn-del"
                                  title="Supprimer"
                                  onClick={() => handleSupprimer(f.id)}
                              >
                                <X size={14} />
                              </button>
                            </div>
                          </div>
                      );
                    })}
                  </div>
              )}
            </div>
        )}
      </div>
  );
};

// ─── Page principale ──────────────────────────────────────────────────────────

const GestionCoursProfesseur = () => {
  const [enseignements, setEnseignements]   = useState([]);
  const [activeTab, setActiveTab]           = useState(null);
  const [seances, setSeances]               = useState([]);
  const [loading, setLoading]               = useState(true);
  const [loadingSeances, setLoadingSeances] = useState(false);
  const [error, setError]                   = useState(null);

  useEffect(() => {
    const load = async () => {
      setLoading(true);
      try {
        const data = await apiEnseignementsProfesseur.listerEnseignements();
        setEnseignements(data || []);
        if (data?.length > 0) setActiveTab(data[0]);
      } catch (err) {
        setError('Impossible de charger les cours : ' + err.message);
      } finally {
        setLoading(false);
      }
    };
    load();
  }, []);

  useEffect(() => {
    if (!activeTab) return;
    const load = async () => {
      setLoadingSeances(true);
      setSeances([]);
      try {
        const data = await apiSeancesProfesseur.rechercherSeances({
          codeClasse:  activeTab.codeClasse,
          codeMatiere: activeTab.codeMatiere,
        });
        const sorted = (data || []).sort((a, b) =>
            new Date(b.date || b.heureDebut || 0) - new Date(a.date || a.heureDebut || 0)
        );
        setSeances(sorted);
      } catch (err) {
        setError('Erreur séances : ' + err.message);
      } finally {
        setLoadingSeances(false);
      }
    };
    load();
  }, [activeTab]);

  if (loading) return (
      <div className="gestion-cours-container">
        <div style={{ display: 'flex', alignItems: 'center', gap: 12, padding: '3rem', color: '#94a3b8' }}>
          <Loader2 size={24} className="spin" /> Chargement des cours...
        </div>
      </div>
  );

  return (
      <div className="gestion-cours-container">

        <div className="page-header">
          <div className="header-text">
            <h1>Gestion des Cours</h1>
            <p>Gérez vos supports pédagogiques, TP et TD.</p>
          </div>
        </div>

        {error && (
            <div style={{ background: '#fee2e2', color: '#b91c1c', padding: '10px 16px', borderRadius: 8, marginBottom: 16, display: 'flex', justifyContent: 'space-between' }}>
              <span>{error}</span>
              <button onClick={() => setError(null)}><X size={14} /></button>
            </div>
        )}

        {enseignements.length === 0 ? (
            <div style={{ textAlign: 'center', padding: '4rem', color: '#94a3b8' }}>
              <Folder size={48} style={{ opacity: 0.2, margin: '0 auto 16px', display: 'block' }} />
              <p>Aucun cours affecté pour le moment.</p>
            </div>
        ) : (
            <>
              {/* Onglets enseignements */}
              <div className="tabs-container">
                {enseignements.map(e => {
                  const key = `${e.codeClasse}-${e.codeMatiere}`;
                  const isActive = activeTab?.codeClasse === e.codeClasse
                      && activeTab?.codeMatiere === e.codeMatiere;
                  return (
                      <button
                          key={key}
                          className={`tab-btn ${isActive ? 'active' : ''}`}
                          onClick={() => setActiveTab(e)}
                      >
                        {e.codeClasse} — {e.nomMatiere || e.codeMatiere}
                      </button>
                  );
                })}
              </div>

              {/* En-tête section */}
              <div className="content-card" style={{ marginBottom: 0, borderBottom: 'none', borderRadius: '12px 12px 0 0' }}>
                <div className="card-header-bar">
                  <h2>Séances — {activeTab?.nomClasse || activeTab?.codeClasse}
                    {activeTab?.nomMatiere && <span style={{ color: '#64748b', fontWeight: 400, fontSize: 15 }}> · {activeTab.nomMatiere}</span>}
                  </h2>
                  <span className="file-count">
                {loadingSeances ? '...' : `${seances.length} SÉANCES`}
              </span>
                </div>
              </div>

              {/* Liste des séances */}
              {loadingSeances ? (
                  <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '2rem', color: '#94a3b8', background: 'white', borderRadius: '0 0 12px 12px', border: '1px solid #e2e8f0', borderTop: 'none' }}>
                    <Loader2 size={20} className="spin" /> Chargement des séances...
                  </div>
              ) : seances.length === 0 ? (
                  <div style={{ padding: '3rem', textAlign: 'center', color: '#94a3b8', background: 'white', borderRadius: '0 0 12px 12px', border: '1px solid #e2e8f0', borderTop: 'none' }}>
                    <FileText size={36} style={{ opacity: 0.2, margin: '0 auto 12px', display: 'block' }} />
                    <p>Aucune séance trouvée pour ce cours.</p>
                  </div>
              ) : (
                  <div className="seances-classroom-list">
                    {seances.map(seance => (
                        <SeanceCard key={seance.id} seance={seance} />
                    ))}
                  </div>
              )}
            </>
        )}
      </div>
  );
};

export default GestionCoursProfesseur;