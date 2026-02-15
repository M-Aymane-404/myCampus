package com.mycampus.backend.mappers.Notification;


import com.mycampus.backend.dto.Notification.NotificationEntrerDTO;
import com.mycampus.backend.dto.Notification.NotificationSortieDTO;
import com.mycampus.backend.entites.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationMappers {



    public NotificationSortieDTO notificationToNotificationSortieDTO(Notification notification) {
        NotificationSortieDTO notificationSortieDTO = new NotificationSortieDTO();
        BeanUtils.copyProperties(notification, notificationSortieDTO);
        return notificationSortieDTO;
    }

 }
