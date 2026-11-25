package ru.kafpin.lb2.entity;

public record AppUser(long id, String username, String passwordHash, Role role) {
}
