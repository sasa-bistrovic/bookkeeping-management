package com.by.sasa.bistrovic.bookkeeping.management;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfig {

    private final JwtFilter jwtFilter;

    public SecurityConfig(JwtFilter jwtFilter) {
        this.jwtFilter = jwtFilter;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            )

            .authorizeHttpRequests(auth -> auth

                // PUBLIC ENDPOINTS
                .requestMatchers(
                        //"/api/**",
                        "/api/users/login",
                        "/api/users/register",

                        "/api/accounts/all/batch",
                        "/api/invoices/all/batch",
                        "/api/giroaccounts/all/batch",
                        "/api/ledger/all/batch",
                        "/api/aop/all/batch",
                        "/api/warehouse/all/batch",

                        "/api/account-classes/batch",
                        "/api/account-groups/batch",
                        "/api/account-synthetics/batch",
                        "/api/account-analyticals/batch",
                        "/api/account-sub-analyticals/batch"
                ).permitAll()

                // ALL API ENDPOINTS REQUIRE AUTH
                .requestMatchers("/api/**").authenticated()

                // EVERYTHING ELSE DENIED
                .anyRequest().permitAll()
            )

            .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}