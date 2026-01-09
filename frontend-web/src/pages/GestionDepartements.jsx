import { useState } from 'react';
import { Building2, Users, GraduationCap, UserPlus, Plus } from 'lucide-react';
import TableauDonnees from '../composants/communs/TableauDonnees';
import FormulaireModal from '../composants/communs/FormulaireModal';
import ChampFormulaire from '../composants/communs/ChampFormulaire';
import CarteStatistique from '../composants/tableauDeBord/CarteStatistique';
import BadgeStatut from '../composants/communs/BadgeStatut';
import BoutonAction from '../composants/communs/BoutonAction';
import './GestionDepartements.css';

const GestionDepartements = () => {
  const [departements, setDepartements] = useState([
    {
      id: 1,
      nom: 'Informatique',
      code: 'INFO',
      description: 'Département d\'Informatique et Génie Logiciel',
      chefDepartement: 'Ahmed Alami',
      nombreProfesseurs: 15,
      nombreFilieres: 3,
      nombreEtudiants: 245
    },
    {
      id: 2,
      nom: 'Industriel',
      code: 'IND',
      description: 'Département de Génie Industriel et Logistique',
      chefDepartement: 'Mohamed Chraibi',
      nombreProfesseurs: 12,
      nombreFilieres: 2,
      nombreEtudiants: 180
    },
    {
      id: 3,
      nom: 'Génie Civil',
      code: 'GC',
      description: 'Département de Génie Civil et Construction',
      chefDepartement: 'Hassan Alaoui',
      nombreProfesseurs: 10,
      nombreFilieres: 1,
      nombreEtudiants: 0
    }
  ]);

  const [professeurs] = useState([
    {
      id: 1,
      ppr: 'PPR001',
      nom: 'Alami',
      prenom: 'Ahmed',
      email: 'ahmed.alami@univ.ma',
      specialite: 'Informatique',
      grade: 'Professeur',
      departement: 'Informatique',
      status: 'Actif'
    },
    {
      id: 2,
      ppr: 'PPR002',
      nom: 'Bennani',
      prenom: 'Fatima',
      email: 'fatima.bennani@univ.ma',
      specialite: 'Mathématiques',
      grade: 'Maître de Conférences',
      departement: 'Informatique',
      status: 'Actif'
    },
    {
      id: 3,
      ppr: 'PPR003',
      nom: 'Chraibi',
      prenom: 'Mohamed',
      email: 'mohamed.chraibi@univ.ma',
      specialite: 'Génie Industriel',
      grade: 'Professeur',
      departement: 'Industriel',
      status: 'Actif'
    },
    {
      id: 4,
      ppr: 'PPR004',
      nom: 'El Amrani',
      prenom: 'Youssef',
      email: 'youssef.elamrani@univ.ma',
      specialite: 'Informatique',
      grade: 'Maître Assistant',
      departement: null,
      status: 'Actif'
    }
  ]);

  const [filieres] = useState([
    {
      id: 1,
      codeFiliere: 'GI-2024',
      nom: 'Génie Informatique',
      departement: 'Informatique',
      etat: 'OUVERTE'
    },
    {
      id: 2,
      codeFiliere: 'GIL-2024',
      nom: 'Génie Industriel et Logistique',
      departement: 'Industriel',
      etat: 'OUVERTE'
    },
    {
      id: 3,
      codeFiliere: 'GC-2024',
      nom: 'Génie Civil',
      departement: 'Génie Civil',
      etat: 'FERMEE'
    }
  ]);

  const [modalAjoutDepartement, setModalAjoutDepartement] = useState(false);
  const [modalAffecterProf, setModalAffecterProf] = useState(false);
  const [modalAjouterFiliere, setModalAjouterFiliere] = useState(false);
  const [departementSelectionne, setDepartementSelectionne] = useState(null);
  const [formulaireDepartement, setFormulaireDepartement] = useState({
    nom: '',
    code: '',
    description: '',
    chefDepartement: ''
  });
  const [formulaireFiliere, setFormulaireFiliere] = useState({
    codeFiliere: '',
    nom: '',
    etat: 'OUVERTE'
  });

  const ouvrirModalAjoutDepartement = () => {
    setFormulaireDepartement({
      nom: '',
      code: '',
      description: '',
      chefDepartement: ''
    });
    setModalAjoutDepartement(true);
  };

  const ouvrirModalAffecterProf = (departement) => {
    setDepartementSelectionne(departement);
    setModalAffecterProf(true);
  };

  const ouvrirModalAjouterFiliere = (departement) => {
    setDepartementSelectionne(departement);
    setFormulaireFiliere({
      codeFiliere: '',
      nom: '',
      etat: 'OUVERTE'
    });
    setModalAjouterFiliere(true);
  };

  const gererChangementDepartement = (e) => {
    const { name, value } = e.target;
    setFormulaireDepartement(prev => ({ ...prev, [name]: value }));
  };

  const gererChangementFiliere = (e) => {
    const { name, value } = e.target;
    setFormulaireFiliere(prev => ({ ...prev, [name]: value }));
  };

  const sauvegarderDepartement = () => {
    if (!formulaireDepartement.nom || !formulaireDepartement.code) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const nouveauDepartement = {
      id: departements.length + 1,
      ...formulaireDepartement,
      nombreProfesseurs: 0,
      nombreFilieres: 0,
      nombreEtudiants: 0
    };
    setDepartements(prev => [...prev, nouveauDepartement]);
    setModalAjoutDepartement(false);
  };

  const affecterProfesseur = (professeurId) => {
    if (!departementSelectionne) return;

    const professeur = professeurs.find(p => p.id === professeurId);
    if (!professeur) return;

    // Mettre à jour le département du professeur
    // Dans une vraie application, ceci serait fait via une API
    const departement = departements.find(d => d.id === departementSelectionne.id);
    if (departement) {
      setDepartements(prev => prev.map(d => 
        d.id === departement.id 
          ? { ...d, nombreProfesseurs: d.nombreProfesseurs + 1 }
          : d
      ));
    }

    alert(`Professeur ${professeur.prenom} ${professeur.nom} affecté au département ${departementSelectionne.nom}`);
    setModalAffecterProf(false);
  };

  const sauvegarderFiliere = () => {
    if (!formulaireFiliere.codeFiliere || !formulaireFiliere.nom || !departementSelectionne) {
      alert('Veuillez remplir tous les champs obligatoires');
      return;
    }

    const nouvelleFiliere = {
      id: filieres.length + 1,
      ...formulaireFiliere,
      departement: departementSelectionne.nom
    };

    // Mettre à jour le nombre de filières du département
    setDepartements(prev => prev.map(d => 
      d.id === departementSelectionne.id 
        ? { ...d, nombreFilieres: d.nombreFilieres + 1 }
        : d
    ));

    alert(`Filière ${nouvelleFiliere.nom} ajoutée au département ${departementSelectionne.nom}`);
    setModalAjouterFiliere(false);
  };

  const obtenirProfesseursDepartement = (departementNom) => {
    return professeurs.filter(p => p.departement === departementNom);
  };

  const obtenirFilieresDepartement = (departementNom) => {
    return filieres.filter(f => f.departement === departementNom);
  };

  const professeursNonAffectes = professeurs.filter(p => !p.departement);

  const colonnes = [
    { key: 'code', label: 'Code' },
    { key: 'nom', label: 'Nom' },
    { key: 'chefDepartement', label: 'Chef de Département' },
    { 
      key: 'nombreProfesseurs', 
      label: 'Professeurs',
      render: (item) => `${item.nombreProfesseurs} professeur(s)`
    },
    { 
      key: 'nombreFilieres', 
      label: 'Filières',
      render: (item) => `${item.nombreFilieres} filière(s)`
    },
    { 
      key: 'nombreEtudiants', 
      label: 'Étudiants',
      render: (item) => `${item.nombreEtudiants} étudiant(s)`
    }
  ];

  const totalDepartements = departements.length;
  const totalProfesseurs = departements.reduce((sum, d) => sum + d.nombreProfesseurs, 0);
  const totalFilieres = departements.reduce((sum, d) => sum + d.nombreFilieres, 0);

  return (
    <div className="gestion-departements">
      <div className="gestion-departements-header">
        <h1 className="gestion-departements-titre">Gestion des Départements</h1>
        <p className="gestion-departements-sous-titre">Gestion des départements, affectation des professeurs et filières</p>
      </div>

      <div className="gestion-departements-statistiques">
        <CarteStatistique
          titre="Total Départements"
          valeur={totalDepartements}
          variation={{ valeur: totalDepartements, label: 'départements', positif: true }}
          icone={Building2}
          couleur="primary"
        />
        <CarteStatistique
          titre="Total Professeurs"
          valeur={totalProfesseurs}
          variation={{ valeur: totalProfesseurs, label: 'professeurs', positif: true }}
          icone={Users}
          couleur="success"
        />
        <CarteStatistique
          titre="Total Filières"
          valeur={totalFilieres}
          variation={{ valeur: totalFilieres, label: 'filières', positif: true }}
          icone={GraduationCap}
          couleur="info"
        />
      </div>

      <TableauDonnees
        titre="Liste des Départements"
        colonnes={colonnes}
        donnees={departements}
        surAjouter={ouvrirModalAjoutDepartement}
        recherche={true}
        actions={false}
      />

      {/* Détails des départements */}
      <div className="gestion-departements-details">
        {departements.map(departement => {
          const profsDepartement = obtenirProfesseursDepartement(departement.nom);
          const filieresDepartement = obtenirFilieresDepartement(departement.nom);

          return (
            <div key={departement.id} className="departement-carte">
              <div className="departement-carte-header">
                <div>
                  <h3 className="departement-carte-titre">{departement.nom}</h3>
                  <p className="departement-carte-code">{departement.code}</p>
                  <p className="departement-carte-description">{departement.description}</p>
                </div>
                <div className="departement-carte-actions">
                  <BoutonAction
                    libelle="Affecter Prof"
                    variante="secondaire"
                    taille="moyen"
                    icone={UserPlus}
                    onClick={() => ouvrirModalAffecterProf(departement)}
                  />
                  <BoutonAction
                    libelle="Ajouter Filière"
                    variante="primaire"
                    taille="moyen"
                    icone={Plus}
                    onClick={() => ouvrirModalAjouterFiliere(departement)}
                  />
                </div>
              </div>

              <div className="departement-carte-contenu">
                <div className="departement-section">
                  <h4 className="departement-section-titre">
                    <Users size={18} />
                    Professeurs ({profsDepartement.length})
                  </h4>
                  {profsDepartement.length > 0 ? (
                    <div className="departement-liste">
                      {profsDepartement.map(prof => (
                        <div key={prof.id} className="departement-item">
                          <div>
                            <span className="departement-item-nom">{prof.prenom} {prof.nom}</span>
                            <span className="departement-item-detail">{prof.grade} - {prof.specialite}</span>
                          </div>
                          <BadgeStatut statut={prof.status === 'Actif' ? 'success' : 'error'} texte={prof.status} />
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="departement-vide">Aucun professeur affecté</p>
                  )}
                </div>

                <div className="departement-section">
                  <h4 className="departement-section-titre">
                    <GraduationCap size={18} />
                    Filières ({filieresDepartement.length})
                  </h4>
                  {filieresDepartement.length > 0 ? (
                    <div className="departement-liste">
                      {filieresDepartement.map(filiere => (
                        <div key={filiere.id} className="departement-item">
                          <div>
                            <span className="departement-item-nom">{filiere.nom}</span>
                            <span className="departement-item-detail">{filiere.codeFiliere}</span>
                          </div>
                          <BadgeStatut 
                            statut={filiere.etat === 'OUVERTE' ? 'success' : 'error'} 
                            texte={filiere.etat === 'OUVERTE' ? 'Ouverte' : 'Fermée'} 
                          />
                        </div>
                      ))}
                    </div>
                  ) : (
                    <p className="departement-vide">Aucune filière</p>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>

      {/* Modal Ajout Département */}
      <FormulaireModal
        titre="Ajouter un Département"
        estOuvert={modalAjoutDepartement}
        surFermer={() => setModalAjoutDepartement(false)}
        surSauvegarder={sauvegarderDepartement}
      >
        <ChampFormulaire
          label="Nom du Département"
          nom="nom"
          valeur={formulaireDepartement.nom}
          onChange={gererChangementDepartement}
          placeholder="Ex: Informatique"
          requis
        />
        <ChampFormulaire
          label="Code"
          nom="code"
          valeur={formulaireDepartement.code}
          onChange={gererChangementDepartement}
          placeholder="Ex: INFO"
          requis
        />
        <ChampFormulaire
          label="Description"
          nom="description"
          type="textarea"
          valeur={formulaireDepartement.description}
          onChange={gererChangementDepartement}
          placeholder="Description du département"
        />
        <ChampFormulaire
          label="Chef de Département"
          nom="chefDepartement"
          valeur={formulaireDepartement.chefDepartement}
          onChange={gererChangementDepartement}
          placeholder="Nom du chef de département"
        />
      </FormulaireModal>

      {/* Modal Affecter Professeur */}
      <FormulaireModal
        titre={`Affecter un Professeur - ${departementSelectionne?.nom || ''}`}
        estOuvert={modalAffecterProf}
        surFermer={() => setModalAffecterProf(false)}
        surSauvegarder={null}
      >
        {professeursNonAffectes.length > 0 ? (
          <div className="liste-professeurs">
            <p className="liste-professeurs-titre">Sélectionnez un professeur à affecter :</p>
            {professeursNonAffectes.map(prof => (
              <div key={prof.id} className="professeur-item-select">
                <div>
                  <span className="professeur-item-nom">{prof.prenom} {prof.nom}</span>
                  <span className="professeur-item-detail">{prof.grade} - {prof.specialite}</span>
                  <span className="professeur-item-email">{prof.email}</span>
                </div>
                <BoutonAction
                  libelle="Affecter"
                  variante="primaire"
                  taille="petit"
                  onClick={() => affecterProfesseur(prof.id)}
                />
              </div>
            ))}
          </div>
        ) : (
          <p>Tous les professeurs sont déjà affectés à un département.</p>
        )}
      </FormulaireModal>

      {/* Modal Ajouter Filière */}
      <FormulaireModal
        titre={`Ajouter une Filière - ${departementSelectionne?.nom || ''}`}
        estOuvert={modalAjouterFiliere}
        surFermer={() => setModalAjouterFiliere(false)}
        surSauvegarder={sauvegarderFiliere}
      >
        <ChampFormulaire
          label="Code Filière"
          nom="codeFiliere"
          valeur={formulaireFiliere.codeFiliere}
          onChange={gererChangementFiliere}
          placeholder="Ex: GI-2024"
          requis
        />
        <ChampFormulaire
          label="Nom de la Filière"
          nom="nom"
          valeur={formulaireFiliere.nom}
          onChange={gererChangementFiliere}
          placeholder="Ex: Génie Informatique"
          requis
        />
        <ChampFormulaire
          label="État"
          nom="etat"
          type="select"
          valeur={formulaireFiliere.etat}
          onChange={gererChangementFiliere}
          options={[
            { value: 'OUVERTE', label: 'Ouverte' },
            { value: 'FERMEE', label: 'Fermée' }
          ]}
          requis
        />
      </FormulaireModal>
    </div>
  );
};

export default GestionDepartements;

