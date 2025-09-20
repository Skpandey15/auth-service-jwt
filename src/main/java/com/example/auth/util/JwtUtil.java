package com.example.auth.util;

import com.nimbusds.jwt.SignedJWT;

import java.text.ParseException;

public class JwtUtil {
    public static String getSubject(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            return jwt.getJWTClaimsSet().getSubject();
        } catch (ParseException e) {
            throw new RuntimeException("Invalid token", e);
        }
    }
}
