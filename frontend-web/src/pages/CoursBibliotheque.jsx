import { useState, useEffect } from 'react';
import {
    Search,
    Book,
    Calendar,
    Download,
    ExternalLink,
    Library,
    FileText,
    Layout,
    Terminal,
    ChevronRight,
    ChevronDown,
    Filter
} from 'lucide-react';
import './CoursBibliotheque.css';
import { apiCours } from '../services/apiCours';

const CoursBibliotheque = () => {
    const [modules, setModules] = useState([]);
    const [activeModule, setActiveModule] = useState(null);
    const [activeTab, setActiveTab] = useState('TOUS');
    const [resourceSearch, setResourceSearch] = useState('');
    const [activeSemester, setActiveSemester] = useState('S4'); // Will be updated by profile
    const [semesters, setSemesters] = useState(apiCours.getSemesters());
    const [resources, setResources] = useState([]);
    const [loading, setLoading] = useState(true);
    const [resourcesLoading, setResourcesLoading] = useState(false);
    const [error, setError] = useState(null);
    const [studentProfile, setStudentProfile] = useState(null);
    const [selectedSeance, setSelectedSeance] = useState('TOUS');
    const [selectedDate, setSelectedDate] = useState('');
    const [selectedMatiere, setSelectedMatiere] = useState('TOUS');
    const [availableMatieres, setAvailableMatieres] = useState([]);

    // Fetch Profile once on mount
    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await apiCours.getStudentProfile();
                setStudentProfile(profile);
                if (profile.semestreActuel) {
                    setActiveSemester(profile.semestreActuel);
                }
            } catch (err) {
                console.error("Erreur lors de la récupération du profil:", err);
            }
        };
        fetchProfile();
    }, []);

    useEffect(() => {
        const fetchModules = async () => {
            try {
                setLoading(true);
                setError(null);
                const modulesData = await apiCours.getStudentModules(activeSemester);
                setModules(modulesData);
                if (modulesData.length > 0) {
                    setActiveModule(modulesData[0].codeModule);
                } else {
                    setActiveModule(null);
                    setResources([]);
                }
            } catch (error) {
                console.error('Erreur lors du chargement des modules:', error);
                setError("Impossible de charger les modules. Le serveur a rencontré une erreur.");
                setModules([]);
            } finally {
                setLoading(false);
            }
        };
        fetchModules();
    }, [activeSemester]);

    useEffect(() => {
        const fetchAggregatedResources = async () => {
            if (!activeModule) {
                setResources([]);
                setAvailableMatieres([]);
                return;
            }
            try {
                setResourcesLoading(true);
                const data = await apiCours.getAggregatedResources(activeModule);
                setResources(data);
                
                // Extract unique matieres for filter dropdown
                const uniqueMatieres = [...new Set(data.map(r => r.matiereNom).filter(Boolean))];
                setAvailableMatieres(uniqueMatieres);
            } catch (error) {
                console.error('Erreur lors de l\'agrégation des ressources:', error);
                setAvailableMatieres([]);
            } finally {
                setResourcesLoading(false);
            }
        };
        fetchAggregatedResources();
    }, [activeModule]);

    const filteredResources = resources.filter(r => {
        const typeMime = r.typeMime || '';
        const typeSeance = r.typeSeance || '';
        const matiereNom = r.matiereNom || '';
        const dateUpload = r.dateUpload || '';
        
        // Filter by date
        const matchesDate = !selectedDate || 
            new Date(dateUpload).toDateString() === new Date(selectedDate).toDateString();
        
        // Filter by matiere
        const matchesMatiere = selectedMatiere === 'TOUS' || matiereNom === selectedMatiere;
        
        // Filter by search
        const matchesSearch = r.nomOriginal.toLowerCase().includes(resourceSearch.toLowerCase());
        
        return matchesDate && matchesMatiere && matchesSearch;
    });

    const getActiveModuleName = () => {
        return modules.find(m => m.codeModule === activeModule)?.nom || 'Module';
    };

    const handleDownload = (res) => {
        const link = document.createElement('a');
        link.href = `http://localhost:8080/api/public/fichiers/voir?chemin=${encodeURIComponent(res.chemin)}`;
        link.download = res.nomOriginal;
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
    };

    const handleView = (res) => {
        window.open(`http://localhost:8080/api/public/fichiers/voir?chemin=${encodeURIComponent(res.chemin)}`, '_blank');
    };

    const formatSize = (bytes) => {
        if (!bytes) return '0 B';
        const k = 1024;
        const sizes = ['B', 'KB', 'MB', 'GB'];
        const i = Math.floor(Math.log(bytes) / Math.log(k));
        return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i];
    };

    const formatDate = (dateString) => {
        if (!dateString) return '-';
        return new Date(dateString).toLocaleDateString('fr-FR');
    };

    const getIconForType = (type) => {
        const t = (type || '').toLowerCase();
        if (t.includes('pdf')) return Book;
        if (t.includes('word') || t.includes('text')) return FileText;
        return Terminal;
    };

    if (loading && modules.length === 0) {
        return <div className="cb-loading">Chargement des modules...</div>;
    }

    return (
        <div className="cb-container">
            {/* Sidebar */}
            <aside className="cb-sidebar">
                <div className="cb-sidebar-header">
                    <div className="cb-sidebar-header-top">
                        <div className="cb-logo-box">
                            <Library size={24} />
                        </div>
                        <h2>Modules</h2>
                    </div>

                    <div className="cb-semester-filter-container">
                        <div className="cb-semester-filter-box">
                            <Filter size={12} className="cb-filter-icon-inline" />
                            <span className="cb-filter-label-text">Semester:</span>
                            <select
                                value={activeSemester}
                                onChange={(e) => setActiveSemester(e.target.value)}
                                className="cb-semester-select-minimal"
                            >
                                {semesters.map(s => (
                                    <option key={s} value={s}>{s}</option>
                                ))}
                            </select>
                            <ChevronDown size={14} className="cb-select-arrow" />
                        </div>
                    </div>
                </div>

                <div className="cb-module-list">
                    {error ? (
                        <div className="cb-error-container" style={{ padding: '20px', textAlign: 'center' }}>
                            <div className="cb-error-msg" style={{ color: '#ef4444', fontSize: '0.875rem', marginBottom: '10px' }}>
                                {error}
                            </div>
                            <button
                                onClick={() => setActiveSemester(prev => {
                                    // Trigger re-fetch by cycling value or same value
                                    const val = activeSemester;
                                    setActiveSemester('');
                                    setTimeout(() => setActiveSemester(val), 10);
                                    return prev;
                                })}
                                className="cb-retry-btn"
                                style={{
                                    padding: '6px 12px',
                                    background: '#6366f1',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '0.75rem'
                                }}
                            >
                                Réessayer
                            </button>
                        </div>
                    ) : (
                        <>
                            {modules.map(module => (
                                <div
                                    key={module.codeModule}
                                    className={`cb-module-item ${activeModule === module.codeModule ? 'active' : ''}`}
                                    onClick={() => setActiveModule(module.codeModule)}
                                >
                                    <div className="cb-module-info">
                                        <div className="cb-status-dot"></div>
                                        <span className="cb-module-name">{module.nom}</span>
                                    </div>
                                </div>
                            ))}
                            {modules.length === 0 && !loading && (
                                <div className="cb-no-data-msg" style={{ padding: '20px', color: '#94a3b8', fontSize: '0.875rem' }}>
                                    Aucun module pour ce semestre.
                                </div>
                            )}
                        </>
                    )}
                </div>
            </aside>

            {/* Main Content */}
            <main className="cb-main">
                <header className="cb-header">
                    <div className="cb-header-row">
                        <h1 className="cb-title">{getActiveModuleName()}</h1>

                        <div className="cb-tabs">
                            {['TOUS', 'COURS', 'TD', 'TP'].map(tab => (
                                <button
                                    key={tab}
                                    className={`cb-tab ${activeTab === tab ? 'active' : ''}`}
                                    onClick={() => setActiveTab(tab)}
                                >
                                    {tab}
                                </button>
                            ))}
                        </div>
                    </div>
                </header>

                <section className="cb-content-controls">
                    <div className="cb-filters-row">
                        <div className="cb-filter-group">
                            <label className="cb-filter-label">Date de séance:</label>
                            <input
                                type="date"
                                value={selectedDate}
                                onChange={(e) => setSelectedDate(e.target.value)}
                                className="cb-filter-select"
                            />
                        </div>
                        
                        <div className="cb-filter-group">
                            <label className="cb-filter-label">Matière:</label>
                            <select
                                value={selectedMatiere}
                                onChange={(e) => setSelectedMatiere(e.target.value)}
                                className="cb-filter-select"
                                disabled={availableMatieres.length === 0}
                            >
                                <option value="TOUS">Toutes</option>
                                {availableMatieres.map(matiere => (
                                    <option key={matiere} value={matiere}>{matiere}</option>
                                ))}
                            </select>
                        </div>
                        
                        <button 
                            className="cb-clear-filters-btn"
                            onClick={() => {
                                setSelectedDate('');
                                setSelectedMatiere('TOUS');
                                setResourceSearch('');
                            }}
                        >
                            Effacer les filtres
                        </button>
                    </div>
                    
                    <div className="cb-search-box cb-resource-search">
                        <Search size={18} />
                        <input
                            type="text"
                            placeholder="Rechercher par titre..."
                            value={resourceSearch}
                            onChange={(e) => setResourceSearch(e.target.value)}
                        />
                    </div>
                </section>

                <section className="cb-resources-container">
                    {resourcesLoading ? (
                        <div className="cb-resources-loading" style={{ padding: '3rem', textAlign: 'center', color: '#6366f1' }}>
                            <span>Récupération des ressources en cours...</span>
                        </div>
                    ) : (
                        <table className="cb-table">
                            <thead>
                                <tr>
                                    <th>RESSOURCE</th>
                                    <th>MATIÈRE</th>
                                    <th>DATE</th>
                                    <th>TAILLE</th>
                                    <th>ACTIONS</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredResources.map(res => {
                                    const Icon = getIconForType(res.typeMime);
                                    return (
                                        <tr key={res.id}>
                                            <td>
                                                <div className="cb-resource-info">
                                                    <div className={`cb-res-icon-box ${(res.typeMime || '').includes('pdf') ? 'cours' : 'td'}`}>
                                                        <Icon size={20} />
                                                    </div>
                                                    <div className="cb-res-details">
                                                        <span className="cb-res-name">{res.nomOriginal}</span>
                                                        <div className="cb-res-meta">
                                                            <span className={`cb-res-type-badge ${(res.typeMime || '').includes('pdf') ? 'cours' : 'td'}`}>
                                                                {res.typeSeance || 'DOCUMENT'}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-matiere-badge" style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                                    {res.matiereNom}
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-date-box">
                                                    <Calendar size={16} className="cb-text-light" />
                                                    <span>{formatDate(res.dateUpload)}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-size-box">
                                                    <span>{formatSize(res.taille)}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-actions">
                                                    <button className="cb-action-btn" title="Télécharger" onClick={() => handleDownload(res)}>
                                                        <Download size={16} />
                                                    </button>
                                                    <button className="cb-action-btn secondary" title="Ouvrir" onClick={() => handleView(res)}>
                                                        <ExternalLink size={16} />
                                                    </button>
                                                </div>
                                            </td>
                                        </tr>
                                    );
                                })}
                                {filteredResources.length === 0 && (
                                    <tr>
                                        <td colSpan="5" style={{ textAlign: 'center', padding: '3rem', color: 'var(--cb-text-light)' }}>
                                            Aucune ressource trouvée pour ce module.
                                        </td>
                                    </tr>
                                )}
                            </tbody>
                        </table>
                    )}
                </section>
            </main>
        </div>
    );
};

export default CoursBibliotheque;

