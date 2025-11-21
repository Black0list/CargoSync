package com.spring.logitrack.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(auth -> auth

                        // PUBLIC ENDPOINTS
                        .requestMatchers("/api/register", "/api/login").permitAll()

                        // PUBLIC PRODUCT GET
                        .requestMatchers(HttpMethod.GET, "/api/products/**").permitAll()

                        // PRODUCTS – ADMIN ONLY (POST, PUT, DELETE, PATCH)
                        .requestMatchers("/api/products/**").hasRole("ADMIN")

                        // USERS – ADMIN ONLY
                        .requestMatchers("/api/users/**").hasRole("ADMIN")

                        // WAREHOUSES – MANAGER + ADMIN
                        .requestMatchers("/api/warehouses/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // INVENTORIES – MANAGER + ADMIN
                        .requestMatchers("/api/inventories/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // SHIPMENTS – MANAGER + ADMIN
                        .requestMatchers("/api/shipments/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // BACKORDERS – MANAGER + ADMIN
                        .requestMatchers("/api/backorders/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // PURCHASE ORDERS – MANAGER + ADMIN
                        .requestMatchers("/api/purchase-orders/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // SIMPLE ORDERS – MANAGER + ADMIN
                        .requestMatchers("/api/simpleorders/**")
                        .hasAnyRole("WAREHOUSE_MANAGER", "ADMIN")

                        // SALES ORDERS – CLIENT + MANAGER + ADMIN
                        .requestMatchers("/api/sales-orders/**")
                        .hasAnyRole("CLIENT","WAREHOUSE_MANAGER","ADMIN")

                        // ANYTHING ELSE → authenticated
                        .anyRequest().authenticated()
                )

                .httpBasic(Customizer.withDefaults());

        return http.build();
    }
}
