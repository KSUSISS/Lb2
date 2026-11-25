package ru.kafpin.lb2.controller;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SchoolForm(
        @NotBlank(message = "Введите название школы.")
        @Size(max = 255, message = "Название должно быть не длиннее 255 символов.")
        String name
) {
}
