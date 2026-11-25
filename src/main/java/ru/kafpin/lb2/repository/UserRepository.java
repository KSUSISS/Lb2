package ru.kafpin.lb2.repository;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.kafpin.lb2.entity.AppUser;
import ru.kafpin.lb2.entity.Role;

import java.util.Optional;

@Repository
public class UserRepository {
    private static final RowMapper<AppUser> USER_ROW_MAPPER = (row, index) -> new AppUser(
            row.getLong("id"),
            row.getString("username"),
            row.getString("password_hash"),
            Role.valueOf(row.getString("role"))
    );

    private final JdbcTemplate jdbcTemplate;

    public UserRepository(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Optional<AppUser> findByUsername(String username) {
        return jdbcTemplate.query("""
                SELECT id, username, password_hash, role
                FROM users WHERE username = ?
                """, USER_ROW_MAPPER, username).stream().findFirst();
    }

    public void insert(String username, String passwordHash, Role role) {
        jdbcTemplate.update("INSERT INTO users (username, password_hash, role) VALUES (?, ?, ?)",
                username, passwordHash, role.name());
    }
}
