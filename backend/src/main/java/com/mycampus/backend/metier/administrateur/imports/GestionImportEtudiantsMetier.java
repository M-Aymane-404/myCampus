package com.mycampus.backend.metier.administrateur.imports;

import com.mycampus.backend.dto.administrateur.imports.RapportImportEtudiantsSortieDTO;
import org.springframework.web.multipart.MultipartFile;

public interface GestionImportEtudiantsMetier {
    
    RapportImportEtudiantsSortieDTO importerEtudiants(
            MultipartFile fichier, 
            String codeClasse, 
            String modeImport, 
            String motDePasseDefaut
    );
}
