package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.kafpin.lb2.repository.SchoolRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import ru.kafpin.lb2.entity.School;

@Controller
@RequestMapping("/schools")
public class SchoolController {

    private final SchoolRepository schoolRepository;

    public SchoolController(SchoolRepository schoolRepository) {
        this.schoolRepository = schoolRepository;
    }

    @GetMapping
    public String getAllSchools(Model model) {
        model.addAttribute("schools", schoolRepository.findAll());
        return "schools";
    }
    @GetMapping("/new")
    public String newSchool(Model model) {
        model.addAttribute("school", new ru.kafpin.lb2.entity.School());
        return "new_school";
    }

    @PostMapping("/new")
    public String saveSchool(
            @org.springframework.web.bind.annotation.ModelAttribute School school) {

        schoolRepository.save(school);
        return "redirect:/schools";
    }
}