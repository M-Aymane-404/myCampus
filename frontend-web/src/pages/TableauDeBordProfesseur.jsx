import { useState, useEffect } from 'react';
import {
  BookOpen, ClipboardList, GraduationCap, Megaphone,
  UserCheck, Clock, MapPin, ChevronRight, AlertCircle,
  CheckCircle2, Calendar, Loader2, TrendingUp, Bell
} from 'lucide-react';
import { apiTableauDeBordProfesseur } from '../services/professeur/apiTableauDeBordProfesseur';
import { apiAnnoncesProfesseur }       from '../services/professeur/apiAnnoncesProfesseur';
import './TableauDeBordProfesseur.css';

// ─── Helpers ──────────────────────────────────────────────────────────────────

const TYPE_COLOR = { COURS: 'blue', TD: 'green', TP: 'purple' };

const ETAT_CONFIG = {
  PLANIFIEE: { label: 'Planifiée', color: '#3b82f6', bg: '#eff6ff' },
  TERMINEE:  { label: 'Terminée',  color: '#10b981', bg: '#f0fdf4' },
  ANNULEE:   { label: 'Annulée',   color: '#ef4444', bg: '#fee2e2' },
};

const ETAT_EXAM_CONFIG = {
  PROGRAMME: { label: 'Programmé', color: '#3b82f6', bg: '#eff6ff', icon: Clock },
  PASSE:     { label: 'Passé',     color: '#f59e0b', bg: '#fffbeb', icon: AlertCircle },
  CORRIGE:   { label: 'Corrigé',   color: '#10b981', bg: '#f0fdf4', icon: CheckCircle2 },
};

const formatDate = (d) => {
  if (!d) return '—';
  return new Date(d).toLocaleDateString('fr-FR', { weekday: 'short', day: '2-digit', month: 'short' });
};

const formatHeure = (d) => {
  if (!d) return '';
  return new Date(d).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' });
};

const tempsDepuis = (dateStr) => {
  if (!dateStr) return '';
  const diff = Date.now() - new Date(dateStr).getTime();
  const h = Math.floor(diff / 3_600_000);
  const d = Math.floor(h / 24);
  if (d >= 1) return `IL Y A ${d}J`;
  if (h >= 1) return `IL Y A ${h}H`;
  return 'RÉCENT';
};

// ─── Composant principal ──────────────────────────────────────────────────────

