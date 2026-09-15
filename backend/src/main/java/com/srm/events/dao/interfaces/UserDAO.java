package com.srm.events.dao.interfaces;

import com.srm.events.model.User;
import java.util.Optional;

public interface UserDAO {
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long userId);
    User save(User user);
    void updateLastLogin(Long userId);
}
