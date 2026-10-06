package com.srm.events.controller;

import com.srm.events.dto.StudentSignupRequestDTO;
import com.srm.events.dto.VerifyAccountOtpRequestDTO;
import com.srm.events.dto.ResendAccountOtpRequestDTO;
import com.srm.events.service.impl.AccountProvisioningService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/provisioning")
public class AccountProvisioningController {
    private final AccountProvisioningService service;
    public AccountProvisioningController(AccountProvisioningService service) { this.service = service; }
    @PostMapping("/student/signup") public ResponseEntity<?> student(@Valid @RequestBody StudentSignupRequestDTO request){return ResponseEntity.status(HttpStatus.ACCEPTED).body(service.signupStudent(request));}
    @PostMapping("/verify-otp") public ResponseEntity<?> verify(@Valid @RequestBody VerifyAccountOtpRequestDTO request){return ResponseEntity.ok(service.verify(request));}
    @PostMapping("/resend-otp") public ResponseEntity<?> resend(@Valid @RequestBody ResendAccountOtpRequestDTO request){return ResponseEntity.accepted().body(service.resend(request));}
}
