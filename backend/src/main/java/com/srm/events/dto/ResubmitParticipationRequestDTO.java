package com.srm.events.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.AssertTrue;

import java.time.LocalDate;

public record ResubmitParticipationRequestDTO(
        @NotBlank @Size(max = 200) String eventName,
        @NotBlank @Size(max = 200) String organizer,
        @NotNull LocalDate startDate,
        @NotNull LocalDate endDate,
        @NotBlank @Pattern(regexp = "ONLINE|OFFLINE|HYBRID") String mode,
        @Size(max = 255) String venue,
        @Size(max = 500) String eventUrl,
        @Size(max = 2000) String description,
        @Size(max = 120) String city,
        @Size(max = 120) String state,
        @Size(max = 120) String country,
        @AssertTrue(message = "Declaration must be accepted") boolean declarationAccepted
) {
}
