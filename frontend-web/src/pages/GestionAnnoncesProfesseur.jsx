import { useState, useEffect } from 'react';
import { Megaphone, AlertTriangle, Calendar, Clock, MapPin, Eye } from 'lucide-react';
import apiAnnonces from '../services/apiAnnonces';
import './GestionAnnoncesProfesseur.css';

const GestionAnnoncesProfesseur = () => {
  const [annonces, setAnnonces] = useState([]);

  useEffect(() => {
    chargerAnnonces();
    // Refresh announcements every 30 seconds
    const interval = setInterval(chargerAnnonces, 30000);
    return () => clearInterval(interval);
  }, []);

  const chargerAnnonces = async () => {
    try {
      const annoncesChargees = await apiAnnonces.obtenirToutes();
      setAnnonces(annoncesChargees);
      
      // Mark announcements as read when viewed
      annoncesChargees.forEach(async (annonce) => {
        if (!annonce.lu && annonce.id) {
          try {
            await apiAnnonces.marquerCommeLu(annonce.id);
          } catch (err) {
            console.error('Erreur lors du marquage comme lu:', err);
          }
        }
      });
    } catch (err) {
      console.error('Erreur lors du chargement des annonces:', err);
    }
  };

  const formaterDate = (dateStr) => {
    const date = new Date(dateStr);
    const maintenant = new Date();
    const diffMs = maintenant - date;
    const diffMins = Math.floor(diffMs / 60000);
    const diffHeures = Math.floor(diffMs / 3600000);
    const diffJours = Math.floor(diffMs / 86400000);

    if (diffMins < 1) return 'À l\'instant';
    if (diffMins < 60) return `Il y a ${diffMins} minute${diffMins > 1 ? 's' : ''}`;
    if (diffHeures < 24) return `Il y a ${diffHeures} heure${diffHeures > 1 ? 's' : ''}`;
    if (diffJours < 7) return `Il y a ${diffJours} jour${diffJours > 1 ? 's' : ''}`;
    return date.toLocaleDateString('fr-FR');
  };

  return (
    <div className="gestion-annonces-professeur">
      <div className="gestion-annonces-professeur-header">
        <div>
          <h1 className="gestion-annonces-professeur-titre">Annonces aux étudiants</h1>
          <p className="gestion-annonces-professeur-sous-titre">
            Consultez les annonces publiées par l'administration
          </p>
        </div>
      </div>

      <div className="annonces-liste">
        {annonces.length === 0 ? (
          <div className="annonces-vide">
            <p>Aucune annonce publiée</p>
          </div>
        ) : (
          annonces.map((annonce) => (
            <div key={annonce.id} className="annonce-card">
              <div className="annonce-card-header">
                <div className="annonce-header-gauche">
                  <div className={`annonce-icone annonce-icone-${annonce.type.toLowerCase()}`}>
                    <AlertTriangle size={24} />
                  </div>
                  <div className="annonce-header-info">
                    <div className="annonce-titre-container">
                      <h3 className="annonce-titre">{annonce.titre}</h3>
                      {annonce.type === 'Urgent' && (
                        <span className="badge-urgent">URGENT</span>
                      )}
                    </div>
                    <p className="annonce-metadata">
                      Publié {formaterDate(annonce.dateCreation)} • {annonce.classe}
                    </p>
                  </div>
                </div>
              </div>

              <div className="annonce-contenu">
                {annonce.date && (
                  <div className="annonce-detail-item">
                    <Calendar size={18} className="annonce-detail-icone" />
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Date:</span>
                      <span className="annonce-detail-valeur">{annonce.date}</span>
                    </div>
                  </div>
                )}
                {annonce.heure && (
                  <div className="annonce-detail-item">
                    <Clock size={18} className="annonce-detail-icone" />
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Heure:</span>
                      <span className="annonce-detail-valeur">{annonce.heure}</span>
                    </div>
                  </div>
                )}
                {annonce.salle && (
                  <div className="annonce-detail-item">
                    <MapPin size={18} className="annonce-detail-icone" />
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Salle:</span>
                      <span className="annonce-detail-valeur">{annonce.salle}</span>
                    </div>
                  </div>
                )}
                {annonce.contenu && (
                  <div className="annonce-detail-item">
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Contenu:</span>
                      <span className="annonce-detail-valeur">{annonce.contenu}</span>
                    </div>
                  </div>
                )}
                {annonce.documentsAutorises && (
                  <div className="annonce-detail-item">
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Documents autorisés:</span>
                      <span className="annonce-detail-valeur">{annonce.documentsAutorises}</span>
                    </div>
                  </div>
                )}
                {annonce.materiel && (
                  <div className="annonce-detail-item">
                    <div className="annonce-detail-content">
                      <span className="annonce-detail-label">Matériel:</span>
                      <span className="annonce-detail-valeur">{annonce.materiel}</span>
                    </div>
                  </div>
                )}
              </div>

              <div className="annonce-card-footer">
                <div className="annonce-etudiants-concernes">
                  <Eye size={16} />
                  <span>{annonce.etudiantsConcernes} étudiants concernés</span>
                </div>
              </div>
            </div>
          ))
        )}
      </div>

    </div>
  );
};

export default GestionAnnoncesProfesseur;
