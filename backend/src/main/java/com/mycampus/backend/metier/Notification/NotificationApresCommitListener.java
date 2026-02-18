package com.mycampus.backend.metier.Notification;

import com.mycampus.backend.dto.Notification.NotificationApresCommitEvenement;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class NotificationApresCommitListener {

    private final NotificationWebSocketMetier notificationWebSocketMetier;

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void diffuser(NotificationApresCommitEvenement evenement) {
        for (NotificationApresCommitEvenement.NotificationDiffusion diffusion : evenement.diffusions()) {
            notificationWebSocketMetier.notifierUtilisateur(diffusion.email(), diffusion.notification());
        }
    }
}