const TableauDeBordProfesseur = ({ surNavigation }) => {
  const [data, setData]               = useState(null);
  const [prochaineSeance, setProchaineSeance] = useState(null);
  const [annonces, setAnnonces]       = useState([]);
  const [loading, setLoading]         = useState(true);
  const [error, setError]             = useState(null);

  useEffect(() => {
    const fetchAll = async () => {
      try {
        setLoading(true);

        // Dashboard principal
        const dashboard = await apiTableauDeBordProfesseur.obtenirTableauDeBord();
        setData(dashboard);

        // Prochaine séance (emploi du temps)
        try {
          const ps = await apiTableauDeBordProfesseur.obtenirProchaineSeance();
          setProchaineSeance(ps?.prochaineSeance || null);
        } catch {
          setProchaineSeance(null);
        }

        // Annonces reçues (mes classes + dept)
        try {
          const ann = await apiAnnoncesProfesseur.listerAnnoncesMesClasses();
          setAnnonces(Array.isArray(ann) ? ann.slice(0, 3) : []);
        } catch {
          setAnnonces([]);
        }

      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchAll();
  }, []);

  // ── État de chargement ────────────────────────────────────────────────────
  if (loading) return (
      <div className="tdb-prof-container">
        <div className="tdb-loading">
          <Loader2 size={32} className="tdb-spin" />
          <p>Chargement du tableau de bord...</p>
        </div>
      </div>
  );

  if (error) return (
      <div className="tdb-prof-container">
        <div className="tdb-error">
          <AlertCircle size={28} />
          <p>Erreur : {error}</p>
        </div>
      </div>
  );

  const { entete, stats, seancesRecentes, examensAVenir } = data || {};

  const statCards = [
    {
      label: 'ENSEIGNEMENTS',
      value: stats?.nbEnseignements ?? '—',
      icon: BookOpen,
      colorClass: 'bg-blue',
      iconClass:  'icon-blue',
      action: () => surNavigation?.('cours'),
    },
    {
      label: 'SÉANCES TOTALES',
      value: stats?.nbSeancesTotales ?? '—',
      sub: `${stats?.nbSeancesTerminees ?? 0} terminées`,
      icon: UserCheck,
      colorClass: 'bg-green',
      iconClass:  'icon-green',
      action: () => surNavigation?.('cours'),
    },
    {
      label: 'EXAMENS PROGRAMMÉS',
      value: stats?.nbExamensProgrammes ?? '—',
      sub: `${stats?.nbExamensTotal ?? 0} au total`,
      icon: ClipboardList,
      colorClass: 'bg-purple',
      iconClass:  'icon-purple',
      action: () => surNavigation?.('examens'),
    },
    {
      label: 'ANNONCES PUBLIÉES',
      value: stats?.nbAnnoncesPubiiees ?? '—',
      icon: Megaphone,
      colorClass: 'bg-orange',
      iconClass:  'icon-orange',
      action: () => surNavigation?.('annonces'),
    },
  ];

  return (
      <div className="tdb-prof-container">

        {/* ── Header ──────────────────────────────────────────────────────── */}
        <div className="tdb-header">
          <div className="tdb-header-content">
            <div className="tdb-header-info">
              <h1>Espace Professeur</h1>
              <p>
                {entete?.grade && <span className="tdb-header-grade">{entete.grade}</span>}
                {entete?.specialite && <> · {entete.specialite}</>}
                {entete?.departement && <> · {entete.departement}</>}
              </p>
            </div>
            <div className="tdb-session-badge">
              <div className="tdb-status-dot" />
              <span>{entete?.statut || 'ACTIF'}</span>
            </div>
          </div>
        </div>

        {/* ── Stat Cards ──────────────────────────────────────────────────── */}
        <div className="tdb-stats-row">
          {statCards.map((card, i) => {
            const Icon = card.icon;
            return (
                <div key={i} className="tdb-stat-card" onClick={card.action} style={{ cursor: 'pointer' }}>
                  <div className="tdb-stat-info">
                    <span className="tdb-stat-label">{card.label}</span>
                    <span className="tdb-stat-number">{card.value}</span>
                    {card.sub && <span className="tdb-stat-sub">{card.sub}</span>}
                  </div>
                  <div className={`tdb-stat-icon-box ${card.colorClass}`}>
                    <Icon size={24} className={card.iconClass} />
                  </div>
                </div>
            );
          })}
        </div>

        {/* ── Grille principale ───────────────────────────────────────────── */}
        <div className="tdb-main-grid">

          {/* ── Colonne gauche ──────────────────────────────────────────── */}
          <div className="tdb-left-col">

            {/* Prochaine séance (emploi du temps) */}
            <div className="tdb-next-seance-card">
              <div className="tdb-card-header">
                <div className="tdb-section-label">
                  <Clock className="tdb-label-icon" size={18} />
                  <h3>Prochaine Séance</h3>
                </div>
              </div>

              {prochaineSeance ? (
                  <div className="tdb-next-seance-content">
                    <div className="tdb-next-seance-type">
                  <span className={`tdb-type-pill pill-${TYPE_COLOR[prochaineSeance.type] || 'grey'}`}>
                    {prochaineSeance.type || 'COURS'}
                  </span>
                      <span className="tdb-next-seance-name">
                    {prochaineSeance.nomMatiere || prochaineSeance.matiere || 'Matière non précisée'}
                  </span>
                    </div>
                    <div className="tdb-next-seance-details">
                      <div className="tdb-detail-row">
                        <Calendar size={14} />
                        <span>{prochaineSeance.dayOfWeek || formatDate(prochaineSeance.date)}</span>
                      </div>
                      <div className="tdb-detail-row">
                        <Clock size={14} />
                        <span>
                      {prochaineSeance.start || formatHeure(prochaineSeance.heureDebut)}
                          {(prochaineSeance.end || prochaineSeance.heureFin) &&
                              ` → ${prochaineSeance.end || formatHeure(prochaineSeance.heureFin)}`}
                    </span>
                      </div>
                      {(prochaineSeance.salle || prochaineSeance.room) && (
                          <div className="tdb-detail-row">
                            <MapPin size={14} />
                            <span>Salle {prochaineSeance.salle || prochaineSeance.room}</span>
                          </div>
                      )}
                      {prochaineSeance.classe && (
                          <div className="tdb-detail-row">
                            <BookOpen size={14} />
                            <span>{prochaineSeance.classe}</span>
                          </div>
                      )}
                    </div>
                  </div>
              ) : (
                  <div className="tdb-empty-small">
                    <Clock size={28} opacity={0.2} />
                    <p>Aucune séance programmée dans l'emploi du temps.</p>
                  </div>
              )}
            </div>

            {/* Annonces reçues */}
            <div className="tdb-annonces-card">
              <div className="tdb-card-header">
                <div className="tdb-section-label">
                  <Bell className="tdb-label-icon" size={18} />
                  <h3>Annonces Reçues</h3>
                </div>
                <span className="tdb-badge">
                {annonces.length > 0 ? `${annonces.length} NOUVELLES` : '0'}
              </span>
              </div>

              {annonces.length > 0 ? (
                  <div className="tdb-annonces-list">
                    {annonces.map(ann => (
                        <div
                            key={ann.id}
                            className="tdb-annonce-row"
                            onClick={() => surNavigation?.('annonces')}
                        >
                          <div className="tdb-annonce-indicator" />
                          <div className="tdb-annonce-body">
                            <p className="tdb-annonce-titre">{ann.titre}</p>
                            <div className="tdb-annonce-meta">
                              <span className="tdb-annonce-cat">{ann.typeAnnonce || 'GÉNÉRAL'}</span>
                              <span className="tdb-annonce-time">
                          {tempsDepuis(ann.datePublication)}
                        </span>
                            </div>
                          </div>
                          <ChevronRight size={16} className="tdb-annonce-arrow" />
                        </div>
                    ))}
                    <button className="tdb-voir-tout" onClick={() => surNavigation?.('annonces')}>
                      Voir toutes les annonces <ChevronRight size={14} />
                    </button>
                  </div>
              ) : (
                  <div className="tdb-empty-small">
                    <Bell size={28} opacity={0.2} />
                    <p>Aucune annonce disponible.</p>
                  </div>
              )}
            </div>
          </div>

          {/* ── Colonne droite ───────────────────────────────────────────── */}
          <div className="tdb-right-col">

            {/* Séances récentes */}
            <div className="tdb-seances-card">
              <div className="tdb-card-header">
                <div className="tdb-section-label">
                  <UserCheck className="tdb-label-icon" size={18} />
                  <h3>Séances Récentes</h3>
                </div>
                <button className="tdb-link-btn" onClick={() => surNavigation?.('cours')}>
                  Voir tout <ChevronRight size={14} />
                </button>
              </div>

              {seancesRecentes?.length > 0 ? (
                  <div className="tdb-seances-list">
                    {seancesRecentes.map(s => {
                      const couleur   = TYPE_COLOR[s.type] || 'grey';
                      const etatCfg   = ETAT_CONFIG[s.etat] || {};
                      return (
                          <div key={s.id} className="tdb-seance-row">
                            <div className={`tdb-seance-strip strip-${couleur}`} />
                            <div className="tdb-seance-body">
                              <div className="tdb-seance-top">
                                <span className={`tdb-type-pill-sm pill-${couleur}`}>{s.type}</span>
                                <span className="tdb-seance-matiere">{s.nomMatiere}</span>
                                <span className="tdb-seance-classe">{s.codeClasse}</span>
                              </div>
                              <div className="tdb-seance-bot">
                          <span className="tdb-seance-date">
                            <Calendar size={12} /> {formatDate(s.date)}
                          </span>
                                {s.heureDebut && (
                                    <span className="tdb-seance-heure">
                              <Clock size={12} /> {formatHeure(s.heureDebut)}
                                      {s.heureFin && ` → ${formatHeure(s.heureFin)}`}
                            </span>
                                )}
                                {s.salle && (
                                    <span className="tdb-seance-salle">
                              <MapPin size={12} /> {s.salle}
                            </span>
                                )}
                              </div>
                            </div>
                            {s.etat && (
                                <span
                                    className="tdb-etat-pill"
                                    style={{ color: etatCfg.color, background: etatCfg.bg }}
                                >
                          {etatCfg.label || s.etat}
                        </span>
                            )}
                          </div>
                      );
                    })}
                  </div>
              ) : (
                  <div className="tdb-empty-small">
                    <UserCheck size={28} opacity={0.2} />
                    <p>Aucune séance enregistrée.</p>
                  </div>
              )}
            </div>

            {/* Examens à venir */}
            <div className="tdb-examens-card">
              <div className="tdb-card-header">
                <div className="tdb-section-label">
                  <ClipboardList className="tdb-label-icon" size={18} />
                  <h3>Examens à Venir</h3>
                </div>
                <button className="tdb-link-btn" onClick={() => surNavigation?.('examens')}>
                  Gérer <ChevronRight size={14} />
                </button>
              </div>

              {examensAVenir?.length > 0 ? (
                  <div className="tdb-examens-list">
                    {examensAVenir.map(ex => {
                      const cfg     = ETAT_EXAM_CONFIG[ex.etat] || ETAT_EXAM_CONFIG.PROGRAMME;
                      const ExIcon  = cfg.icon;
                      return (
                          <div key={ex.id} className="tdb-examen-row">
                            <div className="tdb-examen-icon-box" style={{ background: cfg.bg, color: cfg.color }}>
                              <ExIcon size={18} />
                            </div>
                            <div className="tdb-examen-body">
                              <p className="tdb-examen-matiere">
                                {ex.nomMatiere}
                                {ex.typeExamen === 'RATTRAPAGE' && (
                                    <span className="tdb-rattrapage-badge">Rattrapage</span>
                                )}
                              </p>
                              <div className="tdb-examen-meta">
                                <span>{ex.nomClasse}</span>
                                <span>·</span>
                                <span>n°{ex.numeroExamen}</span>
                                {ex.dateExamen && (
                                    <>
                                      <span>·</span>
                                      <span>{formatDate(ex.dateExamen)}</span>
                                    </>
                                )}
                                {ex.duree > 0 && (
                                    <>
                                      <span>·</span>
                                      <span>{ex.duree} min</span>
                                    </>
                                )}
                              </div>
                            </div>
                            <span
                                className="tdb-etat-pill"
                                style={{ color: cfg.color, background: cfg.bg }}
                            >
                        {cfg.label}
                      </span>
                          </div>
                      );
                    })}
                    <button className="tdb-voir-tout" onClick={() => surNavigation?.('examens')}>
                      Gérer les examens <ChevronRight size={14} />
                    </button>
                  </div>
              ) : (
                  <div className="tdb-empty-small">
                    <GraduationCap size={28} opacity={0.2} />
                    <p>Aucun examen programmé.</p>
                    <button className="tdb-action-btn" onClick={() => surNavigation?.('examens')}>
                      Créer un examen
                    </button>
                  </div>
              )}
            </div>
          </div>
        </div>
      </div>
  );
};

export default TableauDeBordProfesseur;