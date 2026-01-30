import { useState } from 'react';
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
import './AnnoncesEtudiant.css';

const AnnoncesEtudiant = () => {
    const [searchQuery, setSearchQuery] = useState('');
    const [activeFilter, setActiveFilter] = useState('TOUS');

    // Mock Data for Announcements
    const announcements = [
        {
            id: 1,
            title: "Report du Contrôle Continu n°2 de React",
            date: "20 MAI 2024",
            fullDate: "20 Mai 2024 à 08:15",
            category: "EXAMEN",
            subject: "REACT.JS & TYPESCRIPT",
            professor: "Pr. Meryem Alaoui",
            priority: "HAUTE",
            hasDot: true,
            isPinned: true,
            content: `Chers étudiants, nous vous informons que la séance de contrôle continu prévue initialement ce lundi doit être décalée. L'infrastructure réseau du bâtiment A subira une maintenance critique.

Le nouvel horaire est fixé au Mercredi 22 Mai à 10:30. La salle reste inchangée (Amphi A). Veuillez vous assurer que vos environnements de développement sont prêts et que vous avez cloné le repository de base pour l'examen.

Bonnes révisions à tous.`
        },
        {
            id: 2,
            title: "Micro-services : Supports dispos",
            date: "19 MAI 2024",
            fullDate: "19 Mai 2024 à 14:30",
            category: "COURS",
            subject: "Node.js Architecture",
            professor: "Pr. Karim El Idrissi",
            priority: "NORMALE",
            hasDot: false,
            isPinned: false,
            content: `Bonjour à tous, les supports de cours concernant l'architecture micro-services sont maintenant disponibles sur la plateforme. Vous y trouverez les schémas de conception vus en cours ainsi que des exemples d'implémentation.

N'oubliez pas de consulter la section sur Docker Compose.`
        },
        {
            id: 3,
            title: "Google Cloud Seminar : Special Guest",
            date: "18 MAI 2024",
            fullDate: "18 Mai 2024 à 09:00",
            category: "EVENT", // Not in filters but shows in general
            subject: "Général",
            professor: "Administration",
            priority: "HAUTE",
            hasDot: true,
            isPinned: true,
            content: `Nous avons le plaisir d'accueillir un expert de Google Cloud pour un séminaire exceptionnel ce vendredi. La participation est fortement recommandée pour les étudiants de L3.`
        },
        {
            id: 4,
            title: "Changement de Salle : TD UML",
            date: "17 MAI 2024",
            fullDate: "17 Mai 2024 à 11:45",
            category: "COURS",
            subject: "UML Design",
            professor: "Pr. Ahmed Rahali",
            priority: "NORMALE",
            hasDot: false,
            isPinned: false,
            content: `Le TD d'UML initialement prévu en Labo 3 se tiendra exceptionnellement en Salle 105.`
        }
    ];

    const [activeAnnonce, setActiveAnnonce] = useState(announcements[0]);

    const filteredAnnonces = announcements.filter(ann => {
        const matchesSearch = ann.title.toLowerCase().includes(searchQuery.toLowerCase());
        const matchesFilter = activeFilter === 'TOUS' || ann.category === activeFilter;
        return matchesSearch && matchesFilter;
    });

    return (
        <div className="ann-container">
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
                    {filteredAnnonces.map(ann => (
                        <div
                            key={ann.id}
                            className={`ann-item ${activeAnnonce.id === ann.id ? 'active' : ''}`}
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
                    ))}
                </div>
            </aside>

            {/* Main Content Area */}
            <main className="ann-main">
                <article className="ann-detail">
                    <header className="ann-detail-header">
                        <div className="ann-prof-box">
                            <div className="ann-prof-img">
                                {/* Using a generic user icon as placeholder if image fails */}
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
                            <div className="ann-resource-card">
                                <FileText size={24} color="#6366f1" />
                                <div className="ann-resource-info">
                                    {/* Labels would go here */}
                                    <div style={{ fontSize: '0.875rem', fontWeight: 700 }}>Support_React_Mod2.pdf</div>
                                </div>
                            </div>
                        </div>
                    </div>
                </article>
            </main>
        </div>
    );
};

export default AnnoncesEtudiant;
