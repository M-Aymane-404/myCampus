package com.mycampus.backend.repositorys;

 import com.mycampus.backend.entites.RefreshToken;
import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.data.jpa.repository.Modifying;
 import org.springframework.data.jpa.repository.Query;
 import org.springframework.data.repository.query.Param;
 import org.springframework.stereotype.Repository;

 import java.time.LocalDateTime;
 import java.util.List;
 import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);
    List<RefreshToken> findAllByUtilisateurIdAndRevoqueFalse(Long utilisateurId);


    List<RefreshToken> findByUtilisateurIdAndRevoqueFalseOrderByDateCreationAsc(Long utilisateurId);

    @Modifying
    @Query("""
update RefreshToken rt
set rt.revoque = true,
    rt.remplaceParHash = :newHash
where rt.tokenHash = :oldHash
  and rt.revoque = false
  and rt.dateExpiration > :now
  and rt.remplaceParHash is null
""")
    int revokeIfValid(@Param("oldHash") String oldHash,
                      @Param("newHash") String newHash,
                      @Param("now") LocalDateTime now);



    @Modifying
    @Query("""
update RefreshToken rt
set rt.revoque = true
where rt.utilisateur.id = :utilisateurId
  and rt.tokenFamily = :tokenFamily
  and rt.revoque = false
""")
    int revokeFamily(@Param("utilisateurId") Long utilisateurId,@Param("tokenFamily") String tokenFamily);


    @Modifying
    @Query("""
    update RefreshToken rt
    set rt.revoque = true
    where rt.utilisateur.id =:utilisateurId
    and rt.revoque = false
    """)
    int revokeAllByUser(@Param("utilisateurId") Long utilisateurId);
}
