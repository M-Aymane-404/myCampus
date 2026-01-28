import { X, CheckCircle, Mail, Phone, Fingerprint, Calendar, Award, Hash, BookOpen, Briefcase } from 'lucide-react';
import { useEffect } from 'react';
import './ModalDetailsUtilisateur.css';

const ModalDetailsUtilisateur = ({ estOuvert, surFermer, utilisateur, surModifier }) => {
    useEffect(() => {
        if (estOuvert) {
            document.body.style.overflow = 'hidden';
        } else {
            document.body.style.overflow = 'unset';
        }
        return () => {
            document.body.style.overflow = 'unset';
        };
    }, [estOuvert]);

    if (!estOuvert || !utilisateur) return null;

    // Data Mapping
    const initials = utilisateur.name ? utilisateur.name.substring(0, 2).toUpperCase() : '??';

    // Mapping Role to Title/Badge
    let mainBadgeText = utilisateur.role;
    let secondaryBadgeText = "";

    if (utilisateur.role === "STUDENT") {
        mainBadgeText = utilisateur.niveau || "LICENCE 3";
        secondaryBadgeText = "ÉTUDIANT BOURSIER"; // Mock
    } else if (utilisateur.role === "PROFESSOR") {
        mainBadgeText = "PROFESSEUR";
        secondaryBadgeText = utilisateur.grade || "SENIOR";
    } else if (utilisateur.role === "ADMIN") {
        mainBadgeText = "ADMINISTRATEUR";
        secondaryBadgeText = "STAFF";
    }

    const userId = utilisateur.ppr || utilisateur.apogee || "23009845";

    // Check if we have an avatar URL in the user object (mock)
    // For demo matching screenshot, we'll try to use a real image if provided, else initials
    const hasAvatar = false; // Toggle or use user.avatarUrl

    return (
        <div className="formulaire-modal-overlay" onClick={surFermer}>
            <div className="formulaire-modal-contenu" style={{ maxWidth: '900px', borderRadius: '24px', padding: '0' }} onClick={(e) => e.stopPropagation()}>

                <button className="formulaire-modal-fermer" onClick={surFermer} style={{ position: 'absolute', top: '2rem', right: '2rem', zIndex: 10 }}>
                    <X size={28} />
                </button>

                <div className="details-user-container">

                    {/* Header */}
                    <div className="profile-header-card">
                        <div className="large-avatar-wrapper">
                            {hasAvatar ? (
                                <div className="large-avatar"></div>
                            ) : (
                                // Using style from CSS which has background image fallback or color
                                <div className={`large-avatar ${!hasAvatar ? 'large-avatar-placeholder' : ''}`} style={!hasAvatar ? { backgroundImage: 'none' } : {}}>
                                    {/* If explicit image not set in CSS, use initials for safety if design calls for it, 
                         but screenshot has image. CSS has a placeholder image URL for demo. */}
                                    {!hasAvatar && <span style={{ fontSize: '3rem' }}>{initials}</span>}
                                </div>
                            )}

                            <div className="avatar-floating-id">
                                <span className="dot-indicator"></span>
                                {userId}
                            </div>
                        </div>

                        <div className="profile-info-block">
                            <div className="profile-name-row">
                                <h2>{utilisateur.name}</h2>
                                {utilisateur.enabled && (
                                    <span className="status-badge-capsule">
                                        <CheckCircle size={12} strokeWidth={3} /> ACTIF
                                    </span>
                                )}
                            </div>

                            <div className="header-badges-row">
                                <div className="badge-pill primary">
                                    <BookOpen size={16} />
                                    {mainBadgeText}
                                </div>
                                {secondaryBadgeText && (
                                    <div className="badge-pill dark">
                                        <Briefcase size={16} />
                                        {secondaryBadgeText}
                                    </div>
                                )}
                            </div>
                        </div>
                    </div>

                    {/* Grid Layout */}
                    <div className="details-sections-grid">

                        {/* Left Column: Contact */}
                        <div className="fields-column">
                            <div className="section-header-title">INFORMATIONS DE CONTACT</div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Mail size={16} className="field-icon" /> EMAIL
                                </div>
                                <div className="field-value">{utilisateur.email}</div>
                            </div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Phone size={16} className="field-icon" /> NUMÉRO DE TÉLÉPHONE
                                </div>
                                <div className="field-value">{utilisateur.tel || "+212 6 61 23 45 67"}</div>
                            </div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Fingerprint size={16} className="field-icon" /> CIN (IDENTITÉ)
                                </div>
                                <div className="field-value">{utilisateur.cin || "BJ456789"}</div>
                            </div>
                        </div>

                        {/* Right Column: Parcours & Système */}
                        <div className="fields-column">
                            <div className="section-header-title">PARCOURS & SYSTÈME</div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Award size={16} className="field-icon" /> {utilisateur.role === "STUDENT" ? "Classe" : utilisateur.role === "ADMIN" ? "Grade" : "SPÉCIALITÉ"}
                                </div>
                                <div className="field-value">
                                    {utilisateur.role === "STUDENT" ? (utilisateur.className || "Classe non spécifiée") : utilisateur.role === "ADMIN" ? (utilisateur.grade || "Grade non spécifié") : (utilisateur.specialite || "Spécialité non spécifiée")}
                                </div>
                            </div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Hash size={16} className="field-icon" /> {utilisateur.role === "STUDENT" ? "APOGEE" : "PPR"}
                                </div>
                                <div className="field-value">{userId}</div>
                            </div>

                            <div className="detail-field-card">
                                <div className="field-label">
                                    <Calendar size={16} className="field-icon" /> DATE DE CRÉATION
                                </div>
                                <div className="field-value">{utilisateur.dateCreation || "12/09/2023 10:45"}</div>
                            </div>
                        </div>

                    </div>

                    {/* Footer */}
                    {/* Optional: Add buttons if needed, screenshot doesn't explicitly show footer but it's good UX */}
                    {/* 
          <div className="modal-details-footer">
             <button className="btn-modal-action btn-primary-dark" onClick={() => surModifier(utilisateur)}>Modifier le Profil</button>
          </div> 
          */}

                </div>
            </div>
        </div>
    );
};

export default ModalDetailsUtilisateur;
