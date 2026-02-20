import { useState, useEffect } from 'react';
import {
    Search,
    Star,
    Sparkles,
    FileText,
    Download,
    User,
    Calendar,
    Clock,
    Paperclip
} from 'lucide-react';
import { apiAnnonces } from '../services/apiAnnonces';
import './AnnoncesEtudiant.css';

const AnnoncesEtudiant = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [activeFilter, setActiveFilter] = useState('TOUS');
    const [announcements, setAnnouncements] = useState([]);
    const [activeAnnonce, setActiveAnnonce] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Fetch announcements from backend
    useEffect(() => {
        const fetchAnnonces = async () => {
            try {
                setLoading(true);
                setError(null);
                const data = await apiAnnonces.etudiant.obtenirToutes();
                const transformedData = transformBackendData(data);
                setAnnouncements(transformedData);
                if (transformedData.length > 0) {
                    setActiveAnnonce(transformedData[0]);
                }
            } catch (err) {
                setError('Erreur lors du chargement des annonces');
                console.error('Error fetching announcements:', err);
            } finally {
                setLoading(false);
            }
        };

        fetchAnnonces();
    }, []);

    // Transform backend data to match frontend expectations
    const transformBackendData = (backendData) => {
        return backendData.map(annonce => ({
            id: annonce.id,
            title: annonce.titre,
            date: formatDate(annonce.datePublication),
            fullDate: formatFullDate(annonce.datePublication),
            category: getCategoryFromCibleType(annonce.cibleType),
            subject: annonce.codeClasse || 'Général',
            professor: annonce.auteur ? `${annonce.auteur.prenom} ${annonce.auteur.nom}` : 'Administration',
            priority: 'NORMALE', // Default, can be enhanced based on additional backend fields
            hasDot: !annonce.publiee, // Assuming unpublished announcements show as unread
            isPinned: annonce.cibleType === 'ETUDIANTS_GLOBAL', // Global announcements are pinned
            content: annonce.contenu
        }));
    };

    // Format date for display
    const formatDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', { 
            day: 'numeric', 
            month: 'long', 
            year: 'numeric' 
        }).toUpperCase();
    };

    // Format full date with time
    const formatFullDate = (dateString) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('fr-FR', { 
            day: 'numeric', 
            month: 'long', 
            year: 'numeric',
            hour: '2-digit',
            minute: '2-digit'
        });
    };

    // Map backend cibleType to frontend category
    const getCategoryFromCibleType = (cibleType) => {
        switch (cibleType) {
            case 'ETUDIANTS_CLASSE':
                return 'COURS';
            case 'ETUDIANTS_GLOBAL':
                return 'EVENT';
            default:
                return 'COURS';
        }
    };

    const filteredAnnonces = announcements.filter(ann => {
        const matchesSearch = ann.title.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesFilter = activeFilter === 'TOUS' || ann.category === activeFilter;
        return matchesSearch && matchesFilter;
    });

    return (
        <div className="ann-container">
            {/* Loading State */}
            {loading && (
                <div className="ann-loading">
                    <div className="ann-loading-spinner"></div>
                    <p>Chargement des annonces...</p>
                </div>
            )}

            {/* Error State */}
            {error && (
                <div className="ann-error">
                    <p>{error}</p>
                    <button onClick={() => window.location.reload()} className="ann-retry-btn">
                        Réessayer
                    </button>
                </div>
            )}

            {/* Main Content - Only show when not loading and no error */}
            {!loading && !error && (
                <>
                    {/* Left Sidebar */}
                    <aside className="ann-sidebar">
                        <h1 className="ann-sidebar-title">Flux d'annonces</h1>

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
                            {['TOUS', 'EXAMEN', 'COURS'].map(filter => (
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
                                filteredAnnonces.map(ann => (
                                    <div
                                        key={ann.id}
                                        className={`ann-item ${activeAnnonce?.id === ann.id ? 'active' : ''}`}
                                        onClick={() => setActiveAnnonce(ann)}
                                    >
                                        <div className="ann-item-header">
                                            {ann.hasDot && <div className="ann-dot"></div>}
                                            <span className="ann-item-date">{ann.date}</span>
                                        </div>
                                        {ann.isPinned && <Star className="ann-pin" size={16} fill="#fca311" stroke="#fca311" />}
                                        <div className="ann-item-title">{ann.title}</div>
                                        <div className="ann-item-subtitle">{ann.subject}</div>
                                    </div>
                                ))
                            ) : (
                                <div className="ann-empty">
                                    <p>Aucune annonce trouvée</p>
                                </div>
                            )}
                        </div>
                    </aside>

                    {/* Main Content Area */}
                    <main className="ann-main">
                        {activeAnnonce ? (
                            <article className="ann-detail">
                                <header className="ann-detail-header">
                                    <div className="ann-prof-box">
                                        <div className="ann-prof-img">
                                            <User size={30} style={{ margin: 'auto', display: 'block' }} color="#94a3b8" />
                                        </div>
                                        <div className="ann-prof-info">
                                            <div className="ann-prof-name">{activeAnnonce.professor}</div>
                                            <div className="ann-prof-subject">{activeAnnonce.subject}</div>
                                        </div>
                                    </div>

                                    <div className="ann-status-box">
                                        <div className="ann-status-label">PUBLIÉ LE</div>
                                        <div className="ann-status-date">{activeAnnonce.fullDate}</div>
                                    </div>
                                </header>

                                <div className="ann-badges">
                                    <div className={`ann-badge ${activeAnnonce.category.toLowerCase()}`}>
                                        {activeAnnonce.category}
                                    </div>
                                    {activeAnnonce.priority === 'HAUTE' && (
                                        <div className="ann-badge priorite">
                                            <Sparkles size={14} />
                                            PRIORITÉ HAUTE
                                        </div>
                                    )}
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
                                        RESSOURCES ATTACHÉES
                                    </div>
                                    <div className="ann-resources-grid">
                                        {/* Resources would be fetched from backend if available */}
                                        <div className="ann-no-resources">
                                            <p>Aucune ressource attachée</p>
                                        </div>
                                    </div>
                                </div>
                            </article>
                        ) : (
                            <div className="ann-no-selection">
                                <p>Sélectionnez une annonce pour voir les détails</p>
                            </div>
                        )}
                    </main>
                </>
            )}
        </div>
    );
};

export default AnnoncesEtudiant;
