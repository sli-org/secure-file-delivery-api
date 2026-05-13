package za.co.api.statement.test.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@TestConfiguration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true)
@Profile("test")
public class TestSecurityConfig {

    @Bean
    public SecurityFilterChain testSecurityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                .requestMatchers("/api/v1/statements/download/**").permitAll()
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(testJwtAuthenticationConverter()))
            );
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationConverter testJwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(jwt -> {
            List<String> scopes = jwt.getClaimAsStringList("scp");
            List<String> roles = jwt.getClaimAsStringList("roles");
            
            java.util.Collection<org.springframework.security.core.GrantedAuthority> authorities = new java.util.ArrayList<>();
            
            if (scopes != null) {
                authorities.addAll(scopes.stream()
                    .map(scope -> new org.springframework.security.core.authority.SimpleGrantedAuthority("SCOPE_" + scope))
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            if (roles != null) {
                authorities.addAll(roles.stream()
                    .map(role -> new org.springframework.security.core.authority.SimpleGrantedAuthority("ROLE_" + role))
                    .collect(java.util.stream.Collectors.toList()));
            }
            
            return authorities;
        });
        return converter;
    }

    @Bean
    @Primary
    public JwtDecoder testJwtDecoder() {
        return token -> {
            if (token != null && token.equals("invalid.jwt.token")) {
                throw new JwtException("Invalid JWT token");
            }
            
            // Default valid token
            return Jwt.withTokenValue(token != null ? token : "default-token")
                .header("alg", "RS256")
                .claim("scp", "statement:read")
                .claim("roles", List.of())
                .claim("aud", List.of("test-audience"))
                .claim("iss", "https://test-issuer")
                .issuedAt(Instant.now())
                .expiresAt(Instant.now().plus(1, ChronoUnit.HOURS))
                .build();
        };
    }
}