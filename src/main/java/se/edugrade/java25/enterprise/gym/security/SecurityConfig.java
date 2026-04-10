package se.edugrade.java25.enterprise.gym.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
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

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        // Public endpoints - no auth required
                        .requestMatchers("/auth/register","/auth/login").permitAll()
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico").permitAll()
                        //
                        .requestMatchers("/alive").permitAll()
                        // Swagger - public access
                        .requestMatchers(
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/v3/api-docs/**").permitAll()
                        // H2 Console - public access (dev only; not in production!)
                        .requestMatchers("/h2-console/**").permitAll()

                        // GET requests are public
                        .requestMatchers(HttpMethod.GET, "/classes","/classes/**").permitAll()

                        // POST requires USER or ADMIN
                        .requestMatchers(HttpMethod.POST, "/classes/{id}/bookings").hasAnyRole("USER", "ADMIN")

                        // POST requires ADMIN
                        .requestMatchers(HttpMethod.POST, "/classes").hasRole("ADMIN")
                        // PUT requires ADMIN
                        .requestMatchers(HttpMethod.PUT, "/classes/{id}").hasRole("ADMIN")
                        // DELETE requires ADMIN
                        .requestMatchers(HttpMethod.DELETE, "/classes/{id}").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/bookings/{id}").hasRole("ADMIN")

                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )

                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json");
                            response.getWriter().write("{\"error\": \"Unauthorized\"}");
                        })
                )

                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // H2 console needs frames
                .headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()))

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
        ;

        return http.build();
    }
}