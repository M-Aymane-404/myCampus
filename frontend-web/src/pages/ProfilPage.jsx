import { useState, useEffect, useRef } from 'react';
import { User, Plus, Calendar, Save } from 'lucide-react';
import { apiAuth } from '../services/apiAuth';
import './ProfilPage.css';

const ProfilPage = () => {
    // ... (existing state)
    const [infosUtilisateur, setInfosUtilisateur] = useState({
        nom: '',
        prenom: '',
        email: '',
        telephone: '',
        nomUtilisateur: '',
        departement: '',
        role: '',
        dateCreation: '',
        dateNaissance: '2002-05-15', // Default placeholder
        adresseMail: 'test@gmail.com',      // Default placeholder
        adresse: '12 Avenue du Campus, 75000 Paris' // Default placeholder
    });

    // Photo upload state
    const [photoPreview, setPhotoPreview] = useState(null);
    const fileInputRef = useRef(null);

    // ... (useEffect for loading user)

    const handleChange = (e) => {
        const { name, value } = e.target;
        setInfosUtilisateur(prev => ({ ...prev, [name]: value }));
    };

    const handleSave = () => {
        // Prepare data to save
        const updatedUser = {
            ...infosUtilisateur
        };

        // Save to Mock Backend (LocalStorage)
        apiAuth.updateUser(updatedUser);

        // Notify other components (Header) that user data changed
        window.dispatchEvent(new Event('user-updated'));

        console.log("Saving changes:", updatedUser);
        if (photoPreview) {
            console.log("Saving photo...");
            // In a real app, you would upload the photo here and get a URL back
            // For now, we assume the photoPreview is stored if checking locally? 
            // Normally we wouldn't store base64 in localstorage user object for performance, but for this mock:
            // apiAuth.updateUser({ photoProfile: photoPreview }); 
        }
        alert("Modifications enregistrées avec succès !");
    };

    const handlePhotoClick = () => {
        fileInputRef.current.click();
    };

    const handlePhotoChange = (e) => {
        const file = e.target.files[0];
        if (file) {
            if (file.size > 5 * 1024 * 1024) { // 5MB limit
                alert("La taille de l'image ne doit pas dépasser 5 Mo.");
                return;
            }
            const reader = new FileReader();
            reader.onloadend = () => {
                setPhotoPreview(reader.result);
            };
            reader.readAsDataURL(file);
        }
    };

    const currentRole = infosUtilisateur.role ? infosUtilisateur.role.toLowerCase() : 'admin';

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

    const getDisplayName = () => {
        if (!infosUtilisateur.prenom && !infosUtilisateur.nom) return 'Loading...';
        const prefix = currentRole === 'professor' ? 'Dr. ' : '';
        return `${prefix}${infosUtilisateur.prenom} ${infosUtilisateur.nom}`;
    };

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
                            {currentRole === 'student' ? 'L3 INFORMATIQUE' : getRoleLabel(currentRole).toUpperCase()}
                        </div>

                        <div className="profile-meta-row">
                            <span className="meta-label">PPR</span>
                            <span className="meta-value">2023STU001</span>
                        </div>
                        <div className="profile-meta-row">
                            <span className="meta-label">CIN</span>
                            <span className="meta-value">S123456</span>
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
                            <label>DATE DE NAISSANCE</label>
                            <input
                                type="date"
                                name="dateNaissance"
                                value={infosUtilisateur.dateNaissance}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group">
                            <label>NUMÉRO DE TÉLÉPHONE</label>
                            <input
                                type="tel"
                                name="telephone"
                                value={infosUtilisateur.telephone}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group full-width">
                            <label>ADRESSE MAIl</label>
                            <input
                                type="text"
                                name="adresseMail"
                                value={infosUtilisateur.adresseMail}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>

                        <div className="profile-input-group full-width">
                            <label>ADRESSE DE RÉSIDENCE</label>
                            <input
                                type="text"
                                name="adresse"
                                value={infosUtilisateur.adresse}
                                onChange={handleChange}
                                className="profile-input-field"
                            />
                        </div>
                    </div>

                    <button className="btn-save-changes" onClick={handleSave}>
                        <Save size={18} />
                        <span>Sauvegarder</span>
                    </button>
                </div>
            </div>
        </div>
    );
};

export default ProfilPage;
