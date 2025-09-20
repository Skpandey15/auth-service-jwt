
package com.example.auth.service;

import com.example.auth.domain.User;
import com.example.auth.domain.RefreshToken;
import com.example.auth.repository.UserRepository;
import com.example.auth.repository.RefreshTokenRepository;
import com.example.auth.dto.RegisterRequest;
import com.example.auth.dto.LoginRequest;
import com.example.auth.dto.LoginResponse;
import org.springframework.security.crypto.argon2.Argon2PasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
public class AuthService {

    private final UserRepository userRepo;
    private final RefreshTokenRepository refreshTokenRepo;
    private final TokenService tokenService;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepo,
                       RefreshTokenRepository refreshTokenRepo,
                       PasswordEncoder passwordEncoder,   // injected
                       TokenService tokenService) {
        this.userRepo = userRepo;
        this.refreshTokenRepo = refreshTokenRepo;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
    }

    @Transactional
    public void register(RegisterRequest req) {
        User u = new User();
        u.setEmail(req.getEmail());
        u.setName(req.getName());
        String hash = passwordEncoder.encode(req.getPassword());
        u.setPasswordHash(hash);
        userRepo.save(u);
        // send verification email (async) - omitted for brevity
    }

    @Transactional
    public LoginResponse login(LoginRequest req, String deviceInfo, String ipAddress) {
        User user = userRepo.findByEmail(req.getEmail())
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        if (!passwordEncoder.matches(req.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("Invalid credentials");
        }

        String refreshToken = tokenService.createRefreshToken();
        String refreshHash = tokenService.hash(refreshToken);
        RefreshToken rt = new RefreshToken();
        rt.setUserId(user.getId());
        rt.setTokenHash(refreshHash);
        rt.setDeviceInfo(deviceInfo);
        rt.setIpAddress(ipAddress);
        rt.setIssuedAt(Instant.now());
        rt.setExpiresAt(Instant.now().plus(30, ChronoUnit.DAYS));
        refreshTokenRepo.save(rt);

        String accessToken = tokenService.createAccessToken(user);

        return new LoginResponse(accessToken, "Bearer", 900, refreshToken);
    }

    @Transactional
    public void logout(String refreshToken) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new RuntimeException("Refresh token required");
        }

        String hashed = tokenService.hash(refreshToken);

        RefreshToken token = refreshTokenRepo.findByTokenHash(hashed)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (token.getRevokedAt() != null) {
            throw new RuntimeException("Token already revoked");
        }

        token.setRevokedAt(Instant.now());
        refreshTokenRepo.save(token);
    }
}
