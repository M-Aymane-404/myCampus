import {
  LayoutDashboard,
  BookOpen,
  GraduationCap,
  Calendar,
  UserCheck,
  ChevronDown,
  User,
  LogOut,
  Megaphone
} from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import { apiAuth } from '../../services/apiAuth';
import NotificationHeart from './NotificationHeart';
import './EnTete.css';

const EnTeteProfesseur = ({ elementActif, surChangementElement }) => {
  const [menuOuvert, setMenuOuvert] = useState(false);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [infosUtilisateur, setInfosUtilisateur] = useState({
    nom: 'Professeur',
    prenom: 'Utilisateur',
    email: 'professeur@mycampus.ma',
    telephone: '',
    nomUtilisateur: '',
    departement: '',
    dateCreation: ''
  });

  const menuRef = useRef(null);

  useEffect(() => {
    const chargerUtilisateur = () => {
      const currentUser = apiAuth.getUser();

      if (!currentUser) return;

      setInfosUtilisateur((prev) => ({
        ...prev,
        nom: currentUser.nom || prev.nom,
        prenom: currentUser.prenom || prev.prenom,
        email: currentUser.email || prev.email,
        telephone: currentUser.telephone || currentUser.tel || '',
        nomUtilisateur: currentUser.nomUtilisateur || currentUser.username || '',
        departement: currentUser.departement || '',
        dateCreation: currentUser.dateCreation || prev.dateCreation,
      }));

      const photo = currentUser.photoProfile || currentUser.photo || null;
      if (photo) {
        const fullPhotoUrl = photo.startsWith('http')
          ? photo
          : `http://localhost:8080${photo}`;
        setPhotoPreview(fullPhotoUrl);
      } else {
        setPhotoPreview(null);
      }
    };

    chargerUtilisateur();

    const handleUserUpdate = () => chargerUtilisateur();
    window.addEventListener('user-updated', handleUserUpdate);

    return () => {
      window.removeEventListener('user-updated', handleUserUpdate);
    };
  }, []);

  useEffect(() => {
    const handleClickOutside = (event) => {
      if (menuRef.current && !menuRef.current.contains(event.target)) {
        setMenuOuvert(false);
      }
    };

    if (menuOuvert) {
      document.addEventListener('mousedown', handleClickOutside);
    }

    return () => {
      document.removeEventListener('mousedown', handleClickOutside);
    };
  }, [menuOuvert]);

  const elementsMenu = [
    { id: 'tableau-de-bord-professeur', icone: LayoutDashboard, libelle: 'Tableau de bord' },
    { id: 'cours', icone: BookOpen, libelle: 'Cours' },
    { id: 'notes', icone: GraduationCap, libelle: 'Notes' },
    { id: 'calendrier', icone: Calendar, libelle: 'Calendrier' },
    { id: 'presence', icone: UserCheck, libelle: 'Presences' },
    { id: 'annonces', icone: Megaphone, libelle: 'Annonces' },
  ];

  const gererClicMenu = (id) => {
    if (surChangementElement) {
      surChangementElement(id);
    }
    setMenuOuvert(false);
  };

  const afficherInfosUtilisateur = () => {
    if (surChangementElement) {
      surChangementElement('profil');
    }
    setMenuOuvert(false);
  };

  const gererLogout = () => {
    if (window.confirm('Etes-vous sur de vouloir vous deconnecter ?')) {
      apiAuth.logout();
      setMenuOuvert(false);
    }
  };

  const initiales = `${infosUtilisateur.prenom?.charAt(0) || 'P'}${infosUtilisateur.nom?.charAt(0) || 'R'}`;

  return (
    <header className="en-tete">
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

      <nav className="en-tete-nav-container">
        {elementsMenu.map((element) => {
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

      <div className="en-tete-actions">
        <NotificationHeart
          currentRole="professor"
          surChangementElement={surChangementElement}
        />

        <div
          className="profil-admin-container"
          ref={menuRef}
          onClick={() => setMenuOuvert(!menuOuvert)}
        >
          <div className="info-profil-text">
            <span className="nom-profil">
              Dr. {infosUtilisateur.prenom} {infosUtilisateur.nom}
            </span>
            <span className="role-badge">Professeur</span>
          </div>

          <div className="avatar-wrapper">
            <div className="avatar-profil">
              {photoPreview ? (
                <img src={photoPreview} alt="Profile" className="avatar-image-small" />
              ) : (
                <span className="avatar-lettre">{initiales}</span>
              )}
            </div>
            <div className="status-dot"></div>
          </div>

          <ChevronDown size={16} />

          {menuOuvert && (
            <div className="menu-deroulant" onClick={(e) => e.stopPropagation()}>
              <ul className="menu-deroulant-liste">
                <li>
                  <button className="menu-deroulant-item" onClick={afficherInfosUtilisateur}>
                    <User size={18} />
                    <span>Voir le profil</span>
                  </button>
                </li>
              </ul>
              <button
                className="menu-deroulant-item menu-deroulant-logout"
                onClick={gererLogout}
              >
                <LogOut size={18} />
                <span>Deconnexion</span>
              </button>
            </div>
          )}
        </div>
      </div>
    </header>
  );
};

export default EnTeteProfesseur;
