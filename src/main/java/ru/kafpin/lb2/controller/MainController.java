package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import ru.kafpin.lb2.repository.SchoolboyRepository;

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
}