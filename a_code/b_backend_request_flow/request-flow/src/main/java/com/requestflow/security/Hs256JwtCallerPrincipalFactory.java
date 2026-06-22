package com.requestflow.security;

import io.smallrye.jwt.auth.principal.DefaultJWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTAuthContextInfo;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipal;
import io.smallrye.jwt.auth.principal.JWTCallerPrincipalFactory;
import io.smallrye.jwt.auth.principal.ParseException;
import jakarta.annotation.Priority;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.enterprise.inject.Alternative;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.jose4j.jwa.AlgorithmConstraints;
import org.jose4j.jwa.AlgorithmConstraints.ConstraintType;
import org.jose4j.jws.AlgorithmIdentifiers;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.consumer.InvalidJwtException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.jose4j.keys.HmacKey;

import java.nio.charset.StandardCharsets;

@ApplicationScoped
@Alternative
@Priority(1)
public class Hs256JwtCallerPrincipalFactory extends JWTCallerPrincipalFactory {

    @ConfigProperty(name = "mp.jwt.verify.issuer")
    String issuer;

    @ConfigProperty(name = "requestflow.jwt.secret")
    String jwtSecret;

    @Override
    public JWTCallerPrincipal parse(String token, JWTAuthContextInfo authContextInfo) throws ParseException {
        try {
            JwtConsumer consumer = new JwtConsumerBuilder()
                    .setRequireExpirationTime()
                    .setExpectedIssuer(issuer)
                    .setVerificationKey(new HmacKey(jwtSecret.getBytes(StandardCharsets.UTF_8)))
                    .setJwsAlgorithmConstraints(new AlgorithmConstraints(
                            ConstraintType.PERMIT,
                            AlgorithmIdentifiers.HMAC_SHA256
                    ))
                    .build();
            JwtClaims claims = consumer.processToClaims(token);
            return new DefaultJWTCallerPrincipal(token, claims);
        } catch (InvalidJwtException exception) {
            throw new ParseException("Token JWT invalido", exception);
        }
    }
}
