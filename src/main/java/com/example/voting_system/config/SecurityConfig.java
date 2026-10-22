
package com.example.voting_system.config;

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

        UserDetails admin = User.withUsername(adminUsername)
                .password(passwordEncoder.encode(adminPassword))
                .roles("ADMIN")
                .build();

        return new InMemoryUserDetailsManager(admin);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .authorizeHttpRequests(auth -> auth

                // Candidate list is public
                .requestMatchers(
                        HttpMethod.GET,
                        "/candidates"
                ).permitAll()

                // Winner/result is Admin-only
                .requestMatchers(
                        "/candidates/winner"
                ).hasRole("ADMIN")

                // Voter login
                .requestMatchers(
                        "/voters/login"
                ).permitAll()

                // Voter registration and voting
                .requestMatchers(
                        HttpMethod.POST,
                        "/voters",
                        "/voters/vote"
                ).permitAll()

                // Admin-only operations
                .requestMatchers(
                        "/voters/vote-records",
                        "/voters/reset-election",
                        "/voters/delete-all"
                ).hasRole("ADMIN")

                // All other endpoints require Admin
                .anyRequest().hasRole("ADMIN")
            )

            .httpBasic(basic -> {})
            .formLogin(form -> form.disable());

        return http.build();
    }
}

