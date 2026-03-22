import { useEffect, useMemo, useRef, useState } from 'react';
import {
  AlertCircle,
  Loader2,
  Mail,
  Phone,
  Plus,
  Save,
  Shield,
  User,
} from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import { apiProfesseurs } from '../services/apiProfesseurs';
import PhotoSecurisee from '../composants/communs/PhotoSecurisee';
import './ProfilPage.css';
import './ProfilProfesseur.css';

const PHONE_REGEX = /^[+\d\s().-]{8,20}$/;
const ACCEPTED_IMAGE_TYPES = ['image/jpeg', 'image/png'];

const extractPhotoValue = (response) =>
  response?.url
  || response?.photoUrl
  || response?.photo
  || response?.fileName
  || response?.photoNom
  || null;

const getInitials = (prenom, nom) =>
  `${prenom?.charAt(0) || 'P'}${nom?.charAt(0) || 'R'}`.toUpperCase();

const readProfile = (data, fallbackUser) => ({
  nom: data?.nom || fallbackUser?.nom || '',
  prenom: data?.prenom || fallbackUser?.prenom || '',
  email: data?.email || fallbackUser?.email || '',
  tel: data?.tel || data?.telephone || fallbackUser?.tel || fallbackUser?.telephone || '',
  role: 'PROFESSEUR',
  cin: data?.cin || '',
  ppr: data?.ppr || data?.code || '',
  grade: data?.grade || '',
  departement:
    data?.departement?.nom
    || data?.departement?.typeDepartement
    || data?.departement?.libelle
    || data?.departementNom
    || data?.nomDepartement
    || data?.departmentName
    || (typeof data?.departement === 'string' ? data.departement : '')
    || fallbackUser?.departement
    || '',
  specialite: data?.specialite || '',
  photoUrl: data?.photoUrl || data?.photo || fallbackUser?.photoProfile || fallbackUser?.photo || null,
});

const Feedback = ({ type, message }) => {
  if (!message) return null;

  return (
    <div className={`profile-feedback profile-feedback-${type}`}>
      <AlertCircle size={16} />
      <span>{message}</span>
    </div>
  );
};

