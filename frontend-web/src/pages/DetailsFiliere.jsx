import { useState, useEffect } from 'react';
import { Layers, Edit2, Trash2, Plus, ChevronRight, Home, Building2, GraduationCap, Edit, FileText, Hash, User, Award, Clock } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiStructure } from '../services/apiStructure';
import { apiClasses } from '../services/apiClasses';
import DetailsClasse from './DetailsClasse';
import './DetailsFiliere.css';
import './ModernAcademicRedesign.css';

const DetailsFiliere = ({ filiere, departement, onRetour }) => {
    const [classes, setClasses] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal states
    const [modalOpen, setModalOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentClasse, setCurrentClasse] = useState(null);

    // Navigation state
    const [selectedClasse, setSelectedClasse] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        codeClasse: '',
        nom: '',
        niveau: '',
        anneeUniversitaire: '',
        effectifMax: 30,
        codeFiliere: filiere?.codeFiliere || ''
    });

    // Fetch classes on mount
    useEffect(() => {
        fetchClasses();
    }, [filiere]);

    const fetchClasses = async () => {
        setIsLoading(true);
        try {
            // Utiliser l'API dédiée pour récupérer les classes par filière
            const data = await apiClasses.obtenirParFiliere(filiere.codeFiliere);

            setClasses(data);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch classes:', err);
            setError('Impossible de charger les classes.');
        } finally {
            setIsLoading(false);
        }
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleOpenAddModal = () => {
        setFormData({
            codeClasse: '',
            nom: '',
            niveau: '',
            anneeUniversitaire: '',
            effectifMax: 30,
            codeFiliere: filiere.codeFiliere
        });
        setIsEditing(false);
        setCurrentClasse(null);
        setModalOpen(true);
    };

    const handleEditClasse = (classe) => {
        setFormData({
            codeClasse: classe.codeClasse || '',
            nom: classe.nom || '',
            niveau: classe.niveau || '',
            anneeUniversitaire: classe.anneeUniversitaire || '',
            effectifMax: classe.effectifMax || 30,
            codeFiliere: filiere.codeFiliere
        });
        setIsEditing(true);
        setCurrentClasse(classe);
        setModalOpen(true);
    };

    const handleSaveClasse = async () => {
        if (!formData.codeClasse || !formData.nom) {
            alert('Veuillez remplir tous les champs obligatoires');
            return;
        }

        setIsLoading(true);
        try {
            if (isEditing) {
                await apiClasses.modifier(currentClasse.codeClasse, formData);
            } else {
                await apiClasses.creer(formData);
            }

            await fetchClasses();
            setModalOpen(false);
            setFormData({
                codeClasse: '',
                nom: '',
                niveau: '',
                anneeUniversitaire: '',
                effectifMax: 30,
                codeFiliere: filiere.codeFiliere
            });
        } catch (err) {
            console.error('Save failed:', err);
            alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteClasse = async (classe) => {
        if (!window.confirm(`Êtes-vous sûr de vouloir supprimer la classe "${classe.nom}" ? Cette action est irréversible.`)) {
            return;
        }

        setIsLoading(true);
        try {
            await apiClasses.supprimer(classe.codeClasse);
            setClasses(prev => prev.filter(c => c.codeClasse !== classe.codeClasse));
        } catch (err) {
            console.error('Delete failed:', err);
            alert('Erreur lors de la suppression: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const getElementCount = (classe) => {
        return classe.nombreModules || classe.nombreElements || 0;
    };

    // If a classe is selected, show modules view
    if (selectedClasse) {
        return (
            <DetailsClasse
                classe={selectedClasse}
                filiere={filiere}
                departement={departement}
                onRetour={() => setSelectedClasse(null)}
            />
        );
    }

    return (
        <div className="details-filiere-page">
            {/* Breadcrumbs */}
            <div className="details-breadcrumbs">
                <button className="breadcrumb-item" onClick={onRetour}>
                    <Home size={14} />
                    ÉCOLE
                </button>
                <span className="breadcrumb-separator">›</span>
                <button className="breadcrumb-item" onClick={onRetour}>
                    <Building2 size={14} />
                    {departement?.typeDepartement?.toUpperCase() || 'DÉPARTEMENT'}
                </button>
                <span className="breadcrumb-separator">›</span>
                <div className="breadcrumb-item breadcrumb-active">
                    <GraduationCap size={14} />
                    {filiere.nom?.toUpperCase()}
                </div>
            </div>

            {/* Header Banner */}
            <div className="details-header-banner modern-banner">
                <div className="banner-top">
                    <div className="banner-badge">FILIÈRE</div>
                    <h1 className="banner-title">
                        {filiere.nom}
                    </h1>
                </div>

                <div className="banner-stats-container">
                    <div className="stat-item">
                        <div className="stat-header">
                            <Hash size={16} />
                            CODE
                        </div>
                        <div className="stat-value">{filiere.codeFiliere || 'N/A'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <User size={16} />
                            CHEF FILIÈRE
                        </div>
                        <div className="stat-value">{filiere.chefFiliereNom || 'Non défini'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Award size={16} />
                            NIVEAU DIPLÔME
                        </div>
                        <div className="stat-value">{filiere.niveauDiplome || 'Licence Pro'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Clock size={16} />
                            DURÉE
                        </div>
                        <div className="stat-value">{filiere.dureeFormation || 3} Ans</div>
                    </div>
                </div>
            </div>

            {/* Classes Grid */}
            <div className="details-content">
                <div className="classes-grid">
                    {isLoading && classes.length === 0 ? (
                        <div className="classes-loading">Chargement...</div>
                    ) : error ? (
                        <div className="classes-error">{error}</div>
                    ) : (
                        <>
                            {/* Class Cards */}
                            {classes.map((classe) => (
                                <div
                                    key={classe.codeClasse || classe.id}
                                    className="classe-card"
                                    onClick={() => setSelectedClasse(classe)}
                                >
                                    {/* Icon */}
                                    <div className="classe-card-icon">
                                        <Layers size={28} />
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="classe-card-actions">
                                        <button
                                            className="classe-action-btn classe-edit-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleEditClasse(classe);
                                            }}
                                            title="Modifier"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            className="classe-action-btn classe-delete-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleDeleteClasse(classe);
                                            }}
                                            title="Supprimer"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>

                                    {/* Class Name */}
                                    <div className="classe-card-content">
                                        <h3 className="classe-card-title">
                                            {classe.nom}
                                        </h3>
                                    </div>

                                    {/* Footer */}
                                    <div className="classe-card-footer">
                                        <span className="classe-card-count">
                                            {getElementCount(classe)} ÉLÉMENTS
                                        </span>
                                        <ChevronRight size={20} className="classe-card-chevron" />
                                    </div>
                                </div>
                            ))}

                            {/* Add Card */}
                            <div className="classe-card classe-add-card" onClick={handleOpenAddModal}>
                                <div className="classe-add-content">
                                    <div className="classe-add-icon">
                                        <Plus size={32} />
                                    </div>
                                    <p className="classe-add-text">AJOUTER UN ÉLÉMENT</p>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Add/Edit Modal */}
            <FormulaireModal
                titre={isEditing ? "Modifier la Classe" : "Ajouter une Classe"}
                estOuvert={modalOpen}
                surFermer={() => setModalOpen(false)}
                surSauvegarder={handleSaveClasse}
                texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
            >
                <ChampFormulaire
                    label="Code Classe"
                    nom="codeClasse"
                    valeur={formData.codeClasse}
                    onChange={handleInputChange}
                    placeholder="Ex: ILCS-L3-GA"
                    requis
                    disabled={isEditing}
                />
                <ChampFormulaire
                    label="Nom"
                    nom="nom"
                    valeur={formData.nom}
                    onChange={handleInputChange}
                    placeholder="Ex: L3 Génie Logiciel - Groupe A"
                    requis
                />
                <ChampFormulaire
                    label="Niveau"
                    nom="niveau"
                    type="select"
                    valeur={formData.niveau}
                    onChange={handleInputChange}
                    options={[
                        { value: '', label: 'Sélectionner un niveau' },
                        { value: 'L1', label: 'L1 - Licence 1ère année' },
                        { value: 'L2', label: 'L2 - Licence 2ème année' },
                        { value: 'L3', label: 'L3 - Licence 3ème année' },
                        { value: 'M1', label: 'M1 - Master 1ère année' },
                        { value: 'M2', label: 'M2 - Master 2ème année' },
                        { value: 'D1', label: 'D1 - Doctorat 1ère année' },
                        { value: 'D2', label: 'D2 - Doctorat 2ème année' },
                        { value: 'D3', label: 'D3 - Doctorat 3ème année' }
                    ]}
                    requis
                />
                <ChampFormulaire
                    label="Année Universitaire"
                    nom="anneeUniversitaire"
                    valeur={formData.anneeUniversitaire}
                    onChange={handleInputChange}
                    placeholder="Ex: 2023-2024"
                    requis
                />
                <ChampFormulaire
                    label="Effectif Maximum"
                    nom="effectifMax"
                    type="number"
                    valeur={formData.effectifMax}
                    onChange={handleInputChange}
                    min="1"
                    max="200"
                    placeholder="Ex: 30"
                    requis
                />
            </FormulaireModal>
        </div>
    );
};

export default DetailsFiliere;
