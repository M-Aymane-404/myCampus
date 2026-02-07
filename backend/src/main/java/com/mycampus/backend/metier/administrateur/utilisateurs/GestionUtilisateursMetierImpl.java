package com.mycampus.backend.metier.administrateur.utilisateurs;

import com.mycampus.backend.dto.FichierStocker.FichierStockerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AdministrateurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.EtudiantEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ProfesseurEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AdministrateurConnecterModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.AdministrateurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.EtudiantModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.ProfesseurModificationDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AdministrateurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ProfesseurSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.EtudiantSortieDTO;
import com.mycampus.backend.entites.*;
import com.mycampus.backend.enumerations.RolesCode;
import com.mycampus.backend.exceptions.SecuriteException;
import com.mycampus.backend.metier.administrateur.PhotoStorage.PhotoStorageMetier;
import com.mycampus.backend.repositorys.*;
import com.mycampus.backend.mappers.administrateur.UtilisateursMapper;
import com.mycampus.backend.exceptions.UtilisateursException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.ArrayList;
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
    private final RoleRepository roleRepository;
    private final ClasseRepository classeRepository;
    private final PasswordEncoder passwordEncoder;
    private final UtilisateurRoleRepository utilisateurRoleRepository;
    private final PhotoStorageMetier photoStorageMetier;

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
        administrateur.setActive(true);
      administrateur.setPassword(passwordEncoder.encode(administrateurEntrerDTO.getPassword()));

        







         if (administrateur.getUtilisateurRoles() == null) {
            administrateur.setUtilisateurRoles(new ArrayList<>());
        }

        Role role =   roleRepository.findByNomRole(RolesCode.ADMIN)
                .orElseThrow(() -> new SecuriteException("Rôle introuvable: " + RolesCode.ADMIN.name()));

        Administrateur administrateurSauvegarde = administrateurRepository.save(administrateur);

         UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                .utilisateur(administrateurSauvegarde)
                .role(role)
                .dateAttribution(LocalDateTime.now())
                .actif(true)
                .build();

         administrateurSauvegarde.getUtilisateurRoles().add(utilisateurRole);

         utilisateurRoleRepository.save(utilisateurRole);



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
    public AdministrateurSortieDTO modifierAdministrateur(String ppr, AdministrateurModificationDTO administrateurModificationDTO) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }
        
        if (administrateurModificationDTO == null) {
            throw new UtilisateursException("Les données de l'administrateur ne peuvent pas être nulles");
        }
        
        Administrateur adminAModifier = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        

        if (administrateurModificationDTO.getNom() != null && !administrateurModificationDTO.getNom().trim().isEmpty()) {
            adminAModifier.setNom(administrateurModificationDTO.getNom());
        }
        
        if (administrateurModificationDTO.getPrenom() != null && !administrateurModificationDTO.getPrenom().trim().isEmpty()) {
            adminAModifier.setPrenom(administrateurModificationDTO.getPrenom());
        }
        
        if (administrateurModificationDTO.getEmail() != null && !administrateurModificationDTO.getEmail().trim().isEmpty()) {
            if (!adminAModifier.getEmail().equals(administrateurModificationDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(administrateurModificationDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            adminAModifier.setEmail(administrateurModificationDTO.getEmail());
        }
        
        if (administrateurModificationDTO.getFonction() != null) {
            adminAModifier.setFonction(administrateurModificationDTO.getFonction());
        }
        
        if (administrateurModificationDTO.getGrade() != null && !administrateurModificationDTO.getGrade().trim().isEmpty()) {
            adminAModifier.setGrade(administrateurModificationDTO.getGrade());
        }
        if (administrateurModificationDTO.getCin() != null && !administrateurModificationDTO.getCin().trim().isEmpty()) {
            if(!adminAModifier.getCin().equals(administrateurModificationDTO.getCin()) && utilisateurRepository.existsByCin(administrateurModificationDTO.getCin())){
                    throw new UtilisateursException("Un utilisateur avec cet Cin existe déjà");

            }
            adminAModifier.setCin(administrateurModificationDTO.getCin());

        }
        if(administrateurModificationDTO.getPpr() != null && !administrateurModificationDTO.getPpr().trim().isEmpty()) {
            if(!adminAModifier.getPpr().equals(administrateurModificationDTO.getPpr()) && administrateurRepository.existsByPpr(administrateurModificationDTO.getPpr())){
                throw new UtilisateursException("Un utilisateur avec cet Ppr existe déjà");

            }
            adminAModifier.setPpr(administrateurModificationDTO.getPpr());
        }

        if(administrateurModificationDTO.getTel() != null && !administrateurModificationDTO.getTel().trim().isEmpty()) {
            if(!adminAModifier.getTel().equals(administrateurModificationDTO.getTel()) && utilisateurRepository.existsByTel(administrateurModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet Numero existe déjà");
            }
            adminAModifier.setTel(administrateurModificationDTO.getTel());
        }

        if (administrateurModificationDTO.getActive() != null) {
            adminAModifier.setActive(administrateurModificationDTO.getActive());
        }

        if(administrateurModificationDTO.getPassword() != null && !administrateurModificationDTO.getPassword().trim().isEmpty()) {
            adminAModifier.setPassword(passwordEncoder.encode(administrateurModificationDTO.getPassword()));
        }

        
        Administrateur administrateurModifie = administrateurRepository.save(adminAModifier);
        return utilisateursMapper.FromAdministrateurVersAdministrateurSortieDTO(administrateurModifie);
    }


    @Override
    public AdministrateurSortieDTO modifierAdministrateurConnecter(String email, AdministrateurConnecterModificationDTO administrateurModificationDTO) throws UtilisateursException {
        if (email == null || email.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }

        if (administrateurModificationDTO == null) {
            throw new UtilisateursException("Les données de l'administrateur ne peuvent pas être nulles");
        }

        Administrateur adminAModifier = administrateurRepository.findByEmail(email).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + email));



        if (administrateurModificationDTO.getNom() != null && !administrateurModificationDTO.getNom().trim().isEmpty()) {
            adminAModifier.setNom(administrateurModificationDTO.getNom());
        }

        if (administrateurModificationDTO.getPrenom() != null && !administrateurModificationDTO.getPrenom().trim().isEmpty()) {
            adminAModifier.setPrenom(administrateurModificationDTO.getPrenom());
        }

        if (administrateurModificationDTO.getEmail() != null && !administrateurModificationDTO.getEmail().trim().isEmpty()) {
            if (!adminAModifier.getEmail().equals(administrateurModificationDTO.getEmail()) &&
                    utilisateurRepository.existsByEmail(administrateurModificationDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            adminAModifier.setEmail(administrateurModificationDTO.getEmail());
        }




        if (administrateurModificationDTO.getCin() != null && !administrateurModificationDTO.getCin().trim().isEmpty()) {
            if(!adminAModifier.getCin().equals(administrateurModificationDTO.getCin()) && utilisateurRepository.existsByCin(administrateurModificationDTO.getCin())){
                throw new UtilisateursException("Un utilisateur avec cet Cin existe déjà");

            }
            adminAModifier.setCin(administrateurModificationDTO.getCin());

        }


        if(administrateurModificationDTO.getTel() != null && !administrateurModificationDTO.getTel().trim().isEmpty()) {
            if(!adminAModifier.getTel().equals(administrateurModificationDTO.getTel()) && utilisateurRepository.existsByTel(administrateurModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet Numero existe déjà");
            }
            adminAModifier.setTel(administrateurModificationDTO.getTel());
        }



        if(administrateurModificationDTO.getPassword() != null && !administrateurModificationDTO.getPassword().trim().isEmpty()) {
            adminAModifier.setPassword(passwordEncoder.encode(administrateurModificationDTO.getPassword()));
        }


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

        administrateur.setActive(true);
        administrateurRepository.save(administrateur);
    }

    @Override
    public void desactiverAdministrateur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR de l'administrateur ne peut pas être nul");
        }
        
        Administrateur administrateur = administrateurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Administrateur introuvable avec le PPR: " + ppr));

        administrateur.setActive(false);
        administrateurRepository.save(administrateur);
    }


    @Transactional
    @Override
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



        if (professeurEntrerDTO.getCodeDepartementOuTravail() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurEntrerDTO.getCodeDepartementOuTravail())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurEntrerDTO.getCodeDepartementOuTravail()
                    ));
            professeur.setDepartementOuTravail(dep);
        }



        professeur.setActive(true);
        professeur.setDateCreation(LocalDateTime.now());
        professeur.setDateDerniereConnexion(null);
        professeur.setPassword(passwordEncoder.encode(professeurEntrerDTO.getPassword()));

        if (professeur.getUtilisateurRoles() == null) {
            professeur.setUtilisateurRoles(new ArrayList<>());
        }

        Role role =   roleRepository.findByNomRole(RolesCode.PROFESSEUR)
                .orElseThrow(() -> new SecuriteException("Rôle introuvable: " + RolesCode.PROFESSEUR.name()));

 
        Professeur professeurSauvegarde = professeurRepository.save(professeur);



        UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                .utilisateur(professeurSauvegarde)
                .role(role)
                .dateAttribution(LocalDateTime.now())
                .actif(true)
                .build();

        professeurSauvegarde.getUtilisateurRoles().add(utilisateurRole);

        utilisateurRoleRepository.save(utilisateurRole);



        return utilisateursMapper.FromProfesseurVersProfesseurSortirDTO(professeurSauvegarde);
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
    public ProfesseurSortieDTO modifierProfesseur(String ppr, ProfesseurModificationDTO professeurModificationDTO) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }
        
        if (professeurModificationDTO == null) {
            throw new UtilisateursException("Le professeur ne peut pas être nul");
        }
        
        Professeur professeurAModifier = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        if (professeurModificationDTO.getNom() != null && !professeurModificationDTO.getNom().trim().isEmpty()) {
            professeurAModifier.setNom(professeurModificationDTO.getNom());
        }
        
        if (professeurModificationDTO.getPrenom() != null && !professeurModificationDTO.getPrenom().trim().isEmpty()) {
            professeurAModifier.setPrenom(professeurModificationDTO.getPrenom());
        }
        
        if (professeurModificationDTO.getEmail() != null && !professeurModificationDTO.getEmail().trim().isEmpty()) {
            if (!professeurAModifier.getEmail().equals(professeurModificationDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(professeurModificationDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            professeurAModifier.setEmail(professeurModificationDTO.getEmail());
        }
        
        if (professeurModificationDTO.getSpecialite() != null && !professeurModificationDTO.getSpecialite().trim().isEmpty()) {
            professeurAModifier.setSpecialite(professeurModificationDTO.getSpecialite());
        }
        
        if (professeurModificationDTO.getGrade() != null && !professeurModificationDTO.getGrade().trim().isEmpty()) {
            professeurAModifier.setGrade(professeurModificationDTO.getGrade());
        }
        
        if (professeurModificationDTO.getDateRecrutement() != null) {
            professeurAModifier.setDateRecrutement(professeurModificationDTO.getDateRecrutement());
        }
        
        if (professeurModificationDTO.getStatut() != null && !professeurModificationDTO.getStatut().trim().isEmpty()) {
            professeurAModifier.setStatut(professeurModificationDTO.getStatut());
        }
        if(professeurModificationDTO.getCin() != null && !professeurModificationDTO.getCin().trim().isEmpty()) {
            if(!professeurAModifier.getCin().equals(professeurModificationDTO.getCin()) && utilisateurRepository.existsByCin(professeurModificationDTO.getCin())){
                throw new UtilisateursException("Un utilisateur avec cet Cin existe déjà");

            }
            professeurAModifier.setCin(professeurModificationDTO.getCin());
        }

        if(professeurModificationDTO.getTel() != null && !professeurModificationDTO.getTel().trim().isEmpty()) {
            if(!professeurAModifier.getTel().equals(professeurModificationDTO.getTel()) && utilisateurRepository.existsByTel(professeurModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet numero existe déjà");

            }
            professeurAModifier.setTel(professeurModificationDTO.getTel());
        }

        if(professeurModificationDTO.getPpr() != null && !professeurModificationDTO.getPpr().trim().isEmpty()) {
            if(!professeurAModifier.getPpr().equals(professeurModificationDTO.getPpr()) && professeurRepository.existsByPpr(professeurModificationDTO.getPpr())){
                throw new UtilisateursException("Un utilisateur avec cet Ppr existe déjà");

            }
            professeurAModifier.setPpr(professeurModificationDTO.getPpr());
        }

        if(professeurModificationDTO.getActive() != null) {
            professeurAModifier.setActive(professeurModificationDTO.getActive());
        }




        if (professeurModificationDTO.getCodeDepartementOuTravail() != null) {
            Departement dep = departementRepository.findByCodeDepartement(professeurModificationDTO.getCodeDepartementOuTravail())
                    .orElseThrow(() -> new UtilisateursException(
                            "Département de travail introuvable: " + professeurModificationDTO.getCodeDepartementOuTravail()
                    ));
            professeurAModifier.setDepartementOuTravail(dep);
        }



          if(professeurModificationDTO.getPassword() != null && !professeurModificationDTO.getPassword().trim().isEmpty()) {
            professeurAModifier.setPassword(passwordEncoder.encode(professeurModificationDTO.getPassword()));
        }

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

        professeur.setActive(true);
        professeurRepository.save(professeur);
    }

    @Override
    public void desactiverProfesseur(String ppr) throws UtilisateursException {
        if (ppr == null || ppr.trim().isEmpty()) {
            throw new UtilisateursException("Le PPR du professeur ne peut pas être nul");
        }
        
        Professeur professeur = professeurRepository.findByPpr(ppr).orElseThrow(() ->
                new UtilisateursException("Professeur introuvable avec le PPR: " + ppr));

        professeur.setActive(false);
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

        etudiant.setActive(true);
        etudiant.setDateCreation(LocalDateTime.now());
        etudiant.setAnneeFinEtude(null);
        etudiant.setPassword(passwordEncoder.encode(etudiantEntrerDTO.getPassword()));


        if (etudiant.getUtilisateurRoles() == null) {
            etudiant.setUtilisateurRoles(new ArrayList<>());
        }

        Role role =   roleRepository.findByNomRole(RolesCode.ETUDIANT)
                .orElseThrow(() -> new SecuriteException("Rôle introuvable: " + RolesCode.PROFESSEUR.name()));

        Etudiant etudiantSauvegarde = etudiantRepository.save(etudiant);

        UtilisateurRole utilisateurRole = UtilisateurRole.builder()
                .utilisateur(etudiantSauvegarde)
                .role(role)
                .dateAttribution(LocalDateTime.now())
                .actif(true)
                .build();

        etudiantSauvegarde.getUtilisateurRoles().add(utilisateurRole);

        utilisateurRoleRepository.save(utilisateurRole);



        return utilisateursMapper.FromEtudientVersEtudiantSortieDTO(etudiantSauvegarde);
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
    public EtudiantSortieDTO modifierEtudiant(String apogee, EtudiantModificationDTO etudiantModificationDTO) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }
        
        if (etudiantModificationDTO == null) {
            throw new UtilisateursException("L'étudiant ne peut pas être nul");
        }
        
        Etudiant etudiantAModifier = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        if (etudiantModificationDTO.getNom() != null && !etudiantModificationDTO.getNom().trim().isEmpty()) {
            etudiantAModifier.setNom(etudiantModificationDTO.getNom());
        }
        
        if (etudiantModificationDTO.getPrenom() != null && !etudiantModificationDTO.getPrenom().trim().isEmpty()) {
            etudiantAModifier.setPrenom(etudiantModificationDTO.getPrenom());
        }
        
        if (etudiantModificationDTO.getEmail() != null && !etudiantModificationDTO.getEmail().trim().isEmpty()) {
            if (!etudiantAModifier.getEmail().equals(etudiantModificationDTO.getEmail()) &&
                utilisateurRepository.existsByEmail(etudiantModificationDTO.getEmail())) {
                throw new UtilisateursException("Un utilisateur avec cet email existe déjà");
            }
            etudiantAModifier.setEmail(etudiantModificationDTO.getEmail());
        }
        
        if (etudiantModificationDTO.getNiveau() != null && !etudiantModificationDTO.getNiveau().trim().isEmpty()) {
            etudiantAModifier.setNiveau(etudiantModificationDTO.getNiveau());
        }
        if(etudiantModificationDTO.getActive() != null){
            etudiantAModifier.setActive(etudiantModificationDTO.getActive());
        }
        
        if (etudiantModificationDTO.getAnneeInscription() != null) {
            etudiantAModifier.setAnneeInscription(etudiantModificationDTO.getAnneeInscription());
        }
        
        if (etudiantModificationDTO.getSemestreActuel() != null) {
            etudiantAModifier.setSemestreActuel(etudiantModificationDTO.getSemestreActuel());
        }
        
        if (etudiantModificationDTO.getAnneeFinEtude() != null && !etudiantModificationDTO.getAnneeFinEtude().trim().isEmpty()) {
            etudiantAModifier.setAnneeFinEtude(etudiantModificationDTO.getAnneeFinEtude());
        }
        
        if (etudiantModificationDTO.getDateNaissance() != null) {
            etudiantAModifier.setDateNaissance(etudiantModificationDTO.getDateNaissance());
        }
        
        if (etudiantModificationDTO.getEtatInscription() != null) {
            etudiantAModifier.setEtatInscription(etudiantModificationDTO.getEtatInscription());
        }


        if(etudiantModificationDTO.getTel() != null && !etudiantModificationDTO.getTel().trim().isEmpty()) {
            if(!etudiantAModifier.getTel().equals(etudiantModificationDTO.getTel()) && utilisateurRepository.existsByTel(etudiantModificationDTO.getTel())){
                throw new UtilisateursException("Un utilisateur avec cet numero existe déjà");

            }
            etudiantAModifier.setTel(etudiantModificationDTO.getTel());
        }
        if(etudiantModificationDTO.getCin() != null && !etudiantModificationDTO.getCin().trim().isEmpty()) {
            if(!etudiantAModifier.getCin().equals(etudiantModificationDTO.getCin()) && utilisateurRepository.existsByCin(etudiantModificationDTO.getCin())){
                throw new UtilisateursException("Un utilisateur avec cet Cin existe déjà");

            }
            etudiantAModifier.setCin(etudiantModificationDTO.getCin());
        }

        if (etudiantModificationDTO.getCodeClasse() != null) {
            Classe classe = classeRepository.findByCodeClasse(etudiantModificationDTO.getCodeClasse())
                    .orElseThrow(() -> new UtilisateursException(
                            "Classe introuvable: " + etudiantModificationDTO.getCodeClasse()
                    ));
            etudiantAModifier.setClasse(classe);
        }




       if(etudiantModificationDTO.getPassword() != null && !etudiantModificationDTO.getPassword().trim().isEmpty()) {
            etudiantAModifier.setPassword(passwordEncoder.encode(etudiantModificationDTO.getPassword()));
        }

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

        etudiant.setActive(true);
        etudiantRepository.save(etudiant);
    }

    @Override
    public void desactiverEtudiant(String apogee) throws UtilisateursException {
        if (apogee == null || apogee.trim().isEmpty()) {
            throw new UtilisateursException("L'apogée de l'étudiant ne peut pas être nulle");
        }
        
        Etudiant etudiant = etudiantRepository.findByApogee(apogee).orElseThrow(() ->
                new UtilisateursException("Étudiant introuvable avec l'apogée: " + apogee));

        etudiant.setActive(false);
        etudiantRepository.save(etudiant);
    }


    @Transactional
    @Override
    public Utilisateur changerPhotoProfil(String email, MultipartFile photo) throws UtilisateursException {
        if (photo == null  ) {
            throw new UtilisateursException("photo est obligatoire");
        }
        if (email == null  ) {
            throw new UtilisateursException("email est obligatoire");
        }


        Utilisateur u = utilisateurRepository.findByEmail(email)
                .orElseThrow(() -> new UtilisateursException("Utilisateur introuvable"));

         photoStorageMetier.supprimerPhotoUtilisateurSiExsist(u.getPhotoNom());





         FichierStockerDTO stored = photoStorageMetier.enregistrerPhoto(photo);

        u.setPhotoNom(stored.fileName());
        u.setPhotoUrl(stored.url());


        return utilisateurRepository.save(u);

    }


}
