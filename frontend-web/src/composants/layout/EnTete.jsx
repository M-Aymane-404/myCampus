import {
  Bell, GraduationCap, LayoutDashboard, Users, UserCheck,
  BookOpen, Building2, FileText, Calendar, ChevronDown,
  User, LogOut, Megaphone, Shield as ShieldData
} from 'lucide-react';
import { useState, useEffect, useRef, useMemo } from 'react';
import { apiAuth } from '../../services/apiAuth';
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
          setInfosUtilisateur({
            nom: currentUser.nom || '',
            prenom: currentUser.prenom || '',
            email: currentUser.email || '',
            telephone: currentUser.telephone || '',
            nomUtilisateur: currentUser.nomUtilisateur || '',
            departement: currentUser.departement || '',
            role: currentUser.role || 'admin',
            dateCreation: currentUser.dateCreation || new Date().toLocaleDateString(),
            photoProfile: currentUser.photoProfile || null
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
      case 'professor': return 'Prof';
      case 'student': return 'Student';
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
    if (!infosUtilisateur.prenom && !infosUtilisateur.nom) return 'Loading...';
    const prefix = currentRole === 'professor' ? 'Dr. ' : '';
    return `${prefix}${infosUtilisateur.prenom} ${infosUtilisateur.nom}`;
  };

  return (
    <header className="en-tete">
      {/* 1. Left Section: Logo */}
      <div className="en-tete-gauche">
        <div className="logo-container-header">
          <div className="logo-icone-container-header">
            <span className="logo-icone-header-span">E</span>
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
                  style={{ width: '100%', height: '100%', borderRadius: '50%', objectFit: 'cover' }}
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
