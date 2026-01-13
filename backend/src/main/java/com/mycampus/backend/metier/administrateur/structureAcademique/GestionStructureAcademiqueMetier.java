package com.mycampus.backend.metier.administrateur.structureAcademique;

import com.mycampus.backend.dto.administrateur.DonneEntrer.DepartementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.FiliereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ClasseEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.ModuleEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.MatiereEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneEntrer.AffectationEnseignementEntrerDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.DepartementSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.FiliereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ClasseSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.ModuleSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.MatiereSortieDTO;
import com.mycampus.backend.dto.administrateur.DonneSortie.AffectationEnseignementSortieDTO;
import com.mycampus.backend.exceptions.StructureAcademiqueException;

import java.util.List;

public interface GestionStructureAcademiqueMetier {
    
    // Département management
    DepartementSortieDTO creerDepartement(DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException;
    
    DepartementSortieDTO trouverDepartementParId(Long id) throws StructureAcademiqueException;
    
    List<DepartementSortieDTO> listerTousLesDepartements() throws StructureAcademiqueException;
    
    DepartementSortieDTO modifierDepartement(Long departementId, DepartementEntrerDTO departementEntrerDTO) throws StructureAcademiqueException;
    
    void supprimerDepartement(Long departementId) throws StructureAcademiqueException;
    
    // Filière management
    FiliereSortieDTO creerFiliere(FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException;
    
    FiliereSortieDTO trouverFiliereParId(Long id) throws StructureAcademiqueException;
    
    List<FiliereSortieDTO> listerToutesLesFilieres() throws StructureAcademiqueException;
    
    List<FiliereSortieDTO> trouverFilieresParDepartement(Long departementId) throws StructureAcademiqueException;
    
    FiliereSortieDTO modifierFiliere(Long filiereId, FiliereEntrerDTO filiereEntrerDTO) throws StructureAcademiqueException;
    
    void supprimerFiliere(Long filiereId) throws StructureAcademiqueException;
    
    // Classe management
    ClasseSortieDTO creerClasse(ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException;
    
    ClasseSortieDTO trouverClasseParId(Long id) throws StructureAcademiqueException;
    
    List<ClasseSortieDTO> listerToutesLesClasses() throws StructureAcademiqueException;
    
    List<ClasseSortieDTO> trouverClassesParFiliere(Long filiereId) throws StructureAcademiqueException;
    
    ClasseSortieDTO modifierClasse(Long classeId, ClasseEntrerDTO classeEntrerDTO) throws StructureAcademiqueException;
    
    void supprimerClasse(Long classeId) throws StructureAcademiqueException;
    
    // Module management
    ModuleSortieDTO creerModule(ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException;
    
    ModuleSortieDTO trouverModuleParId(Long id) throws StructureAcademiqueException;
    
    List<ModuleSortieDTO> listerTousLesModules() throws StructureAcademiqueException;
    
    List<ModuleSortieDTO> trouverModulesParFiliere(Long filiereId) throws StructureAcademiqueException;
    
    ModuleSortieDTO modifierModule(Long moduleId, ModuleEntrerDTO moduleEntrerDTO) throws StructureAcademiqueException;
    
    void supprimerModule(Long moduleId) throws StructureAcademiqueException;
    
    // Matière management
    MatiereSortieDTO creerMatiere(MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException;
    
    MatiereSortieDTO trouverMatiereParId(Long id) throws StructureAcademiqueException;
    
    List<MatiereSortieDTO> listerToutesLesMatieres() throws StructureAcademiqueException;
    
    List<MatiereSortieDTO> trouverMatieresParModule(Long moduleId) throws StructureAcademiqueException;
    
    MatiereSortieDTO modifierMatiere(Long matiereId, MatiereEntrerDTO matiereEntrerDTO) throws StructureAcademiqueException;
    
    void supprimerMatiere(Long matiereId) throws StructureAcademiqueException;

    AffectationEnseignementSortieDTO creerAffectationEnseignement(AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) throws StructureAcademiqueException;
    
    AffectationEnseignementSortieDTO modifierAffectationEnseignement(Long id, AffectationEnseignementEntrerDTO affectationEnseignementEntrerDTO) throws StructureAcademiqueException;
    
    void activerAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    void desactiverAffectationEnseignement(Long id) throws StructureAcademiqueException;
    
    AffectationEnseignementSortieDTO trouverAffectationParId(Long id) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerToutesLesAffectations() throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParProfesseur(String professeurPpr) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParClasse(Long classeId) throws StructureAcademiqueException;
    
    List<AffectationEnseignementSortieDTO> listerAffectationsParMatiere(Long matiereId) throws StructureAcademiqueException;
    
    void supprimerAffectationEnseignement(Long id) throws StructureAcademiqueException;
}
