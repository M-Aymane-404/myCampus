import { useState, useEffect } from 'react';
import { GraduationCap, ArrowLeft, Users, BookOpen, Eye } from 'lucide-react';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import BoutonAction from '../composants/communs/BoutonAction';
import { apiStructure } from '../services/apiStructure';
import apiClasses from '../services/apiClasses';
import './GestionClasses.css';

const GestionClasses = ({ filiereId, filiereNom, onRetour, surVoirModules }) => {
  const [classes, setClasses] = useState([]);
  const [chargement, setChargement] = useState(true);
  const [erreur, setErreur] = useState(null);

  useEffect(() => {
    if (filiereId) {
      chargerClasses();
    }
  }, [filiereId]);

  const chargerClasses = async () => {
    try {
      setChargement(true);
      setErreur(null);

      // Fetch real data from backend - use codeFiliere if filiereId is a code, otherwise use id
      const codeFiliere = typeof filiereId === 'string' ? filiereId : filiereId?.codeFiliere;
      const donnees = await apiClasses.obtenirParFiliere(codeFiliere || filiereId);

      // Map backend data to frontend format
      const classesMapped = donnees.map(c => ({
        id: c.id,
        codeClasse: c.codeClasse || c.code,
        nom: c.nom || c.libelle,
        nombreEtudiants: c.nombreEtudiants || 0,
        nombreModules: c.nombreModules || 0,
        statut: c.statut || c.etat || 'Actif'
      }));

      setClasses(classesMapped);
    } catch (err) {
      console.error('Erreur lors du chargement des classes:', err);
      setErreur('Erreur lors du chargement des classes');
    } finally {
      setChargement(false);
    }
  };

  const gererVoirModules = (classe) => {
    if (surVoirModules) {
      surVoirModules(classe);
    }
  };

  const totalClasses = classes.length;
  const totalEtudiants = classes.reduce((sum, c) => sum + c.nombreEtudiants, 0);
  const totalModules = classes.reduce((sum, c) => sum + c.nombreModules, 0);

  return (
    <div className="gestion-classes">
      <div className="gestion-classes-header">
        <div className="gestion-classes-header-gauche">
          {onRetour && (
            <BoutonAction
              libelle="Retour"
              variante="secondaire"
              taille="moyen"
              icone={ArrowLeft}
              onClick={onRetour}
            />
          )}
          <div>
            <h1 className="gestion-classes-titre">Classes de la Filière</h1>
            <p className="gestion-classes-sous-titre">
              {filiereNom || 'Filière'} - Gestion des classes
            </p>
          </div>
        </div>
      </div>

      <div className="gestion-classes-statistiques">
        <CarteStatistique
          titre="Total Classes"
          valeur={totalClasses}
          variation={{ valeur: totalClasses, label: '', positif: true }}
          icone={GraduationCap}
          couleur="primary"
          iconeCouleur="dark"
        />
        <CarteStatistique
          titre="Total Étudiants"
          valeur={totalEtudiants}
          variation={{ valeur: totalEtudiants, label: '', positif: true }}
          icone={Users}
          couleur="success"
          iconeCouleur="light"
        />
        <CarteStatistique
          titre="Total Modules"
          valeur={totalModules}
          variation={{ valeur: totalModules, label: '', positif: true }}
          icone={GraduationCap}
          couleur="info"
          iconeCouleur="dark"
        />
      </div>

      {chargement ? (
        <div className="chargement-container">Chargement des classes...</div>
      ) : erreur ? (
        <div className="erreur-container">
          <p>{erreur}</p>
          <button onClick={chargerClasses} className="btn-reessayer">Réessayer</button>
        </div>
      ) : classes.length === 0 ? (
        <div className="no-classes">
          <p>Aucune classe disponible pour cette filière.</p>
        </div>
      ) : (
        <div className="gestion-classes-cartes">
          {classes.map(classe => (
            <div key={classe.id} className="classe-carte">
              <div className="classe-carte-header">
                <div className="classe-carte-icone">
                  <GraduationCap size={24} />
                </div>
                <div className="classe-carte-titre-container">
                  <h3 className="classe-carte-titre">{classe.nom}</h3>
                  <p className="classe-carte-code">{classe.codeClasse}</p>
                </div>
              </div>

              <div className="classe-carte-infos">
                <div className="classe-carte-info-item">
                  <Users size={16} />
                  <span className="classe-carte-info-label">Étudiants</span>
                  <span className="classe-carte-info-value">{classe.nombreEtudiants}</span>
                </div>
                <div className="classe-carte-info-item">
                  <BookOpen size={16} />
                  <span className="classe-carte-info-label">Modules</span>
                  <span className="classe-carte-info-value">{classe.nombreModules}</span>
                </div>
                <div className="classe-carte-info-item">
                  <BadgeStatut
                    statut={classe.statut === 'Actif' ? 'success' : 'error'}
                    texte={classe.statut}
                  />
                </div>
              </div>

              <div className="classe-carte-actions">
                <button
                  className="classe-carte-btn-details"
                  onClick={() => gererVoirModules(classe)}
                >
                  <Eye size={16} />
                  Détails
                </button>
              </div>
            </div>
          ))}
        </div>
      )}
    </div>
  );
};

export default GestionClasses;
