package ru.kafpin.lb2.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.kafpin.lb2.repository.SchoolchildRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.server.ResponseStatusException;
import ru.kafpin.lb2.entity.Schoolchild;
import org.springframework.web.bind.annotation.DeleteMapping;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import ru.kafpin.lb2.entity.Address;
import ru.kafpin.lb2.entity.School;
import ru.kafpin.lb2.repository.SchoolRepository;
import org.springframework.web.bind.annotation.PutMapping;
import java.util.Map;
import java.util.TreeMap;
import org.springframework.web.bind.annotation.PatchMapping;

import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

@RestController
@Transactional
@RequestMapping("/api/schoolchildren")
public class SchoolchildRestController {

    private final SchoolchildRepository schoolchildRepository;
    private final SchoolRepository schoolRepository;

    public SchoolchildRestController(
            SchoolchildRepository schoolchildRepository,
            SchoolRepository schoolRepository
    ) {
        this.schoolchildRepository = schoolchildRepository;
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public List<SchoolchildResponse> getAllSchoolchildren() {
        List<SchoolchildResponse> result = new ArrayList<>();

        schoolchildRepository.findAll().forEach(child -> {
            result.add(toResponse(child));
        });

        return result;
    }

    @GetMapping("/{id}")
    public SchoolchildResponse getSchoolchildById(@PathVariable Long id) {
        Schoolchild child = schoolchildRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школьник с ID " + id + " не найден"
                ));

        return toResponse(child);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SchoolchildResponse createSchoolchild(
            @Valid @RequestBody SchoolchildRequest request
    ) {
        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школа с ID " + request.schoolId() + " не найдена"
                ));

        Address address = new Address();
        address.setPostalCode(request.address().postalCode());
        address.setCountry(request.address().country());
        address.setRegion(request.address().region());
        address.setDistrict(request.address().district());
        address.setCity(request.address().city());
        address.setStreet(request.address().street());
        address.setHouse(request.address().house());
        address.setApartment(request.address().apartment());

        Schoolchild child = new Schoolchild();
        child.setLastName(request.lastName());
        child.setFirstName(request.firstName());
        child.setMiddleName(request.middleName());
        child.setGender(request.gender());
        child.setNationality(request.nationality());
        child.setHeight(request.height());
        child.setWeight(request.weight());
        child.setBirthDate(request.birthDate());
        child.setPhone(request.phone());
        child.setSchoolClass(request.schoolClass());
        child.setSchool(school);
        child.setAddress(address);

        Schoolchild savedChild = schoolchildRepository.save(child);

        return toResponse(savedChild);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSchoolchild(@PathVariable Long id) {
        Schoolchild child = schoolchildRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школьник с ID " + id + " не найден"
                ));

        schoolchildRepository.delete(child);
    }

    @PutMapping("/{id}")
    public SchoolchildResponse updateSchoolchild(
            @PathVariable Long id,
            @Valid @RequestBody SchoolchildRequest request
    ) {
        Schoolchild child = schoolchildRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школьник с ID " + id + " не найден"
                ));

        School school = schoolRepository.findById(request.schoolId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школа с ID " + request.schoolId() + " не найдена"
                ));

        Address address = child.getAddress();
        if (address == null) {
            address = new Address();
            child.setAddress(address);
        }
        address.setPostalCode(request.address().postalCode());
        address.setCountry(request.address().country());
        address.setRegion(request.address().region());
        address.setDistrict(request.address().district());
        address.setCity(request.address().city());
        address.setStreet(request.address().street());
        address.setHouse(request.address().house());
        address.setApartment(request.address().apartment());

        child.setLastName(request.lastName());
        child.setFirstName(request.firstName());
        child.setMiddleName(request.middleName());
        child.setGender(request.gender());
        child.setNationality(request.nationality());
        child.setHeight(request.height());
        child.setWeight(request.weight());
        child.setBirthDate(request.birthDate());
        child.setPhone(request.phone());
        child.setSchoolClass(request.schoolClass());
        child.setSchool(school);

        Schoolchild savedChild = schoolchildRepository.save(child);

        return toResponse(savedChild);
    }

    @GetMapping("/statistics")
    public SchoolchildStatistics getStatistics() {
        long total = 0;
        Map<Long, Long> bySchool = new TreeMap<>();
        Map<String, Long> byClass = new TreeMap<>();

        for (Schoolchild child : schoolchildRepository.findAll()) {
            total++;

            Long schoolId = child.getSchool().getId();
            bySchool.merge(schoolId, 1L, Long::sum);

            String schoolClass = child.getSchoolClass();
            if (schoolClass != null && !schoolClass.isBlank()) {
                byClass.merge(schoolClass, 1L, Long::sum);
            }
        }

        return new SchoolchildStatistics(total, bySchool, byClass);
    }

    public record SchoolchildStatistics(
            long total,
            Map<Long, Long> bySchool,
            Map<String, Long> byClass
    ) {
    }


    @PatchMapping("/{id}")
    public SchoolchildResponse partiallyUpdateSchoolchild(
            @PathVariable Long id,
            @RequestBody SchoolchildPatchRequest request
    ) {
        Schoolchild child = schoolchildRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "Школьник с ID " + id + " не найден"
                ));

        if (request.phone() == null && request.schoolClass() == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Передайте телефон или класс"
            );
        }

        if (request.phone() != null && request.phone().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Телефон не должен быть пустым"
            );
        }

        if (request.schoolClass() != null && request.schoolClass().isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Класс не должен быть пустым"
            );
        }

        if (request.phone() != null) {
            child.setPhone(request.phone().trim());
        }

        if (request.schoolClass() != null) {
            child.setSchoolClass(request.schoolClass().trim());
        }

        Schoolchild savedChild = schoolchildRepository.save(child);

        return toResponse(savedChild);
    }
    public record SchoolchildPatchRequest(
            String phone,
            String schoolClass
    ) {
    }

    private SchoolchildResponse toResponse(Schoolchild child) {
        Address address = child.getAddress();
        AddressResponse addressResponse = address == null ? null : new AddressResponse(
                address.getId(), address.getPostalCode(), address.getCountry(),
                address.getRegion(), address.getDistrict(), address.getCity(),
                address.getStreet(), address.getHouse(), address.getApartment()
        );
        return new SchoolchildResponse(
                child.getId(), child.getLastName(), child.getFirstName(),
                child.getMiddleName(), child.getGender(), child.getNationality(),
                child.getHeight(), child.getWeight(), child.getBirthDate(),
                child.getPhone(), child.getSchoolClass(), child.getSchool().getId(),
                child.getSchool().getName(), addressResponse
        );
    }

    public record AddressResponse(
            Long id, String postalCode, String country, String region,
            String district, String city, String street, String house, String apartment
    ) {
    }

    public record SchoolchildResponse(
            long id, String lastName, String firstName, String middleName,
            String gender, String nationality, int height, int weight,
            LocalDate birthDate, String phone, String schoolClass,
            long schoolId, String schoolName, AddressResponse address
    ) {
    }
}
