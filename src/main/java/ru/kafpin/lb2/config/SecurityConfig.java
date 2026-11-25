package ru.kafpin.lb2.config;

import jakarta.servlet.DispatcherType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        .dispatcherTypeMatchers(DispatcherType.FORWARD, DispatcherType.ERROR).permitAll()
                        .requestMatchers("/login", "/css/**").permitAll()
                        .requestMatchers("/api/**", "/h2-console/**").denyAll()
                        .requestMatchers("/schools/new", "/schoolchildren/new").hasRole("ADMIN")
                        .anyRequest().hasAnyRole("USER", "ADMIN"))
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/schools")
                        .failureUrl("/login?error")
                        .permitAll())
                .logout(logout -> logout.logoutSuccessUrl("/login?logout"))
                .exceptionHandling(errors -> errors.accessDeniedPage("/forbidden"))
                .build();
    }

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
