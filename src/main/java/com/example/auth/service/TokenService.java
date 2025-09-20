package com.example.auth.service;

import com.example.auth.domain.User;
import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.*;
import com.nimbusds.jwt.*;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.stereotype.Service;

import jakarta.annotation.PostConstruct;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;

@Service
public class TokenService {

    private final ResourceLoader resourceLoader;

    @Value("${jwt.private-key-location}")
    private String privateKeyLocation;

    @Value("${jwt.public-key-location}")
    private String publicKeyLocation;

    private RSAPrivateKey privateKey;
    private RSAPublicKey publicKey;

    public TokenService(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }

    @PostConstruct
    public void initKeys() {
        try {
            Resource privRes = resourceLoader.getResource(privateKeyLocation);
            try (InputStream is = privRes.getInputStream()) {
                this.privateKey = loadPrivateKey(is);
            }

            Resource pubRes = resourceLoader.getResource(publicKeyLocation);
            try (InputStream is = pubRes.getInputStream()) {
                this.publicKey = loadPublicKey(is);
            }
        } catch (Exception e) {
            // Fail-fast on startup so you don't get runtime NPEs later
            throw new IllegalStateException("Unable to load RSA keys from configured locations: "
                    + privateKeyLocation + " and " + publicKeyLocation, e);
        }
    }

    private RSAPrivateKey loadPrivateKey(InputStream is) throws Exception {
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PRIVATE KEY-----", "")
                .replace("-----END PRIVATE KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPrivateKey) kf.generatePrivate(spec);
    }

    private RSAPublicKey loadPublicKey(InputStream is) throws Exception {
        String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\\s+", "");
        byte[] decoded = Base64.getDecoder().decode(pem);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(decoded);
        KeyFactory kf = KeyFactory.getInstance("RSA");
        return (RSAPublicKey) kf.generatePublic(spec);
    }

    public String createAccessToken(User user) {
        try {
            JWSSigner signer = new RSASSASigner(privateKey);

            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issuer("https://auth.example.com")
                    .subject(user.getId().toString())
                    .claim("email", user.getEmail())
                    .issueTime(Date.from(Instant.now()))
                    .expirationTime(Date.from(Instant.now().plusSeconds(900)))
                    .jwtID(UUID.randomUUID().toString())
                    .build();

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                    .type(JOSEObjectType.JWT).build();

            SignedJWT signedJWT = new SignedJWT(header, claims);
            signedJWT.sign(signer);
            return signedJWT.serialize();
        } catch (Exception ex) {
            throw new RuntimeException("Token creation failed", ex);
        }
    }

    public String createRefreshToken() {
        return UUID.randomUUID().toString() + UUID.randomUUID().toString();
    }

    public String hash(String token) {
        return DigestUtils.sha256Hex(token);
    }
}
