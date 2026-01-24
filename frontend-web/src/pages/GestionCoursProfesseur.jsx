import { useState } from 'react';
import {
  Plus, FileText, Video, ExternalLink, X
} from 'lucide-react';
import './GestionCoursProfesseur.css';

const GestionCoursProfesseur = () => {
  const [activeTab, setActiveTab] = useState('INF101');

  // Mock Data
  const courses = [
    { id: 'INF101', label: 'INF101 - Informatique 101', name: 'Informatique 101' },
    { id: 'MTH202', label: 'MTH202 - Mathématiques Appliquées', name: 'Mathématiques Appliquées' }
  ];

  const resources = {
    'INF101': [
      { id: 1, name: "Introduction à l'Algorithmique", type: 'PDF', date: '01/10/2023', icon: FileText, color: 'red' },
      { id: 2, name: "Structures de Données", type: 'Video', date: '05/10/2023', icon: Video, color: 'blue' },
      { id: 3, name: "TP 1: Listes Chaînées", type: 'TP', date: '10/10/2023', icon: Plus, color: 'green' }
    ],
    'MTH202': [
      { id: 4, name: "Matrices et Vecteurs", type: 'PDF', date: '02/10/2023', icon: FileText, color: 'red' },
      { id: 5, name: "Calcul Intégral", type: 'PDF', date: '06/10/2023', icon: FileText, color: 'red' }
    ]
  };

  const currentResources = resources[activeTab] || [];
  const currentCourseName = courses.find(c => c.id === activeTab)?.name;

  return (
    <div className="gestion-cours-container">
      {/* Header */}
      <div className="page-header">
        <div className="header-text">
          <h1>Gestion des Cours</h1>
          <p>Gérez vos supports pédagogiques, TP et TD.</p>
        </div>
        <button className="btn-add-doc">
          <Plus size={18} />
          Ajouter un document
        </button>
      </div>

      {/* Tabs */}
      <div className="tabs-container">
        {courses.map(course => (
          <button
            key={course.id}
            className={`tab-btn ${activeTab === course.id ? 'active' : ''}`}
            onClick={() => setActiveTab(course.id)}
          >
            {course.label}
          </button>
        ))}
      </div>

      {/* Content Card */}
      <div className="content-card">
        <div className="card-header-bar">
          <h2>Documents pour {currentCourseName}</h2>
          <span className="file-count">{currentResources.length} FICHIERS</span>
        </div>

        <table className="resources-table">
          <thead>
            <tr>
              <th className="th-name">NOM DU SUPPORT</th>
              <th className="th-type">TYPE</th>
              <th className="th-date">DATE D'AJOUT</th>
              <th className="th-action">ACTION</th>
            </tr>
          </thead>
          <tbody>
            {currentResources.map((resource, index) => (
              <tr key={resource.id}>
                <td className="td-name">
                  <div className={`file-icon-wrapper bg-${resource.color}`}>
                    <resource.icon size={22} className={`icon-${resource.color}`} />
                  </div>
                  <span className="file-name">{resource.name}</span>
                </td>
                <td className="td-type">
                  <span className={`type-badge badge-${resource.color}`}>
                    {resource.type}
                  </span>
                </td>
                <td className="td-date">{resource.date}</td>
                <td className="td-action">
                  {/* Showing actions only on the first item to match visual screenshot, or all if preferred. Let's make them accessible on all but styled precisely */}
                  <div className="action-buttons">
                    <button className="btn-icon-action btn-open">
                      <ExternalLink size={18} />
                    </button>
                    <button className="btn-icon-action btn-delete">
                      <X size={18} />
                    </button>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default GestionCoursProfesseur;
