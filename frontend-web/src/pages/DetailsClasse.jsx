import { useState, useEffect } from 'react';
import { BookOpen, Edit2, Trash2, Plus, ChevronRight, Home, Building2, GraduationCap, Layers, Edit, FileText, Hash, Calendar, Users, GraduationCap as GraduationCapIcon } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiStructure } from '../services/apiStructure';
import DetailsModule from './DetailsModule';
import './DetailsClasse.css';
import './ModernAcademicRedesign.css';

const DetailsClasse = ({ classe, filiere, departement, onRetour }) => {
    const [modules, setModules] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal states
    const [modalOpen, setModalOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentModule, setCurrentModule] = useState(null);

    // Navigation state
    const [selectedModule, setSelectedModule] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        codeModule: '',
        nom: '',
        description: '',
        semestre: '',
        coefficient: 1.0,
        filiereId: filiere?.id || filiere?.codeFiliere || ''
    });

    // Fetch modules on mount
    useEffect(() => {
        fetchModules();
    }, [classe]);

    const fetchModules = async () => {
        if (!filiere) return;
        setIsLoading(true);
        try {
            // Utiliser l'ID de la filière comme suggéré par le schéma DB
            const filiereIdentifier = filiere.id || filiere.codeFiliere;
            const data = await apiStructure.obtenirModulesParFiliere(filiereIdentifier).catch(() =>
                apiStructure.obtenirTousModules().then(all =>
                    all.filter(m => m.filiereId === filiereIdentifier || m.codeFiliere === filiereIdentifier)
                )
            );

            setModules(data || []);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch modules:', err);
            setError('Impossible de charger les modules.');
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
            codeModule: '',
            nom: '',
            description: '',
            semestre: '',
            coefficient: 1.0,
            filiereId: filiere?.id || filiere?.codeFiliere || ''
        });
        setIsEditing(false);
        setCurrentModule(null);
        setModalOpen(true);
    };

    const handleEditModule = (module) => {
        setFormData({
            codeModule: module.codeModule || '',
            nom: module.nom || '',
            description: module.description || '',
            semestre: module.semestre || '',
            coefficient: module.coefficient || 1.0,
            filiereId: module.filiereId || filiere?.id || ''
        });
        setIsEditing(true);
        setCurrentModule(module);
        setModalOpen(true);
    };

    const handleSaveModule = async () => {
        if (!formData.codeModule || !formData.nom) {
            alert('Veuillez remplir tous les champs obligatoires');
            return;
        }

        setIsLoading(true);
        try {
            if (isEditing) {
                await apiStructure.modifierModule(currentModule.codeModule, formData);
            } else {
                await apiStructure.creerModule(formData);
            }

            await fetchModules();
            setModalOpen(false);
            setFormData({
                codeModule: '',
                nom: '',
                description: '',
                semestre: '',
                coefficient: 1.0,
                filiereId: filiere?.id || ''
            });
        } catch (err) {
            console.error('Save failed:', err);
            alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteModule = async (module) => {
        if (!window.confirm(`Êtes-vous sûr de vouloir supprimer le module "${module.nom}" ? Cette action est irréversible.`)) {
            return;
        }

        setIsLoading(true);
        try {
            await apiStructure.supprimerModule(module.codeModule);
            setModules(prev => prev.filter(m => m.codeModule !== module.codeModule));
        } catch (err) {
            console.error('Delete failed:', err);
            alert('Erreur lors de la suppression: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const getElementCount = (module) => {
        return module.nombreMatieres || module.nombreElements || 0;
    };

    // If a module is selected, show subjects view
    if (selectedModule) {
        return (
            <DetailsModule
                module={selectedModule}
                classe={classe}
                filiere={filiere}
                departement={departement}
                onRetour={() => setSelectedModule(null)}
            />
        );
    }

    return (
        <div className="details-classe-page">
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
                <button className="breadcrumb-item" onClick={onRetour}>
                    <GraduationCap size={14} />
                    {filiere?.nom?.toUpperCase() || 'FILIÈRE'}
                </button>
                <span className="breadcrumb-separator">›</span>
                <div className="breadcrumb-item breadcrumb-active">
                    <Layers size={14} />
                    {classe.nom?.toUpperCase()}
                </div>
            </div>

            {/* Header Banner */}
            <div className="details-header-banner modern-banner">
                <div className="banner-top">
                    <div className="banner-badge">CLASSE</div>
                    <h1 className="banner-title">
                        {classe.nom}
                    </h1>
                </div>

                <div className="banner-stats-container">
                    <div className="stat-item">
                        <div className="stat-header">
                            <Hash size={16} />
                            CODE
                        </div>
                        <div className="stat-value">{classe.codeClasse || 'N/A'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Calendar size={16} />
                            ANNÉE UNIV
                        </div>
                        <div className="stat-value">{classe.anneeUniversitaire || '2023/2024'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Users size={16} />
                            EFFECTIF MAX
                        </div>
                        <div className="stat-value">{classe.effectifMax || 35}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <GraduationCapIcon size={16} />
                            NIVEAU
                        </div>
                        <div className="stat-value">{classe.niveau || '3ème Année'}</div>
                    </div>
                </div>
            </div>

            {/* Modules Grid */}
            <div className="details-content">
                <div className="modules-grid">
                    {isLoading && modules.length === 0 ? (
                        <div className="modules-loading">Chargement...</div>
                    ) : error ? (
                        <div className="modules-error">{error}</div>
                    ) : modules.length === 0 ? (
                        <div className="modules-empty">
                            <p>Aucun module trouvé pour cette classe.</p>
                            <div className="module-card module-add-card" onClick={handleOpenAddModal}>
                                <div className="module-add-content">
                                    <div className="module-add-icon">
                                        <Plus size={32} />
                                    </div>
                                    <p className="module-add-text">AJOUTER LE PREMIER MODULE</p>
                                </div>
                            </div>
                        </div>
                    ) : (
                        <>
                            {/* Module Cards */}
                            {modules.map((module) => (
                                <div
                                    key={module.codeModule || module.id}
                                    className="module-card"
                                    onClick={() => setSelectedModule(module)}
                                >
                                    {/* Icon */}
                                    <div className="module-card-icon">
                                        <BookOpen size={28} />
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="module-card-actions">
                                        <button
                                            className="module-action-btn module-edit-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleEditModule(module);
                                            }}
                                            title="Modifier"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            className="module-action-btn module-delete-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleDeleteModule(module);
                                            }}
                                            title="Supprimer"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>

                                    {/* Module Name */}
                                    <div className="module-card-content">
                                        <h3 className="module-card-title">
                                            {module.nom}
                                        </h3>
                                    </div>

                                    {/* Footer */}
                                    <div className="module-card-footer">
                                        <span className="module-card-count">
                                            {getElementCount(module)} ÉLÉMENTS
                                        </span>
                                        <ChevronRight size={20} className="module-card-chevron" />
                                    </div>
                                </div>
                            ))}

                            {/* Add Card */}
                            <div className="module-card module-add-card" onClick={handleOpenAddModal}>
                                <div className="module-add-content">
                                    <div className="module-add-icon">
                                        <Plus size={32} />
                                    </div>
                                    <p className="module-add-text">AJOUTER UN ÉLÉMENT</p>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Add/Edit Modal */}
            <FormulaireModal
                titre={isEditing ? "Modifier le Module" : "Ajouter un Module"}
                estOuvert={modalOpen}
                surFermer={() => setModalOpen(false)}
                surSauvegarder={handleSaveModule}
                texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
            >
                <ChampFormulaire
                    label="Code Module"
                    nom="codeModule"
                    valeur={formData.codeModule}
                    onChange={handleInputChange}
                    placeholder="Ex: MOD-DEV-WEB"
                    requis
                    disabled={isEditing}
                />
                <ChampFormulaire
                    label="Nom"
                    nom="nom"
                    valeur={formData.nom}
                    onChange={handleInputChange}
                    placeholder="Ex: Développement Web"
                    requis
                />
                <ChampFormulaire
                    label="Description"
                    nom="description"
                    type="textarea"
                    valeur={formData.description}
                    onChange={handleInputChange}
                    placeholder="Description du module"
                />
                <ChampFormulaire
                    label="Semestre"
                    nom="semestre"
                    type="select"
                    valeur={formData.semestre}
                    onChange={handleInputChange}
                    options={[
                        { value: '', label: 'Sélectionner un semestre' },
                        { value: 'S1', label: 'Semestre 1' },
                        { value: 'S2', label: 'Semestre 2' },
                        { value: 'S3', label: 'Semestre 3' },
                        { value: 'S4', label: 'Semestre 4' },
                        { value: 'S5', label: 'Semestre 5' },
                        { value: 'S6', label: 'Semestre 6' }
                    ]}
                    requis
                />
                <ChampFormulaire
                    label="Coefficient"
                    nom="coefficient"
                    type="number"
                    step="0.5"
                    valeur={formData.coefficient}
                    onChange={handleInputChange}
                    min="0.5"
                    max="10"
                    placeholder="Ex: 2.5"
                    requis
                />
            </FormulaireModal>
        </div>
    );
};

export default DetailsClasse;
