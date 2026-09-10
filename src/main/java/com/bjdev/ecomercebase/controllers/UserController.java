package com.bjdev.ecomercebase.controllers;

import com.bjdev.ecomercebase.services.interfaces.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @DeleteMapping("/me")
    public ResponseEntity<Void> disableSelf() {
        userService.disableSelf();
        return ResponseEntity.noContent().build();
    }
}
