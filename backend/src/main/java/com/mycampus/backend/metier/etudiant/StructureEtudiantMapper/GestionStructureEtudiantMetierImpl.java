package com.mycampus.backend.metier.etudiant.StructureEtudiantMapper;

import com.mycampus.backend.dto.etudiant.DonneeSortie.EtudiantSortieDTO;
import com.mycampus.backend.exceptions.EtudiantException;
import com.mycampus.backend.mappers.etudiant.StructureEtudiantMapper;
import com.mycampus.backend.repositorys.EtudiantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class GestionStructureEtudiantMetierImpl implements GestionStructureEtudiantMetier {
    private final StructureEtudiantMapper structureEtudiantMapper;
    private final EtudiantRepository etudiantRepository;

    @Override
    public EtudiantSortieDTO trouverEtudiantConnecter(Long id) throws EtudiantException {


        if(id == null){
            throw  new EtudiantException("id est obli");
        }



        return null;
    }
}
