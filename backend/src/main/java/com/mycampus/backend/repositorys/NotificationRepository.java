package com.mycampus.backend.repositorys;

import com.mycampus.backend.entites.Notification;
import com.mycampus.backend.entites.Utilisateur;
import com.mycampus.backend.enumerations.ReferenceType;
import com.mycampus.backend.enumerations.TypeNotification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    interface NotificationInboxProjection {
        Long getId();
        String getTitre();
        String getMessage();
        LocalDateTime getDateEnvoi();
        boolean isLu();
        LocalDateTime getDateLecture();
        Long getReferenceId();
        ReferenceType getReferenceType();
        TypeNotification getType();
        String getNomAuteur();
        String getPrenomAuteur();
        String getNomUtilisateur();
        String getPrenomUtilisateur();
        String getEmailUtilisateur();
    }
    
    boolean existsByUtilisateurAndTypeAndDateEnvoi(Utilisateur utilisateur, TypeNotification type, LocalDateTime dateEnvoi);

    @Query("""
        select
            n.id as id,
            n.titre as titre,
            n.message as message,
            n.dateEnvoi as dateEnvoi,
            n.lu as lu,
            n.dateLecture as dateLecture,
            n.referenceId as referenceId,
            n.referenceType as referenceType,
            n.type as type,
            a.nom as nomAuteur,
            a.prenom as prenomAuteur,
            u.nom as nomUtilisateur,
            u.prenom as prenomUtilisateur,
            u.email as emailUtilisateur
        from Notification n
        join n.auteur a
        join n.utilisateur u
        where u.email = :email
    """)
    Page<NotificationInboxProjection> trouverInboxParEmail(@Param("email") String email, Pageable pageable);


    @Query("""
        select
            n.id as id,
            n.titre as titre,
            n.message as message,
            n.dateEnvoi as dateEnvoi,
            n.lu as lu,
            n.dateLecture as dateLecture,
            n.referenceId as referenceId,
            n.referenceType as referenceType,
            n.type as type,
            a.nom as nomAuteur,
            a.prenom as prenomAuteur,
            u.nom as nomUtilisateur,
            u.prenom as prenomUtilisateur,
            u.email as emailUtilisateur
        from Notification n
        join n.auteur a
        join n.utilisateur u
        where u.id = :id
    """)
    Page<NotificationInboxProjection> trouverInboxParId(@Param("id") Long id, Pageable pageable);


    long countByUtilisateur_EmailAndLuFalse(String email);

    long countByUtilisateur_IdAndLuFalse(Long id);

    Optional<Notification> findByIdAndUtilisateur_Email(Long id, String email);

    Optional<Notification> findByIdAndUtilisateur_Id(Long id, Long utilisateurId);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.lu = true, n.dateLecture = :dateLecture
        where n.id = :id and n.utilisateur.email = :email and n.lu = false
    """)
    int marquerCommeLue(@Param("id") Long id, @Param("email") String email, @Param("dateLecture") LocalDateTime dateLecture);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.lu = true, n.dateLecture = :dateLecture
        where n.id = :id and n.utilisateur.id = :idUser and n.lu = false
    """)
    int marquerCommeLueParIdUtilisateur(@Param("id") Long id, @Param("idUser") Long idUser, @Param("dateLecture") LocalDateTime dateLecture);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.lu = true, n.dateLecture = :dateLecture
        where n.utilisateur.email = :email and n.lu = false
    """)
    int marquerToutesCommeLues(@Param("email") String email, @Param("dateLecture") LocalDateTime dateLecture);


    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("""
        update Notification n
        set n.lu = true, n.dateLecture = :dateLecture
        where n.utilisateur.id = :id and n.lu = false
    """)
    int marquerToutesCommeLuesParIdUtilisateur(@Param("id") Long id, @Param("dateLecture") LocalDateTime dateLecture);
}
