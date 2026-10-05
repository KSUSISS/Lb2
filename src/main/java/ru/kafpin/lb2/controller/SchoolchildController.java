package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
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
        model.addAttribute("schoolchild", new Schoolchild());
        model.addAttribute("schools", schoolRepository.findAll());

        return "new_schoolchild";
    }
    @PostMapping("/new")
    public String saveSchoolchild(
            @ModelAttribute Schoolchild schoolchild) {

        schoolchildRepository.save(schoolchild);

        return "redirect:/schoolchildren";
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