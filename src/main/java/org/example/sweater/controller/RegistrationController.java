package org.example.sweater.controller;

import org.example.sweater.domain.User;
import org.example.sweater.domain.dto.CaptchaResponseDto;
import org.example.sweater.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

import javax.validation.Valid;
import java.util.Collections;
import java.util.Map;

@Controller
public class RegistrationController {
    private final static String CAPTCHA_URL = "https://www.google.com/recaptcha/api/siteverify?secret=%s&response=%s";

    @Autowired
    private UserService userService;

    @Value("${recaptcha.secret}")
    private String secretKey;

    @Autowired
    private RestTemplate restTemplate;


    @GetMapping("/registration")
    public String registration() {
        return "registration";
    }

    @PostMapping("/registration")
    public String register(@RequestParam("passwordConfirmation") String passConfirm,
                           @RequestParam("g-recaptcha-response") String captchaResponse,
                           @Valid User user,
                           BindingResult bindingResult,
                           Model model) {

        String url = String.format(CAPTCHA_URL, secretKey, captchaResponse);
        CaptchaResponseDto responseDto =
                restTemplate.postForObject(url, Collections.emptyList(), CaptchaResponseDto.class);
        if (!responseDto.isSuccess()) {
            model.addAttribute("captchaError", "Fill captcha");
        }

        boolean isConfirmationEmpty = StringUtils.isEmpty(passConfirm);
        if (isConfirmationEmpty) {
            model.addAttribute("passwordConfirmationError", "Please confirm your password!");
        }
        if (user.getPassword() != null && !user.getPassword().equals(passConfirm)) {
            model.addAttribute("passwordError", "Password are different!");
        }

        if (isConfirmationEmpty || bindingResult.hasErrors() || !responseDto.isSuccess()) {
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
            model.addAttribute("messageType", "success");
            model.addAttribute("message", "User was successfully activated");
        } else {
            model.addAttribute("messageType", "danger");
            model.addAttribute("message", "Activation code is not found");
        }
        return "login";
    }

}
