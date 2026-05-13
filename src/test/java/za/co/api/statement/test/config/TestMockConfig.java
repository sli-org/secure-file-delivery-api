package za.co.api.statement.test.config;

import com.azure.storage.blob.BlobContainerClient;
import com.azure.storage.blob.BlobContainerClientBuilder;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@TestConfiguration
@Profile("test")
public class TestMockConfig {

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            if (token != null && token.equals("invalid.jwt.token")) {
                throw new JwtException("Invalid JWT token");
            }
            
            // For "valid-test-token" - admin access
            if (token != null && token.equals("valid-test-token")) {
                return Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .claim("scp", "statement:read statement:create statement:delete")
                    .claim("roles", List.of("common-api.admin"))
                    .claim("aud", List.of("test-audience"))
                    .claim("iss", "https://test-issuer")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();
            }
            
            // For read-only token
            if (token != null && token.equals("readonly-token")) {
                return Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .claim("scp", "statement:read")
                    .claim("roles", List.of())
                    .claim("aud", List.of("test-audience"))
                    .claim("iss", "https://test-issuer")
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                    .build();
            }
            
            // Default - valid but minimal scopes
            return Jwt.withTokenValue(token != null ? token : "default-token")
                .header("alg", "RS256")
                .claim("scp", "statement:read")
                .claim("aud", List.of("test-audience"))
                .claim("iss", "https://test-issuer")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        };
    }

    @Bean
    @Primary
    public BlobContainerClient blobContainerClient() {
        // Mock Azure Blob Container Client
        BlobContainerClient mockClient = mock(BlobContainerClient.class);
        BlobContainerClientBuilder mockBuilder = mock(BlobContainerClientBuilder.class);
        
        when(mockBuilder.buildClient()).thenReturn(mockClient);
        
        return mockClient;
    }
}