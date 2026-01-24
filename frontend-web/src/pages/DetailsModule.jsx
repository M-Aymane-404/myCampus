import { useState, useEffect } from 'react';
import { Library, Edit2, Trash2, Plus, ChevronRight, Home, Building2, GraduationCap, Layers, BookOpen, Edit, FileText } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiMatieres } from '../services/apiMatieres';
import { apiProfesseurs } from '../services/apiProfesseurs';
import './DetailsModule.css';

const DetailsModule = ({ module, classe, filiere, departement, onRetour }) => {
    const [matieres, setMatieres] = useState([]);
    const [professeurs, setProfesseurs] = useState([]);
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

    // Fetch matiéres and professeurs on mount
    useEffect(() => {
        fetchData();
    }, [module]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [matieresData, professeursData] = await Promise.all([
                apiMatieres.obtenirParModule(module.codeModule),
                apiProfesseurs.obtenirTous().catch(() => [])
            ]);
            setMatieres(matieresData);
            setProfesseurs(professeursData);
            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Impossible de charger les matières.');
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
            <div className="details-header-banner">
                <div className="details-header-content">
                    <div className="details-header-info">
                        <div className="details-header-badge">MODULE</div>
                        <div className="details-header-id">ID: {module.id || module.codeModule}</div>
                        <h1 className="details-header-title">
                            {module.nom}
                        </h1>
                        <p className="details-header-description">
                            Informations détaillées sur cet élément structurel. Utilisez l'explorateur pour naviguer dans les sous-catégories.
                        </p>
                    </div>

                </div>
            </div>

            {/* Section Title & Add Button */}
            <div className="matieres-section-header">
                <h2 className="matieres-title">Matières du Module</h2>
                <button className="btn-nouvelle-matiere" onClick={handleOpenAddModal}>
                    <Plus size={18} />
                    Nouvelle Matière
                </button>
            </div>

            {/* Matières List (Rows) */}
            <div className="matieres-list">
                {isLoading && matieres.length === 0 ? (
                    <div className="matieres-loading">Chargement...</div>
                ) : error ? (
                    <div className="matieres-error">{error}</div>
                ) : matieres.length === 0 ? (
                    <div className="matieres-empty">Aucune matière trouvée dans ce module.</div>
                ) : (
                    matieres.map((matiere) => (
                        <div key={matiere.codeMatiere || matiere.id} className="matiere-row">
                            <div className="matiere-row-left">
                                <div className="matiere-icon-container">
                                    <Library size={24} className="matiere-icon" />
                                </div>
                                <div className="matiere-info">
                                    <h3 className="matiere-name">{matiere.nom}</h3>
                                    <p className="matiere-label">MATIÈRE ACADÉMIQUE</p>
                                </div>
                            </div>
                            <div className="matiere-row-right">
                                <div className="matiere-actions">
                                    <button
                                        className="matiere-action-btn matiere-edit-btn"
                                        onClick={() => handleEditMatiere(matiere)}
                                        title="Modifier"
                                    >
                                        <Edit2 size={16} />
                                    </button>
                                    <button
                                        className="matiere-action-btn matiere-delete-btn"
                                        onClick={() => handleDeleteMatiere(matiere)}
                                        title="Supprimer"
                                    >
                                        <Trash2 size={16} />
                                    </button>
                                </div>
                            </div>
                        </div>
                    ))
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
                    label="Professeur"
                    nom="pprProfesseur"
                    type="select"
                    valeur={formData.pprProfesseur}
                    onChange={handleInputChange}
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
        </div>
    );
};

export default DetailsModule;
