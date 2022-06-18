package org.example.sweater.controller;

import org.example.sweater.domain.Role;
import org.example.sweater.domain.User;
import org.example.sweater.repos.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {

    @Autowired
    private UserRepository userRepo;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(User user,
                           Map<String, Object> model) {
        User userFromDB = userRepo.findByUsername(user.getUsername());
        if (userFromDB == null) {
            user.setActive(true);
            user.setRoles(Collections.singleton(Role.USER));
            userRepo.save(user);
            return "redirect:/login";
        }
        model.put("message","user exists");
        return "registration";
    }
}
