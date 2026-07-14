package com.btl.digital.service;

import com.btl.digital.entity.User;

public interface EmailService {

    void sendPasswordResetEmail(User user, String token, int expirationMinutes);
}
