package com.mycampus.backend.metier.annonces.ressources;

import com.mycampus.backend.dto.annonces.RessourceAnnonceSortieDTO;
import com.mycampus.backend.dto.annonces.TelechargementRessourceAnnonceSortieDTO;
import com.mycampus.backend.entites.Annonce;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface GestionRessourcesAnnonceMetier {

    RessourceAnnonceSortieDTO ajouterRessourceAdmin(Long annonceId, MultipartFile fichier);

    List<RessourceAnnonceSortieDTO> listerRessourcesAdmin(Long annonceId);

    TelechargementRessourceAnnonceSortieDTO preparerTelechargementAdmin(Long ressourceId);

    RessourceAnnonceSortieDTO ajouterRessourceProfesseur(Long annonceId, String emailProfesseur, MultipartFile fichier);

    List<RessourceAnnonceSortieDTO> listerRessourcesProfesseur(Long annonceId, String emailProfesseur);

    TelechargementRessourceAnnonceSortieDTO preparerTelechargementProfesseur(Long ressourceId, String emailProfesseur);

    List<RessourceAnnonceSortieDTO> listerRessourcesEtudiant(Long annonceId, Long idEtudiant);

    TelechargementRessourceAnnonceSortieDTO preparerTelechargementEtudiant(Long ressourceId, Long idEtudiant);

    void supprimerRessourcesSilencieusement(Annonce annonce);
}
