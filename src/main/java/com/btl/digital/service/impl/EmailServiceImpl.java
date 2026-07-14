package com.btl.digital.service.impl;

import com.btl.digital.entity.User;
import com.btl.digital.service.EmailService;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.email.enabled:false}")
    private boolean emailEnabled;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendPasswordResetEmail(User user, String token, int expirationMinutes) {
        String resetLink = "http://localhost:3000/reset-password?token=" + token;

        if (!emailEnabled) {
            log.warn("\n==================================================\nPASSWORD RESET LINK\n{}\n==================================================", resetLink);
            return;
        }

        String subject = "Password Reset Request - Digital Banking";
        String htmlContent = buildEmailHtml(user.getFirstName(), resetLink, expirationMinutes);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            helper.setTo(user.getEmail());
            helper.setSubject(subject);
            helper.setText(htmlContent, true);
            mailSender.send(message);
            log.info("Password reset email sent to {}", user.getEmail());
        } catch (MessagingException | RuntimeException e) {
            log.error("Failed to send password reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }

    private String buildEmailHtml(String firstName, String resetLink, int expirationMinutes) {
        return """
                <!DOCTYPE html>
                <html lang="en">
                <head>
                    <meta charset="UTF-8">
                    <meta name="viewport" content="width=device-width, initial-scale=1.0">
                </head>
                <body style="margin: 0; padding: 0; background-color: #f0f2f5; font-family: 'Segoe UI', Arial, sans-serif;">
                    <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #f0f2f5; padding: 40px 0;">
                        <tr>
                            <td align="center">
                                <table width="600" cellpadding="0" cellspacing="0" style="background-color: #ffffff; border-radius: 12px; overflow: hidden; box-shadow: 0 4px 12px rgba(0,0,0,0.08);">
                                    <!-- Header -->
                                    <tr>
                                        <td style="background: linear-gradient(135deg, #1a237e, #283593); padding: 30px 40px; text-align: center;">
                                            <h1 style="color: #ffffff; margin: 0; font-size: 24px; font-weight: 600; letter-spacing: 0.5px;">Digital Banking</h1>
                                            <p style="color: #b3bef5; margin: 6px 0 0; font-size: 13px; letter-spacing: 1px; text-transform: uppercase;">Secure Banking Portal</p>
                                        </td>
                                    </tr>
                                    <!-- Body -->
                                    <tr>
                                        <td style="padding: 40px;">
                                            <h2 style="color: #1a237e; font-size: 20px; margin: 0 0 20px;">Password Reset Request</h2>
                                            <p style="color: #444444; font-size: 15px; line-height: 1.6; margin: 0 0 16px;">Hello <strong>%s</strong>,</p>
                                            <p style="color: #444444; font-size: 15px; line-height: 1.6; margin: 0 0 24px;">We received a request to reset the password for your Digital Banking account. Click the button below to set a new password:</p>
                                            <!-- Button -->
                                            <table width="100%%" cellpadding="0" cellspacing="0">
                                                <tr>
                                                    <td align="center" style="padding: 8px 0 28px;">
                                                        <a href="%s" style="display: inline-block; background-color: #1a237e; color: #ffffff; padding: 14px 40px; text-decoration: none; border-radius: 6px; font-size: 15px; font-weight: 600; letter-spacing: 0.3px;">Reset My Password</a>
                                                    </td>
                                                </tr>
                                            </table>
                                            <!-- Expiry notice -->
                                            <table width="100%%" cellpadding="0" cellspacing="0" style="background-color: #fff3e0; border-radius: 6px; border-left: 4px solid #ff9800;">
                                                <tr>
                                                    <td style="padding: 14px 18px;">
                                                        <p style="color: #e65100; font-size: 14px; margin: 0; line-height: 1.5;">This link will expire in <strong>%d minutes</strong>. If you did not request a password reset, please ignore this email. Your password will remain unchanged.</p>
                                                    </td>
                                                </tr>
                                            </table>
                                        </td>
                                    </tr>
                                    <!-- Footer -->
                                    <tr>
                                        <td style="background-color: #f8f9fa; padding: 20px 40px; border-top: 1px solid #e8eaed;">
                                            <p style="color: #999999; font-size: 12px; margin: 0; text-align: center;">This is an automated message. Please do not reply to this email.</p>
                                            <p style="color: #aaaaaa; font-size: 12px; margin: 8px 0 0; text-align: center;">Digital Banking &copy; 2026. All rights reserved.</p>
                                        </td>
                                    </tr>
                                </table>
                            </td>
                        </tr>
                    </table>
                </body>
                </html>
                """.formatted(firstName, resetLink, expirationMinutes);
    }
}
