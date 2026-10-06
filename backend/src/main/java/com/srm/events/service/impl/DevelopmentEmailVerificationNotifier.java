package com.srm.events.service.impl;

import com.srm.events.service.interfaces.EmailVerificationNotifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Duration;

/** Never enabled outside the local dev/test profile; does not log OTPs. */
@Component
@Profile("dev | test")
public class DevelopmentEmailVerificationNotifier implements EmailVerificationNotifier {
    private final DevelopmentOtpStore store;
    public DevelopmentEmailVerificationNotifier(DevelopmentOtpStore store) { this.store = store; }
    @Override public boolean isDeliveryConfigured() { return true; }
    @Override public void sendSignupOtp(String email, String otp, String purpose, Duration lifetime) {
        store.put(purpose, email, otp);
    }
}
