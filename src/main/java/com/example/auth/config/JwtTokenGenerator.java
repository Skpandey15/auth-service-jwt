package com.example.auth.config;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.RSASSASigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.time.Instant;
import java.util.Base64;
import java.util.Date;

public class JwtTokenGenerator {

    /**
     * Load PKCS#8 private key from classpath (keys/private.pem), build RSAPrivateKey.
     */
    public static RSAPrivateKey loadPrivateKeyFromClasspath(String resourcePath) throws Exception {
        try (InputStream is = JwtTokenGenerator.class.getClassLoader().getResourceAsStream(resourcePath)) {
            if (is == null) {
                throw new IllegalStateException("Resource not found on classpath: " + resourcePath);
            }
            String pem = new String(is.readAllBytes(), StandardCharsets.UTF_8);
            // remove header/footer and whitespace
            String base64 = pem
                    .replaceAll("-----BEGIN (.*)-----", "")
                    .replaceAll("-----END (.*)-----", "")
                    .replaceAll("\\s", "");

            byte[] decoded = Base64.getDecoder().decode(base64);
            PKCS8EncodedKeySpec keySpec = new PKCS8EncodedKeySpec(decoded);
            KeyFactory kf = KeyFactory.getInstance("RSA");
            return (RSAPrivateKey) kf.generatePrivate(keySpec);
        }
    }

    /**
     * Generate RS256 signed JWT.
     *
     * @param issuer Issuer (iss claim)
     * @param subject Subject (sub claim), e.g. user id
     * @param expirySeconds access token lifetime in seconds (from now)
     * @return signed JWT string
     */
    public static String generateToken(RSAPrivateKey privateKey, String issuer, String subject, long expirySeconds) throws Exception {
        Instant now = Instant.now();
        Date iat = Date.from(now);
        Date exp = Date.from(now.plusSeconds(expirySeconds));

        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issuer(issuer)
                .subject(subject)
                .issueTime(iat)
                .expirationTime(exp)
                .claim("typ", "access") // optional custom claims
                .build();

        JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.RS256)
                .type(JOSEObjectType.JWT)
                .build();

        SignedJWT signedJWT = new SignedJWT(header, claims);
        JWSSigner signer = new RSASSASigner(privateKey);
        signedJWT.sign(signer);

        return signedJWT.serialize();
    }

    // example main for quick CLI usage
    public static void main(String[] args) throws Exception {
        // args: <issuer> <subject> <expirySeconds>
        String issuer = args.length > 0 ? args[0] : "https://auth.example.com";
        String subject = args.length > 1 ? args[1] : "user123";
        long expiry = args.length > 2 ? Long.parseLong(args[2]) : 900L; // 15 minutes

        RSAPrivateKey pk = loadPrivateKeyFromClasspath("keys/private.pem"); // classpath path
        String token = generateToken(pk, issuer, subject, expiry);
        System.out.println(token);
    }
}

