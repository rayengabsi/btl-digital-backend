package com.btl.digital.service;

import com.btl.digital.dto.request.CreateNotificationRequest;
import com.btl.digital.dto.response.NotificationResponse;

import java.util.List;

public interface NotificationService {

    List<NotificationResponse> getMyNotifications();

    NotificationResponse markAsRead(Long id);

    void markAllAsRead();

    NotificationResponse createNotification(CreateNotificationRequest request);
}
