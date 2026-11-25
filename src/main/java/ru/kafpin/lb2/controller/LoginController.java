package ru.kafpin.lb2.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {
    @GetMapping("/")
    public String home() {
        return "redirect:/schools";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @RequestMapping("/forbidden")
    public String forbidden() {
        return "forbidden";
    }
}
