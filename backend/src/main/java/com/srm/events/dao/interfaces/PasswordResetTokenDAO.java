package com.srm.events.dao.interfaces;

import com.srm.events.model.PasswordResetToken;
import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenDAO {
    void replaceActiveToken(Long userId, String tokenHash, LocalDateTime expiresAt);
    Optional<PasswordResetToken> findByHash(String tokenHash);
    Optional<Long> consumeAndResetPassword(String tokenHash, String passwordHash, LocalDateTime now);
}
