package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import ru.kafpin.lb2.domain.Schoolboy;
import ru.kafpin.lb2.repository.SchoolboyRepository;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import java.util.Optional;

@Controller
public class MainController {

    private final SchoolboyRepository schoolboyRepository;

    public MainController(SchoolboyRepository schoolboyRepository) {
        this.schoolboyRepository = schoolboyRepository;
    }

    @GetMapping("/")
    public String mainPage(Model model) {
        model.addAttribute("schoolboys", schoolboyRepository.findAll());
        return "index";
    }

    @GetMapping("/add")
    public String addSchoolboy(Model model) {
        model.addAttribute("schoolboy", new Schoolboy());
        return "edit_schoolboy";
    }

    @GetMapping("/update/{id}")
    public String editSchoolboy(@PathVariable("id") Long id, Model model) {
        Optional<Schoolboy> optionalSchoolboy = schoolboyRepository.findById(id);
        if (optionalSchoolboy.isEmpty()) {
            return "redirect:/";
        }
        model.addAttribute("schoolboy", optionalSchoolboy.get());
        return "edit_schoolboy";
    }

    @PostMapping("/update")
    public String saveSchoolboy(@ModelAttribute Schoolboy schoolboy) {
        schoolboyRepository.save(schoolboy);
        return "redirect:/";
    }


    @GetMapping("/delete/{id}")
    public String deleteSchoolboy(@PathVariable("id") Long id) {
        if (schoolboyRepository.existsById(id)) {
            schoolboyRepository.deleteById(id);
        }
        return "redirect:/";
    }
}