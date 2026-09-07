package com.bjdev.base.controllers;

import com.bjdev.base.dto.request.CreateAdminInvitationRequest;
import com.bjdev.base.dto.response.AdminActionResult;
import com.bjdev.base.dto.response.AdminInvitationResponse;
import com.bjdev.base.services.interfaces.AdminInvitationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/invitations")
@RequiredArgsConstructor
public class AdminInvitationController {

    private final AdminInvitationService adminInvitationService;

    @PostMapping
    public ResponseEntity<AdminActionResult<AdminInvitationResponse>> create(@Valid @RequestBody CreateAdminInvitationRequest request) {
        return ResponseEntity.ok(adminInvitationService.createInvitation(request));
    }

    @GetMapping
    public ResponseEntity<List<AdminInvitationResponse>> list() {
        return ResponseEntity.ok(adminInvitationService.listInvitations());
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<AdminInvitationResponse> resend(@PathVariable Long id) {
        return ResponseEntity.ok(adminInvitationService.resendInvitation(id));
    }

    @PostMapping("/{id}/cancel")
    public ResponseEntity<AdminInvitationResponse> cancel(@PathVariable Long id) {
        return ResponseEntity.ok(adminInvitationService.cancelInvitation(id));
    }
}
