import { useState, useEffect } from 'react';
import {
    ClipboardList, Plus, Pencil, Trash2,
    UploadCloud, Download, ChevronDown,
    CheckCircle2, Clock, AlertTriangle, X, Loader2
} from 'lucide-react';
import { apiExamensProfesseur } from '../services/professeur/apiExamensProfesseur';
import './GestionExamensProfesseur.css';

// ─── Constantes ───────────────────────────────────────────────────────────────

const SEMESTRES    = ['S1','S2','S3','S4','S5','S6','S7','S8','S9'];
const TYPES_EXAMEN = ['NORMAL', 'RATTRAPAGE'];
const ETATS_EXAMEN = ['PROGRAMME', 'PASSE', 'CORRIGE'];

const ETAT_CONFIG = {
    PROGRAMME: { label: 'Programmé', color: '#3b82f6', bg: '#eff6ff', icon: Clock },
    PASSE:     { label: 'Passé',     color: '#f59e0b', bg: '#fffbeb', icon: AlertTriangle },
    CORRIGE:   { label: 'Corrigé',   color: '#10b981', bg: '#f0fdf4', icon: CheckCircle2 },
};

const formVide = {
    semestre: 'S1', dateExamen: '', numeroExamen: 1,
    typeExamen: 'NORMAL', duree: 90,
};

const formModifVide = { dateExamen: '', duree: 90, etat: 'PROGRAMME' };

