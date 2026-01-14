import { Bell, GraduationCap, LayoutDashboard, Users, UserCheck, BookOpen, Building2, FileText, Calendar, ChevronDown, User, LogOut, Camera, X } from 'lucide-react';
import { useState, useEffect, useRef } from 'react';
import FormulaireModal from '../communs/FormulaireModal';
import ChampFormulaire from '../communs/ChampFormulaire';
import './EnTete.css';

const EnTete = ({ nombreNotifications = 3, elementActif, surChangementElement }) => {
  const [modeSombre, setModeSombre] = useState(false);
  const [menuOuvert, setMenuOuvert] = useState(false);
  const [modalInfosOuvert, setModalInfosOuvert] = useState(false);
  const [photoProfil, setPhotoProfil] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const menuRef = useRef(null);
  const fileInputRef = useRef(null);

  const [infosUtilisateur, setInfosUtilisateur] = useState({
    nom: 'Admin',
    prenom: 'User',
    email: 'admin@mycampus.ma',
    telephone: '+212 6XX XXX XXX',
    nomUtilisateur: 'admin_user',
    departement: 'Administration',
    dateCreation: '01/01/2024'
  });

  useEffect(() => {
    const modeEnregistre = localStorage.getItem('modeSombre');
    if (modeEnregistre === 'true') {
      setModeSombre(true);
      document.documentElement.classList.add('mode-sombre');
    }
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

  const basculerModeSombre = () => {
    const nouveauMode = !modeSombre;
    setModeSombre(nouveauMode);
    localStorage.setItem('modeSombre', nouveauMode.toString());
    document.documentElement.classList.toggle('mode-sombre', nouveauMode);
  };

  const elementsMenu = [
    { id: 'tableau-de-bord', icone: LayoutDashboard, libelle: 'Dashboard' },
    { id: 'departements', icone: Building2, libelle: 'Departments' },
    { id: 'professeurs', icone: UserCheck, libelle: 'Professors' },
    { id: 'etudiants', icone: Users, libelle: 'Students' },
    { id: 'emploi-temps', icone: Calendar, libelle: 'Schedule' },
    { id: 'comptes', icone: Users, libelle: 'Accounts' },
  ];

  const traductionsMenu = {
    'tableau-de-bord': 'Dashboard',
    'departements': 'Departments',
    'professeurs': 'Professors',
    'etudiants': 'Students',
    'emploi-temps': 'Schedule',
    'comptes': 'Accounts',
  };

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
    // Validation
    if (!infosUtilisateur.nom || !infosUtilisateur.prenom || !infosUtilisateur.email) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    // Ici, vous pouvez envoyer les données à une API
    // Exemple: await api.updateUserProfile(infosUtilisateur, photoProfil);
    
    console.log('Informations utilisateur:', infosUtilisateur);
    if (photoProfil) {
      console.log('Photo de profil:', photoProfil);
    }

    alert('Informations mises à jour avec succès');
    setModalInfosOuvert(false);
  };

  const gererLogout = () => {
    if (window.confirm('Êtes-vous sûr de vouloir vous déconnecter ?')) {
      // TODO: Implémenter la déconnexion (nettoyage session, redirection, etc.)
      console.log('Déconnexion...');
      // Exemple: localStorage.clear();
      // Exemple: window.location.href = '/login';
      alert('Déconnexion (à implémenter)');
      setMenuOuvert(false);
    }
  };

  return (
    <header className="en-tete">
      {/* Top Section */}
      <div className="en-tete-top">
        <div className="en-tete-gauche">
          <div className="logo-container-header">
            <div className="logo-icone-container-header">
              <GraduationCap className="logo-icone-header" size={24} />
            </div>
            <div className="logo-texte-header">
              <h1 className="logo-titre-header">École Supérieure</h1>
              <p className="logo-sous-titre-header">Administration Dashboard</p>
            </div>
          </div>
        </div>
        
        

        <div className="en-tete-actions">
          <div className="notification-container">
            <button className="bouton-notification" aria-label="Notifications">
              <Bell size={20} />
              {nombreNotifications > 0 && (
                <span className="badge-notification">{nombreNotifications}</span>
              )}
            </button>
          </div>

          <div className="separateur-vertical"></div>

          <div className="profil-admin-container" ref={menuRef}>
            <button 
              className="profil-admin"
              onClick={() => setMenuOuvert(!menuOuvert)}
              aria-label="Menu de navigation"
            >
              <div className="info-profil">
                <span className="nom-profil">Administrateur</span>
                <span className="role-profil">{infosUtilisateur.email}</span>
              </div>
              <div className="avatar-profil">
                {photoPreview ? (
                  <img src={photoPreview} alt="Photo de profil" className="avatar-image-small" />
                ) : (
                  <span className="avatar-lettre">{infosUtilisateur.prenom.charAt(0)}{infosUtilisateur.nom.charAt(0)}</span>
                )}
              </div>
              <ChevronDown 
                size={16} 
                className={`chevron-menu ${menuOuvert ? 'ouvert' : ''}`}
              />
            </button>
          
          {menuOuvert && (
            <div className="menu-deroulant">
              <div className="menu-deroulant-header">
                <div className="avatar-profil-menu">
                  {photoPreview ? (
                    <img src={photoPreview} alt="Photo de profil" className="avatar-image-menu" />
                  ) : (
                    <span className="avatar-lettre">{infosUtilisateur.prenom.charAt(0)}{infosUtilisateur.nom.charAt(0)}</span>
                  )}
                </div>
                <div className="info-profil-menu">
                  <span className="nom-profil-menu">{infosUtilisateur.prenom} {infosUtilisateur.nom}</span>
                  <span className="role-profil-menu">Administrator</span>
                </div>
                <button
                  className="bouton-infos-utilisateur"
                  onClick={afficherInfosUtilisateur}
                  aria-label="Afficher les informations utilisateur"
                  title="Informations utilisateur"
                >
                  <User size={18} />
                </button>
              </div>
              <div className="menu-deroulant-separateur"></div>
              <ul className="menu-deroulant-liste">
                <li>
                  <button
                    className={`menu-deroulant-item ${elementActif === 'journal-audit' ? 'actif' : ''}`}
                    onClick={() => gererClicMenu('journal-audit')}
                  >
                    <FileText size={18} />
                    <span>Journal d'audit</span>
                  </button>
                </li>
              </ul>
              <div className="menu-deroulant-separateur"></div>
              <div className="menu-deroulant-footer">
                <button
                  className="menu-deroulant-item menu-deroulant-logout"
                  onClick={gererLogout}
                >
                  <LogOut size={18} />
                  <span>Déconnexion</span>
                </button>
              </div>
            </div>
          )}
        </div>
        </div>
      </div>

      {/* Bottom Navigation Bar */}
      <nav className="en-tete-nav">
        {elementsMenu.map((element) => {
          const Icone = element.icone;
          const estActif = elementActif === element.id;
          return (
            <button
              key={element.id}
              className={`nav-item ${estActif ? 'nav-item-actif' : ''}`}
              onClick={() => gererClicMenu(element.id)}
            >
              <Icone size={18} />
              <span>{element.libelle}</span>
            </button>
          );
        })}
      </nav>

      {/* Modal Informations Utilisateur */}
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
              <p className="infos-utilisateur-role">Administrator</p>
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
              label="Nom d'utilisateur"
              nom="nomUtilisateur"
              valeur={infosUtilisateur.nomUtilisateur}
              onChange={gererChangementInfos}
              placeholder="nom_utilisateur"
              requis
            />
            <ChampFormulaire
              label="Département"
              nom="departement"
              type="select"
              valeur={infosUtilisateur.departement}
              onChange={gererChangementInfos}
              options={[
                { value: 'Administration', label: 'Administration' },
                { value: 'Informatique', label: 'Informatique' },
                { value: 'Industriel', label: 'Industriel' },
                { value: 'Civil', label: 'Génie Civil' }
              ]}
            />
            <div className="infos-utilisateur-item-readonly">
              <div className="infos-utilisateur-item-icon">
                <Calendar size={18} />
              </div>
              <div className="infos-utilisateur-item-content">
                <label className="infos-utilisateur-label">Date de création du compte</label>
                <p className="infos-utilisateur-value">{infosUtilisateur.dateCreation}</p>
              </div>
            </div>
          </div>
        </div>
      </FormulaireModal>
    </header>
  );
};

export default EnTete;