const ProfilProfesseur = () => {
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [profil, setProfil] = useState({
    nom: '',
    prenom: '',
    email: '',
    tel: '',
    role: 'PROFESSEUR',
    cin: '',
    ppr: '',
    grade: '',
    departement: '',
    specialite: '',
    photoUrl: null,
  });
  const [telephone, setTelephone] = useState('');
  const [passwords, setPasswords] = useState({
    oldPassword: '',
    newPassword: '',
    confirmPassword: '',
  });
  const [selectedPhotoFile, setSelectedPhotoFile] = useState(null);
  const [photoPreview, setPhotoPreview] = useState(null);
  const [savingPhone, setSavingPhone] = useState(false);
  const [savingPassword, setSavingPassword] = useState(false);
  const [savingPhoto, setSavingPhoto] = useState(false);
  const [phoneFeedback, setPhoneFeedback] = useState({ type: '', message: '' });
  const [photoFeedback, setPhotoFeedback] = useState({ type: '', message: '' });
  const [passwordFeedback, setPasswordFeedback] = useState({ type: '', message: '' });
  const fileInputRef = useRef(null);

  useEffect(() => {
    let mounted = true;

    const chargerProfil = async () => {
      try {
        setLoading(true);
        setError('');

        const currentUser = apiAuth.getUser();
        const data = await apiProfesseurs.getProfile();
        if (!mounted) return;

        const mappedProfile = readProfile(data, currentUser);
        setProfil(mappedProfile);
        setTelephone(mappedProfile.tel);
        setPhotoPreview(null);
      } catch (err) {
        console.error('Erreur lors du chargement du profil professeur:', err);
        if (mounted) {
          setError(err.message || 'Impossible de charger le profil professeur.');
        }
      } finally {
        if (mounted) {
          setLoading(false);
        }
      }
    };

    chargerProfil();

    return () => {
      mounted = false;
    };
  }, []);

  useEffect(() => {
    if (!selectedPhotoFile) {
      setPhotoPreview(null);
      return;
    }

    const objectUrl = URL.createObjectURL(selectedPhotoFile);
    setPhotoPreview(objectUrl);

    return () => {
      URL.revokeObjectURL(objectUrl);
    };
  }, [selectedPhotoFile]);

  const displayName = useMemo(() => {
    const fullName = `${profil.prenom} ${profil.nom}`.trim();
    return fullName || profil.email || 'Professeur';
  }, [profil.email, profil.nom, profil.prenom]);

  const phoneError = useMemo(() => {
    if (!telephone.trim()) {
      return 'Le numero de telephone est obligatoire.';
    }

    if (!PHONE_REGEX.test(telephone.trim())) {
      return 'Veuillez saisir un numero de telephone valide.';
    }

    return '';
  }, [telephone]);

  const passwordError = useMemo(() => {
    if (!passwords.oldPassword || !passwords.newPassword || !passwords.confirmPassword) {
      return 'Tous les champs de securite sont obligatoires.';
    }

    if (passwords.newPassword !== passwords.confirmPassword) {
      return 'Les deux nouveaux mots de passe doivent etre identiques.';
    }

    return '';
  }, [passwords]);

  const shouldShowPasswordValidation = useMemo(
    () => Object.values(passwords).some(Boolean),
    [passwords]
  );

  const syncStoredUser = (updates) => {
    apiAuth.updateUser(updates);
    window.dispatchEvent(new Event('user-updated'));
  };

  const reloadProfile = async () => {
    const data = await apiProfesseurs.getProfile();
    const currentUser = apiAuth.getUser();
    const mappedProfile = readProfile(data, currentUser);
    setProfil(mappedProfile);
    setTelephone(mappedProfile.tel);
    setSelectedPhotoFile(null);
    syncStoredUser({
      nom: mappedProfile.nom,
      prenom: mappedProfile.prenom,
      email: mappedProfile.email,
      tel: mappedProfile.tel,
      telephone: mappedProfile.tel,
      photoProfile: mappedProfile.photoUrl,
    });
  };

  const handlePhoneSubmit = async () => {
    if (phoneError) {
      setPhoneFeedback({ type: 'error', message: phoneError });
      return;
    }

    try {
      setSavingPhone(true);
      setPhoneFeedback({ type: '', message: '' });

      await apiProfesseurs.updateProfile({
        nom: profil.nom,
        prenom: profil.prenom,
        email: profil.email,
        tel: telephone.trim(),
        telephone: telephone.trim(),
        grade: profil.grade || undefined,
        departement: profil.departement || undefined,
      });

      setProfil((prev) => ({ ...prev, tel: telephone.trim() }));
      syncStoredUser({
        nom: profil.nom,
        prenom: profil.prenom,
        email: profil.email,
        tel: telephone.trim(),
        telephone: telephone.trim(),
      });
      setPhoneFeedback({ type: 'success', message: 'Numero de telephone mis a jour avec succes.' });
    } catch (err) {
      console.error('Erreur lors de la mise a jour du telephone:', err);
      setPhoneFeedback({
        type: 'error',
        message: err.message || 'Impossible de mettre a jour le numero de telephone.',
      });
    } finally {
      setSavingPhone(false);
    }
  };

  const handlePasswordSubmit = async () => {
    if (passwordError) {
      setPasswordFeedback({ type: 'error', message: passwordError });
      return;
    }

    try {
      setSavingPassword(true);
      setPasswordFeedback({ type: '', message: '' });

      await apiProfesseurs.updateProfile({
        nom: profil.nom,
        prenom: profil.prenom,
        email: profil.email,
        tel: profil.tel,
        telephone: profil.tel,
        grade: profil.grade || undefined,
        departement: profil.departement || undefined,
        ancienPassword: passwords.oldPassword,
        nouveauPassword: passwords.newPassword,
      });

      setPasswords({
        oldPassword: '',
        newPassword: '',
        confirmPassword: '',
      });
      setPasswordFeedback({ type: 'success', message: 'Mot de passe modifie avec succes.' });
    } catch (err) {
      console.error('Erreur lors du changement de mot de passe:', err);
      setPasswordFeedback({
        type: 'error',
        message: err.message || 'Impossible de changer le mot de passe.',
      });
    } finally {
      setSavingPassword(false);
    }
  };

  const handlePhotoClick = () => {
    fileInputRef.current?.click();
  };

  const handlePhotoSelection = (event) => {
    const file = event.target.files?.[0];
    if (!file) return;

    if (!ACCEPTED_IMAGE_TYPES.includes(file.type)) {
      setPhotoFeedback({ type: 'error', message: 'Formats acceptes: JPG et PNG uniquement.' });
      event.target.value = '';
      return;
    }

    if (file.size > 5 * 1024 * 1024) {
      setPhotoFeedback({ type: 'error', message: "L'image ne doit pas depasser 5 Mo." });
      event.target.value = '';
      return;
    }

    setSelectedPhotoFile(file);
    setPhotoFeedback({ type: '', message: '' });
  };

  useEffect(() => {
    if (!selectedPhotoFile) {
      return;
    }

    const envoyerPhoto = async () => {
    try {
      setSavingPhoto(true);
      setPhotoFeedback({ type: '', message: '' });

      const response = await apiProfesseurs.uploadPhoto(selectedPhotoFile);
      const responsePhoto = extractPhotoValue(response);

      if (responsePhoto) {
        setProfil((prev) => ({ ...prev, photoUrl: responsePhoto }));
        syncStoredUser({ photoProfile: responsePhoto });
      } else {
        await reloadProfile();
      }

      setSelectedPhotoFile(null);
      if (fileInputRef.current) {
        fileInputRef.current.value = '';
      }
      setPhotoFeedback({ type: 'success', message: 'Photo de profil mise a jour avec succes.' });
    } catch (err) {
      console.error("Erreur lors de l'envoi de la photo:", err);
      setPhotoFeedback({
        type: 'error',
        message: err.message || "Impossible d'enregistrer la photo de profil.",
      });
    } finally {
      setSavingPhoto(false);
    }
    };

    envoyerPhoto();
  }, [selectedPhotoFile]);

  if (loading) {
    return (
      <div className="profil-loading">
        <Loader2 className="animate-spin" size={48} />
        <p>Chargement du profil professeur...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="profil-error">
        <p>{error}</p>
        <button type="button" onClick={() => window.location.reload()} className="btn-retry">
          Reessayer
        </button>
      </div>
    );
  }

  return (
    <div className="profil-page profil-page-professeur">
      <div className="profil-header">
        <h1>Mon Profil Personnel</h1>
        <p>Gerez vos informations personnelles, votre photo de profil et votre securite.</p>
      </div>

      <div className="profile-page-layout">
        <aside className="profile-sidebar">
          <div className="profile-card">
            <div className="profile-avatar-box">
              {photoPreview ? (
                <img src={photoPreview} alt="Profil professeur" className="profile-avatar-image" />
              ) : (
                <PhotoSecurisee
                  photoUrl={profil.photoUrl}
                  alt="Profil professeur"
                  className="profile-avatar-image"
                  fallback={
                    <span className="profile-avatar-initials">
                      {getInitials(profil.prenom, profil.nom)}
                    </span>
                  }
                />
              )}

              <input
                ref={fileInputRef}
                type="file"
                accept=".jpg,.jpeg,.png,image/jpeg,image/png"
                onChange={handlePhotoSelection}
                style={{ display: 'none' }}
              />

              <button type="button" className="btn-add-photo" onClick={handlePhotoClick}>
                {savingPhoto ? <Loader2 className="animate-spin" size={16} /> : <Plus size={16} />}
              </button>
            </div>

            <h2 className="profile-name">{displayName}</h2>
            <div className="profile-role-tag">PROFESSEUR</div>

            <div className="profile-meta-row">
              <span className="meta-label">PPR</span>
              <span className="meta-value">{profil.ppr || 'N/A'}</span>
            </div>
            <div className="profile-meta-row">
              <span className="meta-label">CIN</span>
              <span className="meta-value">{profil.cin || 'N/A'}</span>
            </div>
            <div className="profile-meta-row">
              <span className="meta-label">GRADE</span>
              <span className="meta-value">{profil.grade || 'N/A'}</span>
            </div>
            <div className="profile-meta-row">
              <span className="meta-label">DEPARTEMENT</span>
              <span className="meta-value">{profil.departement || 'N/A'}</span>
            </div>

            <Feedback type={photoFeedback.type} message={photoFeedback.message} />
          </div>
        </aside>

        <main className="profile-main">
          <section>
            <div className="profile-section-header">
              <User size={20} className="section-icon" />
              <h3>Informations Personnelles</h3>
            </div>

            <div className="profile-grid">
              <div className="profile-input-group">
                <label>Prenom</label>
                <input type="text" className="profile-input-field" value={profil.prenom} readOnly />
              </div>

              <div className="profile-input-group">
                <label>Nom</label>
                <input type="text" className="profile-input-field" value={profil.nom} readOnly />
              </div>

              <div className="profile-input-group">
                <label>Email</label>
                <div className="profile-input-with-icon">
                  <Mail size={16} />
                  <input type="email" className="profile-input-field profile-input-with-leading-icon" value={profil.email} readOnly />
                </div>
              </div>

              <div className="profile-input-group">
                <label>Numero de telephone</label>
                <div className="profile-input-with-icon">
                  <Phone size={16} />
                  <input
                    type="tel"
                    className="profile-input-field profile-input-with-leading-icon"
                    value={telephone}
                    onChange={(event) => {
                      setTelephone(event.target.value);
                      setPhoneFeedback({ type: '', message: '' });
                    }}
                    placeholder="+212 6 00 00 00 00"
                  />
                </div>
              </div>

              <div className="profile-input-group">
                <label>Grade</label>
                <input type="text" className="profile-input-field" value={profil.grade} readOnly />
              </div>

              <div className="profile-input-group">
                <label>Departement</label>
                <input type="text" className="profile-input-field" value={profil.departement} readOnly />
              </div>

              <div className="profile-input-group full-width">
                <label>Email institutionnel</label>
                <input type="text" className="profile-input-field" value={profil.email} readOnly />
              </div>
            </div>

            <Feedback type={phoneFeedback.type} message={phoneFeedback.message} />

            <button
              type="button"
              className="btn-save-changes"
              onClick={handlePhoneSubmit}
              disabled={savingPhone || Boolean(phoneError)}
            >
              {savingPhone ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
              <span>{savingPhone ? 'Mise a jour...' : 'Mettre a jour'}</span>
            </button>
          </section>

          <section className="password-section">
            <div className="profile-section-header">
              <Shield size={20} className="section-icon" />
              <h3>Securite</h3>
            </div>

            <div className="profile-grid">
              <div className="profile-input-group">
                <label>Ancien mot de passe</label>
                <input
                  type="password"
                  className="profile-input-field"
                  value={passwords.oldPassword}
                  onChange={(event) => {
                    setPasswords((prev) => ({ ...prev, oldPassword: event.target.value }));
                    setPasswordFeedback({ type: '', message: '' });
                  }}
                  placeholder="Saisissez votre ancien mot de passe"
                />
              </div>

              <div className="profile-input-group">
                <label>Nouveau mot de passe</label>
                <input
                  type="password"
                  className="profile-input-field"
                  value={passwords.newPassword}
                  onChange={(event) => {
                    setPasswords((prev) => ({ ...prev, newPassword: event.target.value }));
                    setPasswordFeedback({ type: '', message: '' });
                  }}
                  placeholder="Saisissez votre nouveau mot de passe"
                />
              </div>

              <div className="profile-input-group full-width">
                <label>Confirmer le nouveau mot de passe</label>
                <input
                  type="password"
                  className="profile-input-field"
                  value={passwords.confirmPassword}
                  onChange={(event) => {
                    setPasswords((prev) => ({ ...prev, confirmPassword: event.target.value }));
                    setPasswordFeedback({ type: '', message: '' });
                  }}
                  placeholder="Confirmez le nouveau mot de passe"
                />
              </div>
            </div>

            <Feedback
              type={passwordFeedback.type || 'error'}
              message={passwordFeedback.message || (shouldShowPasswordValidation ? passwordError : '')}
            />

            <button
              type="button"
              className="btn-change-password"
              onClick={handlePasswordSubmit}
              disabled={savingPassword || Boolean(passwordError)}
            >
              {savingPassword ? <Loader2 className="animate-spin" size={18} /> : <Shield size={18} />}
              <span>{savingPassword ? 'Modification...' : 'Changer le mot de passe'}</span>
            </button>
          </section>
        </main>
      </div>
    </div>
  );
};

export default ProfilProfesseur;
