package com.srm.events.service.interfaces;

import java.time.Duration;

public interface EmailVerificationNotifier {
    boolean isDeliveryConfigured();
    void sendSignupOtp(String email, String otp, String purpose, Duration lifetime);
}
