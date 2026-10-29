
package com.example.voting_system.config;

import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

@Configuration
public class SecurityConfig {

    @Value("${app.admin.username:admin}")
    private String adminUsername;

    @Value("${app.admin.password}")
    private String adminPassword;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            PasswordEncoder passwordEncoder) {

        UserDetails authority = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(authority);
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {

        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOriginPatterns(List.of("*"));

        configuration.setAllowedMethods(
                List.of("GET", "POST", "PUT", "DELETE", "OPTIONS")
        );

        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(false);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .cors(cors ->
                cors.configurationSource(corsConfigurationSource())
            )
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth

                // Allow browser CORS preflight requests
                .requestMatchers(
                        HttpMethod.OPTIONS, "/**"
                ).permitAll()

                // Public candidate list for voters
                .requestMatchers(
                        HttpMethod.GET, "/candidates"
                ).permitAll()

                // Only Election Authority can add candidates
                .requestMatchers(
                        HttpMethod.POST, "/candidates"
                ).hasRole("ADMIN")

                // Election Authority only
                .requestMatchers(
                        "/candidates/winner",
                        "/voters/vote-records",
                        "/voters/reset-election",
                        "/voters/delete-all",
                        "/voters/add"
                ).hasRole("ADMIN")

                // Voter login
                .requestMatchers(
                        HttpMethod.GET, "/voters/login"
                ).permitAll()

                // Voter can submit a vote
                .requestMatchers(
                        HttpMethod.POST, "/voters/vote"
                ).permitAll()

                // Block public voter registration
                .requestMatchers(
                        HttpMethod.POST, "/voters"
                ).denyAll()

                // Prevent public access to voter list
                .requestMatchers(
                        HttpMethod.GET, "/voters"
                ).hasRole("ADMIN")

                // All other requests require Election Authority
                .anyRequest().hasRole("ADMIN")
            )
            .httpBasic(basic -> {})
            .formLogin(form -> form.disable());

        return http.build();
    }
}

