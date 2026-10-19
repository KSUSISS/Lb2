package ru.kafpin.lb2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kafpin.lb2.repository.SchoolchildRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schoolchildren")
public class SchoolchildRestController {

    private final SchoolchildRepository schoolchildRepository;

    public SchoolchildRestController(
            SchoolchildRepository schoolchildRepository
    ) {
        this.schoolchildRepository = schoolchildRepository;
    }

    @GetMapping
    public List<SchoolchildResponse> getAllSchoolchildren() {
        List<SchoolchildResponse> result = new ArrayList<>();

        schoolchildRepository.findAll().forEach(child -> {
            result.add(new SchoolchildResponse(
                    child.getId(),
                    child.getLastName(),
                    child.getFirstName(),
                    child.getMiddleName(),
                    child.getSchoolClass(),
                    child.getSchool().getId(),
                    child.getSchool().getName()
            ));
        });

        return result;
    }

    public record SchoolchildResponse(
            long id,
            String lastName,
            String firstName,
            String middleName,
            String schoolClass,
            long schoolId,
            String schoolName
    ) {
    }
}   