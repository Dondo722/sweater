package org.example.sweater.controller;

import org.example.sweater.domain.Message;
import org.example.sweater.domain.Role;
import org.example.sweater.domain.User;
import org.example.sweater.domain.dto.MessageDto;
import org.example.sweater.repos.MessageRepository;
import org.example.sweater.service.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;

import javax.validation.Valid;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Controller
public class MessageController {
    @Autowired
    private MessageRepository messageRepository;

    @Autowired
    private MessageService messageService;

    @Value("${upload.path}")
    private String uploadPath;

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/main")
    public String main(@RequestParam(required = false, defaultValue = "") String filter,
                       Model model,
                       @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable,
                       @AuthenticationPrincipal User user) {
        Page<MessageDto> page = messageService.messageList(pageable, filter, user);

        model.addAttribute("url", "/main");
        model.addAttribute("page", page);
        model.addAttribute("filter", filter);
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
                               @PathVariable(name = "id") User author,
                               Model model,
                               @RequestParam(required = false) Message message,
                               @PageableDefault(sort = {"id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<MessageDto> page = messageService.messageListForUser(pageable, author, currentUser);
        model.addAttribute("userChannel", author);
        model.addAttribute("subscriptionsCount", author.getSubscriptions().size());
        model.addAttribute("subscribersCount", author.getSubscribers().size());
        model.addAttribute("isSubscriber", author.getSubscribers().contains(currentUser));
        model.addAttribute("message", message);
        model.addAttribute("isCurrentUser", currentUser.equals(author));
        model.addAttribute("page", page);
        model.addAttribute("url", "/user-messages/" + author.getId());

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
            saveFile(messageFromId, file);
            messageRepository.save(messageFromId);
        }

        return "redirect:/user-messages/" + userId;
    }

    @GetMapping("/messages/{message}/like")
    public String like(@AuthenticationPrincipal User currentUser,
                       @PathVariable Message message,
                       RedirectAttributes redirectAttributes,
                       @RequestHeader(required = false) String referer) {
        Set<User> likes = message.getLikes();
        if (likes.contains(currentUser)) {
            likes.remove(currentUser);
        } else {
            likes.add(currentUser);
        }

        UriComponents components = getUriComponents(redirectAttributes, referer);

        return "redirect:" + components.getPath();
    }

    @GetMapping("/messages/{user}/{message}/delete")
    public String deleteUserMessage(
            @AuthenticationPrincipal User currentUser,
            @PathVariable Message message,
            @PathVariable User user,
            RedirectAttributes redirectAttributes,
            @RequestHeader(required = false) String referer) {
        if (currentUser.getId().equals(user.getId()) || currentUser.getRoles().contains(Role.ADMIN)) {
            messageRepository.deleteById(message.getId());
        }
        UriComponents components = getUriComponents(redirectAttributes, referer);
        return "redirect:" + components.getPath();
    }


    private UriComponents getUriComponents(RedirectAttributes redirectAttributes, @RequestHeader(required = false) String referer) {
        UriComponents components = UriComponentsBuilder.fromHttpUrl(referer).build();
        components.getQueryParams()
                .entrySet()
                .forEach(pair -> redirectAttributes.addAttribute(pair.getKey(), pair.getValue()));
        return components;
    }
}
