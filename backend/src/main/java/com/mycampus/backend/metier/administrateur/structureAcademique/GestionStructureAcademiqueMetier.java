package com.mycampus.backend.metier.administrateur.structureAcademique;

import com.mycampus.backend.dto.*;
import com.mycampus.backend.exceptions.StructureAcademiqueException;

import java.util.List;

public interface GestionStructureAcademiqueMetier {
    
    // Département management
    DepartementDTO creerDepartement(DepartementDTO departementDTO) throws StructureAcademiqueException;
    
    DepartementDTO trouverDepartementParId(Long id) throws StructureAcademiqueException;
    
    List<DepartementDTO> listerTousLesDepartements() throws StructureAcademiqueException;
    
    DepartementDTO modifierDepartement(Long departementId, DepartementDTO departementDTO) throws StructureAcademiqueException;
    
    void supprimerDepartement(Long departementId) throws StructureAcademiqueException;
    
    // Filière management
    FiliereDTO creerFiliere(FiliereDTO filiereDTO) throws StructureAcademiqueException;
    
    FiliereDTO trouverFiliereParId(Long id) throws StructureAcademiqueException;
    
    List<FiliereDTO> listerToutesLesFilieres() throws StructureAcademiqueException;
    
    List<FiliereDTO> trouverFilieresParDepartement(Long departementId) throws StructureAcademiqueException;
    
    FiliereDTO modifierFiliere(Long filiereId, FiliereDTO filiereDTO) throws StructureAcademiqueException;
    
    void supprimerFiliere(Long filiereId) throws StructureAcademiqueException;
    
    // Classe management
    ClasseDTO creerClasse(ClasseDTO classeDTO) throws StructureAcademiqueException;
    
    ClasseDTO trouverClasseParId(Long id) throws StructureAcademiqueException;
    
    List<ClasseDTO> listerToutesLesClasses() throws StructureAcademiqueException;
    
    List<ClasseDTO> trouverClassesParFiliere(Long filiereId) throws StructureAcademiqueException;
    
    ClasseDTO modifierClasse(Long classeId, ClasseDTO classeDTO) throws StructureAcademiqueException;
    
    void supprimerClasse(Long classeId) throws StructureAcademiqueException;
    
    // Module management
    ModuleDTO creerModule(ModuleDTO moduleDTO) throws StructureAcademiqueException;
    
    ModuleDTO trouverModuleParId(Long id) throws StructureAcademiqueException;
    
    List<ModuleDTO> listerTousLesModules() throws StructureAcademiqueException;
    
    List<ModuleDTO> trouverModulesParFiliere(Long filiereId) throws StructureAcademiqueException;
    
    ModuleDTO modifierModule(Long moduleId, ModuleDTO moduleDTO) throws StructureAcademiqueException;
    
    void supprimerModule(Long moduleId) throws StructureAcademiqueException;
    
    // Matière management
    MatiereDTO creerMatiere(MatiereDTO matiereDTO) throws StructureAcademiqueException;
    
    MatiereDTO trouverMatiereParId(Long id) throws StructureAcademiqueException;
    
    List<MatiereDTO> listerToutesLesMatieres() throws StructureAcademiqueException;
    
    List<MatiereDTO> trouverMatieresParModule(Long moduleId) throws StructureAcademiqueException;
    
    MatiereDTO modifierMatiere(Long matiereId, MatiereDTO matiereDTO) throws StructureAcademiqueException;
    
    void supprimerMatiere(Long matiereId) throws StructureAcademiqueException;

    AffectationEnseignementDTO creerAffectationEnseignement(AffectationEnseignementDTO affectationEnseignementDTO) throws StructureAcademiqueException;
    
    AffectationEnseignementDTO modifierAffectationEnseignement(Long id, AffectationEnseignementDTO affectationEnseignementDTO) throws StructureAcademiqueException;
    
    void activerAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    void desactiverAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    AffectationEnseignementDTO trouverAffectationParId(Long id) throws StructureAcademiqueException;
    
    List<AffectationEnseignementDTO> listerToutesLesAffectations() throws StructureAcademiqueException;
    
    List<AffectationEnseignementDTO> listerAffectationsParProfesseur(String professeurPpr) throws StructureAcademiqueException;
    
    List<AffectationEnseignementDTO> listerAffectationsParClasse(Long classeId) throws StructureAcademiqueException;
    
    List<AffectationEnseignementDTO> listerAffectationsParMatiere(Long matiereId) throws StructureAcademiqueException;
    
    void supprimerAffectationEnseignement(Long id) throws StructureAcademiqueException;
}
