package com.example.veterinaria.security;

import java.nio.charset.StandardCharsets;
import java.util.function.Supplier;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
                        /* ---------------- públicas ---------------- */
                        .requestMatchers("/", "/staff", "/index.html", "/favicon.ico").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/imagenes/**", "/pages/**").permitAll()
                        .requestMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/registro").permitAll()
                        /* Webhook de Stripe: lo llama Stripe con su firma, sin sesión. */
                        .requestMatchers(HttpMethod.POST, "/api/pagos/webhook").permitAll()
                        .requestMatchers("/error").permitAll()

                        /* ---------------- sólo ADMIN ---------------- */
                        .requestMatchers("/dashboard/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/usuarios/{id}").access(WebSecurityConfig::esPropioOAdmin)
                        .requestMatchers(HttpMethod.PUT, "/api/usuarios/{id}").access(WebSecurityConfig::esPropioOAdmin)
                        .requestMatchers("/api/usuarios", "/api/usuarios/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/mascotas", "/api/citas",
                                "/api/notificaciones", "/api/cartillas", "/api/vacunaciones",
                                "/api/detalle-boletas").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/historial").hasAnyRole("ADMIN", "VETERINARIO")
                        .requestMatchers("/api/roles/**").hasRole("ADMIN")
                        .requestMatchers("/api/permisos/**").hasRole("ADMIN")
                        .requestMatchers("/api/rol-permisos/**").hasRole("ADMIN")
                        .requestMatchers("/api/boletas/**").hasRole("ADMIN")
                        .requestMatchers("/api/detalle-boletas/**").hasRole("ADMIN")
                        /* Pagos: clientes y administradores; el webhook ya quedó público. */
                        .requestMatchers("/api/pagos/**").hasAnyRole("CLIENTE", "ADMIN")
                        /* Páginas de retorno de Stripe (éxito / cancelación). */
                        .requestMatchers("/pago/exito", "/pago/cancelado").authenticated()
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/servicios/**", "/api/vacunas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/servicios/**", "/api/vacunas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/servicios/**", "/api/vacunas/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/mascotas/**", "/api/citas/**",
                                "/api/notificaciones/**", "/api/horarios/**", "/api/historial/**",
                                "/api/cartillas/**", "/api/vacunaciones/**").hasRole("ADMIN")

                        /* ---------------- sólo VETERINARIO ---------------- */
                        .requestMatchers(
                                "/dashboard/perfil-medico", "/dashboard/editar-perfil-medico",
                                "/dashboard/citas-vet", "/dashboard/mascotas-vet",
                                "/dashboard/mascota-vet/**",
                                "/dashboard/registrar-horario", "/dashboard/registrar-atencion/**",
                                "/dashboard/editar-horario/**").hasRole("VETERINARIO")

                        /* ---------------- sólo CLIENTE ---------------- */
                        .requestMatchers(
                                "/dashboard/mascotas", "/dashboard/registrar-mascota",
                                "/dashboard/editar-mascota/**", "/dashboard/mascota/**",
                                "/dashboard/citas", "/dashboard/registrar-cita",
                                "/dashboard/editar-cita/**", "/dashboard/cartilla",
                                "/dashboard/registrar-vacunacion").hasRole("CLIENTE")

                        /* ---------------- cualquier autenticado ---------------- */
                        .requestMatchers("/dashboard/cambiar-password").authenticated()
                        .requestMatchers("/dashboard/**").authenticated()
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form
                        .loginProcessingUrl("/api/auth/login")
                        .usernameParameter("username")
                        .passwordParameter("password")
                        .successHandler((request, response, authentication) -> {
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

                            json(response, 200, "{\"mensaje\":\"Login exitoso\",\"rol\":\"" + authority
                                    + "\",\"redirect\":\"" + redirectUrl + "\"}");
                        })
                        .failureHandler((request, response, exception) ->
                                json(response, 401, "{\"mensaje\":\"Credenciales incorrectas\"}"))
                )
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessHandler((request, response, authentication) ->
                                json(response, 200, "{\"mensaje\":\"Logout exitoso\"}"))
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                json(response, 401, "{\"mensaje\":\"No autenticado\"}");
                            } else {
                                // S1: Redirige al inicio indicando que abra el modal de login
                                response.sendRedirect("/?login=true");
                            }
                        })
                        .accessDeniedHandler((request, response, deniedException) -> {
                            if (request.getRequestURI().startsWith("/api/")) {
                                json(response, 403,
                                        "{\"mensaje\":\"No tiene permisos para realizar esta accion\"}");
                                return;
                            }

                            String destino = "/";
                            Authentication auth =
                                    SecurityContextHolder.getContext().getAuthentication();

                            if (auth != null && auth.isAuthenticated()
                                    && tieneRol(auth, "ROLE_ADMIN")) {
                                destino = "/dashboard/admin";
                            } else if (auth != null && auth.isAuthenticated()
                                    && tieneRol(auth, "ROLE_VETERINARIO")) {
                                destino = "/dashboard/perfil-medico";
                            } else if (auth != null && auth.isAuthenticated()) {
                                destino = "/dashboard/perfil";
                            }
                            response.sendRedirect(destino);
                        })
                );

        return http.build();
    }

    private static boolean tieneRol(Authentication auth, String rol) {
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals(rol));
    }

    private static AuthorizationDecision esPropioOAdmin(
            Supplier<? extends Authentication> authentication,
            RequestAuthorizationContext context) {

        Authentication auth = authentication.get();
        if (auth == null || !auth.isAuthenticated()) {
            return new AuthorizationDecision(false);
        }

        if (tieneRol(auth, "ROLE_ADMIN")) {
            return new AuthorizationDecision(true);
        }

        String idRuta = context.getVariables().get("id");
        Object principal = auth.getPrincipal();

        if (idRuta == null || !(principal instanceof UsuarioPrincipal usuario)) {
            return new AuthorizationDecision(false);
        }

        try {
            return new AuthorizationDecision(usuario.getIdUsuario().equals(Long.parseLong(idRuta)));
        } catch (NumberFormatException e) {
            return new AuthorizationDecision(false);
        }
    }

    private static void json(HttpServletResponse response, int status, String body)
            throws java.io.IOException {
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType("application/json");
        response.getWriter().write(body);
    }
}