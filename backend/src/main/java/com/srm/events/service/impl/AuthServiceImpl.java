package com.srm.events.service.impl;

import com.srm.events.dao.interfaces.UserDAO;
import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.model.User;
import com.srm.events.security.JwtTokenProvider;
import com.srm.events.service.interfaces.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserDAO userDAO;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;

    @Autowired
    public AuthServiceImpl(UserDAO userDAO, PasswordEncoder passwordEncoder, JwtTokenProvider jwtTokenProvider) {
        this.userDAO = userDAO;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
    }

    @Override
    public AuthResponseDTO login(LoginRequestDTO loginRequest) {
        if (loginRequest == null || loginRequest.getEmail() == null || loginRequest.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        User user = userDAO.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials"));

        if (!user.isActive() || !passwordEncoder.matches(loginRequest.getPassword(), user.getPasswordHash())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid credentials");
        }

        userDAO.updateLastLogin(user.getUserId());

        String token = jwtTokenProvider.generateToken(user.getUserId(), user.getEmail(), user.getRoleName());

        return new AuthResponseDTO(token, user.getUserId(), user.getEmail(), user.getRoleName());
    }

    @Override
    public AuthResponseDTO getCurrentUser(String token) {
        if (token == null || token.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Missing or invalid authorization token");
        }

        String rawToken = token.trim();
        if (rawToken.startsWith("Bearer ")) {
            rawToken = rawToken.substring(7).trim();
        }

        if (!jwtTokenProvider.validateToken(rawToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid or expired token");
        }

        Long userId = jwtTokenProvider.getUserIdFromToken(rawToken);
        User user = userDAO.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        if (!user.isActive()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User account is inactive");
        }

        return new AuthResponseDTO(rawToken, user.getUserId(), user.getEmail(), user.getRoleName());
    }
}
