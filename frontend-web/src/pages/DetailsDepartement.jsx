import { useState, useEffect } from 'react';
import { GraduationCap, Edit2, Trash2, Plus, ChevronRight, Home, Building2, Edit, FileText, Hash, User, Tag, Layers as LayersIcon } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import DetailsFiliere from './DetailsFiliere';
import './DetailsDepartement.css';
import './ModernAcademicRedesign.css';

const DetailsDepartement = ({ departement, onRetour }) => {
    const [filieres, setFilieres] = useState([]);
    const [classes, setClasses] = useState([]);
    const [professeurs, setProfesseurs] = useState([]);
    const [isLoading, setIsLoading] = useState(true);
    const [error, setError] = useState(null);

    // Modal states
    const [modalOpen, setModalOpen] = useState(false);
    const [isEditing, setIsEditing] = useState(false);
    const [currentFiliere, setCurrentFiliere] = useState(null);

    // Navigation state
    const [selectedFiliere, setSelectedFiliere] = useState(null);

    // Form data
    const [formData, setFormData] = useState({
        codeFiliere: '',
        nom: '',
        description: '',
        chefFilierePpr: '',
        dureeFormation: 3,
        niveauDiplome: 'Licence',
        etat: 'OUVERTE',
        codeDepartement: departement?.codeDepartement || ''
    });

    // Fetch filières and professeurs on mount
    useEffect(() => {
        fetchData();
    }, [departement]);

    const fetchData = async () => {
        setIsLoading(true);
        try {
            const [filieresData, professeursData, classesData] = await Promise.all([
                apiStructure.obtenirFilieresParDepartement(departement.codeDepartement).catch(() =>
                    apiStructure.obtenirToutesFilieres().then(all =>
                        all.filter(f => f.codeDepartement === departement.codeDepartement)
                    )
                ),
                apiProfesseurs.obtenirTous().catch(() => []),
                apiStructure.obtenirToutesClasses().catch(() => [])
            ]);

            setFilieres(filieresData);
            setProfesseurs(professeursData);
            setClasses(classesData);

            setError(null);
        } catch (err) {
            console.error('Failed to fetch data:', err);
            setError('Impossible de charger les données.');
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
            codeFiliere: '',
            nom: '',
            description: '',
            chefFilierePpr: '',
            dureeFormation: 3,
            niveauDiplome: 'Licence',
            etat: 'OUVERTE',
            codeDepartement: departement.codeDepartement
        });
        setIsEditing(false);
        setCurrentFiliere(null);
        setModalOpen(true);
    };

    const handleEditFiliere = (filiere) => {
        setFormData({
            codeFiliere: filiere.codeFiliere || '',
            nom: filiere.nom || '',
            description: filiere.description || '',
            chefFilierePpr: filiere.chefFilierePpr || '',
            dureeFormation: filiere.dureeFormation || 3,
            niveauDiplome: filiere.niveauDiplome || 'Licence',
            etat: filiere.etat || 'OUVERTE',
            codeDepartement: departement.codeDepartement
        });
        setIsEditing(true);
        setCurrentFiliere(filiere);
        setModalOpen(true);
    };

    const handleSaveFiliere = async () => {
        if (!formData.codeFiliere || !formData.nom) {
            alert('Veuillez remplir tous les champs obligatoires');
            return;
        }

        setIsLoading(true);
        try {
            if (isEditing) {
                await apiStructure.modifierFiliere(currentFiliere.codeFiliere, formData);
            } else {
                await apiStructure.creerFiliere(formData);
            }

            await fetchData();
            setModalOpen(false);
            setFormData({
                codeFiliere: '',
                nom: '',
                description: '',
                chefFilierePpr: '',
                dureeFormation: 3,
                niveauDiplome: 'Licence',
                etat: 'OUVERTE',
                codeDepartement: departement.codeDepartement
            });
        } catch (err) {
            console.error('Save failed:', err);
            alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const handleDeleteFiliere = async (filiere) => {
        if (!window.confirm(`Êtes-vous sûr de vouloir supprimer la filière "${filiere.nom}" ? Cette action est irréversible.`)) {
            return;
        }

        setIsLoading(true);
        try {
            await apiStructure.supprimerFiliere(filiere.codeFiliere);
            setFilieres(prev => prev.filter(f => f.codeFiliere !== filiere.codeFiliere));
        } catch (err) {
            console.error('Delete failed:', err);
            alert('Erreur lors de la suppression: ' + err.message);
        } finally {
            setIsLoading(false);
        }
    };

    const getElementCount = (filiere) => {
        // Count classes that belong to this filiere
        return classes.filter(c => c.codeFiliere === filiere.codeFiliere).length;
    };

    const getProfesseurNom = (ppr) => {
        const prof = professeurs.find(p => p.ppr === ppr);
        return prof ? `${prof.prenom} ${prof.nom}` : ppr || 'Non défini';
    };

    // If a filière is selected, show classes view
    if (selectedFiliere) {
        return (
            <DetailsFiliere
                filiere={selectedFiliere}
                departement={departement}
                onRetour={() => setSelectedFiliere(null)}
            />
        );
    }

    return (
        <div className="details-departement-page">
            {/* Breadcrumbs */}
            <div className="details-breadcrumbs">
                <button className="breadcrumb-item" onClick={onRetour}>
                    <Home size={14} />
                    ÉCOLE
                </button>
                <span className="breadcrumb-separator">›</span>
                <div className="breadcrumb-item breadcrumb-active">
                    <Building2 size={14} />
                    {departement.typeDepartement?.toUpperCase() || departement.codeDepartement}
                </div>
            </div>

            {/* Header Banner */}
            <div className="details-header-banner modern-banner">
                <div className="banner-top">
                    <div className="banner-badge">DÉPARTEMENT</div>
                    <h1 className="banner-title">
                        {departement.typeDepartement || departement.codeDepartement}
                    </h1>
                </div>

                <div className="banner-stats-container">
                    <div className="stat-item">
                        <div className="stat-header">
                            <Hash size={16} />
                            CODE
                        </div>
                        <div className="stat-value">{departement.codeDepartement || 'N/A'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <User size={16} />
                            CHEF DEPT
                        </div>
                        <div className="stat-value">{getProfesseurNom(departement.chefDepartementPpr)}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <Tag size={16} />
                            TYPE
                        </div>
                        <div className="stat-value">{departement.description || 'Scientifique'}</div>
                    </div>
                    <div className="stat-item">
                        <div className="stat-header">
                            <LayersIcon size={16} />
                            FILIÈRES
                        </div>
                        <div className="stat-value">{filieres.length}</div>
                    </div>
                </div>
            </div>

            {/* Filières Grid */}
            <div className="details-content">
                <div className="filieres-grid">
                    {isLoading && filieres.length === 0 ? (
                        <div className="filieres-loading">Chargement...</div>
                    ) : error ? (
                        <div className="filieres-error">{error}</div>
                    ) : (
                        <>
                            {/* Filière Cards */}
                            {filieres.map((filiere) => (
                                <div
                                    key={filiere.codeFiliere || filiere.id}
                                    className="filiere-card"
                                    onClick={() => setSelectedFiliere(filiere)}
                                >
                                    {/* Icon */}
                                    <div className="filiere-card-icon">
                                        <GraduationCap size={28} />
                                    </div>

                                    {/* Action Buttons */}
                                    <div className="filiere-card-actions">
                                        <button
                                            className="filiere-action-btn filiere-edit-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleEditFiliere(filiere);
                                            }}
                                            title="Modifier"
                                        >
                                            <Edit2 size={16} />
                                        </button>
                                        <button
                                            className="filiere-action-btn filiere-delete-btn"
                                            onClick={(e) => {
                                                e.stopPropagation();
                                                handleDeleteFiliere(filiere);
                                            }}
                                            title="Supprimer"
                                        >
                                            <Trash2 size={16} />
                                        </button>
                                    </div>

                                    {/* Filière Name */}
                                    <div className="filiere-card-content">
                                        <h3 className="filiere-card-title">
                                            {filiere.nom}
                                        </h3>
                                    </div>

                                    {/* Footer */}
                                    <div className="filiere-card-footer">
                                        <span className="filiere-card-count">
                                            {getElementCount(filiere)} ÉLÉMENTS
                                        </span>
                                        <ChevronRight size={20} className="filiere-card-chevron" />
                                    </div>
                                </div>
                            ))}

                            {/* Add Card */}
                            <div className="filiere-card filiere-add-card" onClick={handleOpenAddModal}>
                                <div className="filiere-add-content">
                                    <div className="filiere-add-icon">
                                        <Plus size={32} />
                                    </div>
                                    <p className="filiere-add-text">AJOUTER UN ÉLÉMENT</p>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>

            {/* Add/Edit Modal */}
            <FormulaireModal
                titre={isEditing ? "Modifier la Filière" : "Ajouter une Filière"}
                estOuvert={modalOpen}
                surFermer={() => setModalOpen(false)}
                surSauvegarder={handleSaveFiliere}
                texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
            >
                <ChampFormulaire
                    label="Code Filière"
                    nom="codeFiliere"
                    valeur={formData.codeFiliere}
                    onChange={handleInputChange}
                    placeholder="Ex: FIL-INFO-GL"
                    requis
                    disabled={isEditing}
                />
                <ChampFormulaire
                    label="Nom"
                    nom="nom"
                    valeur={formData.nom}
                    onChange={handleInputChange}
                    placeholder="Ex: Génie Logiciel"
                    requis
                />
                <ChampFormulaire
                    label="Description"
                    nom="description"
                    type="textarea"
                    valeur={formData.description}
                    onChange={handleInputChange}
                    placeholder="Description de la filière"
                />
                <ChampFormulaire
                    label="Chef de Filière"
                    nom="chefFilierePpr"
                    type="select"
                    valeur={formData.chefFilierePpr}
                    onChange={handleInputChange}
                    options={[
                        { value: '', label: 'Sélectionner un professeur' },
                        ...professeurs.map(p => ({
                            value: p.ppr,
                            label: `${p.prenom} ${p.nom} (${p.ppr})`
                        }))
                    ]}
                />
                <ChampFormulaire
                    label="Durée de Formation (années)"
                    nom="dureeFormation"
                    type="number"
                    valeur={formData.dureeFormation}
                    onChange={handleInputChange}
                    min="1"
                    max="10"
                    requis
                />
                <ChampFormulaire
                    label="Niveau de Diplôme"
                    nom="niveauDiplome"
                    type="select"
                    valeur={formData.niveauDiplome}
                    onChange={handleInputChange}
                    options={[
                        { value: 'Licence', label: 'Licence' },
                        { value: 'Master', label: 'Master' },
                        { value: 'Doctorat', label: 'Doctorat' },
                        { value: 'DUT', label: 'DUT' },
                        { value: 'Ingénieur', label: 'Ingénieur' }
                    ]}
                    requis
                />
                <ChampFormulaire
                    label="État"
                    nom="etat"
                    type="select"
                    valeur={formData.etat}
                    onChange={handleInputChange}
                    options={[
                        { value: 'OUVERTE', label: 'Ouverte' },
                        { value: 'FERMEE', label: 'Fermée' }
                    ]}
                    requis
                />
            </FormulaireModal>
        </div>
    );
};

export default DetailsDepartement;
