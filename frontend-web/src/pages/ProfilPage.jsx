import { useState, useEffect, useRef } from 'react';
import { User, Plus, Save, Loader2, Eye, EyeOff, Lock, Phone } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import { apiEtudiant } from '../services/apiEtudiant';
import { apiAdmin } from '../services/apiAdmin';
import PhotoSecurisee from '../composants/communs/PhotoSecurisee';
import './ProfilPage.css';

const extractPhotoValue = (response) =>
    response?.url ||
    response?.photoUrl ||
    response?.photo ||
    response?.fileName ||
    response?.photoNom ||
    null;

const ProfilPage = () => {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);
    const [passwordData, setPasswordData] = useState({
        currentPassword: '',
        newPassword: '',
        confirmPassword: ''
    });
    const [showPasswords, setShowPasswords] = useState({
        current: false,
        new: false,
        confirm: false
    });
    const [passwordError, setPasswordError] = useState('');
    const [telephoneInitial, setTelephoneInitial] = useState('');
    const [infosUtilisateur, setInfosUtilisateur] = useState({
        nom: '',
        prenom: '',
        email: '',
        tel: '',
        role: '',
        cin: '',
        apogee: '',
        dateNaissance: '',
        grade: '',
        photoUrl: null
    });
    const [photoPreview, setPhotoPreview] = useState(null);
    const fileInputRef = useRef(null);

    const syncStoredUser = (updates) => {
        apiAuth.updateUser(updates);
        window.dispatchEvent(new Event('user-updated'));
    };

    useEffect(() => {
        const chargerProfil = async () => {
            try {
                setLoading(true);

                const user = apiAuth.getUser();
                if (!user) {
                    throw new Error('Utilisateur non authentifie');
                }
                const data = user.role === 'admin'
                    ? await apiAdmin.getProfile()
                    : await apiEtudiant.getProfile();

                const resolvedPhoto = extractPhotoValue(data) || extractPhotoValue(user);

                setInfosUtilisateur({
                    nom: data.nom || '',
                    prenom: data.prenom || '',
                    email: data.email || '',
                    tel: data.tel || '',
                    role: data.role || user.role || 'ETUDIANT',
                    cin: data.cin || '',
                    apogee: data.apogee || data.ppr || '',
                    dateNaissance: data.dateNaissance || '',
                    grade: data.grade || '',
                    photoUrl: resolvedPhoto
                });
                setTelephoneInitial(data.tel || '');
                setPhotoPreview(null);

                syncStoredUser({
                    nom: data.nom || user.nom || '',
                    prenom: data.prenom || user.prenom || '',
                    email: data.email || user.email || '',
                    tel: data.tel || user.tel || user.telephone || '',
                    telephone: data.tel || user.tel || user.telephone || '',
                    photoProfile: resolvedPhoto,
                });
            } catch (err) {
                console.error('Erreur lors du chargement du profil:', err);
                setError('Impossible de charger les informations du profil.');
            } finally {
                setLoading(false);
            }
        };

        chargerProfil();
    }, []);

    const handleTelephoneChange = (e) => {
        setInfosUtilisateur((prev) => ({ ...prev, tel: e.target.value }));
    };

    const handlePasswordChange = (e) => {
        const { name, value } = e.target;
        setPasswordData((prev) => ({ ...prev, [name]: value }));
        setPasswordError('');
    };

    const togglePasswordVisibility = (field) => {
        setShowPasswords((prev) => ({ ...prev, [field]: !prev[field] }));
    };

    const validatePasswordData = () => {
        if (!passwordData.currentPassword) {
            setPasswordError('Veuillez saisir votre mot de passe actuel');
            return false;
        }

        if (!passwordData.newPassword) {
            setPasswordError('Veuillez saisir votre nouveau mot de passe');
            return false;
        }

        if (passwordData.newPassword.length < 8) {
            setPasswordError('Le nouveau mot de passe doit contenir au moins 8 caracteres');
            return false;
        }

        if (passwordData.newPassword !== passwordData.confirmPassword) {
            setPasswordError('Les mots de passe ne correspondent pas');
            return false;
        }

        if (passwordData.currentPassword === passwordData.newPassword) {
            setPasswordError('Le nouveau mot de passe doit etre different de l ancien');
            return false;
        }

        return true;
    };

    const handlePasswordChangeSubmit = async () => {
        if (!validatePasswordData()) {
            return;
        }

        try {
            setSaving(true);

            const user = apiAuth.getUser();
            const payload = {
                ancienPassword: passwordData.currentPassword,
                nouveauPassword: passwordData.newPassword
            };

            if (user.role === 'admin') {
                await apiAdmin.updateProfile(payload);
            } else {
                await apiEtudiant.updateProfile(payload);
            }

            setPasswordData({
                currentPassword: '',
                newPassword: '',
                confirmPassword: ''
            });
            setPasswordError('');

            alert('Mot de passe modifie avec succes !');
        } catch (err) {
            console.error('Erreur lors du changement de mot de passe:', err);
            setPasswordError(err.message || 'Erreur lors du changement de mot de passe');
        } finally {
            setSaving(false);
        }
    };

    const handleSave = async () => {
        if (infosUtilisateur.tel === telephoneInitial) {
            return;
        }

        try {
            setSaving(true);

            const user = apiAuth.getUser();
            const payload = { tel: infosUtilisateur.tel };

            if (user.role === 'admin') {
                await apiAdmin.updateProfile(payload);
            } else {
                await apiEtudiant.updateProfile(payload);
            }

            setTelephoneInitial(infosUtilisateur.tel);
            alert('Telephone mis a jour avec succes !');
        } catch (err) {
            console.error('Erreur detaillee lors de la sauvegarde:', err);
            alert(`Erreur: ${err.message}`);
        } finally {
            setSaving(false);
        }
    };

    const handlePhotoClick = () => {
        fileInputRef.current?.click();
    };

    const handlePhotoChange = async (e) => {
        const file = e.target.files[0];

        if (!file) {
            return;
        }

        if (file.size > 5 * 1024 * 1024) {
            alert("La taille de l'image ne doit pas depasser 5 Mo.");
            return;
        }

        try {
            const reader = new FileReader();
            reader.onloadend = () => {
                setPhotoPreview(reader.result);
            };
            reader.readAsDataURL(file);

            const user = apiAuth.getUser();
            const response = user.role === 'admin'
                ? await apiAdmin.uploadPhoto(file)
                : await apiEtudiant.uploadPhoto(file);

            const photoUrl =
                response.url ||
                response.photoUrl ||
                response.photo ||
                response.fileName ||
                response.photoNom;

            if (!photoUrl) {
                console.error('Response structure:', response);
                throw new Error('Reponse du serveur invalide: URL de photo non trouvee');
            }

            setInfosUtilisateur((prev) => ({ ...prev, photoUrl }));
            setPhotoPreview(null);
            syncStoredUser({ photoProfile: photoUrl });

            try {
                const refreshedData = user.role === 'admin'
                    ? await apiAdmin.getProfile()
                    : await apiEtudiant.getProfile();

                const refreshedPhoto = extractPhotoValue(refreshedData);
                if (refreshedPhoto) {
                    setInfosUtilisateur((prev) => ({
                        ...prev,
                        photoUrl: refreshedPhoto
                    }));
                    setPhotoPreview(null);
                    syncStoredUser({ photoProfile: refreshedPhoto });
                }
            } catch (refreshError) {
                console.warn('Could not refresh profile data after photo upload:', refreshError);
            }

            alert('Photo de profil mise a jour !');
        } catch (err) {
            console.error("Erreur lors de l'upload de la photo:", err);
            alert(`Erreur lors de l'envoi de la photo: ${err.message}`);
        }
    };

    const currentRole = infosUtilisateur.role
        ? infosUtilisateur.role.toUpperCase()
        : 'ETUDIANT';
    const telephoneModifie = infosUtilisateur.tel !== telephoneInitial;
    const isStudent = () => {
        const role = infosUtilisateur.role || apiAuth.getUser()?.role || '';
        return role.toLowerCase() !== 'admin';
    };

    const getDisplayName = () => {
        if (infosUtilisateur.prenom && infosUtilisateur.nom) {
            return `${infosUtilisateur.prenom} ${infosUtilisateur.nom}`;
        }

        return infosUtilisateur.email
            ? infosUtilisateur.email.split('@')[0]
            : 'Utilisateur';
    };

    if (loading) {
        return (
            <div className="profil-loading">
                <Loader2 className="animate-spin" size={48} />
                <p>Chargement de votre profil...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="profil-error">
                <p>{error}</p>
                <button onClick={() => window.location.reload()} className="btn-retry">Reessayer</button>
            </div>
        );
    }

    return (
        <div className="profil-page">
            <div className="profil-header">
                <h1>Mon Profil Personnel</h1>
                <p>Modifiez uniquement votre telephone, votre mot de passe et votre photo de profil.</p>
            </div>

            <div className="profile-page-layout">
                <div className="profile-sidebar">
                    <div className="profile-card">
                        <div className="profile-avatar-box">
                            {photoPreview ? (
                                <img src={photoPreview} alt="Profil" className="profile-avatar-image" />
                            ) : (
                                <PhotoSecurisee
                                  photoUrl={infosUtilisateur.photoUrl}
                                  alt="Profil"
                                  className="profile-avatar-image"
                                  fallback={
                                    <span className="profile-avatar-initials">
                                      {infosUtilisateur.prenom ? infosUtilisateur.prenom.charAt(0) : ''}
                                      {infosUtilisateur.nom ? infosUtilisateur.nom.charAt(0) : ''}
                                    </span>
                                  }
                                />
                            )}

                            <input
                                type="file"
                                ref={fileInputRef}
                                onChange={handlePhotoChange}
                                style={{ display: 'none' }}
                                accept="image/*"
                            />

                            <button className="btn-add-photo" onClick={handlePhotoClick}>
                                <Plus size={16} />
                            </button>
                        </div>

                        <h2 className="profile-name">{getDisplayName()}</h2>

                        <div className="profile-role-tag">
                            {currentRole === 'ETUDIANT' ? 'ETUDIANT' : currentRole}
                        </div>

                        <div className="profile-meta-row">
                            <span className="meta-label">{isStudent() ? 'APOGEE' : 'PPR'}</span>
                            <span className="meta-value">{infosUtilisateur.apogee || 'N/A'}</span>
                        </div>

                        <div className="profile-meta-row">
                            <span className="meta-label">CIN</span>
                            <span className="meta-value">{infosUtilisateur.cin || 'N/A'}</span>
                        </div>
                    </div>
                </div>

                <div className="profile-main">
                    <div className="profile-section-header">
                        <User size={20} className="section-icon" />
                        <h3>Informations personnelles</h3>
                    </div>

                    <div className="profile-grid">
                        <div className="profile-input-group">
                            <label>Prenom</label>
                            <div className="profile-static-field">{infosUtilisateur.prenom || 'N/A'}</div>
                        </div>

                        <div className="profile-input-group">
                            <label>Nom</label>
                            <div className="profile-static-field">{infosUtilisateur.nom || 'N/A'}</div>
                        </div>

                        <div className="profile-input-group">
                            <label>{infosUtilisateur.role === 'admin' ? 'Grade' : 'Date de naissance'}</label>
                            <div className="profile-static-field">
                                {infosUtilisateur.role === 'admin'
                                    ? (infosUtilisateur.grade || 'N/A')
                                    : (infosUtilisateur.dateNaissance || 'N/A')}
                            </div>
                        </div>

                        <div className="profile-input-group">
                            <label>Numero de telephone</label>
                            <input
                                type="tel"
                                name="tel"
                                value={infosUtilisateur.tel}
                                onChange={handleTelephoneChange}
                                className="profile-input-field"
                            />
                            <span className="profile-field-help">Ce champ est modifiable.</span>
                        </div>

                        <div className="profile-input-group full-width">
                            <label>Adresse mail</label>
                            <div className="profile-static-field">{infosUtilisateur.email || 'N/A'}</div>
                        </div>
                    </div>

                    <div className="profile-permissions-note">
                        <Phone size={16} />
                        <span>Seuls le numero de telephone, le mot de passe et la photo de profil peuvent etre modifies.</span>
                    </div>

                    <div className="password-section">
                        <div className="profile-section-header">
                            <Lock size={20} className="section-icon" />
                            <h3>Changement de mot de passe</h3>
                        </div>

                        <div className="profile-grid">
                            <div className="profile-input-group">
                                <label>Mot de passe actuel</label>
                                <div className="password-input-wrapper">
                                    <input
                                        type={showPasswords.current ? 'text' : 'password'}
                                        name="currentPassword"
                                        value={passwordData.currentPassword}
                                        onChange={handlePasswordChange}
                                        className="profile-input-field"
                                        placeholder="Entrez votre mot de passe actuel"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle-btn"
                                        onClick={() => togglePasswordVisibility('current')}
                                    >
                                        {showPasswords.current ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            <div className="profile-input-group">
                                <label>Nouveau mot de passe</label>
                                <div className="password-input-wrapper">
                                    <input
                                        type={showPasswords.new ? 'text' : 'password'}
                                        name="newPassword"
                                        value={passwordData.newPassword}
                                        onChange={handlePasswordChange}
                                        className="profile-input-field"
                                        placeholder="Entrez le nouveau mot de passe"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle-btn"
                                        onClick={() => togglePasswordVisibility('new')}
                                    >
                                        {showPasswords.new ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            <div className="profile-input-group">
                                <label>Confirmer le mot de passe</label>
                                <div className="password-input-wrapper">
                                    <input
                                        type={showPasswords.confirm ? 'text' : 'password'}
                                        name="confirmPassword"
                                        value={passwordData.confirmPassword}
                                        onChange={handlePasswordChange}
                                        className="profile-input-field"
                                        placeholder="Confirmez le nouveau mot de passe"
                                    />
                                    <button
                                        type="button"
                                        className="password-toggle-btn"
                                        onClick={() => togglePasswordVisibility('confirm')}
                                    >
                                        {showPasswords.confirm ? <EyeOff size={18} /> : <Eye size={18} />}
                                    </button>
                                </div>
                            </div>

                            {passwordError && (
                                <div className="profile-input-group full-width">
                                    <div className="password-error-message">
                                        {passwordError}
                                    </div>
                                </div>
                            )}
                        </div>

                        <button
                            className="btn-change-password"
                            onClick={handlePasswordChangeSubmit}
                            disabled={saving || !passwordData.currentPassword || !passwordData.newPassword || !passwordData.confirmPassword}
                        >
                            {saving ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
                            <span>{saving ? 'Modification...' : 'Changer le mot de passe'}</span>
                        </button>
                    </div>

                    <button className="btn-save-changes" onClick={handleSave} disabled={saving || !telephoneModifie}>
                        {saving ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
                        <span>{saving ? 'Enregistrement...' : 'Enregistrer le telephone'}</span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProfilPage;
