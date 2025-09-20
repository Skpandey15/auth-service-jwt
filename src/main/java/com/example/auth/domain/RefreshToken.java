
package com.example.auth.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "refresh_tokens")
@Getter @Setter @NoArgsConstructor
public class RefreshToken {
    @Id
    @Column(columnDefinition = "uuid")
    private UUID id = UUID.randomUUID();

    @Column(columnDefinition = "uuid", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private String tokenHash;

    private String deviceInfo;
    private String ipAddress;

    private Instant issuedAt = Instant.now();
    private Instant expiresAt;
    private Instant revokedAt;
}
