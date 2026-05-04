package za.co.api.statement.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfigurationSource;

/**
 * Security configuration for Statement API.
 * 
 * <p>This configuration uses supporting beans provided by common-starter-security:</p>
 * <ul>
 *   <li>JwtAuthenticationConverter - Extracts roles/scopes from JWT claims</li>
 *   <li>CorsConfigurationSource - Pre-configured for allowed domains</li>
 *   <li>ClaimsService - JWT claim extraction utilities</li>
 * </ul>
 * 
 * <p>The download endpoint uses token-based authentication (HMAC-signed tokens)
 * instead of JWT, so it is configured as permitAll.</p>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@Profile("!test")
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationConverter jwtAuthenticationConverter,
            CorsConfigurationSource corsConfigurationSource) throws Exception {
        
        http
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource))
            .authorizeHttpRequests(authz -> authz
                // Public endpoints - Swagger UI & API Docs
                .requestMatchers("/swagger-ui/**", "/v3/api-docs/**", "/webjars/**").permitAll()
                
                // Public actuator endpoints (health checks)
                .requestMatchers("/actuator/health", "/actuator/info").permitAll()
                
                // Protected actuator endpoints (admin only)
                .requestMatchers("/actuator/**").hasRole("common-api.admin")
                
                // Token-based download endpoint (no JWT required - uses HMAC-signed tokens)
                .requestMatchers("/api/v1/statements/download/**").permitAll()
                
                // All other API endpoints require authentication
                .requestMatchers("/api/**").authenticated()
                
                // Deny everything else by default
                .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter))
            );
        
        return http.build();
    }
}
