package com.srm.events.service.interfaces;

import com.srm.events.dto.AuthResponseDTO;
import com.srm.events.dto.ChangePasswordRequestDTO;
import com.srm.events.dto.ForgotPasswordRequestDTO;
import com.srm.events.dto.LoginRequestDTO;
import com.srm.events.dto.MessageResponseDTO;
import com.srm.events.dto.ResetPasswordRequestDTO;

public interface AuthService {
    AuthResponseDTO login(LoginRequestDTO loginRequest);
    AuthResponseDTO login(LoginRequestDTO loginRequest, String clientAddress);
    AuthResponseDTO getCurrentUser(String token);
    MessageResponseDTO changePassword(String token, ChangePasswordRequestDTO request);
    MessageResponseDTO requestPasswordReset(ForgotPasswordRequestDTO request);
    MessageResponseDTO resetPassword(ResetPasswordRequestDTO request);
}
