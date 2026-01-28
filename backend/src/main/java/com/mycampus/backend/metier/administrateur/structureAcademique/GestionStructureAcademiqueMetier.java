package com.mycampus.backend.metier.administrateur.structureAcademique;

import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Creation.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.Modification.*;
import com.mycampus.backend.dto.administrateur.DonneSortie.*;
import com.mycampus.backend.exceptions.StructureAcademiqueException;

import java.util.List;

public interface GestionStructureAcademiqueMetier {
    
    // Département management
    DepartementSortieDTO creerDepartement(DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException;
    
    DepartementSortieDTO trouverDepartementParCode(String codeDepartement) throws StructureAcademiqueException;
    
    List<DepartementSortieDTO> listerTousLesDepartements() throws StructureAcademiqueException;
    
    DepartementSortieDTO modifierDepartement(String codeDepartement, DepartementModificationDTO departementModificationDTO) throws StructureAcademiqueException;
    
    void supprimerDepartement(String codeDepartement) throws StructureAcademiqueException;

    void FermerDepartement(String codeDep) throws StructureAcademiqueException;

    void OuvrirDepartement(String codeDep) throws StructureAcademiqueException;

    // Filière management
    FiliereSortieDTO creerFiliere(FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException;
    
    FiliereSortieDTO trouverFiliereParCode(String codeFiliere) throws StructureAcademiqueException;
    
    List<FiliereSortieDTO> listerToutesLesFilieres() throws StructureAcademiqueException;
    
    List<FiliereSortieDTO> trouverFilieresParDepartement(String codeDepartement) throws StructureAcademiqueException;
    
    FiliereSortieDTO modifierFiliere(String codeFiliere, FiliereModificationDTO filiereModificationDTO) throws StructureAcademiqueException;
    
    void supprimerFiliere(String codeFiliere) throws StructureAcademiqueException;

    void FermerFiliere(String codeFiliere) throws StructureAcademiqueException;

    void OuvrirFiliere(String codeFiliere) throws StructureAcademiqueException;

    // Classe management
    ClasseSortieDTO creerClasse(ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException;
    
    ClasseSortieDTO trouverClasseParCode(String codeClasse) throws StructureAcademiqueException;
    
    List<ClasseSortieDTO> listerToutesLesClasses() throws StructureAcademiqueException;
    
    List<ClasseSortieDTO> trouverClassesParFiliere(String codeFiliere) throws StructureAcademiqueException;
    
    ClasseSortieDTO modifierClasse(String codeClasse, ClasseModificationDTO classeModificationDTO) throws StructureAcademiqueException;
    
    void supprimerClasse(String codeClasse) throws StructureAcademiqueException;
    
    // Module management
    ModuleSortieDTO creerModule(ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException;
    
    ModuleSortieDTO trouverModuleParCode(String codeModule) throws StructureAcademiqueException;
    
    List<ModuleSortieDTO> listerTousLesModules() throws StructureAcademiqueException;
    
    List<ModuleSortieDTO> trouverModulesParFiliere(String codeFiliere) throws StructureAcademiqueException;
    
    ModuleSortieDTO modifierModule(String codeModule, ModuleModificationDTO moduleModificationDTO) throws StructureAcademiqueException;
    
    void supprimerModule(String codeModule) throws StructureAcademiqueException;
    
    // Matière management
    MatiereSortieDTO creerMatiere(MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException;
    
    MatiereSortieDTO trouverMatiereParCode(String codeMatiere) throws StructureAcademiqueException;
    
    List<MatiereSortieDTO> listerToutesLesMatieres() throws StructureAcademiqueException;
    
    List<MatiereSortieDTO> trouverMatieresParModule(String codeModule) throws StructureAcademiqueException;
    List<MatiereSortieAvecProfDTO> trouverMatieresParProfAffecterEtModule(String codeModule,String codeClasse) throws StructureAcademiqueException;

    MatiereSortieDTO modifierMatiere(String codeMatiere, MatiereModificationDTO matiereModificationDTO) throws StructureAcademiqueException;
    
    void supprimerMatiere(String codeMatiere) throws StructureAcademiqueException;

    // AffectationEnseignement management
    AffectationEnseignementSortieDTO creerAffectationEnseignement(AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) throws StructureAcademiqueException;
    
    AffectationEnseignementSortieDTO modifierAffectationEnseignement(Long id, AffectationEnseignementModificationDTO affectationEnseignementModificationDTO) throws StructureAcademiqueException;
    
    void activerAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    void desactiverAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    AffectationEnseignementSortieDTO trouverAffectationParId(Long id) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerToutesLesAffectations() throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParProfesseur(String professeurPpr) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParClasse(String codeClasse) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParMatiere(String codeMatiere) throws StructureAcademiqueException;
    
    void supprimerAffectationEnseignement(Long id) throws StructureAcademiqueException;
}
