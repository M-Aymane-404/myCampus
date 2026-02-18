package com.mycampus.backend.metier.administrateur.imports;

import com.mycampus.backend.dto.administrateur.imports.RapportImportEtudiantsSortieDTO;
import com.mycampus.backend.enumerations.ModeImport;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

public interface GestionImportEtudiantsMetier {


     RapportImportEtudiantsSortieDTO importerEtudiants(
            MultipartFile fichier,
            String codeClasse,
            ModeImport modeImport,
            String motDePasseDefaut
    );
}
