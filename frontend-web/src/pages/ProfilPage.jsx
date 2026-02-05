import { useState, useEffect, useRef } from 'react';
import { User, Plus, Calendar, Save, Loader2 } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import { apiEtudiant } from '../services/apiEtudiant';
import { apiProfesseurs } from '../services/apiProfesseurs';
import { apiAdmin } from '../services/apiAdmin';
import './ProfilPage.css';

const ProfilPage = () => {
    const [loading, setLoading] = useState(true);
    const [saving, setSaving] = useState(false);
    const [error, setError] = useState(null);

    const [infosUtilisateur, setInfosUtilisateur] = useState({
        nom: '',
        prenom: '',
        email: '',
        tel: '',
        role: '',
        cin: '',
        apogee: '',
        dateNaissance: '',
        dateRecrutement: '',
        fonction: '', // Add fonction field for admin
        photo: null
    });

    // Photo upload state
    const [photoPreview, setPhotoPreview] = useState(null);
    const fileInputRef = useRef(null);

    useEffect(() => {
        const chargerProfil = async () => {
            try {
                setLoading(true);
                const currentUser = apiAuth.getUser();
                const role = (currentUser?.role || 'student').toLowerCase();

                if (role === 'student') {
                    console.log('Loading student profile...');
                    const data = await apiEtudiant.getProfile();
                    console.log('Student data received:', data);
                    
                    setInfosUtilisateur({
                        nom: data.nom || '',
                        prenom: data.prenom || '',
                        email: data.email || '',
                        tel: data.tel || '',
                        role: data.role || 'ETUDIANT',
                        cin: data.cin || '',
                        apogee: data.apogee || '',
                        dateNaissance: data.dateNaissance || '',
                        dateRecrutement: '', // Students don't have dateRecrutement
                        photo: data.photo || null
                    });

                    if (data.photo) {
                        setPhotoPreview(data.photo.startsWith('http') ? data.photo : `http://localhost:8080/uploads/${data.photo}`);
                    }
                } else if (role === 'professor') {
                    console.log('Loading professor profile...');
                    const data = await apiProfesseurs.getProfile();
                    console.log('Professor data received:', data);
                    
                    setInfosUtilisateur({
                        nom: data.nom || '',
                        prenom: data.prenom || '',
                        email: data.email || '',
                        tel: data.tel || '',
                        role: 'PROFESSEUR',
                        cin: data.cin || '',
                        apogee: data.ppr || '',
                        dateNaissance: '', // Professors don't have dateNaissance
                        dateRecrutement: data.dateRecrutement || '',
                        photo: data.photo || null
                    });

                    if (data.photo) {
                        setPhotoPreview(data.photo.startsWith('http') ? data.photo : `http://localhost:8080/uploads/${data.photo}`);
                    }
                } else {
                    // Admin - use admin profile endpoint
                    console.log('Loading admin profile...');
                    const data = await apiAdmin.getProfile();
                    console.log('Admin data received:', data);
                    console.log('Admin nom:', data.nom);
                    console.log('Admin prenom:', data.prenom);
                    
                    setInfosUtilisateur({
                        nom: data.nom || '',
                        prenom: data.prenom || '',
                        email: data.email || '',
                        tel: data.tel || '',
                        role: 'ADMIN',
                        cin: data.cin || '',
                        apogee: data.ppr || '',
                        dateNaissance: '', // Admins don't have dateNaissance
                        dateRecrutement: '', // Admins don't use dateRecrutement
                        fonction: data.fonction || '', // Load fonction from database
                        photo: data.photo || null
                    });

                    if (data.photo) {
                        setPhotoPreview(data.photo.startsWith('http') ? data.photo : `http://localhost:8080/uploads/${data.photo}`);
                    }
                }
            } catch (err) {
                console.error("Erreur lors du chargement du profil:", err);
                setError("Impossible de charger les informations du profil.");
            } finally {
                setLoading(false);
            }
        };

        chargerProfil();
    }, []);

    const handleChange = (e) => {
        const { name, value } = e.target;
        setInfosUtilisateur(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = async () => {
        try {
            setSaving(true);
            const profileData = {
                nom: infosUtilisateur.nom,
                prenom: infosUtilisateur.prenom,
                dateNaissance: infosUtilisateur.dateNaissance,
                tel: infosUtilisateur.tel,
                email: infosUtilisateur.email
            };

            await apiEtudiant.updateProfile(profileData);

            // Update local storage via apiAuth for synchronization with other components
            apiAuth.updateUser({
                nom: infosUtilisateur.nom,
                prenom: infosUtilisateur.prenom,
                email: infosUtilisateur.email
            });

            // Notify header/other components
            window.dispatchEvent(new Event('user-updated'));

            alert("✅ Modifications enregistrées avec succès !");
        } catch (err) {
            console.error("Erreur détaillée lors de la sauvegarde:", err);

            // Helpful message if backend is not ready
            if (err.message.includes('405') || err.message.includes('404')) {
                alert(`⚠️ Le bouton ne peut pas enregistrer car l'API de mise à jour (PATCH ${BASE_URL}/moi) n'est pas encore implémentée par votre équipe backend.\n\nVérifiez que le contrôleur Java gère bien la méthode PATCH.`);
            } else {
                alert(`❌ Erreur: ${err.message}`);
            }
        } finally {
            setSaving(false);
        }
    };

    const handlePhotoClick = () => {
        fileInputRef.current.click();
    };

    const handlePhotoChange = async (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                alert("La taille de l'image ne doit pas dépasser 5 Mo.");
                return;
            }

            try {
                // 1. Show preview immediately for better UX
                const reader = new FileReader();
                reader.onloadend = () => {
                    setPhotoPreview(reader.result);
                };
                reader.readAsDataURL(file);

                // 2. Upload to backend
                const response = await apiEtudiant.uploadPhoto(file);

                // 3. Update photo in local state and session
                const photoUrl = response.photo; // Assuming backend returns the photo reference/URL
                const fullPhotoUrl = photoUrl.startsWith('http') ? photoUrl : `http://localhost:8080/uploads/${photoUrl}`;

                setInfosUtilisateur(prev => ({ ...prev, photo: photoUrl }));
                setPhotoPreview(fullPhotoUrl);

                apiAuth.updateUser({ photoProfile: fullPhotoUrl });
                window.dispatchEvent(new Event('user-updated'));

                alert("Photo de profil mise à jour !");
            } catch (err) {
                console.error("Erreur lors de l'upload de la photo:", err);
                alert("Erreur lors de l'envoi de la photo.");
            }
        }
    };

    const currentRole = infosUtilisateur.role ? infosUtilisateur.role.toUpperCase() : 'ETUDIANT';

    const getDisplayName = () => {
        console.log('getDisplayName called with:', {
            prenom: infosUtilisateur.prenom,
            nom: infosUtilisateur.nom,
            email: infosUtilisateur.email,
            role: infosUtilisateur.role
        });
        
        if (infosUtilisateur.prenom && infosUtilisateur.nom) {
            const fullName = `${infosUtilisateur.prenom} ${infosUtilisateur.nom}`;
            console.log('Returning full name:', fullName);
            return fullName;
        }
        if (infosUtilisateur.email) {
            const emailName = infosUtilisateur.email.split('@')[0] || 'Utilisateur';
            console.log('Returning email-based name:', emailName);
            return emailName;
        }
        console.log('Returning default name');
        return 'Utilisateur';
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
                <button onClick={() => window.location.reload()} className="btn-retry">Réessayer</button>
            </div>
        );
    }

    return (
        <div className="profil-page">
            <div className="profil-header">
                <h1>Mon Profil Personnel</h1>
                <p>Gérez vos informations personnelles et académiques.</p>
            </div>

            <div className="profile-page-layout">
                {/* Left Sidebar */}
                <div className="profile-sidebar">
                    <div className="profile-card">
                        <div className="profile-avatar-box">
                            {photoPreview ? (
                                <img src={photoPreview} alt="Profil" className="profile-avatar-image" />
                            ) : (
                                <span className="profile-avatar-initials">
                                    {infosUtilisateur.prenom ? infosUtilisateur.prenom.charAt(0) : ''}
                                    {infosUtilisateur.nom ? infosUtilisateur.nom.charAt(0) : ''}
                                </span>
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
                            {currentRole === 'ETUDIANT' ? 'ÉTUDIANT' : currentRole}
                        </div>

                        <div className="profile-meta-row">
                            <span className="meta-label">
                                {currentRole === 'ETUDIANT' ? 'APOGEE' : 'PPR'}
                            </span>
                            <span className="meta-value">{infosUtilisateur.apogee || 'N/A'}</span>
                        </div>
                        <div className="profile-meta-row">
                            <span className="meta-label">CIN</span>
                            <span className="meta-value">{infosUtilisateur.cin || 'N/A'}</span>
                        </div>
                    </div>


                </div>

                {/* Right Main Content */}
                <div className="profile-main">
                    <div className="profile-section-header">
                        <User size={20} className="section-icon" />
                        <h3>Informations Personnelles</h3>
                    </div>

                    <div className="profile-grid">
                        <div className="profile-input-group">
                            <label>PRÉNOM</label>
                            <input
                                type="text"
                                name="prenom"
                                value={infosUtilisateur.prenom}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group">
                            <label>NOM</label>
                            <input
                                type="text"
                                name="nom"
                                value={infosUtilisateur.nom}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group">
                            <label>
                                {infosUtilisateur.role === 'ETUDIANT' ? 'DATE DE NAISSANCE' : 
                                 infosUtilisateur.role === 'ADMIN' ? 'FONCTION' : 'DATE DE RECRUTEMENT'}
                            </label>
                            <input
                                type={infosUtilisateur.role === 'ADMIN' ? 'text' : 'date'}
                                name={infosUtilisateur.role === 'ETUDIANT' ? 'dateNaissance' : 
                                     infosUtilisateur.role === 'ADMIN' ? 'fonction' : 'dateRecrutement'}
                                value={infosUtilisateur.role === 'ETUDIANT' ? infosUtilisateur.dateNaissance : 
                                       infosUtilisateur.role === 'ADMIN' ? infosUtilisateur.fonction : infosUtilisateur.dateRecrutement}
                                onChange={handleChange}
                                className="profile-input-field"
                                readOnly={infosUtilisateur.role === 'ADMIN'}
                            />
                        </div>

                        <div className="profile-input-group">
                            <label>NUMÉRO DE TÉLÉPHONE</label>
                            <input
                                type="tel"
                                name="tel"
                                value={infosUtilisateur.tel}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group full-width">
                            <label>ADRESSE MAIl</label>
                            <input
                                type="email"
                                name="email"
                                value={infosUtilisateur.email}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                    </div>

                    <button className="btn-save-changes" onClick={handleSave} disabled={saving}>
                        {saving ? <Loader2 className="animate-spin" size={18} /> : <Save size={18} />}
                        <span>{saving ? 'Enregistrement...' : 'Sauvegarder'}</span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProfilPage;
