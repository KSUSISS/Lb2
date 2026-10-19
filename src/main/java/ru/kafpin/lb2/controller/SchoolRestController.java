package ru.kafpin.lb2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kafpin.lb2.repository.SchoolRepository;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/api/schools")
public class SchoolRestController {

    private final SchoolRepository schoolRepository;

    public SchoolRestController(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public List<SchoolResponse> getAllSchools() {
        List<SchoolResponse> result = new ArrayList<>();

        schoolRepository.findAll().forEach(school -> {
            result.add(new SchoolResponse(
                    school.getId(),
                    school.getName()
            ));
        });

        return result;
    }

    public record SchoolResponse(long id, String name) {
    }
}