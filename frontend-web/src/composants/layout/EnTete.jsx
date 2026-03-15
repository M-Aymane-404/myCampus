import { User, LogOut } from 'lucide-react';
import { useState, useEffect, useRef, useMemo } from 'react';
import { apiAuth } from '../../services/apiAuth';
import NotificationHeart from './NotificationHeart';
import './EnTete.css';

const EnTete = ({ elementActif, surChangementElement }) => {
  const [menuOuvert, setMenuOuvert] = useState(false);
  const menuRef = useRef(null);

  // Initial state for user info - will be populated from auth
  const [infosUtilisateur, setInfosUtilisateur] = useState({
    nom: '',
    prenom: '',
    email: '',
    telephone: '',
    nomUtilisateur: '',
    departement: '',
    role: '',
    dateCreation: ''
  });

  // Load User Data on Mount
  useEffect(() => {
    const chargerUtilisateur = async () => {
      try {
        const currentUser = apiAuth.getUser();
        if (currentUser) {
          let profileData = null;

          // Fetch detailed profile data based on role
          if (!currentUser.nom || !currentUser.prenom) {
            const role = (currentUser.role || '').toLowerCase();

            try {
              if (role === 'student') {
                const response = await fetch('http://localhost:8080/api/etudiant/moi', {
                  method: 'GET',
                  headers: apiAuth.getAuthHeaders(),
                });

                if (response.ok) {
                  profileData = await response.json();
                }
              } else if (role === 'professor') {
                const response = await fetch('http://localhost:8080/api/professeur/moi', {
                  method: 'GET',
                  headers: apiAuth.getAuthHeaders(),
                });

                if (response.ok) {
                  profileData = await response.json();
                }
              } else if (role === 'admin') {
                // Use admin API to get profile
                const response = await fetch('http://localhost:8080/api/admin/utilisateurs/administrateurs', {
                  method: 'GET',
                  headers: apiAuth.getAuthHeaders(),
                });

                if (response.ok) {
                  const allAdmins = await response.json();
                  profileData = allAdmins.find(admin => admin.email === currentUser.email);
                }
              }

              if (profileData) {
                apiAuth.updateUser({
                  nom: profileData.nom || '',
                  prenom: profileData.prenom || '',
                  email: profileData.email || currentUser.email || '',
                  telephone: profileData.tel || profileData.telephone || '',
                  photoProfile: profileData.photoUrl || profileData.photo || currentUser.photoProfile || null,
                });
              }
            } catch (e) {
              console.error("Erreur lors du chargement du profil de l'utilisateur", e);
            }
          }

          const syncedUser = apiAuth.getUser();
          const userToUse = syncedUser || currentUser;

          const photo = userToUse.photoProfile || null;
          const fullPhotoUrl = photo ? (photo.startsWith('http') ? photo : `http://localhost:8080${photo}`) : null;

          setInfosUtilisateur({
            nom: userToUse.nom || '',
            prenom: userToUse.prenom || '',
            email: userToUse.email || '',
            telephone: userToUse.telephone || '',
            nomUtilisateur: userToUse.nomUtilisateur || '',
            departement: userToUse.departement || '',
            role: userToUse.role || 'admin',
            dateCreation: userToUse.dateCreation || new Date().toLocaleDateString(),
            photoProfile: fullPhotoUrl
          });
        }
      } catch (error) {
        console.error("Erreur lors du chargement de l'utilisateur", error);
      }
    };

    chargerUtilisateur();

    // Listen for profile updates
    const handleUserUpdate = () => chargerUtilisateur();
    window.addEventListener('user-updated', handleUserUpdate);

    return () => {
      window.removeEventListener('user-updated', handleUserUpdate);
    };
  }, []);

  // Format Helper for Role Display
  const getRoleLabel = (role) => {
    if (!role) return '';
    const r = role.toLowerCase();
    switch (r) {
      case 'professor': return 'Professeur';
      case 'student': return 'Etudiant';
      case 'admin': return 'Admin';
      default: return role.charAt(0).toUpperCase() + role.slice(1);
    }
  };

  // Menu Configuration based on Role
  const currentRole = infosUtilisateur.role ? infosUtilisateur.role.toLowerCase() : 'admin';

  const MENU_ITEMS = useMemo(() => ({
    admin: [
      { id: 'tableau-de-bord', libelle: 'Tableau de bord' },
      { id: 'departements', libelle: 'Departements' },
      { id: 'emploi-temps', libelle: 'Emploi-temps' },
      { id: 'comptes', libelle: 'Comptes' },
      { id: 'annonces-admin', libelle: 'Annonces' },
      { id: 'journal-audit', libelle: 'Journal audit' },
    ],
    professor: [
      { id: 'tableau-de-bord-professeur', libelle: 'Tableau de bord' },
      { id: 'cours', libelle: 'Cours' },
      { id: 'notes', libelle: 'Notes' },
      { id: 'calendrier', libelle: 'Calendrier' },
      { id: 'presence', libelle: 'Presence' },
      { id: 'annonces', libelle: 'Annonces' },
    ],
    student: [
      { id: 'tableau-de-bord-etudiant', libelle: 'Tableau de bord' },
      { id: 'cours', libelle: 'Cours' },
      { id: 'notes', libelle: 'Notes' },
      { id: 'presence', libelle: 'Presence' },
      { id: 'calendrier', libelle: 'Calendrier' },
      { id: 'annonces', libelle: 'Annonces' },
    ]
  }), []);

  const menuItemsToDisplay = MENU_ITEMS[currentRole] || MENU_ITEMS['admin'];

  // Event Handlers
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOuvert(false);
      }
    };
    if (menuOuvert) document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, [menuOuvert]);

  const gererClicMenu = (id) => {
    if (surChangementElement) surChangementElement(id);
    setMenuOuvert(false);
  };

  const gererLogout = () => {
    if (window.confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      apiAuth.logout();
      setMenuOuvert(false);
    }
  };

  // Helper to display name with prefix if needed
  const getDisplayName = () => {
    // If we have both first and last name, use them
    if (infosUtilisateur.prenom && infosUtilisateur.nom) {
      const prefix = currentRole === 'professor' ? 'Dr. ' : '';
      return `${prefix}${infosUtilisateur.prenom} ${infosUtilisateur.nom}`;
    }

    // If we have at least one name field, use it
    if (infosUtilisateur.prenom || infosUtilisateur.nom) {
      const prefix = currentRole === 'professor' ? 'Dr. ' : '';
      return `${prefix}${infosUtilisateur.prenom || ''}${infosUtilisateur.nom || ''}`;
    }

    // Fallback to username or email if no names are available
    const fallback = infosUtilisateur.nomUtilisateur || infosUtilisateur.email || 'Utilisateur';

    // Extract username from email if email is the only available option
    if (fallback.includes('@')) {
      const username = fallback.split('@')[0];
      // Capitalize first letter and make it more readable
      return username.charAt(0).toUpperCase() + username.slice(1);
    }

    return fallback;
  };

  return (
    <header className="en-tete">
      {/* 1. Left Section: Logo */}
      <div className="en-tete-gauche">
        <div className="logo-container-header">
          <div className="logo-icone-container-header">
            <img
              src="/src/assets/images/university_logo.jpeg"
              alt="MyCampus Logo"
              className="logo-icone-image"
            />
          </div>
          <div className="logo-text-container">
            <span className="logo-main-text">MyCampus</span>
            <span className="logo-sub-text">INTELLIGENT HUB</span>
          </div>
        </div>
      </div>

      {/* 2. Middle Section: Pill Navigation */}
      <nav className="en-tete-nav-container">
        {menuItemsToDisplay.map((element) => {
          const estActif = elementActif === element.id;
          return (
            <button
              key={element.id}
              className={`nav-item ${estActif ? 'nav-item-actif' : ''}`}
              onClick={() => gererClicMenu(element.id)}
            >
              <span>{element.libelle}</span>
            </button>
          );
        })}
      </nav>

      {/* 3. Right Section: Profile */}
      <div className="en-tete-actions">
        <NotificationHeart
          currentRole={currentRole}
          surChangementElement={surChangementElement}
        />

        <div className="profil-admin-container" ref={menuRef} onClick={() => setMenuOuvert(!menuOuvert)}>
          <div className="info-profil-text">
            <span className="nom-profil">{getDisplayName()}</span>
            <span className="role-badge">{getRoleLabel(currentRole)}</span>
          </div>

          <div className="avatar-wrapper">
            <div className="avatar-profil">
              {infosUtilisateur.photoProfile ? (
                <img
                  src={infosUtilisateur.photoProfile}
                  alt="Profil"
                  style={{ width: '100%', height: '100%', objectFit: 'cover' }}
                />
              ) : (
                <span className="avatar-lettre">
                  {infosUtilisateur.prenom ? infosUtilisateur.prenom.charAt(0) : ''}
                  {infosUtilisateur.nom ? infosUtilisateur.nom.charAt(0) : ''}
                </span>
              )}
            </div>
          </div>

          {menuOuvert && (
            <div className="menu-deroulant" onClick={(e) => e.stopPropagation()}>
              <ul className="menu-deroulant-liste">
                <li>
                  <button className="menu-deroulant-item" onClick={() => gererClicMenu('profil')}>
                    <User size={18} />
                    <span>Voir le profil</span>
                  </button>
                </li>
              </ul>
              <button className="menu-deroulant-item menu-deroulant-logout" onClick={gererLogout}>
                <LogOut size={18} />
                <span>Déconnexion</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default EnTete;
