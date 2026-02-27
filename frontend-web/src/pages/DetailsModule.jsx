import { useState, useEffect } from 'react';
import { Library, Edit2, Trash2, Plus, ChevronRight, Home, Building2, GraduationCap, Layers, BookOpen, Edit, FileText, Hash, Award, Calendar, UserPlus, Trash } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiMatieres } from '../services/apiMatieres';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAffectations } from '../services/apiAffectations';
import './DetailsModule.css';
import './ModernAcademicRedesign.css';

const DetailsModule = ({ module, classe, filiere, departement, onRetour }) => {
    const [matieres, setMatieres] = useState([]);
    const [professeurs, setProfesseurs] = useState([]);
    const [affectations, setAffectations] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal states
    const [modalOpen, setModalOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentMatiere, setCurrentMatiere] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        codeMatiere: '',
        nom: '',
        description: '',
        coefficient: 1.0,
        pprProfesseur: '',
        codeModule: module?.codeModule || ''
    });

    // Affectation Modal states
    const [affectationModalOpen, setAffectationModalOpen] = useState(false);
    const [affectationFormData, setAffectationFormData] = useState({
        professeurId: '',
        active: 'OUI',
        anneeUniversitaire: '2025/2026',
        semestre: module?.semestre || ''
    });

    // Fetch matiéres and professeurs on mount
    useEffect(() => {
        fetchData();
    }, [module]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            console.log('DEBUG - classe object:', classe);
            console.log('DEBUG - classe?.codeClasse for API call:', classe?.codeClasse);
            
            const [matieresData, professeursData, affectationsData] = await Promise.all([
                apiMatieres.obtenirParModuleEtClasse(module.codeModule, classe?.codeClasse),
                apiProfesseurs.obtenirTous().catch(() => []),
                apiAffectations.obtenirParClasse(classe?.codeClasse).catch(() => [])
            ]);
            setMatieres(matieresData);
            setProfesseurs(professeursData);
            setAffectations(affectationsData);
            console.log('DEBUG - Matieres with professor data:', matieresData);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            console.error('Error details:', err.message);
            
            // Try fallback without class filter if the combined call fails
            console.log('Attempting fallback without class filter...');
            try {
                const matieresData = await apiMatieres.obtenirParModule(module.codeModule);
                const professeursData = await apiProfesseurs.obtenirTous().catch(() => []);
                const affectationsData = await apiAffectations.obtenirToutes().catch(() => []);
                
                setMatieres(matieresData);
                setProfesseurs(professeursData);
                setAffectations(affectationsData);
                setError(null);
                console.log('Fallback successful - loaded subjects without class filter');
            } catch (fallbackErr) {
                console.error('Fallback also failed:', fallbackErr);
                setError('Impossible de charger les matières. Erreur: ' + err.message);
            }
        } finally {
            setIsLoading(false);
        }
    };

    const getProfesseurNom = (ppr) => {
        const prof = professeurs.find(p => p.ppr === ppr);
        return prof ? `PR. ${prof.prenom.toUpperCase()} ${prof.nom.toUpperCase()}` : ppr || 'NON DÉFINI';
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleOpenAddModal = () => {
        setFormData({
            codeMatiere: '',
            nom: '',
            description: '',
            coefficient: 1.0,
            pprProfesseur: '',
            codeModule: module.codeModule
        });
        setIsEditing(false);
        setCurrentMatiere(null);
        setModalOpen(true);
    };

    const handleEditMatiere = (matiere) => {
        setFormData({
            codeMatiere: matiere.codeMatiere || '',
            nom: matiere.nom || '',
            description: matiere.description || '',
            coefficient: matiere.coefficient || 1.0,
            pprProfesseur: matiere.pprProfesseur || matiere.professeurPpr || '',
            codeModule: module.codeModule
        });
        setIsEditing(true);
        setCurrentMatiere(matiere);
        setModalOpen(true);
    };

    const handleSaveMatiere = async () => {
        if (!formData.codeMatiere || !formData.nom) {
            alert('Veuillez remplir tous les champs obligatoires');
            return;
        }

        setIsLoading(true);
        try {
            if (isEditing) {
                await apiMatieres.modifier(currentMatiere.codeMatiere, formData);
            } else {
                await apiMatieres.creer(formData);
            }

            await fetchData();
            setModalOpen(false);
        } catch (err) {
            console.error('Save failed:', err);
            alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteMatiere = async (matiere) => {
        if (!window.confirm(`Êtes-vous sûr de vouloir supprimer la matière "${matiere.nom}" ? Cette action est irréversible.`)) {
            return;
        }

        setIsLoading(true);
        try {
            await apiMatieres.supprimer(matiere.codeMatiere);
            setMatieres(prev => prev.filter(m => m.codeMatiere !== matiere.codeMatiere));
        } catch (err) {
            console.error('Delete failed:', err);
            alert('Erreur lors de la suppression: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleOpenAffectationModal = (matiere) => {
        setCurrentMatiere(matiere);
        // Find existing assignment if any
        const existingAff = affectations.find(a => a.codeMatiere === matiere.codeMatiere);

        console.log('DEBUG - Existing assignment found:', existingAff);

        setAffectationFormData({
            professeurId: existingAff?.professeurPpr || '',
            active: existingAff?.active ? 'OUI' : 'NON',
            anneeUniversitaire: existingAff?.anneeUniversitaire || '2025/2026',
            semestre: existingAff?.semestre || module?.semestre || ''
        });
        setAffectationModalOpen(true);
    };

    const handleAffectationInputChange = (e) => {
        const { name, value } = e.target;
        setAffectationFormData(prev => ({ ...prev, [name]: value }));
    };

    const handleSaveAffectation = async () => {
        if (!affectationFormData.professeurId) {
            alert('Veuillez sélectionner un professeur');
            return;
        }

        setIsLoading(true);
        try {
            // Check if there's an existing assignment for this subject and class
            const existingAff = affectations.find(a => a.codeMatiere === currentMatiere.codeMatiere);
            
            if (existingAff) {
                // Update existing assignment using the PUT endpoint
                const updatePayload = {
                    professeurPpr: affectationFormData.professeurId,
                    active: affectationFormData.active === 'OUI'
                };

                console.log('DEBUG - Updating existing assignment:', existingAff.id, updatePayload);
                await apiAffectations.modifier(existingAff.id, updatePayload);
            } else {
                // Create new assignment
                const createPayload = {
                    professeurPpr: affectationFormData.professeurId,
                    codeMatiere: currentMatiere.codeMatiere,
                    codeClasse: classe?.codeClasse,
                    anneeUniversitaire: affectationFormData.anneeUniversitaire,
                    semestre: affectationFormData.semestre,
                    active: affectationFormData.active === 'OUI'
                };

                console.log('DEBUG - Creating new assignment:', createPayload);
                await apiAffectations.assigner(createPayload);
            }

            // Add a small delay before refreshing data to allow backend to process
            await new Promise(resolve => setTimeout(resolve, 300));
            
            await fetchData();
            setAffectationModalOpen(false);
            alert(`Affectation ${affectationFormData.active === 'OUI' ? 'activée' : 'désactivée'} avec succès`);
        } catch (err) {
            console.error('Assignment failed:', err);
            console.error('Error details:', {
                message: err.message,
                payload: {
                    professeurPpr: affectationFormData.professeurId,
                    codeMatiere: currentMatiere?.codeMatiere,
                    active: affectationFormData.active === 'OUI'
                }
            });
            
            // Provide more specific error messages
            let errorMessage = 'Erreur lors de l\'affectation: ';
            if (err.message.includes('404')) {
                errorMessage += 'Ressource non trouvée. Vérifiez que la matière et la classe existent.';
            } else if (err.message.includes('403')) {
                errorMessage += 'Vous n\'avez pas les permissions pour effectuer cette action.';
            } else if (err.message.includes('500')) {
                errorMessage += 'Erreur serveur. Veuillez réessayer plus tard.';
            } else {
                errorMessage += err.message;
            }
            
            alert(errorMessage);
        } finally {
            setIsLoading(false);
        }
    };

    const getMatiereProfessor = (matiere) => {
        if (!matiere) return 'NON DÉFINI';

        console.log('DEBUG - Getting professor for matiere:', matiere.codeMatiere);
        
        // Check if professor data is included in the matiere object
        if (matiere.professeur) {
            const prof = matiere.professeur;
            return `PR. ${prof.prenom.toUpperCase()} ${prof.nom.toUpperCase()}`;
        }
        
        // Check if professor name is directly available
        if (matiere.professeurNom) {
            return `PR. ${matiere.professeurNom}`;
        }
        
        // Check if professor PPR is available
        if (matiere.professeurPpr) {
            return `PR. ${matiere.professeurPpr}`;
        }
        
        return 'NON DÉFINI';
    };

    return (
        <div className="details-module-page">
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
                <button className="breadcrumb-item" onClick={onRetour}>
                    <Layers size={14} />
                    {classe?.nom?.toUpperCase() || 'CLASSE'}
                </button>
                <span className="breadcrumb-separator">›</span>
                <div className="breadcrumb-item breadcrumb-active">
                    <BookOpen size={14} />
                    {module.nom?.toUpperCase()}
                </div>
            </div>

            {/* Header Banner */}
            <div className="details-header-banner modern-banner">
                <div className="banner-top">
                    <div className="banner-badge">MODULE</div>
                    <h1 className="banner-title">
                        {module.nom}
                    </h1>
                </div>

                <div className="banner-stats-container">
                    <div className="stat-item">
                        <div className="stat-header">
                            <Hash size={16} />
                            CODE
                        </div>
                        <div className="stat-value">{module.codeModule || 'N/A'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Calendar size={16} />
                            SEMESTRE
                        </div>
                        <div className="stat-value">{module.semestre || 'S5'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <BookOpen size={16} />
                            SUJETS
                        </div>
                        <div className="stat-value">{matieres.length}</div>
                    </div>
                </div>
            </div>

            {/* Section Title & Add Button */}
            <div className="matieres-section-header">
                <h2 className="matieres-title">Matières du Module</h2>
            </div>

            {/* Matières List (Grid of Cards) */}
            <div className="matiere-grid">
                {isLoading && matieres.length === 0 ? (
                    <div className="matieres-loading">Chargement...</div>
                ) : error ? (
                    <div className="matieres-error">{error}</div>
                ) : (
                    <>
                        {matieres.map((matiere) => (
                            <div key={matiere.codeMatiere || matiere.id} className="matiere-card-modern">
                                <div className="card-top">
                                    <div className="card-icon-box">
                                        <Library size={28} />
                                    </div>
                                    <div className="card-actions">
                                        <button
                                            className="action-btn-circle btn-assign"
                                            title="Affecter un professeur"
                                            onClick={() => handleOpenAffectationModal(matiere)}
                                        >
                                            <UserPlus size={18} />
                                        </button>
                                        <button
                                            className="action-btn-circle btn-assign"
                                            title="Modifier la matière"
                                            onClick={() => handleEditMatiere(matiere)}
                                        >
                                            <Edit size={18} />
                                        </button>
                                        <button
                                            className="action-btn-circle btn-delete"
                                            title="Supprimer"
                                            onClick={() => handleDeleteMatiere(matiere)}
                                        >
                                            <Trash size={18} />
                                        </button>
                                    </div>
                                </div>

                                <div className="card-main">
                                    <h3>{matiere.nom}</h3>
                                    <div className="card-meta">
                                        CODE: {matiere.codeMatiere}
                                    </div>
                                    <div className="card-prof">
                                        <UserPlus size={14} />
                                        PROF: {getMatiereProfessor(matiere)}
                                    </div>
                                </div>
                            </div>
                        ))}

                        {/* New Subject Card */}
                        <div className="matiere-card-modern matiere-card-new" onClick={handleOpenAddModal}>
                            <div className="new-icon-circle">
                                <Plus size={24} />
                            </div>
                            <div className="new-text">NOUVEAU</div>
                        </div>
                    </>
                )}
            </div>

            {/* Add/Edit Modal */}
            <FormulaireModal
                titre={isEditing ? "Modifier la Matière" : "Ajouter une Matière"}
                estOuvert={modalOpen}
                surFermer={() => setModalOpen(false)}
                surSauvegarder={handleSaveMatiere}
                texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
            >
                <ChampFormulaire
                    label="Code Matière"
                    nom="codeMatiere"
                    valeur={formData.codeMatiere}
                    onChange={handleInputChange}
                    placeholder="Ex: MAT-REACT-JS"
                    requis
                    disabled={isEditing}
                />
                <ChampFormulaire
                    label="Nom"
                    nom="nom"
                    valeur={formData.nom}
                    onChange={handleInputChange}
                    placeholder="Ex: React.js & TypeScript"
                    requis
                />
                <ChampFormulaire
                    label="Description"
                    nom="description"
                    type="textarea"
                    valeur={formData.description}
                    onChange={handleInputChange}
                    placeholder="Description de la matière"
                />
            </FormulaireModal>

            {/* Affectation Modal */}
            <FormulaireModal
                titre={`Affecter un Professeur - ${currentMatiere?.nom}`}
                estOuvert={affectationModalOpen}
                surFermer={() => setAffectationModalOpen(false)}
                surSauvegarder={handleSaveAffectation}
                texteBoutonSauvegarder="Enregistrer l'Affectation"
            >
                <ChampFormulaire
                    label="Professeur"
                    nom="professeurId"
                    type="select"
                    valeur={affectationFormData.professeurId}
                    onChange={handleAffectationInputChange}
                    options={[
                        { value: '', label: 'Sélectionner un professeur' },
                        ...professeurs.map(p => ({
                            value: p.ppr,
                            label: `${p.prenom} ${p.nom} (${p.ppr})`
                        }))
                    ]}
                    requis
                />
                <ChampFormulaire
                    label="Actif"
                    nom="active"
                    type="select"
                    valeur={affectationFormData.active}
                    onChange={handleAffectationInputChange}
                    options={[
                        { value: 'OUI', label: 'Oui' },
                        { value: 'NON', label: 'Non' }
                    ]}
                    requis
                />
                
            </FormulaireModal>
        </div>
    );
};

export default DetailsModule;
