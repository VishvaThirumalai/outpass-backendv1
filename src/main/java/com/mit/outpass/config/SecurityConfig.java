package com.mit.outpass.config;

import java.util.Arrays;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.servlet.util.matcher.MvcRequestMatcher;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.servlet.handler.HandlerMappingIntrospector;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

   @Autowired
private JwtAuthenticationFilter jwtAuthenticationFilter;
@Bean
public SecurityFilterChain filterChain(HttpSecurity http, HandlerMappingIntrospector introspector) throws Exception {
    MvcRequestMatcher.Builder mvc = new MvcRequestMatcher.Builder(introspector);
    
    http
        .cors(cors -> cors.configurationSource(corsConfigurationSource()))
        .csrf(csrf -> csrf.disable())
        .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
        
        // Add JWT filter before Spring Security's filters
        .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        
        .authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers(mvc.pattern("/auth/**")).permitAll()
    .requestMatchers(mvc.pattern("/h2-console/**")).permitAll()
    .requestMatchers(mvc.pattern("/admin/simple-reset-password")).permitAll()
    
    // User profile endpoints - accessible to all authenticated users
    .requestMatchers(mvc.pattern("/user/profile")).authenticated()
    
    // Role-based endpoints
    .requestMatchers(mvc.pattern("/student/**")).hasAuthority("ROLE_STUDENT")
    .requestMatchers(mvc.pattern("/warden/**")).hasAuthority("ROLE_WARDEN")
    .requestMatchers(mvc.pattern("/api/security/**")).hasAuthority("ROLE_SECURITY")
    .requestMatchers(mvc.pattern("/admin/**")).hasAuthority("ROLE_ADMIN")
    
    // All other requests require authentication
    .anyRequest().authenticated()
)
        .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));

    return http.build();
}

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList(
        "https://outpass-frontendv1.onrender.com",
        "https://outpass-v2-testing.onrender.com",
        "http://localhost:3000",
        "https://outpass-frontend.onrender.com"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
        configuration.setExposedHeaders(Arrays.asList("Authorization"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
