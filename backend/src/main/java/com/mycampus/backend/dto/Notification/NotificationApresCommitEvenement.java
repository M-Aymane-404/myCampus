package com.mycampus.backend.dto.Notification;

import java.util.List;

public record NotificationApresCommitEvenement(List<NotificationDiffusion> diffusions) {
    public record NotificationDiffusion(String email, NotificationSortieDTO notification) {
    }
}
