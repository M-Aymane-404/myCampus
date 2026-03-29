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
import { apiProfesseurs } from '../../services/apiProfesseurs';
import NotificationHeart from './NotificationHeart';
import PhotoSecurisee from '../communs/PhotoSecurisee';
import './EnTete.css';

const extractPhotoValue = (data) =>
  data?.photoProfile
  || data?.photoUrl
  || data?.photo
  || data?.url
  || data?.fileName
  || data?.photoNom
  || null;

const withCacheBuster = (photoUrl, version) => {
  if (!photoUrl) return null;
  if (!version) return photoUrl;
  const separator = String(photoUrl).includes('?') ? '&' : '?';
  return `${photoUrl}${separator}v=${version}`;
};

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
    const chargerUtilisateur = async ({ forceRefreshPhoto = false } = {}) => {
      const currentUser = apiAuth.getUser();

      if (!currentUser) return;

      const shouldRefreshPhoto = forceRefreshPhoto || !extractPhotoValue(currentUser);
      if (shouldRefreshPhoto) {
        try {
          const data = await apiProfesseurs.getProfile();
          const resolvedPhoto = extractPhotoValue(data);
          if (resolvedPhoto) {
            apiAuth.updateUser({
              nom: data.nom || currentUser.nom || '',
              prenom: data.prenom || currentUser.prenom || '',
              email: data.email || currentUser.email || '',
              telephone: data.tel || data.telephone || currentUser.telephone || currentUser.tel || '',
              photoProfile: resolvedPhoto,
              photoProfileVersion: Date.now(),
            });
          }
        } catch (e) {
          console.error('Erreur rafraichissement profil professeur (navbar):', e);
        }
      }

      const syncedUser = apiAuth.getUser() || currentUser;

      setInfosUtilisateur((prev) => ({
        ...prev,
        nom: syncedUser.nom || prev.nom,
        prenom: syncedUser.prenom || prev.prenom,
        email: syncedUser.email || prev.email,
        telephone: syncedUser.telephone || syncedUser.tel || '',
        nomUtilisateur: syncedUser.nomUtilisateur || syncedUser.username || '',
        departement: syncedUser.departement || '',
        dateCreation: syncedUser.dateCreation || prev.dateCreation,
      }));

      const photo = extractPhotoValue(syncedUser);
      const photoVersion = syncedUser.photoProfileVersion || null;
      if (photo) {
        setPhotoPreview(withCacheBuster(photo, photoVersion));
      } else {
        setPhotoPreview(null);
      }
    };

    chargerUtilisateur({ forceRefreshPhoto: true });

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
                <PhotoSecurisee
                  photoUrl={photoPreview}
                  alt="Profile"
                  className="avatar-image-small"
                  fallback={<span className="avatar-lettre">{initiales}</span>}
                />
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
