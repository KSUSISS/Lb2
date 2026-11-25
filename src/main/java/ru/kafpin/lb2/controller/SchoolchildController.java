package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kafpin.lb2.entity.Address;
import ru.kafpin.lb2.entity.School;
import ru.kafpin.lb2.entity.Schoolchild;
import ru.kafpin.lb2.repository.SchoolRepository;
import ru.kafpin.lb2.repository.SchoolchildRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/schoolchildren")
public class SchoolchildController {

    private final SchoolchildRepository schoolchildRepository;
    private final SchoolRepository schoolRepository;

    public SchoolchildController(
            SchoolchildRepository schoolchildRepository,
            SchoolRepository schoolRepository) {

        this.schoolchildRepository = schoolchildRepository;
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public String getAllSchoolchildren(Model model) {
        model.addAttribute("schoolchildren", schoolchildRepository.findAll());
        return "schoolchildren";
    }

    @GetMapping("/new")
    public String newSchoolchild(Model model) {
        Schoolchild schoolchild = new Schoolchild();
        schoolchild.setAddress(new Address());
        model.addAttribute("schoolchild", schoolchild);
        model.addAttribute("schools", schoolRepository.findAll());

        return "new_schoolchild";
    }
    @PostMapping("/new")
    public String saveSchoolchild(
            @ModelAttribute("schoolchild") Schoolchild schoolchild, BindingResult result,
            @RequestParam(required = false) Long schoolId, Model model) {
        School school = schoolId == null ? null : schoolRepository.findById(schoolId).orElse(null);
        if (school == null) {
            result.reject("school.required", "Выберите школу из списка.");
        }
        if (result.hasErrors()) {
            model.addAttribute("schools", schoolRepository.findAll());
            model.addAttribute("schoolId", schoolId);
            return "new_schoolchild";
        }
        schoolchild.setSchool(school);
        if (schoolchild.getAddress() == null) {
            schoolchild.setAddress(new Address());
        }
        schoolchildRepository.save(schoolchild);
        return "redirect:/schoolchildren";
    }

    @InitBinder("schoolchild")
    void bindSchoolchildForm(WebDataBinder binder) {
        binder.setAllowedFields(
                "lastName", "firstName", "middleName", "gender", "nationality", "height", "weight",
                "birthDate", "phone", "schoolClass", "address.postalCode", "address.country",
                "address.region", "address.district", "address.city", "address.street",
                "address.house", "address.apartment");
    }

    @GetMapping("/search/lastname")
    public String searchByLastName(
            @RequestParam String lastName,
            Model model) {

        if (lastName.isEmpty()) {
            return "redirect:/schoolchildren";
        }

        model.addAttribute(
                "schoolchildren",
                schoolchildRepository.findByLastNameContainsIgnoreCase(lastName)
        );

        return "schoolchildren";
    }
}
