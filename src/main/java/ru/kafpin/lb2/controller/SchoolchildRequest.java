package ru.kafpin.lb2.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Positive;

import java.time.LocalDate;

public record SchoolchildRequest(
        @NotBlank String lastName,
        @NotBlank String firstName,
        String middleName,
        @NotBlank String gender,
        @NotBlank String nationality,
        @Positive int height,
        @Positive int weight,
        @NotNull @Past LocalDate birthDate,
        @NotBlank String phone,
        @NotBlank String schoolClass,
        @NotNull @Positive Long schoolId,
        @NotNull @Valid AddressRequest address
) {

    public record AddressRequest(
            @NotBlank String postalCode,
            @NotBlank String country,
            @NotBlank String region,
            String district,
            @NotBlank String city,
            @NotBlank String street,
            @NotBlank String house,
            String apartment
    ) {
    }
}