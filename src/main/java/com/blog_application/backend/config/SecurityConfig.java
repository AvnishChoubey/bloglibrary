package com.blog_application.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Autowired JwtAuthFilter jwtAuthFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity) {
        httpSecurity
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        /* USER ENDPOINTS */
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        /* AUTH ENDPOINTS */
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                        /* COMMENT ENDPOINTS */
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/*/comments/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/*/comments/**").permitAll()
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*/comments/**").hasAnyRole("AUTHOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*/comments/**").hasAnyRole("AUTHOR")
                        /* POSTS ENDPOINTS */
                        .requestMatchers(HttpMethod.POST, "/api/v1/posts/create").hasAnyRole("ADMIN", "AUTHOR")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/posts/*/update").hasAnyRole("ADMIN", "AUTHOR")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/posts/*/delete").hasAnyRole("ADMIN", "AUTHOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/posts/**").permitAll()
                        .anyRequest().authenticated())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return httpSecurity.build();
    }

    @Bean
    @Primary
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    @Primary
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) {
        return config.getAuthenticationManager();
    }
}
