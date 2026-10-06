package com.srm.events.service.impl;

import com.srm.events.model.User;
import com.srm.events.service.interfaces.PasswordRecoveryNotifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class UnavailablePasswordRecoveryNotifier implements PasswordRecoveryNotifier {
    private static final Logger LOGGER = LoggerFactory.getLogger(UnavailablePasswordRecoveryNotifier.class);

    @Override
    public void sendPasswordReset(User user, String rawToken, Duration lifetime) {
        // Deliberately never log the email address or raw token. A production
        // provider can replace this implementation when institutional mail is available.
        LOGGER.info("Password recovery delivery is not configured; request retained securely for userId={}", user.getUserId());
    }

    @Override
    public boolean isDeliveryConfigured() { return false; }
}
