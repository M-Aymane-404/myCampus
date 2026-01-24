import { useState, useEffect } from 'react';
import { Building2, Edit2, Trash2, Plus, ChevronRight, Home } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiStructure } from '../services/apiStructure';
import DetailsDepartement from './DetailsDepartement';
import './GestionDepartements.css';

const GestionDepartements = () => {
  const [departements, setDepartements] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // Modal states
  const [modalOpen, setModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [currentDepartement, setCurrentDepartement] = useState(null);

  // Navigation state
  const [selectedDepartement, setSelectedDepartement] = useState(null);

  // Form data
  const [formData, setFormData] = useState({
    codeDepartement: '',
    typeDepartement: '',
    description: ''
  });

  // Fetch departments on mount
  useEffect(() => {
    fetchDepartements();
  }, []);

  const fetchDepartements = async () => {
    setIsLoading(true);
    try {
      const data = await apiStructure.obtenirTousDepartements();
      setDepartements(data);
      setError(null);
    } catch (err) {
      console.error('Failed to fetch departments:', err);
      setError('Impossible de charger les départements.');
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
      codeDepartement: '',
      typeDepartement: '',
      description: ''
    });
    setIsEditing(false);
    setCurrentDepartement(null);
    setModalOpen(true);
  };

  const handleEditDepartement = (dept) => {
    setFormData({
      codeDepartement: dept.codeDepartement || '',
      typeDepartement: dept.typeDepartement || '',
      description: dept.description || ''
    });
    setIsEditing(true);
    setCurrentDepartement(dept);
    setModalOpen(true);
  };

  const handleSaveDepartement = async () => {
    if (!formData.codeDepartement || !formData.typeDepartement) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    setIsLoading(true);
    try {
      if (isEditing) {
        await apiStructure.modifierDepartement(currentDepartement.codeDepartement, formData);
      } else {
        await apiStructure.creerDepartement(formData);
      }

      await fetchDepartements();
      setModalOpen(false);
      setFormData({ codeDepartement: '', typeDepartement: '', description: '' });
    } catch (err) {
      console.error('Save failed:', err);
      alert('Une erreur est survenue lors de la sauvegarde: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  const handleDeleteDepartement = async (dept) => {
    if (!window.confirm(`Êtes-vous sûr de vouloir supprimer le département "${dept.typeDepartement}" ? Cette action est irréversible.`)) {
      return;
    }

    setIsLoading(true);
    try {
      await apiStructure.supprimerDepartement(dept.codeDepartement);
      setDepartements(prev => prev.filter(d => d.codeDepartement !== dept.codeDepartement));
    } catch (err) {
      console.error('Delete failed:', err);
      alert('Erreur lors de la suppression: ' + err.message);
    } finally {
      setIsLoading(false);
    }
  };

  // Calculate element count (you can customize this based on your needs)
  const getElementCount = (dept) => {
    // This could be number of filieres, classes, etc.
    return dept.nombreFilieres || dept.nombreElements || 0;
  };

  // If a department is selected, show details view
  if (selectedDepartement) {
    return (
      <DetailsDepartement
        departement={selectedDepartement}
        onRetour={() => setSelectedDepartement(null)}
      />
    );
  }

  return (
    <div className="gestion-departements-page">
      {/* Header Badge */}
      <div className="departements-badge-container">
        <div className="departements-badge">
          <Home size={16} />
          ÉCOLE
        </div>
      </div>

      {/* Departments Grid */}
      <div className="departements-grid">
        {isLoading && departements.length === 0 ? (
          <div className="departements-loading">Chargement...</div>
        ) : error ? (
          <div className="departements-error">{error}</div>
        ) : (
          <>
            {/* Department Cards */}
            {departements.map((dept) => (
              <div
                key={dept.codeDepartement || dept.id}
                className="departement-card"
                onClick={() => setSelectedDepartement(dept)}
              >
                {/* Icon */}
                <div className="departement-card-icon">
                  <Building2 size={28} />
                </div>

                {/* Action Buttons */}
                <div className="departement-card-actions">
                  <button
                    className="departement-action-btn departement-edit-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleEditDepartement(dept);
                    }}
                    title="Modifier"
                  >
                    <Edit2 size={16} />
                  </button>
                  <button
                    className="departement-action-btn departement-delete-btn"
                    onClick={(e) => {
                      e.stopPropagation();
                      handleDeleteDepartement(dept);
                    }}
                    title="Supprimer"
                  >
                    <Trash2 size={16} />
                  </button>
                </div>

                {/* Department Name */}
                <div className="departement-card-content">
                  <h3 className="departement-card-title">
                    {dept.typeDepartement || dept.nom || dept.codeDepartement}
                  </h3>
                  <p className="departement-card-label">DÉPARTEMENT</p>
                </div>

                {/* Footer */}
                <div className="departement-card-footer">
                  <span className="departement-card-count">
                    {getElementCount(dept)} ÉLÉMENTS
                  </span>
                  <ChevronRight size={20} className="departement-card-chevron" />
                </div>
              </div>
            ))}

            {/* Add Card */}
            <div className="departement-card departement-add-card" onClick={handleOpenAddModal}>
              <div className="departement-add-content">
                <div className="departement-add-icon">
                  <Plus size={32} />
                </div>
                <p className="departement-add-text">AJOUTER UN ÉLÉMENT</p>
              </div>
            </div>
          </>
        )}
      </div>

      {/* Add/Edit Modal */}
      <FormulaireModal
        titre={isEditing ? "Modifier le Département" : "Ajouter un Département"}
        estOuvert={modalOpen}
        surFermer={() => setModalOpen(false)}
        surSauvegarder={handleSaveDepartement}
        texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
      >
        <ChampFormulaire
          label="Code Département"
          nom="codeDepartement"
          valeur={formData.codeDepartement}
          onChange={handleInputChange}
          placeholder="Ex: DEPT-INFO"
          requis
          disabled={isEditing} // Code shouldn't change when editing
        />
        <ChampFormulaire
          label="Type de Département"
          nom="typeDepartement"
          valeur={formData.typeDepartement}
          onChange={handleInputChange}
          placeholder="Ex: Informatique"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formData.description}
          onChange={handleInputChange}
          placeholder="Description du département"
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionDepartements;
