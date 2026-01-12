package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.AdministrateurDTO;
import com.mycampus.backend.dto.ProfesseurDTO;
import com.mycampus.backend.dto.EtudiantDTO;
import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Classe;
import com.mycampus.backend.metier.administrateur.structureAcademique.GestionStructureAcademiqueMetier;
import com.mycampus.backend.repositorys.*;
import com.mycampus.backend.mappers.UtilisateursMapper;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionUtilisateursMetierImpl implements GestionUtilisateursMetier {

    private final AdministrateurRepository administrateurRepository;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateursMapper utilisateursMapper;
    private final ProfesseurRepository professeurRepository;
    private final EtudiantRepository etudiantRepository;
    private final DepartementRepository departementRepository;
    private final FiliereRepository filiereRepository;
    private final ClasseRepository classeRepository;
    //private final PasswordEncoder passwordEncoder;

    @Override
    public AdministrateurDTO creerAdministrateur(AdministrateurDTO administrateurDTO) throws UtilisateursException {
        if (administrateurDTO == null) {
            throw new UtilisateursException("L'administrateur ne peut pas être nul");
        }
        
        if (administrateurDTO.getPpr() == null || administrateurDTO.getPpr().trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur est obligatoire");
        }
        
        if (administrateurRepository.existsByPpr(administrateurDTO.getPpr())) {
            throw new UtilisateursException("Un administrateur avec ce PPR existe déjà");
        }
        
        if (administrateurDTO.getEmail() == null || administrateurDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'administrateur est obligatoire");
        }
        
        if (utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }
        
        if (administrateurDTO.getCin() == null || administrateurDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de l'administrateur est obligatoire");
        }
        
        if (utilisateurRepository.existsByCin(administrateurDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }
        
        if (administrateurDTO.getPassword() == null || administrateurDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de l'administrateur est obligatoire");
        }
        
        Administrateur administrateur = utilisateursMapper.versAdministrateur(administrateurDTO);
        administrateur.setDateCreation(LocalDateTime.now());
        administrateur.setEnabled(true);
      //  administrateur.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));

        
        Administrateur administrateurSauvegarde = administrateurRepository.save(administrateur);
        return utilisateursMapper.versAdministrateurDTO(administrateurSauvegarde);
    }

    @Override
    public AdministrateurDTO trouverAdministrateurParId(Long id) throws UtilisateursException {
        if (id == null) {
            throw  new UtilisateursException(
                    "id est obligatoire"
            );
        }
        
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec l'identifiant: " + id));
        return utilisateursMapper.versAdministrateurDTO(administrateur);
    }

    @Override
    public AdministrateurDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException {
        if (ppr == null) {
            throw  new UtilisateursException(
                    "id est obligatoire"
            );
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr);
        if(administrateur == null){
          throw   new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + ppr);
        }


        return utilisateursMapper.versAdministrateurDTO(administrateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrateurDTO> listerTousLesAdministrateurs() throws UtilisateursException {
        try {
            List<Administrateur> administrateurs = administrateurRepository.findAll();

            
            return administrateurs.stream().map(utilisateursMapper::versAdministrateurDTO).toList() ;
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des administrateurs: " + e.getMessage(), e);
        }
    }

    @Override
    public AdministrateurDTO modifierAdministrateur(Long id, AdministrateurDTO administrateurDTO) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'administrateur ne peut pas être nul");
        }
        
        Administrateur adminAModifier = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur introuvable avec l'identifiant: " + id));

        

        if (administrateurDTO.getNom() != null && !administrateurDTO.getNom().trim().isEmpty()) {
            adminAModifier.setNom(administrateurDTO.getNom());
        }
        
        if (administrateurDTO.getPrenom() != null && !administrateurDTO.getPrenom().trim().isEmpty()) {
            adminAModifier.setPrenom(administrateurDTO.getPrenom());
        }
        
        if (administrateurDTO.getEmail() != null && !administrateurDTO.getEmail().trim().isEmpty()) {
            if (!adminAModifier.getEmail().equals(administrateurDTO.getEmail()) && 
                utilisateurRepository.existsByEmail(administrateurDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            adminAModifier.setEmail(administrateurDTO.getEmail());
        }
        
        if (administrateurDTO.getFonction() != null) {
            adminAModifier.setFonction(administrateurDTO.getFonction());
        }
        
        if (administrateurDTO.getGrade() != null && !administrateurDTO.getGrade().trim().isEmpty()) {
            adminAModifier.setGrade(administrateurDTO.getGrade());
        }

       /* if(administrateurDTO.getPassword() != null && !administrateurDTO.getPassword().trim().isEmpty()) {
            adminAModifier.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));
        }*/

        
        Administrateur administrateurModifie = administrateurRepository.save(adminAModifier);
        return utilisateursMapper.versAdministrateurDTO(administrateurModifie);
    }

    @Override
    public void supprimerAdministrateur(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'administrateur ne peut pas être nul");
        }

        Administrateur administrateur = administrateurRepository.findById(id)
                .orElseThrow(() -> new UtilisateursException("Administrateur introuvable avec l'identifiant: " + id));
        
        administrateurRepository.delete(administrateur);
    }



    @Override
    public void activerAdministrateur(Long id) throws UtilisateursException {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));

        
        administrateur.setEnabled(true);
        administrateurRepository.save(administrateur);
    }

    @Override
    public void desactiverAdministrateur(Long id) throws UtilisateursException {
        Administrateur administrateur = administrateurRepository.findById(id).orElseThrow(()->
                new UtilisateursException("Administrateur non trouvé avec l'identifiant: " + id));



        administrateur.setEnabled(false);
        administrateurRepository.save(administrateur);
    }


    @Transactional
    public ProfesseurDTO creerProfesseur(ProfesseurDTO professeurDTO) throws UtilisateursException {

        if(professeurDTO == null){
            throw new UtilisateursException("Professeur ne peut pas être nul");
        }

        if (professeurDTO.getPpr() == null || professeurDTO.getPpr().trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de le professeur est obligatoire");
        }

        if (professeurRepository.existsByPpr(professeurDTO.getPpr())) {
            throw new UtilisateursException("Un professeur avec ce PPR existe déjà");
        }

        if (professeurDTO.getEmail() == null || professeurDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de le professeur est obligatoire");
        }

        if (utilisateurRepository.existsByEmail(professeurDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }

        if (professeurDTO.getCin() == null || professeurDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de le professeur est obligatoire");
        }

        if (utilisateurRepository.existsByCin(professeurDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }

        if (professeurDTO.getPassword() == null || professeurDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de le professeur est obligatoire");
        }

        Professeur professeur =  utilisateursMapper.versProfesseur(professeurDTO);

        if (professeurDTO.getDepartementDirigeeId() != null) {
            Departement dep = departementRepository.findById(professeurDTO.getDepartementDirigeeId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département dirigé introuvable: " + professeurDTO.getDepartementDirigeeId()
                    ));
            professeur.setDepartementDirigee(dep);
        }

        if (professeurDTO.getDepartementOuTravailId() != null) {
            Departement dep = departementRepository.findById(professeurDTO.getDepartementOuTravailId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurDTO.getDepartementOuTravailId()
                    ));
            professeur.setDepartementOuTravail(dep);
        }

        if (professeurDTO.getFiliereDirigeeId() != null) {
            Filiere fil = filiereRepository.findById(professeurDTO.getFiliereDirigeeId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Filière dirigée introuvable: " + professeurDTO.getFiliereDirigeeId()
                    ));
            professeur.setFiliereDirigee(fil);
        }

        professeur.setEnabled(true);
        professeur.setDateCreation(LocalDateTime.now());
        professeur.setDateDerniereConnexion(null);



        return utilisateursMapper.versProfesseurDTO(professeurRepository.save(professeur));
    }

    @Override
    public ProfesseurDTO trouverProfesseurParId(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("id est obligatoire");
        }
        
        Professeur professeur = professeurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec l'identifiant: " + id));
        return utilisateursMapper.versProfesseurDTO(professeur);
    }

    @Override
    public ProfesseurDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException {
        if (ppr == null) {
            throw new UtilisateursException("id est obligatoire");
        }
        
        Professeur professeur = professeurRepository.findByPpr(ppr);
        if(professeur == null){
            throw new UtilisateursException("Professeur introuvable avec l'identifiant: " + ppr);
        }

        return utilisateursMapper.versProfesseurDTO(professeur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesseurDTO> listerTousLesProfesseurs() throws UtilisateursException {
        try {
            List<Professeur> professeurs = professeurRepository.findAll();
            return professeurs.stream().map(utilisateursMapper::versProfesseurDTO).toList();
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des professeurs: " + e.getMessage(), e);
        }
    }

    @Override
    public ProfesseurDTO modifierProfesseur(Long id, ProfesseurDTO professeurDTO) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant du professeur ne peut pas être nul");
        }
        
        if (professeurDTO == null) {
            throw new UtilisateursException("Le professeur ne peut pas être nul");
        }
        
        Professeur professeurAModifier = professeurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec l'identifiant: " + id));

        if (professeurDTO.getNom() != null && !professeurDTO.getNom().trim().isEmpty()) {
            professeurAModifier.setNom(professeurDTO.getNom());
        }
        
        if (professeurDTO.getPrenom() != null && !professeurDTO.getPrenom().trim().isEmpty()) {
            professeurAModifier.setPrenom(professeurDTO.getPrenom());
        }
        
        if (professeurDTO.getEmail() != null && !professeurDTO.getEmail().trim().isEmpty()) {
            if (!professeurAModifier.getEmail().equals(professeurDTO.getEmail()) && 
                utilisateurRepository.existsByEmail(professeurDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            professeurAModifier.setEmail(professeurDTO.getEmail());
        }
        
        if (professeurDTO.getSpecialite() != null && !professeurDTO.getSpecialite().trim().isEmpty()) {
            professeurAModifier.setSpecialite(professeurDTO.getSpecialite());
        }
        
        if (professeurDTO.getGrade() != null && !professeurDTO.getGrade().trim().isEmpty()) {
            professeurAModifier.setGrade(professeurDTO.getGrade());
        }
        
        if (professeurDTO.getDateRecrutement() != null) {
            professeurAModifier.setDateRecrutement(professeurDTO.getDateRecrutement());
        }
        
        if (professeurDTO.getStatut() != null && !professeurDTO.getStatut().trim().isEmpty()) {
            professeurAModifier.setStatut(professeurDTO.getStatut());
        }

        // Relations optionnelles
        if (professeurDTO.getDepartementDirigeeId() != null) {
            Departement dep = departementRepository.findById(professeurDTO.getDepartementDirigeeId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département dirigé introuvable: " + professeurDTO.getDepartementDirigeeId()
                    ));
            professeurAModifier.setDepartementDirigee(dep);
        }

        if (professeurDTO.getDepartementOuTravailId() != null) {
            Departement dep = departementRepository.findById(professeurDTO.getDepartementOuTravailId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurDTO.getDepartementOuTravailId()
                    ));
            professeurAModifier.setDepartementOuTravail(dep);
        }

        if (professeurDTO.getFiliereDirigeeId() != null) {
            Filiere fil = filiereRepository.findById(professeurDTO.getFiliereDirigeeId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Filière dirigée introuvable: " + professeurDTO.getFiliereDirigeeId()
                    ));
            professeurAModifier.setFiliereDirigee(fil);
        }

        /* if(professeurDTO.getPassword() != null && !professeurDTO.getPassword().trim().isEmpty()) {
            professeurAModifier.setPassword(passwordEncoder.encode(professeurDTO.getPassword()));
        }*/

        Professeur professeurModifie = professeurRepository.save(professeurAModifier);
        return utilisateursMapper.versProfesseurDTO(professeurModifie);
    }

    @Override
    public void supprimerProfesseur(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant du professeur ne peut pas être nul");
        }

        Professeur professeur = professeurRepository.findById(id)
                .orElseThrow(() -> new UtilisateursException("Professeur introuvable avec l'identifiant: " + id));
        
        professeurRepository.delete(professeur);
    }

    @Override
    public void activerProfesseur(Long id) throws UtilisateursException {
        Professeur professeur = professeurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec l'identifiant: " + id));

        professeur.setEnabled(true);
        professeurRepository.save(professeur);
    }

    @Override
    public void desactiverProfesseur(Long id) throws UtilisateursException {
        Professeur professeur = professeurRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec l'identifiant: " + id));

        professeur.setEnabled(false);
        professeurRepository.save(professeur);
    }

    // ==================== ETUDIANT ====================

    @Override
    public EtudiantDTO creerEtudiant(EtudiantDTO etudiantDTO) throws UtilisateursException {
        if (etudiantDTO == null) {
            throw new UtilisateursException("L'étudiant ne peut pas être nul");
        }

        if (etudiantDTO.getEmail() == null || etudiantDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'étudiant est obligatoire");
        }

        if (utilisateurRepository.existsByEmail(etudiantDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }

        if (etudiantDTO.getCin() == null || etudiantDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de l'étudiant est obligatoire");
        }

        if (utilisateurRepository.existsByCin(etudiantDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }

        if (etudiantDTO.getPassword() == null || etudiantDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de l'étudiant est obligatoire");
        }

        if (etudiantDTO.getApogee() == null || etudiantDTO.getApogee().trim().isEmpty()) {
            throw new UtilisateursException("Le numéro Apogée de l'étudiant est obligatoire");
        }

        if (etudiantRepository.existsByApogee(etudiantDTO.getApogee())) {
            throw new UtilisateursException("Un étudiant avec ce numéro Apogée existe déjà");
        }

        if (etudiantDTO.getClasseId() == null) {
            throw new UtilisateursException("La classe de l'étudiant est obligatoire");
        }

        Etudiant etudiant = utilisateursMapper.versEtudiant(etudiantDTO);

        Classe classe = classeRepository.findById(etudiantDTO.getClasseId())
                .orElseThrow(() -> new UtilisateursException(
                        "Classe introuvable: " + etudiantDTO.getClasseId()
                ));
        etudiant.setClasse(classe);

        etudiant.setEnabled(true);
        etudiant.setDateCreation(LocalDateTime.now());

        return utilisateursMapper.versEtudiantDTO(etudiantRepository.save(etudiant));
    }

    @Override
    public EtudiantDTO trouverEtudiantParId(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("id est obligatoire");
        }
        
        Etudiant etudiant = etudiantRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'identifiant: " + id));
        return utilisateursMapper.versEtudiantDTO(etudiant);
    }

    @Override
    public EtudiantDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException {
        if (apogee == null) {
            throw new UtilisateursException("id est obligatoire");
        }
        
        Etudiant etudiant = etudiantRepository.findByApogee(apogee);
        if(etudiant == null){
            throw new UtilisateursException("Étudiant introuvable avec l'identifiant: " + apogee);
        }

        return utilisateursMapper.versEtudiantDTO(etudiant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtudiantDTO> listerTousLesEtudiants() throws UtilisateursException {
        try {
            List<Etudiant> etudiants = etudiantRepository.findAll();
            return etudiants.stream().map(utilisateursMapper::versEtudiantDTO).toList();
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des étudiants: " + e.getMessage(), e);
        }
    }

    @Override
    public EtudiantDTO modifierEtudiant(Long id, EtudiantDTO etudiantDTO) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'étudiant ne peut pas être nul");
        }
        
        if (etudiantDTO == null) {
            throw new UtilisateursException("L'étudiant ne peut pas être nul");
        }
        
        Etudiant etudiantAModifier = etudiantRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'identifiant: " + id));

        if (etudiantDTO.getNom() != null && !etudiantDTO.getNom().trim().isEmpty()) {
            etudiantAModifier.setNom(etudiantDTO.getNom());
        }
        
        if (etudiantDTO.getPrenom() != null && !etudiantDTO.getPrenom().trim().isEmpty()) {
            etudiantAModifier.setPrenom(etudiantDTO.getPrenom());
        }
        
        if (etudiantDTO.getEmail() != null && !etudiantDTO.getEmail().trim().isEmpty()) {
            if (!etudiantAModifier.getEmail().equals(etudiantDTO.getEmail()) && 
                utilisateurRepository.existsByEmail(etudiantDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            etudiantAModifier.setEmail(etudiantDTO.getEmail());
        }
        
        if (etudiantDTO.getNiveau() != null && !etudiantDTO.getNiveau().trim().isEmpty()) {
            etudiantAModifier.setNiveau(etudiantDTO.getNiveau());
        }
        
        if (etudiantDTO.getAnneeInscription() != null) {
            etudiantAModifier.setAnneeInscription(etudiantDTO.getAnneeInscription());
        }
        
        if (etudiantDTO.getSemestreActuel() != null) {
            etudiantAModifier.setSemestreActuel(etudiantDTO.getSemestreActuel());
        }
        
        if (etudiantDTO.getAnneeFinEtude() != null && !etudiantDTO.getAnneeFinEtude().trim().isEmpty()) {
            etudiantAModifier.setAnneeFinEtude(etudiantDTO.getAnneeFinEtude());
        }
        
        if (etudiantDTO.getDateNaissance() != null) {
            etudiantAModifier.setDateNaissance(etudiantDTO.getDateNaissance());
        }
        
        if (etudiantDTO.getEtatInscription() != null) {
            etudiantAModifier.setEtatInscription(etudiantDTO.getEtatInscription());
        }

        if (etudiantDTO.getClasseId() != null) {
            Classe classe = classeRepository.findById(etudiantDTO.getClasseId())
                    .orElseThrow(() -> new UtilisateursException(
                            "Classe introuvable: " + etudiantDTO.getClasseId()
                    ));
            etudiantAModifier.setClasse(classe);
        }

        /* if(etudiantDTO.getPassword() != null && !etudiantDTO.getPassword().trim().isEmpty()) {
            etudiantAModifier.setPassword(passwordEncoder.encode(etudiantDTO.getPassword()));
        }*/

        Etudiant etudiantModifie = etudiantRepository.save(etudiantAModifier);
        return utilisateursMapper.versEtudiantDTO(etudiantModifie);
    }

    @Override
    public void supprimerEtudiant(Long id) throws UtilisateursException {
        if (id == null) {
            throw new UtilisateursException("L'identifiant de l'étudiant ne peut pas être nul");
        }

        Etudiant etudiant = etudiantRepository.findById(id)
                .orElseThrow(() -> new UtilisateursException("Étudiant introuvable avec l'identifiant: " + id));
        
        etudiantRepository.delete(etudiant);
    }

    @Override
    public void activerEtudiant(Long id) throws UtilisateursException {
        Etudiant etudiant = etudiantRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'identifiant: " + id));

        etudiant.setEnabled(true);
        etudiantRepository.save(etudiant);
    }

    @Override
    public void desactiverEtudiant(Long id) throws UtilisateursException {
        Etudiant etudiant = etudiantRepository.findById(id).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'identifiant: " + id));

        etudiant.setEnabled(false);
        etudiantRepository.save(etudiant);
    }
}
