package com.srm.events.service.interfaces;

import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.LoginRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO loginRequest);
    AuthResponseDTO getCurrentUser(String token);
}
