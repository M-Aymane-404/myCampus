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
            const [matieresData, professeursData, affectationsData] = await Promise.all([
                apiMatieres.obtenirParModule(module.codeModule),
                apiProfesseurs.obtenirTous().catch(() => []),
                apiAffectations.obtenirParClasse(classe?.id || classe?.codeClasse).catch(() => [])
            ]);
            setMatieres(matieresData);
            setProfesseurs(professeursData);
            setAffectations(affectationsData);
            console.log('DEBUG - Matieres:', matieresData);
            console.log('DEBUG - Affectations for Classe:', affectationsData);
            console.log('DEBUG - Professeurs:', professeursData);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Impossible de charger les matières.');
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
        const existingAff = affectations.find(a => String(a.matiere_id || a.matiereId) === String(matiere.id));

        setAffectationFormData({
            professeurId: existingAff?.professeur_id || existingAff?.professeurId || '',
            active: (existingAff?.active === 1 || existingAff?.active === '1' || existingAff?.active === true) ? 'OUI' : 'NON',
            anneeUniversitaire: existingAff?.annee_universitaire || existingAff?.anneeUniversitaire || '2025/2026',
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
            const payload = {
                professeurId: parseInt(affectationFormData.professeurId),
                matiereId: parseInt(currentMatiere.id),
                classeId: parseInt(classe?.id || classe?.codeClasse),
                anneeUniversitaire: affectationFormData.anneeUniversitaire,
                semestre: affectationFormData.semestre,
                active: affectationFormData.active === 'OUI' ? 1 : 0
            };

            console.log('DEBUG - Sending Payload:', payload);
            await apiAffectations.assigner(payload);

            await fetchData();
            setAffectationModalOpen(false);
            alert('Affectation enregistrée avec succès');
        } catch (err) {
            console.error('Assignment failed:', err);
            alert('Erreur lors de l\'affectation: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const getMatiereProfessor = (matiere) => {
        if (!matiere) return 'NON DÉFINI';

        const mId = String(matiere.id || '');
        const mCode = String(matiere.codeMatiere || '');
        const targetClasseId = String(classe?.id || '');

        // Find assignment for this matiere and the current class
        // Use both ID and code as fallback just in case
        const aff = affectations.find(a =>
            (String(a.matiere_id || a.matiereId) === mId || String(a.matiere_id || a.matiereId) === mCode) &&
            (String(a.classe_id || a.classeId) === targetClasseId)
        );

        if (!aff) return 'NON DÉFINI';

        const profId = String(aff.professeur_id || aff.professeurId || '');
        const prof = professeurs.find(p =>
            String(p.id || '') === profId ||
            String(p.ppr || '') === profId
        );

        return prof ? `PR. ${prof.prenom.toUpperCase()} ${prof.nom.toUpperCase()}` : `PROF ID: ${profId}`;
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
                            <Award size={16} />
                            COEFFICIENT
                        </div>
                        <div className="stat-value">{module.coefficient || 4}</div>
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
                                        CODE: {matiere.codeMatiere} • COEFF: {matiere.coefficient}
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
                            value: p.id || p.ppr,
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
                <ChampFormulaire
                    label="Année Universitaire"
                    nom="anneeUniversitaire"
                    valeur={affectationFormData.anneeUniversitaire}
                    onChange={handleAffectationInputChange}
                    placeholder="Ex: 2023-2024"
                    requis
                />
                <ChampFormulaire
                    label="Semestre"
                    nom="semestre"
                    type="select"
                    valeur={affectationFormData.semestre}
                    onChange={handleAffectationInputChange}
                    options={[
                        { value: 'S1', label: 'Semestre 1' },
                        { value: 'S2', label: 'Semestre 2' },
                        { value: 'S3', label: 'Semestre 3' },
                        { value: 'S4', label: 'Semestre 4' },
                        { value: 'S5', label: 'Semestre 5' },
                        { value: 'S6', label: 'Semestre 6' },
                        { value: 'S7', label: 'Semestre 7' },
                        { value: 'S8', label: 'Semestre 8' }
                    ]}
                    requis
                />
            </FormulaireModal>
        </div>
    );
};

export default DetailsModule;
