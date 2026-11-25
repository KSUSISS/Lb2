package ru.kafpin.lb2.config;

import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.security.crypto.password.PasswordEncoder;
import ru.kafpin.lb2.entity.Role;
import ru.kafpin.lb2.repository.UserRepository;

import java.nio.charset.StandardCharsets;

@Configuration
@Profile("lab-demo")
class DemoUsersConfig {
    @Bean
    ApplicationRunner initializeDemoUsers(UserRepository users, PasswordEncoder encoder, Environment env) {
        return args -> {
            String userPassword = passwordForNewAccount(users, env, "user", "LAB_USER_PASSWORD");
            String adminPassword = passwordForNewAccount(users, env, "admin", "LAB_ADMIN_PASSWORD");
            if (userPassword != null) {
                users.insert("user", encoder.encode(userPassword), Role.ROLE_USER);
            }
            if (adminPassword != null) {
                users.insert("admin", encoder.encode(adminPassword), Role.ROLE_ADMIN);
            }
        };
    }

    private String passwordForNewAccount(UserRepository users, Environment env, String username, String variable) {
        if (users.findByUsername(username).isPresent()) {
            return null;
        }
        String password = env.getProperty(variable);
        if (password == null || password.isBlank() || password.getBytes(StandardCharsets.UTF_8).length > 72) {
            throw new IllegalStateException("Укажите непустой локальный пароль до 72 байт в " + variable);
        }
        return password;
    }
}
