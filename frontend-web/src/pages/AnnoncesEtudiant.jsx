import { useEffect, useMemo, useState } from 'react';
import {
  Search,
  FileText,
  Download,
  User,
  Paperclip,
  RefreshCw,
  ExternalLink,
} from 'lucide-react';
import { apiAnnonces } from '../services/apiAnnonces';
import './AnnoncesEtudiant.css';

const libellesTypeAnnonce = {
  AUTRES: 'AUTRES',
  COURS: 'COURS',
  EVENT: 'EVENT',
  REUNION: 'REUNION',
  EXAMEN: 'EXAMEN',
};

const filtresTypeAnnonce = [
  'TOUS',
  'EXAMEN',
  'COURS',
  'REUNION',
  'EVENT',
  'AUTRES',
];

const getCategoryLabel = (typeAnnonce, cibleType) => {
  if (typeAnnonce && libellesTypeAnnonce[typeAnnonce]) {
    return libellesTypeAnnonce[typeAnnonce];
  }

  if (cibleType === 'ETUDIANTS_CLASSE') {
    return 'COURS';
  }

  return 'AUTRES';
};

const formatDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  }).toUpperCase();
};

const formatFullDate = (dateString) => {
  const date = new Date(dateString);
  return date.toLocaleDateString('fr-FR', {
    day: 'numeric',
    month: 'long',
    year: 'numeric',
    hour: '2-digit',
    minute: '2-digit',
  });
};

const transformAnnonce = (annonce) => {
  const publishedAt = annonce?.datePublication || annonce?.dateCreation || null;
  const publishedTimestamp = publishedAt ? new Date(publishedAt).getTime() : 0;

  return {
    id: annonce?.id,
    title: annonce?.titre || 'Sans titre',
    date: formatDate(publishedAt),
    fullDate: formatFullDate(publishedAt),
    publishedAt,
    publishedTimestamp: Number.isNaN(publishedTimestamp) ? 0 : publishedTimestamp,
    category: getCategoryLabel(annonce?.typeAnnonce, annonce?.cibleType),
    subject: annonce?.codeClasse || 'General',
    professor: annonce?.auteur
      ? `${annonce.auteur.prenom} ${annonce.auteur.nom}`
      : 'Administration',
    authorId: annonce?.auteur?.id,
    authorPhoto: annonce?.auteur?.photoPath || null,
    hasDot: !annonce?.publiee,
    content: annonce?.contenu || '',
  };
};

const transformBackendData = (backendData) =>
  (Array.isArray(backendData) ? backendData : [])
    .map(transformAnnonce)
    .sort((a, b) => b.publishedTimestamp - a.publishedTimestamp);

