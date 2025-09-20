package com.example.auth.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;

import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;

//@Configuration
public class JwtConfig {

    @Bean
    public JwtDecoder jwtDecoder() throws Exception {
        var resource = new ClassPathResource("keys/public.pem");
        var keyBytes = resource.getInputStream().readAllBytes();
        String publicKeyContent = new String(keyBytes)
                .replace("-----BEGIN PUBLIC KEY-----", "")
                .replace("-----END PUBLIC KEY-----", "")
                .replaceAll("\s+","");
        byte[] decoded = Base64.getDecoder().decode(publicKeyContent);
        RSAPublicKey pubKey = (RSAPublicKey) KeyFactory.getInstance("RSA")
                .generatePublic(new X509EncodedKeySpec(decoded));
        return NimbusJwtDecoder.withPublicKey(pubKey).build();
    }
}
