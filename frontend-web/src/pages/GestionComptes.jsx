
import { useState, useEffect } from 'react';
import { Plus, Edit2, Trash2, CheckCircle, XCircle, Eye, Search, Loader } from 'lucide-react';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ModalDetailsUtilisateur from '../composants/communs/ModalDetailsUtilisateur';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import { apiComptes } from '../services/apiComptes';
import { apiEtudiants } from '../services/apiEtudiants';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAdministrateurs } from '../services/apiAdministrateurs';
import { apiClasses } from '../services/apiClasses';
import './GestionComptes.css';

const GestionComptes = () => {
  const [activeFilter, setActiveFilter] = useState('ALL');
  const [searchQuery, setSearchQuery] = useState('');
  const [modalOpen, setModalOpen] = useState(false);
  const [isEditing, setIsEditing] = useState(false);
  const [selectedUserId, setSelectedUserId] = useState(null);

  // Data State
  const [usersList, setUsersList] = useState([]);
  const [classesList, setClassesList] = useState([]);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState(null);

  // View Details State
  const [viewModalOpen, setViewModalOpen] = useState(false);
  const [userToView, setUserToView] = useState(null);

  const initialFormData = {
    nom: '',
    prenom: '',
    email: '',
    password: '',
    cin: '',
    tel: '',
    role: 'ADMIN', // Default role
    enabled: true,
    // Admin specific
    ppr: '',
    grade: '',
    fonction: '',
    // Prof specific
    specialite: '',
    dateRecrutement: '',
    statut: '',
    // Student specific
    apogee: '',
    niveau: '',
    anneeInscription: '',
    semestreActuel: '',
    anneeFinEtude: '',
    dateNaissance: '',
    etatInscription: 'INSCRIT',
    classeId: '' // For student class selection
  };

  const [formData, setFormData] = useState(initialFormData);

  // Fetch Users and Classes on Mount
  useEffect(() => {
    fetchUsers();
    fetchClasses();
  }, []);

  const fetchUsers = async () => {
    setIsLoading(true);
    try {
      const data = await apiComptes.obtenirTous();
      const formattedUsers = data.map(user => ({
        ...user,
        id: user.id || user.apogee || user.ppr, // Fallback ID
        name: `${user.prenom} ${user.nom} `,
        initials: `${user.prenom?.charAt(0) || ''}${user.nom?.charAt(0) || ''} `.toUpperCase(),
        role: normalizeRole(user.role),
        lastActivity: 'Récemment', // Placeholder as backend might not track this yet
        enabled: user.active !== undefined ? user.active : true // Handle backend 'active' vs frontend 'enabled'
      }));
      setUsersList(formattedUsers);
      setError(null);
    } catch (err) {
      console.error("Failed to fetch users:", err);
      setError("Impossible de charger les utilisateurs.");
    } finally {
      setIsLoading(false);
    }
  };

  const fetchClasses = async () => {
    try {
      const data = await apiClasses.obtenirToutes();
      setClassesList(data);
    } catch (err) {
      console.error("Failed to fetch classes:", err);
    }
  };

  const normalizeRole = (role) => {
    if (!role) return 'STUDENT';
    const r = role.toUpperCase();
    if (r.includes('PROF')) return 'PROFESSOR';
    if (r.includes('ADMIN')) return 'ADMIN';
    return 'STUDENT';
  };

  const handleInputChange = (e) => {
    const { name, value, type, checked } = e.target;
    setFormData(prev => ({
      ...prev,
      [name]: type === 'checkbox' ? checked : value
    }));
  };

  const handleOpenAddModal = () => {
    setFormData(initialFormData);
    setIsEditing(false);
    setSelectedUserId(null);
    setModalOpen(true);
  };

  const handleEditUser = async (id) => {
    const userToEdit = usersList.find(u => u.id === id);
    if (userToEdit) {
      // Fetch full user details to get all fields including classeId for students
      try {
        let fullUserData = { ...userToEdit };
        
        // Fetch complete user data based on role
        const role = normalizeRole(userToEdit.role);
        if (role === 'STUDENT' && userToEdit.apogee) {
          try {
            const studentData = await apiEtudiants.obtenirParApogee(userToEdit.apogee);
            fullUserData = { ...fullUserData, ...studentData };
          } catch (err) {
            console.warn('Could not fetch full student data:', err);
          }
        } else if (role === 'PROFESSOR' && userToEdit.ppr) {
          try {
            const profData = await apiProfesseurs.obtenirParPpr(userToEdit.ppr);
            fullUserData = { ...fullUserData, ...profData };
          } catch (err) {
            console.warn('Could not fetch full professor data:', err);
          }
        }

        // Map backend 'active' to frontend 'enabled' if needed
        const enabledValue = fullUserData.enabled !== undefined 
          ? fullUserData.enabled 
          : (fullUserData.active !== undefined ? fullUserData.active : true);

        // For students, map codeClasse back to classeId
        let classeId = '';
        if (role === 'STUDENT' && fullUserData.codeClasse) {
          // Find the class by codeClasse
          const matchingClass = classesList.find(c => 
            c.codeClasse === fullUserData.codeClasse || 
            c.code === fullUserData.codeClasse ||
            c.id?.toString() === fullUserData.codeClasse
          );
          if (matchingClass) {
            classeId = matchingClass.id?.toString() || matchingClass.codeClasse;
          } else {
            // Fallback: use codeClasse directly
            classeId = fullUserData.codeClasse;
          }
        }

        setFormData({
          ...initialFormData,
          ...fullUserData,
          role: normalizeRole(fullUserData.role),
          enabled: enabledValue,
          classeId: classeId,
          // Ensure password is not pre-filled for security
          password: ''
        });
        
        setIsEditing(true);
        setSelectedUserId(id);
        setModalOpen(true);
        setViewModalOpen(false);
      } catch (err) {
        console.error('Error loading user details:', err);
        alert('Erreur lors du chargement des détails de l\'utilisateur');
      }
    }
  };

  const handleSaveUser = async () => {
    setIsLoading(true);
    try {
      const userPayload = { ...formData };
      const originalUser = usersList.find(u => u.id === selectedUserId);
      
      // Check if the enabled status has changed
      const statusChanged = originalUser && originalUser.enabled !== formData.enabled;

      // Backend expects 'enabled' (Boolean), not 'active'
      // Keep enabled as is, don't convert to active
      // The backend DTO uses 'enabled' field directly

      // For students, convert classeId to codeClasse (backend expects codeClasse as String)
      if (formData.role === 'STUDENT') {
        if (!formData.classeId) {
          alert("La classe de l'étudiant est obligatoire");
          setIsLoading(false);
          return;
        }
        
        // Find the class object to get its codeClasse
        const selectedClass = classesList.find(c => c.id === formData.classeId || c.id?.toString() === formData.classeId?.toString());
        if (selectedClass) {
          // Backend expects codeClasse (String), not classe object
          userPayload.codeClasse = selectedClass.codeClasse || selectedClass.code || selectedClass.id?.toString();
        } else {
          // Fallback: use classeId as codeClasse if class not found
          userPayload.codeClasse = formData.classeId;
        }
        delete userPayload.classeId; // Remove the temporary field
      }

      // Clean up fields that shouldn't be sent to backend
      delete userPayload.id;
      delete userPayload.name;
      delete userPayload.initials;
      delete userPayload.lastActivity;

      // Determine service based on role
      let service;
      let id = selectedUserId;

      // Ensure ID for update matches the specific ID field (ppr/apogee) if needed
      switch (formData.role) {
        case 'PROFESSOR':
          service = apiProfesseurs;
          if (isEditing) id = formData.ppr || selectedUserId;
          break;
        case 'STUDENT':
          service = apiEtudiants;
          if (isEditing) id = formData.apogee || selectedUserId;
          break;
        default:
          service = apiAdministrateurs;
          if (isEditing) id = formData.ppr || selectedUserId;
      }

      // Log payload for debugging (remove in production)
      console.log('Saving user payload:', userPayload);

      if (isEditing) {
        // If only the status changed, use the dedicated endpoints
        if (statusChanged && !hasOtherChanges(originalUser, formData)) {
          if (formData.enabled) {
            await service.activer(id);
          } else {
            await service.desactiver(id);
          }
        } else {
          // For other changes or combined changes, use the general modifier endpoint
          await service.modifier(id, userPayload);
          
          // If status also changed, make a separate call to the status endpoint
          if (statusChanged) {
            if (formData.enabled) {
              await service.activer(id);
            } else {
              await service.desactiver(id);
            }
          }
        }
      } else {
        await service.creer(userPayload);
      }

      await fetchUsers(); // Refresh list
      setModalOpen(false);
      setFormData(initialFormData); // Reset form
    } catch (err) {
      console.error("Save failed:", err);
      const errorMessage = err.message || 'Erreur inconnue';
      alert("Une erreur est survenue lors de la sauvegarde: " + errorMessage);
    } finally {
      setIsLoading(false);
    }
  };

  // Helper function to check if there are changes other than the enabled status
  const hasOtherChanges = (original, updated) => {
    if (!original) return true;
    
    const fieldsToCheck = ['nom', 'prenom', 'email', 'password', 'cin', 'tel', 'role', 
                          'ppr', 'grade', 'fonction', 'specialite', 'dateRecrutement', 
                          'statut', 'apogee', 'niveau', 'anneeInscription', 'semestreActuel', 
                          'anneeFinEtude', 'dateNaissance', 'etatInscription', 'classeId'];
    
    return fieldsToCheck.some(field => original[field] !== updated[field]);
  };

  const handleAccountStatusChange = async (userId, newStatus) => {
    try {
      const user = usersList.find(u => u.id === userId);
      if (!user) {
        alert("Utilisateur introuvable");
        return;
      }

      const role = normalizeRole(user.role);
      let service;
      let id;

      // Determine service and ID based on role
      switch (role) {
        case 'PROFESSOR':
          service = apiProfesseurs;
          id = user.ppr || user.id || userId;
          break;
        case 'STUDENT':
          service = apiEtudiants;
          id = user.apogee || user.id || userId;
          break;
        default:
          service = apiAdministrateurs;
          id = user.ppr || user.id || userId;
      }

      if (!id) {
        alert("Impossible de déterminer l'identifiant de l'utilisateur");
        return;
      }

      // Call the dedicated activate/deactivate endpoint
      if (newStatus) {
        await service.activer(id);
      } else {
        await service.desactiver(id);
      }

      // Refresh the users list to get updated status
      await fetchUsers();
      
    } catch (err) {
      console.error("Status change failed:", err);
      const errorMessage = err.message || 'Erreur inconnue';
      alert("Une erreur est survenue lors du changement de statut: " + errorMessage);
      
      // Revert the checkbox state on error
      setFormData(prev => ({ ...prev, enabled: !newStatus }));
    }
  };

  const handleDeleteUser = async (id) => {
    if (!window.confirm("Êtes-vous sûr de vouloir supprimer cet utilisateur ? Cette action est irréversible.")) return;

    setIsLoading(true);
    try {
      const user = usersList.find(u => u.id === id);
      if (!user) {
        alert("Utilisateur introuvable");
        setIsLoading(false);
        return;
      }

      const role = normalizeRole(user.role);

      // Extract the correct identifier for deletion
      let deleteId = id;
      let deleteService = null;

      if (role === 'PROFESSOR') {
        deleteId = user.ppr || user.id || id;
        deleteService = apiProfesseurs;
      } else if (role === 'STUDENT') {
        deleteId = user.apogee || user.id || id;
        deleteService = apiEtudiants;
      } else {
        deleteId = user.ppr || user.id || id;
        deleteService = apiAdministrateurs;
      }

      if (!deleteId) {
        alert("Impossible de déterminer l'identifiant de l'utilisateur à supprimer");
        setIsLoading(false);
        return;
      }

      // Attempt deletion
      await deleteService.supprimer(deleteId);

      // Remove from local state
      setUsersList(prev => prev.filter(u => u.id !== id));
      
      // Show success message
      alert("Utilisateur supprimé avec succès");
    } catch (err) {
      console.error("Delete failed:", err);
      const errorMessage = err.message || 'Erreur inconnue';
      
      // Provide more specific error messages
      if (errorMessage.includes('400')) {
        alert("Erreur lors de la suppression: Requête invalide. L'utilisateur peut avoir des données associées.");
      } else if (errorMessage.includes('500')) {
        alert("Erreur lors de la suppression: Erreur serveur. Veuillez réessayer plus tard.");
      } else if (errorMessage.includes('404')) {
        alert("Erreur lors de la suppression: Utilisateur introuvable.");
      } else {
        alert("Erreur lors de la suppression: " + errorMessage);
      }
    } finally {
      setIsLoading(false);
    }
  };

  const handleViewDetails = async (id) => {
    const user = usersList.find(u => u.id === id);
    if (user) {
      try {
        let fullUserData = { ...user };
        
        // Fetch complete user data based on role
        const role = normalizeRole(user.role);
        if (role === 'STUDENT' && user.apogee) {
          try {
            const studentData = await apiEtudiants.obtenirParApogee(user.apogee);
            fullUserData = { ...fullUserData, ...studentData };
          } catch (err) {
            console.warn('Could not fetch full student data:', err);
          }
        } else if (role === 'PROFESSOR' && user.ppr) {
          try {
            const profData = await apiProfesseurs.obtenirParPpr(user.ppr);
            fullUserData = { ...fullUserData, ...profData };
          } catch (err) {
            console.warn('Could not fetch full professor data:', err);
          }
        }

        // For students, map codeClasse to className for display in modal
        if (role === 'STUDENT' && fullUserData.codeClasse) {
          // Find the class by codeClasse to get its name
          const matchingClass = classesList.find(c => 
            c.codeClasse === fullUserData.codeClasse || 
            c.code === fullUserData.codeClasse ||
            c.id?.toString() === fullUserData.codeClasse
          );
          if (matchingClass) {
            fullUserData.className = matchingClass.nom || matchingClass.nomClasse || `Classe ${matchingClass.id}`;
          } else {
            // Fallback: use codeClasse as className if class not found
            fullUserData.className = fullUserData.codeClasse;
          }
        }

        setUserToView(fullUserData);
        setViewModalOpen(true);
      } catch (err) {
        console.error('Error loading user details for view:', err);
        // Still show basic data if full data fetch fails
        setUserToView(user);
        setViewModalOpen(true);
      }
    }
  };

  const filteredUsers = (activeFilter === 'ALL'
    ? usersList
    : usersList.filter(user => user.role === activeFilter))
    .filter(user => user.name.toLowerCase().includes(searchQuery.toLowerCase()));

  const getRoleBadgeStyle = (role) => {
    switch (role) {
      case 'PROFESSOR':
        return 'role-badge-professor';
      case 'STUDENT':
        return 'role-badge-student';
      case 'ADMIN':
        return 'role-badge-admin';
      default:
        return '';
    }
  };

  const renderSpecificFields = () => {
    switch (formData.role) {
      case 'ADMIN':
        return (
          <>
            <ChampFormulaire label="PPR" nom="ppr" valeur={formData.ppr} onChange={handleInputChange} />
            <ChampFormulaire label="Grade" nom="grade" valeur={formData.grade} onChange={handleInputChange} />
            <ChampFormulaire label="Fonction" nom="fonction" valeur={formData.fonction} onChange={handleInputChange} />
          </>
        );
      case 'PROFESSOR':
        return (
          <>
            <ChampFormulaire label="PPR" nom="ppr" valeur={formData.ppr} onChange={handleInputChange} />
            <ChampFormulaire label="Spécialité" nom="specialite" valeur={formData.specialite} onChange={handleInputChange} />
            <ChampFormulaire label="Grade" nom="grade" valeur={formData.grade} onChange={handleInputChange} />
            <ChampFormulaire label="Date de Recrutement" nom="dateRecrutement" type="date" valeur={formData.dateRecrutement} onChange={handleInputChange} />
            <ChampFormulaire label="Statut" nom="statut" valeur={formData.statut} onChange={handleInputChange} />
          </>
        );
      case 'STUDENT':
        return (
          <>
            <ChampFormulaire label="Apogée" nom="apogee" valeur={formData.apogee} onChange={handleInputChange} />
            <ChampFormulaire label="Niveau" nom="niveau" valeur={formData.niveau} onChange={handleInputChange} />
            <ChampFormulaire label="Année Inscription" nom="anneeInscription" type="number" valeur={formData.anneeInscription} onChange={handleInputChange} />
            <ChampFormulaire label="Semestre Actuel" nom="semestreActuel" valeur={formData.semestreActuel} onChange={handleInputChange} />
            <ChampFormulaire label="Année Fin Étude" nom="anneeFinEtude" valeur={formData.anneeFinEtude} onChange={handleInputChange} />
            <ChampFormulaire label="Date de Naissance" nom="dateNaissance" type="date" valeur={formData.dateNaissance} onChange={handleInputChange} />
            <ChampFormulaire
              label="État Inscription"
              nom="etatInscription"
              type="select"
              valeur={formData.etatInscription}
              onChange={handleInputChange}
              options={[
                { value: 'INSCRIT', label: 'Inscrit' },
                { value: 'SUSPENDU', label: 'Suspendu' },
                { value: 'DIPLOME', label: 'Diplômé' }
              ]}
            />
            <ChampFormulaire
              label="Classe"
              nom="classeId"
              type="select"
              valeur={formData.classeId || ''}
              onChange={handleInputChange}
              options={classesList.length > 0 
                ? classesList.map(c => ({ 
                    value: c.id?.toString() || c.codeClasse || c.code || '', 
                    label: c.nom || c.nomClasse || c.codeClasse || `Classe ${c.id || ''}` 
                  }))
                : [{ value: '', label: 'Chargement des classes...' }]
              }
              requis
              disabled={classesList.length === 0}
            />
          </>
        );
      default:
        return null;
    }
  };

  return (
    <div className="gestion-comptes">
      <div className="gestion-comptes-header">
        <div>
          <h1 className="gestion-comptes-titre">Gestion des Comptes</h1>
          <p className="gestion-comptes-sous-titre">Contrôlez les accès et les permissions de la plateforme.</p>
        </div>

        <div className="header-controls">
          <div className="search-bar-container">
            <Search size={18} className="search-icon" />
            <input
              type="text"
              placeholder="Rechercher un utilisateur..."
              value={searchQuery}
              onChange={(e) => setSearchQuery(e.target.value)}
              className="search-input"
            />
          </div>
          <div className="filter-pills">
            {['ALL', 'ADMIN', 'PROFESSOR', 'STUDENT'].map((filter) => (
              <button
                key={filter}
                className={`filter-btn ${activeFilter === filter ? 'active' : ''}`}
                onClick={() => setActiveFilter(filter)}
              >
                {filter}
              </button>
            ))}
          </div>
          <button className="btn-add" onClick={handleOpenAddModal}>
            <Plus size={24} color="#FFF" />
          </button>
        </div>
      </div>

      <div className="accounts-table-container">
        {isLoading ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: '#64748b' }}>
            <Loader className="animate-spin" size={32} style={{ margin: '0 auto 1rem' }} />
            Chargement des comptes...
          </div>
        ) : error ? (
          <div style={{ padding: '2rem', textAlign: 'center', color: '#dc2626' }}>
            {error}
          </div>
        ) : (
          <table className="accounts-table">
            <thead>
              <tr>
                <th>UTILISATEUR</th>
                <th>E-MAIL</th>
                <th>RÔLE</th>
                <th>STATUT</th>
                <th>DERNIÈRE ACTIVITÉ</th>
                <th>ACTIONS</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan="6" style={{ textAlign: 'center', padding: '2rem', color: '#64748b' }}>
                    Aucun utilisateur trouvé.
                  </td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
                  <tr key={user.id}>
                    <td>
                      <span className="user-name">{user.name}</span>
                    </td>
                    <td className="user-email">{user.email}</td>
                    <td>
                      <span className={`role-badge ${getRoleBadgeStyle(user.role)}`}>
                        {user.role}
                      </span>
                    </td>
                    <td>
                      {user.enabled ? (
                        <span className="status-badge-active">
                          <CheckCircle size={14} /> Actif
                        </span>
                      ) : (
                        <span className="status-badge-inactive">
                          <XCircle size={14} /> Inactif
                        </span>
                      )}
                    </td>
                    <td className="user-activity">{user.lastActivity}</td>
                    <td className="actions-cell">
                      <button className="btn-icon-action btn-view" onClick={() => handleViewDetails(user.id)}>
                        <Eye size={18} />
                      </button>
                      <button className="btn-icon-action btn-edit" onClick={() => handleEditUser(user.id)}>
                        <Edit2 size={18} />
                      </button>
                      <button className="btn-icon-action btn-delete" onClick={() => handleDeleteUser(user.id)}>
                        <Trash2 size={18} />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        )}
      </div>

      {/* Add/Edit User Modal */}
      <FormulaireModal
        titre={isEditing ? "Modifier l'Utilisateur" : "Ajouter un Utilisateur"}
        estOuvert={modalOpen}
        surFermer={() => setModalOpen(false)}
        surSauvegarder={handleSaveUser}
        texteBoutonSauvegarder={isEditing ? "Modifier" : "Créer"}
      >
        <div className="form-grid">
          <ChampFormulaire label="Nom" nom="nom" valeur={formData.nom} onChange={handleInputChange} requis />
          <ChampFormulaire label="Prénom" nom="prenom" valeur={formData.prenom} onChange={handleInputChange} requis />
          <ChampFormulaire label="Email" nom="email" type="email" valeur={formData.email} onChange={handleInputChange} requis />
          <ChampFormulaire label="Mot de passe" nom="password" type="password" valeur={formData.password} onChange={handleInputChange} requis />
          <ChampFormulaire label="CIN" nom="cin" valeur={formData.cin} onChange={handleInputChange} />
          <ChampFormulaire label="Téléphone" nom="tel" type="tel" valeur={formData.tel} onChange={handleInputChange} />

          <ChampFormulaire
            label="Rôle"
            nom="role"
            type="select"
            valeur={formData.role}
            onChange={handleInputChange}
            options={[
              { value: 'ADMIN', label: 'Administrateur' },
              { value: 'PROFESSOR', label: 'Professeur' },
              { value: 'STUDENT', label: 'Étudiant' }
            ]}
            requis
          />

          <div className="form-checkbox-group">
            <label>
              <input
                type="checkbox"
                name="enabled"
                checked={formData.enabled}
                onChange={handleInputChange}
              />
              Compte Actif
            </label>
          </div>

          {/* Dynamic Fields Section */}
          <div className="form-divider">Informations Spécifiques</div>
          {renderSpecificFields()}
        </div>
      </FormulaireModal>

      {/* View Details Modal */}
      <ModalDetailsUtilisateur
        estOuvert={viewModalOpen}
        surFermer={() => setViewModalOpen(false)}
        utilisateur={userToView}
        surModifier={(user) => handleEditUser(user.id)}
      />
    </div>
  );
};

export default GestionComptes;
