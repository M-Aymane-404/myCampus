import { useState, useEffect } from 'react';
import { Building2, Edit2, Trash2, Plus, ChevronRight, Home } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiStructure } from '../services/apiStructure';
import { apiProfesseurs } from '../services/apiProfesseurs';
import DetailsDepartement from './DetailsDepartement';
import './GestionDepartements.css';

const GestionDepartements = () => {
  const [departements, setDepartements] = useState([]);
  const [professeurs, setProfesseurs] = useState([]);
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
    description: '',
    chefDepartementPpr: '',
    etat: 'OUVERTE'
  });

  // Fetch departments on mount
  useEffect(() => {
    fetchDepartements();
    fetchProfesseurs();
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

  const fetchProfesseurs = async () => {
    try {
      const data = await apiProfesseurs.obtenirTous();
      setProfesseurs(data);
    } catch (err) {
      console.error('Failed to fetch professors:', err);
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
      description: '',
      chefDepartementPpr: '',
      etat: 'OUVERTE'
    });
    setIsEditing(false);
    setCurrentDepartement(null);
    setModalOpen(true);
  };

  const handleEditDepartement = (dept) => {
    // Backend might return typeDepartement or nom depending on the version/endpoint
    const typeValue = dept.typeDepartement || dept.nom || '';

    setFormData({
      codeDepartement: dept.codeDepartement || '',
      typeDepartement: typeValue,
      description: dept.description || '',
      chefDepartementPpr: dept.chefDepartementPpr || '',
      etat: dept.etat || 'OUVERTE'
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
        const payload = {
          typeDepartement: formData.typeDepartement,
          description: formData.description,
          chefDepartementPpr: formData.chefDepartementPpr,
          etat: formData.etat
        };
        await apiStructure.modifierDepartement(currentDepartement.codeDepartement, payload);
      } else {
        await apiStructure.creerDepartement(formData);
      }

      await fetchDepartements();
      setModalOpen(false);
      setFormData({
        codeDepartement: '',
        typeDepartement: '',
        description: '',
        chefDepartementPpr: '',
        etat: 'OUVERTE'
      });
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
    // Check all possible count fields from the backend
    return dept.nbFilieres || dept.nombreFilieres || dept.nombreElements || 0;
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
        {!isEditing && (
          <ChampFormulaire
            label="Code Département"
            nom="codeDepartement"
            valeur={formData.codeDepartement}
            onChange={handleInputChange}
            placeholder="Ex: DEPT-INFO"
            requis
          />
        )}
        <ChampFormulaire
          label="Type de Département"
          nom="typeDepartement"
          valeur={formData.typeDepartement}
          onChange={handleInputChange}
          placeholder="Ex: Informatique"
          requis
        />
        <ChampFormulaire
          label="Chef de Département"
          nom="chefDepartementPpr"
          type="select"
          valeur={formData.chefDepartementPpr}
          onChange={handleInputChange}
          options={professeurs.map(p => ({
            value: p.ppr,
            label: `${p.nom} ${p.prenom} (${p.ppr})`
          }))}
        />
        <ChampFormulaire
          label="État"
          nom="etat"
          type="select"
          valeur={formData.etat}
          onChange={handleInputChange}
          options={[
            { value: 'OUVERTE', label: 'OUVERTE' },
            { value: 'FERMEE', label: 'FERMÉE' }
          ]}
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
