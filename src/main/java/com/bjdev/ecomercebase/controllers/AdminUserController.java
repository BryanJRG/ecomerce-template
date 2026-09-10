package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.dto.response.UserResponse;
import com.bjdev.ecomercebase.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/** Protected by hasRole('ADMIN') + the Cloudflare tunnel filter (see SecurityConfig). */
@RestController
@RequestMapping("/api/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserService userService;

    @GetMapping
    public ResponseEntity<List<UserResponse>> list() {
        return ResponseEntity.ok(userService.listUsers());
    }

    @PatchMapping("/{id}/disable")
    public ResponseEntity<Void> disable(@PathVariable Long id) {
        userService.disableUser(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/{id}/enable")
    public ResponseEntity<Void> enable(@PathVariable Long id) {
        userService.enableUser(id);
        return ResponseEntity.noContent().build();
    }
}
