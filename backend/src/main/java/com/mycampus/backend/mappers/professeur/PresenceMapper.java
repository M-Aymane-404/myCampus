package com.mycampus.backend.mappers.professeur;

import com.mycampus.backend.dto.professeur.DonneeSortie.presences.LignePresenceSortieDTO;
import com.mycampus.backend.entites.Presence;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;



@Component
@RequiredArgsConstructor

public class PresenceMapper {



    public LignePresenceSortieDTO FromPresencetoPresencceSortieDTO(Presence presence ) {
    if (presence == null) {
        return null;

    }

      LignePresenceSortieDTO presenceSortieDTO = new LignePresenceSortieDTO();
            BeanUtils.copyProperties(presence,presenceSortieDTO);

         return presenceSortieDTO;




}}
