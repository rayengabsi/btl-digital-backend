package com.btl.digital.service;

import com.btl.digital.dto.request.ForgotPasswordRequest;
import com.btl.digital.dto.request.ResetPasswordRequest;

public interface PasswordResetService {

    void forgotPassword(ForgotPasswordRequest request);

    void resetPassword(ResetPasswordRequest request);
}
