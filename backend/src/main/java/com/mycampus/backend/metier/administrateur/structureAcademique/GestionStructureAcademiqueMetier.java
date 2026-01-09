package com.mycampus.backend.metier.administrateur.structureAcademique;

import com.mycampus.backend.dto.DepartementDTO;
import com.mycampus.backend.dto.FiliereDTO;
import com.mycampus.backend.dto.ClasseDTO;
import com.mycampus.backend.dto.ModuleDTO;
import com.mycampus.backend.dto.MatiereDTO;
import com.mycampus.backend.exceptions.StructureAcademiqueException;

import java.util.List;

public interface GestionStructureAcademiqueMetier {
    
    // Département management
    DepartementDTO creerDepartement(DepartementDTO departementDTO) throws StructureAcademiqueException;
    
    DepartementDTO trouverDepartementParId(Long id) throws StructureAcademiqueException;
    
    List<DepartementDTO> listerTousLesDepartements() throws StructureAcademiqueException;
    
    // Filière management
    FiliereDTO creerFiliere(FiliereDTO filiereDTO) throws StructureAcademiqueException;
    
    FiliereDTO trouverFiliereParId(Long id) throws StructureAcademiqueException;
    
    List<FiliereDTO> listerToutesLesFilieres() throws StructureAcademiqueException;
    
    List<FiliereDTO> trouverFilieresParDepartement(Long departementId) throws StructureAcademiqueException;
    
    // Classe management
    ClasseDTO creerClasse(ClasseDTO classeDTO) throws StructureAcademiqueException;
    
    ClasseDTO trouverClasseParId(Long id) throws StructureAcademiqueException;
    
    List<ClasseDTO> listerToutesLesClasses() throws StructureAcademiqueException;
    
    List<ClasseDTO> trouverClassesParFiliere(Long filiereId) throws StructureAcademiqueException;
    
    // Module management
    ModuleDTO creerModule(ModuleDTO moduleDTO) throws StructureAcademiqueException;
    
    ModuleDTO trouverModuleParId(Long id) throws StructureAcademiqueException;
    
    List<ModuleDTO> listerTousLesModules() throws StructureAcademiqueException;
    
    List<ModuleDTO> trouverModulesParClasse(Long classeId) throws StructureAcademiqueException;
    
    // Matière management
    MatiereDTO creerMatiere(MatiereDTO matiereDTO) throws StructureAcademiqueException;
    
    MatiereDTO trouverMatiereParId(Long id) throws StructureAcademiqueException;
    
    List<MatiereDTO> listerToutesLesMatieres() throws StructureAcademiqueException;
    
    List<MatiereDTO> trouverMatieresParModule(Long moduleId) throws StructureAcademiqueException;
}
