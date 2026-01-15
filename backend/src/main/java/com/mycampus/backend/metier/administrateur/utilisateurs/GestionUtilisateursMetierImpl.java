package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.administrateur.DonneEntrer.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.entites.Administrateur;
import com.mycampus.backend.entites.Departement;
import com.mycampus.backend.entites.Filiere;
import com.mycampus.backend.entites.Professeur;
import com.mycampus.backend.entites.Etudiant;
import com.mycampus.backend.entites.Classe;
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
    public AdministrateurSortieDTO creerAdministrateur(AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException {
        if (administrateurEntrerDTO == null) {
            throw new UtilisateursException("L'administrateur ne peut pas être nul");
        }
        
        if (administrateurEntrerDTO.getPpr() == null || administrateurEntrerDTO.getPpr().trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur est obligatoire");
        }
        
        if (administrateurRepository.existsByPpr(administrateurEntrerDTO.getPpr())) {
            throw new UtilisateursException("Un administrateur avec ce PPR existe déjà");
        }
        
        if (administrateurEntrerDTO.getEmail() == null || administrateurEntrerDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'administrateur est obligatoire");
        }
        
        if (utilisateurRepository.existsByEmail(administrateurEntrerDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }
        
        if (administrateurEntrerDTO.getCin() == null || administrateurEntrerDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de l'administrateur est obligatoire");
        }
        
        if (utilisateurRepository.existsByCin(administrateurEntrerDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }
        
        if (administrateurEntrerDTO.getPassword() == null || administrateurEntrerDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de l'administrateur est obligatoire");
        }
        
        Administrateur administrateur = utilisateursMapper.FromAdministrateurEntrerVersAdministrateur(administrateurEntrerDTO);
        administrateur.setDateCreation(LocalDateTime.now());
        administrateur.setEnabled(true);
      //  administrateur.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));

        
        Administrateur administrateurSauvegarde = administrateurRepository.save(administrateur);
        return utilisateursMapper.FromAdministrateurVersAdministrateurSortieDTO(administrateurSauvegarde);
    }

    @Override
    public AdministrateurSortieDTO trouverAdministrateurParPpr(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur est obligatoire");
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        return utilisateursMapper.FromAdministrateurVersAdministrateurSortieDTO(administrateur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<AdministrateurSortieDTO> listerTousLesAdministrateurs() throws UtilisateursException {
        try {
            List<Administrateur> administrateurs = administrateurRepository.findAll();

            
            return administrateurs.stream().map(utilisateursMapper::FromAdministrateurVersAdministrateurSortieDTO).toList() ;
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des administrateurs: " + e.getMessage(), e);
        }
    }

    @Override
    public AdministrateurSortieDTO modifierAdministrateur(String ppr, AdministrateurEntrerDTO administrateurEntrerDTO) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }
        
        if (administrateurEntrerDTO == null) {
            throw new UtilisateursException("Les données de l'administrateur ne peuvent pas être nulles");
        }
        
        Administrateur adminAModifier = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        

        if (administrateurEntrerDTO.getNom() != null && !administrateurEntrerDTO.getNom().trim().isEmpty()) {
            adminAModifier.setNom(administrateurEntrerDTO.getNom());
        }
        
        if (administrateurEntrerDTO.getPrenom() != null && !administrateurEntrerDTO.getPrenom().trim().isEmpty()) {
            adminAModifier.setPrenom(administrateurEntrerDTO.getPrenom());
        }
        
        if (administrateurEntrerDTO.getEmail() != null && !administrateurEntrerDTO.getEmail().trim().isEmpty()) {
            if (!adminAModifier.getEmail().equals(administrateurEntrerDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(administrateurEntrerDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            adminAModifier.setEmail(administrateurEntrerDTO.getEmail());
        }
        
        if (administrateurEntrerDTO.getFonction() != null) {
            adminAModifier.setFonction(administrateurEntrerDTO.getFonction());
        }
        
        if (administrateurEntrerDTO.getGrade() != null && !administrateurEntrerDTO.getGrade().trim().isEmpty()) {
            adminAModifier.setGrade(administrateurEntrerDTO.getGrade());
        }

       /* if(administrateurDTO.getPassword() != null && !administrateurDTO.getPassword().trim().isEmpty()) {
            adminAModifier.setPassword(passwordEncoder.encode(administrateurDTO.getPassword()));
        }*/

        
        Administrateur administrateurModifie = administrateurRepository.save(adminAModifier);
        return utilisateursMapper.FromAdministrateurVersAdministrateurSortieDTO(administrateurModifie);
    }

    @Override
    public void supprimerAdministrateur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }

        Administrateur administrateur = administrateurRepository.findByPpr(ppr)
                .orElseThrow(() -> new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));
        
        administrateurRepository.delete(administrateur);
    }



    @Override
    public void activerAdministrateur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        administrateur.setEnabled(true);
        administrateurRepository.save(administrateur);
    }

    @Override
    public void desactiverAdministrateur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        administrateur.setEnabled(false);
        administrateurRepository.save(administrateur);
    }


    @Transactional
    public ProfesseurSortieDTO creerProfesseur(ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException {

        if(professeurEntrerDTO == null){
            throw new UtilisateursException("Professeur ne peut pas être nul");
        }

        if (professeurEntrerDTO.getPpr() == null || professeurEntrerDTO.getPpr().trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de le professeur est obligatoire");
        }

        if (professeurRepository.existsByPpr(professeurEntrerDTO.getPpr())) {
            throw new UtilisateursException("Un professeur avec ce PPR existe déjà");
        }

        if (professeurEntrerDTO.getEmail() == null || professeurEntrerDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de le professeur est obligatoire");
        }

        if (utilisateurRepository.existsByEmail(professeurEntrerDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }

        if (professeurEntrerDTO.getCin() == null || professeurEntrerDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de le professeur est obligatoire");
        }

        if (utilisateurRepository.existsByCin(professeurEntrerDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }

        if (professeurEntrerDTO.getPassword() == null || professeurEntrerDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de le professeur est obligatoire");
        }

        Professeur professeur =  utilisateursMapper.FromProfesseurEntrerversProfesseur(professeurEntrerDTO);

        if (professeurEntrerDTO.getCodeDepartementDirigee() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurEntrerDTO.getCodeDepartementDirigee())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département dirigé introuvable: " + professeurEntrerDTO.getCodeDepartementDirigee()
                    ));
            professeur.setDepartementDirigee(dep);
        }

        if (professeurEntrerDTO.getCodeDepartementOuTravail() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurEntrerDTO.getCodeDepartementOuTravail())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurEntrerDTO.getCodeDepartementOuTravail()
                    ));
            professeur.setDepartementOuTravail(dep);
        }

        if (professeurEntrerDTO.getCodeFiliereDirigee() != null) {
            Filiere fil = filiereRepository.findByCodeFiliere(professeurEntrerDTO.getCodeFiliereDirigee())
                    .orElseThrow(() -> new UtilisateursException(
                            "Filière dirigée introuvable: " + professeurEntrerDTO.getCodeFiliereDirigee()
                    ));
            professeur.setFiliereDirigee(fil);
        }

        professeur.setEnabled(true);
        professeur.setDateCreation(LocalDateTime.now());
        professeur.setDateDerniereConnexion(null);



        return utilisateursMapper.FromProfesseurVersProfesseurSortirDTO(professeurRepository.save(professeur));
    }

    @Override
    public ProfesseurSortieDTO trouverProfesseurParPpr(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur est obligatoire");
        }
        
        Professeur professeur = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        return utilisateursMapper.FromProfesseurVersProfesseurSortirDTO(professeur);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesseurSortieDTO> listerTousLesProfesseurs() throws UtilisateursException {
        try {
            List<Professeur> professeurs = professeurRepository.findAll();
            return professeurs.stream().map(utilisateursMapper::FromProfesseurVersProfesseurSortirDTO).toList();
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des professeurs: " + e.getMessage(), e);
        }
    }

    @Override
    public ProfesseurSortieDTO modifierProfesseur(String ppr, ProfesseurEntrerDTO professeurEntrerDTO) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }
        
        if (professeurEntrerDTO == null) {
            throw new UtilisateursException("Le professeur ne peut pas être nul");
        }
        
        Professeur professeurAModifier = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        if (professeurEntrerDTO.getNom() != null && !professeurEntrerDTO.getNom().trim().isEmpty()) {
            professeurAModifier.setNom(professeurEntrerDTO.getNom());
        }
        
        if (professeurEntrerDTO.getPrenom() != null && !professeurEntrerDTO.getPrenom().trim().isEmpty()) {
            professeurAModifier.setPrenom(professeurEntrerDTO.getPrenom());
        }
        
        if (professeurEntrerDTO.getEmail() != null && !professeurEntrerDTO.getEmail().trim().isEmpty()) {
            if (!professeurAModifier.getEmail().equals(professeurEntrerDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(professeurEntrerDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            professeurAModifier.setEmail(professeurEntrerDTO.getEmail());
        }
        
        if (professeurEntrerDTO.getSpecialite() != null && !professeurEntrerDTO.getSpecialite().trim().isEmpty()) {
            professeurAModifier.setSpecialite(professeurEntrerDTO.getSpecialite());
        }
        
        if (professeurEntrerDTO.getGrade() != null && !professeurEntrerDTO.getGrade().trim().isEmpty()) {
            professeurAModifier.setGrade(professeurEntrerDTO.getGrade());
        }
        
        if (professeurEntrerDTO.getDateRecrutement() != null) {
            professeurAModifier.setDateRecrutement(professeurEntrerDTO.getDateRecrutement());
        }
        
        if (professeurEntrerDTO.getStatut() != null && !professeurEntrerDTO.getStatut().trim().isEmpty()) {
            professeurAModifier.setStatut(professeurEntrerDTO.getStatut());
        }

        // Relations optionnelles
        if (professeurEntrerDTO.getCodeDepartementDirigee() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurEntrerDTO.getCodeDepartementDirigee())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département dirigé introuvable: " + professeurEntrerDTO.getCodeDepartementDirigee()
                    ));
            professeurAModifier.setDepartementDirigee(dep);
        }

        if (professeurEntrerDTO.getCodeDepartementOuTravail() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurEntrerDTO.getCodeDepartementOuTravail())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurEntrerDTO.getCodeDepartementOuTravail()
                    ));
            professeurAModifier.setDepartementOuTravail(dep);
        }

        if (professeurEntrerDTO.getCodeFiliereDirigee() != null) {
            Filiere fil = filiereRepository.findByCodeFiliere(professeurEntrerDTO.getCodeFiliereDirigee())
                    .orElseThrow(() -> new UtilisateursException(
                            "Filière dirigée introuvable: " + professeurEntrerDTO.getCodeFiliereDirigee()
                    ));
            professeurAModifier.setFiliereDirigee(fil);
        }

        /* if(professeurDTO.getPassword() != null && !professeurDTO.getPassword().trim().isEmpty()) {
            professeurAModifier.setPassword(passwordEncoder.encode(professeurDTO.getPassword()));
        }*/

        Professeur professeurModifie = professeurRepository.save(professeurAModifier);
        return utilisateursMapper.FromProfesseurVersProfesseurSortirDTO(professeurModifie);
    }

    @Override
    public void supprimerProfesseur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }

        Professeur professeur = professeurRepository.findByPpr(ppr)
                .orElseThrow(() -> new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));
        
        professeurRepository.delete(professeur);
    }

    @Override
    public void activerProfesseur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }
        
        Professeur professeur = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        professeur.setEnabled(true);
        professeurRepository.save(professeur);
    }

    @Override
    public void desactiverProfesseur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }
        
        Professeur professeur = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        professeur.setEnabled(false);
        professeurRepository.save(professeur);
    }

    // ==================== ETUDIANT ====================

    @Override
    public EtudiantSortieDTO creerEtudiant(EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException {
        if (etudiantEntrerDTO == null) {
            throw new UtilisateursException("L'étudiant ne peut pas être nul");
        }

        if (etudiantEntrerDTO.getEmail() == null || etudiantEntrerDTO.getEmail().trim().isEmpty()) {
            throw new UtilisateursException("L'email de l'étudiant est obligatoire");
        }

        if (utilisateurRepository.existsByEmail(etudiantEntrerDTO.getEmail())) {
            throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
        }

        if (etudiantEntrerDTO.getCin() == null || etudiantEntrerDTO.getCin().trim().isEmpty()) {
            throw new UtilisateursException("Le CIN de l'étudiant est obligatoire");
        }

        if (utilisateurRepository.existsByCin(etudiantEntrerDTO.getCin())) {
            throw new UtilisateursException("Un utilisateur avec ce CIN existe déjà");
        }

        if (etudiantEntrerDTO.getPassword() == null || etudiantEntrerDTO.getPassword().trim().isEmpty()) {
            throw new UtilisateursException("Le mot de passe de l'étudiant est obligatoire");
        }

        if (etudiantEntrerDTO.getApogee() == null || etudiantEntrerDTO.getApogee().trim().isEmpty()) {
            throw new UtilisateursException("Le numéro Apogée de l'étudiant est obligatoire");
        }

        if (etudiantRepository.existsByApogee(etudiantEntrerDTO.getApogee())) {
            throw new UtilisateursException("Un étudiant avec ce numéro Apogée existe déjà");
        }

        if (etudiantEntrerDTO.getCodeClasse() == null) {
            throw new UtilisateursException("La classe de l'étudiant est obligatoire");
        }

        Etudiant etudiant = utilisateursMapper.FromEtudiantEntrerversEtudiant(etudiantEntrerDTO);

        Classe classe = classeRepository.findByCodeClasse(etudiantEntrerDTO.getCodeClasse())
                .orElseThrow(() -> new UtilisateursException(
                        "Classe introuvable: " + etudiantEntrerDTO.getCodeClasse()
                ));
        etudiant.setClasse(classe);

        etudiant.setEnabled(true);
        etudiant.setDateCreation(LocalDateTime.now());

        return utilisateursMapper.FromEtudientVersEtudiantSortieDTO(etudiantRepository.save(etudiant));
    }

    @Override
    public EtudiantSortieDTO trouverEtudiantParApogee(String apogee) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant est obligatoire");
        }
        
        Etudiant etudiant = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        return utilisateursMapper.FromEtudientVersEtudiantSortieDTO(etudiant);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EtudiantSortieDTO> listerTousLesEtudiants() throws UtilisateursException {
        try {
            List<Etudiant> etudiants = etudiantRepository.findAll();
            return etudiants.stream().map(utilisateursMapper::FromEtudientVersEtudiantSortieDTO).toList();
        } catch (Exception e) {
            throw new UtilisateursException("Erreur lors de la liste des étudiants: " + e.getMessage(), e);
        }
    }

    @Override
    public EtudiantSortieDTO modifierEtudiant(String apogee, EtudiantEntrerDTO etudiantEntrerDTO) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }
        
        if (etudiantEntrerDTO == null) {
            throw new UtilisateursException("L'étudiant ne peut pas être nul");
        }
        
        Etudiant etudiantAModifier = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        if (etudiantEntrerDTO.getNom() != null && !etudiantEntrerDTO.getNom().trim().isEmpty()) {
            etudiantAModifier.setNom(etudiantEntrerDTO.getNom());
        }
        
        if (etudiantEntrerDTO.getPrenom() != null && !etudiantEntrerDTO.getPrenom().trim().isEmpty()) {
            etudiantAModifier.setPrenom(etudiantEntrerDTO.getPrenom());
        }
        
        if (etudiantEntrerDTO.getEmail() != null && !etudiantEntrerDTO.getEmail().trim().isEmpty()) {
            if (!etudiantAModifier.getEmail().equals(etudiantEntrerDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(etudiantEntrerDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            etudiantAModifier.setEmail(etudiantEntrerDTO.getEmail());
        }
        
        if (etudiantEntrerDTO.getNiveau() != null && !etudiantEntrerDTO.getNiveau().trim().isEmpty()) {
            etudiantAModifier.setNiveau(etudiantEntrerDTO.getNiveau());
        }
        
        if (etudiantEntrerDTO.getAnneeInscription() != null) {
            etudiantAModifier.setAnneeInscription(etudiantEntrerDTO.getAnneeInscription());
        }
        
        if (etudiantEntrerDTO.getSemestreActuel() != null) {
            etudiantAModifier.setSemestreActuel(etudiantEntrerDTO.getSemestreActuel());
        }
        
        if (etudiantEntrerDTO.getAnneeFinEtude() != null && !etudiantEntrerDTO.getAnneeFinEtude().trim().isEmpty()) {
            etudiantAModifier.setAnneeFinEtude(etudiantEntrerDTO.getAnneeFinEtude());
        }
        
        if (etudiantEntrerDTO.getDateNaissance() != null) {
            etudiantAModifier.setDateNaissance(etudiantEntrerDTO.getDateNaissance());
        }
        
        if (etudiantEntrerDTO.getEtatInscription() != null) {
            etudiantAModifier.setEtatInscription(etudiantEntrerDTO.getEtatInscription());
        }

        if (etudiantEntrerDTO.getCodeClasse() != null) {
            Classe classe = classeRepository.findByCodeClasse(etudiantEntrerDTO.getCodeClasse())
                    .orElseThrow(() -> new UtilisateursException(
                            "Classe introuvable: " + etudiantEntrerDTO.getCodeClasse()
                    ));
            etudiantAModifier.setClasse(classe);
        }

        /* if(etudiantDTO.getPassword() != null && !etudiantDTO.getPassword().trim().isEmpty()) {
            etudiantAModifier.setPassword(passwordEncoder.encode(etudiantDTO.getPassword()));
        }*/

        Etudiant etudiantModifie = etudiantRepository.save(etudiantAModifier);
        return utilisateursMapper.FromEtudientVersEtudiantSortieDTO(etudiantModifie);
    }

    @Override
    public void supprimerEtudiant(String apogee) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }

        Etudiant etudiant = etudiantRepository.findByApogee(apogee)
                .orElseThrow(() -> new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));
        
        etudiantRepository.delete(etudiant);
    }

    @Override
    public void activerEtudiant(String apogee) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }
        
        Etudiant etudiant = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        etudiant.setEnabled(true);
        etudiantRepository.save(etudiant);
    }

    @Override
    public void desactiverEtudiant(String apogee) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }
        
        Etudiant etudiant = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        etudiant.setEnabled(false);
        etudiantRepository.save(etudiant);
    }
}
