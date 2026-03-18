import { useState, useEffect, useMemo } from 'react';
import {
    Search,
    Book,
    Calendar,
    Download,
    ExternalLink,
    Library,
    FileText,
    Terminal,
    ChevronDown,
    Filter
} from 'lucide-react';
import './CoursBibliotheque.css';
import { apiCours } from '../services/apiCours';
import {
    downloadBlob,
    openBlobInNewTab,
    readFilenameFromResponse
} from '../utils/fileAccess';

const CoursBibliotheque = () => {
    const [modules, setModules] = useState([]);
    const [activeModule, setActiveModule] = useState(null);
    const [resourceSearch, setResourceSearch] = useState('');
    const [activeSemester, setActiveSemester] = useState('S4');
    const [semesters, setSemesters] = useState(apiCours.getSemesters());
    const [resources, setResources] = useState([]);
    const [loading, setLoading] = useState(true);
    const [resourcesLoading, setResourcesLoading] = useState(false);
    const [error, setError] = useState(null);
    const [studentProfile, setStudentProfile] = useState(null);
    const [selectedSeance, setSelectedSeance] = useState('TOUS');
    const [selectedMatiere, setSelectedMatiere] = useState('TOUS');
    const [availableMatieres, setAvailableMatieres] = useState([]);
    const [availableSeances, setAvailableSeances] = useState([]);

    useEffect(() => {
        const fetchProfile = async () => {
            try {
                const profile = await apiCours.getStudentProfile();
                setStudentProfile(profile);
                if (profile.semestreActuel) {
                    setActiveSemester(profile.semestreActuel);
                }
            } catch (err) {
                console.error("Erreur lors de la recuperation du profil:", err);
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
            } catch (fetchError) {
                console.error('Erreur lors du chargement des modules:', fetchError);
                setError("Impossible de charger les modules. Le serveur a rencontre une erreur.");
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
                setAvailableSeances([]);
                return;
            }

            try {
                setResourcesLoading(true);
                const data = await apiCours.getAggregatedResources(activeModule);
                setResources(data);

                const uniqueMatieres = [...new Set(data.map((r) => r.matiereNom).filter(Boolean))];
                setAvailableMatieres(uniqueMatieres);

                const uniqueSeances = Array.from(
                    new Map(
                        data
                            .filter((r) => r.seanceId)
                            .map((r) => [
                                r.seanceId,
                                {
                                    id: r.seanceId,
                                    label: r.seanceLabel || `Seance ${r.seanceId}`,
                                },
                            ])
                    ).values()
                );
                setAvailableSeances(uniqueSeances);
                setSelectedSeance('TOUS');
            } catch (fetchError) {
                console.error("Erreur lors de l'agregation des ressources:", fetchError);
                setAvailableMatieres([]);
                setAvailableSeances([]);
            } finally {
                setResourcesLoading(false);
            }
        };
        fetchAggregatedResources();
    }, [activeModule]);

    const filteredAvailableSeances = useMemo(() => {
        if (selectedMatiere === 'TOUS') {
            return availableSeances;
        }

        return availableSeances.filter((seance) =>
            resources.some(
                (resource) =>
                    resource.seanceId === seance.id &&
                    resource.matiereNom === selectedMatiere
            )
        );
    }, [availableSeances, resources, selectedMatiere]);

    useEffect(() => {
        if (
            selectedSeance !== 'TOUS' &&
            !filteredAvailableSeances.some(
                (seance) => String(seance.id) === selectedSeance
            )
        ) {
            setSelectedSeance('TOUS');
        }
    }, [filteredAvailableSeances, selectedSeance]);

    const filteredResources = resources.filter((resource) => {
        const matiereNom = resource.matiereNom || '';

        const matchesSeance =
            selectedSeance === 'TOUS' || String(resource.seanceId) === selectedSeance;

        const matchesMatiere =
            selectedMatiere === 'TOUS' || matiereNom === selectedMatiere;

        const matchesSearch = resource.nomOriginal
            .toLowerCase()
            .includes(resourceSearch.toLowerCase());

        return matchesSeance && matchesMatiere && matchesSearch;
    });

    const getActiveModuleName = () =>
        modules.find((module) => module.codeModule === activeModule)?.nom || 'Module';

    const handleDownload = async (resource) => {
        try {
            const response = await apiCours.downloadRessource(resource.id);
            const blob = await response.blob();
            downloadBlob(blob, readFilenameFromResponse(response, resource.nomOriginal || 'ressource'));
        } catch (err) {
            console.error('Erreur de telechargement de ressource:', err);
            setError(err.message || 'Impossible de telecharger la ressource.');
        }
    };

    const handleView = async (resource) => {
        try {
            const response = await apiCours.downloadRessource(resource.id);
            const blob = await response.blob();
            openBlobInNewTab(blob);
        } catch (err) {
            console.error('Erreur d ouverture de ressource:', err);
            setError(err.message || 'Impossible d ouvrir la ressource.');
        }
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
        const normalized = (type || '').toLowerCase();
        if (normalized.includes('pdf')) return Book;
        if (normalized.includes('word') || normalized.includes('text')) return FileText;
        return Terminal;
    };

    if (loading && modules.length === 0) {
        return <div className="cb-loading">Chargement des modules...</div>;
    }

    return (
        <div className="cb-container">
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
                                {semesters.map((semester) => (
                                    <option key={semester} value={semester}>
                                        {semester}
                                    </option>
                                ))}
                            </select>
                            <ChevronDown size={14} className="cb-select-arrow" />
                        </div>
                    </div>
                </div>

                <div className="cb-module-list">
                    {error ? (
                        <div className="cb-error-container" style={{ padding: '20px', textAlign: 'center' }}>
                            <div
                                className="cb-error-msg"
                                style={{ color: '#ef4444', fontSize: '0.875rem', marginBottom: '10px' }}
                            >
                                {error}
                            </div>
                            <button
                                onClick={() =>
                                    setActiveSemester((previous) => {
                                        const current = activeSemester;
                                        setActiveSemester('');
                                        setTimeout(() => setActiveSemester(current), 10);
                                        return previous;
                                    })
                                }
                                className="cb-retry-btn"
                                style={{
                                    padding: '6px 12px',
                                    background: '#6366f1',
                                    color: 'white',
                                    border: 'none',
                                    borderRadius: '6px',
                                    cursor: 'pointer',
                                    fontSize: '0.75rem',
                                }}
                            >
                                Reessayer
                            </button>
                        </div>
                    ) : (
                        <>
                            {modules.map((module) => (
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

            <main className="cb-main">
                <header className="cb-header">
                    <div className="cb-header-row">
                        <h1 className="cb-title">{getActiveModuleName()}</h1>
                    </div>
                </header>

                <section className="cb-content-controls">
                    <div className="cb-filters-row">
                        <div className="cb-filter-group">
                            <label className="cb-filter-label">Matiere:</label>
                            <select
                                value={selectedMatiere}
                                onChange={(e) => setSelectedMatiere(e.target.value)}
                                className="cb-filter-select"
                                disabled={availableMatieres.length === 0}
                            >
                                <option value="TOUS">Toutes</option>
                                {availableMatieres.map((matiere) => (
                                    <option key={matiere} value={matiere}>
                                        {matiere}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <div className="cb-filter-group">
                            <label className="cb-filter-label">Seance:</label>
                            <select
                                value={selectedSeance}
                                onChange={(e) => setSelectedSeance(e.target.value)}
                                className="cb-filter-select"
                                disabled={filteredAvailableSeances.length === 0}
                            >
                                <option value="TOUS">Toutes les seances</option>
                                {filteredAvailableSeances.map((seance) => (
                                    <option key={seance.id} value={String(seance.id)}>
                                        {seance.label}
                                    </option>
                                ))}
                            </select>
                        </div>

                        <button
                            className="cb-clear-filters-btn"
                            onClick={() => {
                                setSelectedSeance('TOUS');
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
                            <span>Recuperation des ressources en cours...</span>
                        </div>
                    ) : (
                        <table className="cb-table">
                            <thead>
                                <tr>
                                    <th>RESSOURCE</th>
                                    <th>MATIERE</th>
                                    <th>DATE</th>
                                    <th>TAILLE</th>
                                    <th>ACTIONS</th>
                                </tr>
                            </thead>
                            <tbody>
                                {filteredResources.map((resource) => {
                                    const Icon = getIconForType(resource.typeMime);
                                    return (
                                        <tr key={resource.id}>
                                            <td>
                                                <div className="cb-resource-info">
                                                    <div className={`cb-res-icon-box ${(resource.typeMime || '').includes('pdf') ? 'cours' : 'td'}`}>
                                                        <Icon size={20} />
                                                    </div>
                                                    <div className="cb-res-details">
                                                        <span className="cb-res-name">{resource.nomOriginal}</span>
                                                        <div className="cb-res-meta">
                                                            <span className={`cb-res-type-badge ${(resource.typeMime || '').includes('pdf') ? 'cours' : 'td'}`}>
                                                                {resource.typeSeance || 'DOCUMENT'}
                                                            </span>
                                                        </div>
                                                    </div>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-matiere-badge" style={{ fontSize: '0.75rem', color: '#64748b' }}>
                                                    {resource.matiereNom}
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-date-box">
                                                    <Calendar size={16} className="cb-text-light" />
                                                    <span>{formatDate(resource.dateUpload)}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-size-box">
                                                    <span>{formatSize(resource.taille)}</span>
                                                </div>
                                            </td>
                                            <td>
                                                <div className="cb-actions">
                                                    <button className="cb-action-btn" title="Telecharger" onClick={() => handleDownload(resource)}>
                                                        <Download size={16} />
                                                    </button>
                                                    <button className="cb-action-btn secondary" title="Ouvrir" onClick={() => handleView(resource)}>
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
                                            Aucune ressource trouvee pour ce module.
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
