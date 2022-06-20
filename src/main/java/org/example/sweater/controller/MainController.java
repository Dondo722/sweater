package org.example.sweater.controller;

import org.example.sweater.domain.Message;
import org.example.sweater.domain.User;
import org.example.sweater.repos.MessageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MainController {
    @Autowired
    private MessageRepository messageRepository;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter, Map<String, Object> model) {
        Iterable<Message> messages;
        if (filter.isEmpty()) {
            messages = messageRepository.findAll();
        } else {
            messages = messageRepository.findByTag(filter);
        }
        model.put("messages", messages);
        model.put("filter", filter);
        return "main";
    }

    @PostMapping("/main")
    public String add(@AuthenticationPrincipal User user,
                      @Valid Message message,
                      BindingResult bindingResult,
                      Model model,
                      @RequestParam MultipartFile file)
            throws IOException {
        message.setAuthor(user);

        if (bindingResult.hasErrors()) {
            Map<String, String> errorsMap = ControllerUtil.getErrorsMap(bindingResult);
            model.mergeAttributes(errorsMap);
            model.addAttribute("message", message);
        } else {
            saveFile(message, file);
            model.addAttribute("message", null);

            messageRepository.save(message);
        }

        Iterable<Message> messages = messageRepository.findAll();
        model.addAttribute("messages", messages);

        return "main";
    }

    private void saveFile(Message message, MultipartFile file) throws IOException {
        if (!file.isEmpty()) {
            File uploadDir = new File(uploadPath);
            if (!uploadDir.exists()) {
                uploadDir.mkdir();
            }
            String uuuidFile = UUID.randomUUID().toString();
            String finalFileName = uuuidFile + "." + file.getOriginalFilename();
            message.setFilename(finalFileName);

            file.transferTo(new File(uploadPath + "/" + finalFileName));
        }
    }

    @GetMapping("/user-messages/{id}")
    public String userMessages(@AuthenticationPrincipal User currentUser,
                               @PathVariable(name = "id") User user,
                               Model model,
                               @RequestParam(required = false) Message message) {
        if (message != null) {
            model.addAttribute("message", message);
        }
        Set<Message> messages = user.getMessages();
        model.addAttribute("messages", messages);
        model.addAttribute("isCurrentUser", currentUser.equals(user));

        return "userMessages";
    }

    @PostMapping("/user-messages/{userId}")
    public String updateMessage(@AuthenticationPrincipal User currentUser,
                                @PathVariable Long userId,
                                @RequestParam("id") Message messageFromId,
                                @RequestParam("text") String text,
                                @RequestParam("tag") String tag,
                                @RequestParam MultipartFile file) throws IOException {
        if (messageFromId.getAuthor().equals(currentUser)) {
            if (!StringUtils.isEmpty(text)) {
                messageFromId.setText(text);
            }
            if (!StringUtils.isEmpty(tag)) {
                messageFromId.setTag(tag);
            }
            saveFile(messageFromId,file);
            messageRepository.save(messageFromId);
        }

        return "redirect:/user-messages/" + userId;
    }

}
