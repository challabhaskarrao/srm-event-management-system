package com.srm.events.service.interfaces;

import com.srm.events.model.User;
import java.time.Duration;

public interface PasswordRecoveryNotifier {
    void sendPasswordReset(User user, String rawToken, Duration lifetime);
    boolean isDeliveryConfigured();
}