const formatDate = (d) => {
    if (!d) return '—';
    return new Date(d).toLocaleDateString('fr-FR', { day: '2-digit', month: 'short', year: 'numeric' });
};
const formatHeure = (d) => {
    if (!d) return '';
    return new Date(d).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

// ─── Composant principal ──────────────────────────────────────────────────────

const GestionExamensProfesseur = () => {

    const [filtres, setFiltres]               = useState({ classes: [] });
    const [selectedClasse, setSelectedClasse] = useState(null);
    const [selectedMatiere, setSelectedMatiere] = useState(null);
    const [loadingFiltres, setLoadingFiltres] = useState(true);

    const [examens, setExamens]               = useState([]);
    const [loadingExamens, setLoadingExamens] = useState(false);

    const [showModalCreer, setShowModalCreer] = useState(false);
    const [showModalModif, setShowModalModif] = useState(false);
    const [examenEnCours, setExamenEnCours]   = useState(null);

    const [formCreer, setFormCreer] = useState(formVide);
    const [formModif, setFormModif] = useState(formModifVide);

    const [loading, setLoading]         = useState(false);
    const [error, setError]             = useState(null);
    const [success, setSuccess]         = useState(null);
    const [uploadingId, setUploadingId] = useState(null);

    // ─── Chargement des filtres ─────────────────────────────────────────────
    useEffect(() => {
        const load = async () => {
            try {
                const data = await apiExamensProfesseur.obtenirFiltres();
                setFiltres(data || { classes: [] });
                if (data?.classes?.length > 0) {
                    const c = data.classes[0];
                    setSelectedClasse(c);
                    setSelectedMatiere(c.matieres?.[0] || null);
                }
            } catch (err) {
                setError('Impossible de charger les cours : ' + err.message);
            } finally {
                setLoadingFiltres(false);
            }
        };
        load();
    }, []);

    // ─── Chargement des examens quand classe/matière change ─────────────────
    useEffect(() => {
        if (!selectedClasse || !selectedMatiere) return;
        let cancelled = false;
        const load = async () => {
            setLoadingExamens(true);
            setExamens([]);
            try {
                const data = await apiExamensProfesseur.listerExamens(
                    selectedClasse.codeClasse,
                    selectedMatiere.codeMatiere
                );
                if (!cancelled) setExamens(data || []);
            } catch {
                // Silencieux — aucun examen n'est une situation normale
                if (!cancelled) setExamens([]);
            } finally {
                if (!cancelled) setLoadingExamens(false);
            }
        };
        load();
        return () => { cancelled = true; };
    }, [selectedClasse, selectedMatiere]);

    // ─── Notification auto-disparaissante ──────────────────────────────────
    useEffect(() => {
        if (success) {
            const t = setTimeout(() => setSuccess(null), 3000);
            return () => clearTimeout(t);
        }
    }, [success]);

    const handleClasseChange = (codeClasse) => {
        const cl = filtres.classes.find(c => c.codeClasse === codeClasse);
        setSelectedClasse(cl || null);
        setSelectedMatiere(cl?.matieres?.[0] || null);
    };

    // ─── Créer ─────────────────────────────────────────────────────────────
    const handleCreer = async () => {
        if (!selectedClasse || !selectedMatiere) return;
        if (!formCreer.dateExamen) { setError('La date est obligatoire.'); return; }
        setLoading(true); setError(null);
        try {
            const nouvel = await apiExamensProfesseur.creerExamen(
                selectedClasse.codeClasse,
                selectedMatiere.codeMatiere,
                {
                    semestre:     formCreer.semestre,
                    dateExamen:   new Date(formCreer.dateExamen).toISOString(),
                    numeroExamen: parseInt(formCreer.numeroExamen),
                    typeExamen:   formCreer.typeExamen,
                    duree:        parseInt(formCreer.duree),
                }
            );
            setExamens(prev => [nouvel, ...prev]);
            setShowModalCreer(false);
            setFormCreer(formVide);
            setSuccess('Examen créé avec succès !');
        } catch (err) {
            setError('Erreur création : ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // ─── Modifier ──────────────────────────────────────────────────────────
    const ouvrirModif = (examen) => {
        setExamenEnCours(examen);
        setFormModif({
            dateExamen: examen.dateExamen
                ? new Date(examen.dateExamen).toISOString().slice(0, 16) : '',
            duree: examen.duree || 90,
            etat:  examen.etat  || 'PROGRAMME',
        });
        setShowModalModif(true);
    };

    const handleModifier = async () => {
        if (!examenEnCours) return;
        setLoading(true); setError(null);
        try {
            const updated = await apiExamensProfesseur.modifierExamen(examenEnCours.idExamen, {
                dateExamen: formModif.dateExamen
                    ? new Date(formModif.dateExamen).toISOString() : undefined,
                duree: parseInt(formModif.duree),
                etat:  formModif.etat,
            });
            setExamens(prev => prev.map(e => e.idExamen === updated.idExamen ? updated : e));
            setShowModalModif(false);
            setExamenEnCours(null);
            setSuccess('Examen modifié avec succès !');
        } catch (err) {
            setError('Erreur modification : ' + err.message);
        } finally {
            setLoading(false);
        }
    };

    // ─── Supprimer ─────────────────────────────────────────────────────────
    const handleSupprimer = async (examen) => {
        if (!window.confirm(`Supprimer l'examen n°${examen.numeroExamen} ?`)) return;
        try {
            await apiExamensProfesseur.supprimerExamen(examen.idExamen);
            setExamens(prev => prev.filter(e => e.idExamen !== examen.idExamen));
            setSuccess('Examen supprimé.');
        } catch (err) {
            setError('Erreur suppression : ' + err.message);
        }
    };

    // ─── Upload corrigé ────────────────────────────────────────────────────
    const handleUploadCorrige = async (e, examenId) => {
        const fichier = e.target.files?.[0];
        if (!fichier) return;
        setUploadingId(examenId);
        try {
            const updated = await apiExamensProfesseur.deposerCorrige(examenId, fichier);
            setExamens(prev => prev.map(e => e.idExamen === examenId ? updated : e));
            setSuccess('Corrigé déposé avec succès !');
        } catch (err) {
            setError('Erreur upload : ' + err.message);
        } finally {
            setUploadingId(null);
            e.target.value = '';
        }
    };

    const handleTelechargerCorrige = async (examenId) => {
        try {
            const { blob, filename } = await apiExamensProfesseur.telechargerCorrige(examenId);
            const url = URL.createObjectURL(blob);
            const a   = document.createElement('a');
            a.href = url; a.download = filename; a.click();
            URL.revokeObjectURL(url);
        } catch (err) {
            setError('Erreur téléchargement : ' + err.message);
        }
    };

    const matieresDisponibles = selectedClasse?.matieres || [];

    // ─────────────────────────────────────────────────────────────────────────
    return (
        <div className="gestion-examens-container">

            {/* ── Header ──────────────────────────────────────────────────── */}
            <div className="examens-header">
                <div className="header-left">
                    <div className="header-icon-box">
                        <ClipboardList size={28} color="white" />
                    </div>
                    <div>
                        <h1>Gestion des Examens</h1>
                        <p>Créez et gérez vos examens par classe et matière</p>
                    </div>
                </div>
                <button
                    className="btn-creer-examen"
                    onClick={() => setShowModalCreer(true)}
                    disabled={!selectedClasse || !selectedMatiere}
                >
                    <Plus size={18} /> Nouvel Examen
                </button>
            </div>

            {/* ── Notifications ───────────────────────────────────────────── */}
            {error && (
                <div className="notif-error">
                    <AlertTriangle size={16} /><span>{error}</span>
                    <button onClick={() => setError(null)}><X size={14} /></button>
                </div>
            )}
            {success && (
                <div className="notif-success">
                    <CheckCircle2 size={16} /><span>{success}</span>
                </div>
            )}

            {/* ── Filtres ─────────────────────────────────────────────────── */}
            <div className="filtres-bar">
                <div className="filtre-group">
                    <label>Classe</label>
                    <div className="select-pill">
                        <select
                            value={selectedClasse?.codeClasse || ''}
                            onChange={e => handleClasseChange(e.target.value)}
                            disabled={loadingFiltres}
                        >
                            {loadingFiltres
                                ? <option>Chargement...</option>
                                : filtres.classes.map(c => (
                                    <option key={c.codeClasse} value={c.codeClasse}>
                                        {c.nomClasse || c.codeClasse}
                                    </option>
                                ))
                            }
                        </select>
                        <ChevronDown size={16} />
                    </div>
                </div>

                <div className="filtre-group">
                    <label>Matière</label>
                    <div className="select-pill">
                        <select
                            value={selectedMatiere?.codeMatiere || ''}
                            onChange={e => {
                                const m = matieresDisponibles.find(x => x.codeMatiere === e.target.value);
                                setSelectedMatiere(m || null);
                            }}
                            disabled={!selectedClasse}
                        >
                            {matieresDisponibles.map(m => (
                                <option key={m.codeMatiere} value={m.codeMatiere}>
                                    {m.nomMatiere || m.codeMatiere}
                                </option>
                            ))}
                        </select>
                        <ChevronDown size={16} />
                    </div>
                </div>

                {selectedClasse && selectedMatiere && (
                    <div className="filtre-info">
                        📋 {selectedClasse.nomClasse || selectedClasse.codeClasse} — {selectedMatiere.nomMatiere || selectedMatiere.codeMatiere}
                    </div>
                )}
            </div>

            {/* ── Tableau des examens ──────────────────────────────────────── */}
            <div className="examens-table-card">
                <div className="table-header-row">
                    <h2>Examens</h2>
                    <span className="badge-count">
                        {loadingExamens
                            ? <Loader2 size={14} style={{ animation: 'spin 0.8s linear infinite' }} />
                            : `${examens.length} EXAMEN${examens.length !== 1 ? 'S' : ''}`
                        }
                    </span>
                </div>

                {loadingExamens ? (
                    <div style={{ display: 'flex', alignItems: 'center', gap: 10, padding: '2.5rem', color: '#94a3b8', justifyContent: 'center' }}>
                        <Loader2 size={22} style={{ animation: 'spin 0.8s linear infinite' }} />
                        Chargement des examens...
                    </div>
                ) : examens.length === 0 ? (
                    <div className="empty-state-examens">
                        <ClipboardList size={48} opacity={0.2} />
                        <p>Aucun examen pour cette sélection.</p>
                        <p style={{ fontSize: '13px', color: '#94a3b8' }}>
                            Cliquez sur "Nouvel Examen" pour créer le premier.
                        </p>
                    </div>
                ) : (
                    <table className="examens-table">
                        <thead>
                        <tr>
                            <th>MATIÈRE</th><th>N°</th><th>TYPE</th><th>SEMESTRE</th>
                            <th>DATE</th><th>DURÉE</th><th>ÉTAT</th><th>CORRIGÉ</th><th>ACTIONS</th>
                        </tr>
                        </thead>
                        <tbody>
                        {examens.map(examen => {
                            const etatCfg = ETAT_CONFIG[examen.etat] || ETAT_CONFIG.PROGRAMME;
                            const EtatIcon = etatCfg.icon;
                            return (
                                <tr key={examen.idExamen}>
                                    <td className="td-matiere">
                                        <span className="matiere-label">{examen.nomMatiere || examen.codeMatiere}</span>
                                    </td>
                                    <td className="td-center">
                                        <span className="num-badge">#{examen.numeroExamen}</span>
                                    </td>
                                    <td>
                                        <span className={`type-pill type-${examen.typeExamen?.toLowerCase()}`}>
                                            {examen.typeExamen === 'RATTRAPAGE' ? 'Rattrapage' : 'Normal'}
                                        </span>
                                    </td>
                                    <td className="td-center">{examen.semestre}</td>
                                    <td>
                                        <div className="date-cell">
                                            <span>{formatDate(examen.dateExamen)}</span>
                                            {examen.dateExamen && (
                                                <span className="heure-cell">{formatHeure(examen.dateExamen)}</span>
                                            )}
                                        </div>
                                    </td>
                                    <td className="td-center">{examen.duree} min</td>
                                    <td>
                                        <span className="etat-pill" style={{ color: etatCfg.color, background: etatCfg.bg }}>
                                            <EtatIcon size={13} />{etatCfg.label}
                                        </span>
                                    </td>
                                    <td className="td-center">
                                        {examen.corrigeDisponible ? (
                                            <button className="btn-dl-corrige" onClick={() => handleTelechargerCorrige(examen.idExamen)}>
                                                <Download size={16} /> PDF
                                            </button>
                                        ) : (
                                            <label className="btn-upload-corrige" htmlFor={`upload-${examen.idExamen}`}>
                                                {uploadingId === examen.idExamen
                                                    ? 'Upload...'
                                                    : <><UploadCloud size={16} /> Déposer</>
                                                }
                                                <input
                                                    id={`upload-${examen.idExamen}`}
                                                    type="file" accept="application/pdf"
                                                    style={{ display: 'none' }}
                                                    onChange={e => handleUploadCorrige(e, examen.idExamen)}
                                                    disabled={uploadingId === examen.idExamen}
                                                />
                                            </label>
                                        )}
                                    </td>
                                    <td>
                                        <div className="actions-cell">
                                            <button className="btn-icon-table btn-edit" onClick={() => ouvrirModif(examen)}>
                                                <Pencil size={16} />
                                            </button>
                                            <button className="btn-icon-table btn-del" onClick={() => handleSupprimer(examen)}>
                                                <Trash2 size={16} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            );
                        })}
                        </tbody>
                    </table>
                )}
            </div>

            {/* ══ MODAL — Créer ═══════════════════════════════════════════════ */}
            {showModalCreer && (
                <div className="modal-overlay" onClick={() => setShowModalCreer(false)}>
                    <div className="modal-box" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2><Plus size={20} /> Nouvel Examen</h2>
                            <button className="btn-close-modal" onClick={() => setShowModalCreer(false)}><X size={20} /></button>
                        </div>

                        <div className="modal-context-info">
                            <span>📚 {selectedClasse?.nomClasse || selectedClasse?.codeClasse}</span>
                            <span>·</span>
                            <span>{selectedMatiere?.nomMatiere || selectedMatiere?.codeMatiere}</span>
                        </div>

                        {error && (
                            <div className="notif-error" style={{ margin: '0 0 12px' }}>
                                <span>{error}</span>
                                <button onClick={() => setError(null)}><X size={14} /></button>
                            </div>
                        )}

                        <div className="modal-form">
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Semestre *</label>
                                    <select value={formCreer.semestre} onChange={e => setFormCreer(f => ({ ...f, semestre: e.target.value }))}>
                                        {SEMESTRES.map(s => <option key={s} value={s}>{s}</option>)}
                                    </select>
                                </div>
                                <div className="form-group">
                                    <label>Type *</label>
                                    <select value={formCreer.typeExamen} onChange={e => setFormCreer(f => ({ ...f, typeExamen: e.target.value }))}>
                                        {TYPES_EXAMEN.map(t => (
                                            <option key={t} value={t}>{t === 'NORMAL' ? 'Normal' : 'Rattrapage'}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>

                            <div className="form-row">
                                <div className="form-group">
                                    <label>Numéro d'examen *</label>
                                    <input type="number" min="1" value={formCreer.numeroExamen}
                                           onChange={e => setFormCreer(f => ({ ...f, numeroExamen: e.target.value }))} />
                                </div>
                                <div className="form-group">
                                    <label>Durée (minutes) *</label>
                                    <input type="number" min="15" step="15" value={formCreer.duree}
                                           onChange={e => setFormCreer(f => ({ ...f, duree: e.target.value }))} />
                                </div>
                            </div>

                            <div className="form-group">
                                <label>Date & heure *</label>
                                <input type="datetime-local" value={formCreer.dateExamen}
                                       onChange={e => setFormCreer(f => ({ ...f, dateExamen: e.target.value }))} />
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button className="btn-annuler" onClick={() => setShowModalCreer(false)}>Annuler</button>
                            <button className="btn-confirmer" onClick={handleCreer} disabled={loading}>
                                {loading ? 'Création...' : "Créer l'examen"}
                            </button>
                        </div>
                    </div>
                </div>
            )}

            {/* ══ MODAL — Modifier ════════════════════════════════════════════ */}
            {showModalModif && examenEnCours && (
                <div className="modal-overlay" onClick={() => setShowModalModif(false)}>
                    <div className="modal-box" onClick={e => e.stopPropagation()}>
                        <div className="modal-header">
                            <h2><Pencil size={20} /> Modifier l'Examen</h2>
                            <button className="btn-close-modal" onClick={() => setShowModalModif(false)}><X size={20} /></button>
                        </div>

                        <div className="modal-context-info">
                            <span>📋 {examenEnCours.nomMatiere}</span>
                            <span>·</span>
                            <span>Examen n°{examenEnCours.numeroExamen}</span>
                            <span>·</span>
                            <span>{examenEnCours.typeExamen === 'RATTRAPAGE' ? 'Rattrapage' : 'Normal'}</span>
                        </div>

                        {error && (
                            <div className="notif-error" style={{ margin: '0 0 12px' }}>
                                <span>{error}</span>
                                <button onClick={() => setError(null)}><X size={14} /></button>
                            </div>
                        )}

                        <div className="modal-form">
                            <div className="form-group">
                                <label>Date & heure</label>
                                <input type="datetime-local" value={formModif.dateExamen}
                                       onChange={e => setFormModif(f => ({ ...f, dateExamen: e.target.value }))} />
                            </div>
                            <div className="form-row">
                                <div className="form-group">
                                    <label>Durée (minutes)</label>
                                    <input type="number" min="15" step="15" value={formModif.duree}
                                           onChange={e => setFormModif(f => ({ ...f, duree: e.target.value }))} />
                                </div>
                                <div className="form-group">
                                    <label>État</label>
                                    <select value={formModif.etat} onChange={e => setFormModif(f => ({ ...f, etat: e.target.value }))}>
                                        {ETATS_EXAMEN.map(et => (
                                            <option key={et} value={et}>{ETAT_CONFIG[et]?.label || et}</option>
                                        ))}
                                    </select>
                                </div>
                            </div>
                        </div>

                        <div className="modal-footer">
                            <button className="btn-annuler" onClick={() => setShowModalModif(false)}>Annuler</button>
                            <button className="btn-confirmer" onClick={handleModifier} disabled={loading}>
                                {loading ? 'Enregistrement...' : 'Enregistrer'}
                            </button>
                        </div>
                    </div>
                </div>
            )}
        </div>
    );
};

export default GestionExamensProfesseur;