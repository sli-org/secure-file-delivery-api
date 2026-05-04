package za.co.api.statement.test.config;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Security configuration for integration tests.
 * Provides a test JwtDecoder that accepts synthetic test tokens.
 */
@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity
public class IntegrationTestSecurityConfig {

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            // Reject truly invalid tokens
            if (token.contains("invalid")) {
                throw new org.springframework.security.oauth2.jwt.JwtException("Invalid test token");
            }

            // Parse synthetic test tokens: format "header.payload.scope-info"
            String scope = "statement:read statement:create statement:delete";
            List<String> roles = List.of("common-api.admin");

            if (token.contains("readonly")) {
                scope = "statement:read";
                roles = List.of("statement-api.readonly");
            } else if (token.contains("noscope")) {
                scope = "";
                roles = Collections.emptyList();
            } else if (token.contains("limited")) {
                // Extract scope from token suffix
                String[] parts = token.split("\\.");
                if (parts.length > 2) {
                    scope = parts[2].replace("-", ":");
                }
                roles = Collections.emptyList();
            }

            return Jwt.withTokenValue(token)
                    .header("alg", "RS256")
                    .header("typ", "JWT")
                    .claim("sub", "test-user")
                    .claim("iss", "https://login.microsoftonline.com/test-tenant/v2.0")
                    .claim("aud", "test-audience-id")
                    .claim("scp", scope)
                    .claim("roles", roles)
                    .issuedAt(Instant.now())
                    .expiresAt(Instant.now().plusSeconds(3600))
                    .build();
        };
    }

    @Bean
    @Primary
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/v1/statements/download/**").permitAll()
                        .anyRequest().authenticated())
                .oauth2ResourceServer(oauth2 -> oauth2
                        .jwt(jwt -> jwt
                                .decoder(testJwtDecoder())
                                .jwtAuthenticationConverter(testJwtAuthenticationConverter())));
        return http.build();
    }

    private JwtAuthenticationConverter testJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            Stream<GrantedAuthority> scopeAuthorities = Stream.empty();
            Stream<GrantedAuthority> roleAuthorities = Stream.empty();

            String scp = jwt.getClaimAsString("scp");
            if (scp != null && !scp.isBlank()) {
                scopeAuthorities = Arrays.stream(scp.split(" "))
                        .map(s -> new SimpleGrantedAuthority("SCOPE_" + s));
            }

            List<String> roles = jwt.getClaimAsStringList("roles");
            if (roles != null) {
                roleAuthorities = roles.stream()
                        .map(r -> new SimpleGrantedAuthority("ROLE_" + r));
            }

            return Stream.concat(scopeAuthorities, roleAuthorities)
                    .collect(Collectors.toList());
        });
        return converter;
    }
}
