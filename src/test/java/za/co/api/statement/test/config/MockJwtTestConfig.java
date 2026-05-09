package za.co.api.statement.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;

@TestConfiguration
public class MockJwtTestConfig {

    @Bean
    @Primary
    public JwtDecoder jwtDecoder() {
        return new JwtDecoder() {
            @Override
            public Jwt decode(String token) throws JwtException {
                return Jwt.withTokenValue(token)
                        .header("alg", "none")
                        .header("typ", "JWT")
                        .claim("sub", "test-user")
                        .claim("scope", "read statements:read")
                        .claim("client_id", "test-client")
                        .issuedAt(Instant.now())
                        .expiresAt(Instant.now().plusSeconds(3600))
                        .build();
            }
        };
    }
}