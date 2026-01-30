import { useState } from 'react';
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

const CoursBibliotheque = () => {
    const [activeModule, setActiveModule] = useState('react');
    const [activeTab, setActiveTab] = useState('TOUS');
    const [resourceSearch, setResourceSearch] = useState('');
    const [activeSemester, setActiveSemester] = useState('TOUS');

    // Mock Modules Data
    const modules = [
        { id: 'react', name: 'React.js & TypeScript', count: 3, active: true, semestre: 'S4' },
        { id: 'node', name: 'Node.js Architecture', count: 2, active: false, semestre: 'S3' },
        { id: 'analyse', name: 'Analyse & Conception', count: 1, active: false, semestre: 'S2' },
        { id: 'sql', name: 'Bases de Données SQL', count: 1, active: false, semestre: 'S1' },
        { id: 'security', name: 'Réseaux & Sécurité', count: 1, active: false, semestre: 'S3' },
        { id: 'algo', name: 'Algorithmique S3', count: 1, active: false, semestre: 'S3' },
        { id: 'management', name: 'Management de Projet', count: 1, active: false, semestre: 'S4' },
    ];

    // Mock Resources Data
    const resources = [
        {
            id: 1,
            name: 'Fondamentaux de TypeScript & React',
            type: 'COURS',
            semestre: 'S-1',
            professor: 'Pr. Meryem Alaoui',
            date: '15/05/2024',
            size: '2.4 MB',
            icon: Book,
            moduleId: 'react'
        },
        {
            id: 2,
            name: "Gestion d'état avec Context API",
            type: 'TD',
            semestre: 'S-1',
            professor: 'Pr. Meryem Alaoui',
            date: '16/05/2024',
            size: '1.1 MB',
            icon: FileText,
            moduleId: 'react'
        },
        {
            id: 3,
            name: 'Projet : Dashboard Architecture',
            type: 'TP',
            semestre: 'S-1',
            professor: 'Pr. Meryem Alaoui',
            date: '17/05/2024',
            size: '4.8 MB',
            icon: Terminal,
            moduleId: 'react'
        }
    ];

    const filteredModules = modules.filter(m =>
        activeSemester === 'TOUS' || m.semestre === activeSemester
    );

    const filteredResources = resources.filter(r => {
        const matchesModule = r.moduleId === activeModule;
        const matchesTab = activeTab === 'TOUS' || r.type === activeTab;
        const matchesSearch = r.name.toLowerCase().includes(resourceSearch.toLowerCase());
        return matchesModule && matchesTab && matchesSearch;
    });

    const getActiveModuleName = () => {
        return modules.find(m => m.id === activeModule)?.name || 'Module';
    };

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
                                <option value="TOUS">Tous</option>
                                <option value="S1">Semestre 1</option>
                                <option value="S2">Semestre 2</option>
                                <option value="S3">Semestre 3</option>
                                <option value="S4">Semestre 4</option>
                            </select>
                            <ChevronDown size={14} className="cb-select-arrow" />
                        </div>
                    </div>
                </div>



                <div className="cb-module-list">
                    {filteredModules.map(module => (
                        <div
                            key={module.id}
                            className={`cb-module-item ${activeModule === module.id ? 'active' : ''}`}
                            onClick={() => setActiveModule(module.id)}
                        >
                            <div className="cb-module-info">
                                <div className="cb-status-dot"></div>
                                <span className="cb-module-name">{module.name}</span>
                            </div>
                            <span className="cb-module-count">{module.count}</span>
                        </div>
                    ))}
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
                    <table className="cb-table">
                        <thead>
                            <tr>
                                <th>RESSOURCE</th>
                                <th>PROFESSEUR</th>
                                <th>DATE</th>
                                <th>TAILLE</th>
                                <th>ACTIONS</th>
                            </tr>
                        </thead>
                        <tbody>
                            {filteredResources.map(res => (
                                <tr key={res.id}>
                                    <td>
                                        <div className="cb-resource-info">
                                            <div className={`cb-res-icon-box ${res.type.toLowerCase()}`}>
                                                <res.icon size={24} />
                                            </div>
                                            <div className="cb-res-details">
                                                <span className="cb-res-name">{res.name}</span>
                                                <div className="cb-res-meta">
                                                    <span className={`cb-res-type-badge ${res.type.toLowerCase()}`}>{res.type}</span>
                                                    <span className="cb-breadcrumb-sep">•</span>
                                                    <span className="cb-res-sem">{res.semestre}</span>
                                                </div>
                                            </div>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="cb-prof-box">
                                            <img src="https://ui-avatars.com/api/?name=Meryem+Alaoui&background=random" alt="" className="cb-prof-avatar" />
                                            <span className="cb-prof-name">{res.professor}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="cb-date-box">
                                            <Calendar size={16} className="cb-text-light" />
                                            <span>{res.date}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="cb-size-box">
                                            <span>{res.size}</span>
                                        </div>
                                    </td>
                                    <td>
                                        <div className="cb-actions">
                                            <button className="cb-action-btn" title="Télécharger">
                                                <Download size={18} />
                                            </button>
                                            <button className="cb-action-btn secondary" title="Ouvrir">
                                                <ExternalLink size={18} />
                                            </button>
                                        </div>
                                    </td>
                                </tr>
                            ))}
                            {filteredResources.length === 0 && (
                                <tr>
                                    <td colSpan="5" style={{ textAlign: 'center', padding: '3rem', color: 'var(--cb-text-light)' }}>
                                        Aucune ressource trouvée pour ce module ou ce filtre.
                                    </td>
                                </tr>
                            )}
                        </tbody>
                    </table>
                </section>
            </main>
        </div>
    );
};

export default CoursBibliotheque;
