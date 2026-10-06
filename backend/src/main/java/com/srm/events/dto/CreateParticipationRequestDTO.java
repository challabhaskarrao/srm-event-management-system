package com.srm.events.dto;

import com.srm.events.model.ParticipationType;
import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public record CreateParticipationRequestDTO(
 @NotBlank @Size(max=200) String eventName, @NotBlank @Size(max=80) String eventTypeCode, @NotNull Long categoryId, @NotBlank @Size(max=200) String organizer,
 @NotNull LocalDate startDate, @NotNull LocalDate endDate, @NotBlank @Pattern(regexp="ONLINE|OFFLINE|HYBRID") String mode,
 @Size(max=255) String venue, @Size(max=500) String eventUrl, @Size(max=2000) String description,
 @NotNull ParticipationType participationType, boolean travelRequired, boolean outsideState,
 @Size(max=150) String teamName, @Size(max=20) List<String> teamMemberRegistrationNumbers,
 @Size(max=150) String otherCategory,
 @Size(max=120) String city, @Size(max=120) String state, @Size(max=120) String country,
 @Size(max=255) String venueAddress, @Size(max=40) String modeOfTravel,
 LocalDateTime departureAt, LocalDateTime returnAt,
 boolean accommodationRequired, @Size(max=150) String accommodationName,
 @Size(max=255) String accommodationAddress, LocalDate checkInDate, LocalDate checkOutDate,
 @Size(max=150) String emergencyContactName, @Size(max=30) String emergencyContactNumber,
 @Size(max=30) String guardianContactNumber, @Size(max=1000) String travelRemarks,
 boolean registrationFeeRequired, @DecimalMin(value="0.00") @Digits(integer=8, fraction=2) BigDecimal registrationFeeAmount,
 @AssertTrue(message="Declaration must be accepted") boolean declarationAccepted,
 @Size(max=30) Map<String, String> typeSpecificDetails) {}
