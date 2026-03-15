package com.mycampus.backend.web.administrateur.tableauDeBord;

import com.mycampus.backend.dto.administrateur.DonneSortie.TableauBordDTO.TableauBordAdministratifDTO;
import com.mycampus.backend.metier.administrateur.tableauDeBord.StatistiqueMetier;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/tableau-de-bord")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('ADMIN_STRUCTURE_GERER')")
public class TableauDeBordAdministratifController {

    private final StatistiqueMetier statistiqueMetier;

    @GetMapping
    public ResponseEntity<TableauBordAdministratifDTO> obtenirTableauDeBord() {
        return ResponseEntity.ok(statistiqueMetier.obtenirTableauDeBordAdministratif());
    }
}
