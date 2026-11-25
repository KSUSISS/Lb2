package ru.kafpin.lb2.service;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.kafpin.lb2.entity.AppUser;
import ru.kafpin.lb2.repository.UserRepository;

@Service
public class DbUserDetailsService implements UserDetailsService {
    private final UserRepository userRepository;

    public DbUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) {
        AppUser account = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Пользователь не найден"));
        return User.withUsername(account.username())
                .password(account.passwordHash())
                .authorities(account.role().name())
                .build();
    }
}
