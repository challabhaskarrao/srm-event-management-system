package com.srm.events.service.impl;

import com.srm.events.service.interfaces.EmailVerificationNotifier;
import org.springframework.stereotype.Component;
import org.springframework.context.annotation.Profile;
import java.time.Duration;

/** Production-safe default: no email provider is configured and no OTP is logged or returned. */
@Component
@Profile("!dev & !test")
public class UnavailableEmailVerificationNotifier implements EmailVerificationNotifier {
    @Override public boolean isDeliveryConfigured() { return false; }
    @Override public void sendSignupOtp(String email, String otp, String purpose, Duration lifetime) { }
}
