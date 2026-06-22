package com.requestflow.service;

import com.requestflow.domain.entity.User;
import io.smallrye.jwt.algorithm.SignatureAlgorithm;
import io.smallrye.jwt.build.Jwt;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.keys.HmacKey;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.Instant;
import java.util.Set;

@ApplicationScoped
public class JwtTokenService {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "requestflow.jwt.expiration.minutes")
    long expirationMinutes;

    @ConfigProperty(name = "requestflow.jwt.secret")
    String jwtSecret;

    public String generateToken(User user) {
        Instant expiresAt = Instant.now().plus(Duration.ofMinutes(expirationMinutes));
        HmacKey secretKey = new HmacKey(jwtSecret.getBytes(StandardCharsets.UTF_8));

        return Jwt.issuer(issuer)
                .subject(user.getId().toString())
                .upn(user.getEmail())
                .groups(Set.of(user.getRole().name()))
                .claim("email", user.getEmail())
                .claim("role", user.getRole().name())
                .expiresAt(expiresAt)
                .jws()
                .algorithm(SignatureAlgorithm.HS256)
                .sign(secretKey);
    }
}