const AnnoncesEtudiant = ({ annonceSelectionneeId = null }) => {
  const [searchQuery, setSearchQuery] = useState('');
  const [activeFilter, setActiveFilter] = useState('TOUS');
  const [announcements, setAnnouncements] = useState([]);
  const [activeAnnonce, setActiveAnnonce] = useState(null);
  const [ressourcesParAnnonce, setRessourcesParAnnonce] = useState({});
  const [ressourceLoading, setRessourceLoading] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  const fetchAnnonces = async () => {
    try {
      setLoading(true);
      setError(null);
      const data = await apiAnnonces.etudiant.obtenirToutes();
      const transformedData = transformBackendData(data);
      setAnnouncements(transformedData);
      setActiveAnnonce((previous) => {
        if (!transformedData.length) {
          return null;
        }

        if (annonceSelectionneeId != null) {
          const annonceCible = transformedData.find(
            (item) => String(item.id) === String(annonceSelectionneeId)
          );
          if (annonceCible) {
            return annonceCible;
          }
        }

        if (previous) {
          return transformedData.find((item) => item.id === previous.id) || transformedData[0];
        }

        return transformedData[0];
      });
    } catch (err) {
      setError(err.message || 'Erreur lors du chargement des annonces');
      console.error('Error fetching announcements:', err);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchAnnonces();
  }, []);

  useEffect(() => {
    if (annonceSelectionneeId == null || !announcements.length) {
      return;
    }

    const annonceCible = announcements.find(
      (item) => String(item.id) === String(annonceSelectionneeId)
    );

    if (annonceCible && annonceCible.id !== activeAnnonce?.id) {
      setActiveAnnonce(annonceCible);
    }
  }, [annonceSelectionneeId, announcements, activeAnnonce]);

  useEffect(() => {
    if (annonceSelectionneeId == null || announcements.some((item) => String(item.id) === String(annonceSelectionneeId))) {
      return;
    }

    const chargerAnnonceCible = async () => {
      try {
        const annonce = await apiAnnonces.etudiant.obtenirParId(annonceSelectionneeId);
        const annonceTransformee = transformAnnonce(annonce);

        setAnnouncements((prev) => {
          if (prev.some((item) => String(item.id) === String(annonceTransformee.id))) {
            return prev;
          }

          return [...prev, annonceTransformee].sort(
            (a, b) => b.publishedTimestamp - a.publishedTimestamp
          );
        });
        setActiveAnnonce(annonceTransformee);
      } catch (err) {
        console.error('Error fetching targeted announcement:', err);
        setError(err.message || "Impossible d'ouvrir l'annonce demandee.");
      }
    };

    chargerAnnonceCible();
  }, [annonceSelectionneeId, announcements]);

  useEffect(() => {
    if (!activeAnnonce?.id || ressourcesParAnnonce[activeAnnonce.id]) {
      return;
    }

    const chargerRessources = async () => {
      try {
        setRessourceLoading(true);
        const ressources = await apiAnnonces.etudiant.listerRessources(activeAnnonce.id);
        setRessourcesParAnnonce((prev) => ({
          ...prev,
          [activeAnnonce.id]: Array.isArray(ressources) ? ressources : [],
        }));
      } catch (err) {
        console.error('Error fetching announcement resources:', err);
        setRessourcesParAnnonce((prev) => ({
          ...prev,
          [activeAnnonce.id]: [],
        }));
      } finally {
        setRessourceLoading(false);
      }
    };

    chargerRessources();
  }, [activeAnnonce, ressourcesParAnnonce]);

  const formatFileSize = (taille) => {
    if (!taille || Number.isNaN(Number(taille))) {
      return 'Taille inconnue';
    }

    const units = ['o', 'Ko', 'Mo', 'Go'];
    let valeur = Number(taille);
    let unitIndex = 0;

    while (valeur >= 1024 && unitIndex < units.length - 1) {
      valeur /= 1024;
      unitIndex += 1;
    }

    const precision = unitIndex === 0 ? 0 : 1;
    return `${valeur.toFixed(precision)} ${units[unitIndex]}`;
  };

  const filteredAnnonces = useMemo(
    () =>
      announcements.filter((annonce) => {
        const matchesSearch = annonce.title
          .toLowerCase()
          .includes(searchQuery.toLowerCase());
        const matchesFilter =
          activeFilter === 'TOUS' || annonce.category === activeFilter;
        return matchesSearch && matchesFilter;
      }),
    [announcements, searchQuery, activeFilter]
  );

  const ressourcesActives = activeAnnonce?.id
    ? ressourcesParAnnonce[activeAnnonce.id] || []
    : [];

  const telechargerRessource = async (ressourceId) => {
    try {
      setError(null);
      await apiAnnonces.etudiant.telechargerRessource(ressourceId);
    } catch (err) {
      setError(err.message || 'Erreur lors du telechargement de la ressource');
    }
  };

  const consulterRessource = async (ressourceId) => {
    try {
      setError(null);
      await apiAnnonces.etudiant.consulterRessource(ressourceId);
    } catch (err) {
      setError(err.message || 'Erreur lors de l ouverture de la ressource');
    }
  };

  return (
    <div className="ann-container">
      {loading && (
        <div className="ann-loading">
          <div className="ann-loading-spinner"></div>
          <p>Chargement des annonces...</p>
        </div>
      )}

      {error && (
        <div className="ann-error">
          <p>{error}</p>
          <button onClick={fetchAnnonces} className="ann-retry-btn">
            Reessayer
          </button>
        </div>
      )}

      {!loading && !error && (
        <>
          <aside className="ann-sidebar">
            <div className="ann-sidebar-header">
              <h1 className="ann-sidebar-title">Flux d'annonces</h1>
              <button
                onClick={fetchAnnonces}
                className="ann-refresh-btn"
                title="Rafraichir les annonces"
              >
                <RefreshCw size={18} />
              </button>
            </div>

            <div className="ann-search-box">
              <Search className="ann-search-icon" size={18} />
              <input
                type="text"
                placeholder="Rechercher..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>

            <div className="ann-filters">
              {filtresTypeAnnonce.map((filter) => (
                <button
                  key={filter}
                  className={`ann-filter-pill ${activeFilter === filter ? 'active' : ''}`}
                  onClick={() => setActiveFilter(filter)}
                >
                  {filter}
                </button>
              ))}
            </div>

            <div className="ann-list">
              {filteredAnnonces.length > 0 ? (
                filteredAnnonces.map((annonce) => (
                  <div
                    key={annonce.id}
                    className={`ann-item ${activeAnnonce?.id === annonce.id ? 'active' : ''}`}
                    onClick={() => setActiveAnnonce(annonce)}
                  >
                    <div className="ann-item-header">
                      {annonce.hasDot && <div className="ann-dot"></div>}
                      <span className="ann-item-date">{annonce.date}</span>
                    </div>
                    <div className="ann-item-title">{annonce.title}</div>
                    <div className="ann-item-subtitle">{annonce.subject}</div>
                  </div>
                ))
              ) : (
                <div className="ann-empty">
                  <p>Aucune annonce trouvee</p>
                </div>
              )}
            </div>
          </aside>

          <main className="ann-main">
            {activeAnnonce ? (
              <article className="ann-detail">
                <header className="ann-detail-header">
                  <div className="ann-prof-box">
                    <div className="ann-prof-img">
                      {activeAnnonce.authorPhoto ? (
                        <img
                          src={`http://localhost:8080/uploads/usersPhotos/${activeAnnonce.authorPhoto}`}
                          alt={activeAnnonce.professor}
                          onError={(e) => {
                            e.target.onerror = null;
                            e.target.style.display = 'none';
                            if (e.target.nextSibling) {
                              e.target.nextSibling.style.display = 'block';
                            }
                          }}
                          style={{
                            width: '100%',
                            height: '100%',
                            objectFit: 'cover',
                            borderRadius: '50%',
                          }}
                        />
                      ) : null}
                      <User
                        size={30}
                        style={{
                          margin: 'auto',
                          display: activeAnnonce.authorPhoto ? 'none' : 'block',
                        }}
                        color="#94a3b8"
                      />
                    </div>
                    <div className="ann-prof-info">
                      <div className="ann-prof-name">{activeAnnonce.professor}</div>
                      <div className="ann-prof-subject">{activeAnnonce.subject}</div>
                    </div>
                  </div>

                  <div className="ann-status-box">
                    <div className="ann-status-label">PUBLIE LE</div>
                    <div className="ann-status-date">{activeAnnonce.fullDate}</div>
                  </div>
                </header>

                <div className="ann-badges">
                  <div className={`ann-badge ${activeAnnonce.category.toLowerCase()}`}>
                    {activeAnnonce.category}
                  </div>
                </div>

                <h2 className="ann-headline">{activeAnnonce.title}</h2>

                <div className="ann-content">
                  {activeAnnonce.content.split('\n\n').map((paragraph, idx) => (
                    <p key={idx}>{paragraph}</p>
                  ))}
                </div>

                <div className="ann-resources-section">
                  <div className="ann-resources-title">
                    <Paperclip size={14} />
                    RESSOURCES ATTACHEES
                  </div>

                  <div className="ann-resources-grid">
                    {ressourceLoading ? (
                      <div className="ann-no-resources">
                        <p>Chargement des ressources...</p>
                      </div>
                    ) : ressourcesActives.length > 0 ? (
                      ressourcesActives.map((ressource) => (
                        <div
                          key={ressource.id}
                          className="ann-resource-card"
                        >
                          <div className="ann-resource-icon">
                            <FileText size={20} />
                          </div>
                          <div className="ann-resource-info">
                            <strong>{ressource.nomOriginal}</strong>
                            <span>{formatFileSize(ressource.taille)}</span>
                          </div>
                          <div className="ann-resource-actions">
                            <button
                              type="button"
                              className="ann-resource-btn secondary"
                              onClick={() => consulterRessource(ressource.id)}
                            >
                              <ExternalLink size={16} />
                              Consulter
                            </button>
                            <button
                              type="button"
                              className="ann-resource-btn"
                              onClick={() => telechargerRessource(ressource.id)}
                            >
                              <Download size={16} />
                              Telecharger
                            </button>
                          </div>
                        </div>
                      ))
                    ) : (
                      <div className="ann-no-resources">
                        <p>Aucune ressource attachee</p>
                      </div>
                    )}
                  </div>
                </div>
              </article>
            ) : (
              <div className="ann-no-selection">
                <p>Selectionnez une annonce pour voir les details</p>
              </div>
            )}
          </main>
        </>
      )}
    </div>
  );
};

export default AnnoncesEtudiant;