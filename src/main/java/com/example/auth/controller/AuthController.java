
package com.example.auth.controller;

import com.example.auth.dto.*;
import com.example.auth.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService s) { this.authService = s; }

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest req) {
        authService.register(req);
        return ResponseEntity.status(201).body(new GenericResponse(true, "Verification email sent"));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req, HttpServletRequest servletReq) {
        String deviceInfo = servletReq.getHeader("User-Agent");
        String ip = servletReq.getRemoteAddr();
        LoginResponse resp = authService.login(req, deviceInfo, ip);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(@RequestBody LogoutRequest req) {
        authService.logout(req.getRefreshToken());
        return ResponseEntity.ok(new GenericResponse(true, "Logged out"));
    }
}
