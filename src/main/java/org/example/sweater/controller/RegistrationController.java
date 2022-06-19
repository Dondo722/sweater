package org.example.sweater.controller;

import org.example.sweater.domain.User;
import org.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import javax.validation.Valid;
import java.util.Map;

@Controller
public class RegistrationController {

    @Autowired
    private UserService userService;

    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@Valid User user,
                           BindingResult bindingResult,
                           Model model) {
        if (user.getPassword() != null && !user.getPassword().equals(user.getPasswordConfirmation())) {
            model.addAttribute("passwordError", "Password are different!");
        }

        if (bindingResult.hasErrors()) {
            Map<String, String> errorsMap = ControllerUtil.getErrorsMap(bindingResult);
            model.mergeAttributes(errorsMap);

            return "registration";
        }

        if (userService.addUser(user)) {
            return "redirect:/login";
        }
        model.addAttribute("usernameError", "user exists");
        return "registration";
    }

    @GetMapping("/activate/{code}")
    public String activate(Model model, @PathVariable String code) {
        boolean isActivated = userService.activateUser(code);

        if (isActivated) {
            model.addAttribute("message", "User was successfully activated");
        } else {
            model.addAttribute("message", "Activation code is not found");
        }
        return "login";
    }

}
