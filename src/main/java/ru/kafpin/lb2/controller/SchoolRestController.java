package ru.kafpin.lb2.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ru.kafpin.lb2.entity.School;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
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

    @GetMapping("/{id}")
    public SchoolResponse getSchoolById(@PathVariable Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школа с ID " + id + " не найдена"
                ));

        return new SchoolResponse(school.getId(), school.getName());
    }



    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolResponse createSchool(@RequestBody SchoolRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название школы не должно быть пустым"
            );
        }

        School school = new School();
        school.setName(request.name().trim());

        School savedSchool = schoolRepository.save(school);

        return new SchoolResponse(
                savedSchool.getId(),
                savedSchool.getName()
        );
    }

    @PutMapping("/{id}")
    public SchoolResponse updateSchool(
            @PathVariable Long id,
            @RequestBody SchoolRequest request
    ) {
        if (request.name() == null || request.name().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Название школы не должно быть пустым"
            );
        }

        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школа с ID " + id + " не найдена"
                ));

        school.setName(request.name().trim());

        School savedSchool = schoolRepository.save(school);

        return new SchoolResponse(
                savedSchool.getId(),
                savedSchool.getName()
        );
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchool(@PathVariable Long id) {
        School school = schoolRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школа с ID " + id + " не найдена"
                ));

        if (school.getSchoolchildren() != null
                && !school.getSchoolchildren().isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Нельзя удалить школу, в которой есть школьники"
            );
        }

        schoolRepository.delete(school);
    }

    public record SchoolResponse(long id, String name) {
    }



    public record SchoolRequest(String name) {
    }

}