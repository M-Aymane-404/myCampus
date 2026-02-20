import { Bell, LayoutDashboard, BookOpen, GraduationCap, Calendar, UserCheck, ChevronDown, User, LogOut, Camera, X, Megaphone } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import FormulaireModal from '../communs/FormulaireModal';
import ChampFormulaire from '../communs/ChampFormulaire';
import apiAnnonces from '../../services/apiAnnonces';
import { apiAuth } from '../../services/apiAuth';
import './EnTete.css';

const EnTeteProfesseur = ({ elementActif, surChangementElement }) => {
  const [menuOuvert, setMenuOuvert] = useState(false);
  const [modalInfosOuvert, setModalInfosOuvert] = useState(false);
  const [photoProfil, setPhotoProfil] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const menuRef = useRef(null);
  const fileInputRef = useRef(null);

  const [infosUtilisateur, setInfosUtilisateur] = useState({
    nom: 'Professeur',
    prenom: 'User',
    email: 'professeur@mycampus.ma',
    telephone: '+212 6XX XXX XXX',
    nomUtilisateur: 'prof_user',
    departement: 'Informatique',
    dateCreation: '01/01/2024'
  });

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
    { id: 'tableau-de-bord-professeur', icone: LayoutDashboard, libelle: 'Dashboard' },
    { id: 'cours', icone: BookOpen, libelle: 'Courses' },
    { id: 'notes', icone: GraduationCap, libelle: 'Grades' },
    { id: 'calendrier', icone: Calendar, libelle: 'Calendar' },
    { id: 'presence', icone: UserCheck, libelle: 'Attendance' },
    { id: 'annonces', icone: Megaphone, libelle: 'Announcements' },
  ];

  const gererClicMenu = (id) => {
    if (surChangementElement) {
      surChangementElement(id);
    }
    setMenuOuvert(false);
  };

  const afficherInfosUtilisateur = () => {
    setModalInfosOuvert(true);
    setMenuOuvert(false);
  };

  const gererChangementInfos = (e) => {
    const { name, value } = e.target;
    setInfosUtilisateur(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementPhoto = (e) => {
    const fichier = e.target.files[0];
    if (fichier) {
      if (fichier.size > 5 * 1024 * 1024) {
        alert('La taille de l\'image ne doit pas dépasser 5MB');
        return;
      }
      if (!fichier.type.startsWith('image/')) {
        alert('Veuillez sélectionner un fichier image');
        return;
      }
      setPhotoProfil(fichier);
      const lecteur = new FileReader();
      lecteur.onload = (e) => {
        setPhotoPreview(e.target.result);
      };
      lecteur.readAsDataURL(fichier);
    }
  };

  const supprimerPhoto = () => {
    setPhotoProfil(null);
    setPhotoPreview(null);
    if (fileInputRef.current) {
      fileInputRef.current.value = '';
    }
  };

  const sauvegarderInfosUtilisateur = () => {
    if (!infosUtilisateur.nom || !infosUtilisateur.prenom || !infosUtilisateur.email) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    console.log('Informations utilisateur:', infosUtilisateur);
    if (photoProfil) {
      console.log('Photo de profil:', photoProfil);
    }

    alert('Informations mises à jour avec succès');
    setModalInfosOuvert(false);
  };

  const gererLogout = () => {
    if (window.confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      // Use API auth service for proper logout
      apiAuth.logout();
      setMenuOuvert(false);
    }
  };

  return (
    <header className="en-tete">
      {/* 1. Logo Section */}
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
        {elementsMenu.map((element) => {
          const Icone = element.icone;
          const estActif = elementActif === element.id;
          return (
            <button
              key={element.id}
              className={`nav-item ${estActif ? 'nav-item-actif' : ''}`}
              onClick={() => gererClicMenu(element.id)}
            >
              <Icone size={20} />
              <span>{element.libelle}</span>
            </button>
          );
        })}
      </nav>

      {/* 3. Right Section: Notifications & Profile */}
      <div className="en-tete-actions">

        <div className="profil-admin-container" ref={menuRef} onClick={() => setMenuOuvert(!menuOuvert)}>
          <div className="info-profil-text">
            <span className="nom-profil">Dr. {infosUtilisateur.prenom} {infosUtilisateur.nom}</span>
            <span className="role-badge">Professor</span>
          </div>

          <div className="avatar-wrapper">
            <div className="avatar-profil">
              {photoPreview ? (
                <img src={photoPreview} alt="Profile" className="avatar-image-small" />
              ) : (
                <span className="avatar-lettre">{infosUtilisateur.prenom.charAt(0)}{infosUtilisateur.nom.charAt(0)}</span>
              )}
            </div>
            <div className="status-dot"></div>
          </div>

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
              <button className="menu-deroulant-item menu-deroulant-logout" onClick={gererLogout}>
                <LogOut size={18} />
                <span>Déconnexion</span>
              </button>
            </div>
          )}
        </div>
      </div>

      <FormulaireModal
        titre="Informations Utilisateur"
        estOuvert={modalInfosOuvert}
        surFermer={() => {
          setModalInfosOuvert(false);
          setPhotoProfil(null);
          setPhotoPreview(null);
        }}
        surSauvegarder={sauvegarderInfosUtilisateur}
      >
        <div className="infos-utilisateur-content">
          <div className="infos-utilisateur-avatar-section">
            <div className="infos-utilisateur-avatar-container">
              <div className="infos-utilisateur-avatar-grand">
                {photoPreview ? (
                  <img src={photoPreview} alt="Photo de profil" className="avatar-image" />
                ) : (
                  <span className="avatar-lettre">{infosUtilisateur.prenom.charAt(0)}{infosUtilisateur.nom.charAt(0)}</span>
                )}
              </div>
              <button
                type="button"
                className="bouton-changer-photo"
                onClick={() => fileInputRef.current?.click()}
                title="Changer la photo"
              >
                <Camera size={18} />
              </button>
              {photoPreview && (
                <button
                  type="button"
                  className="bouton-supprimer-photo"
                  onClick={supprimerPhoto}
                  title="Supprimer la photo"
                >
                  <X size={16} />
                </button>
              )}
              <input
                type="file"
                ref={fileInputRef}
                accept="image/*"
                style={{ display: 'none' }}
                onChange={gererChangementPhoto}
              />
            </div>
            <div className="infos-utilisateur-nom-section">
              <h3 className="infos-utilisateur-nom">{infosUtilisateur.prenom} {infosUtilisateur.nom}</h3>
              <p className="infos-utilisateur-role">Professor</p>
            </div>
          </div>

          <div className="infos-utilisateur-details">
            <ChampFormulaire
              label="Prénom"
              nom="prenom"
              valeur={infosUtilisateur.prenom}
              onChange={gererChangementInfos}
              placeholder="Prénom"
              requis
            />
            <ChampFormulaire
              label="Nom"
              nom="nom"
              valeur={infosUtilisateur.nom}
              onChange={gererChangementInfos}
              placeholder="Nom"
              requis
            />
            <ChampFormulaire
              label="Email"
              nom="email"
              type="email"
              valeur={infosUtilisateur.email}
              onChange={gererChangementInfos}
              placeholder="email@mycampus.ma"
              requis
            />
            <ChampFormulaire
              label="Téléphone"
              nom="telephone"
              type="tel"
              valeur={infosUtilisateur.telephone}
              onChange={gererChangementInfos}
              placeholder="+212 6XX XXX XXX"
            />
            <ChampFormulaire
              label="Département"
              nom="departement"
              type="select"
              valeur={infosUtilisateur.departement}
              onChange={gererChangementInfos}
              options={[
                { value: 'Informatique', label: 'Informatique' },
                { value: 'Industriel', label: 'Industriel' },
                { value: 'Civil', label: 'Génie Civil' },
                { value: 'Mathématiques', label: 'Mathématiques' }
              ]}
            />
          </div>
        </div>
      </FormulaireModal>
    </header>
  );
};

export default EnTeteProfesseur;
