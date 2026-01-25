import React, { useState, useEffect, useMemo } from 'react';
import {
  FileText,
  ChevronDown,
  Plus,
  TrendingUp,
  Award,
  Globe,
  Save,
  CheckCircle2,
  UploadCloud,
  Search
} from 'lucide-react';
import { apiClasses } from '../services/apiClasses';
import { apiMatieres } from '../services/apiMatieres';
import { apiProfesseur } from '../services/apiProfesseur';
import './GestionNotesProfesseur.css';

const GestionNotesProfesseur = () => {
  const [classes, setClasses] = useState([]);
  const [matieres, setMatieres] = useState([]);
  const [selectedClasse, setSelectedClasse] = useState(null);
  const [selectedMatiere, setSelectedMatiere] = useState(null);
  const [etudiants, setEtudiants] = useState([]);
  const [isLoading, setIsLoading] = useState(false);

  // Mock initial data if API returns empty
  const mockEtudiants = [
    { id: 1, nom: 'Yassine Belkadi', noteDS: '', noteExamen: '', remarque: '' },
    { id: 2, nom: 'Amine Radi', noteDS: '', noteExamen: '', remarque: '' },
    { id: 3, nom: 'Sara El Amrani', noteDS: '', noteExamen: '', remarque: '' },
    { id: 4, nom: 'Meryem Tazi', noteDS: '', noteExamen: '', remarque: '' },
    { id: 5, nom: 'Omar Benjelloun', noteDS: '', noteExamen: '', remarque: '' }
  ];

  useEffect(() => {
    const loadInitialData = async () => {
      try {
        const [classesData, matieresData] = await Promise.all([
          apiClasses.obtenirToutes(),
          apiMatieres.obtenirTous()
        ]);
        setClasses(classesData);
        setMatieres(matieresData);

        if (classesData.length > 0) setSelectedClasse(classesData[0]);
        if (matieresData.length > 0) setSelectedMatiere(matieresData[0]);

        // Fallback students
        setEtudiants(mockEtudiants);
      } catch (err) {
        console.error('Error loading initial data', err);
        setEtudiants(mockEtudiants);
      }
    };
    loadInitialData();
  }, []);

  const handleGradeChange = (id, field, value) => {
    setEtudiants(prev => prev.map(e =>
      e.id === id ? { ...e, [field]: value } : e
    ));
  };

  // Calculate Class Statistics
  const stats = useMemo(() => {
    const validNotes = etudiants
      .map(e => {
        const ds = parseFloat(e.noteDS) || 0;
        const ex = parseFloat(e.noteExamen) || 0;
        // Simple average for mock: (DS*0.4 + EX*0.6)
        return (ds * 0.4) + (ex * 0.6);
      })
      .filter(n => !isNaN(n) && n > 0);

    if (validNotes.length === 0) return { avg: 0, min: 0, max: 0 };

    const sum = validNotes.reduce((a, b) => a + b, 0);
    return {
      avg: (sum / validNotes.length).toFixed(1),
      min: Math.min(...validNotes).toFixed(1),
      max: Math.max(...validNotes).toFixed(1)
    };
  }, [etudiants]);

  return (
    <div className="gestion-notes-modern">
      {/* 1. Header & Selection Banner */}
      <div className="notes-header-banner">
        <div className="header-left">
          <div className="header-icon-box">
            <FileText size={32} color="white" />
          </div>
          <div className="header-titles">
            <h1>Gestion des Notes</h1>
            <p className="subtitle">SAISIE & PUBLICATION DES RÉSULTATS</p>
          </div>
        </div>

        <div className="header-selectors">
          <div className="pill-selector">
            <span className="selector-text">
              {selectedClasse ? `${selectedClasse.nom} | ${selectedClasse.codeClasse || 'L3'}` : 'Sélectionner une classe...'}
            </span>
            <ChevronDown size={18} color="#94A3B8" />
          </div>

          <div className="pill-selector">
            <span className="selector-text">
              {selectedMatiere ? selectedMatiere.nom : 'Sélectionner la matière...'}
            </span>
            <ChevronDown size={18} color="#94A3B8" />
          </div>
        </div>
      </div>

      {/* 2. Main Layout Grid */}
      <div className="notes-main-grid">

        {/* Left: Grade Table Area */}
        <div className="grade-table-card">
          <div className="table-title-row">
            <div className="title-with-icon">
              <div className="plus-icon-box">
                <Plus size={20} />
              </div>
              <h2>Contrôle Continu n°1</h2>
            </div>
            <span className="student-badge">{etudiants.length} ÉTUDIANTS</span>
          </div>

          <table className="modern-grades-table">
            <thead>
              <tr>
                <th>ÉTUDIANT</th>
                <th>NOTE DS (/20)</th>
                <th>EXAMEN (/20)</th>
                <th>REMARQUE</th>
              </tr>
            </thead>
            <tbody>
              {etudiants.map(etud => (
                <tr key={etud.id}>
                  <td className="student-cell">
                    <div className="student-avatar">
                      {etud.nom.split(' ').map(n => n[0]).join('')}
                    </div>
                    <span className="student-name-bold">{etud.nom}</span>
                  </td>
                  <td>
                    <input
                      type="text"
                      className="note-input-modern"
                      placeholder="--"
                      value={etud.noteDS}
                      onChange={(e) => handleGradeChange(etud.id, 'noteDS', e.target.value)}
                    />
                  </td>
                  <td>
                    <input
                      type="text"
                      className="note-input-modern"
                      placeholder="--"
                      value={etud.noteExamen}
                      onChange={(e) => handleGradeChange(etud.id, 'noteExamen', e.target.value)}
                    />
                  </td>
                  <td>
                    <input
                      type="text"
                      className="comment-input-modern"
                      placeholder="Commentaire..."
                      value={etud.remarque}
                      onChange={(e) => handleGradeChange(etud.id, 'remarque', e.target.value)}
                    />
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {/* Right: Sidebar Statistics & Actions */}
        <div className="sidebar-right">

          {/* Stats Card */}
          <div className="stats-card">
            <div className="stats-card-header">
              <TrendingUp size={16} color="#10B981" />
              <span>STATISTIQUES DE CLASSE</span>
            </div>

            <div className="main-stat-box">
              <div>
                <span className="stat-label-mini">MOYENNE GÉNÉRALE</span>
                <div className="stat-value-large">{stats.avg}/20</div>
              </div>
              <Award size={40} className="medal-icon-box" />
            </div>

            <div className="secondary-stats">
              <div className="stat-pill-small">
                <h4>NOTE MAX</h4>
                <p>{stats.max}</p>
              </div>
              <div className="stat-pill-small">
                <h4>NOTE MIN</h4>
                <p>{stats.min}</p>
              </div>
            </div>
          </div>

          {/* Actions Box */}
          <div className="actions-container">
            <button className="btn-publish">
              <Globe size={18} />
              PUBLIER LES RÉSULTATS
            </button>
            <button className="btn-draft">
              <Save size={18} />
              ENREGISTRER BROUILLON
            </button>
            <div className="auto-save-status">
              <CheckCircle2 size={14} />
              Sauvegarde automatique active
            </div>
          </div>

          {/* PDF Correction Card */}
          <div className="pdf-upload-card">
            <div className="pdf-header">
              <div className="pdf-icon-box">
                <FileText size={20} />
              </div>
              <div>
                <h3>Correction PDF</h3>
                <p>DOCUMENT OFFICIEL</p>
              </div>
            </div>
            <div className="upload-dropzone">
              <UploadCloud size={32} className="upload-icon" />
              <p>Uploader la correction</p>
              <span>Format PDF</span>
            </div>
          </div>

        </div>
      </div>
    </div>
  );
};

export default GestionNotesProfesseur;
