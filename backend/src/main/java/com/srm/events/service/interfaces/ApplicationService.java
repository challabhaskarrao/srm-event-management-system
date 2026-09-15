package com.srm.events.service.interfaces;

import com.srm.events.dto.ApplicationResponseDTO;
import com.srm.events.dto.RegisterApplicationRequestDTO;
import java.util.List;

public interface ApplicationService {
    ApplicationResponseDTO registerApplication(RegisterApplicationRequestDTO request, Long userId);
    List<ApplicationResponseDTO> getMyApplications(Long userId);
    ApplicationResponseDTO getApplicationByCode(String code, Long userId);
}
