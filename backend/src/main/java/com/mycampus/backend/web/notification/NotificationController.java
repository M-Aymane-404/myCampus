package com.mycampus.backend.web.notification;

import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.metier.Notification.NotificationMetier;
import com.mycampus.backend.metier.security.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationMetier notificationMetier;

    @GetMapping
    public ResponseEntity<Page<NotificationSortieDTO>> listerNotifications(
              @AuthenticationPrincipal CustomUserDetails user,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dateEnvoi"));
         return ResponseEntity.ok(notificationMetier.listerNotificationsUtilisateur(user.getId(), pageable));
    }

    @GetMapping("/non-lues/compteur")
    public ResponseEntity<Map<String, Long>> compterNotificationsNonLues(  @AuthenticationPrincipal CustomUserDetails user) {
         long compteur = notificationMetier.compterNotificationsNonLuesUtilisateur(user.getId());
        return ResponseEntity.ok(Map.of("compteur", compteur));
    }

    @PatchMapping("/{id}/lire")
    public ResponseEntity<NotificationSortieDTO> marquerCommeLue(
              @AuthenticationPrincipal CustomUserDetails user,
            @PathVariable("id") Long id
    ) {
         return ResponseEntity.ok(notificationMetier.marquerNotificationCommeLue(user.getId(), id));
    }

    @PatchMapping("/lire-tout")
    public ResponseEntity<Map<String, Integer>> marquerToutesCommeLues(  @AuthenticationPrincipal CustomUserDetails user) {
         int modifiees = notificationMetier.marquerToutesNotificationsCommeLues(user.getId());
        return ResponseEntity.ok(Map.of("notificationsModifiees", modifiees));
    }
}
