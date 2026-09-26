package com.example.veterinaria.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .authorizeHttpRequests(auth -> auth
                // Public routes - Thymeleaf pages
                .requestMatchers("/", "/staff").permitAll()
                .requestMatchers("/css/**", "/js/**", "/imagenes/**").permitAll()
                .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                // API endpoints - require authentication
                .requestMatchers("/api/**").authenticated()
                // Dashboard pages - require authentication
                .requestMatchers("/dashboard/**").authenticated()
                // Everything else
                .anyRequest().permitAll()
            )
            .formLogin(form -> form
                .loginProcessingUrl("/api/auth/login")
                .usernameParameter("username")
                .passwordParameter("password")
                .successHandler((request, response, authentication) -> {
                    // Determine redirect URL based on role
                    String authority = authentication.getAuthorities().iterator().next().getAuthority();
                    String redirectUrl;
                    
                    switch (authority) {
                        case "ROLE_CLIENTE":
                            redirectUrl = "/dashboard/perfil";
                            break;
                        case "ROLE_VETERINARIO":
                            redirectUrl = "/dashboard/perfil-medico";
                            break;
                        case "ROLE_ADMIN":
                            redirectUrl = "/dashboard/admin";
                            break;
                        default:
                            redirectUrl = "/";
                    }
                    
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"mensaje\":\"Login exitoso\",\"rol\":\"" + authority + "\",\"redirect\":\"" + redirectUrl + "\"}");
                })
                .failureHandler((request, response, exception) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"mensaje\":\"Credenciales incorrectas\"}");
                })
            )
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> {
                    response.setStatus(200);
                    response.setContentType("application/json");
                    response.getWriter().write("{\"mensaje\":\"Logout exitoso\"}");
                })
            )
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    // For API requests, return 401
                    if (request.getRequestURI().startsWith("/api/")) {
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write("{\"mensaje\":\"No autenticado\"}");
                    } else {
                        // For page requests, redirect to login
                        response.sendRedirect("/");
                    }
                })
            );

        return http.build();
    }
}
