package com.ecommerce.config;

import com.ecommerce.security.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Spring Security Configuration
 * Session-based authentication with form login
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final CustomUserDetailsService userDetailsService;

    /**
     * BCrypt Password Encoder Bean
     * Uses BCrypt hashing algorithm with strength 10
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Authentication Manager Bean
     * Required for authentication in controllers
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * DAO Authentication Provider
     * Connects UserDetailsService with PasswordEncoder for database authentication
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * Security Filter Chain Configuration
     * Session-based authentication with form login.
     * Public: /, /products/**, /error/**, /actuator/health
     * Authenticated: /orders/**, /cart/**, /payments/**
     */
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // Disable CSRF (for simplicity — H2 console + Thymeleaf forms)
                .csrf(csrf -> csrf.disable())

                // Enable H2 Console (disable frame options)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable()))

                // Configure endpoint authorization
                .authorizeHttpRequests(auth -> auth
                        // Public — no login required
                        .requestMatchers("/").permitAll()
                        .requestMatchers("/products/**").permitAll()
                        .requestMatchers("/error/**").permitAll()
                        .requestMatchers("/actuator/health").permitAll()
                        .requestMatchers("/login").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/h2-console/**").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**", "/favicon.ico").permitAll()

                        // Protected — must be authenticated
                        .requestMatchers("/orders/**").authenticated()
                        .requestMatchers("/cart/**").authenticated()
                        .requestMatchers("/payments/**").authenticated()

                        // All other endpoints require authentication
                        .anyRequest().authenticated())

                // Enable form login
                .formLogin(form -> form
                        .loginPage("/login")
                        .loginProcessingUrl("/auth/login")
                        .defaultSuccessUrl("/", true)
                        .failureUrl("/login?error=true")
                        .permitAll())

                // Enable logout
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout=true")
                        .permitAll())

                // Set authentication provider
                .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
